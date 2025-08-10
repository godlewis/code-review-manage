package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.AssignmentConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分配配置数据访问层
 */
@Mapper
public interface AssignmentConfigRepository extends BaseMapper<AssignmentConfigEntity> {
    
    /**
     * 根据配置键查找配置
     */
    @Select("SELECT * FROM assignment_configs WHERE config_key = #{configKey} AND enabled = 1")
    AssignmentConfigEntity findByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置类型查找配置
     */
    @Select("SELECT * FROM assignment_configs WHERE config_type = #{configType} AND enabled = 1")
    List<AssignmentConfigEntity> findByConfigType(@Param("configType") String configType);
    
    /**
     * 根据配置类型和关联ID查找配置
     */
    @Select("SELECT * FROM assignment_configs WHERE config_type = #{configType} AND related_id = #{relatedId} AND enabled = 1")
    List<AssignmentConfigEntity> findByConfigTypeAndRelatedId(@Param("configType") String configType, @Param("relatedId") Long relatedId);
    
    /**
     * 查找团队特殊配置
     */
    @Select("SELECT * FROM assignment_configs WHERE config_type = 'TEAM' AND related_id = #{teamId} AND enabled = 1")
    List<AssignmentConfigEntity> findTeamConfigs(@Param("teamId") Long teamId);
    
    /**
     * 查找用户特殊配置
     */
    @Select("SELECT * FROM assignment_configs WHERE config_type = 'USER' AND related_id = #{userId} AND enabled = 1")
    List<AssignmentConfigEntity> findUserConfigs(@Param("userId") Long userId);
    
    /**
     * 查找全局配置
     */
    @Select("SELECT * FROM assignment_configs WHERE config_type = 'GLOBAL' AND enabled = 1")
    List<AssignmentConfigEntity> findGlobalConfigs();
}