package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

/**
 * 团队数据访问接口
 */
@Mapper
public interface TeamRepository extends BaseMapper<Team> {
    
    /**
     * 根据团队名称查找团队
     */
    @Select("SELECT * FROM teams WHERE name = #{name} AND is_deleted = false")
    Optional<Team> findByName(@Param("name") String name);
    
    /**
     * 根据负责人ID查找团队
     */
    @Select("SELECT * FROM teams WHERE leader_id = #{leaderId} AND is_deleted = false")
    Optional<Team> findByLeaderId(@Param("leaderId") Long leaderId);
    
    /**
     * 查找所有激活的团队
     */
    @Select("SELECT * FROM teams WHERE is_active = true AND is_deleted = false ORDER BY name")
    List<Team> findAllActive();
    
    /**
     * 更新团队成员数量
     */
    @Update("UPDATE teams SET member_count = (SELECT COUNT(*) FROM users WHERE team_id = #{teamId} AND is_deleted = false AND is_active = true) WHERE id = #{teamId}")
    void updateMemberCount(@Param("teamId") Long teamId);
    
    /**
     * 检查团队名称是否存在
     */
    @Select("SELECT COUNT(*) FROM teams WHERE name = #{name} AND is_deleted = false")
    boolean existsByName(@Param("name") String name);
    
    /**
     * 统计总团队数量
     */
    int countAll();
    
    /**
     * 获取团队表现数据
     */
    List<java.util.Map<String, Object>> getTeamPerformanceData(@Param("startDate") java.time.LocalDateTime startDate, 
                                                               @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 获取评审覆盖率最高的团队
     */
    java.util.Map<String, Object> getTeamWithHighestCoverage(@Param("startDate") java.time.LocalDateTime startDate, 
                                                             @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 获取问题解决率最高的团队
     */
    java.util.Map<String, Object> getTeamWithHighestResolutionRate(@Param("startDate") java.time.LocalDateTime startDate, 
                                                                   @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 获取代码质量最高的团队
     */
    java.util.Map<String, Object> getTeamWithHighestQuality(@Param("startDate") java.time.LocalDateTime startDate, 
                                                            @Param("endDate") java.time.LocalDateTime endDate);
}