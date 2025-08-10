package com.company.codereview.user.dto;

import com.company.codereview.common.enums.IssueType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 问题模式DTO
 */
@Data
@Builder
public class IssuePattern {
    
    /**
     * 模式ID
     */
    private String patternId;
    
    /**
     * 模式名称
     */
    private String patternName;
    
    /**
     * 模式描述
     */
    private String description;
    
    /**
     * 匹配的问题类型
     */
    private List<IssueType> matchedTypes;
    
    /**
     * 模式出现频率
     */
    private Long frequency;
    
    /**
     * 模式置信度（0-1）
     */
    private Double confidence;
    
    /**
     * 相关关键词
     */
    private List<String> keywords;
    
    /**
     * 典型示例
     */
    private List<String> examples;
    
    /**
     * 建议的预防措施
     */
    private String preventionSuggestion;
    
    /**
     * 影响的代码区域
     */
    private List<String> affectedAreas;
}