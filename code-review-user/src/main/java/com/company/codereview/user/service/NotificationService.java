package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.codereview.user.dto.NotificationDTO;
import com.company.codereview.user.dto.NotificationRequest;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationPreference;
import com.company.codereview.user.entity.NotificationTemplate;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.NotificationRepository;
import com.company.codereview.user.repository.NotificationPreferenceRepository;
import com.company.codereview.user.repository.NotificationTemplateRepository;
import com.company.codereview.user.repository.UserRepository;
import com.company.codereview.user.service.notification.NotificationChannelService;
import com.company.codereview.user.service.notification.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final NotificationChannelService channelService;
    private final NotificationTemplateService templateService;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String NOTIFICATION_QUEUE = "notification.queue";
    private static final String FREQUENCY_LIMIT_KEY = "notification:frequency:";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 发送通知
     */
    @Transactional
    public void sendNotification(NotificationRequest request) {
        log.info("发送通知请求: {}", request);
        
        if (CollectionUtils.isEmpty(request.getRecipientIds())) {
            log.warn("接收者列表为空，跳过发送");
            return;
        }
        
        // 获取接收者信息
        List<User> recipients = userRepository.selectBatchIds(request.getRecipientIds());
        if (CollectionUtils.isEmpty(recipients)) {
            log.warn("未找到有效的接收者，跳过发送");
            return;
        }
        
        // 为每个接收者创建通知记录
        List<Notification> notifications = new ArrayList<>();
        for (User recipient : recipients) {
            // 检查用户偏好设置
            if (!shouldSendNotification(recipient.getId(), request.getNotificationType())) {
                log.debug("用户 {} 已禁用此类型通知，跳过发送", recipient.getId());
                continue;
            }
            
            // 检查频率限制
            if (isFrequencyLimited(recipient.getId(), request.getNotificationType())) {
                log.debug("用户 {} 通知频率受限，跳过发送", recipient.getId());
                continue;
            }
            
            // 获取用户的通知渠道
            List<String> channels = getUserNotificationChannels(recipient.getId(), 
                request.getNotificationType(), request.getChannels());
            
            if (CollectionUtils.isEmpty(channels)) {
                log.debug("用户 {} 没有启用的通知渠道，跳过发送", recipient.getId());
                continue;
            }
            
            // 渲染通知内容
            String title = renderNotificationContent(request.getTitle(), request.getTemplateVariables());
            String content = renderNotificationContent(request.getContent(), request.getTemplateVariables());
            
            // 创建通知记录
            Notification notification = Notification.builder()
                    .recipientId(recipient.getId())
                    .notificationType(request.getNotificationType())
                    .title(title)
                    .content(content)
                    .relatedId(request.getRelatedId())
                    .relatedType(request.getRelatedType())
                    .channels(String.join(",", channels))
                    .status(Notification.NotificationStatus.PENDING)
                    .isRead(false)
                    .retryCount(0)
                    .build();
            
            notifications.add(notification);
        }
        
        if (CollectionUtils.isEmpty(notifications)) {
            log.info("没有需要发送的通知");
            return;
        }
        
        // 批量保存通知记录
        for (Notification notification : notifications) {
            notificationRepository.insert(notification);
        }
        
        // 发送通知
        if (Boolean.TRUE.equals(request.getImmediate())) {
            // 立即发送
            for (Notification notification : notifications) {
                processNotification(notification);
            }
        } else {
            // 异步发送
            for (Notification notification : notifications) {
                if (request.getDelayMinutes() != null && request.getDelayMinutes() > 0) {
                    // 延迟发送
                    scheduleNotification(notification, request.getDelayMinutes());
                } else {
                    // 加入队列
                    rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, notification.getId());
                }
            }
        }
        
        log.info("通知发送请求处理完成，共创建 {} 条通知记录", notifications.size());
    }
    
    /**
     * 处理单个通知
     */
    @Transactional
    public void processNotification(Notification notification) {
        log.debug("处理通知: {}", notification.getId());
        
        try {
            // 检查是否在免打扰时间
            if (isInQuietTime(notification.getRecipientId())) {
                log.debug("用户 {} 处于免打扰时间，延迟发送", notification.getRecipientId());
                scheduleNotification(notification, 60); // 延迟1小时
                return;
            }
            
            // 解析通知渠道
            List<String> channels = Arrays.asList(notification.getChannels().split(","));
            boolean allSuccess = true;
            StringBuilder errorMessages = new StringBuilder();
            
            // 逐个渠道发送
            for (String channel : channels) {
                try {
                    channelService.sendNotification(notification, channel.trim());
                    log.debug("通知 {} 通过渠道 {} 发送成功", notification.getId(), channel);
                } catch (Exception e) {
                    log.error("通知 {} 通过渠道 {} 发送失败", notification.getId(), channel, e);
                    allSuccess = false;
                    if (errorMessages.length() > 0) {
                        errorMessages.append("; ");
                    }
                    errorMessages.append(channel).append(": ").append(e.getMessage());
                }
            }
            
            // 更新通知状态
            if (allSuccess) {
                updateNotificationStatus(notification.getId(), 
                    Notification.NotificationStatus.SENT, 
                    LocalDateTime.now(), null, notification.getRetryCount());
                
                // 更新频率限制
                updateFrequencyLimit(notification.getRecipientId(), notification.getNotificationType());
            } else {
                // 部分失败，检查是否需要重试
                if (notification.getRetryCount() < MAX_RETRY_COUNT) {
                    updateNotificationStatus(notification.getId(), 
                        Notification.NotificationStatus.PENDING, 
                        null, errorMessages.toString(), notification.getRetryCount() + 1);
                    
                    // 延迟重试
                    scheduleNotification(notification, (notification.getRetryCount() + 1) * 5);
                } else {
                    updateNotificationStatus(notification.getId(), 
                        Notification.NotificationStatus.FAILED, 
                        null, errorMessages.toString(), notification.getRetryCount() + 1);
                }
            }
            
        } catch (Exception e) {
            log.error("处理通知 {} 时发生异常", notification.getId(), e);
            
            // 更新为失败状态
            updateNotificationStatus(notification.getId(), 
                Notification.NotificationStatus.FAILED, 
                null, e.getMessage(), notification.getRetryCount() + 1);
        }
    }
    
    /**
     * 获取用户通知列表
     */
    @Cacheable(value = "user-notifications", key = "#userId + '-' + #page + '-' + #size")
    public IPage<NotificationDTO> getUserNotifications(Long userId, int page, int size, Boolean unreadOnly) {
        Page<Notification> pageRequest = new Page<>(page, size);
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recipient_id", userId);
        
        if (Boolean.TRUE.equals(unreadOnly)) {
            queryWrapper.eq("is_read", false);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        IPage<Notification> notificationPage = notificationRepository.selectPage(pageRequest, queryWrapper);
        
        // 转换为DTO
        IPage<NotificationDTO> dtoPage = new Page<>(page, size, notificationPage.getTotal());
        List<NotificationDTO> dtoList = notificationPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 获取用户未读通知数量
     */
    @Cacheable(value = "unread-count", key = "#userId")
    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByRecipientId(userId);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional
    @CacheEvict(value = {"user-notifications", "unread-count"}, key = "#userId")
    public void markAsRead(Long userId, List<Long> notificationIds) {
        if (CollectionUtils.isEmpty(notificationIds)) {
            return;
        }
        
        notificationRepository.batchMarkAsRead(notificationIds, userId, LocalDateTime.now());
        log.info("用户 {} 标记 {} 条通知为已读", userId, notificationIds.size());
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional
    @CacheEvict(value = {"user-notifications", "unread-count"}, key = "#userId")
    public void markAllAsRead(Long userId) {
        int count = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("用户 {} 标记所有通知为已读，共 {} 条", userId, count);
    }
    
    /**
     * 删除通知
     */
    @Transactional
    @CacheEvict(value = {"user-notifications", "unread-count"}, key = "#userId")
    public void deleteNotifications(Long userId, List<Long> notificationIds) {
        if (CollectionUtils.isEmpty(notificationIds)) {
            return;
        }
        
        QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("recipient_id", userId);
        queryWrapper.in("id", notificationIds);
        
        int count = notificationRepository.delete(queryWrapper);
        log.info("用户 {} 删除 {} 条通知", userId, count);
    }
    
    /**
     * 清理过期通知
     */
    @Transactional
    public void cleanupExpiredNotifications(int daysToKeep) {
        LocalDateTime expiredBefore = LocalDateTime.now().minusDays(daysToKeep);
        int count = notificationRepository.deleteExpiredNotifications(expiredBefore);
        log.info("清理过期通知完成，删除 {} 条记录", count);
    }
    
    /**
     * 重试失败的通知
     */
    @Transactional
    public void retryFailedNotifications() {
        LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(30);
        List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(MAX_RETRY_COUNT, retryAfter, 100);
        
        for (Notification notification : failedNotifications) {
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, notification.getId());
        }
        
        log.info("重新加入队列的失败通知数量: {}", failedNotifications.size());
    }
    
    // 私有辅助方法
    
    private boolean shouldSendNotification(Long userId, Notification.NotificationType notificationType) {
        Optional<NotificationPreference> preference = preferenceRepository
                .findByUserIdAndType(userId, notificationType);
        
        if (preference.isPresent()) {
            NotificationPreference pref = preference.get();
            // 检查是否有任何渠道启用
            return Boolean.TRUE.equals(pref.getInAppEnabled()) ||
                   Boolean.TRUE.equals(pref.getEmailEnabled()) ||
                   Boolean.TRUE.equals(pref.getWechatWorkEnabled()) ||
                   Boolean.TRUE.equals(pref.getSmsEnabled());
        }
        
        // 默认启用站内信
        return true;
    }
    
    private boolean isFrequencyLimited(Long userId, Notification.NotificationType notificationType) {
        String key = FREQUENCY_LIMIT_KEY + userId + ":" + notificationType.name();
        String lastSent = (String) redisTemplate.opsForValue().get(key);
        
        if (lastSent == null) {
            return false;
        }
        
        // 获取用户的频率限制设置
        Optional<NotificationPreference> preference = preferenceRepository
                .findByUserIdAndType(userId, notificationType);
        
        int limitMinutes = preference.map(NotificationPreference::getFrequencyLimit).orElse(5);
        
        LocalDateTime lastSentTime = LocalDateTime.parse(lastSent);
        return lastSentTime.plusMinutes(limitMinutes).isAfter(LocalDateTime.now());
    }
    
    private void updateFrequencyLimit(Long userId, Notification.NotificationType notificationType) {
        String key = FREQUENCY_LIMIT_KEY + userId + ":" + notificationType.name();
        redisTemplate.opsForValue().set(key, LocalDateTime.now().toString(), 1, TimeUnit.HOURS);
    }
    
    private List<String> getUserNotificationChannels(Long userId, 
                                                    Notification.NotificationType notificationType,
                                                    List<String> requestChannels) {
        List<String> channels = new ArrayList<>();
        
        // 如果请求中指定了渠道，直接使用
        if (!CollectionUtils.isEmpty(requestChannels)) {
            return requestChannels;
        }
        
        // 否则根据用户偏好设置确定渠道
        Optional<NotificationPreference> preference = preferenceRepository
                .findByUserIdAndType(userId, notificationType);
        
        if (preference.isPresent()) {
            NotificationPreference pref = preference.get();
            if (Boolean.TRUE.equals(pref.getInAppEnabled())) {
                channels.add("IN_APP");
            }
            if (Boolean.TRUE.equals(pref.getEmailEnabled())) {
                channels.add("EMAIL");
            }
            if (Boolean.TRUE.equals(pref.getWechatWorkEnabled())) {
                channels.add("WECHAT_WORK");
            }
            if (Boolean.TRUE.equals(pref.getSmsEnabled())) {
                channels.add("SMS");
            }
        } else {
            // 默认启用站内信
            channels.add("IN_APP");
        }
        
        return channels;
    }
    
    private boolean isInQuietTime(Long userId) {
        Optional<NotificationPreference> preference = preferenceRepository
                .findByUserIdAndType(userId, null); // 获取通用设置
        
        if (preference.isEmpty() || !Boolean.TRUE.equals(preference.get().getQuietEnabled())) {
            return false;
        }
        
        NotificationPreference pref = preference.get();
        if (!StringUtils.hasText(pref.getQuietStartTime()) || 
            !StringUtils.hasText(pref.getQuietEndTime())) {
            return false;
        }
        
        try {
            LocalTime now = LocalTime.now();
            LocalTime startTime = LocalTime.parse(pref.getQuietStartTime());
            LocalTime endTime = LocalTime.parse(pref.getQuietEndTime());
            
            if (startTime.isBefore(endTime)) {
                // 同一天内的时间段
                return now.isAfter(startTime) && now.isBefore(endTime);
            } else {
                // 跨天的时间段
                return now.isAfter(startTime) || now.isBefore(endTime);
            }
        } catch (Exception e) {
            log.warn("解析免打扰时间失败: {}", e.getMessage());
            return false;
        }
    }
    
    private String renderNotificationContent(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template) || CollectionUtils.isEmpty(variables)) {
            return template;
        }
        
        return templateService.renderTemplate(template, variables);
    }
    
    private void scheduleNotification(Notification notification, int delayMinutes) {
        // 使用RabbitMQ的延迟队列功能
        rabbitTemplate.convertAndSend("notification.delay.exchange", 
            "notification.delay.routing.key", 
            notification.getId(),
            message -> {
                message.getMessageProperties().setDelay(delayMinutes * 60 * 1000);
                return message;
            });
    }
    
    private void updateNotificationStatus(Long id, Notification.NotificationStatus status, 
                                        LocalDateTime sentAt, String errorMessage, Integer retryCount) {
        notificationRepository.updateNotificationStatus(id, status, sentAt, errorMessage, retryCount);
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .notificationType(notification.getNotificationType())
                .notificationTypeDesc(notification.getNotificationType().getDescription())
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedId(notification.getRelatedId())
                .relatedType(notification.getRelatedType())
                .channels(notification.getChannels())
                .status(notification.getStatus())
                .statusDesc(notification.getStatus().getDescription())
                .isRead(notification.getIsRead())
                .readAt(notification.getReadAt())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .retryCount(notification.getRetryCount())
                .errorMessage(notification.getErrorMessage())
                .build();
    }
}