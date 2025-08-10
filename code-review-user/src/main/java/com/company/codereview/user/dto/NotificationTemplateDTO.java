package com.company.codereview.user.dto;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 通知模板DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateDTO {
    
    /**
     * 模板ID
     */
    private Long id;
    
    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    
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
     * 通知渠道
     */
    @NotNull(message = "通知渠道不能为空")
    private NotificationTemplate.NotificationChannel channel;
    
    /**
     * 通知渠道描述
     */
    private String channelDesc;
    
    /**
     * 模板标题
     */
    @NotBlank(message = "标题模板不能为空")
    private String titleTemplate;
    
    /**
     * 模板内容
     */
    @NotBlank(message = "内容模板不能为空")
    private String contentTemplate;
    
    /**
     * 模板变量说明
     */
    private String variables;
    
    /**
     * 是否启用
     */
    private Boolean isEnabled;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 渲染后的标题（用于预览）
     */
    private String renderedTitle;
    
    /**
     * 渲染后的内容（用于预览）
     */
    private String renderedContent;
    
    /**
     * 获取通知类型描述
     */
    public String getNotificationTypeDesc() {
        return notificationType != null ? notificationType.getDescription() : null;
    }
    
    /**
     * 获取通知渠道描述
     */
    public String getChannelDesc() {
        return channel != null ? channel.getDescription() : null;
    }
}