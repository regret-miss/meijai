package com.mdd.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.query.MPJQueryWrapper;
import com.mdd.admin.service.IGoodsService;
import com.mdd.admin.validate.goods.GoodsCreateValidate;
import com.mdd.admin.validate.goods.GoodsSearchValidate;
import com.mdd.admin.validate.goods.GoodsUpdateValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.goods.GoodsDetailVo;
import com.mdd.admin.vo.goods.GoodsListedVo;
import com.mdd.admin.vo.goods.GoodsSkuVo;
import com.mdd.common.config.GlobalConfig;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.goods.Goods;
import com.mdd.common.entity.goods.GoodsCategory;
import com.mdd.common.entity.goods.GoodsSku;
import com.mdd.common.mapper.goods.GoodsCategoryMapper;
import com.mdd.common.mapper.goods.GoodsMapper;
import com.mdd.common.mapper.goods.GoodsSkuMapper;
import com.mdd.common.util.StringUtils;
import com.mdd.common.util.TimeUtils;
import com.mdd.common.util.UrlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class GoodsServiceImpl implements IGoodsService {

    @Resource
    GoodsMapper goodsMapper;

    @Resource
    GoodsSkuMapper goodsSkuMapper;

    @Resource
    GoodsCategoryMapper goodsCategoryMapper;

    /**
     * 商品列表
     *
     * @author fzr
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<GoodsListedVo>
     */
    @Override
    public PageResult<GoodsListedVo> list(PageValidate pageValidate, GoodsSearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPageNo();
        Integer pageSize = pageValidate.getPageSize();

        MPJQueryWrapper<Goods> mpjQueryWrapper = new MPJQueryWrapper<Goods>()
                .selectAll(Goods.class)
                .select("gc.name as category")
                .innerJoin("?_goods_category gc ON gc.id=t.category_id".replace("?_", GlobalConfig.tablePrefix))
                .eq("t.is_delete", 0)
                .orderByDesc(Arrays.asList("t.sort", "t.id"));

        goodsMapper.setSearch(mpjQueryWrapper, searchValidate, new String[]{
                "like:name@t.name:str",
                "=:categoryId@t.category_id:int",
                "=:isShow@t.is_show:int",
                "datetime:startTime-endTime@t.create_time:str"
        });

        IPage<GoodsListedVo> iPage = goodsMapper.selectJoinPage(
                new Page<>(pageNo, pageSize),
                GoodsListedVo.class,
                mpjQueryWrapper);

        for (GoodsListedVo vo : iPage.getRecords()) {
            vo.setImage(UrlUtils.toAbsoluteUrl(vo.getImage()));
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
        }

        return PageResult.iPageHandle(iPage);
    }

    /**
     * 商品详情
     *
     * @author fzr
     * @param id 主键ID
     */
    @Override
    public GoodsDetailVo detail(Integer id) {
        Goods model = goodsMapper.selectOne(
                new QueryWrapper<Goods>()
                        .select(Goods.class, info->
                          !info.getColumn().equals("is_delete") &&
                          !info.getColumn().equals("delete_time"))
                        .eq("id", id)
                        .eq("is_delete", 0));

        Assert.notNull(model, "商品不存在");

        GoodsDetailVo vo = new GoodsDetailVo();
        BeanUtils.copyProperties(model, vo);
        vo.setContent(StringUtils.isNull(model.getContent()) ? "" : model.getContent());
        vo.setImages(StringUtils.isNull(model.getImages()) ? "" : model.getImages());
        vo.setImage(UrlUtils.toAbsoluteUrl(model.getImage()));
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));

        // 查询SKU列表
        List<GoodsSku> skuList = goodsSkuMapper.selectList(
                new QueryWrapper<GoodsSku>()
                        .eq("goods_id", id)
                        .eq("is_delete", 0));

        List<GoodsSkuVo> skuVoList = new ArrayList<>();
        for (GoodsSku sku : skuList) {
            GoodsSkuVo skuVo = new GoodsSkuVo();
            BeanUtils.copyProperties(sku, skuVo);
            skuVo.setImage(UrlUtils.toAbsoluteUrl(skuVo.getImage()));
            skuVoList.add(skuVo);
        }
        vo.setSkuList(skuVoList);

        return vo;
    }

    /**
     * 商品新增
     *
     * @author fzr
     * @param createValidate 商品参数
     */
    @Override
    @Transactional
    public void add(GoodsCreateValidate createValidate) {
        // 校验分类
        Assert.notNull(goodsCategoryMapper.selectOne(
                new QueryWrapper<GoodsCategory>()
                .eq("id", createValidate.getCategoryId())
                .eq("is_delete", 0)), "分类不存在");

        Goods model = new Goods();
        model.setCategoryId(createValidate.getCategoryId());
        model.setName(createValidate.getName());
        model.setImage(UrlUtils.toRelativeUrl(createValidate.getImage()));
        model.setImages(createValidate.getImages());
        model.setContent(createValidate.getContent());
        model.setSort(createValidate.getSort());
        model.setIsShow(createValidate.getIsShow());
        model.setSalesVirtual(createValidate.getSalesVirtual());
        model.setCreateTime(TimeUtils.timestamp());
        model.setUpdateTime(TimeUtils.timestamp());

        // 计算价格和库存
        calcPriceAndStock(model, createValidate.getSkuList(), true);

        goodsMapper.insert(model);

        // 保存SKU
        saveSkuList(model.getId(), createValidate.getSkuList());
    }

    /**
     * 商品编辑
     *
     * @author fzr
     * @param updateValidate 商品参数
     */
    @Override
    @Transactional
    public void edit(GoodsUpdateValidate updateValidate) {
        Goods model = goodsMapper.selectOne(
                new QueryWrapper<Goods>()
                .eq("id", updateValidate.getId())
                .eq("is_delete", 0));

        Assert.notNull(model, "商品不存在!");

        Assert.notNull(goodsCategoryMapper.selectOne(
                new QueryWrapper<GoodsCategory>()
                .eq("id", updateValidate.getCategoryId())
                .eq("is_delete", 0)), "分类不存在");

        model.setCategoryId(updateValidate.getCategoryId());
        model.setName(updateValidate.getName());
        model.setImage(UrlUtils.toRelativeUrl(updateValidate.getImage()));
        model.setImages(updateValidate.getImages());
        model.setContent(updateValidate.getContent());
        model.setSort(updateValidate.getSort());
        model.setIsShow(updateValidate.getIsShow());
        model.setSalesVirtual(updateValidate.getSalesVirtual());
        model.setUpdateTime(TimeUtils.timestamp());

        // 重新计算价格和库存
        calcPriceAndStock(model, updateValidate.getSkuList(), true);

        goodsMapper.updateById(model);

        // 删除旧的SKU
        GoodsSku updateSku = new GoodsSku();
        updateSku.setIsDelete(1);
        updateSku.setDeleteTime(TimeUtils.timestamp());
        goodsSkuMapper.update(updateSku,
                new QueryWrapper<GoodsSku>()
                        .eq("goods_id", model.getId())
                        .eq("is_delete", 0));

        // 保存新的SKU
        saveSkuList(model.getId(), updateValidate.getSkuList());
    }

    /**
     * 商品删除
     *
     * @author fzr
     * @param id 商品ID
     */
    @Override
    @Transactional
    public void del(Integer id) {
        Goods goods = goodsMapper.selectOne(
                new QueryWrapper<Goods>()
                        .select("id,is_show")
                        .eq("id", id)
                        .eq("is_delete", 0));

        Assert.notNull(goods, "商品不存在!");

        goods.setIsDelete(1);
        goods.setDeleteTime(TimeUtils.timestamp());
        goodsMapper.updateById(goods);

        // 删除关联SKU
        GoodsSku updateSku = new GoodsSku();
        updateSku.setIsDelete(1);
        updateSku.setDeleteTime(TimeUtils.timestamp());
        goodsSkuMapper.update(updateSku,
                new QueryWrapper<GoodsSku>()
                        .eq("goods_id", id)
                        .eq("is_delete", 0));
    }

    /**
     * 商品上下架
     *
     * @author fzr
     * @param id 商品主键
     */
    @Override
    public void change(Integer id) {
        Goods goods = goodsMapper.selectOne(
                new QueryWrapper<Goods>()
                        .select("id,is_show")
                        .eq("id", id)
                        .eq("is_delete", 0));

        Assert.notNull(goods, "商品不存在!");

        goods.setIsShow(goods.getIsShow()==0?1:0);
        goods.setUpdateTime(TimeUtils.timestamp());
        goodsMapper.updateById(goods);
    }

    /**
     * 计算价格和库存
     */
    private void calcPriceAndStock(Goods goods, List<?> skuList, boolean isSave) {
        if (skuList == null || skuList.isEmpty()) {
            goods.setPriceMin(BigDecimal.ZERO);
            goods.setPriceMax(BigDecimal.ZERO);
            goods.setCostPrice(BigDecimal.ZERO);
            goods.setStockTotal(0);
            return;
        }

        BigDecimal minPrice = null;
        BigDecimal maxPrice = BigDecimal.ZERO;
        BigDecimal minCostPrice = null;
        int totalStock = 0;

        for (Object obj : skuList) {
            BigDecimal price = null;
            BigDecimal costPrice = BigDecimal.ZERO;
            int stock = 0;

            if (obj instanceof GoodsCreateValidate.GoodsSkuValidate) {
                GoodsCreateValidate.GoodsSkuValidate sku = (GoodsCreateValidate.GoodsSkuValidate) obj;
                price = sku.getPrice();
                costPrice = sku.getCostPrice() != null ? sku.getCostPrice() : BigDecimal.ZERO;
                stock = sku.getStock();
            } else if (obj instanceof GoodsUpdateValidate.GoodsSkuValidate) {
                GoodsUpdateValidate.GoodsSkuValidate sku = (GoodsUpdateValidate.GoodsSkuValidate) obj;
                price = sku.getPrice();
                costPrice = sku.getCostPrice() != null ? sku.getCostPrice() : BigDecimal.ZERO;
                stock = sku.getStock();
            }

            if (price != null) {
                if (minPrice == null || price.compareTo(minPrice) < 0) {
                    minPrice = price;
                }
                if (price.compareTo(maxPrice) > 0) {
                    maxPrice = price;
                }
            }

            if (minCostPrice == null || costPrice.compareTo(minCostPrice) < 0) {
                minCostPrice = costPrice;
            }

            totalStock += stock;
        }

        goods.setPriceMin(minPrice != null ? minPrice : BigDecimal.ZERO);
        goods.setPriceMax(maxPrice);
        goods.setCostPrice(minCostPrice != null ? minCostPrice : BigDecimal.ZERO);
        goods.setStockTotal(totalStock);
    }

    /**
     * 保存SKU列表
     */
    private void saveSkuList(Integer goodsId, List<?> skuList) {
        if (skuList == null || skuList.isEmpty()) {
            return;
        }

        for (Object obj : skuList) {
            GoodsSku sku = new GoodsSku();
            sku.setGoodsId(goodsId);
            sku.setCreateTime(TimeUtils.timestamp());
            sku.setUpdateTime(TimeUtils.timestamp());

            if (obj instanceof GoodsCreateValidate.GoodsSkuValidate) {
                GoodsCreateValidate.GoodsSkuValidate sv = (GoodsCreateValidate.GoodsSkuValidate) obj;
                sku.setSkuName(sv.getSkuName());
                sku.setImage(UrlUtils.toRelativeUrl(sv.getImage()));
                sku.setPrice(sv.getPrice());
                sku.setCostPrice(sv.getCostPrice() != null ? sv.getCostPrice() : BigDecimal.ZERO);
                sku.setStock(sv.getStock());
                sku.setSkuData(sv.getSkuData());
            } else if (obj instanceof GoodsUpdateValidate.GoodsSkuValidate) {
                GoodsUpdateValidate.GoodsSkuValidate sv = (GoodsUpdateValidate.GoodsSkuValidate) obj;
                sku.setSkuName(sv.getSkuName());
                sku.setImage(UrlUtils.toRelativeUrl(sv.getImage()));
                sku.setPrice(sv.getPrice());
                sku.setCostPrice(sv.getCostPrice() != null ? sv.getCostPrice() : BigDecimal.ZERO);
                sku.setStock(sv.getStock());
                sku.setSkuData(sv.getSkuData());
            }

            goodsSkuMapper.insert(sku);
        }
    }

}