package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 整改记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fix_records")
public class FixRecord extends BaseEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的问题ID
     */
    @TableField("issue_id")
    private Long issueId;
    
    /**
     * 整改人ID
     */
    @TableField("fixer_id")
    private Long fixerId;
    
    /**
     * 整改描述
     */
    @TableField("fix_description")
    private String fixDescription;
    
    /**
     * 整改前代码链接
     */
    @TableField("before_code_url")
    private String beforeCodeUrl;
    
    /**
     * 整改后代码链接
     */
    @TableField("after_code_url")
    private String afterCodeUrl;
    
    /**
     * 整改状态
     */
    @TableField("status")
    private FixStatus status;
    
    /**
     * 验证人ID
     */
    @TableField("verifier_id")
    private Long verifierId;
    
    /**
     * 验证结果
     */
    @TableField("verification_result")
    private VerificationResult verificationResult;
    
    /**
     * 验证备注
     */
    @TableField("verification_remarks")
    private String verificationRemarks;
    
    /**
     * 验证时间
     */
    @TableField("verified_at")
    private java.time.LocalDateTime verifiedAt;
    
    /**
     * 整改状态枚举
     */
    public enum FixStatus {
        SUBMITTED("已提交"),
        UNDER_REVIEW("审核中"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        NEED_REVISION("需要修改");
        
        private final String description;
        
        FixStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 验证结果枚举
     */
    public enum VerificationResult {
        PASS("通过"),
        FAIL("不通过"),
        NEED_FURTHER_FIX("需要进一步修改");
        
        private final String description;
        
        VerificationResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}