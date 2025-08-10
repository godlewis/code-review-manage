package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 评审记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review_records")
public class ReviewRecord extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的评审分配ID
     */
    @TableField("assignment_id")
    private Long assignmentId;
    
    /**
     * 评审标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 代码仓库地址
     */
    @TableField("code_repository")
    private String codeRepository;
    
    /**
     * 代码文件路径
     */
    @TableField("code_file_path")
    private String codeFilePath;
    
    /**
     * 评审描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 总体评分 (1-10分)
     */
    @TableField("overall_score")
    private Integer overallScore;
    
    /**
     * 评审总结
     */
    @TableField("summary")
    private String summary;
    
    /**
     * 评审状态
     */
    @TableField("status")
    private ReviewStatus status;
    
    /**
     * 是否需要重新评审
     */
    @TableField("needs_re_review")
    private Boolean needsReReview;
    
    /**
     * 评审完成时间
     */
    @TableField("completed_at")
    private java.time.LocalDateTime completedAt;
    
    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;
    
    /**
     * 代码截图列表（不存储在数据库中，通过关联查询获取）
     */
    @TableField(exist = false)
    private List<CodeScreenshot> screenshots;
    
    /**
     * 问题列表（不存储在数据库中，通过关联查询获取）
     */
    @TableField(exist = false)
    private List<Issue> issues;
    
    /**
     * 评审状态枚举
     */
    public enum ReviewStatus {
        DRAFT("草稿"),
        SUBMITTED("已提交"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String description;
        
        ReviewStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}