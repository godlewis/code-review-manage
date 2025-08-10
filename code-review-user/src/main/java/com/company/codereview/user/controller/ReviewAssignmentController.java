package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.ReviewAssignmentDTO;
import com.company.codereview.user.entity.ReviewAssignment;
import com.company.codereview.user.service.ReviewAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 评审分配控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review-assignments")
@RequiredArgsConstructor
@Tag(name = "评审分配管理", description = "评审任务分配相关接口")
public class ReviewAssignmentController {
    
    private final ReviewAssignmentService assignmentService;
    
    /**
     * 生成周度分配
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "生成周度分配", description = "为指定团队生成指定周的评审分配")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> generateWeeklyAssignments(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            log.info("开始生成周度分配: teamId={}, weekStart={}", teamId, weekStart);
            
            List<ReviewAssignment> assignments = assignmentService.generateWeeklyAssignments(teamId, weekStart);
            List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
            
            log.info("成功生成 {} 个分配", assignmentDTOs.size());
            return ResponseEntity.ok(ResponseResult.success(assignmentDTOs));
        } catch (Exception e) {
            log.error("生成周度分配失败: teamId={}, weekStart={}, error={}", teamId, weekStart, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 批量生成多周分配
     */
    @PostMapping("/generate-batch")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "批量生成多周分配", description = "为指定团队批量生成多周的评审分配")
    public ResponseEntity<ResponseResult<Map<LocalDate, List<ReviewAssignmentDTO>>>> generateBatchAssignments(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startWeek,
            @RequestParam Integer weekCount) {
        try {
            log.info("开始批量生成分配: teamId={}, startWeek={}, weekCount={}", teamId, startWeek, weekCount);
            
            if (weekCount <= 0 || weekCount > 12) {
                return ResponseEntity.ok(ResponseResult.error(400, "周数必须在1-12之间"));
            }
            
            Map<LocalDate, List<ReviewAssignmentDTO>> batchResult = new java.util.HashMap<>();
            
            for (int i = 0; i < weekCount; i++) {
                LocalDate weekStart = startWeek.plusWeeks(i);
                List<ReviewAssignment> assignments = assignmentService.generateWeeklyAssignments(teamId, weekStart);
                List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
                batchResult.put(weekStart, assignmentDTOs);
            }
            
            log.info("成功批量生成 {} 周的分配", weekCount);
            return ResponseEntity.ok(ResponseResult.success(batchResult));
        } catch (Exception e) {
            log.error("批量生成分配失败: teamId={}, startWeek={}, weekCount={}, error={}", 
                teamId, startWeek, weekCount, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 查询团队分配历史
     */
    @GetMapping("/team/{teamId}/history")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "查询团队分配历史", description = "查询指定团队在指定时间范围内的分配历史")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> getTeamAssignmentHistory(
            @PathVariable Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("查询团队分配历史: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.ok(ResponseResult.error(400, "开始日期不能晚于结束日期"));
            }
            
            List<ReviewAssignment> assignments = assignmentService.getTeamAssignmentHistory(teamId, startDate, endDate);
            List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
            
            log.info("查询到 {} 条分配历史", assignmentDTOs.size());
            return ResponseEntity.ok(ResponseResult.success(assignmentDTOs));
        } catch (Exception e) {
            log.error("查询团队分配历史失败: teamId={}, error={}", teamId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 查询用户分配历史
     */
    @GetMapping("/user/{userId}/history")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "查询用户分配历史", description = "查询指定用户在指定时间范围内的分配历史")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> getUserAssignmentHistory(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("查询用户分配历史: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.ok(ResponseResult.error(400, "开始日期不能晚于结束日期"));
            }
            
            List<ReviewAssignment> assignments = assignmentService.getUserAssignmentHistory(userId, startDate, endDate);
            List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
            
            log.info("查询到 {} 条分配历史", assignmentDTOs.size());
            return ResponseEntity.ok(ResponseResult.success(assignmentDTOs));
        } catch (Exception e) {
            log.error("查询用户分配历史失败: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 查询当前周分配
     */
    @GetMapping("/current-week")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT') or hasRole('DEVELOPER')")
    @Operation(summary = "查询当前周分配", description = "查询当前周的评审分配")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> getCurrentWeekAssignments(
            @RequestParam(required = false) Long teamId) {
        try {
            LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            log.info("查询当前周分配: teamId={}, weekStart={}", teamId, currentWeekStart);
            
            List<ReviewAssignment> assignments;
            if (teamId != null) {
                assignments = assignmentService.getTeamAssignmentHistory(teamId, currentWeekStart, currentWeekStart.plusDays(6));
            } else {
                // 如果没有指定团队，返回当前用户相关的分配
                // 这里需要获取当前用户ID，暂时返回空列表
                assignments = List.of();
            }
            
            List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
            
            log.info("查询到当前周 {} 条分配", assignmentDTOs.size());
            return ResponseEntity.ok(ResponseResult.success(assignmentDTOs));
        } catch (Exception e) {
            log.error("查询当前周分配失败: teamId={}, error={}", teamId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 手动调整分配
     */
    @PutMapping("/{assignmentId}/adjust")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "手动调整分配", description = "手动调整指定的评审分配")
    public ResponseEntity<ResponseResult<ReviewAssignmentDTO>> adjustAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AdjustAssignmentRequest request) {
        try {
            log.info("手动调整分配: assignmentId={}, newRevieweeId={}", assignmentId, request.getNewRevieweeId());
            
            ReviewAssignment adjustedAssignment = assignmentService.adjustAssignment(
                assignmentId, request.getNewRevieweeId(), request.getRemarks());
            
            ReviewAssignmentDTO assignmentDTO = convertToDTO(adjustedAssignment);
            
            log.info("成功调整分配: assignmentId={}", assignmentId);
            return ResponseEntity.ok(ResponseResult.success(assignmentDTO));
        } catch (Exception e) {
            log.error("调整分配失败: assignmentId={}, error={}", assignmentId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 批量调整分配
     */
    @PutMapping("/batch-adjust")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "批量调整分配", description = "批量调整多个评审分配")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> batchAdjustAssignments(
            @Valid @RequestBody List<BatchAdjustRequest> requests) {
        try {
            log.info("批量调整分配: 数量={}", requests.size());
            
            List<ReviewAssignmentDTO> adjustedAssignments = new java.util.ArrayList<>();
            
            for (BatchAdjustRequest request : requests) {
                try {
                    ReviewAssignment adjustedAssignment = assignmentService.adjustAssignment(
                        request.getAssignmentId(), request.getNewRevieweeId(), request.getRemarks());
                    adjustedAssignments.add(convertToDTO(adjustedAssignment));
                } catch (Exception e) {
                    log.warn("调整分配失败: assignmentId={}, error={}", request.getAssignmentId(), e.getMessage());
                    // 继续处理其他分配，不中断整个批量操作
                }
            }
            
            log.info("批量调整完成: 成功={}, 总数={}", adjustedAssignments.size(), requests.size());
            return ResponseEntity.ok(ResponseResult.success(adjustedAssignments));
        } catch (Exception e) {
            log.error("批量调整分配失败: error={}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 删除分配
     */
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "删除分配", description = "删除指定的评审分配")
    public ResponseEntity<ResponseResult<Void>> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            log.info("删除分配: assignmentId={}", assignmentId);
            
            assignmentService.deleteAssignment(assignmentId);
            
            log.info("成功删除分配: assignmentId={}", assignmentId);
            return ResponseEntity.ok(ResponseResult.success("删除成功", null));
        } catch (Exception e) {
            log.error("删除分配失败: assignmentId={}, error={}", assignmentId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 批量删除分配
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "批量删除分配", description = "批量删除多个评审分配")
    public ResponseEntity<ResponseResult<Void>> batchDeleteAssignments(
            @RequestBody List<Long> assignmentIds) {
        try {
            log.info("批量删除分配: 数量={}", assignmentIds.size());
            
            assignmentService.batchDeleteAssignments(assignmentIds);
            
            log.info("批量删除完成: 数量={}", assignmentIds.size());
            return ResponseEntity.ok(ResponseResult.success("批量删除成功", null));
        } catch (Exception e) {
            log.error("批量删除分配失败: error={}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取分配统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取分配统计信息", description = "获取评审分配的统计信息")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getAssignmentStatistics(
            @RequestParam(required = false) Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("获取分配统计信息: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
            
            List<ReviewAssignment> assignments;
            if (teamId != null) {
                assignments = assignmentService.getTeamAssignmentHistory(teamId, startDate, endDate);
            } else {
                // 获取所有团队的分配（这里需要实现相应的服务方法）
                assignments = List.of();
            }
            
            Map<String, Object> statistics = calculateStatistics(assignments);
            
            log.info("获取分配统计信息成功");
            return ResponseEntity.ok(ResponseResult.success(statistics));
        } catch (Exception e) {
            log.error("获取分配统计信息失败: error={}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 验证分配结果
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "验证分配结果", description = "验证评审分配是否符合规则")
    public ResponseEntity<ResponseResult<AssignmentValidationResult>> validateAssignments(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            log.info("验证分配结果: teamId={}, weekStart={}", teamId, weekStart);
            
            List<String> conflicts = assignmentService.checkAssignmentConflicts(teamId, weekStart);
            
            AssignmentValidationResult validationResult = new AssignmentValidationResult();
            validationResult.setValid(conflicts.isEmpty());
            validationResult.setErrors(conflicts);
            
            log.info("分配验证完成: 有效={}, 错误数={}", validationResult.isValid(), validationResult.getErrors().size());
            return ResponseEntity.ok(ResponseResult.success(validationResult));
        } catch (Exception e) {
            log.error("验证分配结果失败: error={}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 预览分配结果
     */
    @PostMapping("/preview")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "预览分配结果", description = "预览评审分配结果，不保存到数据库")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> previewAssignments(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            log.info("预览分配结果: teamId={}, weekStart={}", teamId, weekStart);
            
            List<ReviewAssignment> assignments = assignmentService.previewWeeklyAssignments(teamId, weekStart);
            List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
            
            log.info("预览完成: 将生成 {} 个分配", assignmentDTOs.size());
            return ResponseEntity.ok(ResponseResult.success(assignmentDTOs));
        } catch (Exception e) {
            log.error("预览分配结果失败: teamId={}, weekStart={}, error={}", teamId, weekStart, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取分配详情
     */
    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT') or @reviewAssignmentService.isUserInvolved(#assignmentId, authentication.name)")
    @Operation(summary = "获取分配详情", description = "获取指定分配的详细信息")
    public ResponseEntity<ResponseResult<ReviewAssignmentDTO>> getAssignmentDetail(@PathVariable Long assignmentId) {
        try {
            log.info("获取分配详情: assignmentId={}", assignmentId);
            
            ReviewAssignment assignment = assignmentService.getAssignmentDetail(assignmentId);
            ReviewAssignmentDTO assignmentDTO = convertToDTO(assignment);
            
            log.info("获取分配详情成功: assignmentId={}", assignmentId);
            return ResponseEntity.ok(ResponseResult.success(assignmentDTO));
        } catch (Exception e) {
            log.error("获取分配详情失败: assignmentId={}, error={}", assignmentId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 更新分配状态
     */
    @PutMapping("/{assignmentId}/status")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT') or @reviewAssignmentService.isUserInvolved(#assignmentId, authentication.name)")
    @Operation(summary = "更新分配状态", description = "更新评审分配的状态")
    public ResponseEntity<ResponseResult<ReviewAssignmentDTO>> updateAssignmentStatus(
            @PathVariable Long assignmentId,
            @RequestParam String status) {
        try {
            log.info("更新分配状态: assignmentId={}, status={}", assignmentId, status);
            
            ReviewAssignment.AssignmentStatus assignmentStatus;
            try {
                assignmentStatus = ReviewAssignment.AssignmentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(ResponseResult.error(400, "无效的状态值: " + status));
            }
            
            ReviewAssignment assignment = assignmentService.updateAssignmentStatus(assignmentId, assignmentStatus);
            ReviewAssignmentDTO assignmentDTO = convertToDTO(assignment);
            
            log.info("更新分配状态成功: assignmentId={}, status={}", assignmentId, status);
            return ResponseEntity.ok(ResponseResult.success(assignmentDTO));
        } catch (Exception e) {
            log.error("更新分配状态失败: assignmentId={}, status={}, error={}", assignmentId, status, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取用户分配统计
     */
    @GetMapping("/user/{userId}/statistics")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT') or @userService.isCurrentUser(#userId)")
    @Operation(summary = "获取用户分配统计", description = "获取指定用户的分配统计信息")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getUserAssignmentStatistics(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("获取用户分配统计: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
            
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.ok(ResponseResult.error(400, "开始日期不能晚于结束日期"));
            }
            
            Map<String, Object> statistics = assignmentService.getUserAssignmentStatistics(userId, startDate, endDate);
            
            log.info("获取用户分配统计成功: userId={}", userId);
            return ResponseEntity.ok(ResponseResult.success(statistics));
        } catch (Exception e) {
            log.error("获取用户分配统计失败: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取我的当前分配
     */
    @GetMapping("/my-current")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取我的当前分配", description = "获取当前用户本周的分配")
    public ResponseEntity<ResponseResult<List<ReviewAssignmentDTO>>> getMyCurrentAssignments() {
        try {
            // TODO: 从SecurityContext获取当前用户ID
            Long currentUserId = getCurrentUserId();
            LocalDate currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            
            log.info("获取我的当前分配: userId={}, weekStart={}", currentUserId, currentWeekStart);
            
            List<ReviewAssignment> assignments = assignmentService.getCurrentUserAssignments(currentUserId, currentWeekStart);
            List<ReviewAssignmentDTO> assignmentDTOs = convertToDTO(assignments);
            
            log.info("获取到我的当前分配 {} 条", assignmentDTOs.size());
            return ResponseEntity.ok(ResponseResult.success(assignmentDTOs));
        } catch (Exception e) {
            log.error("获取我的当前分配失败: error={}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 检查分配冲突
     */
    @GetMapping("/conflicts")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "检查分配冲突", description = "检查指定团队和周的分配冲突")
    public ResponseEntity<ResponseResult<List<String>>> checkAssignmentConflicts(
            @RequestParam Long teamId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            log.info("检查分配冲突: teamId={}, weekStart={}", teamId, weekStart);
            
            List<String> conflicts = assignmentService.checkAssignmentConflicts(teamId, weekStart);
            
            log.info("冲突检查完成: 发现 {} 个冲突", conflicts.size());
            return ResponseEntity.ok(ResponseResult.success(conflicts));
        } catch (Exception e) {
            log.error("检查分配冲突失败: teamId={}, weekStart={}, error={}", teamId, weekStart, e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 转换为DTO
     */
    private List<ReviewAssignmentDTO> convertToDTO(List<ReviewAssignment> assignments) {
        return assignments.stream()
            .map(this::convertToDTO)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 转换单个实体为DTO
     */
    private ReviewAssignmentDTO convertToDTO(ReviewAssignment assignment) {
        ReviewAssignmentDTO dto = new ReviewAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setTeamId(assignment.getTeamId());
        dto.setReviewerId(assignment.getReviewerId());
        dto.setRevieweeId(assignment.getRevieweeId());
        dto.setWeekStartDate(assignment.getWeekStartDate());
        dto.setStatus(assignment.getStatus().name());
        dto.setSkillMatchScore(assignment.getSkillMatchScore());
        dto.setLoadBalanceScore(assignment.getLoadBalanceScore());
        dto.setDiversityScore(assignment.getDiversityScore());
        dto.setTotalScore(assignment.getTotalScore());
        dto.setIsManualAdjusted(assignment.getIsManualAdjusted());
        dto.setRemarks(assignment.getRemarks());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setUpdatedAt(assignment.getUpdatedAt());
        return dto;
    }
    
    /**
     * 计算统计信息
     */
    private Map<String, Object> calculateStatistics(List<ReviewAssignment> assignments) {
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        statistics.put("totalAssignments", assignments.size());
        statistics.put("completedAssignments", assignments.stream()
            .mapToInt(a -> a.getStatus() == ReviewAssignment.AssignmentStatus.COMPLETED ? 1 : 0).sum());
        statistics.put("inProgressAssignments", assignments.stream()
            .mapToInt(a -> a.getStatus() == ReviewAssignment.AssignmentStatus.IN_PROGRESS ? 1 : 0).sum());
        statistics.put("manualAdjustedCount", assignments.stream()
            .mapToInt(a -> a.getIsManualAdjusted() ? 1 : 0).sum());
        statistics.put("averageTotalScore", assignments.stream()
            .filter(a -> a.getTotalScore() != null)
            .mapToDouble(ReviewAssignment::getTotalScore)
            .average().orElse(0.0));
        
        return statistics;
    }
    
    /**
     * 验证分配规则
     */
    private AssignmentValidationResult validateAssignmentRules(List<ReviewAssignment> assignments) {
        AssignmentValidationResult result = new AssignmentValidationResult();
        result.setValid(true);
        result.setErrors(new java.util.ArrayList<>());
        
        // 检查是否有重复分配
        Set<String> pairSet = new java.util.HashSet<>();
        for (ReviewAssignment assignment : assignments) {
            String pair = assignment.getReviewerId() + "-" + assignment.getRevieweeId();
            if (pairSet.contains(pair)) {
                result.setValid(false);
                result.getErrors().add("发现重复分配: " + pair);
            }
            pairSet.add(pair);
        }
        
        // 检查是否有自己评审自己的情况
        for (ReviewAssignment assignment : assignments) {
            if (assignment.getReviewerId().equals(assignment.getRevieweeId())) {
                result.setValid(false);
                result.getErrors().add("发现自己评审自己: " + assignment.getReviewerId());
            }
        }
        
        return result;
    }
    
    /**
     * 调整分配请求
     */
    public static class AdjustAssignmentRequest {
        private Long newRevieweeId;
        private String remarks;
        
        public Long getNewRevieweeId() { return newRevieweeId; }
        public void setNewRevieweeId(Long newRevieweeId) { this.newRevieweeId = newRevieweeId; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
    
    /**
     * 批量调整请求
     */
    public static class BatchAdjustRequest {
        private Long assignmentId;
        private Long newRevieweeId;
        private String remarks;
        
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public Long getNewRevieweeId() { return newRevieweeId; }
        public void setNewRevieweeId(Long newRevieweeId) { this.newRevieweeId = newRevieweeId; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
    }
    
    /**
     * 分配验证结果
     */
    public static class AssignmentValidationResult {
        private boolean valid;
        private List<String> errors;
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
    
    /**
     * 获取当前用户ID（临时实现，实际项目中应从SecurityContext获取）
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 1L; // 临时返回固定值
    }
}