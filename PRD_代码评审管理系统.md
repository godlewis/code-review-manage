# 团队代码评审管理系统 PRD

## 1. 产品概述

### 1.1 产品背景
为了提升团队代码质量，建立规范的代码评审流程，需要开发一个智能化的代码评审管理系统，支持自动分配评审任务、记录评审结果、智能汇总问题并跟踪整改情况。

### 1.2 产品目标
- 建立规范化的代码评审流程
- 提高代码质量和团队协作效率
- 通过AI智能汇总，提升问题分析能力
- 建立完整的问题跟踪和整改机制

### 1.3 目标用户
- **开发人员**：执行代码评审，提交评审记录，查看和整改问题
- **团队负责人**：管理团队评审进度，查看统计数据，汇总问题
- **架构师**：进行周度总结，制定改进策略

## 2. 功能需求

### 2.1 用户管理模块

#### 2.1.1 角色权限
- **开发人员**
  - 查看分配给自己的评审任务
  - 提交代码评审记录
  - 查看自己的问题列表
  - 提交整改记录
  - 查看待办提醒

- **团队负责人**
  - 开发人员的所有权限
  - 查看团队评审进度
  - 查看团队成员统计数据
  - 智能汇总小组问题
  - 管理团队成员

- **架构师**
  - 团队负责人的所有权限
  - 进行周度汇总总结
  - 查看跨团队数据
  - 制定评审标准和规范

#### 2.1.2 用户信息
- 用户ID、姓名、邮箱、角色
- 所属团队、技能标签
- 注册时间、最后登录时间

### 2.2 评审分配模块

#### 2.2.1 自动分配算法
- **分配原则**
  - 每周自动生成新的评审配对
  - 确保检查者和被检查者在近期周期内不重复
  - 考虑技能匹配度和工作负载均衡
  - 支持手动调整分配结果

#### 2.2.2 分配规则配置
- 避重周期设置（默认4周）
- 每人每周评审任务数量
- 技能匹配权重
- 特殊分配规则（如新人优先安排经验丰富的评审者）

### 2.3 代码评审记录模块

#### 2.3.1 评审记录字段
- **基本信息**
  - 评审ID、评审者、被评审者
  - 评审日期、代码提交版本
  - 评审状态（进行中/已完成/需整改）

- **代码信息**
  - 代码截图（支持多张）
  - 代码文件路径
  - 代码行号范围
  - 代码仓库链接

- **问题详情**
  - 问题类型（功能缺陷/性能问题/安全漏洞/代码规范/设计问题）
  - 问题严重级别（严重/一般/轻微/建议）
  - 问题描述
  - 改进建议
  - 参考资料链接

- **评审结果**
  - 总体评分（1-10分）
  - 评审总结
  - 是否需要重新评审

#### 2.3.2 评审记录操作
- 创建评审记录
- 编辑评审记录
- 提交评审记录
- 查看评审历史
- 导出评审报告

### 2.4 问题管理模块

#### 2.4.1 问题列表
- **个人问题列表**
  - 显示个人所有待整改问题
  - 按严重级别和时间排序
  - 支持筛选和搜索

- **团队问题列表**
  - 显示团队所有问题统计
  - 按人员、类型、级别分组
  - 支持导出和分析

#### 2.4.2 整改跟踪
- 整改记录提交
- 整改前后代码对比
- 整改验证和确认
- 整改效果评估

### 2.5 智能汇总模块

#### 2.5.1 AI问题汇总
- **小组问题汇总**（团队负责人）
  - 自动分析团队问题模式
  - 识别高频问题类型
  - 生成改进建议
  - 支持人工编辑和补充

- **周度总结汇总**（架构师）
  - 跨团队问题趋势分析
  - 技术债务评估
  - 培训需求识别
  - 流程改进建议

#### 2.5.2 AI功能特性
- 自然语言处理问题描述
- 问题分类和聚类
- 趋势分析和预测
- 智能推荐解决方案

### 2.6 统计分析模块

#### 2.6.1 个人统计
- 评审完成率
- 问题发现数量和质量
- 整改及时率
- 个人成长趋势

