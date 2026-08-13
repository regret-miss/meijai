package com.mdd.admin.nail.storage;

import com.mdd.common.exception.OperateException;
import com.mdd.common.util.YmlUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalNailAssetStorage implements NailAssetStorage {
    private static final long MAX_BYTES = 20L * 1024 * 1024;
    private static final long MAX_PIXELS = 40_000_000L;
    private static final Set<String> MIME_TYPES = Set.of("image/png", "image/jpeg");

    @Override
    public StoredImage store(MultipartFile file, String namespace) throws IOException {
        if (file == null || file.isEmpty()) throw new OperateException("请选择图片");
        byte[] bytes = file.getBytes();
        return storeVerified(bytes, file.getContentType(), namespace);
    }

    @Override
    public StoredImage store(byte[] bytes, String mimeType, String namespace) throws IOException {
        return storeVerified(bytes, mimeType, namespace);
    }

    private StoredImage storeVerified(byte[] sourceBytes, String claimedMime, String namespace) throws IOException {
        if (sourceBytes == null || sourceBytes.length == 0 || sourceBytes.length > MAX_BYTES) {
            throw new OperateException("单张图片大小必须在20MB以内");
        }
        String detectedMime = detectMime(sourceBytes);
        String normalizedClaim = normalizeClaimedMime(claimedMime);
        if (!normalizedClaim.isEmpty() && !normalizedClaim.equals(detectedMime)) {
            throw new OperateException("图片真实格式与上传类型不一致");
        }
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(sourceBytes));
        if (source == null) throw new OperateException("文件内容不是有效图片");
        if (source.getWidth() <= 0 || source.getHeight() <= 0 || (long) source.getWidth() * source.getHeight() > MAX_PIXELS) {
            throw new OperateException("图片像素不得超过4000万");
        }

        String format = "image/jpeg".equals(detectedMime) ? "jpg" : "png";
        byte[] sanitized = encode(source, format);
        String day = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String base = "nail/" + safeNamespace(namespace) + "/" + day + "/" + UUID.randomUUID();
        String originalUri = write(base + "." + format, sanitized);
        String thumb200Uri = write(base + "_200." + format, encode(scale(source, 200), format));
        String thumb600Uri = write(base + "_600." + format, encode(scale(source, 600), format));
        return new StoredImage(originalUri, detectedMime, sanitized.length, source.getWidth(), source.getHeight(),
                sha256(sourceBytes), thumb200Uri, thumb600Uri);
    }

    @Override
    public byte[] read(String uri) throws IOException {
        Path root = storageRoot();
        Path target = root.resolve(uri == null ? "" : uri).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target)) throw new OperateException("图片文件不存在");
        return Files.readAllBytes(target);
    }

    private String write(String relative, byte[] bytes) throws IOException {
        Path root = storageRoot();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) throw new OperateException("非法存储路径");
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        return relative.replace('\\', '/');
    }

    private Path storageRoot() {
        return Path.of(YmlUtils.get("like.upload-directory")).toAbsolutePath().normalize();
    }

    private BufferedImage scale(BufferedImage source, int maxEdge) {
        double ratio = Math.min(1d, Math.min((double) maxEdge / source.getWidth(), (double) maxEdge / source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));
        int type = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resized = new BufferedImage(width, height, type);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private byte[] encode(BufferedImage source, String format) throws IOException {
        BufferedImage output = source;
        if ("jpg".equals(format) && source.getType() != BufferedImage.TYPE_INT_RGB) {
            output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = output.createGraphics();
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        if (!ImageIO.write(output, format, buffer)) throw new OperateException("图片编码失败");
        return buffer.toByteArray();
    }

    private String detectMime(byte[] bytes) {
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) return "image/png";
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) return "image/jpeg";
        throw new OperateException("仅支持真实的 PNG 或 JPG 图片");
    }

    private String normalizeClaimedMime(String mimeType) {
        String value = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (value.isEmpty()) return value;
        if (!MIME_TYPES.contains(value)) throw new OperateException("仅支持 PNG 或 JPG 图片");
        return value;
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private String safeNamespace(String namespace) {
        return namespace != null && namespace.matches("[a-z0-9-]+") ? namespace : "misc";
    }
}
