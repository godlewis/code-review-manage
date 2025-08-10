package com.company.codereview.user.service.notification.channel;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.service.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 站内信通知渠道
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationChannel implements NotificationChannel {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void sendNotification(Notification notification) throws Exception {
        if (!validateNotification(notification)) {
            throw new IllegalArgumentException("通知内容验证失败");
        }
        
        try {
            // 构建WebSocket消息
            Map<String, Object> message = new HashMap<>();
            message.put("id", notification.getId());
            message.put("type", notification.getNotificationType().name());
            message.put("title", notification.getTitle());
            message.put("content", notification.getContent());
            message.put("relatedId", notification.getRelatedId());
            message.put("relatedType", notification.getRelatedType());
            message.put("timestamp", System.currentTimeMillis());
            
            // 发送WebSocket消息到指定用户
            String destination = "/user/" + notification.getRecipientId() + "/notifications";
            messagingTemplate.convertAndSend(destination, message);
            
            // 缓存通知到Redis，用于用户重新连接时获取未读消息
            String cacheKey = "user:notifications:" + notification.getRecipientId();
            redisTemplate.opsForList().leftPush(cacheKey, message);
            redisTemplate.expire(cacheKey, 7, TimeUnit.DAYS); // 缓存7天
            
            // 限制缓存数量，只保留最近100条
            redisTemplate.opsForList().trim(cacheKey, 0, 99);
            
            log.debug("站内信发送成功: 用户={}, 通知={}", notification.getRecipientId(), notification.getId());
            
        } catch (Exception e) {
            log.error("站内信发送失败: 用户={}, 通知={}", notification.getRecipientId(), notification.getId(), e);
            throw new Exception("站内信发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        // 站内信默认启用
        return true;
    }
    
    @Override
    public String getChannelName() {
        return "IN_APP";
    }
    
    @Override
    public String getChannelDescription() {
        return "站内信通知";
    }
}