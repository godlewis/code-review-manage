-- 创建分配配置表
CREATE TABLE assignment_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值（JSON格式）',
    description VARCHAR(500) COMMENT '配置描述',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型：GLOBAL-全局配置，TEAM-团队配置，USER-用户配置',
    related_id BIGINT COMMENT '关联ID（团队ID或用户ID）',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    updated_by BIGINT NOT NULL COMMENT '更新者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）'
) COMMENT='分配配置表';

-- 创建索引
CREATE INDEX idx_assignment_configs_key ON assignment_configs(config_key);
CREATE INDEX idx_assignment_configs_type ON assignment_configs(config_type);
CREATE INDEX idx_assignment_configs_type_related ON assignment_configs(config_type, related_id);
CREATE INDEX idx_assignment_configs_enabled ON assignment_configs(enabled);
CREATE UNIQUE INDEX uk_assignment_configs_key_type_related ON assignment_configs(config_key, config_type, related_id);

-- 插入默认配置数据
INSERT INTO assignment_configs (config_key, config_value, description, config_type, related_id, enabled, created_by, updated_by) VALUES
('global_basic_config', '{"avoidanceWeeks":4,"maxAssignmentsPerWeek":3,"skillMatchWeight":0.4,"loadBalanceWeight":0.3,"diversityWeight":0.3,"enableAutoAssignment":true,"enableNewUserPriority":true,"newUserThresholdMonths":3,"experiencedUserThresholdMonths":6}', '全局基础配置', 'GLOBAL', NULL, TRUE, 1, 1),
('exclude_pairs', '[]', '排除配对规则', 'GLOBAL', NULL, TRUE, 1, 1),
('force_pairs', '[]', '强制配对规则', 'GLOBAL', NULL, TRUE, 1, 1);