package com.hmall.cart.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelConfig {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化流控规则（可选，也可以在 Dashboard 配置）
     */
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 为 /carts/test 接口设置流控规则
        FlowRule testRule = new FlowRule();
        testRule.setResource("/carts/test");
        testRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        testRule.setCount(10);  // QPS 限制为 10
        rules.add(testRule);

        // 为所有 /carts 接口设置流控规则
        FlowRule cartsRule = new FlowRule();
        cartsRule.setResource("/carts");
        cartsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        cartsRule.setCount(20);
        rules.add(cartsRule);

        FlowRuleManager.loadRules(rules);
    }
}