package com.company.codereview.user.dto;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题DTO
 */
@Data
public class IssueDTO {
    
    private Long id;
    
    @NotNull(message = "评审记录ID不能为空")
    private Long reviewRecordId;
    
    @NotNull(message = "问题类型不能为空")
    private IssueType issueType;
    
    @NotNull(message = "严重级别不能为空")
    private Severity severity;
    
    @NotBlank(message = "问题标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;
    
    @NotBlank(message = "问题描述不能为空")
    @Size(max = 2000, message = "描述长度不能超过2000字符")
    private String description;
    
    @Size(max = 1000, message = "改进建议长度不能超过1000字符")
    private String suggestion;
    
    private String referenceLinks;
    
    private Issue.IssueStatus status;
    
    private Integer lineNumber;
    
    @Size(max = 1000, message = "代码片段长度不能超过1000字符")
    private String codeSnippet;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // 关联数据
    private List<FixRecordDTO> fixRecords;
    
    // 统计信息
    private Integer fixRecordCount;
    
    private String latestFixStatus;
}