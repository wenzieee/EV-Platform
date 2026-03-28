package com.wenzi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置类
 * 允许前端工程（如 Vue）跨域访问后端的接口
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 匹配所有后端接口
                .allowedOriginPatterns("*") // 允许所有来源（开发环境设置 * 即可）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*") // 允许包含所有的请求头
                .allowCredentials(true) // 允许前端携带凭证（如 Cookie、Session、Token）
                .maxAge(3600); // 预检请求的有效期，单位为秒
    }
}