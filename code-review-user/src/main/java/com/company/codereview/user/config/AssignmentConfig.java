package com.company.codereview.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 评审分配配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "review.assignment")
public class AssignmentConfig {
    
    /**
     * 避重周期（周）
     */
    @NotNull
    @Min(1)
    @Max(12)
    private Integer avoidanceWeeks = 4;
    
    /**
     * 每周最大任务数
     */
    @NotNull
    @Min(1)
    @Max(10)
    private Integer maxAssignmentsPerWeek = 3;
    
    /**
     * 技能匹配权重
     */
    @NotNull
    @Min(0)
    @Max(1)
    private Double skillMatchWeight = 0.4;
    
    /**
     * 负载均衡权重
     */
    @NotNull
    @Min(0)
    @Max(1)
    private Double loadBalanceWeight = 0.3;
    
    /**
     * 多样性权重
     */
    @NotNull
    @Min(0)
    @Max(1)
    private Double diversityWeight = 0.3;
    
    /**
     * 是否启用自动分配
     */
    @NotNull
    private Boolean enableAutoAssignment = true;
    
    /**
     * 是否启用新人优先分配经验丰富的评审者
     */
    @NotNull
    private Boolean enableNewUserPriority = true;
    
    /**
     * 新用户定义阈值（月）
     */
    @NotNull
    @Min(1)
    @Max(12)
    private Integer newUserThresholdMonths = 3;
    
    /**
     * 经验用户定义阈值（月）
     */
    @NotNull
    @Min(3)
    @Max(60)
    private Integer experiencedUserThresholdMonths = 6;
    
    /**
     * 团队特殊配置
     * key: teamId, value: 团队特殊配置
     */
    private Map<Long, TeamSpecialConfig> teamSpecialConfigs;
    
    /**
     * 用户特殊配置
     * key: userId, value: 用户特殊配置
     */
    private Map<Long, UserSpecialConfig> userSpecialConfigs;
    
    /**
     * 排除配对列表
     * 某些用户之间不应该互相评审
     */
    private List<ExcludePair> excludePairs;
    
    /**
     * 强制配对列表
     * 某些用户之间应该优先配对
     */
    private List<ForcePair> forcePairs;
    
    /**
     * 团队特殊配置
     */
    @Data
    public static class TeamSpecialConfig {
        /**
         * 团队ID
         */
        private Long teamId;
        
        /**
         * 团队名称
         */
        private String teamName;
        
        /**
         * 是否启用特殊配置
         */
        private Boolean enabled = true;
        
        /**
         * 团队专用避重周期
         */
        private Integer avoidanceWeeks;
        
        /**
         * 团队专用最大任务数
         */
        private Integer maxAssignmentsPerWeek;
        
        /**
         * 团队专用权重配置
         */
        private Double skillMatchWeight;
        private Double loadBalanceWeight;
        private Double diversityWeight;
        
        /**
         * 备注
         */
        private String remarks;
    }
    
    /**
     * 用户特殊配置
     */
    @Data
    public static class UserSpecialConfig {
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 是否启用特殊配置
         */
        private Boolean enabled = true;
        
        /**
         * 用户最大任务数限制
         */
        private Integer maxAssignmentsPerWeek;
        
        /**
         * 是否只能作为评审者
         */
        private Boolean reviewerOnly = false;
        
        /**
         * 是否只能作为被评审者
         */
        private Boolean revieweeOnly = false;
        
        /**
         * 是否暂停分配
         */
        private Boolean pauseAssignment = false;
        
        /**
         * 暂停原因
         */
        private String pauseReason;
        
        /**
         * 备注
         */
        private String remarks;
    }
    
    /**
     * 排除配对
     */
    @Data
    public static class ExcludePair {
        /**
         * 用户1 ID
         */
        private Long userId1;
        
        /**
         * 用户2 ID
         */
        private Long userId2;
        
        /**
         * 排除原因
         */
        private String reason;
        
        /**
         * 是否启用
         */
        private Boolean enabled = true;
        
        /**
         * 生效开始时间
         */
        private String startDate;
        
        /**
         * 生效结束时间
         */
        private String endDate;
    }
    
    /**
     * 强制配对
     */
    @Data
    public static class ForcePair {
        /**
         * 评审者ID
         */
        private Long reviewerId;
        
        /**
         * 被评审者ID
         */
        private Long revieweeId;
        
        /**
         * 强制配对原因
         */
        private String reason;
        
        /**
         * 优先级（数字越大优先级越高）
         */
        private Integer priority = 1;
        
        /**
         * 是否启用
         */
        private Boolean enabled = true;
        
        /**
         * 生效开始时间
         */
        private String startDate;
        
        /**
         * 生效结束时间
         */
        private String endDate;
    }
    
    /**
     * 验证权重配置是否合理
     */
    public boolean isWeightConfigValid() {
        double totalWeight = skillMatchWeight + loadBalanceWeight + diversityWeight;
        return Math.abs(totalWeight - 1.0) < 0.001; // 允许小的浮点误差
    }
    
    /**
     * 获取团队特殊配置
     */
    public TeamSpecialConfig getTeamSpecialConfig(Long teamId) {
        if (teamSpecialConfigs == null) {
            return null;
        }
        return teamSpecialConfigs.get(teamId);
    }
    
    /**
     * 获取用户特殊配置
     */
    public UserSpecialConfig getUserSpecialConfig(Long userId) {
        if (userSpecialConfigs == null) {
            return null;
        }
        return userSpecialConfigs.get(userId);
    }
    
    /**
     * 检查两个用户是否被排除配对
     */
    public boolean isExcludedPair(Long userId1, Long userId2) {
        if (excludePairs == null) {
            return false;
        }
        
        return excludePairs.stream()
            .filter(pair -> pair.getEnabled())
            .anyMatch(pair -> 
                (pair.getUserId1().equals(userId1) && pair.getUserId2().equals(userId2)) ||
                (pair.getUserId1().equals(userId2) && pair.getUserId2().equals(userId1))
            );
    }
    
    /**
     * 获取强制配对列表
     */
    public List<ForcePair> getActiveForcePairs() {
        if (forcePairs == null) {
            return List.of();
        }
        
        return forcePairs.stream()
            .filter(pair -> pair.getEnabled())
            .sorted((a, b) -> b.getPriority().compareTo(a.getPriority()))
            .toList();
    }
    
    /**
     * 检查用户是否暂停分配
     */
    public boolean isUserPaused(Long userId) {
        UserSpecialConfig config = getUserSpecialConfig(userId);
        return config != null && config.getEnabled() && config.getPauseAssignment();
    }
    
    /**
     * 获取用户的最大任务数限制
     */
    public Integer getUserMaxAssignments(Long userId) {
        UserSpecialConfig config = getUserSpecialConfig(userId);
        if (config != null && config.getEnabled() && config.getMaxAssignmentsPerWeek() != null) {
            return config.getMaxAssignmentsPerWeek();
        }
        return maxAssignmentsPerWeek;
    }
}