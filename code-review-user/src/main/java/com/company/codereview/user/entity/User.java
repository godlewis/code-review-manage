package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.codereview.common.entity.BaseEntity;
import com.company.codereview.common.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    
    private String username;
    private String password;
    private String email;
    private String realName;
    private String phone;
    private Role role;
    private Long teamId;
    private Boolean active = true;
}