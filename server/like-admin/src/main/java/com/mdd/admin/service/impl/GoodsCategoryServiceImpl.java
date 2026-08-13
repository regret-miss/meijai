package com.mdd.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mdd.admin.service.IGoodsCategoryService;
import com.mdd.admin.validate.goods.GoodsCategoryCreateValidate;
import com.mdd.admin.validate.goods.GoodsCategoryUpdateValidate;
import com.mdd.admin.validate.goods.GoodsCategorySearchValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.goods.GoodsCategoryVo;
import com.mdd.common.core.PageResult;
import com.mdd.common.entity.goods.Goods;
import com.mdd.common.entity.goods.GoodsCategory;
import com.mdd.common.mapper.goods.GoodsCategoryMapper;
import com.mdd.common.mapper.goods.GoodsMapper;
import com.mdd.common.util.TimeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商品分类服务实现类
 */
@Service
public class GoodsCategoryServiceImpl implements IGoodsCategoryService {

    @Resource
    GoodsCategoryMapper goodsCategoryMapper;

    @Resource
    GoodsMapper goodsMapper;

    /**
     * 分类所有
     *
     * @author fzr
     * @return List<GoodsCategoryVo>
     */
    @Override
    public List<GoodsCategoryVo> all() {
        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name", "sort", "is_show", "create_time", "update_time")
                .eq("is_delete", 0)
                .orderByDesc(Arrays.asList("sort", "id"));

        List<GoodsCategory> lists = goodsCategoryMapper.selectList(queryWrapper);

        List<GoodsCategoryVo> vos = new ArrayList<>();
        for (GoodsCategory category : lists) {
            GoodsCategoryVo vo = new GoodsCategoryVo();
            BeanUtils.copyProperties(category, vo);

            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            vos.add(vo);
        }

        return vos;
    }

    /**
     * 分类列表
     *
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<GoodsCategoryVo>
     */
    @Override
    public PageResult<GoodsCategoryVo> list(PageValidate pageValidate, GoodsCategorySearchValidate searchValidate) {
        Integer pageNo   = pageValidate.getPageNo();
        Integer pageSize = pageValidate.getPageSize();

        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "name", "sort", "is_show", "create_time", "update_time")
                .eq("is_delete", 0)
                .orderByDesc(Arrays.asList("sort", "id"));

        goodsCategoryMapper.setSearch(queryWrapper, searchValidate, new String[]{
                "like:name:str",
                "=:isShow@is_show:int"
        });

        IPage<GoodsCategory> iPage = goodsCategoryMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);

        List<GoodsCategoryVo> list = new ArrayList<>();
        for (GoodsCategory category : iPage.getRecords()) {
            GoodsCategoryVo vo = new GoodsCategoryVo();
            BeanUtils.copyProperties(category, vo);

            Long number = goodsMapper.selectCount(new QueryWrapper<Goods>()
                    .eq("category_id", category.getId())
                    .eq("is_delete", 0));

            vo.setNumber(number);
            vo.setCreateTime(TimeUtils.timestampToDate(vo.getCreateTime()));
            vo.setUpdateTime(TimeUtils.timestampToDate(vo.getUpdateTime()));
            list.add(vo);
        }

        return PageResult.iPageHandle(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), list);
    }

    /**
     * 分类详情
     *
     * @author fzr
     * @param id 分类ID
     * @return GoodsCategoryVo
     */
    @Override
    public GoodsCategoryVo detail(Integer id) {
        GoodsCategory model = goodsCategoryMapper.selectOne(
                new QueryWrapper<GoodsCategory>()
                        .select(GoodsCategory.class, info->
                          !info.getColumn().equals("is_delete") &&
                          !info.getColumn().equals("delete_time"))
                        .eq("id", id)
                        .eq("is_delete", 0));

        Assert.notNull(model, "分类不存在");

        GoodsCategoryVo vo = new GoodsCategoryVo();
        BeanUtils.copyProperties(model, vo);
        vo.setCreateTime(TimeUtils.timestampToDate(model.getCreateTime()));
        vo.setUpdateTime(TimeUtils.timestampToDate(model.getUpdateTime()));

        return vo;
    }

    /**
     * 分类新增
     *
     * @author fzr
     * @param createValidate 分类参数
     */
    @Override
    public void add(GoodsCategoryCreateValidate createValidate) {
        GoodsCategory model = new GoodsCategory();
        model.setName(createValidate.getName());
        model.setSort(createValidate.getSort());
        model.setIsShow(createValidate.getIsShow());
        model.setCreateTime(TimeUtils.timestamp());
        model.setUpdateTime(TimeUtils.timestamp());
        goodsCategoryMapper.insert(model);
    }

    /**
     * 分类编辑
     *
     * @author fzr
     * @param updateValidate 参数
     */
    @Override
    public void edit(GoodsCategoryUpdateValidate updateValidate) {
        GoodsCategory model = goodsCategoryMapper.selectOne(
                new QueryWrapper<GoodsCategory>()
                        .select(GoodsCategory.class, info->
                           !info.getColumn().equals("is_delete") &&
                           !info.getColumn().equals("delete_time"))
                        .eq("id", updateValidate.getId())
                        .eq("is_delete", 0));

        Assert.notNull(model, "分类不存在");

        model.setName(updateValidate.getName());
        model.setSort(updateValidate.getSort());
        model.setIsShow(updateValidate.getIsShow());
        model.setUpdateTime(TimeUtils.timestamp());
        goodsCategoryMapper.updateById(model);
    }

    /**
     * 分类删除
     *
     * @author fzr
     * @param id 分类ID
     */
    @Override
    public void del(Integer id) {
        GoodsCategory model = goodsCategoryMapper.selectOne(
                new QueryWrapper<GoodsCategory>()
                        .select("id,is_show")
                        .eq("id", id)
                        .eq("is_delete", 0));

        Assert.notNull(model, "分类不存在");

        Goods goods = goodsMapper.selectOne(new QueryWrapper<Goods>()
                .eq("category_id", id)
                .eq("is_delete", 0)
                .last("limit 1"));

        Assert.isNull(goods, "当前分类已被商品使用,请先移除!");

        model.setIsDelete(1);
        model.setDeleteTime(TimeUtils.timestamp());
        goodsCategoryMapper.updateById(model);
    }

    /**
     * 分类状态
     *
     * @author fzr
     * @param id 分类ID
     */
    @Override
    public void change(Integer id) {
        GoodsCategory model = goodsCategoryMapper.selectOne(
                new QueryWrapper<GoodsCategory>()
                        .select("id,is_show")
                        .eq("id", id)
                        .eq("is_delete", 0));

        Assert.notNull(model, "分类不存在");

        model.setIsShow(model.getIsShow()==0?1:0);
        model.setUpdateTime(TimeUtils.timestamp());
        goodsCategoryMapper.updateById(model);
    }

}