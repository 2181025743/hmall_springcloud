package com.hmall.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@MapperScan("com.hmall.cart.mapper")
@EnableFeignClients(basePackages = "com.hmall.api.client")  // 指定扫描包
@SpringBootApplication
@ComponentScan(basePackages = {"com.hmall.cart", "com.hmall.api", "com.hmall.common"})  // 扫描多个包
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }

}