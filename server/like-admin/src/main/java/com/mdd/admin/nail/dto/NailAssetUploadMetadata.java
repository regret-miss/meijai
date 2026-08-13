package com.mdd.admin.nail.dto;

import lombok.Data;

@Data
public class NailAssetUploadMetadata {
    private String name;
    private String copyrightStatus = "ORIGINAL";
    private String category = "INSPIRATION";
    private String style = "QUIET_LUXURY";
    private String colorFamily = "NEUTRAL";
    private String nailShape = "SHORT_ALMOND";
    private String craft = "GLOSSY_GEL";
    private String tags = "";
    private Integer aiUsable = 1;
}
