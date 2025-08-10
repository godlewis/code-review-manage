package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知规则实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_rules")
public class NotificationRule extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;
    
    /**
     * 通知类型
     */
    @TableField("notification_type")
    private Notification.NotificationType notificationType;
    
    /**
     * 触发条件（JSON格式）
     */
    @TableField("trigger_conditions")
    private String triggerConditions;
    
    /**
     * 目标用户类型
     */
    @TableField("target_user_type")
    private TargetUserType targetUserType;
    
    /**
     * 目标用户ID列表（JSON格式，当targetUserType为SPECIFIC时使用）
     */
    @TableField("target_user_ids")
    private String targetUserIds;
    
    /**
     * 延迟发送时间（分钟）
     */
    @TableField("delay_minutes")
    private Integer delayMinutes;
    
    /**
     * 重复发送间隔（分钟，0表示不重复）
     */
    @TableField("repeat_interval")
    private Integer repeatInterval;
    
    /**
     * 最大重复次数
     */
    @TableField("max_repeat_count")
    private Integer maxRepeatCount;
    
    /**
     * 优先级（1-10，数字越大优先级越高）
     */
    @TableField("priority")
    private Integer priority;
    
    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;
    
    /**
     * 规则描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 生效开始时间
     */
    @TableField("effective_start_time")
    private String effectiveStartTime;
    
    /**
     * 生效结束时间
     */
    @TableField("effective_end_time")
    private String effectiveEndTime;
    
    /**
     * 目标用户类型枚举
     */
    public enum TargetUserType {
        ALL("所有用户"),
        ROLE_BASED("基于角色"),
        TEAM_BASED("基于团队"),
        SPECIFIC("指定用户"),
        REVIEWER("评审者"),
        REVIEWEE("被评审者"),
        ISSUE_ASSIGNEE("问题负责人");
        
        private final String description;
        
        TargetUserType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}