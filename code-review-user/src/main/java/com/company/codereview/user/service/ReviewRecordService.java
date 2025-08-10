package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.user.entity.*;
import com.company.codereview.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 评审记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRecordService {
    
    private final ReviewRecordRepository reviewRecordRepository;
    private final CodeScreenshotRepository screenshotRepository;
    private final IssueRepository issueRepository;
    private final FixRecordRepository fixRecordRepository;
    private final ReviewAssignmentRepository assignmentRepository;
    
    /**
     * 创建评审记录
     */
    @Transactional
    public ReviewRecord createReviewRecord(ReviewRecord reviewRecord) {
        log.info("创建评审记录: assignmentId={}, title={}", 
                reviewRecord.getAssignmentId(), reviewRecord.getTitle());
        
        // 验证分配记录是否存在
        ReviewAssignment assignment = assignmentRepository.selectById(reviewRecord.getAssignmentId());
        if (assignment == null) {
            throw new RuntimeException("评审分配记录不存在");
        }
        
        // 检查是否已经存在评审记录
        QueryWrapper<ReviewRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("assignment_id", reviewRecord.getAssignmentId());
        ReviewRecord existingRecord = reviewRecordRepository.selectOne(queryWrapper);
        if (existingRecord != null) {
            throw new RuntimeException("该分配已存在评审记录");
        }
        
        // 设置默认值
        if (reviewRecord.getStatus() == null) {
            reviewRecord.setStatus(ReviewRecord.ReviewStatus.DRAFT);
        }
        if (reviewRecord.getNeedsReReview() == null) {
            reviewRecord.setNeedsReReview(false);
        }
        
        // 保存评审记录
        reviewRecordRepository.insert(reviewRecord);
        
        log.info("评审记录创建成功: id={}", reviewRecord.getId());
        return reviewRecord;
    }
    
    /**
     * 更新评审记录
     */
    @Transactional
    public ReviewRecord updateReviewRecord(ReviewRecord reviewRecord) {
        log.info("更新评审记录: id={}", reviewRecord.getId());
        
        ReviewRecord existingRecord = reviewRecordRepository.selectById(reviewRecord.getId());
        if (existingRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        
        // 检查状态转换是否合法
        validateStatusTransition(existingRecord.getStatus(), reviewRecord.getStatus());
        
        // 如果状态变为已完成，设置完成时间
        if (reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED && 
            existingRecord.getStatus() != ReviewRecord.ReviewStatus.COMPLETED) {
            reviewRecord.setCompletedAt(LocalDateTime.now());
            
            // 更新分配记录状态
            ReviewAssignment assignment = assignmentRepository.selectById(existingRecord.getAssignmentId());
            if (assignment != null) {
                assignment.setStatus(ReviewAssignment.AssignmentStatus.COMPLETED);
                assignmentRepository.updateById(assignment);
            }
        }
        
        reviewRecordRepository.updateById(reviewRecord);
        
        log.info("评审记录更新成功: id={}", reviewRecord.getId());
        return reviewRecord;
    }
    
    /**
     * 验证状态转换是否合法
     */
    private void validateStatusTransition(ReviewRecord.ReviewStatus currentStatus, 
                                        ReviewRecord.ReviewStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // 状态未变化
        }
        
        switch (currentStatus) {
            case DRAFT:
                if (newStatus != ReviewRecord.ReviewStatus.SUBMITTED && 
                    newStatus != ReviewRecord.ReviewStatus.CANCELLED) {
                    throw new RuntimeException("草稿状态只能转换为已提交或已取消");
                }
                break;
            case SUBMITTED:
                if (newStatus != ReviewRecord.ReviewStatus.IN_PROGRESS && 
                    newStatus != ReviewRecord.ReviewStatus.CANCELLED) {
                    throw new RuntimeException("已提交状态只能转换为进行中或已取消");
                }
                break;
            case IN_PROGRESS:
                if (newStatus != ReviewRecord.ReviewStatus.COMPLETED && 
                    newStatus != ReviewRecord.ReviewStatus.CANCELLED) {
                    throw new RuntimeException("进行中状态只能转换为已完成或已取消");
                }
                break;
            case COMPLETED:
                if (newStatus != ReviewRecord.ReviewStatus.IN_PROGRESS) {
                    throw new RuntimeException("已完成状态只能转换为进行中（重新评审）");
                }
                break;
            case CANCELLED:
                throw new RuntimeException("已取消的评审记录不能再次修改状态");
            default:
                throw new RuntimeException("未知的评审状态");
        }
    }
    
    /**
     * 删除评审记录
     */
    @Transactional
    public void deleteReviewRecord(Long id) {
        log.info("删除评审记录: id={}", id);
        
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(id);
        if (reviewRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        
        // 检查是否可以删除
        if (reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED) {
            throw new RuntimeException("已完成的评审记录不能删除");
        }
        
        // 删除关联的截图
        screenshotRepository.deleteByReviewRecordId(id);
        
        // 删除关联的问题和整改记录
        List<Issue> issues = issueRepository.findByReviewRecordId(id);
        for (Issue issue : issues) {
            // 删除整改记录
            QueryWrapper<FixRecord> fixRecordQuery = new QueryWrapper<>();
            fixRecordQuery.eq("issue_id", issue.getId());
            fixRecordRepository.delete(fixRecordQuery);
        }
        
        // 删除问题
        QueryWrapper<Issue> issueQuery = new QueryWrapper<>();
        issueQuery.eq("review_record_id", id);
        issueRepository.delete(issueQuery);
        
        // 删除评审记录
        reviewRecordRepository.deleteById(id);
        
        log.info("评审记录删除成功: id={}", id);
    }
    
    /**
     * 根据ID查询评审记录
     */
    public ReviewRecord getReviewRecordById(Long id) {
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(id);
        if (reviewRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        return reviewRecord;
    }
    
    /**
     * 根据ID查询评审记录详情（包含截图和问题）
     */
    public ReviewRecord getReviewRecordWithDetails(Long id) {
        ReviewRecord reviewRecord = reviewRecordRepository.findByIdWithDetails(id);
        if (reviewRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        
        // 查询截图
        List<CodeScreenshot> screenshots = screenshotRepository.findByReviewRecordId(id);
        reviewRecord.setScreenshots(screenshots);
        
        // 查询问题
        List<Issue> issues = issueRepository.findByReviewRecordId(id);
        reviewRecord.setIssues(issues);
        
        return reviewRecord;
    }
    
    /**
     * 根据分配ID查询评审记录
     */
    public List<ReviewRecord> getReviewRecordsByAssignmentId(Long assignmentId) {
        return reviewRecordRepository.findByAssignmentId(assignmentId);
    }
    
    /**
     * 根据评审者ID查询评审记录
     */
    public List<ReviewRecord> getReviewRecordsByReviewerId(Long reviewerId, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate) {
        return reviewRecordRepository.findByReviewerId(reviewerId, startDate, endDate);
    }
    
    /**
     * 根据被评审者ID查询评审记录
     */
    public List<ReviewRecord> getReviewRecordsByRevieweeId(Long revieweeId, 
                                                          LocalDateTime startDate, 
                                                          LocalDateTime endDate) {
        return reviewRecordRepository.findByRevieweeId(revieweeId, startDate, endDate);
    }
    
    /**
     * 根据团队ID查询评审记录
     */
    public List<ReviewRecord> getReviewRecordsByTeamId(Long teamId, 
                                                      LocalDateTime startDate, 
                                                      LocalDateTime endDate) {
        return reviewRecordRepository.findByTeamId(teamId, startDate, endDate);
    }
    
    /**
     * 根据状态查询评审记录
     */
    public List<ReviewRecord> getReviewRecordsByStatus(ReviewRecord.ReviewStatus status) {
        return reviewRecordRepository.findByStatus(status);
    }
    
    /**
     * 查询需要重新评审的记录
     */
    public List<ReviewRecord> getNeedsReReviewRecords(Long teamId) {
        return reviewRecordRepository.findNeedsReReview(teamId);
    }
    
    /**
     * 提交评审记录
     */
    @Transactional
    public ReviewRecord submitReviewRecord(Long id) {
        log.info("提交评审记录: id={}", id);
        
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(id);
        if (reviewRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        
        // 验证评审记录是否完整
        validateReviewRecordForSubmission(reviewRecord);
        
        // 更新状态为已提交
        reviewRecord.setStatus(ReviewRecord.ReviewStatus.SUBMITTED);
        reviewRecordRepository.updateById(reviewRecord);
        
        // 更新分配记录状态
        ReviewAssignment assignment = assignmentRepository.selectById(reviewRecord.getAssignmentId());
        if (assignment != null) {
            assignment.setStatus(ReviewAssignment.AssignmentStatus.IN_PROGRESS);
            assignmentRepository.updateById(assignment);
        }
        
        log.info("评审记录提交成功: id={}", id);
        return reviewRecord;
    }
    
    /**
     * 验证评审记录是否可以提交
     */
    private void validateReviewRecordForSubmission(ReviewRecord reviewRecord) {
        if (reviewRecord.getStatus() != ReviewRecord.ReviewStatus.DRAFT) {
            throw new RuntimeException("只有草稿状态的评审记录才能提交");
        }
        
        if (reviewRecord.getTitle() == null || reviewRecord.getTitle().trim().isEmpty()) {
            throw new RuntimeException("评审标题不能为空");
        }
        
        if (reviewRecord.getOverallScore() == null || 
            reviewRecord.getOverallScore() < 1 || reviewRecord.getOverallScore() > 10) {
            throw new RuntimeException("总体评分必须在1-10分之间");
        }
        
        if (reviewRecord.getSummary() == null || reviewRecord.getSummary().trim().isEmpty()) {
            throw new RuntimeException("评审总结不能为空");
        }
    }
    
    /**
     * 标记需要重新评审
     */
    @Transactional
    public ReviewRecord markForReReview(Long id, String reason) {
        log.info("标记需要重新评审: id={}, reason={}", id, reason);
        
        ReviewRecord reviewRecord = reviewRecordRepository.selectById(id);
        if (reviewRecord == null) {
            throw new RuntimeException("评审记录不存在");
        }
        
        if (reviewRecord.getStatus() != ReviewRecord.ReviewStatus.COMPLETED) {
            throw new RuntimeException("只有已完成的评审记录才能标记为需要重新评审");
        }
        
        // 更新状态和标记
        reviewRecord.setNeedsReReview(true);
        reviewRecord.setStatus(ReviewRecord.ReviewStatus.IN_PROGRESS);
        reviewRecord.setRemarks(reason);
        reviewRecordRepository.updateById(reviewRecord);
        
        log.info("标记重新评审成功: id={}", id);
        return reviewRecord;
    }
    
    /**
     * 统计用户的评审记录数量
     */
    public int countReviewRecordsByReviewerId(Long reviewerId, 
                                            LocalDateTime startDate, 
                                            LocalDateTime endDate) {
        return reviewRecordRepository.countByReviewerId(reviewerId, startDate, endDate);
    }
    
    /**
     * 统计团队的评审记录数量
     */
    public int countReviewRecordsByTeamId(Long teamId, 
                                        LocalDateTime startDate, 
                                        LocalDateTime endDate) {
        return reviewRecordRepository.countByTeamId(teamId, startDate, endDate);
    }
    
    /**
     * 批量更新评审记录状态
     */
    @Transactional
    public void batchUpdateStatus(List<Long> ids, 
                                 ReviewRecord.ReviewStatus status, 
                                 Long updatedBy) {
        log.info("批量更新评审记录状态: ids={}, status={}", ids, status);
        
        int updatedCount = reviewRecordRepository.batchUpdateStatus(ids, status, updatedBy);
        
        log.info("批量更新完成: 更新了{}条记录", updatedCount);
    }
    
    /**
     * 复制评审记录（用于重新评审）
     */
    @Transactional
    public ReviewRecord copyReviewRecord(Long originalId, Long newAssignmentId) {
        log.info("复制评审记录: originalId={}, newAssignmentId={}", originalId, newAssignmentId);
        
        ReviewRecord original = getReviewRecordWithDetails(originalId);
        if (original == null) {
            throw new RuntimeException("原评审记录不存在");
        }
        
        // 创建新的评审记录
        ReviewRecord newRecord = new ReviewRecord();
        newRecord.setAssignmentId(newAssignmentId);
        newRecord.setTitle(original.getTitle() + " (重新评审)");
        newRecord.setCodeRepository(original.getCodeRepository());
        newRecord.setCodeFilePath(original.getCodeFilePath());
        newRecord.setDescription(original.getDescription());
        newRecord.setStatus(ReviewRecord.ReviewStatus.DRAFT);
        newRecord.setNeedsReReview(false);
        
        reviewRecordRepository.insert(newRecord);
        
        // 复制截图
        if (original.getScreenshots() != null) {
            for (CodeScreenshot screenshot : original.getScreenshots()) {
                CodeScreenshot newScreenshot = new CodeScreenshot();
                newScreenshot.setReviewRecordId(newRecord.getId());
                newScreenshot.setFileName(screenshot.getFileName());
                newScreenshot.setFileUrl(screenshot.getFileUrl());
                newScreenshot.setFileSize(screenshot.getFileSize());
                newScreenshot.setFileType(screenshot.getFileType());
                newScreenshot.setDescription(screenshot.getDescription());
                newScreenshot.setSortOrder(screenshot.getSortOrder());
                screenshotRepository.insert(newScreenshot);
            }
        }
        
        log.info("评审记录复制成功: newId={}", newRecord.getId());
        return newRecord;
    }
}