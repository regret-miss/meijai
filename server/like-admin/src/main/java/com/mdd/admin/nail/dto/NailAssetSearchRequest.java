package com.mdd.admin.nail.dto;

import lombok.Data;

@Data
public class NailAssetSearchRequest {
    private String cursor;
    private String keyword;
    private String category;
    private String style;
    private String colorFamily;
    private String nailShape;
    private String craft;
    private String tag;
    private String source;
    private String copyrightStatus;
    private Integer aiUsable;
    private String status;
    private Long createdStart;
    private Long createdEnd;
    private String sort;
}
