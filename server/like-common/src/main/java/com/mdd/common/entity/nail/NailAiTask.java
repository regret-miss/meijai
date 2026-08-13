package com.mdd.common.entity.nail;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class NailAiTask {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String taskType;
    private String title;
    private String status;
    private String provider;
    private String modelCode;
    private String promptRaw;
    private String promptCompiled;
    private String negativePrompt;
    private String aspectRatio;
    private String resolution;
    private Integer outputCount;
    private Integer referenceAssetId;
    private String creativeMode;
    private String nailShape;
    private String finish;
    private String designStyle;
    private String layoutStyle;
    private String trendPreset;
    private String referenceStrategy;
    private String colorPalette;
    private String templateVersion;
    private String errorMessage;
    private Integer creatorId;
    private String publicToken;
    private Long startedTime;
    private Long finishedTime;
    private Long createTime;
    private Long updateTime;
}
