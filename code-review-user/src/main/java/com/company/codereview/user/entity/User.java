package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.codereview.common.entity.BaseEntity;
import com.company.codereview.common.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    
    /**
     * 用户名
     */
    @TableField("username")
    private String username;
    
    /**
     * 密码
     */
    @TableField("password")
    private String password;
    
    /**
     * 邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;
    
    /**
     * 用户角色
     */
    @TableField("role")
    private Role role;
    
    /**
     * 所属团队ID
     */
    @TableField("team_id")
    private Long teamId;
    
    /**
     * 技能标签（JSON格式存储）
     */
    @TableField("skills")
    private String skills;
    
    /**
     * 头像URL
     */
    @TableField("avatar_url")
    private String avatarUrl;
    
    /**
     * 是否激活
     */
    @TableField("is_active")
    private Boolean active = true;
    
    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private java.time.LocalDateTime lastLoginAt;
}