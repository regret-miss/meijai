package com.mdd.common.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("商品SKU实体")
public class GoodsSku implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Integer id;

    @ApiModelProperty("商品ID")
    private Integer goodsId;

    @ApiModelProperty("SKU名称")
    private String skuName;

    @ApiModelProperty("SKU图片")
    private String image;

    @ApiModelProperty("销售价格")
    private BigDecimal price;

    @ApiModelProperty("成本价格")
    private BigDecimal costPrice;

    @ApiModelProperty("库存数量")
    private Integer stock;

    @ApiModelProperty("销量")
    private Integer sales;

    @ApiModelProperty("SKU规格数据(JSON)")
    private String skuData;

    @ApiModelProperty("是否删除: [0=否, 1=是]")
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    private Long createTime;

    @ApiModelProperty("更新时间")
    private Long updateTime;

    @ApiModelProperty("删除时间")
    private Long deleteTime;

}