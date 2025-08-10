package com.company.codereview.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.NotificationDTO;
import com.company.codereview.user.dto.NotificationRequest;
import com.company.codereview.user.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import com.company.codereview.user.entity.Notification;

/**
 * 通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 发送通知
     */
    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送通知给指定用户")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("发送通知请求: {}", request);
        
        try {
            notificationService.sendNotification(request);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return ResponseResult.error("发送通知失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户通知列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户通知列表", description = "分页获取指定用户的通知列表")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IPage<NotificationDTO>> getUserNotifications(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "只显示未读") @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        
        log.info("获取用户 {} 的通知列表，页码：{}，大小：{}，只显示未读：{}", userId, page, size, unreadOnly);
        
        try {
            IPage<NotificationDTO> notifications = notificationService.getUserNotifications(userId, page, size, unreadOnly);
            return ResponseResult.success(notifications);
        } catch (Exception e) {
            log.error("获取用户通知列表失败", e);
            return ResponseResult.error("获取通知列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户通知列表
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户通知列表", description = "分页获取当前登录用户的通知列表")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IPage<NotificationDTO>> getCurrentUserNotifications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "只显示未读") @RequestParam(defaultValue = "false") Boolean unreadOnly) {
        
        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = getCurrentUserId();
        log.info("获取当前用户 {} 的通知列表", currentUserId);
        
        try {
            IPage<NotificationDTO> notifications = notificationService.getUserNotifications(currentUserId, page, size, unreadOnly);
            return ResponseResult.success(notifications);
        } catch (Exception e) {
            log.error("获取当前用户通知列表失败", e);
            return ResponseResult.error("获取通知列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/unread-count/{userId}")
    @Operation(summary = "获取用户未读通知数量", description = "获取指定用户的未读通知数量")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Integer> getUnreadCount(@Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("获取用户 {} 的未读通知数量", userId);
        
        try {
            int count = notificationService.getUnreadCount(userId);
            return ResponseResult.success(count);
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return ResponseResult.error("获取未读通知数量失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取当前用户未读通知数量", description = "获取当前登录用户的未读通知数量")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Integer> getCurrentUserUnreadCount() {
        Long currentUserId = getCurrentUserId();
        log.info("获取当前用户 {} 的未读通知数量", currentUserId);
        
        try {
            int count = notificationService.getUnreadCount(currentUserId);
            return ResponseResult.success(count);
        } catch (Exception e) {
            log.error("获取当前用户未读通知数量失败", e);
            return ResponseResult.error("获取未读通知数量失败：" + e.getMessage());
        }
    }
    
    /**
     * 标记通知为已读
     */
    @PutMapping("/mark-read")
    @Operation(summary = "标记通知为已读", description = "批量标记指定通知为已读")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> markAsRead(@RequestBody List<Long> notificationIds) {
        Long currentUserId = getCurrentUserId();
        log.info("用户 {} 标记通知为已读: {}", currentUserId, notificationIds);
        
        try {
            notificationService.markAsRead(currentUserId, notificationIds);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("标记通知为已读失败", e);
            return ResponseResult.error("标记通知为已读失败：" + e.getMessage());
        }
    }
    
    /**
     * 标记所有通知为已读
     */
    @PutMapping("/mark-all-read")
    @Operation(summary = "标记所有通知为已读", description = "标记当前用户的所有通知为已读")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> markAllAsRead() {
        Long currentUserId = getCurrentUserId();
        log.info("用户 {} 标记所有通知为已读", currentUserId);
        
        try {
            notificationService.markAllAsRead(currentUserId);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("标记所有通知为已读失败", e);
            return ResponseResult.error("标记所有通知为已读失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除通知
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除通知", description = "批量删除指定通知")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteNotifications(@RequestBody List<Long> notificationIds) {
        Long currentUserId = getCurrentUserId();
        log.info("用户 {} 删除通知: {}", currentUserId, notificationIds);
        
        try {
            notificationService.deleteNotifications(currentUserId, notificationIds);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return ResponseResult.error("删除通知失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取通知统计信息", description = "获取系统通知发送统计信息")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Map<String, Object>> getNotificationStatistics(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate) {
        
        log.info("获取通知统计信息，时间范围：{} 到 {}", startDate, endDate);
        
        try {
            Map<String, Object> statistics = notificationService.getNotificationStatistics(startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取通知统计信息失败", e);
            return ResponseResult.error("获取通知统计信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 重试失败的通知
     */
    @PostMapping("/retry-failed")
    @Operation(summary = "重试失败的通知", description = "重新发送失败的通知")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> retryFailedNotifications() {
        log.info("重试失败的通知");
        
        try {
            notificationService.retryFailedNotifications();
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("重试失败的通知失败", e);
            return ResponseResult.error("重试失败的通知失败：" + e.getMessage());
        }
    }
    
    /**
     * 清理过期通知
     */
    @PostMapping("/cleanup")
    @Operation(summary = "清理过期通知", description = "清理指定天数之前的通知记录")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> cleanupExpiredNotifications(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") int daysToKeep) {
        
        log.info("清理过期通知，保留天数：{}", daysToKeep);
        
        try {
            notificationService.cleanupExpiredNotifications(daysToKeep);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("清理过期通知失败", e);
            return ResponseResult.error("清理过期通知失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知详情
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "获取通知详情", description = "获取指定通知的详细信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<NotificationDTO> getNotificationDetail(@PathVariable Long notificationId) {
        Long currentUserId = getCurrentUserId();
        log.info("用户 {} 获取通知详情: {}", currentUserId, notificationId);
        
        try {
            NotificationDTO notification = notificationService.getNotificationDetail(notificationId, currentUserId);
            return ResponseResult.success(notification);
        } catch (IllegalArgumentException e) {
            log.warn("获取通知详情失败: {}", e.getMessage());
            return ResponseResult.error("通知不存在或无权限访问");
        } catch (Exception e) {
            log.error("获取通知详情异常", e);
            return ResponseResult.error("获取通知详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量操作通知
     */
    @PostMapping("/batch-operation")
    @Operation(summary = "批量操作通知", description = "批量标记已读、删除或其他操作")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> batchOperateNotifications(
            @RequestBody Map<String, Object> request) {
        
        Long currentUserId = getCurrentUserId();
        String operation = (String) request.get("operation");
        @SuppressWarnings("unchecked")
        List<Long> notificationIds = (List<Long>) request.get("notificationIds");
        
        log.info("用户 {} 批量操作通知: operation={}, ids={}", currentUserId, operation, notificationIds);
        
        try {
            switch (operation) {
                case "markRead":
                    notificationService.markAsRead(currentUserId, notificationIds);
                    break;
                case "delete":
                    notificationService.deleteNotifications(currentUserId, notificationIds);
                    break;
                default:
                    return ResponseResult.error("不支持的操作类型: " + operation);
            }
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("批量操作通知失败", e);
            return ResponseResult.error("批量操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知类型列表
     */
    @GetMapping("/types")
    @Operation(summary = "获取通知类型列表", description = "获取系统支持的所有通知类型")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> getNotificationTypes() {
        try {
            List<Map<String, Object>> types = Arrays.stream(Notification.NotificationType.values())
                    .map(type -> {
                        Map<String, Object> typeInfo = new HashMap<>();
                        typeInfo.put("code", type.name());
                        typeInfo.put("name", type.getDescription());
                        return typeInfo;
                    })
                    .collect(Collectors.toList());
            
            return ResponseResult.success(types);
        } catch (Exception e) {
            log.error("获取通知类型列表失败", e);
            return ResponseResult.error("获取通知类型列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知渠道列表
     */
    @GetMapping("/channels")
    @Operation(summary = "获取通知渠道列表", description = "获取系统支持的所有通知渠道")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> getNotificationChannels() {
        try {
            List<Map<String, Object>> channels = Arrays.asList(
                Map.of("code", "IN_APP", "name", "站内信", "description", "系统内部消息通知"),
                Map.of("code", "EMAIL", "name", "邮件", "description", "电子邮件通知"),
                Map.of("code", "WECHAT_WORK", "name", "企业微信", "description", "企业微信群通知"),
                Map.of("code", "SMS", "name", "短信", "description", "手机短信通知")
            );
            
            return ResponseResult.success(channels);
        } catch (Exception e) {
            log.error("获取通知渠道列表失败", e);
            return ResponseResult.error("获取通知渠道列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试通知发送
     */
    @PostMapping("/test-send")
    @Operation(summary = "测试通知发送", description = "发送测试通知，用于验证通知渠道配置")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> testNotificationSend(@RequestBody Map<String, Object> request) {
        log.info("测试通知发送: {}", request);
        
        try {
            Long recipientId = Long.valueOf(request.get("recipientId").toString());
            String channel = (String) request.get("channel");
            String message = (String) request.getOrDefault("message", "这是一条测试通知");
            
            NotificationRequest testRequest = new NotificationRequest();
            testRequest.setRecipientIds(Arrays.asList(recipientId));
            testRequest.setNotificationType(Notification.NotificationType.SYSTEM_NOTIFICATION);
            testRequest.setTitle("测试通知");
            testRequest.setContent(message);
            testRequest.setChannels(Arrays.asList(channel));
            testRequest.setImmediate(true);
            
            notificationService.sendNotification(testRequest);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("测试通知发送失败", e);
            return ResponseResult.error("测试通知发送失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "获取通知历史记录", description = "获取系统通知发送历史记录")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IPage<NotificationDTO>> getNotificationHistory(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "通知类型") @RequestParam(required = false) String notificationType,
            @Parameter(description = "通知状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        
        log.info("获取通知历史记录: page={}, size={}, type={}, status={}", page, size, notificationType, status);
        
        try {
            IPage<NotificationDTO> history = notificationService.getNotificationHistory(
                page, size, notificationType, status, startDate, endDate);
            return ResponseResult.success(history);
        } catch (Exception e) {
            log.error("获取通知历史记录失败", e);
            return ResponseResult.error("获取通知历史记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户通知摘要
     */
    @GetMapping("/summary/{userId}")
    @Operation(summary = "获取用户通知摘要", description = "获取用户的通知统计摘要信息")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Map<String, Object>> getUserNotificationSummary(@PathVariable Long userId) {
        log.info("获取用户 {} 的通知摘要", userId);
        
        try {
            Map<String, Object> summary = notificationService.getUserNotificationSummary(userId);
            return ResponseResult.success(summary);
        } catch (Exception e) {
            log.error("获取用户通知摘要失败", e);
            return ResponseResult.error("获取用户通知摘要失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户通知摘要
     */
    @GetMapping("/current/summary")
    @Operation(summary = "获取当前用户通知摘要", description = "获取当前登录用户的通知统计摘要信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Map<String, Object>> getCurrentUserNotificationSummary() {
        Long currentUserId = getCurrentUserId();
        log.info("获取当前用户 {} 的通知摘要", currentUserId);
        
        try {
            Map<String, Object> summary = notificationService.getUserNotificationSummary(currentUserId);
            return ResponseResult.success(summary);
        } catch (Exception e) {
            log.error("获取当前用户通知摘要失败", e);
            return ResponseResult.error("获取用户通知摘要失败：" + e.getMessage());
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