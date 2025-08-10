package com.company.codereview.user.dto;

import com.company.codereview.user.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 通知偏好设置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    
    /**
     * 偏好设置ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 通知类型
     */
    @NotNull(message = "通知类型不能为空")
    private Notification.NotificationType notificationType;
    
    /**
     * 通知类型描述
     */
    private String notificationTypeDesc;
    
    /**
     * 是否启用站内信
     */
    private Boolean inAppEnabled;
    
    /**
     * 是否启用邮件通知
     */
    private Boolean emailEnabled;
    
    /**
     * 是否启用企业微信通知
     */
    private Boolean wechatWorkEnabled;
    
    /**
     * 是否启用短信通知
     */
    private Boolean smsEnabled;
    
    /**
     * 是否启用免打扰
     */
    private Boolean quietEnabled;
    
    /**
     * 免打扰时间开始（HH:mm格式）
     */
    private String quietStartTime;
    
    /**
     * 免打扰时间结束（HH:mm格式）
     */
    private String quietEndTime;
    
    /**
     * 通知频率限制（分钟）
     */
    @Min(value = 1, message = "频率限制不能小于1分钟")
    @Max(value = 1440, message = "频率限制不能大于1440分钟")
    private Integer frequencyLimit;
    
    /**
     * 获取通知类型描述
     */
    public String getNotificationTypeDesc() {
        return notificationType != null ? notificationType.getDescription() : null;
    }
}