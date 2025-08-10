package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.AISummaryRequest;
import com.company.codereview.user.dto.AISummaryResponse;
import com.company.codereview.user.entity.AISummary;
import com.company.codereview.user.service.AISummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI Summary Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-summaries")
@RequiredArgsConstructor
@Tag(name = "AI Summary Management", description = "AI-powered code review summary APIs")
public class AISummaryController {
    
    private final AISummaryService aiSummaryService;
    
    /**
     * Generate AI summary
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate AI Summary", description = "Generate AI-powered code review summary")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CompletableFuture<AISummaryResponse>> generateSummary(
            @Valid @RequestBody AISummaryRequest request) {
        
        log.info("Generating AI summary for team: {}, type: {}", request.getTeamId(), request.getSummaryType());
        
        // Get current user ID (placeholder - should be from security context)
        Long currentUserId = getCurrentUserId();
        
        CompletableFuture<AISummaryResponse> future = aiSummaryService.generateSummary(request, currentUserId);
        
        return ResponseResult.success(future);
    }
    
    /**
     * Get summary by ID
     */
    @GetMapping("/{summaryId}")
    @Operation(summary = "Get Summary by ID", description = "Retrieve AI summary by ID")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<AISummaryResponse> getSummaryById(
            @Parameter(description = "Summary ID") @PathVariable Long summaryId) {
        
        log.info("Retrieving AI summary: {}", summaryId);
        
        AISummaryResponse response = aiSummaryService.getSummaryById(summaryId);
        return ResponseResult.success(response);
    }
    
    /**
     * Get summaries by team
     */
    @GetMapping("/team/{teamId}")
    @Operation(summary = "Get Team Summaries", description = "Get AI summaries for a specific team")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<AISummaryResponse>> getTeamSummaries(
            @Parameter(description = "Team ID") @PathVariable Long teamId,
            @Parameter(description = "Summary status") @RequestParam(required = false) AISummary.SummaryStatus status) {
        
        log.info("Retrieving team summaries for team: {}, status: {}", teamId, status);
        
        List<AISummaryResponse> summaries = aiSummaryService.getSummariesByTeam(teamId, status);
        return ResponseResult.success(summaries);
    }
    
    /**
     * Get summaries by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get Summaries by Date Range", description = "Get AI summaries within date range")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<AISummaryResponse>> getSummariesByDateRange(
            @Parameter(description = "Team ID (optional)") @RequestParam(required = false) Long teamId,
            @Parameter(description = "Start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Retrieving summaries by date range: {} to {}, team: {}", startDate, endDate, teamId);
        
        List<AISummaryResponse> summaries = aiSummaryService.getSummariesByDateRange(teamId, startDate, endDate);
        return ResponseResult.success(summaries);
    }
    
    /**
     * Get latest summary by type
     */
    @GetMapping("/latest")
    @Operation(summary = "Get Latest Summary", description = "Get latest AI summary by type")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<AISummaryResponse> getLatestSummary(
            @Parameter(description = "Summary type") @RequestParam AISummaryRequest.SummaryType summaryType,
            @Parameter(description = "Team ID (optional)") @RequestParam(required = false) Long teamId) {
        
        log.info("Retrieving latest summary by type: {}, team: {}", summaryType, teamId);
        
        AISummaryResponse response = aiSummaryService.getLatestSummaryByType(summaryType, teamId);
        return ResponseResult.success(response);
    }
    
    /**
     * Update summary content
     */
    @PutMapping("/{summaryId}")
    @Operation(summary = "Update Summary", description = "Update AI summary content")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<AISummaryResponse> updateSummary(
            @Parameter(description = "Summary ID") @PathVariable Long summaryId,
            @RequestBody UpdateSummaryRequest request) {
        
        log.info("Updating AI summary: {}", summaryId);
        
        Long currentUserId = getCurrentUserId();
        AISummaryResponse response = aiSummaryService.updateSummary(
            summaryId, request.getContent(), request.getRecommendations(), currentUserId);
        
        return ResponseResult.success(response);
    }
    
