package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.ReviewRecordDTO;
import com.company.codereview.user.entity.ReviewRecord;
import com.company.codereview.user.service.ReviewRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评审记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review-records")
@RequiredArgsConstructor
@Validated
@Api(tags = "评审记录管理")
public class ReviewRecordController {
    
    private final ReviewRecordService reviewRecordService;
    
    /**
     * 创建评审记录
     */
    @PostMapping
    @ApiOperation("创建评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> createReviewRecord(
            @Valid @RequestBody ReviewRecordDTO reviewRecordDTO) {
        
        log.info("创建评审记录: assignmentId={}, title={}", 
                reviewRecordDTO.getAssignmentId(), reviewRecordDTO.getTitle());
        
        ReviewRecord reviewRecord = convertToEntity(reviewRecordDTO);
        reviewRecord = reviewRecordService.createReviewRecord(reviewRecord);
        
        return ResponseResult.success(convertToDTO(reviewRecord));
    }
    
    /**
     * 更新评审记录
     */
    @PutMapping("/{id}")
    @ApiOperation("更新评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> updateReviewRecord(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRecordDTO reviewRecordDTO) {
        
        log.info("更新评审记录: id={}", id);
        
        reviewRecordDTO.setId(id);
        ReviewRecord reviewRecord = convertToEntity(reviewRecordDTO);
        reviewRecord = reviewRecordService.updateReviewRecord(reviewRecord);
        
        return ResponseResult.success(convertToDTO(reviewRecord));
    }
    
    /**
     * 删除评审记录
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteReviewRecord(@PathVariable Long id) {
        
        log.info("删除评审记录: id={}", id);
        
        reviewRecordService.deleteReviewRecord(id);
        
        return ResponseResult.success();
    }
    
    /**
     * 根据ID查询评审记录
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> getReviewRecord(@PathVariable Long id) {
        
        ReviewRecord reviewRecord = reviewRecordService.getReviewRecordById(id);
        
        return ResponseResult.success(convertToDTO(reviewRecord));
    }
    
    /**
     * 查询评审记录详情（包含截图和问题）
     */
    @GetMapping("/{id}/details")
    @ApiOperation("查询评审记录详情")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> getReviewRecordDetails(@PathVariable Long id) {
        
        ReviewRecord reviewRecord = reviewRecordService.getReviewRecordWithDetails(id);
        
        return ResponseResult.success(convertToDTOWithDetails(reviewRecord));
    }
    
