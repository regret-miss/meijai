package com.mdd.admin.validate.goods;

import com.mdd.common.validator.annotation.IDMust;
import com.mdd.common.validator.annotation.IntegerContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("商品更新参数")
public class GoodsUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @ApiModelProperty(value = "id", required = true)
    private Integer id;

    @IDMust(message = "categoryId参数必传且需大于0")
    @ApiModelProperty(value = "分类ID", required = true)
    private Integer categoryId;

    @NotEmpty(message = "商品名称不能为空")
    @Length(min = 1, max = 120, message = "商品名称不能大于120个字符")
    @ApiModelProperty(value = "商品名称", required = true)
    private String name;

    @Length(max = 200, message = "图片链接过长不能超200个字符")
    @ApiModelProperty(value = "商品主图")
    private String image = "";

    @ApiModelProperty(value = "商品轮播图")
    private String images = "";

    @ApiModelProperty(value = "商品详情")
    private String content = "";

    @NotNull(message = "排序号不能为空")
    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @ApiModelProperty(value = "排序", required = true)
    private Integer sort;

    @NotNull(message = "缺少isShow参数")
    @IntegerContains(values = {0, 1}, message = "isShow不是合法值")
    @ApiModelProperty(value = "是否上架", required = true)
    private Integer isShow;

    @DecimalMin(value = "0", message = "虚拟销量不能少于0")
    @ApiModelProperty(value = "虚拟销量")
    private Integer salesVirtual = 0;

    @ApiModelProperty(value = "SKU列表")
    private List<GoodsSkuValidate> skuList;

    @Data
    @ApiModel("商品SKU更新参数")
    public static class GoodsSkuValidate implements Serializable {

        private static final long serialVersionUID = 1L;

        @NotEmpty(message = "SKU名称不能为空")
        @Length(min = 1, max = 120, message = "SKU名称不能大于120个字符")
        @ApiModelProperty(value = "SKU名称", required = true)
        private String skuName;

        @Length(max = 200, message = "SKU图片链接过长不能超200个字符")
        @ApiModelProperty(value = "SKU图片")
        private String image = "";

        @NotNull(message = "销售价格不能为空")
        @DecimalMin(value = "0", message = "销售价格不能少于0")
        @ApiModelProperty(value = "销售价格", required = true)
        private BigDecimal price;

        @DecimalMin(value = "0", message = "成本价格不能少于0")
        @ApiModelProperty(value = "成本价格")
        private BigDecimal costPrice = BigDecimal.ZERO;

        @NotNull(message = "库存数量不能为空")
        @DecimalMin(value = "0", message = "库存数量不能少于0")
        @ApiModelProperty(value = "库存数量", required = true)
        private Integer stock;

        @ApiModelProperty(value = "SKU规格数据(JSON)")
        private String skuData = "";

    }

}