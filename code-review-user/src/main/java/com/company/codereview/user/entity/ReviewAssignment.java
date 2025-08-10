package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 评审分配实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_assignments")
public class ReviewAssignment extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 团队ID
     */
    @TableField("team_id")
    private Long teamId;
    
    /**
     * 评审者ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;
    
    /**
     * 被评审者ID
     */
    @TableField("reviewee_id")
    private Long revieweeId;
    
    /**
     * 周开始日期
     */
    @TableField("week_start_date")
    private LocalDate weekStartDate;
    
    /**
     * 分配状态
     */
    @TableField("status")
    private AssignmentStatus status;
    
    /**
     * 技能匹配度分数
     */
    @TableField("skill_match_score")
    private Double skillMatchScore;
    
    /**
     * 负载均衡分数
     */
    @TableField("load_balance_score")
    private Double loadBalanceScore;
    
    /**
     * 多样性分数
     */
    @TableField("diversity_score")
    private Double diversityScore;
    
    /**
     * 总分数
     */
    @TableField("total_score")
    private Double totalScore;
    
    /**
     * 是否手动调整
     */
    @TableField("is_manual_adjusted")
    private Boolean isManualAdjusted;
    
    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;
    
    /**
     * 分配状态枚举
     */
    public enum AssignmentStatus {
        PENDING("待分配"),
        ASSIGNED("已分配"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String description;
        
        AssignmentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}