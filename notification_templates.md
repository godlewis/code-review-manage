# 代码评审管理系统通知规则和模板管理

## 概述

本文档定义了代码评审管理系统的通知规则配置、模板管理和发送机制，确保用户能够及时收到相关的任务提醒和系统通知。

## 1. 通知类型定义

### 1.1 通知类型枚举

| 通知类型 | 代码 | 描述 | 触发条件 | 优先级 |
|----------|------|------|----------|--------|
| 任务分配 | ASSIGNMENT | 新的评审任务分配 | 系统自动分配评审任务 | 高 |
| 截止提醒 | DEADLINE | 评审截止时间提醒 | 距离截止时间24小时 | 中 |
| 需要整改 | FIX_REQUIRED | 问题需要整改提醒 | 评审者提交问题后 | 高 |
| 需要评审 | REVIEW_REQUIRED | 整改验证提醒 | 整改者提交整改记录 | 中 |
| 系统通知 | SYSTEM | 系统维护和更新通知 | 系统维护、版本更新 | 低 |
| 团队通知 | TEAM | 团队相关通知 | 团队负责人发布 | 中 |

### 1.2 通知优先级

- **高优先级**: 需要立即处理的任务，如任务分配、问题整改
- **中优先级**: 需要关注但不紧急的任务，如截止提醒、验证请求
- **低优先级**: 信息性通知，如系统维护、团队通知

## 2. 通知规则配置

### 2.1 通知触发规则

#### 任务分配通知规则
```json
{
  "type": "ASSIGNMENT",
  "trigger": "AUTO",
  "conditions": {
    "event": "review_assignment_created",
    "recipients": ["reviewer"],
    "delay": 0,
    "retry_count": 3,
    "retry_interval": 300
  },
  "channels": ["system", "email", "wechat"],
  "template": "assignment_notification"
}
```

#### 截止提醒规则
```json
{
  "type": "DEADLINE",
  "trigger": "SCHEDULED",
  "conditions": {
    "event": "review_deadline_approaching",
    "recipients": ["reviewer"],
    "advance_hours": 24,
    "retry_count": 2,
    "retry_interval": 3600
  },
  "channels": ["system", "email"],
  "template": "deadline_reminder"
}
```

#### 整改提醒规则
```json
{
  "type": "FIX_REQUIRED",
  "trigger": "AUTO",
  "conditions": {
    "event": "issue_created",
    "recipients": ["reviewee"],
    "delay": 0,
    "retry_count": 2,
    "retry_interval": 1800
  },
  "channels": ["system", "email", "wechat"],
  "template": "fix_required_notification"
}
```

### 2.2 通知发送时机

| 通知类型 | 发送时机 | 重复策略 | 静默期 |
|----------|----------|----------|--------|
| 任务分配 | 立即发送 | 不重复 | 无 |
| 截止提醒 | 提前24小时 | 每6小时重复 | 2小时 |
| 需要整改 | 立即发送 | 每30分钟重复 | 1小时 |
| 需要评审 | 立即发送 | 不重复 | 无 |
| 系统通知 | 定时发送 | 不重复 | 无 |
| 团队通知 | 立即发送 | 不重复 | 无 |

## 3. 通知模板管理

### 3.1 模板变量定义

#### 通用变量
- `{{user_name}}`: 用户姓名
- `{{user_email}}`: 用户邮箱
- `{{team_name}}`: 团队名称
- `{{current_time}}`: 当前时间
- `{{system_name}}`: 系统名称

#### 任务相关变量
- `{{reviewer_name}}`: 评审者姓名
- `{{reviewee_name}}`: 被评审者姓名
- `{{assignment_id}}`: 评审分配ID
- `{{week_start_date}}`: 周开始日期
- `{{deadline_date}}`: 截止日期

#### 问题相关变量
- `{{issue_title}}`: 问题标题
- `{{issue_type}}`: 问题类型
- `{{issue_severity}}`: 问题严重级别
- `{{issue_count}}`: 问题数量
- `{{fix_deadline}}`: 整改截止时间

### 3.2 通知模板

