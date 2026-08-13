package com.mdd.admin.nail.service;

import com.mdd.admin.nail.config.NailAiProperties;
import com.mdd.common.exception.OperateException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NailPublicRateLimiter {
    private record Window(long hour, AtomicInteger count) {}
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final NailAiProperties properties;
    public NailPublicRateLimiter(NailAiProperties properties) { this.properties = properties; }

    public void acquire(String clientKey) {
        if (!properties.isPublicEnabled()) throw new OperateException("公开 AI 创作功能当前未开放");
        long hour = Instant.now().getEpochSecond() / 3600;
        Window window = windows.compute(clientKey, (key, old) -> old == null || old.hour() != hour ? new Window(hour, new AtomicInteger()) : old);
        if (window.count().incrementAndGet() > Math.max(1, properties.getPublicHourlyLimit())) {
            throw new OperateException("本小时生成次数已用完，请稍后再试");
        }
        if (windows.size() > 10_000) windows.entrySet().removeIf(entry -> entry.getValue().hour() < hour);
    }
}
