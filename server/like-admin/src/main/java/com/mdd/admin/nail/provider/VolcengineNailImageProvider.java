package com.mdd.admin.nail.provider;

import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.exception.ArkHttpException;
import com.volcengine.ark.runtime.service.ArkService;
import com.mdd.admin.nail.config.NailAiProperties;
import com.mdd.common.exception.OperateException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class VolcengineNailImageProvider implements NailImageProvider {
    private static final Logger log = LoggerFactory.getLogger(VolcengineNailImageProvider.class);
    private final NailAiProperties properties;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private volatile ArkService arkService;

    public VolcengineNailImageProvider(NailAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void validateConfiguration() {
        NailAiProperties.Volcengine config = properties.getVolcengine();
        if (!StringUtils.hasText(config.getApiKey()) || !StringUtils.hasText(config.getModel())) {
            throw new OperateException("火山方舟尚未配置，请设置 VOLC_ARK_API_KEY 和 VOLC_ARK_MODEL");
        }
    }

    @Override
    public GeneratedImage generate(NailGenerationCommand command) {
        validateConfiguration();
        NailAiProperties.Volcengine config = properties.getVolcengine();
        String lastError = "无可用模型";
        for (String model : modelCandidates(config, command.model())) {
            try {
                return generateWithModel(command, config, model);
            } catch (OperateException e) {
                // BaseException 未调用 super(msg)，getMessage() 恒为 null，必须读 getMsg()
                lastError = e.getMsg() == null ? e.getClass().getSimpleName() : e.getMsg();
                log.warn("美甲生成模型 {} 失败，降级尝试下一个候选模型：{}", model, lastError);
            }
        }
        throw new OperateException("所有美甲生成模型均失败：" + lastError);
    }

    private GeneratedImage generateWithModel(NailGenerationCommand command, NailAiProperties.Volcengine config, String model) {
        int seed = command.seed() > 0 ? (int) Math.floorMod(command.seed(), Integer.MAX_VALUE) : randomSeed();
        if (seed <= 0) seed = randomSeed();
        GenerateImagesRequest.Builder builder = GenerateImagesRequest.builder()
                .model(model)
                .prompt(command.prompt())
                .size(resolveSize(command.aspectRatio(), command.resolution()))
                .responseFormat("b64_json")
                .optimizePrompt(config.isOptimizePrompt())
                .seed(seed)
                .stream(false)
                .watermark(false);
        // 注意：Seedream 5.0 系列端点不支持 guidance_scale 与 sequential_image_generation，
        // 传了会直接 400，故不再发送这两个参数（4.x 也无需它们即可出图）。
        if (command.referenceImages() != null && !command.referenceImages().isEmpty()) {
            List<String> dataUris = new ArrayList<>();
            for (int i = 0; i < command.referenceImages().size(); i++) {
                String mime = command.referenceMimeTypes() != null && i < command.referenceMimeTypes().size()
                        ? command.referenceMimeTypes().get(i) : "image/png";
                dataUris.add("data:" + mime + ";base64," + Base64.getEncoder().encodeToString(command.referenceImages().get(i)));
            }
            builder.image(dataUris);
        }

        try {
            ImagesResponse response = service(config).generateImages(builder.build());
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                throw new OperateException("火山方舟未返回图片结果");
            }
            ImagesResponse.Image image = response.getData().get(0);
            if (StringUtils.hasText(image.getB64Json())) {
                byte[] bytes = Base64.getDecoder().decode(image.getB64Json());
                return new GeneratedImage(bytes, detectMimeType(bytes));
            }
            if (StringUtils.hasText(image.getUrl())) {
                byte[] bytes = download(image.getUrl());
                return new GeneratedImage(bytes, detectMimeType(bytes));
            }
            throw new OperateException("火山方舟返回的图片内容为空");
        } catch (OperateException e) {
            throw e;
        } catch (ArkHttpException e) {
            // 火山 API 明确报错（如 429 限额、400 参数错误）：透传真实 code 与消息，避免被吞掉
            StringBuilder detail = new StringBuilder("HTTP ").append(e.statusCode).append(" [").append(e.code).append("]");
            if (e.getMessage() != null && !e.getMessage().isBlank()) {
                detail.append(" ").append(e.getMessage());
            }
            if (e.requestId != null && !e.requestId.isBlank()) {
                detail.append(" (requestId=").append(e.requestId).append(")");
            }
            throw new OperateException("火山图片生成失败：" + detail);
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            log.warn("火山图片生成请求异常（模型 {}）：{}", model, message, e);
            throw new OperateException("火山图片生成失败：" + message);
        }
    }

    private List<String> modelCandidates(NailAiProperties.Volcengine config, String requestedModel) {
        List<String> models = new ArrayList<>();
        models.add(StringUtils.hasText(requestedModel) ? requestedModel.trim() : config.getModel());
        if (config.getFallbackModels() != null) {
            for (String fallback : config.getFallbackModels()) {
                if (StringUtils.hasText(fallback) && !models.contains(fallback)) {
                    models.add(fallback);
                }
            }
        }
        return models;
    }

    @Override
    public String modelCode() {
        return properties.getVolcengine().getModel();
    }

    private ArkService service(NailAiProperties.Volcengine config) {
        ArkService current = arkService;
        if (current != null) return current;
        synchronized (this) {
            if (arkService == null) {
                arkService = ArkService.builder().apiKey(config.getApiKey()).baseUrl(config.getBaseUrl())
                        .timeout(Duration.ofMinutes(3)).retryTimes(2).build();
            }
            return arkService;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (arkService != null) arkService.shutdownExecutor();
    }

    private byte[] download(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(2)).GET().build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() / 100 != 2) throw new OperateException("下载模型结果失败，HTTP " + response.statusCode());
        return response.body();
    }

    private String detectMimeType(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return "image/png";
        }
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        throw new OperateException("火山方舟返回的内容不是受支持的 PNG 或 JPG 图片");
    }

    private int randomSeed() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    private String resolveSize(String ratio, String resolution) {
        Map<String, String> twoK = Map.of(
                "1:1", "2048x2048", "16:9", "2560x1440", "9:16", "1440x2560", "4:3", "2304x1728",
                "3:4", "1728x2304", "3:2", "2496x1664", "2:3", "1664x2496", "21:9", "3024x1296");
        // 实测约束：Seedream 5.0 端点图像面积必须 ≥ 3,686,400 且 ≤ 4,624,220 像素。
        // 1.5K 档（如 1536x1536）低于下限会被 400 拒绝；4K 档（4096 起）超上限也会被拒。
        // 因此 1.5K / 4K 一律按 2K 档处理（2048 级别均在合法区间内）。
        return twoK.getOrDefault(ratio, "2048x2048");
    }
}
