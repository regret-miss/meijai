package com.mdd.admin.nail.service;

import com.mdd.common.exception.OperateException;
import com.mdd.common.util.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class NailMediaSigner {
    private static final long URL_TTL_SECONDS = 600L;

    @Value("${nail.asset.signing-secret:}")
    private String configuredSecret;
    @Value("${spring.profiles.active:}")
    private String activeProfiles;
    @Value("${like.upload-directory:./public/uploads/}")
    private String uploadDirectory;
    private byte[] secret;

    @PostConstruct
    public void init() {
        if (configuredSecret != null && configuredSecret.length() >= 32) {
            secret = configuredSecret.getBytes(StandardCharsets.UTF_8);
            return;
        }
        if (!activeProfiles.contains("local") && !activeProfiles.contains("dev")) {
            throw new IllegalStateException("NAIL_ASSET_SIGNING_SECRET must be configured with at least 32 characters outside local/dev profiles");
        }
        secret = loadOrCreateLocalSecret();
    }

    public String assetUrl(Integer id, String variant, boolean download) {
        return signedUrl("asset", id.longValue(), variant, download);
    }

    public String resultUrl(Long id, boolean download) {
        return signedUrl("result", id, "original", download);
    }

    public String styleUrl(Integer id, String variant, boolean download) {
        return signedUrl("style", id.longValue(), variant, download);
    }

    public void verify(String type, long id, String variant, boolean download, long expires, String signature) {
        if (expires < System.currentTimeMillis() / 1000L) throw new OperateException("图片访问地址已过期，请刷新页面");
        if (expires > System.currentTimeMillis() / 1000L + URL_TTL_SECONDS + 30L) throw new OperateException("图片访问地址无效");
        byte[] expected = sign(payload(type, id, variant, download, expires));
        byte[] actual;
        try {
            actual = Base64.getUrlDecoder().decode(signature == null ? "" : signature);
        } catch (IllegalArgumentException invalid) {
            throw new OperateException("图片访问签名无效");
        }
        if (!MessageDigest.isEqual(expected, actual)) throw new OperateException("图片访问签名无效");
    }

    private String signedUrl(String type, long id, String variant, boolean download) {
        long expires = System.currentTimeMillis() / 1000L + URL_TTL_SECONDS;
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sign(payload(type, id, variant, download, expires)));
        return RequestUtils.uri() + "/api/nail/media/" + type + "/" + id
                + "?variant=" + variant + "&download=" + download + "&expires=" + expires + "&signature=" + signature;
    }

    private String payload(String type, long id, String variant, boolean download, long expires) {
        return type + ":" + id + ":" + variant + ":" + download + ":" + expires;
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
            throw new IllegalStateException("Unable to sign nail media URL", error);
        }
    }

    private byte[] loadOrCreateLocalSecret() {
        try {
            Path privateDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize().resolve(".private");
            Path keyFile = privateDirectory.resolve("nail-media-signing.key");
            Files.createDirectories(privateDirectory);
            if (!Files.exists(keyFile)) {
                byte[] generated = new byte[32];
                new SecureRandom().nextBytes(generated);
                String encoded = Base64.getEncoder().encodeToString(generated);
                try {
                    Files.writeString(keyFile, encoded, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                } catch (java.nio.file.FileAlreadyExistsException ignored) {
                    // Another local process created the shared key first.
                }
            }
            byte[] loaded = Base64.getDecoder().decode(Files.readString(keyFile, StandardCharsets.US_ASCII).trim());
            if (loaded.length < 32) throw new IllegalStateException("Local nail media signing key is too short");
            return loaded;
        } catch (Exception error) {
            throw new IllegalStateException("Unable to load the local nail media signing key", error);
        }
    }
}
