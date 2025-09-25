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

### Configuration Management
- Each service has:
  - `application.yaml`: Base configuration
  - `application-local.yaml`: Local development settings
  - `application-dev.yaml`: Development environment settings
  - `bootstrap.yaml`: Nacos configuration

### Database Access
- MyBatis-Plus for ORM
- Entity classes in `domain/po` packages
- Mapper interfaces in `mapper` packages
- Service layer follows IService pattern from MyBatis-Plus

### Security
- JWT-based authentication handled by gateway
- Token validation in AuthorizeGlobalFilter
- Excluded paths configured in AuthProperties