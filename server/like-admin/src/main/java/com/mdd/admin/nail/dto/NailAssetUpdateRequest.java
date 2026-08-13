package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class NailAssetUpdateRequest {
    @NotNull(message = "请选择资产")
    private Integer id;

    @NotBlank(message = "请输入资产名称")
    @Size(max = 160, message = "资产名称不能超过160个字符")
    private String name;

    @Pattern(regexp = "ORIGINAL|AUTHORIZED|AI_GENERATED", message = "不支持的版权状态")
    private String copyrightStatus;

    @NotNull(message = "请选择是否允许AI使用")
    private Integer aiUsable;

    @NotBlank(message = "请选择资产分类")
    @Pattern(regexp = "INSPIRATION|AI_WORK|TREND|COMMERCIAL|CLIENT_REFERENCE", message = "不支持的资产分类")
    private String category;

    @NotBlank(message = "请选择设计风格")
    @Pattern(regexp = "QUIET_LUXURY|KOREAN_CLEAR|RUNWAY|FUTURISTIC|ROMANTIC|SWEET_COOL", message = "不支持的设计风格")
    private String style;

    @NotBlank(message = "请选择色系")
    @Pattern(regexp = "PINK|RED|NUDE|WHITE|BLACK|BLUE|PURPLE|GREEN|YELLOW|METALLIC|NEUTRAL|MULTICOLOR", message = "不支持的色系")
    private String colorFamily;

    @NotBlank(message = "请选择甲形")
    @Pattern(regexp = "SHORT_ALMOND|SHORT_SQUOVAL|ALMOND|SQUARE|COFFIN", message = "不支持的甲形")
    private String nailShape;

    @NotBlank(message = "请选择工艺")
    @Pattern(regexp = "VELVET_CAT_EYE|JELLY|CHROME|MICRO_FRENCH|AURA|SCULPTED_GEL|GLOSSY_GEL", message = "不支持的美甲工艺")
    private String craft;

    @Size(max = 500, message = "标签内容不能超过500个字符")
    private String tags;
}
