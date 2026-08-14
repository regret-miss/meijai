package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class NailStyleReferenceSaveRequest {
    private Integer id;

    @NotBlank(message = "请输入风格名称")
    @Size(max = 80, message = "风格名称不能超过80个字符")
    private String name;

    @NotBlank(message = "请选择风格分类")
    @Size(max = 32, message = "分类不能超过32个字符")
    private String category;

    @Size(max = 500, message = "提示词增强不能超过500个字符")
    private String promptEnhance = "";

    @Pattern(regexp = "REINTERPRET|KEEP_PALETTE|KEEP_LAYOUT|KEEP_TEXTURE", message = "不支持的参考策略")
    private String referenceStrategy = "REINTERPRET";

    private Integer sort = 0;
}
