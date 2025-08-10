package com.company.codereview.user.dto;

import com.company.codereview.user.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 接收者用户ID
     */
    private Long recipientId;
    
    /**
     * 接收者用户名
     */
    private String recipientName;
    
    /**
     * 通知类型
     */
    private Notification.NotificationType notificationType;
    
    /**
     * 通知类型描述
     */
    private String notificationTypeDesc;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 关联的业务ID
     */
    private Long relatedId;
    
    /**
     * 关联的业务类型
     */
    private String relatedType;
    
    /**
     * 通知渠道
     */
    private String channels;
    
    /**
     * 通知状态
     */
    private Notification.NotificationStatus status;
    
    /**
     * 通知状态描述
     */
    private String statusDesc;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 阅读时间
     */
    private LocalDateTime readAt;
    
    /**
     * 发送时间
     */
    private LocalDateTime sentAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 扩展数据
     */
    private Map<String, Object> extraData;
}