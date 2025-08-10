-- 代码评审管理系统数据库表结构DDL脚本
-- 创建时间: 2024年
-- 数据库名: code_review
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci

-- 创建数据库
CREATE DATABASE IF NOT EXISTS code_review DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE code_review;

-- 1. 创建团队表
CREATE TABLE teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '团队名称',
    description TEXT COMMENT '团队描述',
    leader_id BIGINT COMMENT '团队负责人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_leader_id (leader_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- 2. 创建用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role ENUM('DEVELOPER', 'TEAM_LEADER', 'ARCHITECT') NOT NULL COMMENT '用户角色',
    team_id BIGINT COMMENT '所属团队ID',
    skills JSON COMMENT '技能标签',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '用户状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_team_id (team_id),
    INDEX idx_role (role),
    INDEX idx_status (status),
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 创建评审分配表
CREATE TABLE review_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_start_date DATE NOT NULL COMMENT '周开始日期',
    reviewer_id BIGINT NOT NULL COMMENT '评审者ID',
    reviewee_id BIGINT NOT NULL COMMENT '被评审者ID',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE') DEFAULT 'PENDING' COMMENT '分配状态',
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_week_reviewer (week_start_date, reviewer_id),
    INDEX idx_week_reviewee (week_start_date, reviewee_id),
    INDEX idx_status (status),
    INDEX idx_assigned_at (assigned_at),
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewee_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审分配表';

-- 4. 创建评审记录表
CREATE TABLE review_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL COMMENT '评审分配ID',
    title VARCHAR(200) NOT NULL COMMENT '评审标题',
    code_repository VARCHAR(500) COMMENT '代码仓库链接',
    code_file_path VARCHAR(500) COMMENT '代码文件路径',
    code_line_start INT COMMENT '代码起始行号',
    code_line_end INT COMMENT '代码结束行号',
    code_version VARCHAR(100) COMMENT '代码版本',
    overall_score TINYINT CHECK (overall_score BETWEEN 1 AND 10) COMMENT '总体评分',
    summary TEXT COMMENT '评审总结',
    status ENUM('DRAFT', 'SUBMITTED', 'REVIEWED') DEFAULT 'DRAFT' COMMENT '评审状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_assignment_id (assignment_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_overall_score (overall_score),
    FOREIGN KEY (assignment_id) REFERENCES review_assignments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审记录表';

-- 5. 创建代码截图表
CREATE TABLE code_screenshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_record_id BIGINT NOT NULL COMMENT '评审记录ID',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_name VARCHAR(200) COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小(字节)',
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_review_record_id (review_record_id),
    INDEX idx_upload_time (upload_time),
    FOREIGN KEY (review_record_id) REFERENCES review_records(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码截图表';

-- 6. 创建问题记录表
CREATE TABLE issues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_record_id BIGINT NOT NULL COMMENT '评审记录ID',
    issue_type ENUM('FUNCTIONAL_DEFECT', 'PERFORMANCE_ISSUE', 'SECURITY_VULNERABILITY', 'CODE_STANDARD', 'DESIGN_ISSUE') NOT NULL COMMENT '问题类型',
    severity ENUM('CRITICAL', 'MAJOR', 'MINOR', 'SUGGESTION') NOT NULL COMMENT '严重级别',
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    description TEXT NOT NULL COMMENT '问题描述',
    suggestion TEXT COMMENT '改进建议',
    reference_links JSON COMMENT '参考资料链接',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN' COMMENT '问题状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_review_record_id (review_record_id),
    INDEX idx_issue_type_severity (issue_type, severity),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (review_record_id) REFERENCES review_records(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题记录表';

-- 7. 创建整改记录表
CREATE TABLE fix_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    issue_id BIGINT NOT NULL COMMENT '问题ID',
    fixer_id BIGINT NOT NULL COMMENT '整改人ID',
    fix_description TEXT NOT NULL COMMENT '整改描述',
    before_code_url VARCHAR(500) COMMENT '整改前代码URL',
    after_code_url VARCHAR(500) COMMENT '整改后代码URL',
    fix_commit_hash VARCHAR(100) COMMENT '整改提交哈希',
    status ENUM('SUBMITTED', 'VERIFIED', 'REJECTED') DEFAULT 'SUBMITTED' COMMENT '整改状态',
    verified_by BIGINT COMMENT '验证人ID',
    verified_at TIMESTAMP NULL COMMENT '验证时间',
    reject_reason TEXT COMMENT '拒绝原因',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_issue_id (issue_id),
    INDEX idx_fixer_id (fixer_id),
    INDEX idx_status (status),
    INDEX idx_verified_at (verified_at),
    FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    FOREIGN KEY (fixer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='整改记录表';

-- 8. 创建智能汇总表
CREATE TABLE ai_summaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    summary_type ENUM('TEAM_WEEKLY', 'ARCHITECT_WEEKLY') NOT NULL COMMENT '汇总类型',
    team_id BIGINT COMMENT '团队ID',
    week_start_date DATE NOT NULL COMMENT '周开始日期',
    ai_content TEXT NOT NULL COMMENT 'AI生成内容',
    human_edited_content TEXT COMMENT '人工编辑内容',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    status ENUM('DRAFT', 'PUBLISHED') DEFAULT 'DRAFT' COMMENT '汇总状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_summary_type_team_week (summary_type, team_id, week_start_date),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能汇总表';

-- 9. 创建提醒通知表
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type ENUM('ASSIGNMENT', 'DEADLINE', 'FIX_REQUIRED', 'REVIEW_REQUIRED') NOT NULL COMMENT '通知类型',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    related_id BIGINT COMMENT '关联ID',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_at TIMESTAMP NULL COMMENT '阅读时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_related_id (related_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒通知表';

-- 添加团队表的外键约束（需要在用户表创建后）
ALTER TABLE teams ADD FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE SET NULL;

-- 验证表结构
SELECT 'Database schema created successfully!' AS status; 