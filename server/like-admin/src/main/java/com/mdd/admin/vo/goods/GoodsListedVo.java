package com.mdd.admin.vo.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("商品列表Vo")
public class GoodsListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "分类名称")
    private String category;

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品主图")
    private String image;

    @ApiModelProperty(value = "最低价格")
    private BigDecimal priceMin;

    @ApiModelProperty(value = "最高价格")
    private BigDecimal priceMax;

    @ApiModelProperty(value = "总库存")
    private Integer stockTotal;

    @ApiModelProperty(value = "总销量")
    private Integer salesTotal;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否上架")
    private Integer isShow;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

}