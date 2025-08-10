package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码截图实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("code_screenshots")
public class CodeScreenshot extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的评审记录ID
     */
    @TableField("review_record_id")
    private Long reviewRecordId;
    
    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 文件访问URL
     */
    @TableField("file_url")
    private String fileUrl;
    
    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;
    
    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;
    
    /**
     * 截图描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;
    
    /**
     * 文件存储路径
     */
    @TableField("file_path")
    private String filePath;
}