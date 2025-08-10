package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND is_deleted = false")
    Optional<User> findByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND is_deleted = false")
    Optional<User> findByEmail(@Param("email") String email);
    
    /**
     * 根据团队ID查找用户列表
     */
    @Select("SELECT * FROM users WHERE team_id = #{teamId} AND is_deleted = false AND is_active = true")
    List<User> findByTeamId(@Param("teamId") Long teamId);
    
    /**
     * 根据角色查找用户列表
     */
    @Select("SELECT * FROM users WHERE role = #{role} AND is_deleted = false AND is_active = true")
    List<User> findByRole(@Param("role") String role);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username} AND is_deleted = false")
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email} AND is_deleted = false")
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * 统计团队成员数量
     */
    int countByTeamId(@Param("teamId") Long teamId);
    
    /**
     * 统计活跃团队成员数量
     */
    int countActiveByTeamId(@Param("teamId") Long teamId, 
                           @Param("startDate") java.time.LocalDateTime startDate, 
                           @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 统计总用户数量
     */
    int countAll();
    
    /**
     * 统计活跃用户数量
     */
    int countActive(@Param("startDate") java.time.LocalDateTime startDate, 
                   @Param("endDate") java.time.LocalDateTime endDate);
}