package com.hmall.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 动态路由加载器
 * 监听Nacos配置变化，动态更新网关路由
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {

    // Nacos配置
    private static final String DATA_ID = "gateway-routes.json";
    private static final String GROUP = "DEFAULT_GROUP";
    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;
    // 保存当前路由的ID，用于删除
    private final Set<String> routeIds = new HashSet<>();

    /**
     * 初始化路由配置监听器
     * 在Bean初始化完成后自动调用，用于首次加载路由配置并注册Nacos配置监听器
     *
     * @throws Exception 如果配置服务获取或监听器注册失败可能抛出异常
     */
    @PostConstruct
    public void initRouteConfigListener() throws Exception {
        // 1. 获取ConfigService
        ConfigService configService = nacosConfigManager.getConfigService();

        // 2. 首次加载配置并添加监听器
        String configInfo = configService.getConfigAndSignListener(
                DATA_ID,
                GROUP,
                5000,
                new Listener() {
                    /**
                     * 接收配置变更通知
                     * 当Nacos中的配置发生变化时被调用
                     *
                     * @param configInfo 变更后的配置内容
                     */
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        log.info("监听到路由配置变更");
                        updateRoutes(configInfo);
                    }

                    /**
                     * 获取执行器
                     * 返回null表示使用默认线程池执行配置变更回调
                     *
                     * @return 执行器实例
                     */
                    @Override
                    public Executor getExecutor() {
                        return null; // 使用默认线程池
                    }
                }
        );

        // 3. 首次启动时更新路由
        log.info("首次加载路由配置");
        updateRoutes(configInfo);
    }

    /**
     * 更新路由表
     * 解析配置信息并更新网关的路由定义
     *
     * @param configInfo 路由配置信息的JSON字符串
     */
    private void updateRoutes(String configInfo) {
        log.debug("开始更新路由，配置内容：{}", configInfo);

        try {
            // 1. 删除旧路由
            for (String routeId : routeIds) {
                routeDefinitionWriter.delete(Mono.just(routeId))
                        .subscribe(result -> log.debug("删除路由成功: {}", routeId));
            }
            routeIds.clear();

            // 2. 解析配置
            List<RouteDefinition> definitions = JSON.parseObject(configInfo, new TypeReference<List<RouteDefinition>>() {
            });

            // 3. 更新路由
            for (RouteDefinition definition : definitions) {
                routeDefinitionWriter.save(Mono.just(definition))
                        .subscribe(result -> log.debug("保存路由成功: {}", definition.getId()));
                routeIds.add(definition.getId());
            }

            log.info("路由更新完成，共{}条路由", definitions.size());

        } catch (Exception e) {
            log.error("更新路由失败", e);
        }
    }
}