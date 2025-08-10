package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 通知偏好Repository
 */
@Mapper
public interface NotificationPreferenceRepository extends BaseMapper<NotificationPreference> {
    
    /**
     * 根据用户ID查询通知偏好
     */
    List<NotificationPreference> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和通知类型查询偏好
     */
    Optional<NotificationPreference> findByUserIdAndType(@Param("userId") Long userId,
                                                        @Param("notificationType") Notification.NotificationType notificationType);
    
    /**
     * 批量插入或更新用户偏好
     */
    int batchInsertOrUpdate(@Param("preferences") List<NotificationPreference> preferences);
    
    /**
     * 重置用户偏好为默认值
     */
    int resetToDefault(@Param("userId") Long userId);
    
    /**
     * 查询启用了特定渠道的用户
     */
    List<Long> findUsersWithChannelEnabled(@Param("notificationType") Notification.NotificationType notificationType,
                                          @Param("channel") String channel);
}