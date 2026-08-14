package com.mdd.admin.nail.service;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mdd.admin.cache.CaptchaCache;
import com.mdd.admin.nail.dto.NailMemberLoginRequest;
import com.mdd.admin.service.ISystemLoginService;
import com.mdd.admin.validate.system.SystemAdminLoginsValidate;
import com.mdd.admin.vo.system.SystemLoginVo;
import com.mdd.common.entity.system.SystemAuthAdmin;
import com.mdd.common.entity.user.User;
import com.mdd.common.exception.OperateException;
import com.mdd.common.mapper.system.SystemAuthAdminMapper;
import com.mdd.common.mapper.user.UserMapper;
import com.mdd.common.util.ToolUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NailMemberAuthService {
    private static final StpLogic MEMBER_LOGIC = new StpLogic("nail-member");
    private static final String ADMIN_PREFIX = "ADMIN:";

    @Resource
    private UserMapper userMapper;

    @Resource
    private SystemAuthAdminMapper systemAuthAdminMapper;

    @Resource
    private ISystemLoginService systemLoginService;

    public Map<String, Object> login(NailMemberLoginRequest request) {
        String username = request.getUsername().trim();
        boolean isAdmin = systemAuthAdminMapper.exists(new QueryWrapper<SystemAuthAdmin>()
                .eq("username", username)
                .eq("is_delete", 0));
        if (isAdmin) {
            return loginAdmin(request);
        }
        verifyCaptcha(request);
        return loginMember(request);
    }

    private void verifyCaptcha(NailMemberLoginRequest request) {
        String code = request.getCode();
        String uuid = request.getUuid();
        if ((code == null || code.isBlank()) && (uuid == null || uuid.isBlank())) {
            return;
        }
        String expectedCode = CaptchaCache.get(uuid == null ? "" : uuid);
        if (expectedCode.isBlank() || !expectedCode.equalsIgnoreCase(code == null ? "" : code.trim())) {
            throw new OperateException("验证码错误或已失效，请刷新后重试");
        }
    }

    private Map<String, Object> loginMember(NailMemberLoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id", "username", "nickname", "password", "salt", "is_disable")
                .eq("username", request.getUsername().trim())
                .eq("is_delete", 0)
                .last("limit 1"));
        if (user == null || !ToolUtils.makeMd5(request.getPassword() + user.getSalt()).equals(user.getPassword())) {
            throw new OperateException("账号或密码错误");
        }
        if (Integer.valueOf(1).equals(user.getIsDisable())) {
            throw new OperateException("账号已被停用");
        }

        MEMBER_LOGIC.login(user.getId());
        Map<String, Object> result = memberView(user);
        result.put("token", MEMBER_LOGIC.getTokenValue());
        return result;
    }

    private Map<String, Object> loginAdmin(NailMemberLoginRequest request) {
        SystemAdminLoginsValidate loginRequest = new SystemAdminLoginsValidate();
        loginRequest.setUsername(request.getUsername().trim());
        loginRequest.setPassword(request.getPassword());
        loginRequest.setCode(request.getCode());
        loginRequest.setUuid(request.getUuid());
        SystemLoginVo login = systemLoginService.login(loginRequest);

        SystemAuthAdmin admin = systemAuthAdminMapper.selectOne(new QueryWrapper<SystemAuthAdmin>()
                .select("id", "username", "nickname", "is_disable", "is_delete")
                .eq("id", login.getId())
                .last("limit 1"));
        if (admin == null || Integer.valueOf(1).equals(admin.getIsDelete())
                || Integer.valueOf(1).equals(admin.getIsDisable())) {
            throw new OperateException("管理员账号不可用");
        }

        MEMBER_LOGIC.login(ADMIN_PREFIX + admin.getId());
        Map<String, Object> result = adminView(admin);
        result.put("token", MEMBER_LOGIC.getTokenValue());
        result.put("adminToken", login.getToken());
        return result;
    }

    public Map<String, Object> session(String token) {
        Map<String, Object> result = new LinkedHashMap<>();
        Object loginId = token == null || token.isBlank() ? null : MEMBER_LOGIC.getLoginIdByToken(token);
        if (loginId == null) {
            result.put("loggedIn", false);
            return result;
        }
        String identity = loginId.toString();
        if (identity.startsWith(ADMIN_PREFIX)) {
            return adminSession(identity.substring(ADMIN_PREFIX.length()), token);
        }
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .select("id", "username", "nickname", "is_disable")
                .eq("id", Integer.parseInt(identity))
                .eq("is_delete", 0)
                .last("limit 1"));
        if (user == null || Integer.valueOf(1).equals(user.getIsDisable())) {
            MEMBER_LOGIC.logoutByTokenValue(token);
            result.put("loggedIn", false);
            return result;
        }
        result.putAll(memberView(user));
        return result;
    }

    private Map<String, Object> adminSession(String adminId, String token) {
        Map<String, Object> result = new LinkedHashMap<>();
        SystemAuthAdmin admin = systemAuthAdminMapper.selectOne(new QueryWrapper<SystemAuthAdmin>()
                .select("id", "username", "nickname", "is_disable", "is_delete")
                .eq("id", Integer.parseInt(adminId))
                .last("limit 1"));
        if (admin == null || Integer.valueOf(1).equals(admin.getIsDelete())
                || Integer.valueOf(1).equals(admin.getIsDisable())) {
            MEMBER_LOGIC.logoutByTokenValue(token);
            result.put("loggedIn", false);
            return result;
        }
        result.putAll(adminView(admin));
        return result;
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            MEMBER_LOGIC.logoutByTokenValue(token);
        }
    }

    /**
     * 解析当前登录会员 ID：仅当会话有效且身份为用户时返回 ID，否则返回 null。
     */
    public Integer currentMemberId(String token) {
        Map<String, Object> session = session(token);
        if (!Boolean.TRUE.equals(session.get("loggedIn"))) {
            return null;
        }
        if (!"USER".equals(session.get("role"))) {
            return null;
        }
        return Integer.parseInt(session.get("id").toString());
    }

    private Map<String, Object> memberView(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("loggedIn", true);
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("displayName", user.getNickname() == null || user.getNickname().isBlank()
                ? user.getUsername() : user.getNickname());
        result.put("role", "USER");
        result.put("roleName", "用户");
        return result;
    }

    private Map<String, Object> adminView(SystemAuthAdmin admin) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("loggedIn", true);
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("displayName", admin.getNickname() == null || admin.getNickname().isBlank()
                ? admin.getUsername() : admin.getNickname());
        result.put("role", "ADMIN");
        result.put("roleName", "管理员");
        return result;
    }
}
