package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.entity.ReviewRecord;
import com.company.codereview.user.repository.IssueRepository;
import com.company.codereview.user.repository.ReviewRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问题服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {
    
    private final IssueRepository issueRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    
    /**
     * 创建问题
     */
    @Transactional
    public Issue createIssue(Issue issue) {
        log.info("创建问题: reviewRecordId={}, title={}, type={}, severity={}", 
                issue.getReviewRecordId(), issue.getTitle(), issue.getIssueType(), issue.getSeverity());
        
        // 验证评审记录是否存在
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(issue.getReviewRecordId());
        if (reviewRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        
        // 检查评审记录状态
        if (reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED) {
            throw new RuntimeException("已完成的评审记录不能添加问题");
        }
        
        // 设置默认状态
        if (issue.getStatus() == null) {
            issue.setStatus(Issue.IssueStatus.OPEN);
        }
        
        // 验证必填字段
        validateIssue(issue);
        
        issueRepository.insert(issue);
        
        log.info("问题创建成功: id={}", issue.getId());
        return issue;
    }
    
    /**
     * 验证问题数据
     */
    private void validateIssue(Issue issue) {
        if (issue.getTitle() == null || issue.getTitle().trim().isEmpty()) {
            throw new RuntimeException("问题标题不能为空");
        }
        
        if (issue.getIssueType() == null) {
            throw new RuntimeException("问题类型不能为空");
        }
        
        if (issue.getSeverity() == null) {
            throw new RuntimeException("严重级别不能为空");
        }
        
        if (issue.getDescription() == null || issue.getDescription().trim().isEmpty()) {
            throw new RuntimeException("问题描述不能为空");
        }
    }
    
    /**
     * 更新问题
     */
    @Transactional
    public Issue updateIssue(Issue issue) {
        log.info("更新问题: id={}", issue.getId());
        
        Issue existingIssue = issueRepository.selectById(issue.getId());
        if (existingIssue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        // 验证评审记录状态
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(existingIssue.getReviewRecordId());
        if (reviewRecord != null && reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED) {
            // 已完成的评审记录只能更新问题状态
            if (!existingIssue.getTitle().equals(issue.getTitle()) ||
                !existingIssue.getDescription().equals(issue.getDescription()) ||
                !existingIssue.getIssueType().equals(issue.getIssueType()) ||
                !existingIssue.getSeverity().equals(issue.getSeverity())) {
                throw new RuntimeException("已完成的评审记录只能更新问题状态");
            }
        }
        
        // 验证状态转换
        validateStatusTransition(existingIssue.getStatus(), issue.getStatus());
        
        issueRepository.updateById(issue);
        
        log.info("问题更新成功: id={}", issue.getId());
        return issue;
    }
    
    /**
     * 验证状态转换是否合法
     */
    private void validateStatusTransition(Issue.IssueStatus currentStatus, Issue.IssueStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // 状态未变化
        }
        
        switch (currentStatus) {
            case OPEN:
                if (newStatus != Issue.IssueStatus.IN_PROGRESS && 
                    newStatus != Issue.IssueStatus.CLOSED && 
                    newStatus != Issue.IssueStatus.REJECTED) {
                    throw new RuntimeException("待处理状态只能转换为处理中、已关闭或已拒绝");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != Issue.IssueStatus.RESOLVED && 
                    newStatus != Issue.IssueStatus.OPEN && 
                    newStatus != Issue.IssueStatus.CLOSED) {
                    throw new RuntimeException("处理中状态只能转换为已解决、待处理或已关闭");
                }
                break;
            case RESOLVED:
                if (newStatus != Issue.IssueStatus.CLOSED && 
                    newStatus != Issue.IssueStatus.IN_PROGRESS) {
                    throw new RuntimeException("已解决状态只能转换为已关闭或处理中");
                }
                break;
            case CLOSED:
                if (newStatus != Issue.IssueStatus.OPEN) {
                    throw new RuntimeException("已关闭状态只能转换为待处理（重新打开）");
                }
                break;
            case REJECTED:
                if (newStatus != Issue.IssueStatus.OPEN) {
                    throw new RuntimeException("已拒绝状态只能转换为待处理（重新打开）");
                }
                break;
            default:
                throw new RuntimeException("未知的问题状态");
        }
    }
    
    /**
     * 删除问题
     */
    @Transactional
    public void deleteIssue(Long id) {
        log.info("删除问题: id={}", id);
        
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        // 验证评审记录状态
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(issue.getReviewRecordId());
        if (reviewRecord != null && reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED) {
            throw new RuntimeException("已完成的评审记录不能删除问题");
        }
        
        // 检查问题状态
        if (issue.getStatus() == Issue.IssueStatus.IN_PROGRESS || 
            issue.getStatus() == Issue.IssueStatus.RESOLVED) {
            throw new RuntimeException("处理中或已解决的问题不能删除");
        }
        
        issueRepository.deleteById(id);
        
        log.info("问题删除成功: id={}", id);
    }
    
    /**
     * 根据ID查询问题
     */
    public Issue getIssueById(Long id) {
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        return issue;
    }
    
    /**
     * 根据ID查询问题详情（包含整改记录）
     */
    public Issue getIssueWithFixRecords(Long id) {
        return issueRepository.findByIdWithFixRecords(id);
    }
    
    /**
     * 根据评审记录ID查询问题列表
     */
    public List<Issue> getIssuesByReviewRecordId(Long reviewRecordId) {
        return issueRepository.findByReviewRecordId(reviewRecordId);
    }
    
    /**
     * 查询分配给用户的问题
     */
    public List<Issue> getIssuesAssignedToUser(Long userId, Issue.IssueStatus status) {
        return issueRepository.findAssignedToUser(userId, status);
    }
    
    /**
     * 根据团队ID查询问题
     */
    public List<Issue> getIssuesByTeamId(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return issueRepository.findByTeamId(teamId, startDate, endDate);
    }
    
    /**
     * 根据类型和严重级别查询问题
     */
    public List<Issue> getIssuesByTypeAndSeverity(IssueType issueType, Severity severity, Long teamId) {
        return issueRepository.findByTypeAndSeverity(issueType, severity, teamId);
    }
    
    /**
     * 查询待整改的问题
     */
    public List<Issue> getPendingFixIssues(Long userId) {
        return issueRepository.findPendingFix(userId);
    }
    
    /**
     * 更新问题状态
     */
    @Transactional
    public Issue updateIssueStatus(Long id, Issue.IssueStatus status) {
        log.info("更新问题状态: id={}, status={}", id, status);
        
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        // 验证状态转换
        validateStatusTransition(issue.getStatus(), status);
        
        issue.setStatus(status);
        issueRepository.updateById(issue);
        
        log.info("问题状态更新成功: id={}, status={}", id, status);
        return issue;
    }
    
    /**
     * 批量更新问题状态
     */
    @Transactional
    public void batchUpdateIssueStatus(List<Long> ids, Issue.IssueStatus status, Long updatedBy) {
        log.info("批量更新问题状态: ids={}, status={}", ids, status);
        
        int updatedCount = issueRepository.batchUpdateStatus(ids, status, updatedBy);
        
        log.info("批量更新完成: 更新了{}条问题", updatedCount);
    }
    
    /**
     * 关闭问题
     */
    @Transactional
    public Issue closeIssue(Long id, String reason) {
        log.info("关闭问题: id={}, reason={}", id, reason);
        
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        if (issue.getStatus() == Issue.IssueStatus.CLOSED) {
            throw new RuntimeException("问题已经关闭");
        }
        
        issue.setStatus(Issue.IssueStatus.CLOSED);
        if (reason != null && !reason.trim().isEmpty()) {
            issue.setDescription(issue.getDescription() + "\n\n关闭原因: " + reason);
        }
        
        issueRepository.updateById(issue);
        
        log.info("问题关闭成功: id={}", id);
        return issue;
    }
    
    /**
     * 重新打开问题
     */
    @Transactional
    public Issue reopenIssue(Long id, String reason) {
        log.info("重新打开问题: id={}, reason={}", id, reason);
        
        Issue issue = issueRepository.selectById(id);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        if (issue.getStatus() != Issue.IssueStatus.CLOSED && 
            issue.getStatus() != Issue.IssueStatus.REJECTED) {
            throw new RuntimeException("只有已关闭或已拒绝的问题才能重新打开");
        }
        
        issue.setStatus(Issue.IssueStatus.OPEN);
        if (reason != null && !reason.trim().isEmpty()) {
            issue.setDescription(issue.getDescription() + "\n\n重新打开原因: " + reason);
        }
        
        issueRepository.updateById(issue);
        
        log.info("问题重新打开成功: id={}", id);
        return issue;
    }
    
    /**
     * 获取团队问题统计
     */
    public List<Map<String, Object>> getTeamIssueStatistics(Long teamId, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate) {
        return issueRepository.getTeamIssueStatistics(teamId, startDate, endDate);
    }
    
    /**
     * 获取用户问题统计
     */
    public List<Map<String, Object>> getUserIssueStatistics(Long userId, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate) {
        return issueRepository.getUserIssueStatistics(userId, startDate, endDate);
    }
    
    /**
     * 获取高频问题
     */
    public List<Map<String, Object>> getFrequentIssues(Long teamId, 
                                                      LocalDateTime startDate, 
                                                      LocalDateTime endDate, 
                                                      Integer limit) {
        return issueRepository.getFrequentIssues(teamId, startDate, endDate, limit);
    }
    
    /**
     * 统计问题数量按类型分组
     */
    public List<Map<String, Object>> countIssuesByType(Long teamId, 
                                                      LocalDateTime startDate, 
                                                      LocalDateTime endDate) {
        return issueRepository.countByIssueType(teamId, startDate, endDate);
    }
    
    /**
     * 统计问题数量按严重级别分组
     */
    public List<Map<String, Object>> countIssuesBySeverity(Long teamId, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate) {
        return issueRepository.countBySeverity(teamId, startDate, endDate);
    }
}