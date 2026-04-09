-- 简化的NL2SQL Assistant数据库初始化脚本

-- 数据源表
CREATE TABLE IF NOT EXISTS datasource (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL COMMENT '数据源名称',
    type VARCHAR(50) NOT NULL COMMENT '数据源类型：mysql',
    host VARCHAR(255) NOT NULL COMMENT '主机地址',
    port INT NOT NULL COMMENT '端口号',
    database_name VARCHAR(255) NOT NULL COMMENT '数据库名称',
    username VARCHAR(255) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    connection_url VARCHAR(1000) COMMENT '完整连接URL',
    status VARCHAR(50) DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用',
    description TEXT COMMENT '描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '数据源表';

-- 知识库表（RAG向量存储）
CREATE TABLE IF NOT EXISTS knowledge (
    id INT NOT NULL AUTO_INCREMENT,
    datasource_id INT NOT NULL COMMENT '关联的数据源ID',
    content TEXT NOT NULL COMMENT '知识内容',
    content_type VARCHAR(50) DEFAULT 'schema' COMMENT '内容类型：schema-表结构，comment-注释，sample-示例数据',
    table_name VARCHAR(255) COMMENT '关联的表名',
    column_name VARCHAR(255) COMMENT '关联的字段名',
    embedding_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '向量化状态：PENDING-待处理，COMPLETED-已完成，FAILED-失败',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_embedding_status (embedding_status),
    INDEX idx_table_name (table_name),
    FOREIGN KEY (datasource_id) REFERENCES datasource(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '知识库表';

-- 会话表
CREATE TABLE IF NOT EXISTS chat_session (
    id VARCHAR(36) NOT NULL COMMENT '会话ID（UUID）',
    datasource_id INT NOT NULL COMMENT '数据源ID',
    title VARCHAR(255) DEFAULT '新对话' COMMENT '会话标题',
    status VARCHAR(50) DEFAULT 'active' COMMENT '状态：active-活跃，archived-归档',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (datasource_id) REFERENCES datasource(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '聊天会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(36) NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：user-用户，assistant-助手，system-系统',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(50) DEFAULT 'text' COMMENT '消息类型：text-文本，sql-SQL查询，result-查询结果，error-错误',
    metadata JSON COMMENT '元数据（JSON格式）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_session_id (session_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '消息表';

-- 模型配置表
CREATE TABLE IF NOT EXISTS model_config (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    llm_model VARCHAR(100) DEFAULT 'qwen-plus' COMMENT 'LLM模型名称',
    embedding_model VARCHAR(100) DEFAULT 'text-embedding-v3' COMMENT 'Embedding模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    max_tokens INT DEFAULT 2000 COMMENT '最大token数',
    top_p DECIMAL(3,2) DEFAULT 0.9 COMMENT 'Top-P参数',
    is_default TINYINT DEFAULT 0 COMMENT '是否为默认配置：0-否，1-是',
    description TEXT COMMENT '配置描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_is_default (is_default)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '模型配置表';

-- 插入默认配置
INSERT INTO model_config (name, llm_model, embedding_model, temperature, max_tokens, top_p, is_default, description)
VALUES ('默认配置', 'qwen-plus', 'text-embedding-v3', 0.7, 2000, 0.9, 1, '系统默认的模型配置');

-- 示例数据源
INSERT INTO datasource (name, type, host, port, database_name, username, password, status, description)
VALUES ('示例MySQL', 'mysql', '127.0.0.1', 3306, 'sample_db', 'root', 'root', 'active', '示例数据源配置');
