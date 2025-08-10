package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.ReviewRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审记录Repository
 */
@Mapper
public interface ReviewRecordRepository extends BaseMapper<ReviewRecord> {
    
    /**
     * 根据分配ID查询评审记录
     */
    List<ReviewRecord> findByAssignmentId(@Param("assignmentId") Long assignmentId);
    
    /**
     * 根据评审者ID查询评审记录
     */
    List<ReviewRecord> findByReviewerId(@Param("reviewerId") Long reviewerId, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据被评审者ID查询评审记录
     */
    List<ReviewRecord> findByRevieweeId(@Param("revieweeId") Long revieweeId, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据团队ID查询评审记录
     */
    List<ReviewRecord> findByTeamId(@Param("teamId") Long teamId, 
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据状态查询评审记录
     */
    List<ReviewRecord> findByStatus(@Param("status") ReviewRecord.ReviewStatus status);
    
    /**
     * 查询需要重新评审的记录
     */
    List<ReviewRecord> findNeedsReReview(@Param("teamId") Long teamId);
    
    /**
     * 统计用户的评审记录数量
     */
    int countByReviewerId(@Param("reviewerId") Long reviewerId, 
                         @Param("startDate") LocalDateTime startDate, 
                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计团队的评审记录数量
     */
    int countByTeamId(@Param("teamId") Long teamId, 
                     @Param("startDate") LocalDateTime startDate, 
                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询用户的评审记录（包含关联数据）
     */
    List<ReviewRecord> findByReviewerIdWithDetails(@Param("reviewerId") Long reviewerId, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询评审记录详情（包含截图和问题）
     */
    ReviewRecord findByIdWithDetails(@Param("id") Long id);
    
    /**
     * 批量更新评审记录状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, 
                         @Param("status") ReviewRecord.ReviewStatus status, 
                         @Param("updatedBy") Long updatedBy);
    
    /**
     * 统计已完成的评审数量
     */
    int countCompletedByReviewer(@Param("reviewerId") Long reviewerId, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 计算平均评审分数
     */
    Double calculateAverageScore(@Param("reviewerId") Long reviewerId, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计有评审记录的团队成员数量
     */
    int countMembersWithReviews(@Param("teamId") Long teamId, 
                               @Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * 计算团队平均评审分数
     */
    Double calculateTeamAverageScore(@Param("teamId") Long teamId, 
                                    @Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计全局评审数量
     */
    long countAll(@Param("startDate") LocalDateTime startDate, 
                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 计算全局平均评审分数
     */
    Double calculateGlobalAverageScore(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
}