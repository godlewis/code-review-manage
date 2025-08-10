package com.company.codereview.user.dto;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationRule;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知规则DTO
 */
@Data
public class NotificationRuleDTO {
    
    /**
     * 规则ID
     */
    private Long id;
    
    /**
     * 规则名称
     */
    private String ruleName;
    
    /**
     * 通知类型
     */
    private Notification.NotificationType notificationType;
    
    /**
     * 触发条件
     */
    private Map<String, Object> triggerConditions;
    
    /**
     * 目标用户类型
     */
    private NotificationRule.TargetUserType targetUserType;
    
    /**
     * 目标用户ID列表
     */
    private List<Long> targetUserIds;
    
    /**
     * 延迟发送时间（分钟）
     */
    private Integer delayMinutes;
    
    /**
     * 重复发送间隔（分钟）
     */
    private Integer repeatInterval;
    
    /**
     * 最大重复次数
     */
    private Integer maxRepeatCount;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 是否启用
     */
    private Boolean isEnabled;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 生效开始时间
     */
    private String effectiveStartTime;
    
    /**
     * 生效结束时间
     */
    private String effectiveEndTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 规则统计信息
     */
    private RuleStatistics statistics;
    
    /**
     * 规则统计信息内部类
     */
    @Data
    public static class RuleStatistics {
        
        /**
         * 触发次数
         */
        private Long triggerCount;
        
        /**
         * 成功发送次数
         */
        private Long successCount;
        
        /**
         * 失败次数
         */
        private Long failureCount;
        
        /**
         * 最后触发时间
         */
        private LocalDateTime lastTriggerTime;
        
        /**
         * 平均响应时间（毫秒）
         */
        private Double averageResponseTime;
    }
}