package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.common.enums.Role;
import com.company.codereview.user.dto.UserDTO;
import com.company.codereview.user.entity.Team;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.TeamRepository;
import com.company.codereview.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userRepository.selectById(id))
                .filter(user -> !user.getDeleted());
    }
    
    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole(Role.DEVELOPER);
        }
        if (user.getActive() == null) {
            user.setActive(true);
        }
        
        userRepository.insert(user);
        
        // 更新团队成员数量
        if (user.getTeamId() != null) {
            teamRepository.updateMemberCount(user.getTeamId());
        }
        
        log.info("创建用户成功: {}", user.getUsername());
        return user;
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.selectById(user.getId());
        if (existingUser == null || existingUser.getDeleted()) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果更新了团队，需要更新相关团队的成员数量
        Long oldTeamId = existingUser.getTeamId();
        Long newTeamId = user.getTeamId();
        
        userRepository.updateById(user);
        
        // 更新团队成员数量
        if (oldTeamId != null && !oldTeamId.equals(newTeamId)) {
            teamRepository.updateMemberCount(oldTeamId);
        }
        if (newTeamId != null && !newTeamId.equals(oldTeamId)) {
            teamRepository.updateMemberCount(newTeamId);
        }
        
        log.info("更新用户信息成功: {}", user.getUsername());
        return user;
    }
    
    /**
     * 更新最后登录时间
     */
    @Transactional
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.updateById(user);
    }
    
    /**
     * 根据团队ID查找用户列表
     */
    public List<User> findByTeamId(Long teamId) {
        return userRepository.findByTeamId(teamId);
    }
    
    /**
     * 根据角色查找用户列表
     */
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role.name());
    }
    
    /**
     * 转换为DTO
     */
    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole());
        dto.setTeamId(user.getTeamId());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setActive(user.getActive());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        
        // 获取团队名称
        if (user.getTeamId() != null) {
            Optional<Team> team = Optional.ofNullable(teamRepository.selectById(user.getTeamId()));
            team.ifPresent(t -> dto.setTeamName(t.getName()));
        }
        
        // 解析技能标签
        if (user.getSkills() != null) {
            try {
                List<String> skills = objectMapper.readValue(user.getSkills(), new TypeReference<List<String>>() {});
                dto.setSkills(skills);
            } catch (Exception e) {
                log.warn("解析用户技能标签失败: {}", e.getMessage());
            }
        }
        
        return dto;
    }
    
    /**
     * 批量转换为DTO
     */
    public List<UserDTO> convertToDTOs(List<User> users) {
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除用户（软删除）
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null || user.getDeleted()) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setDeleted(true);
        userRepository.updateById(user);
        
        // 更新团队成员数量
        if (user.getTeamId() != null) {
            teamRepository.updateMemberCount(user.getTeamId());
        }
        
        log.info("删除用户成功: {}", user.getUsername());
    }
    
    /**
     * 验证密码
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}