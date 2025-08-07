package com.company.codereview.common.enums;

import lombok.Getter;

/**
 * 严重级别枚举
 */
@Getter
public enum Severity {
    
    CRITICAL("严重", 1),
    MAJOR("一般", 2),
    MINOR("轻微", 3),
    SUGGESTION("建议", 4);
    
    private final String description;
    private final Integer level;
    
    Severity(String description, Integer level) {
        this.description = description;
        this.level = level;
    }
}