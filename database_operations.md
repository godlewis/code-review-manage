# 代码评审管理系统数据库操作手册

## 概述

本文档提供了代码评审管理系统数据库的日常操作、维护和故障处理指南，包括常用SQL查询、备份恢复、性能优化等内容。

## 1. 数据库连接信息

### 连接参数
- **数据库名**: code_review
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB
- **端口**: 3306 (默认)

### 连接示例
```bash
# 命令行连接
mysql -h localhost -u root -p code_review

# 使用配置文件连接
mysql --defaults-file=/path/to/my.cnf code_review
```

## 2. 常用SQL查询

### 2.1 用户管理查询

#### 查询所有活跃用户
```sql
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.email,
    u.role,
    t.name as team_name,
    u.status,
    u.created_at
FROM users u
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.is_deleted = FALSE
ORDER BY u.created_at DESC;
```

#### 查询团队成员
```sql
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.role,
    u.skills,
    u.status
FROM users u
WHERE u.team_id = ? AND u.is_deleted = FALSE
ORDER BY u.role, u.real_name;
```

#### 查询用户评审统计
```sql
SELECT 
    u.real_name,
    COUNT(ra.id) as total_assignments,
    SUM(CASE WHEN ra.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_reviews,
    SUM(CASE WHEN ra.status = 'OVERDUE' THEN 1 ELSE 0 END) as overdue_reviews
FROM users u
LEFT JOIN review_assignments ra ON u.id = ra.reviewer_id
WHERE u.is_deleted = FALSE
GROUP BY u.id, u.real_name
ORDER BY total_assignments DESC;
```

### 2.2 评审管理查询

#### 查询当前周评审分配
```sql
SELECT 
    ra.id,
    u1.real_name as reviewer,
    u2.real_name as reviewee,
    ra.status,
    ra.assigned_at,
    ra.completed_at
FROM review_assignments ra
JOIN users u1 ON ra.reviewer_id = u1.id
JOIN users u2 ON ra.reviewee_id = u2.id
WHERE ra.week_start_date = ? AND ra.is_deleted = FALSE
ORDER BY ra.status, ra.assigned_at;
```

#### 查询评审记录详情
```sql
SELECT 
    rr.id,
    rr.title,
    rr.code_repository,
    rr.overall_score,
    rr.status,
    rr.created_at,
    u1.real_name as reviewer,
    u2.real_name as reviewee,
    COUNT(i.id) as issue_count
FROM review_records rr
JOIN review_assignments ra ON rr.assignment_id = ra.id
JOIN users u1 ON ra.reviewer_id = u1.id
JOIN users u2 ON ra.reviewee_id = u2.id
LEFT JOIN issues i ON rr.id = i.review_record_id AND i.is_deleted = FALSE
WHERE rr.is_deleted = FALSE
GROUP BY rr.id
ORDER BY rr.created_at DESC;
```

#### 查询问题统计
```sql
SELECT 
    i.issue_type,
    i.severity,
    COUNT(*) as count
FROM issues i
WHERE i.is_deleted = FALSE
GROUP BY i.issue_type, i.severity
ORDER BY i.issue_type, i.severity;
```

### 2.3 统计分析查询

#### 团队评审覆盖率
```sql
SELECT 
    t.name as team_name,
    COUNT(DISTINCT u.id) as total_members,
    COUNT(DISTINCT ra.reviewer_id) as active_reviewers,
    ROUND(COUNT(DISTINCT ra.reviewer_id) * 100.0 / COUNT(DISTINCT u.id), 2) as coverage_rate
FROM teams t
LEFT JOIN users u ON t.id = u.team_id AND u.is_deleted = FALSE
LEFT JOIN review_assignments ra ON u.id = ra.reviewer_id 
    AND ra.week_start_date >= DATE_SUB(CURDATE(), INTERVAL 4 WEEK)
    AND ra.is_deleted = FALSE
WHERE t.is_deleted = FALSE
GROUP BY t.id, t.name;
```

#### 问题趋势分析
```sql
SELECT 
    DATE_FORMAT(i.created_at, '%Y-%m') as month,
    i.issue_type,
    COUNT(*) as count
FROM issues i
WHERE i.created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
    AND i.is_deleted = FALSE
GROUP BY DATE_FORMAT(i.created_at, '%Y-%m'), i.issue_type
ORDER BY month DESC, i.issue_type;
```