    /**
     * 根据分配ID查询评审记录
     */
    @GetMapping("/assignment/{assignmentId}")
    @ApiOperation("根据分配ID查询评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<ReviewRecordDTO>> getReviewRecordsByAssignment(
            @PathVariable Long assignmentId) {
        
        List<ReviewRecord> reviewRecords = reviewRecordService.getReviewRecordsByAssignmentId(assignmentId);
        List<ReviewRecordDTO> dtoList = reviewRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 根据评审者ID查询评审记录
     */
    @GetMapping("/reviewer/{reviewerId}")
    @ApiOperation("根据评审者ID查询评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<ReviewRecordDTO>> getReviewRecordsByReviewer(
            @PathVariable Long reviewerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<ReviewRecord> reviewRecords = reviewRecordService.getReviewRecordsByReviewerId(
                reviewerId, startDate, endDate);
        List<ReviewRecordDTO> dtoList = reviewRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 根据被评审者ID查询评审记录
     */
    @GetMapping("/reviewee/{revieweeId}")
    @ApiOperation("根据被评审者ID查询评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<ReviewRecordDTO>> getReviewRecordsByReviewee(
            @PathVariable Long revieweeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<ReviewRecord> reviewRecords = reviewRecordService.getReviewRecordsByRevieweeId(
                revieweeId, startDate, endDate);
        List<ReviewRecordDTO> dtoList = reviewRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 根据团队ID查询评审记录
     */
    @GetMapping("/team/{teamId}")
    @ApiOperation("根据团队ID查询评审记录")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<ReviewRecordDTO>> getReviewRecordsByTeam(
            @PathVariable Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<ReviewRecord> reviewRecords = reviewRecordService.getReviewRecordsByTeamId(
                teamId, startDate, endDate);
        List<ReviewRecordDTO> dtoList = reviewRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 根据状态查询评审记录
     */
    @GetMapping("/status/{status}")
    @ApiOperation("根据状态查询评审记录")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<ReviewRecordDTO>> getReviewRecordsByStatus(
            @PathVariable ReviewRecord.ReviewStatus status) {
        
        List<ReviewRecord> reviewRecords = reviewRecordService.getReviewRecordsByStatus(status);
        List<ReviewRecordDTO> dtoList = reviewRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 查询需要重新评审的记录
     */
    @GetMapping("/team/{teamId}/re-review")
    @ApiOperation("查询需要重新评审的记录")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<ReviewRecordDTO>> getNeedsReReviewRecords(@PathVariable Long teamId) {
        
        List<ReviewRecord> reviewRecords = reviewRecordService.getNeedsReReviewRecords(teamId);
        List<ReviewRecordDTO> dtoList = reviewRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 提交评审记录
     */
    @PostMapping("/{id}/submit")
    @ApiOperation("提交评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> submitReviewRecord(@PathVariable Long id) {
        
        log.info("提交评审记录: id={}", id);
        
        ReviewRecord reviewRecord = reviewRecordService.submitReviewRecord(id);
        
        return ResponseResult.success(convertToDTO(reviewRecord));
    }
    
    /**
     * 标记需要重新评审
     */
    @PostMapping("/{id}/mark-re-review")
    @ApiOperation("标记需要重新评审")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> markForReReview(
            @PathVariable Long id,
            @RequestParam @ApiParam("重新评审原因") String reason) {
        
        log.info("标记需要重新评审: id={}, reason={}", id, reason);
        
        ReviewRecord reviewRecord = reviewRecordService.markForReReview(id, reason);
        
        return ResponseResult.success(convertToDTO(reviewRecord));
    }
    
    /**
     * 复制评审记录
     */
    @PostMapping("/{id}/copy")
    @ApiOperation("复制评审记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<ReviewRecordDTO> copyReviewRecord(
            @PathVariable Long id,
            @RequestParam @ApiParam("新分配ID") Long newAssignmentId) {
        
        log.info("复制评审记录: originalId={}, newAssignmentId={}", id, newAssignmentId);
        
        ReviewRecord reviewRecord = reviewRecordService.copyReviewRecord(id, newAssignmentId);
        
        return ResponseResult.success(convertToDTO(reviewRecord));
    }
    
    /**
     * 统计用户的评审记录数量
     */
    @GetMapping("/reviewer/{reviewerId}/count")
    @ApiOperation("统计用户的评审记录数量")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Integer> countReviewRecordsByReviewer(
            @PathVariable Long reviewerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        int count = reviewRecordService.countReviewRecordsByReviewerId(reviewerId, startDate, endDate);
        
        return ResponseResult.success(count);
    }
    
    /**
     * 统计团队的评审记录数量
     */
    @GetMapping("/team/{teamId}/count")
    @ApiOperation("统计团队的评审记录数量")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Integer> countReviewRecordsByTeam(
            @PathVariable Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        int count = reviewRecordService.countReviewRecordsByTeamId(teamId, startDate, endDate);
        
        return ResponseResult.success(count);
    }
    
    /**
     * 批量更新评审记录状态
     */
    @PutMapping("/batch/status")
    @ApiOperation("批量更新评审记录状态")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> batchUpdateStatus(
            @RequestParam List<Long> ids,
            @RequestParam ReviewRecord.ReviewStatus status,
            @RequestParam Long updatedBy) {
        
        log.info("批量更新评审记录状态: ids={}, status={}", ids, status);
        
        reviewRecordService.batchUpdateStatus(ids, status, updatedBy);
        
        return ResponseResult.success();
    }
    
    /**
     * 转换实体为DTO
     */
    private ReviewRecordDTO convertToDTO(ReviewRecord reviewRecord) {
        if (reviewRecord == null) {
            return null;
        }
        
        ReviewRecordDTO dto = new ReviewRecordDTO();
        dto.setId(reviewRecord.getId());
        dto.setAssignmentId(reviewRecord.getAssignmentId());
        dto.setTitle(reviewRecord.getTitle());
        dto.setCodeRepository(reviewRecord.getCodeRepository());
        dto.setCodeFilePath(reviewRecord.getCodeFilePath());
        dto.setDescription(reviewRecord.getDescription());
        dto.setOverallScore(reviewRecord.getOverallScore());
        dto.setSummary(reviewRecord.getSummary());
        dto.setStatus(reviewRecord.getStatus());
        dto.setNeedsReReview(reviewRecord.getNeedsReReview());
        dto.setCompletedAt(reviewRecord.getCompletedAt());
        dto.setCreatedAt(reviewRecord.getCreatedAt());
        dto.setUpdatedAt(reviewRecord.getUpdatedAt());
        dto.setCreatedBy(reviewRecord.getCreatedBy());
        dto.setUpdatedBy(reviewRecord.getUpdatedBy());
        
        return dto;
    }
    
    /**
     * 转换DTO为实体
     */
    private ReviewRecord convertToEntity(ReviewRecordDTO dto) {
        if (dto == null) {
            return null;
        }
        
        ReviewRecord reviewRecord = new ReviewRecord();
        reviewRecord.setId(dto.getId());
        reviewRecord.setAssignmentId(dto.getAssignmentId());
        reviewRecord.setTitle(dto.getTitle());
        reviewRecord.setCodeRepository(dto.getCodeRepository());
        reviewRecord.setCodeFilePath(dto.getCodeFilePath());
        reviewRecord.setDescription(dto.getDescription());
        reviewRecord.setOverallScore(dto.getOverallScore());
        reviewRecord.setSummary(dto.getSummary());
        reviewRecord.setStatus(dto.getStatus());
        reviewRecord.setNeedsReReview(dto.getNeedsReReview());
        reviewRecord.setCompletedAt(dto.getCompletedAt());
        
        return reviewRecord;
    }
    
    /**
     * 转换实体为DTO（包含详细信息）
     */
    private ReviewRecordDTO convertToDTOWithDetails(ReviewRecord reviewRecord) {
        ReviewRecordDTO dto = convertToDTO(reviewRecord);
        
        if (reviewRecord.getScreenshots() != null) {
            // TODO: 转换截图列表
        }
        
        if (reviewRecord.getIssues() != null) {
            // TODO: 转换问题列表
        }
        
        return dto;
    }
}