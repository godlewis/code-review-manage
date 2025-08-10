package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 分配配置实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("assignment_configs")
public class AssignmentConfigEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;
    
    /**
     * 配置值（JSON格式）
     */
    @TableField("config_value")
    private String configValue;
    
    /**
     * 配置描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 配置类型：GLOBAL-全局配置，TEAM-团队配置，USER-用户配置
     */
    @TableField("config_type")
    private String configType;
    
    /**
     * 关联ID（团队ID或用户ID）
     */
    @TableField("related_id")
    private Long relatedId;
    
    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;
    
    /**
     * 创建者ID
     */
    @TableField("created_by")
    private Long createdBy;
    
    /**
     * 更新者ID
     */
    @TableField("updated_by")
    private Long updatedBy;
    
    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;
}