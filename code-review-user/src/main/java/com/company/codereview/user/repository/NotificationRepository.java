package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知Repository
 */
@Mapper
public interface NotificationRepository extends BaseMapper<Notification> {
    
    /**
     * 根据接收者ID查询通知列表
     */
    List<Notification> findByRecipientId(@Param("recipientId") Long recipientId, 
                                        @Param("limit") Integer limit, 
                                        @Param("offset") Integer offset);
    
    /**
     * 根据接收者ID和状态查询通知列表
     */
    List<Notification> findByRecipientIdAndStatus(@Param("recipientId") Long recipientId, 
                                                 @Param("status") Notification.NotificationStatus status,
                                                 @Param("limit") Integer limit, 
                                                 @Param("offset") Integer offset);
    
    /**
     * 根据接收者ID和是否已读查询通知列表
     */
    List<Notification> findByRecipientIdAndIsRead(@Param("recipientId") Long recipientId, 
                                                 @Param("isRead") Boolean isRead,
                                                 @Param("limit") Integer limit, 
                                                 @Param("offset") Integer offset);
    
    /**
     * 统计用户未读通知数量
     */
    int countUnreadByRecipientId(@Param("recipientId") Long recipientId);
    
    /**
     * 批量标记通知为已读
     */
    int batchMarkAsRead(@Param("ids") List<Long> ids, 
                       @Param("recipientId") Long recipientId, 
                       @Param("readAt") LocalDateTime readAt);
    
    /**
     * 标记所有通知为已读
     */
    int markAllAsRead(@Param("recipientId") Long recipientId, 
                     @Param("readAt") LocalDateTime readAt);
    
    /**
     * 查询待发送的通知
     */
    List<Notification> findPendingNotifications(@Param("limit") Integer limit);
    
    /**
     * 查询发送失败需要重试的通知
     */
    List<Notification> findFailedNotificationsForRetry(@Param("maxRetryCount") Integer maxRetryCount,
                                                       @Param("retryAfter") LocalDateTime retryAfter,
                                                       @Param("limit") Integer limit);
    
    /**
     * 更新通知发送状态
     */
    int updateNotificationStatus(@Param("id") Long id, 
                                @Param("status") Notification.NotificationStatus status,
                                @Param("sentAt") LocalDateTime sentAt,
                                @Param("errorMessage") String errorMessage,
                                @Param("retryCount") Integer retryCount);
    
    /**
     * 删除过期通知
     */
    int deleteExpiredNotifications(@Param("expiredBefore") LocalDateTime expiredBefore);
    
    /**
     * 根据关联业务查询通知
     */
    List<Notification> findByRelatedIdAndType(@Param("relatedId") Long relatedId, 
                                             @Param("relatedType") String relatedType);
    
    /**
     * 统计通知发送情况
     */
    List<java.util.Map<String, Object>> getNotificationStatistics(@Param("startDate") LocalDateTime startDate,
                                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询用户最近的通知
     */
    List<Notification> findRecentByRecipientId(@Param("recipientId") Long recipientId, 
                                              @Param("hours") Integer hours,
                                              @Param("notificationType") Notification.NotificationType notificationType);
}