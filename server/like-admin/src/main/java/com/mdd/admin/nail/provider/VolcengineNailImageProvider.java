package com.mdd.admin.nail.provider;

import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.service.ArkService;
import com.mdd.admin.nail.config.NailAiProperties;
import com.mdd.common.exception.OperateException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Component
public class VolcengineNailImageProvider implements NailImageProvider {
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
        GenerateImagesRequest.Builder builder = GenerateImagesRequest.builder()
                .model(config.getModel())
                .prompt(command.prompt())
                .size(resolveSize(command.aspectRatio(), command.resolution()))
                .responseFormat("b64_json")
                .guidanceScale(5.5)
                .sequentialImageGeneration("disabled")
                .stream(false)
                .watermark(false);
        if (command.referenceImage() != null) {
            String dataUri = "data:" + command.referenceMimeType() + ";base64," + Base64.getEncoder().encodeToString(command.referenceImage());
            builder.image(dataUri);
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
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            throw new OperateException("火山图片生成失败：" + message);
        }
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

    private String resolveSize(String ratio, String resolution) {
        Map<String, String> oneHalfK = Map.of(
                "1:1", "1536x1536", "16:9", "1920x1080", "9:16", "1080x1920", "4:3", "1728x1296",
                "3:4", "1296x1728", "3:2", "1872x1248", "2:3", "1248x1872", "21:9", "2016x864");
        Map<String, String> twoK = Map.of(
                "1:1", "2048x2048", "16:9", "2560x1440", "9:16", "1440x2560", "4:3", "2304x1728",
                "3:4", "1728x2304", "3:2", "2496x1664", "2:3", "1664x2496", "21:9", "3024x1296");
        Map<String, String> fourK = Map.of(
                "1:1", "4096x4096", "16:9", "4096x2304", "9:16", "2304x4096", "4:3", "4096x3072",
                "3:4", "3072x4096", "3:2", "4096x2736", "2:3", "2736x4096", "21:9", "4096x1752");
        if ("1.5K".equals(resolution)) return oneHalfK.getOrDefault(ratio, "1536x1536");
        return "4K".equals(resolution) ? fourK.getOrDefault(ratio, "4096x4096") : twoK.getOrDefault(ratio, "2048x2048");
    }
}
