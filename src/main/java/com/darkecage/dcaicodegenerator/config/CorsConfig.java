package com.darkecage.dcaicodegenerator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 *
 * @author kaiqi.hu
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求路径
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 允许所有来源（使用 patterns 方式兼容 allowCredentials）
                .allowedOriginPatterns("*")
                // 允许所有请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头
                .allowedHeaders("*")
                // 暴露所有响应头
                .exposedHeaders("*")
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }
}
