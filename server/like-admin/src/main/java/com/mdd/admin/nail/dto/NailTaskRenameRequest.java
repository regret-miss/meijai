package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class NailTaskRenameRequest {
    @NotNull(message = "请选择设计记录")
    private Long id;

    @NotBlank(message = "请输入记录名称")
    @Size(max = 80, message = "记录名称不能超过80个字符")
    private String title;
}
