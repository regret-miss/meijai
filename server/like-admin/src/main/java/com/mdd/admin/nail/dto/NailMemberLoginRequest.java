package com.mdd.admin.nail.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class NailMemberLoginRequest {
    @NotBlank(message = "请输入账号")
    @Size(min = 2, max = 32, message = "账号格式不正确")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 32, message = "密码格式不正确")
    private String password;

    // 验证码可选：前台用户登录可不填；带验证码登录时按需校验
    private String code;

    private String uuid;
}
