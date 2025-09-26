# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Cloud microservices e-commerce platform (hmall) using:

- Spring Boot 2.7.12
- Spring Cloud 2021.0.3
- Spring Cloud Alibaba 2021.0.4.0
- Java 11
- MyBatis-Plus 3.4.3
- MySQL 8.0.23

## Architecture

### Microservices Structure

- **hm-gateway** (port 8080): API Gateway using Spring Cloud Gateway
- **cart-service** (port 8082): Shopping cart management
- **item-service**: Product catalog management
- **user-service**: User authentication and management
- **trade-service**: Order processing
- **pay-service**: Payment processing
- **hm-api**: Feign client interfaces for inter-service communication
- **hm-common**: Shared utilities and configurations

### Service Discovery & Configuration

- Nacos server at `101.42.238.233:8848` for service discovery and configuration management
- Sentinel for circuit breaking and flow control (dashboard at `localhost:8858`)
- Services use bootstrap.yaml for Nacos configuration with shared configs:
    - shared-jdbc.yaml
    - shared-log.yaml
    - shared-swagger.yaml
    - shared-auth.yaml
- Gateway handles dynamic route configuration via Nacos config `gateway-routes.json`

### Business Flow

1. **Search Flow**: Frontend → hm-gateway → item-service → Database
2. **Cart Flow**: Frontend → hm-gateway → cart-service → item-service → Database
3. **Order Flow**: Frontend → hm-gateway → trade-service → (cart-service, item-service) → Database
4. **Payment Flow**: Frontend → hm-gateway → pay-service → (user-service, trade-service) → Database

## Build Commands

```bash
# Build entire project
mvn clean package

# Build specific service
mvn clean package -pl cart-service -am

# Skip tests during build
mvn clean package -DskipTests

# Run specific service
java -jar [service-name]/target/[service-name].jar
```

## Development Workflow

### Running Services Locally

1. Ensure Nacos is accessible at configured address
2. Start services in order: gateway → user-service → other services
3. Services run with `spring.profiles.active=local` by default
4. Frontend runs at `http://localhost:18080`

### Testing

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl cart-service

# Run specific test class
mvn test -Dtest=CartControllerTest
```

## Key Patterns

### Service Communication

- Services communicate via Feign clients defined in hm-api module
- All requests go through hm-gateway which handles:
    - CORS configuration (allows http://localhost:18080)
    - JWT authentication
    - Route forwarding based on service discovery
- Dynamic route configuration is loaded from Nacos configuration

### Configuration Management

- Each service has:
    - `application.yaml`: Base configuration
    - `application-local.yaml`: Local development settings
    - `application-dev.yaml`: Development environment settings
    - `bootstrap.yaml`: Nacos configuration
- Gateway routes are configured dynamically via Nacos config

### Database Access

- MyBatis-Plus for ORM
- Entity classes in `domain/po` packages
- Mapper interfaces in `mapper` packages
- Service layer follows IService pattern from MyBatis-Plus

### Security

- JWT-based authentication handled by gateway
- Token validation in AuthorizeGlobalFilter
- Excluded paths configured in AuthProperties

### Critical Business Logic Points

- **Price Consistency**: Cart and Order services always fetch latest prices from item-service to ensure accuracy
- **Stock Management**: Trade service handles inventory deduction with atomic operations
- **Order Status Sync**: Payment service notifies trade service on payment success
- **Fault Tolerance**: Feign clients have fallback mechanisms for service failures

# 用户的完整环境信息汇总

## 一、云服务器信息

```yaml
服务器信息:
  主机名: VM-8-13-opencloudos
  公网IP: 101.42.238.233
  内网IP: 10.2.8.13
  操作系统: OpenCloudOS
  SSH端口: 22
  SSH用户: root
```

## 二、MySQL信息

```yaml
MySQL Docker容器:
  容器名: mysql
  镜像: mysql:8.0
  端口映射: 0.0.0.0:3306 -> 3306
  数据目录: /root/mysql-data/

