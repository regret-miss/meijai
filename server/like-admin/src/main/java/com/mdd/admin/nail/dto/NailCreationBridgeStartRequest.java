package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NailCreationBridgeStartRequest {
    @NotBlank(message = "请先写下设计意图")
    @Size(max = 1000, message = "设计意图不能超过1000字")
    private String prompt;
}
