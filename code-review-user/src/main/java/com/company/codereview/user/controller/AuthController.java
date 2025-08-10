package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.LoginRequest;
import com.company.codereview.user.dto.LoginResponse;
import com.company.codereview.user.dto.UserDTO;
import com.company.codereview.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出相关接口")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT token")
    public ResponseEntity<ResponseResult<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(ResponseResult.success(response));
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(400, e.getMessage()));
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，清除客户端token")
    public ResponseEntity<ResponseResult<Void>> logout(HttpServletRequest request) {
        // JWT是无状态的，登出主要由前端处理（清除本地存储的token）
        // 这里可以记录登出日志或者将token加入黑名单（如果需要的话）
        log.info("用户登出");
        return ResponseEntity.ok(ResponseResult.success("登出成功", null));
    }
    
    /**
     * 验证token
     */
    @GetMapping("/validate")
    @Operation(summary = "验证token", description = "验证JWT token有效性并返回用户信息")
    public ResponseEntity<ResponseResult<UserDTO>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            UserDTO user = authService.validateTokenAndGetUser(token);
            return ResponseEntity.ok(ResponseResult.success(user));
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(401, e.getMessage()));
        }
    }
    
    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新token", description = "使用当前token刷新获取新的token")
    public ResponseEntity<ResponseResult<LoginResponse>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            LoginResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(ResponseResult.success(response));
        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(401, e.getMessage()));
        }
    }
}