#### 整改及时率统计
```sql
SELECT 
    u.real_name,
    COUNT(i.id) as total_issues,
    COUNT(fr.id) as fixed_issues,
    ROUND(COUNT(fr.id) * 100.0 / COUNT(i.id), 2) as fix_rate
FROM users u
LEFT JOIN review_assignments ra ON u.id = ra.reviewee_id
LEFT JOIN review_records rr ON ra.id = rr.assignment_id
LEFT JOIN issues i ON rr.id = i.review_record_id AND i.is_deleted = FALSE
LEFT JOIN fix_records fr ON i.id = fr.issue_id AND fr.status = 'VERIFIED' AND fr.is_deleted = FALSE
WHERE u.is_deleted = FALSE
GROUP BY u.id, u.real_name
HAVING total_issues > 0
ORDER BY fix_rate DESC;
```

## 3. 数据维护操作

### 3.1 数据备份

#### 全量备份
```bash
# 备份整个数据库
mysqldump -h localhost -u root -p --single-transaction --routines --triggers \
    --databases code_review > backup_$(date +%Y%m%d_%H%M%S).sql

# 备份特定表
mysqldump -h localhost -u root -p --single-transaction \
    code_review users teams review_assignments > users_backup_$(date +%Y%m%d).sql
```

#### 增量备份
```bash
# 使用二进制日志进行增量备份
mysqlbinlog --start-datetime="2024-01-01 00:00:00" \
    --stop-datetime="2024-01-02 00:00:00" \
    /var/lib/mysql/mysql-bin.* > incremental_backup_20240101.sql
```

### 3.2 数据恢复

#### 全量恢复
```bash
# 恢复整个数据库
mysql -h localhost -u root -p < backup_20240101_120000.sql

# 恢复特定表
mysql -h localhost -u root -p code_review < users_backup_20240101.sql
```

#### 增量恢复
```bash
# 应用增量备份
mysql -h localhost -u root -p code_review < incremental_backup_20240101.sql
```

### 3.3 数据清理

#### 清理软删除数据
```sql
-- 清理6个月前的软删除数据
DELETE FROM users WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM teams WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM review_assignments WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM review_records WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM issues WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM fix_records WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM notifications WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 3 MONTH);
DELETE FROM ai_summaries WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```

#### 清理过期通知
```sql
-- 清理3个月前的已读通知
DELETE FROM notifications 
WHERE is_read = TRUE AND read_at < DATE_SUB(NOW(), INTERVAL 3 MONTH);
```

### 3.4 数据修复

#### 修复外键约束
```sql
-- 检查并修复孤立数据
DELETE FROM review_assignments 
WHERE reviewer_id NOT IN (SELECT id FROM users WHERE is_deleted = FALSE)
   OR reviewee_id NOT IN (SELECT id FROM users WHERE is_deleted = FALSE);

DELETE FROM review_records 
WHERE assignment_id NOT IN (SELECT id FROM review_assignments WHERE is_deleted = FALSE);

DELETE FROM issues 
WHERE review_record_id NOT IN (SELECT id FROM review_records WHERE is_deleted = FALSE);
```

#### 修复数据一致性
```sql
-- 更新用户状态
UPDATE users SET status = 'INACTIVE' 
WHERE last_login_at < DATE_SUB(NOW(), INTERVAL 90 DAY) AND status = 'ACTIVE';

-- 更新评审分配状态
UPDATE review_assignments SET status = 'OVERDUE' 
WHERE status = 'PENDING' AND assigned_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

## 4. 性能优化

### 4.1 索引优化

#### 分析索引使用情况
```sql
-- 查看索引使用统计
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    SUB_PART,
    PACKED,
    NULLABLE,
    INDEX_TYPE
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'code_review'
ORDER BY TABLE_NAME, INDEX_NAME;
```

#### 优化慢查询
```sql
-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 分析慢查询
SELECT 
    sql_text,
    exec_count,
    avg_timer_wait/1000000000 as avg_time_sec,
    sum_timer_wait/1000000000 as total_time_sec
FROM performance_schema.events_statements_summary_by_digest
WHERE SCHEMA_NAME = 'code_review'
ORDER BY avg_timer_wait DESC
LIMIT 10;
```

### 4.2 查询优化

#### 优化大表查询
```sql
-- 使用分页查询
SELECT * FROM review_records 
WHERE is_deleted = FALSE 
ORDER BY created_at DESC 
LIMIT 20 OFFSET 0;

