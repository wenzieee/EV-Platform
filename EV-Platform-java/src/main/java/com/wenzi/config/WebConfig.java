package com.wenzi.config;

import com.wenzi.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    // 👇 1. 新增：映射本地文件目录为网络静态资源
    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        String path = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + path);
    }



    // 全局跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
                .maxAge(168000)
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // 拦截器配置
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 默认拦截一切
                .excludePathPatterns(
                        // 1. 用户基础操作
                        "/user/login",
                        "/user/register",

                        // 2. 车辆及门店展示 (游客可用)
                        "/vehicle/list",
                        "/vehicle/page",
                        "/vehicle/hot",
                        "/vehicle/*",      // 匹配 /vehicle/{id} 车辆详情
                        "/dealer/nearest", // 附近门店

                        // 3. 社区内容展示 (游客可用)
                        "/post/page",
                        "/post/*",         // 匹配 /post/{postId} 帖子详情
                        "/comment/page",
                        "/comment/*",      // 匹配 /comment/{commentId} 评论详情

                        // 4. 状态查询与统计展示 (前端没登录时会优雅返回 false/0，而不该报 401 错误)
                        "/favorite/check/*",           // 车辆收藏状态
                        "/post/like/status/*",         // 帖子点赞状态
                        "/post/collection/status/*",   // 帖子收藏状态
                        "/user/follow/status/*",       // 用户关注状态
                        "/user/follow/count/**",        // 用户关注数/粉丝数

                        "/upload/image", // 👇 2. 放行上传接口
                        "/upload/video",
                        "/uploads/**"    // 👇 2. 放行静态图片资源的访问
                );
    }
}