MySQL账户密码:
  root@%: Hm#2024@MySQL  # Windows远程连接使用
  root@localhost: Hm#2024@MySQL  # 服务器本地连接使用

  运行中的服务
  YAML

MySQL容器:
  容器名: mysql
  镜像: mysql:8.0
  端口: 0.0.0.0:3306->3306
  root密码: Hm#2024@MySQL
  数据目录: /root/mysql-data/

Redis容器:
  容器名: redis-hmdp
  镜像: redis:7.2-alpine
  端口: 0.0.0.0:6379->6379
  密码: Redis@2024hmdp
  配置文件: /docker/redis/conf/redis.conf

Nacos容器:
  容器名: nacos
  镜像: nacos/nacos-server:v2.3.0
  端口: 8848, 9848-9849
  账号: nacos/nacos
  配置文件: /opt/nacos/conf/application.properties
  数据库: nacos_config

Seata容器:
  容器名: seata-server
  镜像: seataio/seata-server:1.5.2
  端口: 7099->7091, 8099->8091
  控制台账号: admin/admin
  运行模式: file模式（未注册到Nacos）
  配置文件: /docker/seata/config/application-minimal.yml
  内存限制: 256M

本地项目路径:
  后端: D:\project\java\hm-dianping
  前端: D:\project\nginx\html\hmdp


## 六、项目配置（application.yml）
  ```yaml
server:
  port: 8081
spring:
  application:
    name: hmdp
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://101.42.238.233:3306/hmdp?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: root
    password: Hm#2024@MySQL  # 使用root@%的密码
  redis:
    host: 101.42.238.233
    port: 6379
    password: Redis@2024hmdp
    timeout: 5000ms
    database: 0
    lettuce:
      pool:
        max-active: 10
        max-idle: 10
        min-idle: 1
        max-wait: -1ms
        time-between-eviction-runs: 10s
```

数据库信息
YAML

MySQL数据库:

- hmall: 业务数据库（包含用户、商品、订单等表）
- nacos_config: Nacos配置数据库
- seata: Seata事务数据库（已创建表结构）

备份信息:
备份脚本: /root/mysql_backup.sh
备份目录: /root/mysql_backups/
定时任务: 每天凌晨2点自动备份

## 七、备份相关

```yaml
MySQL备份:
  备份脚本: /root/mysql_backup.sh
  备份目录: /root/mysql_backups/
  定时任务: 每天凌晨2点自动备份
  保留时间: 7天

手动备份数据:
  /root/hmall.sql (36MB)
  /root/hm-item.sql (36MB)
```

2. Seata启动失败
   问题：library initialization failed - unable to allocate file descriptor table
   原因：文件描述符限制和内存不足
   解决方案：
   Bash

# 1. 增加文件描述符限制

ulimit -n 65536

# 2. Docker容器启动时添加ulimit参数

--ulimit nofile=65536:65536

# 3. 限制内存使用

-m 256m

# 4. 使用file模式而非nacos模式

3. 服务器资源紧张
   现状：1.9G总内存，Nacos占用879MB
   应对：Seata使用最小化配置，file模式运行

## 八、安全相关

```yaml
已安装的安全工具:
  - fail2ban (SSH防暴力破解)
  - 定时备份脚本



## 九、常用命令
  ```bash
  # MySQL连接
  docker exec -it mysql mysql -uroot -p123456  # 服务器上执行
  mysql -h101.42.238.233 -uroot -pHm#2024@MySQL  # Windows上执行

  # Redis连接
  docker exec -it redis-hmdp redis-cli -a Redis@2024hmdp

  # 查看容器
  docker ps

  # 备份MySQL
  /root/mysql_backup.sh

  # 查看日志
  docker logs mysql
  docker logs redis-hmdp
