package com.hmall.user.config;// ...

import com.hmall.user.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class MvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    // private final JwtProperties jwtProperties; // 这个字段在这里并未使用，可以移除

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/users/login",
                        "/users/code",
                        "/error",
                        "/favicon.ico",
                        "/doc.html", // 增加 Knife4j UI 路径
                        "/webjars/**", // 增加 Knife4j 静态资源
                        "/swagger-resources/**", // 增加 Knife4j 静态资源
                        "/v2/api-docs/**", // 增加 Knife4j API 定义
                        "/v3/api-docs/**"  // 兼容 OpenAPI 3.0
                );
    }
}