package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.user.dto.NotificationPreferenceDTO;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationPreference;
import com.company.codereview.user.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通知偏好设置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    
    /**
     * 获取用户通知偏好设置
     */
    @Cacheable(value = "notification-preferences", key = "#userId")
    public List<NotificationPreferenceDTO> getUserPreferences(Long userId) {
        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        
        // 如果用户没有设置偏好，返回默认设置
        if (CollectionUtils.isEmpty(preferences)) {
            return getDefaultPreferences(userId);
        }
        
        return preferences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户特定类型的通知偏好
     */
    @Cacheable(value = "notification-preference", key = "#userId + '-' + #notificationType")
    public NotificationPreferenceDTO getUserPreference(Long userId, Notification.NotificationType notificationType) {
        Optional<NotificationPreference> preferenceOpt = preferenceRepository
                .findByUserIdAndType(userId, notificationType);
        
        if (preferenceOpt.isPresent()) {
            return convertToDTO(preferenceOpt.get());
        }
        
        // 返回默认设置
        return getDefaultPreference(userId, notificationType);
    }
    
    /**
     * 更新用户通知偏好设置
     */
    @Transactional
    @CacheEvict(value = {"notification-preferences", "notification-preference"}, key = "#userId")
    public void updateUserPreferences(Long userId, List<NotificationPreferenceDTO> preferences) {
        log.info("更新用户 {} 的通知偏好设置", userId);
        
        if (CollectionUtils.isEmpty(preferences)) {
            log.warn("通知偏好设置为空，跳过更新");
            return;
        }
        
        // 删除现有设置
        QueryWrapper<NotificationPreference> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("user_id", userId);
        preferenceRepository.delete(deleteWrapper);
        
        // 插入新设置
        List<NotificationPreference> entities = preferences.stream()
                .map(dto -> convertToEntity(dto, userId))
                .collect(Collectors.toList());
        
        for (NotificationPreference entity : entities) {
            preferenceRepository.insert(entity);
        }
        
        log.info("用户 {} 的通知偏好设置更新完成，共 {} 项", userId, entities.size());
    }
    
    /**
     * 更新单个通知偏好设置
     */
    @Transactional
    @CacheEvict(value = {"notification-preferences", "notification-preference"}, key = "#userId")
    public void updateUserPreference(Long userId, NotificationPreferenceDTO preference) {
        log.info("更新用户 {} 的通知偏好设置: {}", userId, preference.getNotificationType());
        
        Optional<NotificationPreference> existingOpt = preferenceRepository
                .findByUserIdAndType(userId, preference.getNotificationType());
        
        NotificationPreference entity = convertToEntity(preference, userId);
        
        if (existingOpt.isPresent()) {
            // 更新现有设置
            entity.setId(existingOpt.get().getId());
            preferenceRepository.updateById(entity);
        } else {
            // 插入新设置
            preferenceRepository.insert(entity);
        }
        
        log.info("用户 {} 的通知偏好设置更新完成: {}", userId, preference.getNotificationType());
    }
    
    /**
     * 重置用户通知偏好为默认值
     */
    @Transactional
    @CacheEvict(value = {"notification-preferences", "notification-preference"}, key = "#userId")
    public void resetToDefault(Long userId) {
        log.info("重置用户 {} 的通知偏好设置为默认值", userId);
        
        // 删除现有设置
        QueryWrapper<NotificationPreference> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("user_id", userId);
        preferenceRepository.delete(deleteWrapper);
        
        // 插入默认设置
        List<NotificationPreferenceDTO> defaultPreferences = getDefaultPreferences(userId);
        List<NotificationPreference> entities = defaultPreferences.stream()
                .map(dto -> convertToEntity(dto, userId))
                .collect(Collectors.toList());
        
        for (NotificationPreference entity : entities) {
            preferenceRepository.insert(entity);
        }
        
        log.info("用户 {} 的通知偏好设置重置完成", userId);
    }
    
    /**
     * 批量获取用户偏好设置
     */
    public Map<Long, List<NotificationPreferenceDTO>> getBatchUserPreferences(List<Long> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        this::getUserPreferences
                ));
    }
    
    /**
     * 检查用户是否启用了特定渠道的通知
     */
    public boolean isChannelEnabled(Long userId, Notification.NotificationType notificationType, String channel) {
        NotificationPreferenceDTO preference = getUserPreference(userId, notificationType);
        
        switch (channel.toUpperCase()) {
            case "IN_APP":
                return Boolean.TRUE.equals(preference.getInAppEnabled());
            case "EMAIL":
                return Boolean.TRUE.equals(preference.getEmailEnabled());
            case "WECHAT_WORK":
                return Boolean.TRUE.equals(preference.getWechatWorkEnabled());
            case "SMS":
                return Boolean.TRUE.equals(preference.getSmsEnabled());
            default:
                return false;
        }
    }
    
    /**
     * 获取启用了特定渠道的用户列表
     */
    public List<Long> getUsersWithChannelEnabled(Notification.NotificationType notificationType, String channel) {
        return preferenceRepository.findUsersWithChannelEnabled(notificationType, channel);
    }
    
    // 私有辅助方法
    
    private List<NotificationPreferenceDTO> getDefaultPreferences(Long userId) {
        List<NotificationPreferenceDTO> defaults = new ArrayList<>();
        
        // 为每种通知类型创建默认设置
        for (Notification.NotificationType type : Notification.NotificationType.values()) {
            defaults.add(getDefaultPreference(userId, type));
        }
        
        return defaults;
    }
    
    private NotificationPreferenceDTO getDefaultPreference(Long userId, Notification.NotificationType notificationType) {
        NotificationPreferenceDTO preference = new NotificationPreferenceDTO();
        preference.setUserId(userId);
        preference.setNotificationType(notificationType);
        
        // 根据通知类型设置默认值
        switch (notificationType) {
            case REVIEW_ASSIGNED:
            case ISSUE_ASSIGNED:
                // 重要通知默认启用所有渠道
                preference.setInAppEnabled(true);
                preference.setEmailEnabled(true);
                preference.setWechatWorkEnabled(false);
                preference.setSmsEnabled(false);
                break;
                
            case REVIEW_SUBMITTED:
            case FIX_SUBMITTED:
            case FIX_VERIFIED:
                // 一般通知默认启用站内信和邮件
                preference.setInAppEnabled(true);
                preference.setEmailEnabled(true);
                preference.setWechatWorkEnabled(false);
                preference.setSmsEnabled(false);
                break;
                
            case DEADLINE_REMINDER:
                // 提醒类通知默认启用站内信
                preference.setInAppEnabled(true);
                preference.setEmailEnabled(false);
                preference.setWechatWorkEnabled(false);
                preference.setSmsEnabled(false);
                break;
                
            case SYSTEM_ANNOUNCEMENT:
                // 系统公告默认只启用站内信
                preference.setInAppEnabled(true);
                preference.setEmailEnabled(false);
                preference.setWechatWorkEnabled(false);
                preference.setSmsEnabled(false);
                break;
                
            default:
                // 其他通知默认只启用站内信
                preference.setInAppEnabled(true);
                preference.setEmailEnabled(false);
                preference.setWechatWorkEnabled(false);
                preference.setSmsEnabled(false);
                break;
        }
        
        // 默认免打扰设置
        preference.setQuietEnabled(false);
        preference.setQuietStartTime("22:00");
        preference.setQuietEndTime("08:00");
        preference.setFrequencyLimit(5); // 5分钟频率限制
        
        return preference;
    }
    
    private NotificationPreferenceDTO convertToDTO(NotificationPreference entity) {
        NotificationPreferenceDTO dto = new NotificationPreferenceDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setNotificationType(entity.getNotificationType());
        dto.setInAppEnabled(entity.getInAppEnabled());
        dto.setEmailEnabled(entity.getEmailEnabled());
        dto.setWechatWorkEnabled(entity.getWechatWorkEnabled());
        dto.setSmsEnabled(entity.getSmsEnabled());
        dto.setQuietEnabled(entity.getQuietEnabled());
        dto.setQuietStartTime(entity.getQuietStartTime());
        dto.setQuietEndTime(entity.getQuietEndTime());
        dto.setFrequencyLimit(entity.getFrequencyLimit());
        return dto;
    }
    
    private NotificationPreference convertToEntity(NotificationPreferenceDTO dto, Long userId) {
        NotificationPreference entity = new NotificationPreference();
        entity.setId(dto.getId());
        entity.setUserId(userId);
        entity.setNotificationType(dto.getNotificationType());
        entity.setInAppEnabled(dto.getInAppEnabled());
        entity.setEmailEnabled(dto.getEmailEnabled());
        entity.setWechatWorkEnabled(dto.getWechatWorkEnabled());
        entity.setSmsEnabled(dto.getSmsEnabled());
        entity.setQuietEnabled(dto.getQuietEnabled());
        entity.setQuietStartTime(dto.getQuietStartTime());
        entity.setQuietEndTime(dto.getQuietEndTime());
        entity.setFrequencyLimit(dto.getFrequencyLimit());
        return entity;
    }
}