package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.entity.FixRecord;
import com.company.codereview.user.dto.FixRecordRequest;
import com.company.codereview.user.dto.VerificationRequest;
import com.company.codereview.user.service.FixRecordService;
import com.company.codereview.user.service.IssueTrackingService;
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

/**
 * 整改记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/fix-records")
@RequiredArgsConstructor
@Tag(name = "整改记录管理", description = "整改记录管理相关接口")
public class FixRecordController {
    
    private final FixRecordService fixRecordService;
    private final IssueTrackingService issueTrackingService;
    
    /**
     * 创建整改记录
     */
    @PostMapping
    @Operation(summary = "创建整改记录", description = "为问题创建整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> createFixRecord(@Valid @RequestBody FixRecordRequest request) {
        log.info("创建整改记录: fixerId={}", request.getFixerId());
        
        try {
            FixRecord fixRecord = new FixRecord();
            fixRecord.setFixerId(request.getFixerId());
            fixRecord.setFixDescription(request.getFixDescription());
            fixRecord.setBeforeCodeUrl(request.getBeforeCodeUrl());
            fixRecord.setAfterCodeUrl(request.getAfterCodeUrl());
            
            FixRecord createdRecord = fixRecordService.createFixRecord(fixRecord);
            return ResponseResult.success(createdRecord);
        } catch (Exception e) {
            log.error("创建整改记录失败", e);
            return ResponseResult.error("创建整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交整改记录
     */
    @PostMapping("/submit/{issueId}")
    @Operation(summary = "提交整改记录", description = "为指定问题提交整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> submitFixRecord(
            @PathVariable Long issueId,
            @Valid @RequestBody FixRecordRequest request) {
        log.info("提交整改记录: issueId={}, fixerId={}", issueId, request.getFixerId());
        
        try {
            FixRecord fixRecord = issueTrackingService.submitFixRecord(issueId, request);
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("提交整改记录失败: issueId={}", issueId, e);
            return ResponseResult.error("提交整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新整改记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新整改记录", description = "更新整改记录信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> updateFixRecord(@PathVariable Long id, @Valid @RequestBody FixRecord fixRecord) {
        log.info("更新整改记录: id={}", id);
        
        try {
            fixRecord.setId(id);
            FixRecord updatedRecord = fixRecordService.updateFixRecord(fixRecord);
            return ResponseResult.success(updatedRecord);
        } catch (Exception e) {
            log.error("更新整改记录失败: id={}", id, e);
            return ResponseResult.error("更新整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除整改记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除整改记录", description = "删除指定整改记录")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteFixRecord(@PathVariable Long id) {
        log.info("删除整改记录: id={}", id);
        
        try {
            fixRecordService.deleteFixRecord(id);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("删除整改记录失败: id={}", id, e);
            return ResponseResult.error("删除整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询整改记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询整改记录详情", description = "根据ID查询整改记录详细信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> getFixRecordById(@PathVariable Long id) {
        log.info("查询整改记录详情: id={}", id);
        
        try {
            FixRecord fixRecord = fixRecordService.getFixRecordById(id);
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("查询整改记录详情失败: id={}", id, e);
            return ResponseResult.error("查询整改记录详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据问题ID查询整改记录列表
     */
    @GetMapping("/by-issue/{issueId}")
    @Operation(summary = "查询问题的整改记录", description = "根据问题ID查询相关整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<FixRecord>> getFixRecordsByIssueId(@PathVariable Long issueId) {
        log.info("查询问题的整改记录: issueId={}", issueId);
        
        try {
            List<FixRecord> fixRecords = fixRecordService.getFixRecordsByIssueId(issueId);
            return ResponseResult.success(fixRecords);
        } catch (Exception e) {
            log.error("查询问题的整改记录失败: issueId={}", issueId, e);
            return ResponseResult.error("查询整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据整改人ID查询整改记录
     */
    @GetMapping("/by-fixer/{fixerId}")
    @Operation(summary = "查询用户的整改记录", description = "根据整改人ID查询整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<FixRecord>> getFixRecordsByFixerId(
            @PathVariable Long fixerId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("查询用户的整改记录: fixerId={}, startDate={}, endDate={}", fixerId, startDate, endDate);
        
        try {
            List<FixRecord> fixRecords = fixRecordService.getFixRecordsByFixerId(fixerId, startDate, endDate);
            return ResponseResult.success(fixRecords);
        } catch (Exception e) {
            log.error("查询用户的整改记录失败: fixerId={}", fixerId, e);
            return ResponseResult.error("查询整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询待验证的整改记录
     */
    @GetMapping("/pending-verification/{verifierId}")
    @Operation(summary = "查询待验证的整改记录", description = "查询分配给验证人的待验证整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<FixRecord>> getPendingVerificationRecords(@PathVariable Long verifierId) {
        log.info("查询待验证的整改记录: verifierId={}", verifierId);
        
        try {
            List<FixRecord> fixRecords = fixRecordService.getPendingVerificationRecords(verifierId);
            return ResponseResult.success(fixRecords);
        } catch (Exception e) {
            log.error("查询待验证的整改记录失败: verifierId={}", verifierId, e);
            return ResponseResult.error("查询待验证记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询整改记录
     */
    @GetMapping("/by-status")
    @Operation(summary = "按状态查询整改记录", description = "根据状态查询整改记录")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<FixRecord>> getFixRecordsByStatus(
            @Parameter(description = "整改记录状态") @RequestParam FixRecord.FixStatus status,
            @Parameter(description = "团队ID") @RequestParam Long teamId) {
        log.info("按状态查询整改记录: status={}, teamId={}", status, teamId);
        
        try {
            List<FixRecord> fixRecords = fixRecordService.getFixRecordsByStatus(status, teamId);
            return ResponseResult.success(fixRecords);
        } catch (Exception e) {
            log.error("按状态查询整改记录失败: status={}", status, e);
            return ResponseResult.error("查询整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交整改记录进行验证
     */
    @PutMapping("/{id}/submit-for-verification")
    @Operation(summary = "提交验证", description = "提交整改记录进行验证")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> submitForVerification(
            @PathVariable Long id,
            @Parameter(description = "验证人ID") @RequestParam Long verifierId) {
        log.info("提交整改记录进行验证: id={}, verifierId={}", id, verifierId);
        
        try {
            FixRecord fixRecord = fixRecordService.submitForVerification(id, verifierId);
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("提交验证失败: id={}", id, e);
            return ResponseResult.error("提交验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证整改记录
     */
    @PutMapping("/{id}/verify")
    @Operation(summary = "验证整改记录", description = "对整改记录进行验证")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> verifyFixRecord(
            @PathVariable Long id,
            @Valid @RequestBody VerificationRequest request) {
        log.info("验证整改记录: id={}, result={}", id, request.getResult());
        
        try {
            FixRecord fixRecord = fixRecordService.verifyFixRecord(id, request.getResult(), 
                    request.getRemarks(), request.getVerifierId());
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("验证整改记录失败: id={}", id, e);
            return ResponseResult.error("验证整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 使用跟踪服务验证整改记录
     */
    @PutMapping("/{id}/verify-with-tracking")
    @Operation(summary = "验证整改记录（跟踪）", description = "使用跟踪服务验证整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> verifyFixRecordWithTracking(
            @PathVariable Long id,
            @Valid @RequestBody VerificationRequest request) {
        log.info("使用跟踪服务验证整改记录: id={}, result={}", id, request.getResult());
        
        try {
            FixRecord fixRecord = issueTrackingService.verifyFixRecord(id, request);
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("验证整改记录失败: id={}", id, e);
            return ResponseResult.error("验证整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新整改记录状态
     */
    @PutMapping("/batch-update-status")
    @Operation(summary = "批量更新整改记录状态", description = "批量更新多个整改记录的状态")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> batchUpdateFixRecordStatus(
            @Parameter(description = "整改记录ID列表") @RequestParam List<Long> ids,
            @Parameter(description = "新状态") @RequestParam FixRecord.FixStatus status,
            @Parameter(description = "更新人ID") @RequestParam Long updatedBy) {
        log.info("批量更新整改记录状态: ids={}, status={}, updatedBy={}", ids, status, updatedBy);
        
        try {
            fixRecordService.batchUpdateFixRecordStatus(ids, status, updatedBy);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("批量更新整改记录状态失败", e);
            return ResponseResult.error("批量更新状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最新的整改记录
     */
    @GetMapping("/latest/{issueId}")
    @Operation(summary = "获取最新整改记录", description = "获取问题的最新整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> getLatestFixRecord(@PathVariable Long issueId) {
        log.info("获取最新整改记录: issueId={}", issueId);
        
        try {
            FixRecord fixRecord = fixRecordService.getLatestFixRecord(issueId);
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("获取最新整改记录失败: issueId={}", issueId, e);
            return ResponseResult.error("获取最新整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 统计用户的整改记录数量
     */
    @GetMapping("/count/{fixerId}")
    @Operation(summary = "统计用户整改记录", description = "统计用户在指定时间段的整改记录数量")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Integer> countFixRecordsByUser(
            @PathVariable Long fixerId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("统计用户整改记录: fixerId={}, startDate={}, endDate={}", fixerId, startDate, endDate);
        
        try {
            int count = fixRecordService.countFixRecordsByUser(fixerId, startDate, endDate);
            return ResponseResult.success(count);
        } catch (Exception e) {
            log.error("统计用户整改记录失败: fixerId={}", fixerId, e);
            return ResponseResult.error("统计整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算整改及时率
     */
    @GetMapping("/timeliness/{userId}")
    @Operation(summary = "计算整改及时率", description = "计算用户的整改及时率")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Double> calculateFixTimeliness(
            @PathVariable Long userId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("计算整改及时率: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        
        try {
            Double timeliness = fixRecordService.calculateFixTimeliness(userId, startDate, endDate);
            return ResponseResult.success(timeliness);
        } catch (Exception e) {
            log.error("计算整改及时率失败: userId={}", userId, e);
            return ResponseResult.error("计算整改及时率失败: " + e.getMessage());
        }
    }
    
    /**
     * 重新提交整改记录
     */
    @PutMapping("/{id}/resubmit")
    @Operation(summary = "重新提交整改记录", description = "重新提交被拒绝或需要修改的整改记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecord> resubmitFixRecord(
            @PathVariable Long id,
            @Parameter(description = "新的整改描述") @RequestParam(required = false) String newDescription,
            @Parameter(description = "新的整改后代码链接") @RequestParam(required = false) String newAfterCodeUrl) {
        log.info("重新提交整改记录: id={}", id);
        
        try {
            FixRecord fixRecord = fixRecordService.resubmitFixRecord(id, newDescription, newAfterCodeUrl);
            return ResponseResult.success(fixRecord);
        } catch (Exception e) {
            log.error("重新提交整改记录失败: id={}", id, e);
            return ResponseResult.error("重新提交整改记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取整改记录统计信息
     */
    @GetMapping("/statistics/{teamId}")
    @Operation(summary = "获取整改记录统计", description = "获取团队的整改记录统计信息")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FixRecordService.FixRecordStatistics> getFixRecordStatistics(
            @PathVariable Long teamId,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("获取整改记录统计: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        try {
            FixRecordService.FixRecordStatistics statistics = 
                    fixRecordService.getFixRecordStatistics(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取整改记录统计失败: teamId={}", teamId, e);
            return ResponseResult.error("获取统计信息失败: " + e.getMessage());
        }
    }
}