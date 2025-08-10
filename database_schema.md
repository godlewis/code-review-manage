# 代码评审管理系统数据库表结构说明

## 数据库信息
- 数据库名：`code_review`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 存储引擎：`InnoDB`

## 表结构概览

### 1. teams (团队表)
存储团队基本信息

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 团队ID |
| name | VARCHAR(100) | NOT NULL | 团队名称 |
| description | TEXT | NULL | 团队描述 |
| leader_id | BIGINT | FOREIGN KEY | 团队负责人ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**索引：**
- `idx_leader_id (leader_id)`
- `idx_name (name)`

**外键约束：**
- `leader_id` -> `users(id)` ON DELETE SET NULL

### 2. users (用户表)
存储用户基本信息

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| password | VARCHAR(255) | NOT NULL | 密码 |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| role | ENUM | NOT NULL | 用户角色 |
| team_id | BIGINT | FOREIGN KEY | 所属团队ID |
| skills | JSON | NULL | 技能标签 |
| status | ENUM | DEFAULT 'ACTIVE' | 用户状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**角色枚举值：**
- `DEVELOPER` - 开发人员
- `TEAM_LEADER` - 团队负责人
- `ARCHITECT` - 架构师

**状态枚举值：**
- `ACTIVE` - 活跃
- `INACTIVE` - 非活跃

**索引：**
- `idx_username (username)`
- `idx_email (email)`
- `idx_team_id (team_id)`
- `idx_role (role)`
- `idx_status (status)`

**外键约束：**
- `team_id` -> `teams(id)` ON DELETE SET NULL

### 3. review_assignments (评审分配表)
存储每周的评审任务分配

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 分配ID |
| week_start_date | DATE | NOT NULL | 周开始日期 |
| reviewer_id | BIGINT | NOT NULL, FOREIGN KEY | 评审者ID |
| reviewee_id | BIGINT | NOT NULL, FOREIGN KEY | 被评审者ID |
| status | ENUM | DEFAULT 'PENDING' | 分配状态 |
| assigned_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 分配时间 |
| completed_at | TIMESTAMP | NULL | 完成时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**状态枚举值：**
- `PENDING` - 待处理
- `IN_PROGRESS` - 进行中
- `COMPLETED` - 已完成
- `OVERDUE` - 已逾期

**索引：**
- `idx_week_reviewer (week_start_date, reviewer_id)`
- `idx_week_reviewee (week_start_date, reviewee_id)`
- `idx_status (status)`
- `idx_assigned_at (assigned_at)`

**外键约束：**
- `reviewer_id` -> `users(id)` ON DELETE CASCADE
- `reviewee_id` -> `users(id)` ON DELETE CASCADE

### 4. review_records (评审记录表)
存储具体的代码评审记录

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 评审记录ID |
| assignment_id | BIGINT | NOT NULL, FOREIGN KEY | 评审分配ID |
| title | VARCHAR(200) | NOT NULL | 评审标题 |
| code_repository | VARCHAR(500) | NULL | 代码仓库链接 |
| code_file_path | VARCHAR(500) | NULL | 代码文件路径 |
| code_line_start | INT | NULL | 代码起始行号 |
| code_line_end | INT | NULL | 代码结束行号 |
| code_version | VARCHAR(100) | NULL | 代码版本 |
| overall_score | TINYINT | CHECK (1-10) | 总体评分 |
| summary | TEXT | NULL | 评审总结 |
| status | ENUM | DEFAULT 'DRAFT' | 评审状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**状态枚举值：**
- `DRAFT` - 草稿
- `SUBMITTED` - 已提交
- `REVIEWED` - 已评审

**索引：**
- `idx_assignment_id (assignment_id)`
- `idx_status (status)`
- `idx_created_at (created_at)`
- `idx_overall_score (overall_score)`

**外键约束：**
- `assignment_id` -> `review_assignments(id)` ON DELETE CASCADE

### 5. code_screenshots (代码截图表)
存储评审过程中的代码截图

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 截图ID |
| review_record_id | BIGINT | NOT NULL, FOREIGN KEY | 评审记录ID |
| file_url | VARCHAR(500) | NOT NULL | 文件URL |
| file_name | VARCHAR(200) | NULL | 文件名 |
| file_size | BIGINT | NULL | 文件大小(字节) |
| upload_time | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 上传时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**索引：**
- `idx_review_record_id (review_record_id)`
- `idx_upload_time (upload_time)`

**外键约束：**
- `review_record_id` -> `review_records(id)` ON DELETE CASCADE

### 6. issues (问题记录表)
存储评审过程中发现的问题

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 问题ID |
| review_record_id | BIGINT | NOT NULL, FOREIGN KEY | 评审记录ID |
| issue_type | ENUM | NOT NULL | 问题类型 |
| severity | ENUM | NOT NULL | 严重级别 |
| title | VARCHAR(200) | NOT NULL | 问题标题 |
| description | TEXT | NOT NULL | 问题描述 |
| suggestion | TEXT | NULL | 改进建议 |
| reference_links | JSON | NULL | 参考资料链接 |
| status | ENUM | DEFAULT 'OPEN' | 问题状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**问题类型枚举值：**
- `FUNCTIONAL_DEFECT` - 功能缺陷
- `PERFORMANCE_ISSUE` - 性能问题
- `SECURITY_VULNERABILITY` - 安全漏洞
- `CODE_STANDARD` - 代码规范
- `DESIGN_ISSUE` - 设计问题

**严重级别枚举值：**
- `CRITICAL` - 严重
- `MAJOR` - 一般
- `MINOR` - 轻微
- `SUGGESTION` - 建议

