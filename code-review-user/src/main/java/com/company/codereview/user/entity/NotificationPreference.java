package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知偏好设置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_preferences")
public class NotificationPreference extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 通知类型
     */
    @TableField("notification_type")
    private Notification.NotificationType notificationType;
    
    /**
     * 是否启用站内信
     */
    @TableField("in_app_enabled")
    private Boolean inAppEnabled;
    
    /**
     * 是否启用邮件通知
     */
    @TableField("email_enabled")
    private Boolean emailEnabled;
    
    /**
     * 是否启用企业微信通知
     */
    @TableField("wechat_work_enabled")
    private Boolean wechatWorkEnabled;
    
    /**
     * 是否启用短信通知
     */
    @TableField("sms_enabled")
    private Boolean smsEnabled;
    
    /**
     * 免打扰时间开始（HH:mm格式）
     */
    @TableField("quiet_start_time")
    private String quietStartTime;
    
    /**
     * 免打扰时间结束（HH:mm格式）
     */
    @TableField("quiet_end_time")
    private String quietEndTime;
    
    /**
     * 是否启用免打扰
     */
    @TableField("quiet_enabled")
    private Boolean quietEnabled;
    
    /**
     * 通知频率限制（分钟）
     */
    @TableField("frequency_limit")
    private Integer frequencyLimit;
}