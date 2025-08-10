package com.company.codereview.user.controller;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.service.IssueService;
import com.company.codereview.user.service.IssueTrackingService;
import com.company.codereview.user.service.IssueClassificationService;
import com.company.codereview.user.service.IssueTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问题管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "问题管理", description = "问题管理相关接口")
public class IssueController {
    
    private final IssueService issueService;
    private final IssueTrackingService issueTrackingService;
    private final IssueClassificationService classificationService;
    private final IssueTemplateService templateService;
    
    /**
     * 创建问题
     */
    @PostMapping
    @Operation(summary = "创建问题", description = "创建新的代码评审问题")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> createIssue(@Valid @RequestBody Issue issue) {
        log.info("创建问题: title={}, type={}, severity={}", issue.getTitle(), issue.getIssueType(), issue.getSeverity());
        
        try {
            // 智能分类问题
            Issue classifiedIssue = classificationService.classifyIssue(issue);
            
            // 创建问题
            Issue createdIssue = issueService.createIssue(classifiedIssue);
            
            return ResponseResult.success(createdIssue);
        } catch (Exception e) {
            log.error("创建问题失败", e);
            return ResponseResult.error("创建问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新问题
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新问题", description = "更新问题信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> updateIssue(@PathVariable Long id, @Valid @RequestBody Issue issue) {
        log.info("更新问题: id={}", id);
        
        try {
            issue.setId(id);
            Issue updatedIssue = issueService.updateIssue(issue);
            return ResponseResult.success(updatedIssue);
        } catch (Exception e) {
            log.error("更新问题失败: id={}", id, e);
            return ResponseResult.error("更新问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除问题
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除问题", description = "删除指定问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteIssue(@PathVariable Long id) {
        log.info("删除问题: id={}", id);
        
        try {
            issueService.deleteIssue(id);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("删除问题失败: id={}", id, e);
            return ResponseResult.error("删除问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询问题
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询问题详情", description = "根据ID查询问题详细信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> getIssueById(@PathVariable Long id) {
        log.info("查询问题详情: id={}", id);
        
        try {
            Issue issue = issueService.getIssueById(id);
            return ResponseResult.success(issue);
        } catch (Exception e) {
            log.error("查询问题详情失败: id={}", id, e);
            return ResponseResult.error("查询问题详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询问题详情（包含整改记录）
     */
    @GetMapping("/{id}/with-fix-records")
    @Operation(summary = "查询问题详情（含整改记录）", description = "查询问题详情及其所有整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> getIssueWithFixRecords(@PathVariable Long id) {
        log.info("查询问题详情（含整改记录）: id={}", id);
        
        try {
            Issue issue = issueService.getIssueWithFixRecords(id);
            return ResponseResult.success(issue);
        } catch (Exception e) {
            log.error("查询问题详情失败: id={}", id, e);
            return ResponseResult.error("查询问题详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据评审记录ID查询问题列表
     */
    @GetMapping("/by-review-record/{reviewRecordId}")
    @Operation(summary = "查询评审记录的问题", description = "根据评审记录ID查询相关问题")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Issue>> getIssuesByReviewRecordId(@PathVariable Long reviewRecordId) {
        log.info("查询评审记录的问题: reviewRecordId={}", reviewRecordId);
        
        try {
            List<Issue> issues = issueService.getIssuesByReviewRecordId(reviewRecordId);
            return ResponseResult.success(issues);
        } catch (Exception e) {
            log.error("查询评审记录的问题失败: reviewRecordId={}", reviewRecordId, e);
            return ResponseResult.error("查询问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询分配给用户的问题
     */
    @GetMapping("/assigned-to-user/{userId}")
    @Operation(summary = "查询用户分配的问题", description = "查询分配给指定用户的问题")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Issue>> getIssuesAssignedToUser(
            @PathVariable Long userId,
            @Parameter(description = "问题状态") @RequestParam(required = false) Issue.IssueStatus status) {
        log.info("查询用户分配的问题: userId={}, status={}", userId, status);
        
        try {
            List<Issue> issues = issueService.getIssuesAssignedToUser(userId, status);
            return ResponseResult.success(issues);
        } catch (Exception e) {
            log.error("查询用户分配的问题失败: userId={}", userId, e);
            return ResponseResult.error("查询问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询团队问题
     */
    @GetMapping("/by-team/{teamId}")
    @Operation(summary = "查询团队问题", description = "查询指定团队的问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Issue>> getIssuesByTeamId(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("查询团队问题: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            List<Issue> issues = issueService.getIssuesByTeamId(teamId, startDate, endDate);
            return ResponseResult.success(issues);
        } catch (Exception e) {
            log.error("查询团队问题失败: teamId={}", teamId, e);
            return ResponseResult.error("查询团队问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型和严重级别查询问题
     */
    @GetMapping("/by-type-and-severity")
    @Operation(summary = "按类型和严重级别查询", description = "根据问题类型和严重级别查询问题")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Issue>> getIssuesByTypeAndSeverity(
            @Parameter(description = "问题类型") @RequestParam IssueType issueType,
            @Parameter(description = "严重级别") @RequestParam Severity severity,
            @Parameter(description = "团队ID") @RequestParam Long teamId) {
        log.info("按类型和严重级别查询问题: type={}, severity={}, teamId={}", issueType, severity, teamId);
        
        try {
            List<Issue> issues = issueService.getIssuesByTypeAndSeverity(issueType, severity, teamId);
            return ResponseResult.success(issues);
        } catch (Exception e) {
            log.error("按类型和严重级别查询问题失败", e);
            return ResponseResult.error("查询问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询待整改的问题
     */
    @GetMapping("/pending-fix/{userId}")
    @Operation(summary = "查询待整改问题", description = "查询用户待整改的问题")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Issue>> getPendingFixIssues(@PathVariable Long userId) {
        log.info("查询待整改问题: userId={}", userId);
        
        try {
            List<Issue> issues = issueService.getPendingFixIssues(userId);
            return ResponseResult.success(issues);
        } catch (Exception e) {
            log.error("查询待整改问题失败: userId={}", userId, e);
            return ResponseResult.error("查询待整改问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新问题状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新问题状态", description = "更新问题的状态")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> updateIssueStatus(
            @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam Issue.IssueStatus status) {
        log.info("更新问题状态: id={}, status={}", id, status);
        
        try {
            Issue issue = issueService.updateIssueStatus(id, status);
            return ResponseResult.success(issue);
        } catch (Exception e) {
            log.error("更新问题状态失败: id={}", id, e);
            return ResponseResult.error("更新问题状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新问题状态
     */
    @PutMapping("/batch-update-status")
    @Operation(summary = "批量更新问题状态", description = "批量更新多个问题的状态")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> batchUpdateIssueStatus(
            @Parameter(description = "问题ID列表") @RequestParam List<Long> ids,
            @Parameter(description = "新状态") @RequestParam Issue.IssueStatus status,
            @Parameter(description = "更新人ID") @RequestParam Long updatedBy) {
        log.info("批量更新问题状态: ids={}, status={}, updatedBy={}", ids, status, updatedBy);
        
        try {
            issueService.batchUpdateIssueStatus(ids, status, updatedBy);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("批量更新问题状态失败", e);
            return ResponseResult.error("批量更新问题状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 关闭问题
     */
    @PutMapping("/{id}/close")
    @Operation(summary = "关闭问题", description = "关闭指定问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> closeIssue(
            @PathVariable Long id,
            @Parameter(description = "关闭原因") @RequestParam(required = false) String reason) {
        log.info("关闭问题: id={}, reason={}", id, reason);
        
        try {
            Issue issue = issueService.closeIssue(id, reason);
            return ResponseResult.success(issue);
        } catch (Exception e) {
            log.error("关闭问题失败: id={}", id, e);
            return ResponseResult.error("关闭问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新打开问题
     */
    @PutMapping("/{id}/reopen")
    @Operation(summary = "重新打开问题", description = "重新打开已关闭的问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> reopenIssue(
            @PathVariable Long id,
            @Parameter(description = "重新打开原因") @RequestParam(required = false) String reason) {
        log.info("重新打开问题: id={}, reason={}", id, reason);
        
        try {
            Issue issue = issueService.reopenIssue(id, reason);
            return ResponseResult.success(issue);
        } catch (Exception e) {
            log.error("重新打开问题失败: id={}", id, e);
            return ResponseResult.error("重新打开问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取团队问题统计
     */
    @GetMapping("/statistics/team/{teamId}")
    @Operation(summary = "团队问题统计", description = "获取团队问题统计信息")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> getTeamIssueStatistics(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取团队问题统计: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            List<Map<String, Object>> statistics = issueService.getTeamIssueStatistics(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取团队问题统计失败: teamId={}", teamId, e);
            return ResponseResult.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户问题统计
     */
    @GetMapping("/statistics/user/{userId}")
    @Operation(summary = "用户问题统计", description = "获取用户问题统计信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> getUserIssueStatistics(
            @PathVariable Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取用户问题统计: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        try {
            List<Map<String, Object>> statistics = issueService.getUserIssueStatistics(userId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取用户问题统计失败: userId={}", userId, e);
            return ResponseResult.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取高频问题
     */
    @GetMapping("/frequent/{teamId}")
    @Operation(summary = "获取高频问题", description = "获取团队的高频问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> getFrequentIssues(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取高频问题: teamId={}, startDate={}, endDate={}, limit={}", teamId, startDate, endDate, limit);
        
        try {
            List<Map<String, Object>> frequentIssues = issueService.getFrequentIssues(teamId, startDate, endDate, limit);
            return ResponseResult.success(frequentIssues);
        } catch (Exception e) {
            log.error("获取高频问题失败: teamId={}", teamId, e);
            return ResponseResult.error("获取高频问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 统计问题数量按类型分组
     */
    @GetMapping("/count-by-type/{teamId}")
    @Operation(summary = "按类型统计问题", description = "统计团队问题数量按类型分组")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> countIssuesByType(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("按类型统计问题: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            List<Map<String, Object>> statistics = issueService.countIssuesByType(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("按类型统计问题失败: teamId={}", teamId, e);
            return ResponseResult.error("统计问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 统计问题数量按严重级别分组
     */
    @GetMapping("/count-by-severity/{teamId}")
    @Operation(summary = "按严重级别统计问题", description = "统计团队问题数量按严重级别分组")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, Object>>> countIssuesBySeverity(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("按严重级别统计问题: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            List<Map<String, Object>> statistics = issueService.countIssuesBySeverity(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("按严重级别统计问题失败: teamId={}", teamId, e);
            return ResponseResult.error("统计问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取问题分类建议
     */
    @PostMapping("/classification-suggestion")
    @Operation(summary = "获取问题分类建议", description = "基于问题内容获取智能分类建议")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IssueClassificationService.IssueClassificationSuggestion> getClassificationSuggestion(
            @Parameter(description = "问题标题") @RequestParam String title,
            @Parameter(description = "问题描述") @RequestParam String description) {
        log.info("获取问题分类建议: title={}", title);
        
        try {
            IssueClassificationService.IssueClassificationSuggestion suggestion = 
                    classificationService.getClassificationSuggestion(title, description);
            return ResponseResult.success(suggestion);
        } catch (Exception e) {
            log.error("获取问题分类建议失败", e);
            return ResponseResult.error("获取分类建议失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取问题模板列表
     */
    @GetMapping("/templates")
    @Operation(summary = "获取问题模板", description = "获取所有问题模板")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<IssueTemplateService.IssueTemplate>> getIssueTemplates() {
        log.info("获取问题模板列表");
        
        try {
            List<IssueTemplateService.IssueTemplate> templates = templateService.getIssueTemplates();
            return ResponseResult.success(templates);
        } catch (Exception e) {
            log.error("获取问题模板失败", e);
            return ResponseResult.error("获取问题模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据类型获取问题模板
     */
    @GetMapping("/templates/by-type")
    @Operation(summary = "按类型获取问题模板", description = "根据问题类型获取相关模板")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<IssueTemplateService.IssueTemplate>> getIssueTemplatesByType(
            @Parameter(description = "问题类型") @RequestParam IssueType issueType) {
        log.info("按类型获取问题模板: type={}", issueType);
        
        try {
            List<IssueTemplateService.IssueTemplate> templates = templateService.getIssueTemplatesByType(issueType);
            return ResponseResult.success(templates);
        } catch (Exception e) {
            log.error("按类型获取问题模板失败: type={}", issueType, e);
            return ResponseResult.error("获取问题模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据模板创建问题
     */
    @PostMapping("/create-from-template")
    @Operation(summary = "从模板创建问题", description = "使用模板快速创建问题")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Issue> createIssueFromTemplate(
            @Parameter(description = "模板ID") @RequestParam String templateId,
            @Parameter(description = "评审记录ID") @RequestParam Long reviewRecordId,
            @Parameter(description = "模板参数") @RequestBody Map<String, String> parameters) {
        log.info("从模板创建问题: templateId={}, reviewRecordId={}", templateId, reviewRecordId);
        
        try {
            Issue issue = templateService.createIssueFromTemplate(templateId, reviewRecordId, parameters);
            Issue createdIssue = issueService.createIssue(issue);
            return ResponseResult.success(createdIssue);
        } catch (Exception e) {
            log.error("从模板创建问题失败: templateId={}", templateId, e);
            return ResponseResult.error("从模板创建问题失败: " + e.getMessage());
        }
    }
}