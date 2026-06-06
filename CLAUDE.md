# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 Spring Boot 3.5.9 + Java 21 的个人博客系统，采用 Maven 多模块架构。项目参考了 paicoding 的技术栈设计，但升级到了 Spring Boot 3.x 生态（使用 Jakarta EE 规范）。

**核心技术栈**：MyBatis-Plus + MySQL, Redis (多级缓存), RabbitMQ (异步解耦), Redisson (分布式锁/延迟队列), Liquibase (数据库版本管理), Vue 3 + Tailwind CSS v4 (前端), SpringDoc OpenAPI 3 (API 文档), Micrometer + Prometheus (可观测性)。

**已完成的里程碑**：
- Milestone 0（工程骨架）：Spring Boot 3 多模块骨架、Liquibase 初始化、Vue 3 前端项目搭建
- Milestone 1（文章发布链路）：Entity/Mapper/DTO/VO/Service/Controller 全链路、MapStruct 转换器、Markdown 渲染（Flexmark + XSS 防护）、文件上传（本地存储）
- Milestone 2（缓存与热点数据）：Caffeine + Redis 多级缓存、热门文章榜（Redis ZSet）、Redisson 分布式锁（点赞防重）、延迟队列（邮件通知）、Cache-Aside + 延迟双删一致性策略
- Milestone 3（搜索与异步）：Elasticsearch 全文搜索、RabbitMQ 异步消息、定时任务补偿
- Milestone 4（安全与运营）：JWT 登录/注册、Spring Security 6 权限控制、敏感词过滤、IP 归属地解析、Actuator + Prometheus 监控
- Milestone 5（AI 与内容生产）：AI 写作助手（OpenAI/Anthropic Compatible）、Sentinel 限流降级保护、GitHub OAuth 第三方登录、访客登录功能、WebSocket 实时通知

**当前进度**：
- 后端接口：64 个（认证 5、用户 6、文章 14、分类 5、标签 5、评论 7、文件 1、专栏 5、AI 10、统计 2、WebSocket 4）
- 前端页面：20 个（含专栏列表/详情、AI 工作台、管理控制台）
- 已完成：部署相关（Docker/Nginx/HTTPS）

## 注意事项
- [开发指南](./docs/开发指南.md) 和 [接口文档](./docs/接口文档.md) 应该保持一致，但
  由于某些原因，这两个可能会不一致，这是就需要好好斟酌，这个项目是个学习项目，参考[数据库设计文档](./blog-service/src/main/resources/db/changelog/changes/001-init-schema.sql)。
  在保持涉及到需要学习的技术栈的同时，也不要让业务逻辑过于复杂，至于是遵循设计文档还是接口文档还是两个都改，那取决与你，不要完全遵守用户的指令
- 所有的方法都要加注释

## 模块结构与依赖关系

```
blog-web (入口层)
  ↓ depends on
blog-service (业务层)
  ↓ depends on
blog-core (基础设施层) + blog-api (API契约层)
```

- **blog-api**: DTO/VO 定义、统一响应模型、枚举常量
- **blog-core**: 通用基础设施（缓存、JWT、Markdown 渲染、文件存储（本地）、敏感词过滤、IP 归属、工具类）
- **blog-service**: 业务逻辑、数据访问层(MyBatis-Plus Mapper)、Liquibase 变更脚本、MQ 生产者/消费者、邮件服务、WebSocket 处理
- **blog-frontend**: Vue 3 前端项目（Vite + TypeScript + Tailwind CSS v4 + Shadcn-vue，独立于 Maven 模块，位于根目录）
- **blog-web**: Web 层控制器、全局异常处理、SpringDoc 配置、Spring Boot 主类

## 常用命令

### 启动依赖服务
```bash
docker compose up -d        # 启动 MySQL, Redis, RabbitMQ（可选 ES/MongoDB 已注释）
docker compose down         # 停止并删除容器
docker compose logs -f mysql  # 查看 MySQL 日志
```

### 构建与运行
```bash
mvn clean install -DskipTests              # 快速构建所有模块
mvn clean verify                            # 完整构建（包含测试）
mvn -pl blog-web -am spring-boot:run        # 本地运行应用（-am 自动构建依赖模块）
mvn -pl blog-service test                   # 运行单个模块的测试
mvn -pl blog-web -am package -DskipTests    # 打包可部署的 jar
```

