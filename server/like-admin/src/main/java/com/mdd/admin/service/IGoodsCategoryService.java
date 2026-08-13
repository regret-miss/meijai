package com.mdd.admin.service;

import com.mdd.admin.validate.goods.GoodsCategoryCreateValidate;
import com.mdd.admin.validate.goods.GoodsCategoryUpdateValidate;
import com.mdd.admin.validate.goods.GoodsCategorySearchValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.goods.GoodsCategoryVo;
import com.mdd.common.core.PageResult;

import java.util.List;

/**
 * 商品分类服务接口类
 */
public interface IGoodsCategoryService {

    /**
     * 分类所有
     *
     * @author fzr
     * @return List<GoodsCategoryVo>
     */
    List<GoodsCategoryVo> all();

    /**
     * 分类列表
     *
     * @author fzr
     * @param pageValidate 分页参数
     * @param searchValidate 搜索参数
     * @return PageResult<GoodsCategoryVo>
     */
    PageResult<GoodsCategoryVo> list(PageValidate pageValidate, GoodsCategorySearchValidate searchValidate);

    /**
     * 分类详情
     *
     * @author fzr
     * @param id 分类ID
     */
    GoodsCategoryVo detail(Integer id);

    /**
     * 分类新增
     *
     * @author fzr
     * @param createValidate 参数
     */
    void add(GoodsCategoryCreateValidate createValidate);

    /**
     * 分类编辑
     *
     * @author fzr
     * @param updateValidate 参数
     */
    void edit(GoodsCategoryUpdateValidate updateValidate);

    /**
     * 分类删除
     *
     * @author fzr
     * @param id 分类ID
     */
    void del(Integer id);

    /**
     * 分类状态
     *
     * @author fzr
     * @param id 分类ID
     */
    void change(Integer id);

}