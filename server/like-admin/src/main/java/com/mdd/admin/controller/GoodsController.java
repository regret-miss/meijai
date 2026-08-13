package com.mdd.admin.controller;

import com.mdd.admin.aop.Log;
import com.mdd.admin.service.IGoodsService;
import com.mdd.admin.validate.goods.GoodsCreateValidate;
import com.mdd.admin.validate.goods.GoodsSearchValidate;
import com.mdd.admin.validate.goods.GoodsUpdateValidate;

import com.mdd.admin.validate.commons.IdValidate;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.admin.vo.goods.GoodsDetailVo;
import com.mdd.admin.vo.goods.GoodsListedVo;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
import com.mdd.common.validator.annotation.IDMust;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("api/goods")
@Api(tags = "商品管理")
public class GoodsController {

    @Resource
    IGoodsService iGoodsService;

    @GetMapping("/list")
    @ApiOperation(value="商品列表")
    public AjaxResult<PageResult<GoodsListedVo>> list(@Validated PageValidate pageValidate,
                                                       @Validated GoodsSearchValidate searchValidate) {
        PageResult<GoodsListedVo> vos = iGoodsService.list(pageValidate, searchValidate);
        return AjaxResult.success(vos);
    }

    @GetMapping("/detail")
    @ApiOperation(value="商品详情")
    public AjaxResult<GoodsDetailVo> detail(@Validated @IDMust() @RequestParam("id") Integer id) {
        GoodsDetailVo vo = iGoodsService.detail(id);
        return AjaxResult.success(vo);
    }

    @Log(title = "商品新增")
    @PostMapping("/add")
    @ApiOperation(value="商品新增")
    public AjaxResult<Object> add(@Validated @RequestBody GoodsCreateValidate createValidate) {
        iGoodsService.add(createValidate);
        return AjaxResult.success();
    }

    @Log(title = "商品编辑")
    @PostMapping("/edit")
    @ApiOperation(value="商品编辑")
    public AjaxResult<Object> edit(@Validated @RequestBody GoodsUpdateValidate updateValidate) {
        iGoodsService.edit(updateValidate);
        return AjaxResult.success();
    }

    @Log(title = "商品删除")
    @PostMapping("/del")
    @ApiOperation(value="商品删除")
    public AjaxResult<Object> del(@Validated @RequestBody IdValidate idValidate) {
        iGoodsService.del(idValidate.getId());
        return AjaxResult.success();
    }

    @Log(title = "商品上下架")
    @PostMapping("/change")
    @ApiOperation(value="商品上下架")
    public AjaxResult<Object> change(@Validated @RequestBody IdValidate idValidate) {
        iGoodsService.change(idValidate.getId());
        return AjaxResult.success();
    }

}