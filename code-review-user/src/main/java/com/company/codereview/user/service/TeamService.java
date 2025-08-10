package com.company.codereview.user.service;

import com.company.codereview.user.dto.TeamDTO;
import com.company.codereview.user.dto.UserDTO;
import com.company.codereview.user.entity.Team;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.TeamRepository;
import com.company.codereview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 团队服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    /**
     * 根据ID查找团队
     */
    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(teamRepository.selectById(id))
                .filter(team -> !team.getDeleted());
    }
    
    /**
     * 根据名称查找团队
     */
    public Optional<Team> findByName(String name) {
        return teamRepository.findByName(name);
    }
    
    /**
     * 创建团队
     */
    @Transactional
    public Team createTeam(Team team) {
        // 检查团队名称是否已存在
        if (teamRepository.existsByName(team.getName())) {
            throw new RuntimeException("团队名称已存在");
        }
        
        // 设置默认值
        if (team.getActive() == null) {
            team.setActive(true);
        }
        if (team.getMemberCount() == null) {
            team.setMemberCount(0);
        }
        
        teamRepository.insert(team);
        log.info("创建团队成功: {}", team.getName());
        return team;
    }
    
    /**
     * 更新团队信息
     */
    @Transactional
    public Team updateTeam(Team team) {
        Team existingTeam = teamRepository.selectById(team.getId());
        if (existingTeam == null || existingTeam.getDeleted()) {
            throw new RuntimeException("团队不存在");
        }
        
        teamRepository.updateById(team);
        log.info("更新团队信息成功: {}", team.getName());
        return team;
    }
    
    /**
     * 获取所有激活的团队
     */
    public List<Team> findAllActive() {
        return teamRepository.findAllActive();
    }
    
    /**
     * 获取团队成员列表
     */
    public List<User> getTeamMembers(Long teamId) {
        return userRepository.findByTeamId(teamId);
    }
    
    /**
     * 添加团队成员
     */
    @Transactional
    public void addTeamMember(Long teamId, Long userId) {
        Team team = teamRepository.selectById(teamId);
        if (team == null || team.getDeleted() || !team.getActive()) {
            throw new RuntimeException("团队不存在或已禁用");
        }
        
        User user = userRepository.selectById(userId);
        if (user == null || user.getDeleted() || !user.getActive()) {
            throw new RuntimeException("用户不存在或已禁用");
        }
        
        // 更新用户的团队ID
        user.setTeamId(teamId);
        userRepository.updateById(user);
        
        // 更新团队成员数量
        teamRepository.updateMemberCount(teamId);
        
        log.info("添加团队成员成功: 用户{} 加入团队{}", user.getUsername(), team.getName());
    }
    
    /**
     * 移除团队成员
     */
    @Transactional
    public void removeTeamMember(Long teamId, Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null || user.getDeleted()) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!teamId.equals(user.getTeamId())) {
            throw new RuntimeException("用户不属于该团队");
        }
        
        // 清除用户的团队ID
        user.setTeamId(null);
        userRepository.updateById(user);
        
        // 更新团队成员数量
        teamRepository.updateMemberCount(teamId);
        
        log.info("移除团队成员成功: 用户{} 离开团队{}", user.getUsername(), teamId);
    }
    
    /**
     * 转换为DTO
     */
    public TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setLeaderId(team.getLeaderId());
        dto.setMemberCount(team.getMemberCount());
        dto.setActive(team.getActive());
        dto.setCreatedAt(team.getCreatedAt());
        
        // 获取负责人姓名
        if (team.getLeaderId() != null) {
            Optional<User> leader = Optional.ofNullable(userRepository.selectById(team.getLeaderId()));
            leader.ifPresent(l -> dto.setLeaderName(l.getRealName()));
        }
        
        return dto;
    }
    
    /**
     * 转换为详细DTO（包含成员列表）
     */
    public TeamDTO convertToDetailDTO(Team team) {
        TeamDTO dto = convertToDTO(team);
        
        // 获取团队成员列表
        List<User> members = getTeamMembers(team.getId());
        List<UserDTO> memberDTOs = userService.convertToDTOs(members);
        dto.setMembers(memberDTOs);
        
        return dto;
    }
    
    /**
     * 批量转换为DTO
     */
    public List<TeamDTO> convertToDTOs(List<Team> teams) {
        return teams.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 删除团队（软删除）
     */
    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.selectById(teamId);
        if (team == null || team.getDeleted()) {
            throw new RuntimeException("团队不存在");
        }
        
        // 检查是否还有成员
        List<User> members = getTeamMembers(teamId);
        if (!members.isEmpty()) {
            throw new RuntimeException("团队还有成员，无法删除");
        }
        
        team.setDeleted(true);
        teamRepository.updateById(team);
        
        log.info("删除团队成功: {}", team.getName());
    }
}