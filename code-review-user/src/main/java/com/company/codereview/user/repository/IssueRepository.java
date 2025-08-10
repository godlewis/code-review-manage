package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问题Repository
 */
@Mapper
public interface IssueRepository extends BaseMapper<Issue> {
    
    /**
     * 根据评审记录ID查询问题列表
     */
    List<Issue> findByReviewRecordId(@Param("reviewRecordId") Long reviewRecordId);
    
    /**
     * 根据用户ID查询分配给该用户的问题
     */
    List<Issue> findAssignedToUser(@Param("userId") Long userId, 
                                  @Param("status") Issue.IssueStatus status);
    
    /**
     * 根据团队ID查询团队问题
     */
    List<Issue> findByTeamId(@Param("teamId") Long teamId, 
                            @Param("startDate") LocalDateTime startDate, 
                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据问题类型和严重级别查询
     */
    List<Issue> findByTypeAndSeverity(@Param("issueType") IssueType issueType, 
                                     @Param("severity") Severity severity, 
                                     @Param("teamId") Long teamId);
    
    /**
     * 查询团队问题统计
     */
    List<Map<String, Object>> getTeamIssueStatistics(@Param("teamId") Long teamId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询用户问题统计
     */
    List<Map<String, Object>> getUserIssueStatistics(@Param("userId") Long userId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询高频问题
     */
    List<Map<String, Object>> getFrequentIssues(@Param("teamId") Long teamId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate, 
                                               @Param("limit") Integer limit);
    
    /**
     * 统计问题数量按类型分组
     */
    List<Map<String, Object>> countByIssueType(@Param("teamId") Long teamId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计问题数量按严重级别分组
     */
    List<Map<String, Object>> countBySeverity(@Param("teamId") Long teamId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询待整改的问题
     */
    List<Issue> findPendingFix(@Param("userId") Long userId);
    
    /**
     * 批量更新问题状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, 
                         @Param("status") Issue.IssueStatus status, 
                         @Param("updatedBy") Long updatedBy);
    
    /**
     * 查询问题详情（包含整改记录）
     */
    Issue findByIdWithFixRecords(@Param("id") Long id);
    
    /**
     * 统计评审者发现的问题数量
     */
    long countIssuesFoundByReviewer(@Param("reviewerId") Long reviewerId, 
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计待处理问题数量
     */
    long countPendingByAssignee(@Param("assigneeId") Long assigneeId);
    
    /**
     * 统计已解决问题数量
     */
    long countResolvedByAssignee(@Param("assigneeId") Long assigneeId, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取问题类型分布（按评审者）
     */
    List<Map<String, Object>> getIssueTypeDistributionByReviewer(@Param("reviewerId") Long reviewerId, 
                                                                 @Param("startDate") LocalDateTime startDate, 
                                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取严重级别分布（按评审者）
     */
    List<Map<String, Object>> getSeverityDistributionByReviewer(@Param("reviewerId") Long reviewerId, 
                                                               @Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计团队问题数量
     */
    long countByTeamId(@Param("teamId") Long teamId, 
                      @Param("startDate") LocalDateTime startDate, 
                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计团队已解决问题数量
     */
    long countResolvedByTeamId(@Param("teamId") Long teamId, 
                              @Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取团队问题类型分布
     */
    List<Map<String, Object>> getTeamIssueTypeDistribution(@Param("teamId") Long teamId, 
                                                           @Param("startDate") LocalDateTime startDate, 
                                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取团队严重级别分布
     */
    List<Map<String, Object>> getTeamSeverityDistribution(@Param("teamId") Long teamId, 
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计团队严重问题数量
     */
    long countCriticalByTeamId(@Param("teamId") Long teamId, 
                              @Param("startDate") LocalDateTime startDate, 
                              @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计全局问题数量
     */
    long countAll(@Param("startDate") LocalDateTime startDate, 
                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计全局已解决问题数量
     */
    long countAllResolved(@Param("startDate") LocalDateTime startDate, 
                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计全局严重问题数量
     */
    long countAllCritical(@Param("startDate") LocalDateTime startDate, 
                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 获取跨团队问题分布
     */
    List<Map<String, Object>> getCrossTeamIssueDistribution(@Param("startDate") LocalDateTime startDate, 
                                                            @Param("endDate") LocalDateTime endDate);
}