#### 任务分配通知模板
```html
<!-- 系统内通知 -->
<div class="notification">
  <h3>新的评审任务</h3>
  <p>您好，{{user_name}}！</p>
  <p>您有一个新的代码评审任务：</p>
  <ul>
    <li><strong>被评审者：</strong>{{reviewee_name}}</li>
    <li><strong>周开始日期：</strong>{{week_start_date}}</li>
    <li><strong>截止时间：</strong>{{deadline_date}}</li>
  </ul>
  <p>请及时登录系统查看详情并完成评审。</p>
  <a href="{{system_url}}/review/{{assignment_id}}">查看详情</a>
</div>

<!-- 邮件模板 -->
主题：新的代码评审任务 - {{week_start_date}}

正文：
尊敬的 {{user_name}}：

您有一个新的代码评审任务需要处理：

评审任务详情：
- 被评审者：{{reviewee_name}}
- 周开始日期：{{week_start_date}}
- 截止时间：{{deadline_date}}

请及时登录系统完成评审工作。

系统链接：{{system_url}}/review/{{assignment_id}}

如有疑问，请联系您的团队负责人。

{{system_name}} 团队
```

#### 截止提醒模板
```html
<!-- 系统内通知 -->
<div class="notification warning">
  <h3>评审截止提醒</h3>
  <p>您好，{{user_name}}！</p>
  <p>您的评审任务即将到期：</p>
  <ul>
    <li><strong>被评审者：</strong>{{reviewee_name}}</li>
    <li><strong>截止时间：</strong>{{deadline_date}}</li>
    <li><strong>剩余时间：</strong>{{remaining_time}}</li>
  </ul>
  <p>请尽快完成评审，避免逾期。</p>
  <a href="{{system_url}}/review/{{assignment_id}}">立即处理</a>
</div>

<!-- 邮件模板 -->
主题：评审任务即将到期提醒

正文：
尊敬的 {{user_name}}：

您的代码评审任务即将到期，请及时处理：

任务详情：
- 被评审者：{{reviewee_name}}
- 截止时间：{{deadline_date}}
- 剩余时间：{{remaining_time}}

请尽快登录系统完成评审工作。

系统链接：{{system_url}}/review/{{assignment_id}}

{{system_name}} 团队
```

#### 整改提醒模板
```html
<!-- 系统内通知 -->
<div class="notification urgent">
  <h3>需要整改</h3>
  <p>您好，{{user_name}}！</p>
  <p>您的代码评审中发现了一些问题需要整改：</p>
  <ul>
    <li><strong>问题数量：</strong>{{issue_count}}</li>
    <li><strong>严重问题：</strong>{{critical_issues}}</li>
    <li><strong>整改截止：</strong>{{fix_deadline}}</li>
  </ul>
  <p>请及时查看问题详情并进行整改。</p>
  <a href="{{system_url}}/issues/my">查看问题</a>
</div>

<!-- 邮件模板 -->
主题：代码评审问题需要整改

正文：
尊敬的 {{user_name}}：

您的代码评审中发现了一些问题需要整改：

问题统计：
- 总问题数：{{issue_count}}
- 严重问题：{{critical_issues}}
- 一般问题：{{major_issues}}
- 轻微问题：{{minor_issues}}

请及时登录系统查看问题详情并进行整改。

系统链接：{{system_url}}/issues/my

{{system_name}} 团队
```

#### 验证请求模板
```html
<!-- 系统内通知 -->
<div class="notification">
  <h3>整改验证请求</h3>
  <p>您好，{{user_name}}！</p>
  <p>{{reviewee_name}} 已提交整改记录，需要您进行验证：</p>
  <ul>
    <li><strong>问题标题：</strong>{{issue_title}}</li>
    <li><strong>整改描述：</strong>{{fix_description}}</li>
    <li><strong>提交时间：</strong>{{submit_time}}</li>
  </ul>
  <p>请及时验证整改结果。</p>
  <a href="{{system_url}}/fix/{{fix_record_id}}">查看详情</a>
</div>
```

### 3.3 模板管理功能

#### 模板CRUD操作
```java
@Service
public class NotificationTemplateService {
    
    // 创建模板
    public NotificationTemplate createTemplate(TemplateCreateRequest request);
    
    // 更新模板
    public NotificationTemplate updateTemplate(Long id, TemplateUpdateRequest request);
    
    // 删除模板
    public void deleteTemplate(Long id);
    
    // 获取模板列表
    public Page<NotificationTemplate> getTemplates(TemplateQueryRequest request);
    
    // 获取模板详情
    public NotificationTemplate getTemplate(Long id);
    
    // 渲染模板
    public String renderTemplate(String templateCode, Map<String, Object> variables);
}
```