```

2. 访问地址
   Nacos控制台：http://101.42.238.233:8848/nacos (nacos/nacos)
   Seata控制台：http://101.42.238.233:7099 (admin/admin)
   前端项目：http://localhost:18080
   API网关：http://101.42.238.233:8080

## 十、注意事项

2. 已放行所有端口，尽管存在安全风险
3. 建议定期检查 `/root/mysql_backups/` 确保备份正常
4. 曾遭受勒索攻击，已删除RECOVER_YOUR_DATA数据库

nacos中配置：
shared-jdbc.yaml：
spring:
datasource:
url: jdbc:mysql://${hm.db.host:101.42.238.233}:${hm.db.port:
3306}/${hm.db.database}?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
driver-class-name: com.mysql.cj.jdbc.Driver
username: ${hm.db.username:root}
password: ${hm.db.pw:123456}
mybatis-plus:
configuration:
default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
global-config:
db-config:
update-strategy: not_null
id-type: auto

shared-log.yaml：
logging:
level:
com.hmall: debug
pattern:
dateformat: HH:mm:ss:SSS
file:
path: "logs/${spring.application.name}"

shared-swagger.yaml：

# 注意：spring.mvc 必须独立配置，不能在 knife4j 下面

spring:
mvc:
pathmatch:
matching-strategy: ant_path_matcher

knife4j:
enable: true
openapi:
title: ${hm.swagger.title:黑马商城接口文档}
description: ${hm.swagger.desc:黑马商城接口文档}
email: 3351163616@qq.com
concat: 杨潇
url: https://www.itcast.cn
version: v1.0.0
group:
default:
group-name: default
api-rule: package
api-rule-resources:
- ${hm.swagger.package}

shared-auth.yaml:

# shared-auth.yaml

hm:
jwt:
location: classpath:hmall.jks
alias: hmall
password: hmall123
tokenTTL: 30m
auth:
excludePaths:
- /search/**
- /users/login
- /items/**
- /hi

gateway-routes.json：
[
{
"id": "item-service",
"uri": "lb://item-service",
"predicates": [
{
"name": "Path",
"args": {
"_genkey_0": "/items/**",
"_genkey_1": "/search/**"
}
}
]
},
{
"id": "user-service",
"uri": "lb://user-service",
"predicates": [
{
"name": "Path",
"args": {
"_genkey_0": "/users/**",
"_genkey_1": "/addresses/**"
}
}
]
},
{
"id": "cart-service",
"uri": "lb://cart-service",
"predicates": [
{
"name": "Path",
"args": {
"_genkey_0": "/carts/**"
}
}
]
},
{
"id": "trade-service",
"uri": "lb://trade-service",
"predicates": [
{
"name": "Path",
"args": {
"_genkey_0": "/orders/**"
}
}
]
},
{
"id": "pay-service",
"uri": "lb://pay-service",
"predicates": [
{
"name": "Path",
"args": {
"_genkey_0": "/pay-orders/**"
}
}
]
}
]

        代码完整性：

提供完整可执行的代码，禁止使用占位符如"此处放你的某部分代码"
所有代码必须即拿即用
配置信息处理：

使用上述真实配置信息填充代码
MySQL密码使用：123456
IP地址使用：101.42.238.233（公网）或10.2.8.13（内网）
其它
禁止行为： 不得使用示例值如"password:000000（改为你的密码）"
未知信息处理：

若确实缺少必要配置信息，使用醒目注释标记：
Java

// ⚠️ 注意：此处需要填写[具体信息名称]，用户环境中未提供
在代码块外用粗体警告说明缺失的信息
知识讲解：

对复杂概念、高级特性、不常见方法提供详细解释
使用注释或独立段落说明原理和用法
提供相关最佳实践建议
如果你需要日志排查你给我日志排查代码请加上日志限制代码，例如限制100行，200行，因为我要把日志发给你，看你需求设置限制行数
如果在代码中有需要我改成仅我知道的密码之类的你不仅要在代码或命令里注释，也要在代码块外说明，因为我一般不看代码或命令直接就复制粘贴了
以上是我的信息，请作为上下文记忆