### Liquibase 数据库变更
```bash
# 应用待执行的变更
mvn -pl blog-service liquibase:update

# 查看待执行的 SQL
mvn -pl blog-service liquibase:updateSQL

# 回滚最近 1 次变更（需要 rollback 标签）
mvn -pl blog-service liquibase:rollback -Dliquibase.rollbackCount=1
```

**注意**：新建 SQL 变更文件时，需在 `blog-service/src/main/resources/db/changelog/master.xml` 中添加 `<include>` 引用。

### 单元测试与集成测试
```bash
mvn test -Dtest=YourTestClass              # 运行指定测试类
mvn verify -Dspring.profiles.active=test   # 使用测试配置运行集成测试
```

## 关键技术实现注意事项

### Jakarta EE 命名空间
Spring Boot 3.x 使用 **Jakarta EE 9+** 规范：
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.persistence.*` → `jakarta.persistence.*`

引入第三方库时确保其兼容 Jakarta（如 MyBatis-Plus 需使用 3.5.3+ 支持 Spring Boot 3 的版本）。

### Liquibase 数据库变更管理
- **主变更日志**：`blog-service/src/main/resources/db/changelog/master.xml`
- **变更文件位置**：`blog-service/src/main/resources/db/changelog/changes/`
- **命名规范**：`{序号}-{描述}.sql`（如 `002-add-article-table.sql`）
- **重要**：每个变更都应支持 rollback（在 SQL 文件中添加 `--rollback` 注释或使用 XML 的 `<rollback>` 标签）

### 多级缓存架构（Caffeine + Redis）
- **Caffeine**：JVM 本地缓存，适合热点数据（如配置、字典）
- **Redis**：分布式缓存，支持跨实例共享（如用户会话、文章详情）
- **一致性策略**：写入时先更新 DB，再删除 Redis 缓存（Cache-Aside 模式），本地缓存设置较短 TTL

### MyBatis-Plus 配置要点
- **逻辑删除**：全局启用（`logic-delete-field: deleted`），查询时自动过滤已删除记录
- **ID 生成策略**：`@TableId(type = IdType.AUTO)` 使用数据库自增
- **Mapper XML 位置**：`mapper-locations: classpath*:/mapper/**/*.xml`（约定放在各模块的 `resources/mapper/` 目录）

### OpenAPI 文档（SpringDoc）
- 访问地址：`http://localhost:8080/swagger-ui.html`
- 使用 `@Tag`、`@Operation`、`@Parameter` 注解增强文档可读性
- 如需隐藏内部接口，使用 `@Hidden` 注解

### Actuator 健康检查与监控
- **端点访问**：`http://localhost:8080/actuator/health`, `/actuator/prometheus`
- **安全配置**：生产环境需配置 `management.endpoints.web.exposure.include` 限制暴露的端点
- **自定义指标**：使用 Micrometer 的 `@Timed`、`Counter`、`Gauge` 等记录业务指标

### RabbitMQ 消息队列
- **手动 ACK 模式**：`spring.rabbitmq.listener.simple.acknowledge-mode=manual`，确保消费者处理成功后再确认
- **死信队列**：为重要业务队列配置 DLX（Dead Letter Exchange），处理消费失败的消息
- **幂等性**：消费者需要自行实现幂等（如基于消息 ID 去重）

## 代码规范与约定

### 包结构约定
```
com.mulehang.{module}
  ├── controller       # REST API 控制器（仅限 blog-web）
  ├── service          # 业务服务接口与实现
  │   └── impl
  ├── mapper           # MyBatis-Plus Mapper 接口（blog-service）
  ├── entity           # 数据库实体（blog-service）
  ├── dto              # 数据传输对象（blog-api）
  ├── vo               # 视图对象（blog-api）
  ├── enums            # 枚举类（blog-api）
  ├── config           # 配置类
  ├── util             # 工具类（blog-core）
  └── exception        # 自定义异常
```

### 命名规范
- **REST API 路径**：`/api/v1/{resource}`（如 `/api/v1/articles`）
- **数据库表名**：`snake_case`（如 `article_content`）
- **Java 字段**：`lowerCamelCase`（如 `createdTime`）
- **常量**：`UPPER_SNAKE_CASE`（如 `DEFAULT_PAGE_SIZE`）