#### 模板版本管理
```java
@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {
    @Id
    private Long id;
    
    @Column(nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type;
    
    @Column(columnDefinition = "TEXT")
    private String systemContent;
    
    @Column(columnDefinition = "TEXT")
    private String emailSubject;
    
    @Column(columnDefinition = "TEXT")
    private String emailContent;
    
    @Column(columnDefinition = "TEXT")
    private String wechatContent;
    
    private Integer version;
    
    private Boolean isActive;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

## 4. 通知偏好设置

### 4.1 用户通知偏好

```java
@Entity
@Table(name = "user_notification_preferences")
public class UserNotificationPreference {
    @Id
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String notificationType;
    
    private Boolean systemEnabled = true;
    
    private Boolean emailEnabled = true;
    
    private Boolean wechatEnabled = false;
    
    private Boolean smsEnabled = false;
    
    private String quietHoursStart = "22:00";
    
    private String quietHoursEnd = "08:00";
    
    private Boolean quietHoursEnabled = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 4.2 团队通知设置

```java
@Entity
@Table(name = "team_notification_settings")
public class TeamNotificationSetting {
    @Id
    private Long id;
    
    @Column(nullable = false)
    private Long teamId;
    
    @Column(nullable = false)
    private String notificationType;
    
    private Boolean enabled = true;
    
    private String recipients; // JSON格式，指定接收人角色
    
    private String channels; // JSON格式，指定通知渠道
    
    private Integer advanceHours = 24; // 提前提醒小时数
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

## 5. 通知发送机制

### 5.1 发送策略

#### 立即发送
- 任务分配通知
- 问题整改通知
- 验证请求通知

#### 定时发送
- 截止提醒通知
- 系统维护通知
- 定期统计报告

#### 批量发送
- 团队通知
- 系统公告
- 周度汇总

### 5.2 发送失败处理

```java
@Service
public class NotificationRetryService {
    
    @Scheduled(fixedDelay = 300000) // 5分钟
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository
            .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, 3);
        
        for (Notification notification : failedNotifications) {
            try {
                notificationService.sendNotification(notification);
                notification.setStatus(NotificationStatus.SENT);
                notification.setRetryCount(notification.getRetryCount() + 1);
            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                if (notification.getRetryCount() >= 3) {
                    notification.setStatus(NotificationStatus.ABANDONED);
                }
            }
            notificationRepository.save(notification);
        }
    }
}
```

### 5.3 静默期管理

```java
@Component
public class NotificationQuietHoursService {
    
    public boolean isInQuietHours(UserNotificationPreference preference) {
        if (!preference.getQuietHoursEnabled()) {
            return false;
        }
        
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(preference.getQuietHoursStart());
        LocalTime end = LocalTime.parse(preference.getQuietHoursEnd());
        
        if (start.isBefore(end)) {
            return now.isAfter(start) && now.isBefore(end);
        } else {
            // 跨天的情况
            return now.isAfter(start) || now.isBefore(end);
        }
    }
    
    public void scheduleNotification(Notification notification, UserNotificationPreference preference) {
        if (isInQuietHours(preference)) {
            // 延迟到静默期结束后发送
            LocalDateTime sendTime = calculateNextSendTime(preference);
            notification.setScheduledAt(sendTime);
            notification.setStatus(NotificationStatus.SCHEDULED);
        } else {
            // 立即发送
            notification.setStatus(NotificationStatus.PENDING);
        }
    }
}
```

## 6. 通知统计和分析

### 6.1 发送统计

```sql
-- 通知发送统计
SELECT 
    type,
    status,
    COUNT(*) as count,
    DATE(created_at) as date
FROM notifications
WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY type, status, DATE(created_at)
ORDER BY date DESC, type;

-- 用户通知偏好统计
SELECT 
    notification_type,
    SUM(CASE WHEN system_enabled THEN 1 ELSE 0 END) as system_users,
    SUM(CASE WHEN email_enabled THEN 1 ELSE 0 END) as email_users,
    SUM(CASE WHEN wechat_enabled THEN 1 ELSE 0 END) as wechat_users
