package com.company.codereview.user.dto;

import com.company.codereview.user.entity.ReviewAssignment;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审分配DTO
 */
@Data
public class ReviewAssignmentDTO {
    
    private Long id;
    
    @NotNull(message = "团队ID不能为空")
    private Long teamId;
    
    @NotNull(message = "评审者ID不能为空")
    private Long reviewerId;
    
    @NotNull(message = "被评审者ID不能为空")
    private Long revieweeId;
    
    @NotNull(message = "周开始日期不能为空")
    private LocalDate weekStartDate;
    
    private ReviewAssignment.AssignmentStatus status;
    
    private Double skillMatchScore;
    
    private Double loadBalanceScore;
    
    private Double diversityScore;
    
    private Double totalScore;
    
    private Boolean isManualAdjusted;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // 关联信息
    private String reviewerName;
    
    private String revieweeName;
    
    private String teamName;
    
    // 评审记录
    private List<ReviewRecordDTO> reviewRecords;
    
    // 统计信息
    private Integer reviewRecordCount;
    
    private Integer completedReviewCount;
    
    private Integer issueCount;
    
    private Double averageScore;
}