# 📅 语音智能日历 - 后端 API
为语音智能日历小程序提供支持，实现用户认证、日程管理、AI 语音交互等核心能力。

---

## 📺 Demo 视频
[点击观看 Demo 视频]（。。。）
> 视频内容涵盖：微信登录流程、日程增删查、语音日程管理、AI 对话交互等核心功能演示。

---

## 1. 项目简介
本项目为语音智能日历小程序后端服务，基于 **Spring Boot 3** 开发，集成 MyBatis-Plus、Redis、阿里云语音服务与通义千问大模型，提供用户认证、日程 CRUD、AI 语音对话、智能工具调用等完整能力。

**主要特点：**
- 🔐 安全认证：JWT + Redis 实现微信登录、接口鉴权
- 📅 日程管理：支持新增、删除、按日期/月份查询
- 🤖 AI 交互：阿里云 ASR/TTS + 通义千问，支持语音指令
- 🛠️ 智能调用：AI 自动解析意图并执行日程操作
- 📝 规范架构：统一响应体、全局异常、接口文档

---

## 2. 技术栈
| 技术 | 说明 |
| --- | --- |
| 框架 | Spring Boot 3.2.5 |
| 核心 ORM | MyBatis-Plus |
| 数据库 | MySQL 8+ |
| 连接池 | Druid |
| 缓存 | Redis |
| 登录鉴权 | JWT + Redis |
| AI 模型 | 通义千问（Spring AI Alibaba） |
| 语音服务 | 阿里云 ASR / TTS |
| 接口文档 | Knife4j (OpenAPI3) |
| 工具库 | Hutool、Fastjson2、MapStruct |

---

## 3. 项目结构
```
src/
├── main/
│   ├── java/top/xym/voice/calendar/app/
│   │   ├── common/          # 通用模块、常量、枚举
│   │   ├── config/          # 全局配置类
│   │   ├── controller/      # 接口控制器
│   │   ├── exception/       # 全局异常处理
│   │   ├── handler/         # 字段自动填充
│   │   ├── interceptor/     # 登录拦截器
│   │   ├── mapper/          # 数据访问层
│   │   ├── model/           # 实体、DTO、VO
│   │   ├── service/         # 业务逻辑
│   │   ├── tool/            # AI 工具类
│   │   └── utils/           # 工具包
│   └── resources/
│       ├── mapper/          # XML 文件
│       ├── application.yml   # 主配置
│       └── application-dev.yml # 开发环境配置
└── test/                    # 测试代码
```

---

## 4. 快速开始

### 4.1 环境要求
- JDK 17+
- MySQL 8+
- Redis 6+
- Maven 3.6+

### 4.2 配置说明
- 项目已集成完整配置结构
- **yml 配置中的密钥、密码未上传**，需自行补充：
  - 数据库账号密码
  - Redis 密码
  - 阿里云 AccessKey
  - 通义千问 API-Key

### 4.3 启动项目
```bash
mvn clean install
mvn spring-boot:run
```

### 4.4 接口文档
访问地址：
```
http://localhost:8080/voice-calendar-app-api/doc.html#/home
```

---

## 5. 核心模块

### 5.1 用户认证模块
- 微信授权登录，自动注册用户
- JWT 签发令牌，Redis 缓存登录状态
- 统一登录拦截与接口鉴权
- 登出清理缓存，令牌失效

### 5.2 日程管理模块
- 日程新增、查询、删除完整 CRUD
- 支持按日期、按月份查询
- 用户数据隔离，安全可靠

### 5.3 AI 语音交互模块
- 阿里云 ASR：音频 Base64 → 文本
- 阿里云 TTS：文本 → 音频 Base64
- 通义千问意图解析
- AI 自动调用日程操作工具

---

## 6. 依赖说明
| 依赖 | 用途 |
| --- | --- |
| spring-boot-starter-web | Web 服务 |
| spring-boot-starter-data-redis | Redis 缓存 |
| mybatis-plus-spring-boot3-starter | ORM 框架 |
| druid-spring-boot-3-starter | 数据库连接池 |
| mysql-connector-j | MySQL 驱动 |
| knife4j-openapi3-jakarta-spring-boot-starter | 接口文档 |
| fastjson2 | 序列化工具 |
| hutool-all | 工具类库 |
| mapstruct | 对象属性转换 |
| spring-ai-alibaba-starter-dashscope | 通义千问大模型 |
| aliyun-java-sdk-core | 阿里云语音服务 |
| jaxb-api / jaxb-runtime | Java17 兼容适配 |

---

© 2026 语音智能日历 | 语音操控 · 轻松管理每一天
