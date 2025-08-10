package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.user.entity.FixRecord;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.repository.FixRecordRepository;
import com.company.codereview.user.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 整改记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixRecordService {
    
    private final FixRecordRepository fixRecordRepository;
    private final IssueRepository issueRepository;
    
    /**
     * 创建整改记录
     */
    @Transactional
    public FixRecord createFixRecord(FixRecord fixRecord) {
        log.info("创建整改记录: issueId={}, fixerId={}", fixRecord.getIssueId(), fixRecord.getFixerId());
        
        // 验证问题是否存在
        Issue issue = issueRepository.selectById(fixRecord.getIssueId());
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        // 检查问题状态
        if (issue.getStatus() == Issue.IssueStatus.CLOSED || 
            issue.getStatus() == Issue.IssueStatus.REJECTED) {
            throw new RuntimeException("已关闭或已拒绝的问题不能提交整改记录");
        }
        
        // 验证必填字段
        validateFixRecord(fixRecord);
        
        // 设置默认状态
        if (fixRecord.getStatus() == null) {
            fixRecord.setStatus(FixRecord.FixStatus.SUBMITTED);
        }
        
        fixRecordRepository.insert(fixRecord);
        
        // 更新问题状态为处理中
        if (issue.getStatus() == Issue.IssueStatus.OPEN) {
            issue.setStatus(Issue.IssueStatus.IN_PROGRESS);
            issueRepository.updateById(issue);
        }
        
        log.info("整改记录创建成功: id={}", fixRecord.getId());
        return fixRecord;
    }
    
    /**
     * 验证整改记录数据
     */
    private void validateFixRecord(FixRecord fixRecord) {
        if (fixRecord.getIssueId() == null) {
            throw new RuntimeException("问题ID不能为空");
        }
        
        if (fixRecord.getFixerId() == null) {
            throw new RuntimeException("整改人ID不能为空");
        }
        
        if (fixRecord.getFixDescription() == null || fixRecord.getFixDescription().trim().isEmpty()) {
            throw new RuntimeException("整改描述不能为空");
        }
    }
    
    /**
     * 更新整改记录
     */
    @Transactional
    public FixRecord updateFixRecord(FixRecord fixRecord) {
        log.info("更新整改记录: id={}", fixRecord.getId());
        
        FixRecord existingRecord = fixRecordRepository.selectById(fixRecord.getId());
        if (existingRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        
        // 检查状态是否允许修改
        if (existingRecord.getStatus() == FixRecord.FixStatus.APPROVED || 
            existingRecord.getStatus() == FixRecord.FixStatus.REJECTED) {
            throw new RuntimeException("已审核的整改记录不能修改");
        }
        
        // 验证状态转换
        validateStatusTransition(existingRecord.getStatus(), fixRecord.getStatus());
        
        fixRecordRepository.updateById(fixRecord);
        
        log.info("整改记录更新成功: id={}", fixRecord.getId());
        return fixRecord;
    }
    
    /**
     * 验证状态转换是否合法
     */
    private void validateStatusTransition(FixRecord.FixStatus currentStatus, FixRecord.FixStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // 状态未变化
        }
        
        switch (currentStatus) {
            case SUBMITTED:
                if (newStatus != FixRecord.FixStatus.UNDER_REVIEW && 
                    newStatus != FixRecord.FixStatus.NEED_REVISION) {
                    throw new RuntimeException("已提交状态只能转换为审核中或需要修改");
                }
                break;
            case UNDER_REVIEW:
                if (newStatus != FixRecord.FixStatus.APPROVED && 
                    newStatus != FixRecord.FixStatus.REJECTED && 
                    newStatus != FixRecord.FixStatus.NEED_REVISION) {
                    throw new RuntimeException("审核中状态只能转换为已通过、已拒绝或需要修改");
                }
                break;
            case NEED_REVISION:
                if (newStatus != FixRecord.FixStatus.SUBMITTED) {
                    throw new RuntimeException("需要修改状态只能转换为已提交");
                }
                break;
            case APPROVED:
            case REJECTED:
                throw new RuntimeException("已审核的整改记录不能修改状态");
            default:
                throw new RuntimeException("未知的整改记录状态");
        }
    }
    
    /**
     * 删除整改记录
     */
    @Transactional
    public void deleteFixRecord(Long id) {
        log.info("删除整改记录: id={}", id);
        
        FixRecord fixRecord = fixRecordRepository.selectById(id);
        if (fixRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        
        // 检查状态是否允许删除
        if (fixRecord.getStatus() == FixRecord.FixStatus.APPROVED) {
            throw new RuntimeException("已通过的整改记录不能删除");
        }
        
        fixRecordRepository.deleteById(id);
        
        log.info("整改记录删除成功: id={}", id);
    }
    
    /**
     * 根据ID查询整改记录
     */
    public FixRecord getFixRecordById(Long id) {
        FixRecord fixRecord = fixRecordRepository.selectById(id);
        if (fixRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        return fixRecord;
    }
    
    /**
     * 根据问题ID查询整改记录列表
     */
    public List<FixRecord> getFixRecordsByIssueId(Long issueId) {
        return fixRecordRepository.findByIssueId(issueId);
    }
    
    /**
     * 根据整改人ID查询整改记录
     */
    public List<FixRecord> getFixRecordsByFixerId(Long fixerId, LocalDateTime startDate, LocalDateTime endDate) {
        return fixRecordRepository.findByFixerId(fixerId, startDate, endDate);
    }
    
    /**
     * 查询待验证的整改记录
     */
    public List<FixRecord> getPendingVerificationRecords(Long verifierId) {
        return fixRecordRepository.findPendingVerification(verifierId);
    }
    
    /**
     * 根据状态查询整改记录
     */
    public List<FixRecord> getFixRecordsByStatus(FixRecord.FixStatus status, Long teamId) {
        return fixRecordRepository.findByStatus(status, teamId);
    }
    
    /**
     * 提交整改记录进行验证
     */
    @Transactional
    public FixRecord submitForVerification(Long id, Long verifierId) {
        log.info("提交整改记录进行验证: id={}, verifierId={}", id, verifierId);
        
        FixRecord fixRecord = fixRecordRepository.selectById(id);
        if (fixRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        
        if (fixRecord.getStatus() != FixRecord.FixStatus.SUBMITTED && 
            fixRecord.getStatus() != FixRecord.FixStatus.NEED_REVISION) {
            throw new RuntimeException("只有已提交或需要修改的整改记录才能提交验证");
        }
        
        fixRecord.setStatus(FixRecord.FixStatus.UNDER_REVIEW);
        fixRecord.setVerifierId(verifierId);
        fixRecordRepository.updateById(fixRecord);
        
        log.info("整改记录提交验证成功: id={}", id);
        return fixRecord;
    }
    
    /**
     * 验证整改记录
     */
    @Transactional
    public FixRecord verifyFixRecord(Long id, FixRecord.VerificationResult result, String remarks, Long verifierId) {
        log.info("验证整改记录: id={}, result={}, verifierId={}", id, result, verifierId);
        
        FixRecord fixRecord = fixRecordRepository.selectById(id);
        if (fixRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        
        if (fixRecord.getStatus() != FixRecord.FixStatus.UNDER_REVIEW) {
            throw new RuntimeException("只有审核中的整改记录才能进行验证");
        }
        
        if (!verifierId.equals(fixRecord.getVerifierId())) {
            throw new RuntimeException("只有指定的验证人才能进行验证");
        }
        
        // 更新验证结果
        fixRecord.setVerificationResult(result);
        fixRecord.setVerificationRemarks(remarks);
        fixRecord.setVerifiedAt(LocalDateTime.now());
        
        // 根据验证结果更新状态
        switch (result) {
            case PASS:
                fixRecord.setStatus(FixRecord.FixStatus.APPROVED);
                // 更新问题状态为已解决
                updateIssueStatusAfterVerification(fixRecord.getIssueId(), Issue.IssueStatus.RESOLVED);
                break;
            case FAIL:
                fixRecord.setStatus(FixRecord.FixStatus.REJECTED);
                // 问题状态保持处理中
                break;
            case NEED_FURTHER_FIX:
                fixRecord.setStatus(FixRecord.FixStatus.NEED_REVISION);
                // 问题状态保持处理中
                break;
        }
        
        fixRecordRepository.updateById(fixRecord);
        
        log.info("整改记录验证完成: id={}, result={}", id, result);
        return fixRecord;
    }
    
    /**
     * 验证后更新问题状态
     */
    private void updateIssueStatusAfterVerification(Long issueId, Issue.IssueStatus status) {
        Issue issue = issueRepository.selectById(issueId);
        if (issue != null) {
            issue.setStatus(status);
            issueRepository.updateById(issue);
            log.info("问题状态已更新: issueId={}, status={}", issueId, status);
        }
    }
    
    /**
     * 批量更新整改记录状态
     */
    @Transactional
    public void batchUpdateFixRecordStatus(List<Long> ids, FixRecord.FixStatus status, Long updatedBy) {
        log.info("批量更新整改记录状态: ids={}, status={}", ids, status);
        
        int updatedCount = fixRecordRepository.batchUpdateStatus(ids, status, updatedBy);
        
        log.info("批量更新完成: 更新了{}条整改记录", updatedCount);
    }
    
    /**
     * 获取最新的整改记录
     */
    public FixRecord getLatestFixRecord(Long issueId) {
        return fixRecordRepository.findLatestByIssueId(issueId);
    }
    
    /**
     * 统计用户的整改记录数量
     */
    public int countFixRecordsByUser(Long fixerId, LocalDateTime startDate, LocalDateTime endDate) {
        return fixRecordRepository.countByFixerId(fixerId, startDate, endDate);
    }
    
    /**
     * 计算整改及时率
     */
    public Double calculateFixTimeliness(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return fixRecordRepository.calculateFixTimeliness(userId, startDate, endDate);
    }
    
    /**
     * 重新提交整改记录
     */
    @Transactional
    public FixRecord resubmitFixRecord(Long id, String newDescription, String newAfterCodeUrl) {
        log.info("重新提交整改记录: id={}", id);
        
        FixRecord fixRecord = fixRecordRepository.selectById(id);
        if (fixRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        
        if (fixRecord.getStatus() != FixRecord.FixStatus.NEED_REVISION && 
            fixRecord.getStatus() != FixRecord.FixStatus.REJECTED) {
            throw new RuntimeException("只有需要修改或已拒绝的整改记录才能重新提交");
        }
        
        // 更新整改内容
        if (newDescription != null && !newDescription.trim().isEmpty()) {
            fixRecord.setFixDescription(newDescription);
        }
        
        if (newAfterCodeUrl != null && !newAfterCodeUrl.trim().isEmpty()) {
            fixRecord.setAfterCodeUrl(newAfterCodeUrl);
        }
        
        // 重置验证相关字段
        fixRecord.setStatus(FixRecord.FixStatus.SUBMITTED);
        fixRecord.setVerificationResult(null);
        fixRecord.setVerificationRemarks(null);
        fixRecord.setVerifiedAt(null);
        
        fixRecordRepository.updateById(fixRecord);
        
        log.info("整改记录重新提交成功: id={}", id);
        return fixRecord;
    }
    
    /**
     * 获取整改记录统计信息
     */
    public FixRecordStatistics getFixRecordStatistics(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        QueryWrapper<FixRecord> queryWrapper = new QueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge("created_at", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("created_at", endDate);
        }
        
        List<FixRecord> fixRecords = fixRecordRepository.selectList(queryWrapper);
        
        FixRecordStatistics stats = new FixRecordStatistics();
        stats.setTotalRecords(fixRecords.size());
        stats.setSubmittedRecords((int) fixRecords.stream().filter(r -> r.getStatus() == FixRecord.FixStatus.SUBMITTED).count());
        stats.setUnderReviewRecords((int) fixRecords.stream().filter(r -> r.getStatus() == FixRecord.FixStatus.UNDER_REVIEW).count());
        stats.setApprovedRecords((int) fixRecords.stream().filter(r -> r.getStatus() == FixRecord.FixStatus.APPROVED).count());
        stats.setRejectedRecords((int) fixRecords.stream().filter(r -> r.getStatus() == FixRecord.FixStatus.REJECTED).count());
        stats.setNeedRevisionRecords((int) fixRecords.stream().filter(r -> r.getStatus() == FixRecord.FixStatus.NEED_REVISION).count());
        
        // 计算通过率
        int totalVerified = stats.getApprovedRecords() + stats.getRejectedRecords();
        stats.setApprovalRate(totalVerified > 0 ? (double) stats.getApprovedRecords() / totalVerified * 100 : 0.0);
        
        return stats;
    }
    
    /**
     * 整改记录统计信息类
     */
    public static class FixRecordStatistics {
        private int totalRecords;
        private int submittedRecords;
        private int underReviewRecords;
        private int approvedRecords;
        private int rejectedRecords;
        private int needRevisionRecords;
        private double approvalRate;
        
        // Getters and Setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public int getSubmittedRecords() { return submittedRecords; }
        public void setSubmittedRecords(int submittedRecords) { this.submittedRecords = submittedRecords; }
        
        public int getUnderReviewRecords() { return underReviewRecords; }
        public void setUnderReviewRecords(int underReviewRecords) { this.underReviewRecords = underReviewRecords; }
        
        public int getApprovedRecords() { return approvedRecords; }
        public void setApprovedRecords(int approvedRecords) { this.approvedRecords = approvedRecords; }
        
        public int getRejectedRecords() { return rejectedRecords; }
        public void setRejectedRecords(int rejectedRecords) { this.rejectedRecords = rejectedRecords; }
        
        public int getNeedRevisionRecords() { return needRevisionRecords; }
        public void setNeedRevisionRecords(int needRevisionRecords) { this.needRevisionRecords = needRevisionRecords; }
        
        public double getApprovalRate() { return approvalRate; }
        public void setApprovalRate(double approvalRate) { this.approvalRate = approvalRate; }
    }
}