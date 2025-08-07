-- 创建用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role ENUM('DEVELOPER', 'TEAM_LEADER', 'ARCHITECT') NOT NULL DEFAULT 'DEVELOPER' COMMENT '角色',
    team_id BIGINT COMMENT '团队ID',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_team_id (team_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建团队表
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '团队名称',
    description TEXT COMMENT '团队描述',
    leader_id BIGINT NOT NULL COMMENT '团队负责人ID',
    active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_name (name),
    INDEX idx_leader_id (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

-- 创建评审分配表
CREATE TABLE review_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    week_start_date DATE NOT NULL COMMENT '周开始日期',
    reviewer_id BIGINT NOT NULL COMMENT '评审者ID',
    reviewee_id BIGINT NOT NULL COMMENT '被评审者ID',
    team_id BIGINT NOT NULL COMMENT '团队ID',
    status ENUM('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ASSIGNED' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_week_reviewer (week_start_date, reviewer_id),
    INDEX idx_week_reviewee (week_start_date, reviewee_id),
    INDEX idx_team_week (team_id, week_start_date),
    INDEX idx_status (status),
    UNIQUE KEY uk_week_reviewer_reviewee (week_start_date, reviewer_id, reviewee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审分配表';

-- 创建评审记录表
CREATE TABLE review_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL COMMENT '分配ID',
    title VARCHAR(200) NOT NULL COMMENT '评审标题',
    code_repository VARCHAR(500) COMMENT '代码仓库地址',
    code_file_path VARCHAR(1000) COMMENT '代码文件路径',
    overall_score INT COMMENT '总体评分(1-10)',
    summary TEXT COMMENT '评审总结',
    status ENUM('DRAFT', 'SUBMITTED', 'COMPLETED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_assignment_id (assignment_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审记录表';

-- 创建代码截图表
CREATE TABLE code_screenshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_record_id BIGINT NOT NULL COMMENT '评审记录ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_url VARCHAR(1000) NOT NULL COMMENT '文件URL',
    file_size BIGINT COMMENT '文件大小(字节)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_review_record_id (review_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码截图表';

-- 创建问题表
CREATE TABLE issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_record_id BIGINT NOT NULL COMMENT '评审记录ID',
    issue_type ENUM('FUNCTIONAL_DEFECT', 'PERFORMANCE_ISSUE', 'SECURITY_VULNERABILITY', 'CODE_STANDARD', 'DESIGN_ISSUE') NOT NULL COMMENT '问题类型',
    severity ENUM('CRITICAL', 'MAJOR', 'MINOR', 'SUGGESTION') NOT NULL COMMENT '严重级别',
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    description TEXT NOT NULL COMMENT '问题描述',
    suggestion TEXT COMMENT '改进建议',
    reference_links JSON COMMENT '参考链接',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_review_record_id (review_record_id),
    INDEX idx_type_severity (issue_type, severity),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- 创建整改记录表
CREATE TABLE fix_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    issue_id BIGINT NOT NULL COMMENT '问题ID',
    fixer_id BIGINT NOT NULL COMMENT '整改人ID',
    fix_description TEXT NOT NULL COMMENT '整改描述',
    before_code_url VARCHAR(1000) COMMENT '整改前代码链接',
    after_code_url VARCHAR(1000) COMMENT '整改后代码链接',
    status ENUM('SUBMITTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态',
    reviewer_comment TEXT COMMENT '评审者评论',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_issue_id (issue_id),
    INDEX idx_fixer_id (fixer_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='整改记录表';

-- 创建智能汇总表
CREATE TABLE ai_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL COMMENT '团队ID',
    week_start_date DATE NOT NULL COMMENT '周开始日期',
    summary_type ENUM('TEAM_WEEKLY', 'ARCHITECT_WEEKLY') NOT NULL COMMENT '汇总类型',
    ai_analysis TEXT COMMENT 'AI分析结果',
    manual_content TEXT COMMENT '手动编辑内容',
    status ENUM('DRAFT', 'PUBLISHED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_team_week (team_id, week_start_date),
    INDEX idx_type_week (summary_type, week_start_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能汇总表';

-- 创建通知表
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT NOT NULL COMMENT '通知内容',
    type ENUM('ASSIGNMENT', 'REVIEW_SUBMITTED', 'ISSUE_ASSIGNED', 'FIX_SUBMITTED', 'SYSTEM') NOT NULL COMMENT '通知类型',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    related_id BIGINT COMMENT '关联ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人',
    updated_by BIGINT COMMENT '更新人',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 添加外键约束
ALTER TABLE users ADD CONSTRAINT fk_users_team_id FOREIGN KEY (team_id) REFERENCES teams(id);
ALTER TABLE teams ADD CONSTRAINT fk_teams_leader_id FOREIGN KEY (leader_id) REFERENCES users(id);
ALTER TABLE review_assignments ADD CONSTRAINT fk_assignments_reviewer_id FOREIGN KEY (reviewer_id) REFERENCES users(id);
ALTER TABLE review_assignments ADD CONSTRAINT fk_assignments_reviewee_id FOREIGN KEY (reviewee_id) REFERENCES users(id);
ALTER TABLE review_assignments ADD CONSTRAINT fk_assignments_team_id FOREIGN KEY (team_id) REFERENCES teams(id);
ALTER TABLE review_records ADD CONSTRAINT fk_records_assignment_id FOREIGN KEY (assignment_id) REFERENCES review_assignments(id);
ALTER TABLE code_screenshots ADD CONSTRAINT fk_screenshots_review_record_id FOREIGN KEY (review_record_id) REFERENCES review_records(id);
ALTER TABLE issues ADD CONSTRAINT fk_issues_review_record_id FOREIGN KEY (review_record_id) REFERENCES review_records(id);
ALTER TABLE fix_records ADD CONSTRAINT fk_fix_records_issue_id FOREIGN KEY (issue_id) REFERENCES issues(id);
ALTER TABLE fix_records ADD CONSTRAINT fk_fix_records_fixer_id FOREIGN KEY (fixer_id) REFERENCES users(id);
ALTER TABLE ai_summaries ADD CONSTRAINT fk_summaries_team_id FOREIGN KEY (team_id) REFERENCES teams(id);
ALTER TABLE notifications ADD CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users(id);