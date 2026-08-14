package com.mdd.admin.nail.controller;

import com.mdd.admin.nail.dto.NailGenerateRequest;
import com.mdd.admin.nail.service.NailAiTaskService;
import com.mdd.admin.nail.service.NailAssetService;
import com.mdd.admin.nail.service.NailMemberAuthService;
import com.mdd.admin.nail.service.NailPublicRateLimiter;
import com.mdd.admin.nail.service.NailStyleReferenceService;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/nail/public")
public class NailPublicAiController {
    @Resource private NailAiTaskService taskService;
    @Resource private NailAssetService assetService;
    @Resource private NailPublicRateLimiter limiter;
    @Resource private NailStyleReferenceService styleReferenceService;
    @Resource private NailMemberAuthService memberAuthService;

    @NotLogin @NotPower
    @PostMapping("/reference")
    public AjaxResult<Map<String, Object>> reference(@RequestParam("file") MultipartFile file, HttpServletRequest servletRequest,
                                                     @CookieValue(value = "nail-member-token", required = false) String memberToken) {
        limiter.acquire(clientKey(servletRequest));
        Integer memberId = memberAuthService.currentMemberId(memberToken);
        int creatorId = memberId == null ? 0 : memberId;
        String name = memberId == null ? "访客参考图" : "我的参考图";
        return AjaxResult.success(assetService.upload(file, name, "AUTHORIZED", creatorId, "PUBLIC_REFERENCE"));
    }

    @NotLogin @NotPower
    @PostMapping("/task/create")
    public AjaxResult<Map<String, Object>> create(@Validated @RequestBody NailGenerateRequest request, HttpServletRequest servletRequest,
                                                   @CookieValue(value = "nail-member-token", required = false) String memberToken) {
        limiter.acquire(clientKey(servletRequest));
        Integer memberId = memberAuthService.currentMemberId(memberToken);
        if (memberId != null) {
            Map<String, Object> access = new LinkedHashMap<>();
            access.put("id", taskService.createMember(request, memberId));
            access.put("member", true);
            return AjaxResult.success(access);
        }
        return AjaxResult.success(taskService.createPublic(request));
    }

    @NotLogin @NotPower
    @GetMapping("/task/detail")
    public AjaxResult<Map<String, Object>> detail(@RequestParam Long id,
                                                  @RequestParam(value = "token", required = false) String token) {
        return AjaxResult.success(taskService.publicDetail(id, token));
    }

    @NotLogin @NotPower
    @PostMapping("/task/delete")
    public AjaxResult<Object> delete(@RequestParam Long id,
                                     @RequestParam(value = "token", required = false) String token) {
        taskService.deletePublic(id, token);
        return AjaxResult.success();
    }

    @NotLogin @NotPower
    @GetMapping("/style-references")
    public AjaxResult<List<Map<String, Object>>> styleReferences() {
        return AjaxResult.success(styleReferenceService.publicList());
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
