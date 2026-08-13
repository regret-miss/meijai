package com.mdd.admin.nail.service;

import com.mdd.admin.nail.config.NailCreationBridgeProperties;
import com.mdd.common.exception.OperateException;
import com.mdd.common.util.RedisUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NailCreationBridgeService {
    private static final String KEY_PREFIX = "nail:creation-bridge:";
    private static final String STATE_PENDING = "PENDING";
    private static final String STATE_COMPLETED = "COMPLETED";
    private static final String CONSUMED_MARKER = "CONSUMED";

    @Resource
    private NailCreationBridgeProperties properties;

    public Map<String, Object> start(String prompt) {
        String ticket = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("prompt", prompt.trim());
        payload.put("state", STATE_PENDING);
        RedisUtils.set(key(ticket), payload, properties.getTicketTtlSeconds());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ticket", ticket);
        result.put("loginUrl", appendTicket(properties.getAdminLoginUrl(), ticket));
        return result;
    }

    public Map<String, Object> completeByAdmin(String ticket, Integer adminId, String displayName) {
        return complete(ticket, "ADMIN", adminId, displayName);
    }

    public Map<String, Object> completeByMember(String ticket, Integer memberId, String displayName) {
        return complete(ticket, "USER", memberId, displayName);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> consume(String ticket) {
        Object raw = RedisUtils.getSet(key(ticket), CONSUMED_MARKER);
        if (!(raw instanceof Map)) {
            throw new OperateException("创作回填凭证已失效，请从首页重新开始");
        }
        Map<String, Object> payload = (Map<String, Object>) raw;
        if (!STATE_COMPLETED.equals(payload.get("state"))) {
            throw new OperateException("请先完成登录后再继续创作");
        }
        RedisUtils.del(key(ticket));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prompt", payload.get("prompt"));
        result.put("role", payload.get("role"));
        result.put("roleName", "ADMIN".equals(payload.get("role")) ? "管理员" : "用户");
        result.put("displayName", payload.get("displayName"));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> complete(String ticket, String role, Integer id, String displayName) {
        Object raw = RedisUtils.get(key(ticket));
        if (!(raw instanceof Map)) {
            throw new OperateException("创作回填凭证已失效，请返回首页重新开始");
        }
        Map<String, Object> payload = new LinkedHashMap<>((Map<String, Object>) raw);
        if (!STATE_PENDING.equals(payload.get("state"))) {
            throw new OperateException("该创作回填凭证已完成或已失效");
        }
        payload.put("state", STATE_COMPLETED);
        payload.put("role", role);
        payload.put("identityId", id);
        payload.put("displayName", displayName);
        RedisUtils.set(key(ticket), payload, properties.getTicketTtlSeconds());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("returnUrl", appendTicket(properties.getPublicAiUrl(), ticket));
        return result;
    }

    private String key(String ticket) {
        if (ticket == null || !ticket.matches("[A-Za-z0-9]{32,64}")) {
            throw new OperateException("创作回填凭证格式不正确");
        }
        return KEY_PREFIX + ticket;
    }

    private String appendTicket(String baseUrl, String ticket) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "creation_ticket=" + URLEncoder.encode(ticket, StandardCharsets.UTF_8);
    }
}