-- 使用索引提示
SELECT * FROM users USE INDEX (idx_team_id)
WHERE team_id = ? AND is_deleted = FALSE;
```

#### 优化JOIN查询
```sql
-- 使用EXISTS替代JOIN
SELECT u.* FROM users u
WHERE EXISTS (
    SELECT 1 FROM review_assignments ra 
    WHERE ra.reviewer_id = u.id 
    AND ra.week_start_date = ?
    AND ra.is_deleted = FALSE
);
```

## 5. 监控和维护

### 5.1 数据库监控

#### 查看数据库状态
```sql
-- 查看数据库大小
SELECT 
    TABLE_SCHEMA,
    ROUND(SUM(DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS 'DB Size in MB'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'code_review'
GROUP BY TABLE_SCHEMA;

-- 查看表大小
SELECT 
    TABLE_NAME,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size in MB',
    TABLE_ROWS
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'code_review'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;
```

#### 查看连接状态
```sql
-- 查看当前连接
SELECT 
    ID,
    USER,
    HOST,
    DB,
    COMMAND,
    TIME,
    STATE,
    INFO
FROM information_schema.PROCESSLIST
WHERE DB = 'code_review';
```

### 5.2 定期维护任务

#### 每日维护
```sql
-- 更新统计信息
ANALYZE TABLE users, teams, review_assignments, review_records, issues;

-- 检查表完整性
CHECK TABLE users, teams, review_assignments, review_records, issues;
```

#### 每周维护
```sql
-- 优化表
OPTIMIZE TABLE users, teams, review_assignments, review_records, issues;

-- 更新索引统计
ANALYZE TABLE users, teams, review_assignments, review_records, issues;
```

#### 每月维护
```sql
-- 清理日志表
DELETE FROM notifications 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 3 MONTH) AND is_read = TRUE;

-- 备份重要数据
-- 执行备份脚本
```

## 6. 故障处理

### 6.1 常见问题

#### 连接数过多
```sql
-- 查看最大连接数
SHOW VARIABLES LIKE 'max_connections';

-- 增加最大连接数
SET GLOBAL max_connections = 1000;
```

#### 内存不足
```sql
-- 查看内存使用
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';

-- 调整缓冲池大小
SET GLOBAL innodb_buffer_pool_size = 1073741824; -- 1GB
```

#### 锁等待
```sql
-- 查看锁等待
SELECT 
    r.trx_id waiting_trx_id,
    r.trx_mysql_thread_id waiting_thread,
    r.trx_query waiting_query,
    b.trx_id blocking_trx_id,
    b.trx_mysql_thread_id blocking_thread,
    b.trx_query blocking_query
FROM information_schema.innodb_lock_waits w
INNER JOIN information_schema.innodb_trx b ON b.trx_id = w.blocking_trx_id
INNER JOIN information_schema.innodb_trx r ON r.trx_id = w.requesting_trx_id;
```

### 6.2 紧急恢复

#### 数据库损坏恢复
```bash
# 停止MySQL服务
sudo systemctl stop mysql

# 使用mysqlcheck修复
mysqlcheck -r --all-databases

# 重启MySQL服务
sudo systemctl start mysql
```

#### 数据丢失恢复
```bash
# 从备份恢复
mysql -u root -p < latest_backup.sql

# 应用增量备份
mysql -u root -p < incremental_backup.sql
```

## 7. 安全措施

### 7.1 访问控制
```sql
-- 创建只读用户
CREATE USER 'readonly'@'%' IDENTIFIED BY 'password';
GRANT SELECT ON code_review.* TO 'readonly'@'%';

-- 创建应用用户
CREATE USER 'app_user'@'%' IDENTIFIED BY 'password';
GRANT SELECT, INSERT, UPDATE, DELETE ON code_review.* TO 'app_user'@'%';
```

### 7.2 数据加密
```sql
-- 启用SSL连接
ALTER USER 'app_user'@'%' REQUIRE SSL;

-- 加密敏感字段
UPDATE users SET password = AES_ENCRYPT(password, 'encryption_key');
```

## 8. 自动化脚本

### 8.1 备份脚本
```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/mysql"
DB_NAME="code_review"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
mysqldump -h localhost -u root -p --single-transaction --routines --triggers \
    --databases $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/backup_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete

echo "Backup completed: backup_$DATE.sql.gz"
```

### 8.2 清理脚本
```bash
#!/bin/bash
# cleanup.sh

mysql -u root -p code_review << EOF
-- 清理软删除数据
DELETE FROM users WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);
DELETE FROM notifications WHERE is_deleted = TRUE AND updated_at < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 清理过期通知
DELETE FROM notifications WHERE is_read = TRUE AND read_at < DATE_SUB(NOW(), INTERVAL 3 MONTH);

-- 更新状态
UPDATE review_assignments SET status = 'OVERDUE' 
WHERE status = 'PENDING' AND assigned_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
EOF

echo "Cleanup completed"
```

## 9. 联系信息

### 技术支持
- **数据库管理员**: dba@company.com
- **系统管理员**: admin@company.com
- **紧急联系**: +86-xxx-xxxx-xxxx

### 文档更新
- **最后更新**: 2024年1月
- **版本**: 1.0
- **维护者**: 数据库团队 