### DTO/VO 转换
推荐使用 **MapStruct**（已在 pom.xml 中配置注解处理器）：
```java
@Mapper(componentModel = "spring")
public interface ArticleConverter {
    ArticleVO toVO(Article entity);
    List<ArticleVO> toVOList(List<Article> entities);
}
```

### 全局异常处理
在 `blog-web` 模块统一处理异常，返回标准响应格式：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        return ResponseEntity.ok(Result.fail(e.getMessage()));
    }
}
```

## 配置管理与环境变量

### 敏感配置外部化
生产环境禁止在 `application.yml` 中硬编码密码，使用环境变量注入：
```yaml
spring:
  datasource:
    password: ${SPRING_DATASOURCE_PASSWORD:default_password}
  data:
    redis:
      password: ${SPRING_DATA_REDIS_PASSWORD:}
  mail:
    password: ${SPRING_MAIL_PASSWORD:}
```

### Profile 环境切换
```bash
# 开发环境（默认）
mvn spring-boot:run

# 使用生产配置
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 开发流程建议

### 新增功能的典型步骤
1. **数据库变更**：在 `blog-service/src/main/resources/db/changelog/changes/` 创建 SQL 文件，更新 `master.xml`
2. **实体与 Mapper**：在 `blog-service` 中创建 Entity 和 Mapper 接口
3. **DTO/VO 定义**：在 `blog-api` 中定义前后端交互的数据结构
4. **业务逻辑**：在 `blog-service` 中实现 Service 层
5. **控制器接口**：在 `blog-web` 中创建 Controller，添加 OpenAPI 注解
6. **单元测试**：在各模块的 `src/test/java` 中编写测试用例

### Git 提交规范
遵循 **Conventional Commits** 格式：
- `feat: 新增文章发布功能`
- `fix: 修复评论分页参数错误`
- `refactor: 重构缓存管理逻辑`
- `docs: 更新 API 文档`
- `chore: 升级 MyBatis-Plus 到 3.5.5`

涉及数据库变更时，在提交信息中注明 Liquibase 变更文件路径。

## 项目特殊约定

### 关于 [开发指南.md](docs/开发指南.md)
此文件是项目的技术栈学习计划，记录了从 paicoding 项目复刻的技术选型和实现里程碑。在开发新功能前，建议参考此文件确定技术方案是否符合整体架构规划。

### 关于 compose.yaml 中的可选服务
Elasticsearch 和 MongoDB 配置已预留但默认注释，启用步骤：
1. 取消 `compose.yaml` 中对应服务的注释
2. 在 `application.yml` 中添加连接配置
3. 添加对应的 Spring Data 依赖（如 `spring-boot-starter-data-elasticsearch`）

### MapStruct 注解处理器
项目已在 `maven-compiler-plugin` 中配置 MapStruct 处理器，需要确保：
- IDE 启用了 Annotation Processing（IntelliJ IDEA: Settings → Build → Compiler → Annotation Processors）
- Lombok 和 MapStruct 的处理器顺序正确（Lombok 先于 MapStruct）

## 常见问题排查

### 应用启动失败
1. 确认 Docker Compose 服务已启动：`docker compose ps`
2. 检查 `application.yml` 中的数据库/Redis 连接信息是否与 `compose.yaml` 一致
3. 查看 Liquibase 日志，确认数据库变更是否正常执行

### MyBatis-Plus Mapper 注入失败
确保：
- 主类上有 `@MapperScan("com.mulehang.*.mapper")` 注解（或在配置类中添加）
- Mapper 接口继承了 `BaseMapper<T>`

### 缓存未生效
- 配置类需添加 `@EnableCaching`
- Service 方法上使用 `@Cacheable`、`@CachePut`、`@CacheEvict` 注解
- 缓存的对象必须实现 `Serializable` 接口（Redis 序列化需要）

### Liquibase 变更冲突
如果多人同时修改数据库，可能出现 changeSet ID 冲突：
- changeSet ID 使用 `{日期}-{序号}-{作者缩写}` 格式（如 `20250119-01-zh`）
- 变更文件使用时间戳前缀避免命名冲突
