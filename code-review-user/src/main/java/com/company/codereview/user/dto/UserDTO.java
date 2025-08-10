package com.company.codereview.user.dto;

import com.company.codereview.common.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO {
    
    private Long id;
    private String username;
    private String email;
    private String realName;
    private Role role;
    private Long teamId;
    private String teamName;
    private List<String> skills;
    private String avatarUrl;
    private Boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}