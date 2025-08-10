package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.dto.AISummaryRequest;
import com.company.codereview.user.entity.AISummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Summary Repository
 */
@Mapper
public interface AISummaryRepository extends BaseMapper<AISummary> {
    
    /**
     * Find summaries by team ID
     */
    List<AISummary> findByTeamId(@Param("teamId") Long teamId, 
                                @Param("status") AISummary.SummaryStatus status);
    
    /**
     * Find summaries by date range
     */
    List<AISummary> findByDateRange(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate,
                                   @Param("teamId") Long teamId);
    
    /**
     * Find summaries by type
     */
    List<AISummary> findBySummaryType(@Param("summaryType") AISummaryRequest.SummaryType summaryType,
                                     @Param("teamId") Long teamId,
                                     @Param("limit") Integer limit);
    
    /**
     * Find published summaries
     */
    List<AISummary> findPublishedSummaries(@Param("teamId") Long teamId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Find latest summary by type
     */
    AISummary findLatestByType(@Param("summaryType") AISummaryRequest.SummaryType summaryType,
                              @Param("teamId") Long teamId);
    
    /**
     * Find summaries for comparison
     */
    List<AISummary> findForComparison(@Param("teamId") Long teamId,
                                     @Param("summaryType") AISummaryRequest.SummaryType summaryType,
                                     @Param("limit") Integer limit);
    
    /**
     * Count summaries by status
     */
    Long countByStatus(@Param("status") AISummary.SummaryStatus status,
                      @Param("teamId") Long teamId);
    
    /**
     * Find summaries by creator
     */
    List<AISummary> findByCreator(@Param("createdBy") Long createdBy,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Update summary status
     */
    int updateStatus(@Param("id") Long id, 
                    @Param("status") AISummary.SummaryStatus status,
                    @Param("updatedBy") Long updatedBy);
    
    /**
     * Publish summary
     */
    int publishSummary(@Param("id") Long id,
                      @Param("publishedBy") Long publishedBy,
                      @Param("publishedAt") LocalDateTime publishedAt);
    
    /**
     * Archive old summaries
     */
    int archiveOldSummaries(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * Find summary statistics
     */
    List<java.util.Map<String, Object>> getSummaryStatistics(@Param("teamId") Long teamId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
}