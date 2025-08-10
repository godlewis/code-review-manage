package com.company.codereview.user.service.notification;

import com.company.codereview.user.entity.Notification;

/**
 * 通知渠道接口
 */
public interface NotificationChannel {
    
    /**
     * 发送通知
     */
    void sendNotification(Notification notification) throws Exception;
    
    /**
     * 渠道是否启用
     */
    boolean isEnabled();
    
    /**
     * 获取渠道名称
     */
    String getChannelName();
    
    /**
     * 获取渠道描述
     */
    String getChannelDescription();
    
    /**
     * 验证通知内容格式
     */
    default boolean validateNotification(Notification notification) {
        return notification != null && 
               notification.getTitle() != null && 
               notification.getContent() != null &&
               notification.getRecipientId() != null;
    }
}