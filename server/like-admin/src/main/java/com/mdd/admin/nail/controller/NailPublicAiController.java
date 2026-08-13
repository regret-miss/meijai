package com.mdd.admin.nail.controller;

import com.mdd.admin.nail.dto.NailGenerateRequest;
import com.mdd.admin.nail.service.NailAiTaskService;
import com.mdd.admin.nail.service.NailAssetService;
import com.mdd.admin.nail.service.NailPublicRateLimiter;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("api/nail/public")
public class NailPublicAiController {
    @Resource private NailAiTaskService taskService;
    @Resource private NailAssetService assetService;
    @Resource private NailPublicRateLimiter limiter;

    @NotLogin @NotPower
    @PostMapping("/reference")
    public AjaxResult<Map<String, Object>> reference(@RequestParam("file") MultipartFile file, HttpServletRequest servletRequest) {
        limiter.acquire(clientKey(servletRequest));
        return AjaxResult.success(assetService.upload(file, "访客参考图", "AUTHORIZED", 0, "PUBLIC_REFERENCE"));
    }

    @NotLogin @NotPower
    @PostMapping("/task/create")
    public AjaxResult<Map<String, Object>> create(@Validated @RequestBody NailGenerateRequest request, HttpServletRequest servletRequest) {
        limiter.acquire(clientKey(servletRequest));
        return AjaxResult.success(taskService.createPublic(request));
    }

    @NotLogin @NotPower
    @GetMapping("/task/detail")
    public AjaxResult<Map<String, Object>> detail(@RequestParam Long id,
                                                  @RequestParam(value = "token", required = false) String token) {
        return AjaxResult.success(taskService.publicDetail(id, token));
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
