package com.hmall.api.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 的默认配置
 */
@Configuration
public class DefaultFeignConfig {

    /**
     * 配置 Feign 日志级别
     */
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.BASIC;
    }
}