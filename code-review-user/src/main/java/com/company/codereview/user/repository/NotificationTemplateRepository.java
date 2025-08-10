package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 通知模板Repository
 */
@Mapper
public interface NotificationTemplateRepository extends BaseMapper<NotificationTemplate> {
    
    /**
     * 根据通知类型和渠道查询模板
     */
    Optional<NotificationTemplate> findByTypeAndChannel(@Param("notificationType") Notification.NotificationType notificationType,
                                                       @Param("channel") NotificationTemplate.NotificationChannel channel);
    
    /**
     * 根据通知类型查询所有启用的模板
     */
    List<NotificationTemplate> findEnabledByType(@Param("notificationType") Notification.NotificationType notificationType);
    
    /**
     * 查询所有启用的模板
     */
    List<NotificationTemplate> findAllEnabled();
    
    /**
     * 根据渠道查询模板
     */
    List<NotificationTemplate> findByChannel(@Param("channel") NotificationTemplate.NotificationChannel channel);
    
    /**
     * 批量启用/禁用模板
     */
    int batchUpdateEnabled(@Param("ids") List<Long> ids, 
                          @Param("enabled") Boolean enabled);
}