#### 2.6.2 团队统计
- 团队评审覆盖率
- 问题分布统计
- 代码质量趋势
- 团队协作效率

#### 2.6.3 全局统计
- 跨团队对比分析
- 技术栈问题分布
- 评审效果评估
- ROI分析

### 2.7 提醒通知模块

#### 2.7.1 待办提醒
- 新评审任务分配提醒
- 评审截止时间提醒
- 整改任务提醒
- 复查任务提醒

#### 2.7.2 通知方式
- 系统内消息
- 邮件通知
- 企业微信/钉钉集成
- 移动端推送

## 3. 技术架构

### 3.1 技术栈
- **后端**：Spring Boot 2.7+
- **前端**：Vue 3 + Element Plus
- **数据库**：MySQL 8.0+
- **缓存**：Redis
- **消息队列**：RabbitMQ
- **AI服务**：集成OpenAI API或本地大模型

### 3.2 系统架构
```
前端层 (Vue 3)
├── 用户界面组件
├── 状态管理 (Vuex/Pinia)
└── 路由管理 (Vue Router)

API网关层
├── 认证授权
├── 限流熔断
└── 日志监控

业务服务层 (Spring Boot)
├── 用户管理服务
├── 评审分配服务
├── 评审记录服务
├── 问题管理服务
├── 智能汇总服务
└── 统计分析服务

数据访问层
├── MySQL (主数据)
├── Redis (缓存)
└── 文件存储 (OSS)

外部集成
├── AI服务接口
├── 代码仓库集成
└── 通知服务集成
```

## 4. 数据库设计

### 4.1 核心表结构

#### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role ENUM('DEVELOPER', 'TEAM_LEADER', 'ARCHITECT') NOT NULL,
    team_id BIGINT,
    skills JSON,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 团队表 (teams)
