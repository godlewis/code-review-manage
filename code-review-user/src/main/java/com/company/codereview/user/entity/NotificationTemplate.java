package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_templates")
public class NotificationTemplate extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;
    
    /**
     * 通知类型
     */
    @TableField("notification_type")
    private Notification.NotificationType notificationType;
    
    /**
     * 通知渠道
     */
    @TableField("channel")
    private NotificationChannel channel;
    
    /**
     * 模板标题
     */
    @TableField("title_template")
    private String titleTemplate;
    
    /**
     * 模板内容
     */
    @TableField("content_template")
    private String contentTemplate;
    
    /**
     * 模板变量说明（JSON格式）
     */
    @TableField("variables")
    private String variables;
    
    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;
    
    /**
     * 模板描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 通知渠道枚举
     */
    public enum NotificationChannel {
        IN_APP("站内信"),
        EMAIL("邮件"),
        WECHAT_WORK("企业微信"),
        SMS("短信"),
        WEBHOOK("Webhook");
        
        private final String description;
        
        NotificationChannel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}