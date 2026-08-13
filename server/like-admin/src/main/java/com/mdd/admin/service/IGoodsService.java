package com.mdd.admin.service;

import com.mdd.admin.validate.goods.GoodsCreateValidate;
import com.mdd.admin.validate.goods.GoodsSearchValidate;
import com.mdd.admin.validate.goods.GoodsUpdateValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.goods.GoodsDetailVo;
import com.mdd.admin.vo.goods.GoodsListedVo;
import com.mdd.common.core.PageResult;

/**
 * 商品服务接口类
 */
public interface IGoodsService {

    /**
     * 商品列表
     *
     * @author fzr
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<GoodsListedVo>
     */
    PageResult<GoodsListedVo> list(PageValidate pageValidate, GoodsSearchValidate searchValidate);

    /**
     * 商品详情
     *
     * @author fzr
     * @param id 主键ID
     */
    GoodsDetailVo detail(Integer id);

    /**
     * 商品新增
     *
     * @author fzr
     * @param createValidate 参数
     */
    void add(GoodsCreateValidate createValidate);

    /**
     * 商品编辑
     *
     * @author fzr
     * @param updateValidate 参数
     */
    void edit(GoodsUpdateValidate updateValidate);

    /**
     * 商品删除
     *
     * @author fzr
     * @param id 商品主键
     */
    void del(Integer id);

    /**
     * 商品上下架
     *
     * @author fzr
     * @param id 商品主键
     */
    void change(Integer id);

}