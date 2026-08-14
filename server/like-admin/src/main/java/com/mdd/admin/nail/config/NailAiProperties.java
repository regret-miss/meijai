package com.mdd.admin.nail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    private PostProcess postprocess = new PostProcess();
    private Aesthetic aesthetic = new Aesthetic();

    @Data
    public static class Volcengine {
        private String apiKey = "";
        private String model = "";
        private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
        private double guidanceScale = 4.0;
        private boolean optimizePrompt = true;
        private String promptProfile = "auto";
        private List<String> fallbackModels = new ArrayList<>();
    }

    /** 出图后处理：轻量锐化（Unsharp Mask），不影响生成质量，只增强边缘观感。 */
    @Data
    public static class PostProcess {
        private boolean enabled = true;
        private double sharpenAmount = 0.35;
    }

    public boolean isPostprocessEnabled() {
        return postprocess != null && postprocess.isEnabled();
    }

    public double getSharpenAmount() {
        return postprocess == null ? 0 : postprocess.getSharpenAmount();
    }

    /** 云美学打分：默认走智谱 GLM 免费视觉模型；也可配置火山豆包视觉（需开通服务包）。 */
    @Data
    public static class Aesthetic {
        private boolean enabled = false;
        /** zhipu（智谱，免费）| ark（火山豆包，需开通） */
        private String provider = "zhipu";
        private String apiKey = "";
        private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";
        private String visionModel = "glm-4v-flash";
        private String scorePrompt = "对这张美甲效果图按美学质量打分（1-10分，保留一位小数），只返回一个数字。";
        private int timeoutSeconds = 90;
    }
}
