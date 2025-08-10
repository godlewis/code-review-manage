package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.NotificationPreferenceDTO;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 通知偏好设置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
@Tag(name = "通知偏好设置", description = "通知偏好设置相关接口")
public class NotificationPreferenceController {
    
    private final NotificationPreferenceService preferenceService;
    
    /**
     * 获取用户通知偏好设置
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户通知偏好设置", description = "获取指定用户的所有通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<NotificationPreferenceDTO>> getUserPreferences(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        
        log.info("获取用户 {} 的通知偏好设置", userId);
        
        try {
            List<NotificationPreferenceDTO> preferences = preferenceService.getUserPreferences(userId);
            return ResponseResult.success(preferences);
        } catch (Exception e) {
            log.error("获取用户通知偏好设置失败", e);
            return ResponseResult.error("获取通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户通知偏好设置
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户通知偏好设置", description = "获取当前登录用户的所有通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<NotificationPreferenceDTO>> getCurrentUserPreferences() {
        Long currentUserId = getCurrentUserId();
        log.info("获取当前用户 {} 的通知偏好设置", currentUserId);
        
        try {
            List<NotificationPreferenceDTO> preferences = preferenceService.getUserPreferences(currentUserId);
            return ResponseResult.success(preferences);
        } catch (Exception e) {
            log.error("获取当前用户通知偏好设置失败", e);
            return ResponseResult.error("获取通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户特定类型的通知偏好
     */
    @GetMapping("/user/{userId}/type/{notificationType}")
    @Operation(summary = "获取用户特定类型的通知偏好", description = "获取用户指定类型的通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<NotificationPreferenceDTO> getUserPreference(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "通知类型") @PathVariable Notification.NotificationType notificationType) {
        
        log.info("获取用户 {} 的通知偏好设置: {}", userId, notificationType);
        
        try {
            NotificationPreferenceDTO preference = preferenceService.getUserPreference(userId, notificationType);
            return ResponseResult.success(preference);
        } catch (Exception e) {
            log.error("获取用户通知偏好设置失败", e);
            return ResponseResult.error("获取通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新用户通知偏好设置
     */
    @PutMapping("/user/{userId}")
    @Operation(summary = "更新用户通知偏好设置", description = "批量更新用户的通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> updateUserPreferences(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody List<NotificationPreferenceDTO> preferences) {
        
        log.info("更新用户 {} 的通知偏好设置", userId);
        
        try {
            preferenceService.updateUserPreferences(userId, preferences);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("更新用户通知偏好设置失败", e);
            return ResponseResult.error("更新通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新当前用户通知偏好设置
     */
    @PutMapping("/current")
    @Operation(summary = "更新当前用户通知偏好设置", description = "批量更新当前用户的通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> updateCurrentUserPreferences(
            @Valid @RequestBody List<NotificationPreferenceDTO> preferences) {
        
        Long currentUserId = getCurrentUserId();
        log.info("更新当前用户 {} 的通知偏好设置", currentUserId);
        
        try {
            preferenceService.updateUserPreferences(currentUserId, preferences);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("更新当前用户通知偏好设置失败", e);
            return ResponseResult.error("更新通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新单个通知偏好设置
     */
    @PutMapping("/user/{userId}/preference")
    @Operation(summary = "更新单个通知偏好设置", description = "更新用户的单个通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> updateUserPreference(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody NotificationPreferenceDTO preference) {
        
        log.info("更新用户 {} 的通知偏好设置: {}", userId, preference.getNotificationType());
        
        try {
            preferenceService.updateUserPreference(userId, preference);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("更新用户通知偏好设置失败", e);
            return ResponseResult.error("更新通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新当前用户单个通知偏好设置
     */
    @PutMapping("/current/preference")
    @Operation(summary = "更新当前用户单个通知偏好设置", description = "更新当前用户的单个通知偏好设置")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> updateCurrentUserPreference(
            @Valid @RequestBody NotificationPreferenceDTO preference) {
        
        Long currentUserId = getCurrentUserId();
        log.info("更新当前用户 {} 的通知偏好设置: {}", currentUserId, preference.getNotificationType());
        
        try {
            preferenceService.updateUserPreference(currentUserId, preference);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("更新当前用户通知偏好设置失败", e);
            return ResponseResult.error("更新通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 重置用户通知偏好为默认值
     */
    @PostMapping("/user/{userId}/reset")
    @Operation(summary = "重置用户通知偏好为默认值", description = "将用户的通知偏好设置重置为系统默认值")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> resetToDefault(@Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("重置用户 {} 的通知偏好设置为默认值", userId);
        
        try {
            preferenceService.resetToDefault(userId);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("重置用户通知偏好设置失败", e);
            return ResponseResult.error("重置通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 重置当前用户通知偏好为默认值
     */
    @PostMapping("/current/reset")
    @Operation(summary = "重置当前用户通知偏好为默认值", description = "将当前用户的通知偏好设置重置为系统默认值")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> resetCurrentUserToDefault() {
        Long currentUserId = getCurrentUserId();
        log.info("重置当前用户 {} 的通知偏好设置为默认值", currentUserId);
        
        try {
            preferenceService.resetToDefault(currentUserId);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("重置当前用户通知偏好设置失败", e);
            return ResponseResult.error("重置通知偏好设置失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否启用了特定渠道的通知
     */
    @GetMapping("/user/{userId}/channel-enabled")
    @Operation(summary = "检查用户是否启用了特定渠道的通知", description = "检查用户是否启用了指定类型和渠道的通知")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Boolean> isChannelEnabled(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "通知类型") @RequestParam Notification.NotificationType notificationType,
            @Parameter(description = "通知渠道") @RequestParam String channel) {
        
        log.info("检查用户 {} 是否启用了通知渠道: type={}, channel={}", userId, notificationType, channel);
        
        try {
            boolean enabled = preferenceService.isChannelEnabled(userId, notificationType, channel);
            return ResponseResult.success(enabled);
        } catch (Exception e) {
            log.error("检查用户通知渠道状态失败", e);
            return ResponseResult.error("检查通知渠道状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取启用了特定渠道的用户列表
     */
    @GetMapping("/users-with-channel-enabled")
    @Operation(summary = "获取启用了特定渠道的用户列表", description = "获取启用了指定类型和渠道通知的用户ID列表")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Long>> getUsersWithChannelEnabled(
            @Parameter(description = "通知类型") @RequestParam Notification.NotificationType notificationType,
            @Parameter(description = "通知渠道") @RequestParam String channel) {
        
        log.info("获取启用了通知渠道的用户列表: type={}, channel={}", notificationType, channel);
        
        try {
            List<Long> userIds = preferenceService.getUsersWithChannelEnabled(notificationType, channel);
            return ResponseResult.success(userIds);
        } catch (Exception e) {
            log.error("获取启用了通知渠道的用户列表失败", e);
            return ResponseResult.error("获取用户列表失败：" + e.getMessage());
        }
    }
    
    // 私有辅助方法
    
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        // SecurityContext context = SecurityContextHolder.getContext();
        // Authentication authentication = context.getAuthentication();
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // return userDetails.getUserId();
        return 1L; // 临时返回固定值
    }
}