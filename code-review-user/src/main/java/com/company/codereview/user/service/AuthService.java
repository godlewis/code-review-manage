package com.company.codereview.user.service;

import com.company.codereview.user.dto.LoginRequest;
import com.company.codereview.user.dto.LoginResponse;
import com.company.codereview.user.dto.UserDTO;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.security.JwtUtil;
import com.company.codereview.user.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // 获取用户详情
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();
            
            // 生成JWT token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("teamId", user.getTeamId());
            
            String token = jwtUtil.generateToken(userPrincipal, extraClaims);
            
            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());
            
            // 转换为DTO
            UserDTO userDTO = userService.convertToDTO(user);
            
            log.info("用户登录成功: {}", loginRequest.getUsername());
            return new LoginResponse(token, userDTO);
            
        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {} - {}", loginRequest.getUsername(), e.getMessage());
            throw new BadCredentialsException("用户名或密码错误");
        }
    }
    
    /**
     * 验证token并获取用户信息
     */
    public UserDTO validateTokenAndGetUser(String token) {
        try {
            if (!jwtUtil.validateTokenFormat(token)) {
                throw new RuntimeException("无效的token格式");
            }
            
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            return userService.convertToDTO(user);
            
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new RuntimeException("Token验证失败", e);
        }
    }
    
    /**
     * 刷新token
     */
    public LoginResponse refreshToken(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 生成新的token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole().name());
            extraClaims.put("teamId", user.getTeamId());
            
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                new CustomUserDetailsService.CustomUserPrincipal(user);
            String newToken = jwtUtil.generateToken(userPrincipal, extraClaims);
            
            UserDTO userDTO = userService.convertToDTO(user);
            
            log.info("Token刷新成功: {}", username);
            return new LoginResponse(newToken, userDTO);
            
        } catch (Exception e) {
            log.warn("Token刷新失败: {}", e.getMessage());
            throw new RuntimeException("Token刷新失败", e);
        }
    }
}