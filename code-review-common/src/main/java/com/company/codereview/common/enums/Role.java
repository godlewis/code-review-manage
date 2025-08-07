package com.company.codereview.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum Role {
    
    DEVELOPER("开发人员", "ROLE_DEVELOPER"),
    TEAM_LEADER("团队负责人", "ROLE_TEAM_LEADER"),
    ARCHITECT("架构师", "ROLE_ARCHITECT");
    
    private final String description;
    private final String authority;
    
    Role(String description, String authority) {
        this.description = description;
        this.authority = authority;
    }
}