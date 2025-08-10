package com.company.codereview.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.codereview.user.entity.FixRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 整改记录Repository
 */
@Mapper
public interface FixRecordRepository extends BaseMapper<FixRecord> {
    
    /**
     * 根据问题ID查询整改记录
     */
    List<FixRecord> findByIssueId(@Param("issueId") Long issueId);
    
    /**
     * 根据整改人ID查询整改记录
     */
    List<FixRecord> findByFixerId(@Param("fixerId") Long fixerId, 
                                 @Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * 根据验证人ID查询待验证的整改记录
     */
    List<FixRecord> findPendingVerification(@Param("verifierId") Long verifierId);
    
    /**
     * 根据状态查询整改记录
     */
    List<FixRecord> findByStatus(@Param("status") FixRecord.FixStatus status, 
                                @Param("teamId") Long teamId);
    
    /**
     * 统计用户的整改记录数量
     */
    int countByFixerId(@Param("fixerId") Long fixerId, 
                      @Param("startDate") LocalDateTime startDate, 
                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计整改及时率
     */
    Double calculateFixTimeliness(@Param("userId") Long userId, 
                                 @Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询最新的整改记录
     */
    FixRecord findLatestByIssueId(@Param("issueId") Long issueId);
    
    /**
     * 批量更新整改记录状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, 
                         @Param("status") FixRecord.FixStatus status, 
                         @Param("updatedBy") Long updatedBy);
    
    /**
     * 计算团队整改及时率
     */
    Double calculateTeamFixTimeliness(@Param("teamId") Long teamId, 
                                     @Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
}