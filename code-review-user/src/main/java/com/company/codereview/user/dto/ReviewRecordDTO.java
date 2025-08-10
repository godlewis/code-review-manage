package com.company.codereview.user.dto;

import com.company.codereview.user.entity.ReviewRecord;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审记录DTO
 */
@Data
public class ReviewRecordDTO {
    
    private Long id;
    
    @NotNull(message = "分配ID不能为空")
    private Long assignmentId;
    
    @NotBlank(message = "评审标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;
    
    @Size(max = 500, message = "代码仓库地址长度不能超过500字符")
    private String codeRepository;
    
    @Size(max = 1000, message = "代码文件路径长度不能超过1000字符")
    private String codeFilePath;
    
    @Size(max = 2000, message = "评审描述长度不能超过2000字符")
    private String description;
    
    @Min(value = 1, message = "评分不能小于1")
    @Max(value = 10, message = "评分不能大于10")
    private Integer overallScore;
    
    @Size(max = 1000, message = "评审总结长度不能超过1000字符")
    private String summary;
    
    private ReviewRecord.ReviewStatus status;
    
    private Boolean needsReReview;
    
    private LocalDateTime completedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // 关联数据
    private List<CodeScreenshotDTO> screenshots;
    
    private List<IssueDTO> issues;
    
    // 分配信息
    private Long reviewerId;
    
    private Long revieweeId;
    
    private String reviewerName;
    
    private String revieweeName;
    
    private Long teamId;
    
    private String teamName;
}