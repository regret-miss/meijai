package com.mdd.admin.nail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nail.creation-bridge")
public class NailCreationBridgeProperties {
    /** 后台登录入口。生产部署时通过 NAIL_CREATION_BRIDGE_ADMIN_LOGIN_URL 覆盖。 */
    private String adminLoginUrl = "http://127.0.0.1:8082/admin/index.html#/login";

    /** 完成认证后管理员回到后台 AI 创作台的绝对地址。 */
    private String adminAiUrl = "http://127.0.0.1:8082/admin/index.html#/nail/ai";

    /** 完成认证后回到访客/会员 AI 创作台的绝对地址。 */
    private String publicAiUrl = "http://127.0.0.1:8082/nail-site/AI.html";

    /** 回填票据有效期，单位秒。 */
    private long ticketTtlSeconds = 600;
}
