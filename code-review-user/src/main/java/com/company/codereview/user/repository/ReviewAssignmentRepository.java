package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.ReviewAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 评审分配Repository
 */
@Mapper
public interface ReviewAssignmentRepository extends BaseMapper<ReviewAssignment> {
    
    /**
     * 根据团队ID和周开始日期查询分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE team_id = #{teamId} AND week_start_date = #{weekStartDate} AND deleted = 0")
    List<ReviewAssignment> findByTeamIdAndWeekStartDate(@Param("teamId") Long teamId, 
                                                       @Param("weekStartDate") LocalDate weekStartDate);
    
    /**
     * 查询指定时间范围内的分配记录（用于避重检查）
     */
    @Select("SELECT * FROM review_assignments WHERE team_id = #{teamId} " +
            "AND week_start_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY week_start_date DESC")
    List<ReviewAssignment> findByTeamIdAndDateRange(@Param("teamId") Long teamId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    /**
     * 查询用户在指定时间范围内的分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE " +
            "(reviewer_id = #{userId} OR reviewee_id = #{userId}) " +
            "AND week_start_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY week_start_date DESC")
    List<ReviewAssignment> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    /**
     * 查询两个用户之间的历史分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE team_id = #{teamId} " +
            "AND ((reviewer_id = #{userId1} AND reviewee_id = #{userId2}) " +
            "OR (reviewer_id = #{userId2} AND reviewee_id = #{userId1})) " +
            "AND week_start_date >= #{sinceDate} AND deleted = 0")
    List<ReviewAssignment> findPairAssignmentsSince(@Param("teamId") Long teamId,
                                                   @Param("userId1") Long userId1,
                                                   @Param("userId2") Long userId2,
                                                   @Param("sinceDate") LocalDate sinceDate);
    
    /**
     * 统计用户在指定时间范围内的分配数量
     */
    @Select("SELECT COUNT(*) FROM review_assignments WHERE " +
            "(reviewer_id = #{userId} OR reviewee_id = #{userId}) " +
            "AND week_start_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0")
    int countUserAssignments(@Param("userId") Long userId,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate);
    
    /**
     * 批量插入分配记录
     */
    int insertBatch(@Param("assignments") List<ReviewAssignment> assignments);
    
    /**
     * 查询指定时间范围内的所有分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE " +
            "week_start_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY week_start_date DESC")
    List<ReviewAssignment> findByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * 根据状态查询分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE status = #{status} AND deleted = 0")
    List<ReviewAssignment> findByStatus(@Param("status") String status);
    
    /**
     * 查询团队在指定周的分配数量
     */
    @Select("SELECT COUNT(*) FROM review_assignments WHERE team_id = #{teamId} " +
            "AND week_start_date = #{weekStartDate} AND deleted = 0")
    int countTeamAssignments(@Param("teamId") Long teamId, 
                           @Param("weekStartDate") LocalDate weekStartDate);
    
    /**
     * 查询用户作为评审者的分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE reviewer_id = #{userId} " +
            "AND week_start_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY week_start_date DESC")
    List<ReviewAssignment> findByReviewerId(@Param("userId") Long userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * 查询用户作为被评审者的分配记录
     */
    @Select("SELECT * FROM review_assignments WHERE reviewee_id = #{userId} " +
            "AND week_start_date BETWEEN #{startDate} AND #{endDate} " +
            "AND deleted = 0 ORDER BY week_start_date DESC")
    List<ReviewAssignment> findByRevieweeId(@Param("userId") Long userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * 统计分配给用户的任务数量
     */
    int countAssignedToUser(@Param("userId") Long userId, 
                           @Param("startDate") java.time.LocalDateTime startDate, 
                           @Param("endDate") java.time.LocalDateTime endDate);
}