FROM user_notification_preferences
GROUP BY notification_type;
```

### 6.2 效果分析

```sql
-- 通知阅读率统计
SELECT 
    type,
    COUNT(*) as total_sent,
    SUM(CASE WHEN is_read THEN 1 ELSE 0 END) as read_count,
    ROUND(SUM(CASE WHEN is_read THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as read_rate
FROM notifications
WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY type
ORDER BY read_rate DESC;

-- 用户响应时间统计
SELECT 
    u.real_name,
    AVG(TIMESTAMPDIFF(MINUTE, n.created_at, n.read_at)) as avg_response_minutes
FROM notifications n
JOIN users u ON n.user_id = u.id
WHERE n.is_read = TRUE 
    AND n.read_at IS NOT NULL
    AND n.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY u.id, u.real_name
HAVING avg_response_minutes > 0
ORDER BY avg_response_minutes;
```

## 7. 配置管理

### 7.1 系统配置

```yaml
# application.yml
notification:
  # 默认通知设置
  default:
    system-enabled: true
    email-enabled: true
    wechat-enabled: false
    quiet-hours-enabled: true
    quiet-hours-start: "22:00"
    quiet-hours-end: "08:00"
  
  # 重试设置
  retry:
    max-attempts: 3
    initial-delay: 300000  # 5分钟
    multiplier: 2
    max-delay: 3600000     # 1小时
  
  # 批量发送设置
  batch:
    max-size: 100
    delay-between-batches: 1000  # 1秒
  
  # 渠道配置
  channels:
    email:
      enabled: true
      from-address: "noreply@company.com"
      template-path: "templates/email"
    wechat:
      enabled: false
      app-id: "${WECHAT_APP_ID}"
      app-secret: "${WECHAT_APP_SECRET}"
    sms:
      enabled: false
      provider: "aliyun"
      access-key: "${SMS_ACCESS_KEY}"
      secret-key: "${SMS_SECRET_KEY}"
```

### 7.2 动态配置

```java
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfig {
    
    private DefaultSettings defaultSettings = new DefaultSettings();
    private RetrySettings retrySettings = new RetrySettings();
    private BatchSettings batchSettings = new BatchSettings();
    private Map<String, ChannelConfig> channels = new HashMap<>();
    
    @Data
    public static class DefaultSettings {
        private boolean systemEnabled = true;
        private boolean emailEnabled = true;
        private boolean wechatEnabled = false;
        private boolean quietHoursEnabled = true;
        private String quietHoursStart = "22:00";
        private String quietHoursEnd = "08:00";
    }
    
    @Data
    public static class RetrySettings {
        private int maxAttempts = 3;
        private long initialDelay = 300000;
        private double multiplier = 2.0;
        private long maxDelay = 3600000;
    }
    
    @Data
    public static class BatchSettings {
        private int maxSize = 100;
        private long delayBetweenBatches = 1000;
    }
    
    @Data
    public static class ChannelConfig {
        private boolean enabled = false;
        private Map<String, String> properties = new HashMap<>();
    }
}
```

## 8. 监控和告警

### 8.1 监控指标

- 通知发送成功率
- 通知发送延迟
- 用户阅读率
- 渠道可用性
- 系统负载

### 8.2 告警规则

```yaml
# 告警配置
alerts:
  - name: "notification_send_failure_rate"
    condition: "failure_rate > 0.1"  # 失败率超过10%
    duration: "5m"
    severity: "warning"
    
  - name: "notification_send_delay"
    condition: "avg_delay > 300000"  # 平均延迟超过5分钟
    duration: "10m"
    severity: "warning"
    
  - name: "notification_queue_size"
    condition: "queue_size > 1000"   # 队列大小超过1000
    duration: "2m"
    severity: "critical"
```

## 9. 最佳实践

### 9.1 模板设计原则

1. **简洁明了**: 通知内容要简洁，重点突出
2. **行动导向**: 明确告知用户需要做什么
3. **个性化**: 使用用户姓名和相关上下文信息
4. **多渠道适配**: 不同渠道使用不同的内容格式

### 9.2 发送策略建议

1. **避免过度通知**: 合理设置通知频率和静默期
2. **优先级管理**: 根据通知重要性设置不同优先级
3. **用户控制**: 允许用户自定义通知偏好
4. **效果跟踪**: 持续监控通知效果并优化

### 9.3 性能优化

1. **异步处理**: 使用消息队列异步发送通知
2. **批量发送**: 对相同类型的通知进行批量处理
3. **缓存模板**: 缓存渲染后的通知模板
4. **连接池**: 为外部服务连接使用连接池 