package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class NailResultReviewRequest {
    @NotNull(message = "请选择生成结果")
    private Long id;

    @Size(max = 500, message = "审阅备注不能超过500个字符")
    private String note = "";
}
