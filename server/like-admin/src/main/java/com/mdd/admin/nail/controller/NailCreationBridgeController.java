package com.mdd.admin.nail.controller;

import com.mdd.admin.LikeAdminThreadLocal;
import com.mdd.admin.nail.dto.NailCreationBridgeStartRequest;
import com.mdd.admin.nail.service.NailCreationBridgeService;
import com.mdd.admin.nail.service.NailMemberAuthService;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.exception.OperateException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("api/nail/creation-bridge")
public class NailCreationBridgeController {
    private static final String MEMBER_COOKIE = "nail-member-token";

    @Resource
    private NailCreationBridgeService bridgeService;

    @Resource
    private NailMemberAuthService memberAuthService;

    @NotLogin
    @NotPower
    @PostMapping("/start")
    public AjaxResult<Map<String, Object>> start(@Validated @RequestBody NailCreationBridgeStartRequest request) {
        return AjaxResult.success(bridgeService.start(request.getPrompt()));
    }

    @NotPower
    @PostMapping("/complete-admin")
    public AjaxResult<Map<String, Object>> completeAdmin(@RequestParam String ticket) {
        Integer adminId = LikeAdminThreadLocal.getAdminId();
        String displayName = LikeAdminThreadLocal.get("username").toString();
        return AjaxResult.success(bridgeService.completeByAdmin(ticket, adminId, displayName));
    }

    @NotLogin
    @NotPower
    @PostMapping("/complete-member")
    public AjaxResult<Map<String, Object>> completeMember(@RequestParam String ticket,
                                                           @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        Map<String, Object> session = memberAuthService.session(token);
        if (!Boolean.TRUE.equals(session.get("loggedIn"))) {
            throw new OperateException("用户登录状态已失效，请重新登录");
        }
        return AjaxResult.success(bridgeService.completeByMember(
                ticket,
                Integer.parseInt(session.get("id").toString()),
                session.get("displayName").toString()));
    }

    @NotLogin
    @NotPower
    @PostMapping("/complete-session")
    public AjaxResult<Map<String, Object>> completeSession(@RequestParam String ticket,
                                                            @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        Map<String, Object> session = memberAuthService.session(token);
        if (!Boolean.TRUE.equals(session.get("loggedIn"))) {
            throw new OperateException("登录状态已失效，请重新登录");
        }
        Integer identityId = Integer.parseInt(session.get("id").toString());
        String displayName = session.get("displayName").toString();
        if ("ADMIN".equals(session.get("role"))) {
            return AjaxResult.success(bridgeService.completeByAdmin(ticket, identityId, displayName));
        }
        return AjaxResult.success(bridgeService.completeByMember(ticket, identityId, displayName));
    }

    @NotLogin
    @NotPower
    @GetMapping("/consume")
    public AjaxResult<Map<String, Object>> consume(@RequestParam String ticket) {
        return AjaxResult.success(bridgeService.consume(ticket));
    }
}
