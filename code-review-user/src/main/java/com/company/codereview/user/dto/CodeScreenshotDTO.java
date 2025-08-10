package com.company.codereview.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 代码截图DTO
 */
@Data
public class CodeScreenshotDTO {
    
    private Long id;
    
    @NotNull(message = "评审记录ID不能为空")
    private Long reviewRecordId;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255字符")
    private String fileName;
    
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;
    
    private Long fileSize;
    
    private String fileType;
    
    @Size(max = 500, message = "描述长度不能超过500字符")
    private String description;
    
    private Integer sortOrder;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Long createdBy;
    
    private Long updatedBy;
}