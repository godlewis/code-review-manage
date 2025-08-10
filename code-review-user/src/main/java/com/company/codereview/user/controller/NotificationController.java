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
import java.util.List;
import java.util.Map;

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
            // TODO: 实现通知统计逻辑
            Map<String, Object> statistics = Map.of(
                "totalSent", 1000,
                "totalFailed", 50,
                "successRate", 0.95,
                "channelDistribution", Map.of(
                    "IN_APP", 600,
                    "EMAIL", 300,
                    "WECHAT_WORK", 80,
                    "SMS", 20
                )
            );
            
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