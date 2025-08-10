package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teams")
public class Team extends BaseEntity {
    
    /**
     * 团队名称
     */
    @TableField("name")
    private String name;
    
    /**
     * 团队描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 团队负责人ID
     */
    @TableField("leader_id")
    private Long leaderId;
    
    /**
     * 团队成员数量
     */
    @TableField("member_count")
    private Integer memberCount = 0;
    
    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean active = true;
}