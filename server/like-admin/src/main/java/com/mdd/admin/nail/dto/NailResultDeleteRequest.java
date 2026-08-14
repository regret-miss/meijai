package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NailResultDeleteRequest {
    @NotNull(message = "请选择要删除的生成结果")
    private Long id;
}
