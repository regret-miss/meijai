package com.mdd.admin.nail.controller;

import com.mdd.admin.nail.dto.NailMemberLoginRequest;
import com.mdd.admin.nail.service.NailMemberAuthService;
import com.mdd.admin.service.ISystemLoginService;
import com.mdd.admin.vo.system.SystemCaptchaVo;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("api/nail/auth")
public class NailMemberAuthController {
    private static final String COOKIE_NAME = "nail-member-token";

    @Resource
    private NailMemberAuthService authService;

    @Resource
    private ISystemLoginService systemLoginService;

    @NotLogin
    @NotPower
    @GetMapping("/captcha")
    public AjaxResult<SystemCaptchaVo> captcha() {
        return AjaxResult.success(systemLoginService.captcha());
    }

    @NotLogin
    @NotPower
    @PostMapping("/login")
    public AjaxResult<Map<String, Object>> login(@Validated @RequestBody NailMemberLoginRequest request,
                                                 HttpServletRequest servletRequest,
                                                 HttpServletResponse servletResponse) {
        Map<String, Object> result = authService.login(request);
        String token = String.valueOf(result.remove("token"));
        writeCookie(servletResponse, token, Duration.ofDays(30), servletRequest.isSecure());
        return AjaxResult.success(result);
    }

    @NotLogin
    @NotPower
    @GetMapping("/session")
    public AjaxResult<Map<String, Object>> session(
            @CookieValue(value = COOKIE_NAME, required = false) String token) {
        return AjaxResult.success(authService.session(token));
    }

    @NotLogin
    @NotPower
    @PostMapping("/logout")
    public AjaxResult<Object> logout(@CookieValue(value = COOKIE_NAME, required = false) String token,
                                     HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse) {
        authService.logout(token);
        writeCookie(servletResponse, "", Duration.ZERO, servletRequest.isSecure());
        return AjaxResult.success();
    }

    private void writeCookie(HttpServletResponse response, String value, Duration maxAge, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