    /**
     * Publish summary
     */
    @PostMapping("/{summaryId}/publish")
    @Operation(summary = "Publish Summary", description = "Publish AI summary")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> publishSummary(
            @Parameter(description = "Summary ID") @PathVariable Long summaryId) {
        
        log.info("Publishing AI summary: {}", summaryId);
        
        Long currentUserId = getCurrentUserId();
        aiSummaryService.publishSummary(summaryId, currentUserId);
        
        return ResponseResult.success();
    }
    
    /**
     * Archive summary
     */
    @PostMapping("/{summaryId}/archive")
    @Operation(summary = "Archive Summary", description = "Archive AI summary")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> archiveSummary(
            @Parameter(description = "Summary ID") @PathVariable Long summaryId) {
        
        log.info("Archiving AI summary: {}", summaryId);
        
        Long currentUserId = getCurrentUserId();
        aiSummaryService.archiveSummary(summaryId, currentUserId);
        
        return ResponseResult.success();
    }
    
    /**
     * Delete summary
     */
    @DeleteMapping("/{summaryId}")
    @Operation(summary = "Delete Summary", description = "Delete AI summary")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteSummary(
            @Parameter(description = "Summary ID") @PathVariable Long summaryId) {
        
        log.info("Deleting AI summary: {}", summaryId);
        
        aiSummaryService.deleteSummary(summaryId);
        
        return ResponseResult.success();
    }
    
    /**
     * Compare summaries
     */
    @GetMapping("/{currentSummaryId}/compare/{previousSummaryId}")
    @Operation(summary = "Compare Summaries", description = "Compare two AI summaries")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<AISummaryService.SummaryComparison> compareSummaries(
            @Parameter(description = "Current summary ID") @PathVariable Long currentSummaryId,
            @Parameter(description = "Previous summary ID") @PathVariable Long previousSummaryId) {
        
        log.info("Comparing summaries: {} vs {}", currentSummaryId, previousSummaryId);
        
        AISummaryService.SummaryComparison comparison = aiSummaryService.compareSummaries(currentSummaryId, previousSummaryId);
        return ResponseResult.success(comparison);
    }
    
    /**
     * Generate team weekly summary
     */
    @PostMapping("/team/{teamId}/weekly")
    @Operation(summary = "Generate Team Weekly Summary", description = "Generate weekly summary for team")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CompletableFuture<AISummaryResponse>> generateTeamWeeklySummary(
            @Parameter(description = "Team ID") @PathVariable Long teamId,
            @Parameter(description = "Week start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        log.info("Generating team weekly summary for team: {}, week: {}", teamId, weekStart);
        
        AISummaryRequest request = new AISummaryRequest();
        request.setTeamId(teamId);
        request.setSummaryType(AISummaryRequest.SummaryType.TEAM_WEEKLY);
        request.setStartDate(weekStart);
        request.setEndDate(weekStart.plusDays(6));
        
        Long currentUserId = getCurrentUserId();
        CompletableFuture<AISummaryResponse> future = aiSummaryService.generateSummary(request, currentUserId);
        
        return ResponseResult.success(future);
    }
    
    /**
     * Generate architect weekly summary
     */
    @PostMapping("/architect/weekly")
    @Operation(summary = "Generate Architect Weekly Summary", description = "Generate weekly summary for architect")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<CompletableFuture<AISummaryResponse>> generateArchitectWeeklySummary(
            @Parameter(description = "Week start date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        log.info("Generating architect weekly summary for week: {}", weekStart);
        
        AISummaryRequest request = new AISummaryRequest();
        request.setTeamId(null); // Global summary
        request.setSummaryType(AISummaryRequest.SummaryType.ARCHITECT_WEEKLY);
        request.setStartDate(weekStart);
        request.setEndDate(weekStart.plusDays(6));
        
        Long currentUserId = getCurrentUserId();
        CompletableFuture<AISummaryResponse> future = aiSummaryService.generateSummary(request, currentUserId);
        
        return ResponseResult.success(future);
    }
    
    /**
     * Get current user ID (placeholder implementation)
     */
    private Long getCurrentUserId() {
        // In real implementation, this would get the user ID from SecurityContext
        return 1L;
    }
    
    /**
     * Update Summary Request DTO
     */
    @lombok.Data
    public static class UpdateSummaryRequest {
        private String content;
        private List<String> recommendations;
    }
}