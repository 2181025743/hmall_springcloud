package com.hmall.cart.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmall.common.domain.R;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class SentinelWebConfig {

    /**
     * 自定义 URL 清洗器
     * 将 /carts/1, /carts/2 等统一为 /carts/{id}
     */
    @Bean
    public UrlCleaner urlCleaner() {
        return url -> {
            if (url == null || url.isEmpty()) {
                return url;
            }
            // 将 /carts/数字 统一为 /carts/{id}
            if (url.matches("/carts/\\d+")) {
                return "/carts/{id}";
            }
            // 将 /items/数字 统一为 /items/{id}
            if (url.matches("/items/\\d+")) {
                return "/items/{id}";
            }
            return url;
        };
    }

    /**
     * 自定义限流异常处理
     */
    @Bean
    public BlockExceptionHandler blockExceptionHandler() {
        return (request, response, e) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(429);  // Too Many Requests

            R<Void> result = R.error(429, "请求过于频繁，请稍后再试");

            new ObjectMapper().writeValue(response.getWriter(), result);
        };
    }

    /**
     * 自定义请求来源解析器（可选）
     * 用于针对不同来源设置不同的限流规则
     */
    @Bean
    public RequestOriginParser requestOriginParser() {
        return request -> {
            // 可以从请求头、参数等获取来源信息
            String origin = request.getHeader("X-Request-From");
            return origin != null ? origin : "default";
        };
    }
}