```sql
CREATE TABLE teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    leader_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 评审分配表 (review_assignments)
```sql
CREATE TABLE review_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_start_date DATE NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE') DEFAULT 'PENDING',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    INDEX idx_week_reviewer (week_start_date, reviewer_id),
    INDEX idx_week_reviewee (week_start_date, reviewee_id)
);
```

#### 评审记录表 (review_records)
```sql
CREATE TABLE review_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    code_repository VARCHAR(500),
    code_file_path VARCHAR(500),
    code_line_start INT,
    code_line_end INT,
    code_version VARCHAR(100),
    overall_score TINYINT CHECK (overall_score BETWEEN 1 AND 10),
    summary TEXT,
    status ENUM('DRAFT', 'SUBMITTED', 'REVIEWED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 代码截图表 (code_screenshots)
```sql
CREATE TABLE code_screenshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_record_id BIGINT NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(200),
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 问题记录表 (issues)
```sql
CREATE TABLE issues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_record_id BIGINT NOT NULL,
    issue_type ENUM('FUNCTIONAL_BUG', 'PERFORMANCE', 'SECURITY', 'CODE_STYLE', 'DESIGN') NOT NULL,
    severity ENUM('CRITICAL', 'MAJOR', 'MINOR', 'SUGGESTION') NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    suggestion TEXT,
    reference_links JSON,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 整改记录表 (fix_records)
```sql
CREATE TABLE fix_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    fixer_id BIGINT NOT NULL,
    fix_description TEXT NOT NULL,
    before_code_url VARCHAR(500),
    after_code_url VARCHAR(500),
    fix_commit_hash VARCHAR(100),
    status ENUM('SUBMITTED', 'VERIFIED', 'REJECTED') DEFAULT 'SUBMITTED',
    verified_by BIGINT,
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 智能汇总表 (ai_summaries)
```sql
CREATE TABLE ai_summaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    summary_type ENUM('TEAM_WEEKLY', 'ARCHITECT_WEEKLY') NOT NULL,
    team_id BIGINT,
    week_start_date DATE NOT NULL,
    ai_content TEXT NOT NULL,
    human_edited_content TEXT,
    created_by BIGINT NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 提醒通知表 (notifications)
```sql
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('ASSIGNMENT', 'DEADLINE', 'FIX_REQUIRED', 'REVIEW_REQUIRED') NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    related_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 接口设计

### 5.1 用户管理接口
- POST /api/auth/login - 用户登录
- POST /api/auth/logout - 用户登出
- GET /api/users/profile - 获取用户信息
- PUT /api/users/profile - 更新用户信息

### 5.2 评审分配接口
- GET /api/assignments/current - 获取当前周评审分配
- GET /api/assignments/history - 获取历史分配记录
- POST /api/assignments/generate - 生成新周分配（管理员）

### 5.3 评审记录接口
- GET /api/reviews - 获取评审记录列表
- POST /api/reviews - 创建评审记录
- PUT /api/reviews/{id} - 更新评审记录
- GET /api/reviews/{id} - 获取评审记录详情
- POST /api/reviews/{id}/submit - 提交评审记录

### 5.4 问题管理接口
- GET /api/issues/my - 获取个人问题列表
- GET /api/issues/team - 获取团队问题列表
- POST /api/issues/{id}/fix - 提交整改记录
- PUT /api/issues/{id}/verify - 验证整改结果

### 5.5 智能汇总接口
- POST /api/summaries/generate - 生成AI汇总
- GET /api/summaries - 获取汇总列表
- PUT /api/summaries/{id} - 编辑汇总内容

### 5.6 统计分析接口
- GET /api/statistics/personal - 个人统计数据
- GET /api/statistics/team - 团队统计数据
- GET /api/statistics/global - 全局统计数据

## 6. 用户界面设计

### 6.1 主要页面
1. **登录页面** - 用户认证
2. **仪表板** - 个人工作台，显示待办事项和统计概览
3. **评审任务页面** - 显示分配的评审任务
4. **评审记录页面** - 创建和编辑评审记录
5. **问题列表页面** - 显示个人或团队问题
6. **统计分析页面** - 各类统计图表
7. **智能汇总页面** - AI生成的汇总报告
8. **设置页面** - 个人设置和系统配置

### 6.2 界面特性
- 响应式设计，支持PC和移动端
- 直观的数据可视化图表
- 便捷的文件上传和预览
- 实时通知和提醒
- 支持暗色主题

## 7. 非功能需求

### 7.1 性能要求
- 页面加载时间 < 3秒
- 支持1000+并发用户
- 数据库查询响应时间 < 500ms
- 文件上传支持最大10MB

### 7.2 安全要求
- 用户认证和授权
- 数据传输加密（HTTPS）
- SQL注入防护
- XSS攻击防护
- 敏感数据脱敏

### 7.3 可用性要求
- 系统可用性 > 99.5%
- 支持数据备份和恢复
- 支持水平扩展
- 完善的日志和监控

## 8. 实施计划

### 8.1 开发阶段
**第一阶段（4周）**：基础框架搭建
- 用户管理和认证
- 基础数据模型
- 前端框架搭建

**第二阶段（6周）**：核心功能开发
- 评审分配算法
- 评审记录管理
- 问题管理功能

**第三阶段（4周）**：高级功能开发
- 智能汇总功能
- 统计分析模块
- 通知提醒系统

**第四阶段（3周）**：测试和优化
- 功能测试
- 性能优化
- 安全测试

**第五阶段（2周）**：部署和上线
- 生产环境部署
- 用户培训
- 监控和维护

### 8.2 团队配置
- 项目经理：1人
- 后端开发：2人
- 前端开发：2人
- UI/UX设计：1人
- 测试工程师：1人
- 运维工程师：1人

## 9. 风险评估

### 9.1 技术风险
- AI服务稳定性和准确性
- 大数据量下的性能问题
- 第三方服务集成风险

### 9.2 业务风险
- 用户接受度和使用习惯
- 评审质量标准制定
- 跨团队协作配合

### 9.3 风险应对
- 建立AI服务降级机制
- 进行充分的性能测试
- 制定详细的用户培训计划
- 建立反馈收集和快速迭代机制

## 10. 成功指标

### 10.1 业务指标
- 代码评审覆盖率 > 90%
- 问题发现和修复及时率 > 85%
- 用户活跃度 > 80%
- 代码质量提升可量化

### 10.2 技术指标
- 系统响应时间 < 500ms
- 系统可用性 > 99.5%
- 错误率 < 0.1%
- 用户满意度 > 4.0/5.0



