package com.mdd.admin.nail.controller;

import com.mdd.admin.nail.service.NailAiTaskService;
import com.mdd.admin.nail.service.NailAssetService;
import com.mdd.admin.nail.service.NailMemberAuthService;
import com.mdd.admin.validate.commons.PageValidate;
import com.mdd.common.aop.NotLogin;
import com.mdd.common.aop.NotPower;
import com.mdd.common.core.AjaxResult;
import com.mdd.common.core.PageResult;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 前台会员（用户）中心接口：资产库、AI 结果采纳、概览。
 * 身份通过 nail-member-token Cookie 判定，仅“用户”角色可访问。
 */
@RestController
@RequestMapping("api/nail/member")
public class NailMemberController {
    private static final String MEMBER_COOKIE = "nail-member-token";

    @Resource private NailMemberAuthService memberAuthService;
    @Resource private NailAssetService assetService;
    @Resource private NailAiTaskService taskService;

    private int requireMember(String token) {
        Integer memberId = memberAuthService.currentMemberId(token);
        if (memberId == null) {
            throw new OperateException("请先登录用户账号");
        }
        return memberId;
    }

    @NotLogin
    @NotPower
    @GetMapping("/overview")
    public AjaxResult<Map<String, Object>> overview(@CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("memberId", memberId);
        data.put("assetCount", assetService.countByCreator(memberId));
        return AjaxResult.success(data);
    }

    @NotLogin
    @NotPower
    @GetMapping("/assets")
    public AjaxResult<PageResult<Map<String, Object>>> assets(@Validated PageValidate page,
                                                               @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        return AjaxResult.success(assetService.listByCreator(page, memberId));
    }

    @NotLogin
    @NotPower
    @PostMapping("/assets/adopt")
    public AjaxResult<Object> adopt(@RequestBody Map<String, Object> body,
                                    @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        Object resultIdRaw = body.get("resultId");
        if (resultIdRaw == null) {
            throw new OperateException("请选择要收藏的设计结果");
        }
        Long resultId = Long.valueOf(String.valueOf(resultIdRaw));
        String accessToken = body.get("accessToken") == null ? "" : String.valueOf(body.get("accessToken"));
        taskService.adoptMemberResult(resultId, accessToken, memberId);
        return AjaxResult.success();
    }

    @NotLogin
    @NotPower
    @PostMapping("/assets/delete")
    public AjaxResult<Object> delete(@RequestBody Map<String, Integer> body,
                                     @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        Integer id = body.get("id");
        if (id == null) {
            throw new OperateException("请选择要删除的资产");
        }
        assetService.deleteMemberAsset(id, memberId);
        return AjaxResult.success();
    }

    @NotLogin
    @NotPower
    @GetMapping("/tasks")
    public AjaxResult<PageResult<Map<String, Object>>> tasks(@Validated PageValidate page,
                                                              @RequestParam(value = "status", required = false) String status,
                                                              @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        return AjaxResult.success(taskService.listByCreator(page, memberId, status));
    }

    @NotLogin
    @NotPower
    @GetMapping("/tasks/detail")
    public AjaxResult<Map<String, Object>> taskDetail(@RequestParam Long id,
                                                       @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        return AjaxResult.success(taskService.memberDetail(id, memberId));
    }

    @NotLogin
    @NotPower
    @PostMapping("/tasks/delete")
    public AjaxResult<Object> deleteTask(@RequestBody Map<String, Long> body,
                                          @CookieValue(value = MEMBER_COOKIE, required = false) String token) {
        int memberId = requireMember(token);
        Long id = body.get("id");
        if (id == null) {
            throw new OperateException("请选择要删除的设计记录");
        }
        taskService.deleteMemberTask(id, memberId);
        return AjaxResult.success();
    }
}
