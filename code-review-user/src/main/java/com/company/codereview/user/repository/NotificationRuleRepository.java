package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 通知规则Repository
 */
@Mapper
public interface NotificationRuleRepository extends BaseMapper<NotificationRule> {
    
    /**
     * 查找所有启用的规则
     */
    @Select("SELECT * FROM notification_rules WHERE is_enabled = 1 ORDER BY priority DESC, created_at ASC")
    List<NotificationRule> findAllEnabled();
    
    /**
     * 根据通知类型查找启用的规则
     */
    @Select("SELECT * FROM notification_rules WHERE notification_type = #{notificationType} AND is_enabled = 1 ORDER BY priority DESC, created_at ASC")
    List<NotificationRule> findEnabledByType(@Param("notificationType") Notification.NotificationType notificationType);
    
    /**
     * 批量更新规则启用状态
     */
    @Update("<script>" +
            "UPDATE notification_rules SET is_enabled = #{enabled}, updated_at = NOW() " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateEnabled(@Param("ids") List<Long> ids, @Param("enabled") Boolean enabled);
    
    /**
     * 根据优先级范围查找规则
     */
    @Select("SELECT * FROM notification_rules WHERE priority BETWEEN #{minPriority} AND #{maxPriority} AND is_enabled = 1 ORDER BY priority DESC")
    List<NotificationRule> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);
    
    /**
     * 查找在指定时间范围内生效的规则
     */
    @Select("SELECT * FROM notification_rules WHERE is_enabled = 1 " +
            "AND (effective_start_time IS NULL OR effective_start_time <= #{currentTime}) " +
            "AND (effective_end_time IS NULL OR effective_end_time >= #{currentTime}) " +
            "ORDER BY priority DESC, created_at ASC")
    List<NotificationRule> findEffectiveRules(@Param("currentTime") String currentTime);
    
    /**
     * 统计各类型规则数量
     */
    @Select("SELECT notification_type, COUNT(*) as count FROM notification_rules WHERE is_enabled = 1 GROUP BY notification_type")
    List<java.util.Map<String, Object>> countByType();
}