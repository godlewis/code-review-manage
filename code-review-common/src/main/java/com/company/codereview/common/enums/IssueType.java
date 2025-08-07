package com.company.codereview.common.enums;

import lombok.Getter;

/**
 * 问题类型枚举
 */
@Getter
public enum IssueType {
    
    FUNCTIONAL_DEFECT("功能缺陷"),
    PERFORMANCE_ISSUE("性能问题"),
    SECURITY_VULNERABILITY("安全漏洞"),
    CODE_STANDARD("代码规范"),
    DESIGN_ISSUE("设计问题");
    
    private final String description;
    
    IssueType(String description) {
        this.description = description;
    }
}