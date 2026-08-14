package com.mdd.admin.nail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 后端启动后自动拉起本地 Redis（127.0.0.1:6379）。
 * 仅做开发环境兜底：如果 Redis 已在运行则跳过；找不到 redis-server 则不启动并告警。
 */
@Component
public class RedisAutoStarter {
    private static final Logger log = LoggerFactory.getLogger(RedisAutoStarter.class);

    @Value("${nail.redis-auto-start.enabled:true}")
    private boolean enabled;

    @Value("${nail.redis-auto-start.server-path:}")
    private String serverPath;

    @EventListener(ApplicationReadyEvent.class)
    public void startIfNeeded() {
        if (!enabled || !StringUtils.hasText(serverPath)) return;
        if (isRedisUp()) {
            log.info("Redis 已在运行（127.0.0.1:6379），跳过自动拉起");
            return;
        }
        File exe = new File(serverPath);
        if (!exe.exists()) {
            log.warn("Redis 自动拉起跳过：找不到 redis-server 可执行文件 {}", serverPath);
            return;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(serverPath, "--port", "6379", "--save", "", "--appendonly", "no");
            pb.directory(exe.getParentFile());
            pb.redirectErrorStream(true);
            pb.start();
            log.info("已自动拉起 Redis：{}", serverPath);
        } catch (IOException e) {
            log.warn("自动拉起 Redis 失败：{}", e.getMessage());
        }
    }

    private boolean isRedisUp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 6379), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
