package com.mdd.admin.nail.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdd.admin.nail.config.NailAiProperties;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionContentPart;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 云美学打分客户端，双通道：
 *  - provider=zhipu（默认）：智谱 GLM 视觉模型，官方免费（OpenAI 兼容接口，走 HttpClient）。
 *  - provider=ark：火山方舟豆包视觉模型（需开通服务包，走 Ark SDK）。
 * 未配置 api-key、未启用或调用失败时返回 0，不影响生成流程。
 */
@Component
public class NailAestheticClient {
    private static final Logger log = LoggerFactory.getLogger(NailAestheticClient.class);
    private static final Pattern NUMBER = Pattern.compile("(\\d+(?:\\.\\d+)?)");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final NailAiProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private volatile ArkService arkService;

    public NailAestheticClient(NailAiProperties properties) {
        this.properties = properties;
    }

    public double score(byte[] imageBytes, String mimeType) {
        NailAiProperties.Aesthetic cfg = properties.getAesthetic();
        if (!cfg.isEnabled() || !StringUtils.hasText(cfg.getApiKey())) return 0;
        String dataUrl = "data:" + (StringUtils.hasText(mimeType) ? mimeType : "image/jpeg") + ";base64,"
                + Base64.getEncoder().encodeToString(imageBytes);
        try {
            double score = "ark".equalsIgnoreCase(cfg.getProvider())
                    ? scoreViaArk(dataUrl, cfg)
                    : scoreViaZhipu(dataUrl, cfg);
            return Math.max(1.0, Math.min(10.0, score));
        } catch (Exception e) {
            log.warn("美学打分失败（{}）：{}", cfg.getProvider(), e.getMessage());
            return 0;
        }
    }

    /** 智谱 GLM（OpenAI 兼容接口，官方免费视觉模型）。免费档偶发空响应，重试一次。 */
    private double scoreViaZhipu(String dataUrl, NailAiProperties.Aesthetic cfg) throws Exception {
        String raw = callZhipu(cfg, dataUrl);
        Double score = parseScore(raw);
        if (score == null) {
            Thread.sleep(1200);
            raw = callZhipu(cfg, dataUrl);
            score = parseScore(raw);
        }
        if (score == null) {
            String snippet = raw == null ? "null" : (raw.length() > 300 ? raw.substring(0, 300) : raw);
            log.warn("美学打分（zhipu）响应不可解析，原始返回：{}", snippet);
            throw new IllegalStateException("无法解析模型输出");
        }
        return score;
    }

    private String callZhipu(NailAiProperties.Aesthetic cfg, String dataUrl) throws Exception {
        Map<String, Object> body = Map.of(
                "model", cfg.getVisionModel(),
                "messages", List.of(Map.of("role", "user", "content", List.of(
                        Map.of("type", "text", "text", cfg.getScorePrompt()),
                        Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))))),
                "max_tokens", 512,
                "temperature", 0);
        HttpRequest request = HttpRequest.newBuilder(URI.create(cfg.getBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(Math.max(30, cfg.getTimeoutSeconds())))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + cfg.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            String detail = response.body() == null ? "" : response.body();
            if (detail.length() > 300) detail = detail.substring(0, 300);
            throw new IllegalStateException("HTTP " + response.statusCode() + " " + detail);
        }
        return response.body();
    }

    private Double parseScore(String raw) {
        if (raw == null) return null;
        try {
            JsonNode root = MAPPER.readTree(raw);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            Matcher matcher = NUMBER.matcher(content == null ? "" : content);
            if (!matcher.find()) return null;
            double score = Double.parseDouble(matcher.group(1));
            return Math.max(1.0, Math.min(10.0, score));
        } catch (Exception e) {
            return null;
        }
    }

    /** 火山方舟豆包视觉（需开通服务包）。 */
    private double scoreViaArk(String dataUrl, NailAiProperties.Aesthetic cfg) throws Exception {
        ChatCompletionContentPart textPart = ChatCompletionContentPart.builder()
                .type("text").text(cfg.getScorePrompt()).build();
        ChatCompletionContentPart imagePart = ChatCompletionContentPart.builder()
                .type("image_url")
                .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(dataUrl))
                .build();
        ChatMessage message = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .multiContent(List.of(textPart, imagePart))
                .build();
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(cfg.getVisionModel())
                .messages(List.of(message))
                .maxTokens(512)
                .temperature(0.0)
                .build();
        ChatCompletionResult result = arkService().createChatCompletion(request);
        if (result == null || result.getChoices() == null || result.getChoices().isEmpty()
                || result.getChoices().get(0).getMessage() == null) {
            throw new IllegalStateException("模型未返回有效内容");
        }
        String content = String.valueOf(result.getChoices().get(0).getMessage().getContent());
        Matcher matcher = NUMBER.matcher(content == null ? "" : content);
        if (!matcher.find()) throw new IllegalStateException("无法解析模型输出：" + content);
        return Double.parseDouble(matcher.group(1));
    }

    private ArkService arkService() {
        ArkService current = arkService;
        if (current != null) return current;
        synchronized (this) {
            if (arkService == null) {
                NailAiProperties.Volcengine config = properties.getVolcengine();
                arkService = ArkService.builder().apiKey(config.getApiKey()).baseUrl(config.getBaseUrl())
                        .timeout(Duration.ofSeconds(Math.max(30, properties.getAesthetic().getTimeoutSeconds())))
                        .retryTimes(1).build();
            }
            return arkService;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (arkService != null) arkService.shutdownExecutor();
    }

    // 保留 getter 供测试/健康检查使用
    Map<String, String> configSummary() {
        NailAiProperties.Aesthetic cfg = properties.getAesthetic();
        return Map.of("provider", cfg.getProvider(), "model", cfg.getVisionModel(),
                "enabled", String.valueOf(cfg.isEnabled()), "hasKey", String.valueOf(StringUtils.hasText(cfg.getApiKey())));
    }
}
