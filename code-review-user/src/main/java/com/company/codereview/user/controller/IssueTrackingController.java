package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.service.IssueTrackingService;
import com.company.codereview.user.service.FixTrackingWorkflowService;
import com.company.codereview.user.service.FixTrackingMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 问题跟踪控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/issue-tracking")
@RequiredArgsConstructor
@Tag(name = "问题跟踪", description = "问题跟踪相关接口")
public class IssueTrackingController {
    
    private final IssueTrackingService issueTrackingService;
    private final FixTrackingWorkflowService workflowService;
    private final FixTrackingMetricsService metricsService;
    
    /**
     * 获取问题跟踪仪表板
     */
    @GetMapping("/dashboard/{teamId}")
    @Operation(summary = "获取跟踪仪表板", description = "获取团队问题跟踪仪表板数据")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IssueTrackingService.IssueTrackingDashboard> getTrackingDashboard(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取跟踪仪表板: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            IssueTrackingService.IssueTrackingDashboard dashboard = 
                    issueTrackingService.getTrackingDashboard(teamId, startDate, endDate);
            return ResponseResult.success(dashboard);
        } catch (Exception e) {
            log.error("获取跟踪仪表板失败: teamId={}", teamId, e);
            return ResponseResult.error("获取仪表板数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取问题趋势数据
     */
    @GetMapping("/trend/{teamId}")
    @Operation(summary = "获取问题趋势", description = "获取团队问题趋势数据")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<IssueTrackingService.IssueTrendData>> getIssueTrendData(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取问题趋势数据: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            List<IssueTrackingService.IssueTrendData> trendData = 
                    issueTrackingService.getIssueTrendData(teamId, startDate, endDate);
            return ResponseResult.success(trendData);
        } catch (Exception e) {
            log.error("获取问题趋势数据失败: teamId={}", teamId, e);
            return ResponseResult.error("获取趋势数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取热点问题
     */
    @GetMapping("/hot-issues/{teamId}")
    @Operation(summary = "获取热点问题", description = "获取团队热点问题列表")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<com.company.codereview.user.entity.Issue>> getHotIssues(
            @PathVariable Long teamId,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") int limit) {
        log.info("获取热点问题: teamId={}, limit={}", teamId, limit);
        
        try {
            List<com.company.codereview.user.entity.Issue> hotIssues = 
                    issueTrackingService.getHotIssues(teamId, limit);
            return ResponseResult.success(hotIssues);
        } catch (Exception e) {
            log.error("获取热点问题失败: teamId={}", teamId, e);
            return ResponseResult.error("获取热点问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户问题分配情况
     */
    @GetMapping("/user-assignments/{teamId}")
    @Operation(summary = "获取用户问题分配", description = "获取团队成员问题分配情况")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<IssueTrackingService.UserIssueAssignment>> getUserIssueAssignments(
            @PathVariable Long teamId) {
        log.info("获取用户问题分配情况: teamId={}", teamId);
        
        try {
            List<IssueTrackingService.UserIssueAssignment> assignments = 
                    issueTrackingService.getUserIssueAssignments(teamId);
            return ResponseResult.success(assignments);
        } catch (Exception e) {
            log.error("获取用户问题分配情况失败: teamId={}", teamId, e);
            return ResponseResult.error("获取分配情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 自动分配问题
     */
    @PostMapping("/auto-assign/{teamId}")
    @Operation(summary = "自动分配问题", description = "自动为团队成员分配未分配的问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> autoAssignIssues(@PathVariable Long teamId) {
        log.info("自动分配问题: teamId={}", teamId);
        
        try {
            issueTrackingService.autoAssignIssues(teamId);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("自动分配问题失败: teamId={}", teamId, e);
            return ResponseResult.error("自动分配问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 升级超时问题
     */
    @PostMapping("/escalate-overdue")
    @Operation(summary = "升级超时问题", description = "升级所有超时未处理的问题")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> escalateOverdueIssues() {
        log.info("升级超时问题");
        
        try {
            issueTrackingService.escalateOverdueIssues();
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("升级超时问题失败", e);
            return ResponseResult.error("升级超时问题失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取问题解决时间统计
     */
    @GetMapping("/resolution-stats/{teamId}")
    @Operation(summary = "获取解决时间统计", description = "获取团队问题解决时间统计")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IssueTrackingService.IssueResolutionStats> getResolutionStats(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取解决时间统计: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            IssueTrackingService.IssueResolutionStats stats = 
                    issueTrackingService.getResolutionStats(teamId, startDate, endDate);
            return ResponseResult.success(stats);
        } catch (Exception e) {
            log.error("获取解决时间统计失败: teamId={}", teamId, e);
            return ResponseResult.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取整改跟踪详情
     */
    @GetMapping("/fix-tracking/{issueId}")
    @Operation(summary = "获取整改跟踪详情", description = "获取问题的整改跟踪详细信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IssueTrackingService.IssueFixTrackingDetail> getFixTrackingDetail(
            @PathVariable Long issueId) {
        log.info("获取整改跟踪详情: issueId={}", issueId);
        
        try {
            IssueTrackingService.IssueFixTrackingDetail detail = 
                    issueTrackingService.getFixTrackingDetail(issueId);
            return ResponseResult.success(detail);
        } catch (Exception e) {
            log.error("获取整改跟踪详情失败: issueId={}", issueId, e);
            return ResponseResult.error("获取跟踪详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的整改任务列表
     */
    @GetMapping("/user-fix-tasks/{userId}")
    @Operation(summary = "获取用户整改任务", description = "获取用户的整改任务列表")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<IssueTrackingService.UserFixTask>> getUserFixTasks(@PathVariable Long userId) {
        log.info("获取用户整改任务: userId={}", userId);
        
        try {
            List<IssueTrackingService.UserFixTask> tasks = issueTrackingService.getUserFixTasks(userId);
            return ResponseResult.success(tasks);
        } catch (Exception e) {
            log.error("获取用户整改任务失败: userId={}", userId, e);
            return ResponseResult.error("获取整改任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取整改效果报告
     */
    @GetMapping("/effectiveness-report/{teamId}")
    @Operation(summary = "获取整改效果报告", description = "获取团队整改效果评估报告")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IssueTrackingService.FixEffectivenessReport> getFixEffectivenessReport(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取整改效果报告: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            IssueTrackingService.FixEffectivenessReport report = 
                    issueTrackingService.getFixEffectivenessReport(teamId, startDate, endDate);
            return ResponseResult.success(report);
        } catch (Exception e) {
            log.error("获取整改效果报告失败: teamId={}", teamId, e);
            return ResponseResult.error("获取效果报告失败: " + e.getMessage());
        }
    }
    
    /**
     * 启动整改工作流
     */
    @PostMapping("/workflow/start/{issueId}")
    @Operation(summary = "启动整改工作流", description = "为问题启动整改工作流")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingWorkflowService.FixTrackingWorkflow> startFixWorkflow(
            @PathVariable Long issueId,
            @Parameter(description = "分配给的用户ID") @RequestParam Long assigneeId) {
        log.info("启动整改工作流: issueId={}, assigneeId={}", issueId, assigneeId);
        
        try {
            FixTrackingWorkflowService.FixTrackingWorkflow workflow = 
                    workflowService.startFixWorkflow(issueId, assigneeId);
            return ResponseResult.success(workflow);
        } catch (Exception e) {
            log.error("启动整改工作流失败: issueId={}", issueId, e);
            return ResponseResult.error("启动工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 推进工作流
     */
    @PostMapping("/workflow/advance/{issueId}")
    @Operation(summary = "推进工作流", description = "推进整改工作流到下一阶段")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingWorkflowService.FixTrackingWorkflow> advanceWorkflow(
            @PathVariable Long issueId,
            @Parameter(description = "工作流动作") @RequestParam FixTrackingWorkflowService.WorkflowAction action,
            @Parameter(description = "动作参数") @RequestBody(required = false) Map<String, Object> parameters) {
        log.info("推进工作流: issueId={}, action={}", issueId, action);
        
        try {
            if (parameters == null) {
                parameters = new java.util.HashMap<>();
            }
            
            FixTrackingWorkflowService.FixTrackingWorkflow workflow = 
                    workflowService.advanceWorkflow(issueId, action, parameters);
            return ResponseResult.success(workflow);
        } catch (Exception e) {
            log.error("推进工作流失败: issueId={}, action={}", issueId, action, e);
            return ResponseResult.error("推进工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前工作流状态
     */
    @GetMapping("/workflow/current/{issueId}")
    @Operation(summary = "获取工作流状态", description = "获取问题的当前工作流状态")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingWorkflowService.FixTrackingWorkflow> getCurrentWorkflow(
            @PathVariable Long issueId) {
        log.info("获取当前工作流状态: issueId={}", issueId);
        
        try {
            FixTrackingWorkflowService.FixTrackingWorkflow workflow = 
                    workflowService.getCurrentWorkflow(issueId);
            return ResponseResult.success(workflow);
        } catch (Exception e) {
            log.error("获取当前工作流状态失败: issueId={}", issueId, e);
            return ResponseResult.error("获取工作流状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取超时的工作流
     */
    @GetMapping("/workflow/overdue")
    @Operation(summary = "获取超时工作流", description = "获取所有超时的整改工作流")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<FixTrackingWorkflowService.FixTrackingWorkflow>> getOverdueWorkflows() {
        log.info("获取超时的工作流");
        
        try {
            List<FixTrackingWorkflowService.FixTrackingWorkflow> workflows = 
                    workflowService.getOverdueWorkflows();
            return ResponseResult.success(workflows);
        } catch (Exception e) {
            log.error("获取超时工作流失败", e);
            return ResponseResult.error("获取超时工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理超时工作流
     */
    @PostMapping("/workflow/handle-overdue")
    @Operation(summary = "处理超时工作流", description = "处理所有超时的整改工作流")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> handleOverdueWorkflows() {
        log.info("处理超时工作流");
        
        try {
            workflowService.handleOverdueWorkflows();
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("处理超时工作流失败", e);
            return ResponseResult.error("处理超时工作流失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取工作流统计信息
     */
    @GetMapping("/workflow/statistics/{teamId}")
    @Operation(summary = "获取工作流统计", description = "获取团队工作流统计信息")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingWorkflowService.WorkflowStatistics> getWorkflowStatistics(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取工作流统计信息: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            FixTrackingWorkflowService.WorkflowStatistics statistics = 
                    workflowService.getWorkflowStatistics(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取工作流统计信息失败: teamId={}", teamId, e);
            return ResponseResult.error("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取整改效率指标
     */
    @GetMapping("/metrics/efficiency/{teamId}")
    @Operation(summary = "获取整改效率指标", description = "获取团队整改效率指标")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingMetricsService.FixEfficiencyMetrics> getFixEfficiencyMetrics(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取整改效率指标: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            FixTrackingMetricsService.FixEfficiencyMetrics metrics = 
                    metricsService.calculateFixEfficiencyMetrics(teamId, startDate, endDate);
            return ResponseResult.success(metrics);
        } catch (Exception e) {
            log.error("获取整改效率指标失败: teamId={}", teamId, e);
            return ResponseResult.error("获取效率指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取质量改进指标
     */
    @GetMapping("/metrics/quality-improvement/{teamId}")
    @Operation(summary = "获取质量改进指标", description = "获取团队质量改进指标")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingMetricsService.QualityImprovementMetrics> getQualityImprovementMetrics(
            @PathVariable Long teamId,
            @Parameter(description = "当前周期开始时间") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime currentPeriodStart,
            @Parameter(description = "当前周期结束时间") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime currentPeriodEnd,
            @Parameter(description = "上一周期开始时间") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime previousPeriodStart,
            @Parameter(description = "上一周期结束时间") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime previousPeriodEnd) {
        log.info("获取质量改进指标: teamId={}", teamId);
        
        try {
            FixTrackingMetricsService.QualityImprovementMetrics metrics = 
                    metricsService.calculateQualityImprovementMetrics(teamId, 
                            currentPeriodStart, currentPeriodEnd, 
                            previousPeriodStart, previousPeriodEnd);
            return ResponseResult.success(metrics);
        } catch (Exception e) {
            log.error("获取质量改进指标失败: teamId={}", teamId, e);
            return ResponseResult.error("获取改进指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取团队成员表现指标
     */
    @GetMapping("/metrics/member-performance/{teamId}")
    @Operation(summary = "获取成员表现指标", description = "获取团队成员表现指标")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<FixTrackingMetricsService.MemberPerformanceMetrics>> getMemberPerformanceMetrics(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取团队成员表现指标: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            List<FixTrackingMetricsService.MemberPerformanceMetrics> metrics = 
                    metricsService.calculateMemberPerformanceMetrics(teamId, startDate, endDate);
            return ResponseResult.success(metrics);
        } catch (Exception e) {
            log.error("获取团队成员表现指标失败: teamId={}", teamId, e);
            return ResponseResult.error("获取成员表现指标失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成整改趋势报告
     */
    @GetMapping("/metrics/trend-report/{teamId}")
    @Operation(summary = "生成整改趋势报告", description = "生成团队整改趋势报告")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixTrackingMetricsService.FixTrendReport> generateFixTrendReport(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "时间间隔（天）") @RequestParam(defaultValue = "7") int intervalDays) {
        log.info("生成整改趋势报告: teamId={}, startDate={}, endDate={}, intervalDays={}", 
                teamId, startDate, endDate, intervalDays);
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            FixTrackingMetricsService.FixTrendReport report = 
                    metricsService.generateFixTrendReport(teamId, startDate, endDate, intervalDays);
            return ResponseResult.success(report);
        } catch (Exception e) {
            log.error("生成整改趋势报告失败: teamId={}", teamId, e);
            return ResponseResult.error("生成趋势报告失败: " + e.getMessage());
        }
    }
}