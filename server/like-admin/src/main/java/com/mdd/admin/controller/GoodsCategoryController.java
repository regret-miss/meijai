package com.mdd.admin.controller;

import com.mdd.admin.aop.Log;
import com.mdd.common.aop.NotPower;
import com.mdd.admin.service.IGoodsCategoryService;
import com.mdd.admin.validate.goods.GoodsCategoryCreateValidate;
import com.mdd.admin.validate.goods.GoodsCategoryUpdateValidate;
import com.mdd.admin.validate.goods.GoodsCategorySearchValidate;
import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.goods.GoodsCategoryVo;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("api/goods/category")
@Api(tags = "商品分类管理")
public class GoodsCategoryController {

    @Resource
    IGoodsCategoryService iGoodsCategoryService;

    @NotPower
    @GetMapping("/all")
    @ApiOperation(value="所有分类")
    public AjaxResult<List<GoodsCategoryVo>> all() {
        List<GoodsCategoryVo> list = iGoodsCategoryService.all();
        return AjaxResult.success(list);
    }

    @GetMapping("/list")
    @ApiOperation(value="分类列表")
    public AjaxResult<PageResult<GoodsCategoryVo>> list(@Validated PageValidate pageValidate,
                                                         @Validated GoodsCategorySearchValidate searchValidate) {
        PageResult<GoodsCategoryVo> list = iGoodsCategoryService.list(pageValidate, searchValidate);
        return AjaxResult.success(list);
    }

    @GetMapping("/detail")
    @ApiOperation(value="分类详情")
    public AjaxResult<GoodsCategoryVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        GoodsCategoryVo vo = iGoodsCategoryService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "商品分类新增")
    @PostMapping("/add")
    @ApiOperation(value="分类新增")
    public AjaxResult<Object> add(@Validated @RequestBody GoodsCategoryCreateValidate createValidate) {
        iGoodsCategoryService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "商品分类编辑")
    @PostMapping("/edit")
    @ApiOperation(value="分类编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody GoodsCategoryUpdateValidate updateValidate) {
        iGoodsCategoryService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "商品分类删除")
    @PostMapping("/del")
    @ApiOperation(value="分类删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iGoodsCategoryService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @Log(title = "商品分类状态")
    @PostMapping("/change")
    @ApiOperation(value="分类状态")
    public AjaxResult<Object> change(@Validated @RequestBody IdValidate idValidate) {
        iGoodsCategoryService.change(idValidate.getId());
        return AjaxResult.success();
    }

}