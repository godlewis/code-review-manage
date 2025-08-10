package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("notifications")
public class Notification extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 接收者用户ID
     */
    @TableField("recipient_id")
    private Long recipientId;
    
    /**
     * 通知类型
     */
    @TableField("notification_type")
    private NotificationType notificationType;
    
    /**
     * 通知标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 通知内容
     */
    @TableField("content")
    private String content;
    
    /**
     * 关联的业务ID（如评审记录ID、问题ID等）
     */
    @TableField("related_id")
    private Long relatedId;
    
    /**
     * 关联的业务类型
     */
    @TableField("related_type")
    private String relatedType;
    
    /**
     * 通知渠道（多个渠道用逗号分隔）
     */
    @TableField("channels")
    private String channels;
    
    /**
     * 通知状态
     */
    @TableField("status")
    private NotificationStatus status;
    
    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead;
    
    /**
     * 阅读时间
     */
    @TableField("read_at")
    private LocalDateTime readAt;
    
    /**
     * 发送时间
     */
    @TableField("sent_at")
    private LocalDateTime sentAt;
    
    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;
    
    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;
    
    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        REVIEW_ASSIGNED("评审分配"),
        REVIEW_SUBMITTED("评审提交"),
        ISSUE_CREATED("问题创建"),
        ISSUE_ASSIGNED("问题分配"),
        FIX_SUBMITTED("整改提交"),
        FIX_VERIFIED("整改验证"),
        SUMMARY_GENERATED("汇总生成"),
        DEADLINE_REMINDER("截止提醒"),
        SYSTEM_ANNOUNCEMENT("系统公告"),
        SYSTEM_NOTIFICATION("系统通知");
        
        private final String description;
        
        NotificationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 通知状态枚举
     */
    public enum NotificationStatus {
        PENDING("待发送"),
        SENT("已发送"),
        FAILED("发送失败"),
        CANCELLED("已取消");
        
        private final String description;
        
        NotificationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}