package com.mdd.admin.nail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nail.ai")
public class NailAiProperties {
    private String provider = "volcengine";
    private boolean publicEnabled = true;
    private int publicHourlyLimit = 6;
    private int workerConcurrency = 2;
    private String promptTemplateVersion = "nail-designer-v2";
    private Volcengine volcengine = new Volcengine();

    @Data
    public static class Volcengine {
        private String apiKey = "";
        private String model = "";
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
    }
}
