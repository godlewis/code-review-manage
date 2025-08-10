package com.company.codereview.user.dto;

import com.company.codereview.user.entity.FixRecord;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 整改记录DTO
 */
@Data
public class FixRecordDTO {
    
    private Long id;
    
    @NotNull(message = "问题ID不能为空")
    private Long issueId;
    
    @NotNull(message = "整改人ID不能为空")
    private Long fixerId;
    
    @NotBlank(message = "整改描述不能为空")
    @Size(max = 2000, message = "整改描述长度不能超过2000字符")
    private String fixDescription;
    
    @Size(max = 500, message = "整改前代码链接长度不能超过500字符")
    private String beforeCodeUrl;
    
    @Size(max = 500, message = "整改后代码链接长度不能超过500字符")
    private String afterCodeUrl;
    
    private FixRecord.FixStatus status;
    
    private Long verifierId;
    
    private FixRecord.VerificationResult verificationResult;
    
    @Size(max = 1000, message = "验证备注长度不能超过1000字符")
    private String verificationRemarks;
    
    private LocalDateTime verifiedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    // 关联信息
    private String fixerName;
    
    private String verifierName;
    
    private String issueTitle;
    
    private String issueDescription;
}