package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NailTaskDeleteRequest {
    @NotNull(message = "请选择要删除的设计记录")
    private Long id;
}
