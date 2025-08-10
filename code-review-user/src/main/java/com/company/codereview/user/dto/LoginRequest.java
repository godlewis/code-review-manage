package com.company.codereview.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求对象
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}