package com.company.codereview.user.service.notification;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.service.notification.channel.EmailNotificationChannel;
import com.company.codereview.user.service.notification.channel.InAppNotificationChannel;
import com.company.codereview.user.service.notification.channel.SmsNotificationChannel;
import com.company.codereview.user.service.notification.channel.WechatWorkNotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知渠道服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationChannelService {
    
    private final InAppNotificationChannel inAppChannel;
    private final EmailNotificationChannel emailChannel;
    private final WechatWorkNotificationChannel wechatWorkChannel;
    private final SmsNotificationChannel smsChannel;
    
    private final Map<String, NotificationChannel> channels = new HashMap<>();
    
    /**
     * 初始化通知渠道
     */
    public void initChannels() {
        channels.put("IN_APP", inAppChannel);
        channels.put("EMAIL", emailChannel);
        channels.put("WECHAT_WORK", wechatWorkChannel);
        channels.put("SMS", smsChannel);
    }
    
    /**
     * 发送通知
     */
    public void sendNotification(Notification notification, String channelName) throws Exception {
        if (channels.isEmpty()) {
            initChannels();
        }
        
        NotificationChannel channel = channels.get(channelName.toUpperCase());
        if (channel == null) {
            throw new IllegalArgumentException("不支持的通知渠道: " + channelName);
        }
        
        if (!channel.isEnabled()) {
            log.warn("通知渠道 {} 已禁用，跳过发送", channelName);
            return;
        }
        
        try {
            channel.sendNotification(notification);
            log.debug("通过渠道 {} 发送通知成功: {}", channelName, notification.getId());
        } catch (Exception e) {
            log.error("通过渠道 {} 发送通知失败: {}", channelName, notification.getId(), e);
            throw e;
        }
    }
    
    /**
     * 检查渠道是否可用
     */
    public boolean isChannelAvailable(String channelName) {
        if (channels.isEmpty()) {
            initChannels();
        }
        
        NotificationChannel channel = channels.get(channelName.toUpperCase());
        return channel != null && channel.isEnabled();
    }
    
    /**
     * 获取所有可用的渠道
     */
    public Map<String, Boolean> getAvailableChannels() {
        if (channels.isEmpty()) {
            initChannels();
        }
        
        Map<String, Boolean> availableChannels = new HashMap<>();
        for (Map.Entry<String, NotificationChannel> entry : channels.entrySet()) {
            availableChannels.put(entry.getKey(), entry.getValue().isEnabled());
        }
        
        return availableChannels;
    }
}