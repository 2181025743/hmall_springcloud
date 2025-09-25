package com.hmall.api.client.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ItemClient 的降级处理工厂类
 * 当调用 item-service 失败时（异常或被限流），会走这里的降级逻辑
 */
@Slf4j
@Component
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable throwable) {
        // 返回 ItemClient 的降级实现
        return new ItemClient() {

            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                // 记录错误日志
                log.error("查询商品服务失败，ids: {}, 原因: {}", ids, throwable.getMessage());
                // 返回空集合，避免前端报错
                return Collections.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                // 扣减库存是关键业务，失败时记录日志并抛出异常
                log.error("扣减商品库存失败，原因: {}", throwable.getMessage(), throwable);
                throw new RuntimeException("库存扣减失败，请稍后重试", throwable);
            }
        };
    }
}