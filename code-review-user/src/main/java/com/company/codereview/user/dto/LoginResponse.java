package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private UserDTO user;
    
    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
}