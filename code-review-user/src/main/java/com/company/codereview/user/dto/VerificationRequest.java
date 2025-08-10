package com.company.codereview.user.dto;

import com.company.codereview.user.entity.FixRecord;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 验证请求DTO
 */
@Data
public class VerificationRequest {
    
    /**
     * 验证人ID
     */
    @NotNull(message = "验证人ID不能为空")
    private Long verifierId;
    
    /**
     * 验证结果
     */
    @NotNull(message = "验证结果不能为空")
    private FixRecord.VerificationResult result;
    
    /**
     * 验证备注
     */
    private String remarks;
}