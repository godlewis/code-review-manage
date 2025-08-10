package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.UserDTO;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.security.CustomUserDetailsService;
import com.company.codereview.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<ResponseResult<UserDTO>> getProfile() {
        try {
            User currentUser = getCurrentUser();
            UserDTO userDTO = userService.convertToDTO(currentUser);
            return ResponseEntity.ok(ResponseResult.success(userDTO));
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的信息")
    public ResponseEntity<ResponseResult<UserDTO>> updateProfile(@Valid @RequestBody UserDTO userDTO) {
        try {
            User currentUser = getCurrentUser();
            
            // 只允许更新部分字段
            currentUser.setRealName(userDTO.getRealName());
            currentUser.setEmail(userDTO.getEmail());
            currentUser.setAvatarUrl(userDTO.getAvatarUrl());
            
            // 处理技能标签
            if (userDTO.getSkills() != null) {
                // 将技能列表转换为JSON字符串存储
                // 这里简化处理，实际项目中应该使用ObjectMapper
                currentUser.setSkills(String.join(",", userDTO.getSkills()));
            }
            
            User updatedUser = userService.updateUser(currentUser);
            UserDTO responseDTO = userService.convertToDTO(updatedUser);
            
            return ResponseEntity.ok(ResponseResult.success(responseDTO));
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取用户信息（团队负责人和架构师权限）
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "根据ID获取用户信息", description = "根据用户ID获取用户详细信息")
    public ResponseEntity<ResponseResult<UserDTO>> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查权限：团队负责人只能查看本团队成员
            User currentUser = getCurrentUser();
            if (currentUser.getRole().name().equals("TEAM_LEADER")) {
                if (!currentUser.getTeamId().equals(user.getTeamId())) {
                    return ResponseEntity.ok(ResponseResult.error(403, "无权限查看该用户信息"));
                }
            }
            
            UserDTO userDTO = userService.convertToDTO(user);
            return ResponseEntity.ok(ResponseResult.success(userDTO));
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取团队成员列表（团队负责人和架构师权限）
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取团队成员列表", description = "根据团队ID获取团队成员列表")
    public ResponseEntity<ResponseResult<List<UserDTO>>> getTeamMembers(@PathVariable Long teamId) {
        try {
            // 检查权限：团队负责人只能查看本团队成员
            User currentUser = getCurrentUser();
            if (currentUser.getRole().name().equals("TEAM_LEADER")) {
                if (!currentUser.getTeamId().equals(teamId)) {
                    return ResponseEntity.ok(ResponseResult.error(403, "无权限查看该团队成员"));
                }
            }
            
            List<User> teamMembers = userService.findByTeamId(teamId);
            List<UserDTO> memberDTOs = userService.convertToDTOs(teamMembers);
            
            return ResponseEntity.ok(ResponseResult.success(memberDTOs));
        } catch (Exception e) {
            log.error("获取团队成员失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 创建用户（架构师权限）
     */
    @PostMapping
    @PreAuthorize("hasRole('ARCHITECT')")
    @Operation(summary = "创建用户", description = "创建新用户（仅架构师权限）")
    public ResponseEntity<ResponseResult<UserDTO>> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            UserDTO userDTO = userService.convertToDTO(createdUser);
            
            return ResponseEntity.ok(ResponseResult.success(userDTO));
        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 删除用户（架构师权限）
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ARCHITECT')")
    @Operation(summary = "删除用户", description = "删除用户（仅架构师权限）")
    public ResponseEntity<ResponseResult<Void>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(ResponseResult.success("用户删除成功", null));
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser();
    }
}