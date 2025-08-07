package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 团队实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teams")
public class Team extends BaseEntity {
    
    private String name;
    private String description;
    private Long leaderId;
    private Boolean active = true;
}