**状态枚举值：**
- `OPEN` - 开放
- `IN_PROGRESS` - 进行中
- `RESOLVED` - 已解决
- `CLOSED` - 已关闭

**索引：**
- `idx_review_record_id (review_record_id)`
- `idx_issue_type_severity (issue_type, severity)`
- `idx_status (status)`
- `idx_created_at (created_at)`

**外键约束：**
- `review_record_id` -> `review_records(id)` ON DELETE CASCADE

### 7. fix_records (整改记录表)
存储问题整改的详细记录

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 整改记录ID |
| issue_id | BIGINT | NOT NULL, FOREIGN KEY | 问题ID |
| fixer_id | BIGINT | NOT NULL, FOREIGN KEY | 整改人ID |
| fix_description | TEXT | NOT NULL | 整改描述 |
| before_code_url | VARCHAR(500) | NULL | 整改前代码URL |
| after_code_url | VARCHAR(500) | NULL | 整改后代码URL |
| fix_commit_hash | VARCHAR(100) | NULL | 整改提交哈希 |
| status | ENUM | DEFAULT 'SUBMITTED' | 整改状态 |
| verified_by | BIGINT | NULL, FOREIGN KEY | 验证人ID |
| verified_at | TIMESTAMP | NULL | 验证时间 |
| reject_reason | TEXT | NULL | 拒绝原因 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**状态枚举值：**
- `SUBMITTED` - 已提交
- `VERIFIED` - 已验证
- `REJECTED` - 已拒绝

**索引：**
- `idx_issue_id (issue_id)`
- `idx_fixer_id (fixer_id)`
- `idx_status (status)`
- `idx_verified_at (verified_at)`

**外键约束：**
- `issue_id` -> `issues(id)` ON DELETE CASCADE
- `fixer_id` -> `users(id)` ON DELETE CASCADE
- `verified_by` -> `users(id)` ON DELETE SET NULL

### 8. ai_summaries (智能汇总表)
存储AI生成的汇总报告

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 汇总ID |
| summary_type | ENUM | NOT NULL | 汇总类型 |
| team_id | BIGINT | NULL, FOREIGN KEY | 团队ID |
| week_start_date | DATE | NOT NULL | 周开始日期 |
| ai_content | TEXT | NOT NULL | AI生成内容 |
| human_edited_content | TEXT | NULL | 人工编辑内容 |
| created_by | BIGINT | NOT NULL, FOREIGN KEY | 创建人ID |
| status | ENUM | DEFAULT 'DRAFT' | 汇总状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**汇总类型枚举值：**
- `TEAM_WEEKLY` - 团队周度汇总
- `ARCHITECT_WEEKLY` - 架构师周度汇总

**状态枚举值：**
- `DRAFT` - 草稿
- `PUBLISHED` - 已发布

**索引：**
- `idx_summary_type_team_week (summary_type, team_id, week_start_date)`
- `idx_status (status)`
- `idx_created_at (created_at)`

**外键约束：**
- `team_id` -> `teams(id)` ON DELETE CASCADE
- `created_by` -> `users(id)` ON DELETE CASCADE

### 9. notifications (提醒通知表)
存储系统通知和提醒

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 通知ID |
| user_id | BIGINT | NOT NULL, FOREIGN KEY | 用户ID |
| type | ENUM | NOT NULL | 通知类型 |
| title | VARCHAR(200) | NOT NULL | 通知标题 |
| content | TEXT | NOT NULL | 通知内容 |
| related_id | BIGINT | NULL | 关联ID |
| is_read | BOOLEAN | DEFAULT FALSE | 是否已读 |
| read_at | TIMESTAMP | NULL | 阅读时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| created_by | BIGINT | NULL | 创建人ID |
| updated_by | BIGINT | NULL | 更新人ID |
| is_deleted | BOOLEAN | DEFAULT FALSE | 是否删除 |

**通知类型枚举值：**
- `ASSIGNMENT` - 任务分配
- `DEADLINE` - 截止提醒
- `FIX_REQUIRED` - 需要整改
- `REVIEW_REQUIRED` - 需要评审

**索引：**
- `idx_user_unread (user_id, is_read)`
- `idx_type (type)`
- `idx_created_at (created_at)`
- `idx_related_id (related_id)`

**外键约束：**
- `user_id` -> `users(id)` ON DELETE CASCADE

## 表关系图

```
teams (1) ←→ (N) users
users (1) ←→ (N) review_assignments (reviewer)
users (1) ←→ (N) review_assignments (reviewee)
review_assignments (1) ←→ (N) review_records
review_records (1) ←→ (N) code_screenshots
review_records (1) ←→ (N) issues
issues (1) ←→ (N) fix_records
users (1) ←→ (N) fix_records (fixer)
users (1) ←→ (N) fix_records (verifier)
teams (1) ←→ (N) ai_summaries
users (1) ←→ (N) ai_summaries
users (1) ←→ (N) notifications
```

## 索引策略

### 主要查询索引
1. **评审分配查询**：`idx_week_reviewer`, `idx_week_reviewee`
2. **问题管理查询**：`idx_issue_type_severity`, `idx_status`
3. **通知查询**：`idx_user_unread`
4. **统计分析查询**：`idx_created_at`, `idx_status`

### 性能优化建议
1. 定期分析慢查询日志，优化索引
2. 对于大数据量表，考虑分区策略
3. 定期清理软删除的数据
4. 监控索引使用情况，删除无用索引

## 数据维护

### 备份策略
- 每日全量备份
- 每小时增量备份
- 保留30天的备份文件

### 清理策略
- 软删除数据保留6个月后物理删除
- 通知数据保留3个月
- 临时文件定期清理

### 监控指标
- 表大小增长趋势
- 索引使用率
- 慢查询数量
- 连接数使用情况 