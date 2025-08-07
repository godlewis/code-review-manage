-- 插入初始数据

-- 插入默认团队
INSERT INTO teams (id, name, description, leader_id, active, created_at, updated_at, created_by, updated_by, is_deleted) 
VALUES (1, '默认团队', '系统默认团队', 1, TRUE, NOW(), NOW(), 1, 1, FALSE);

-- 插入系统管理员用户
INSERT INTO users (id, username, password, email, real_name, phone, role, team_id, active, created_at, updated_at, created_by, updated_by, is_deleted) 
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@company.com', '系统管理员', '13800138000', 'ARCHITECT', 1, TRUE, NOW(), NOW(), 1, 1, FALSE);

-- 插入测试用户
INSERT INTO users (username, password, email, real_name, phone, role, team_id, active, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'zhangsan@company.com', '张三', '13800138001', 'TEAM_LEADER', 1, TRUE, NOW(), NOW(), 1, 1, FALSE),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'lisi@company.com', '李四', '13800138002', 'DEVELOPER', 1, TRUE, NOW(), NOW(), 1, 1, FALSE),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'wangwu@company.com', '王五', '13800138003', 'DEVELOPER', 1, TRUE, NOW(), NOW(), 1, 1, FALSE),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'zhaoliu@company.com', '赵六', '13800138004', 'DEVELOPER', 1, TRUE, NOW(), NOW(), 1, 1, FALSE);

-- 更新团队负责人
UPDATE teams SET leader_id = 2 WHERE id = 1;

-- 插入示例评审分配
INSERT INTO review_assignments (week_start_date, reviewer_id, reviewee_id, team_id, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
('2023-12-04', 2, 3, 1, 'ASSIGNED', NOW(), NOW(), 1, 1, FALSE),
('2023-12-04', 3, 4, 1, 'ASSIGNED', NOW(), NOW(), 1, 1, FALSE),
('2023-12-04', 4, 5, 1, 'ASSIGNED', NOW(), NOW(), 1, 1, FALSE),
('2023-12-04', 5, 2, 1, 'ASSIGNED', NOW(), NOW(), 1, 1, FALSE);

-- 插入示例评审记录
INSERT INTO review_records (assignment_id, title, code_repository, code_file_path, overall_score, summary, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
(1, '用户登录模块代码评审', 'https://github.com/company/project.git', 'src/main/java/com/company/user/LoginController.java', 8, '整体代码质量良好，有几个小问题需要修复', 'SUBMITTED', NOW(), NOW(), 2, 2, FALSE),
(2, '订单处理逻辑优化评审', 'https://github.com/company/project.git', 'src/main/java/com/company/order/OrderService.java', 7, '逻辑清晰，但性能方面需要优化', 'DRAFT', NOW(), NOW(), 3, 3, FALSE);

-- 插入示例问题
INSERT INTO issues (review_record_id, issue_type, severity, title, description, suggestion, status, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
(1, 'SECURITY_VULNERABILITY', 'MAJOR', 'SQL注入风险', '在用户登录验证中直接拼接SQL语句，存在SQL注入风险', '建议使用参数化查询或ORM框架', 'OPEN', NOW(), NOW(), 2, 2, FALSE),
(1, 'CODE_STANDARD', 'MINOR', '变量命名不规范', '部分变量使用拼音命名，不符合代码规范', '建议使用英文命名，遵循驼峰命名法', 'OPEN', NOW(), NOW(), 2, 2, FALSE),
(2, 'PERFORMANCE_ISSUE', 'MAJOR', '数据库查询效率低', '在循环中执行数据库查询，导致N+1问题', '建议使用批量查询或缓存机制', 'OPEN', NOW(), NOW(), 3, 3, FALSE);

-- 插入示例通知
INSERT INTO notifications (user_id, title, content, type, is_read, related_id, created_at, updated_at, created_by, updated_by, is_deleted) VALUES
(3, '新的评审任务分配', '您被分配了一个新的代码评审任务：用户登录模块代码评审', 'ASSIGNMENT', FALSE, 1, NOW(), NOW(), 1, 1, FALSE),
(4, '评审已提交', '张三已提交对您代码的评审，请查看评审结果', 'REVIEW_SUBMITTED', FALSE, 1, NOW(), NOW(), 1, 1, FALSE),
(3, '问题需要整改', '您的代码中发现了安全漏洞问题，请及时整改', 'ISSUE_ASSIGNED', FALSE, 1, NOW(), NOW(), 1, 1, FALSE);