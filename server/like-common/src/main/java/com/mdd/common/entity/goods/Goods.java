package com.mdd.common.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("商品实体")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("分类ID")
    private Integer categoryId;

    @ApiModelProperty("商品名称")
    private String name;

    @ApiModelProperty("商品主图")
    private String image;

    @ApiModelProperty("商品轮播图")
    private String images;

    @ApiModelProperty("商品详情")
    private String content;

    @ApiModelProperty("最低价格")
    private BigDecimal priceMin;

    @ApiModelProperty("最高价格")
    private BigDecimal priceMax;

    @ApiModelProperty("成本价")
    private BigDecimal costPrice;

    @ApiModelProperty("总库存")
    private Integer stockTotal;

    @ApiModelProperty("总销量")
    private Integer salesTotal;

    @ApiModelProperty("虚拟销量")
    private Integer salesVirtual;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("是否上架: [0=下架, 1=上架]")
    private Integer isShow;

    @ApiModelProperty("是否删除: [0=否, 1=是]")
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    private Long createTime;

    @ApiModelProperty("更新时间")
    private Long updateTime;

    @ApiModelProperty("删除时间")
    private Long deleteTime;

}