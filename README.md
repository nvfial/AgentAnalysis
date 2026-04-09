# NL2SQL 智能数据分析助手

<div align="center">

**基于 Spring AI Alibaba 的个人 NL2SQL 项目**

*自然语言转 SQL | RAG 检索增强 | SSE 流式输出 | Vue 3 前端*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green)](https://spring.io/projects/spring-boot)
[![Spring AI Alibaba](https://img.shields.io/badge/Spring%20AI%20Alibaba-1.0.0.4-blue)](https://github.com/alibaba/spring-ai-alibaba)
[![Vue](https://img.shields.io/badge/Vue-3.4-42b883)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-red)](LICENSE)

</div>

---

## 📖 项目简介

这是一个精简版的 **NL2SQL 智能数据分析助手**，基于 Spring AI Alibaba 实现自然语言转 SQL 查询功能。项目保留了核心能力，同时移除了复杂的企业级功能，适合个人学习和二次开发。

### 核心技术栈

| 技术 | 说明 |
|:---|:---|
| **后端** | Spring Boot 3.3.0 + Spring AI Alibaba 1.0.0.4 |
| **前端** | Vue 3 + Element Plus + ECharts |
| **数据库** | MySQL + Elasticsearch (向量存储) |
| **AI 模型** | 阿里云 DashScope (通义千问) |
| **流式输出** | Server-Sent Events (SSE) |

---

## ✨ 核心特性

- 💬 **自然语言转 SQL**：用自然语言描述查询需求，自动生成 SQL 语句
- 🔍 **RAG 检索增强**：基于向量数据库的 Schema 语义检索，提升 SQL 准确率
- ⚡ **SSE 流式输出**：实时展示 AI 思考过程和生成结果
- 📊 **Markdown 渲染**：SQL 代码高亮展示，支持表格数据可视化
- 🗄️ **多数据源支持**：MySQL、PostgreSQL 等主流数据库
- 💾 **会话管理**：支持多会话上下文记忆

---

## 🏗️ 项目结构

```
├── data-agent-management/     # 后端 Spring Boot 项目
│   ├── src/main/java/
│   │   └── com/alibaba/cloud/ai/dataagent/
│   │       ├── controller/    # REST API 控制器
│   │       ├── service/       # 业务服务层
│   │       ├── entity/        # 实体类
│   │       ├── mapper/        # MyBatis Mapper
│   │       └── util/          # 工具类
│   └── src/main/resources/
│       └── application.yml    # 配置文件
│
└── data-agent-ui/             # 前端 Vue 3 项目
    ├── src/
    │   ├── App.vue           # 主组件
    │   ├── main.js           # 入口文件
    │   └── styles/           # 样式文件
    └── vite.config.js        # Vite 配置
```

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 5.7+
- Elasticsearch 8.x (可选，用于 RAG)

### 1. 导入数据库

```sql
mysql -u root -p < data-agent-management/src/main/resources/sql/schema.sql
```

### 2. 配置环境变量

```bash
# 阿里云 DashScope API Key
export DASHSCOPE_API_KEY=your-api-key

# Elasticsearch 配置 (可选)
export ELASTICSEARCH_HOST=127.0.0.1
export ELASTICSEARCH_PORT=9200
```

### 3. 启动后端

```bash
cd data-agent-management
mvn spring-boot:run
# 服务运行在 http://localhost:8065
```

### 4. 启动前端

```bash
cd data-agent-ui
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

### 5. 访问系统

打开浏览器访问 http://localhost:5173

---

## 📡 API 接口

| 接口 | 方法 | 说明 |
|:---|:---|:---|
| `/api/datasources` | GET | 获取所有数据源 |
| `/api/datasources` | POST | 创建数据源 |
| `/api/datasources/{id}/test` | POST | 测试连接 |
| `/api/datasource/{id}/sessions` | GET | 获取会话列表 |
| `/api/sessions/{id}/messages` | GET | 获取消息历史 |
| `/api/stream/search` | GET | SSE 流式 NL2SQL |

---

## 🎨 界面预览

- **深色主题**：GitHub Dark 风格
- **流式响应**：实时展示 AI 生成过程
- **代码高亮**：SQL 语句语法高亮
- **会话管理**：侧边栏快速切换会话

---

## 📝 License

Apache License 2.0

---

*Made with ❤️ by NL2SQL Assistant*
