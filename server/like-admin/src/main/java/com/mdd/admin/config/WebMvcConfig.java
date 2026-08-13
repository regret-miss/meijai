package com.mdd.admin.config;

import com.mdd.admin.LikeAdminInterceptor;
import com.mdd.admin.nail.config.NailRawMediaInterceptor;
import com.mdd.common.config.GlobalConfig;
import com.mdd.common.util.YmlUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${like.cors.allowed-origin-patterns:http://127.0.0.1:*,http://localhost:*}")
    private String[] allowedOriginPatterns;

    @Resource
    LikeAdminInterceptor likeAdminInterceptor;
    @Resource
    NailRawMediaInterceptor nailRawMediaInterceptor;

    /**
     * 配置允许跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 登录拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(nailRawMediaInterceptor)
                .addPathPatterns("/uploads/nail/**");
        registry.addInterceptor(likeAdminInterceptor)
                .addPathPatterns("/**");
    }

    /**
     * 资源目录映射
     */
    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        String directory = YmlUtils.get("like.upload-directory");
        registry.addResourceHandler("/"+ GlobalConfig.publicPrefix +"/**")
                .addResourceLocations("file:" + directory);

        String webDirectory = YmlUtils.get("like.web-directory");
        if (StringUtils.hasText(webDirectory)) {
            String location = "file:" + (webDirectory.endsWith("/") ? webDirectory : webDirectory + "/");
            registry.addResourceHandler("/**").addResourceLocations(location);
        }
    }

    /**
     * 前台美甲站点发布在 public/nail-site，保持后台与上传资源既有路径不变。
     */
    @Override
    public void addViewControllers(@NotNull ViewControllerRegistry registry) {
        registry.addRedirectViewController("/nail-site", "/nail-site/首页.html");
        registry.addRedirectViewController("/nail-site/", "/nail-site/首页.html");
    }

}
