package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class NailGenerateRequest {
    @NotBlank(message = "请选择生成模式")
    @Pattern(regexp = "TEXT_TO_IMAGE|IMAGE_TO_IMAGE", message = "不支持的生成模式")
    private String taskType;

    @NotBlank(message = "请输入创作描述")
    @Size(min = 2, max = 1000, message = "创作描述长度需为2到1000个字符")
    private String prompt;

    @Pattern(regexp = "1:1|16:9|9:16|4:3|3:4|3:2|2:3|21:9", message = "不支持的图片比例")
    private String aspectRatio = "1:1";

    @Pattern(regexp = "1\\.5K|2K|4K", message = "清晰度仅支持1.5K、2K或4K")
    private String resolution = "2K";

    @Min(value = 1, message = "至少生成1张")
    @Max(value = 4, message = "一次最多生成4张")
    private Integer outputCount = 1;

    private Integer referenceAssetId;

    private Long referenceResultId;

    private Integer styleReferenceId;

    @Size(max = 120, message = "模型标识不能超过120个字符")
    private String model;

    @Min(value = 1, message = "随机种子必须为正整数")
    private Long seed;

    @Pattern(regexp = "DESIGN_BOARD|ON_HAND", message = "不支持的呈现方式")
    private String creativeMode = "ON_HAND";

    @Pattern(regexp = "SHORT_ALMOND|SHORT_SQUOVAL|ALMOND|SQUARE|COFFIN|ROUND|STILETTO|LIPSTICK", message = "不支持的甲型")
    private String nailShape = "SHORT_ALMOND";

    @Pattern(regexp = "VELVET_CAT_EYE|JELLY|CHROME|MICRO_FRENCH|AURA|SCULPTED_GEL|GLOSSY_GEL|FRENCH_TIP|MILK_BATH|OMBRE|GLITTER|PEARL", message = "不支持的材质工艺")
    private String finish = "VELVET_CAT_EYE";

    @Pattern(regexp = "QUIET_LUXURY|KOREAN_CLEAR|RUNWAY|FUTURISTIC|ROMANTIC|SWEET_COOL|MINIMALIST|Y2K|COQUETTE|OLD_MONEY|DOPAMINE|MORANDI", message = "不支持的设计风格")
    private String designStyle = "QUIET_LUXURY";

    @Pattern(regexp = "UNIFIED|TWO_ACCENTS|MICRO_FRENCH_LAYOUT|MISMATCHED", message = "不支持的甲面排版")
    private String layoutStyle = "TWO_ACCENTS";

    @Pattern(regexp = "ROSE_VELVET|SEA_GLASS|BUTTER_MICRO_FRENCH|MIXED_METAL|AURORA_MAGNETIC|KOREAN_SYRUP|JADE_CAT_EYE|MINT_FRENCH|LACE_NAILS|REVERSE_FRENCH|LEOPARD_PRINT|METALLIC_FRENCH|MILKY_WHITE|SUNSET_OMBRE|CUSTOM", message = "不支持的趋势预设")
    private String trendPreset = "ROSE_VELVET";

    @Pattern(regexp = "REINTERPRET|KEEP_PALETTE|KEEP_LAYOUT|KEEP_TEXTURE", message = "不支持的参考图改款方式")
    private String referenceStrategy = "REINTERPRET";

    @Size(max = 120, message = "配色描述不能超过120个字符")
    private String colorPalette = "烟粉、香槟金、奶白";
}
