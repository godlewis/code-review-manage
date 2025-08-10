package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 问题实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("issues")
public class Issue extends BaseEntity {
    
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
     * 问题类型
     */
    @TableField("issue_type")
    private IssueType issueType;
    
    /**
     * 严重级别
     */
    @TableField("severity")
    private Severity severity;
    
    /**
     * 问题标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 问题描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 改进建议
     */
    @TableField("suggestion")
    private String suggestion;
    
    /**
     * 参考链接（JSON格式存储）
     */
    @TableField("reference_links")
    private String referenceLinks;
    
    /**
     * 问题状态
     */
    @TableField("status")
    private IssueStatus status;
    
    /**
     * 代码行号
     */
    @TableField("line_number")
    private Integer lineNumber;
    
    /**
     * 代码片段
     */
    @TableField("code_snippet")
    private String codeSnippet;
    
    /**
     * 分配给的用户ID
     */
    @TableField("assigned_to")
    private Long assignedTo;
    
    /**
     * 整改记录列表（不存储在数据库中，通过关联查询获取）
     */
    @TableField(exist = false)
    private List<FixRecord> fixRecords;
    
    /**
     * 问题状态枚举
     */
    public enum IssueStatus {
        OPEN("待处理"),
        IN_PROGRESS("处理中"),
        RESOLVED("已解决"),
        CLOSED("已关闭"),
        REJECTED("已拒绝");
        
        private final String description;
        
        IssueStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}