package com.mdd.admin.nail.service;

import com.mdd.common.config.GlobalConfig;
import com.mdd.common.util.RequestUtils;
import com.mdd.common.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NailUrlResolver {
    private static final Logger log = LoggerFactory.getLogger(NailUrlResolver.class);

    public String toAbsoluteUrl(String uri) {
        try {
            return UrlUtils.toAbsoluteUrl(uri);
        } catch (RuntimeException cacheUnavailable) {
            log.warn("Storage config cache unavailable, using local nail asset URL: {}", cacheUnavailable.getMessage());
            String normalized = uri == null ? "" : uri.replace('\\', '/').replaceFirst("^/+", "");
            return RequestUtils.uri() + "/" + GlobalConfig.publicPrefix + "/" + normalized;
        }
    }
}
