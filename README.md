# mulehang-blog

> 基于 Spring Boot 3.5.9 + Java 21 的全栈博客系统学习实践项目。

## 🚀 项目简介

`mulehang-blog` 是一个旨在通过实践掌握现代 Java 后端技术栈的个人博客系统。项目采用多模块架构，集成了缓存、消息队列、搜索引擎及 AI 辅助内容生产等核心功能。
至于为什么要叫`mulehang-blog`并且我的用户名也叫`mulehang`，暂且按下不表。

## 🛠 技术栈

**后端**:
- **核心框架**: Java 21, Spring Boot 3.5.9
- **持久层**: MySQL 8.0, MyBatis-Plus, Liquibase (数据库版本管理)
- **缓存/中间件**: Redis, RabbitMQ, Redisson (分布式锁/延迟队列)
- **AI/限流**: OpenAI/Anthropic Compatible (AI 写作助手), Sentinel (限流熔断)
- **认证**: JWT, GitHub OAuth
- **文档/监控**: Knife4j (OpenAPI3), Actuator + Prometheus
- **工具库**: MapStruct, Lombok, Flexmark (Markdown 渲染)

**前端**:
- **框架**: Vue 3 + TypeScript + Vite
- **样式**: Tailwind CSS v4 (CSS-first)
- **组件库**: Shadcn-vue
- **状态管理**: Pinia + Vue Router
- **HTTP 客户端**: Axios

## 📂 项目结构

```text
mulehang-blog
├── blog-api        # 接口层：DTO/VO 定义、外部接口协议
├── blog-core       # 核心层：基础工具类、通用常量、异常处理
├── blog-service    # 业务层：核心业务逻辑、Mapper、Entity、Service
├── blog-frontend   # 前端：Vue 3 + Vite + Tailwind CSS v4 + Shadcn-vue
├── blog-web        # 启动层：项目配置、Controller、主启动类
└── docs            # 文档：开发指南、技术栈规划
```


## 🛠️ 环境要求

- **JDK**: 21
- **Maven**: 3.9+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **RabbitMQ**: 3.x+ 非必需
- **Elasticsearch**: 8.x+ 非必需
- **Node.js**: 18+ (前端开发)
- **pnpm**: 8+ (前端包管理器)
## 🚦 快速启动

1. **配置环境**: 修改 `blog-web/src/main/resources/application.yml` 中的数据源及中间件连接信息。
2. **构建项目**:
   ```powershell
   mvn clean install
   ```
3. **启动应用**: 运行 `com.mulehang.BlogWebApplication` 启动类。
4. **访问 API 文档**: 项目启动后访问 `http://localhost:8080/doc.html` 查看 API 文档。

### 前端启动

1. **进入前端目录**:
   ```powershell
   cd blog-frontend
   ```
2. **安装依赖**:
   ```powershell
   pnpm install
   ```
3. **启动开发服务器**:
   ```powershell
   pnpm dev
   ```
4. **访问前端**: 浏览器打开 `http://localhost:5173`

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可。
