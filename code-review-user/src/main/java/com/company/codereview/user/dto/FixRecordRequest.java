package com.company.codereview.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 整改记录请求DTO
 */
@Data
public class FixRecordRequest {
    
    /**
     * 整改人ID
     */
    @NotNull(message = "整改人ID不能为空")
    private Long fixerId;
    
    /**
     * 整改描述
     */
    @NotBlank(message = "整改描述不能为空")
    private String fixDescription;
    
    /**
     * 整改前代码链接
     */
    private String beforeCodeUrl;
    
    /**
     * 整改后代码链接
     */
    private String afterCodeUrl;
    
    /**
     * 备注
     */
    private String remarks;
}