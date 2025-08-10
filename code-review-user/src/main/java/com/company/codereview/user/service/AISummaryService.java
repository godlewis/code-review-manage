package com.company.codereview.user.service;

import com.company.codereview.user.dto.*;
import com.company.codereview.user.entity.AISummary;
import com.company.codereview.user.repository.AISummaryRepository;
import com.company.codereview.user.service.ai.AIClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI Summary Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AISummaryService {
    
    private final AISummaryRepository summaryRepository;
    private final IssueAnalysisService issueAnalysisService;
    private final AIClient aiClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Generate AI summary
     */
    @Async
    @Transactional
    public CompletableFuture<AISummaryResponse> generateSummary(AISummaryRequest request, Long userId) {
        log.info("Starting AI summary generation for team: {}, type: {}", request.getTeamId(), request.getSummaryType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create summary record
            AISummary summary = createSummaryRecord(request, userId);
            
            // Perform issue analysis
            IssueAnalysisResult analysisResult = performIssueAnalysis(request);
            
            // Generate AI content
            String aiContent = generateAIContent(request, analysisResult);
            
            // Extract insights and recommendations
            List<String> keyInsights = extractKeyInsights(analysisResult);
            List<String> recommendations = extractRecommendations(analysisResult);
            
            // Update summary with results
            updateSummaryWithResults(summary, aiContent, analysisResult, keyInsights, recommendations, startTime);
            
            // Build response
            AISummaryResponse response = buildSummaryResponse(summary, analysisResult, keyInsights, recommendations, userId, startTime);
            
            log.info("AI summary generation completed for ID: {}", summary.getId());
            return CompletableFuture.completedFuture(response);
            
        } catch (Exception e) {
            log.error("Failed to generate AI summary", e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Get summary by ID
     */
    public AISummaryResponse getSummaryById(Long summaryId) {
        AISummary summary = summaryRepository.selectById(summaryId);
        if (summary == null) {
            throw new RuntimeException("Summary not found: " + summaryId);
        }
        
        return convertToResponse(summary);
    }
    
    /**
     * Get summaries by team
     */
    public List<AISummaryResponse> getSummariesByTeam(Long teamId, AISummary.SummaryStatus status) {
        List<AISummary> summaries = summaryRepository.findByTeamId(teamId, status);
        return summaries.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get summaries by date range
     */
    public List<AISummaryResponse> getSummariesByDateRange(Long teamId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        List<AISummary> summaries = summaryRepository.findByDateRange(startDate, endDate, teamId);
        return summaries.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get latest summary by type
     */
    public AISummaryResponse getLatestSummaryByType(AISummaryRequest.SummaryType summaryType, Long teamId) {
        AISummary summary = summaryRepository.findLatestByType(summaryType, teamId);
        if (summary == null) {
            return null;
        }
        return convertToResponse(summary);
    }
    
    /**
     * Update summary content
     */
    @Transactional
    public AISummaryResponse updateSummary(Long summaryId, String content, List<String> recommendations, Long userId) {
        AISummary summary = summaryRepository.selectById(summaryId);
        if (summary == null) {
            throw new RuntimeException("Summary not found: " + summaryId);
        }
        
        summary.setContent(content);
        try {
            summary.setRecommendations(objectMapper.writeValueAsString(recommendations));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize recommendations", e);
        }
        summary.setUpdatedBy(userId);
        summary.setUpdatedAt(LocalDateTime.now());
        
        summaryRepository.updateById(summary);
        
        return convertToResponse(summary);
    }
    
    /**
     * Publish summary
     */
    @Transactional
    public void publishSummary(Long summaryId, Long userId) {
        AISummary summary = summaryRepository.selectById(summaryId);
        if (summary == null) {
            throw new RuntimeException("Summary not found: " + summaryId);
        }
        
        summary.setStatus(AISummary.SummaryStatus.PUBLISHED);
        summary.setPublished(true);
        summary.setPublishedAt(LocalDateTime.now());
        summary.setPublishedBy(userId);
        summary.setUpdatedBy(userId);
        summary.setUpdatedAt(LocalDateTime.now());
        
        summaryRepository.updateById(summary);
        
        log.info("Summary published: {}", summaryId);
    }
    
    /**
     * Archive summary
     */
    @Transactional
    public void archiveSummary(Long summaryId, Long userId) {
        AISummary summary = summaryRepository.selectById(summaryId);
        if (summary == null) {
            throw new RuntimeException("Summary not found: " + summaryId);
        }
        
        summary.setStatus(AISummary.SummaryStatus.ARCHIVED);
        summary.setUpdatedBy(userId);
        summary.setUpdatedAt(LocalDateTime.now());
        
        summaryRepository.updateById(summary);
        
        log.info("Summary archived: {}", summaryId);
    }
    
    /**
     * Delete summary
     */
    @Transactional
    public void deleteSummary(Long summaryId) {
        summaryRepository.deleteById(summaryId);
        log.info("Summary deleted: {}", summaryId);
    }
    
    /**
     * Compare summaries
     */
    public SummaryComparison compareSummaries(Long currentSummaryId, Long previousSummaryId) {
        AISummary currentSummary = summaryRepository.selectById(currentSummaryId);
        AISummary previousSummary = summaryRepository.selectById(previousSummaryId);
        
        if (currentSummary == null || previousSummary == null) {
            throw new RuntimeException("One or both summaries not found");
        }
        
        return SummaryComparison.builder()
            .currentSummary(convertToResponse(currentSummary))
            .previousSummary(convertToResponse(previousSummary))
            .improvements(findImprovements(currentSummary, previousSummary))
            .regressions(findRegressions(currentSummary, previousSummary))
            .newPatterns(findNewPatterns(currentSummary, previousSummary))
            .build();
    }
    
    /**
     * Create summary record
     */
    private AISummary createSummaryRecord(AISummaryRequest request, Long userId) {
        AISummary summary = new AISummary();
        summary.setTitle(generateSummaryTitle(request));
        summary.setSummaryType(request.getSummaryType());
        summary.setTeamId(request.getTeamId());
        summary.setStartDate(request.getStartDate());
        summary.setEndDate(request.getEndDate());
        summary.setStatus(AISummary.SummaryStatus.GENERATING);
        summary.setPublished(false);
        summary.setVersion("1.0");
        summary.setCreatedBy(userId);
        summary.setCreatedAt(LocalDateTime.now());
        
        summaryRepository.insert(summary);
        return summary;
    }
    
    /**
     * Perform issue analysis
     */
    private IssueAnalysisResult performIssueAnalysis(AISummaryRequest request) {
        if (request.getTeamId() != null) {
            return issueAnalysisService.analyzeTeamIssues(request.getTeamId(), request.getStartDate(), request.getEndDate());
        } else {
            return issueAnalysisService.analyzeGlobalIssues(request.getStartDate(), request.getEndDate());
        }
    }
    
    /**
     * Generate AI content
     */
    private String generateAIContent(AISummaryRequest request, IssueAnalysisResult analysisResult) {
        String prompt = buildPrompt(request, analysisResult);
        
        try {
            // This would call the actual AI service
            // For now, return a placeholder
            return generatePlaceholderContent(request, analysisResult);
        } catch (Exception e) {
            log.error("Failed to generate AI content", e);
            return generateFallbackContent(request, analysisResult);
        }
    }
    
    /**
     * Build AI prompt
     */
    private String buildPrompt(AISummaryRequest request, IssueAnalysisResult analysisResult) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Generate a comprehensive code review summary report based on the following analysis:\n\n");
        
        // Add analysis data
        prompt.append("Analysis Period: ").append(request.getStartDate()).append(" to ").append(request.getEndDate()).append("\n");
        prompt.append("Total Issues: ").append(analysisResult.getTotalIssues()).append("\n");
        prompt.append("Resolution Rate: ").append(String.format("%.1f%%", analysisResult.getResolutionRate())).append("\n\n");
        
        // Add type distribution
        prompt.append("Issue Type Distribution:\n");
        analysisResult.getTypeDistribution().forEach((type, count) -> 
            prompt.append("- ").append(type.getDescription()).append(": ").append(count).append("\n"));
        
        // Add patterns
        if (!analysisResult.getPatterns().isEmpty()) {
            prompt.append("\nIdentified Patterns:\n");
            analysisResult.getPatterns().forEach(pattern -> 
                prompt.append("- ").append(pattern.getPatternName()).append(" (").append(pattern.getFrequency()).append(" occurrences)\n"));
        }
        
        // Add custom prompt if provided
        if (request.getCustomPrompt() != null && !request.getCustomPrompt().trim().isEmpty()) {
            prompt.append("\nAdditional Instructions: ").append(request.getCustomPrompt()).append("\n");
        }
        
        prompt.append("\nPlease provide:\n");
        prompt.append("1. Executive summary\n");
        prompt.append("2. Key findings and insights\n");
        prompt.append("3. Trend analysis\n");
        prompt.append("4. Actionable recommendations\n");
        prompt.append("5. Areas for improvement\n");
        
        return prompt.toString();
    }
    
    /**
     * Generate placeholder content
     */
    private String generatePlaceholderContent(AISummaryRequest request, IssueAnalysisResult analysisResult) {
        StringBuilder content = new StringBuilder();
        
        content.append("# Code Review Summary Report\n\n");
        content.append("**Analysis Period:** ").append(request.getStartDate()).append(" to ").append(request.getEndDate()).append("\n\n");
        
        content.append("## Executive Summary\n\n");
        content.append("During the analysis period, a total of ").append(analysisResult.getTotalIssues())
               .append(" issues were identified with a resolution rate of ")
               .append(String.format("%.1f%%", analysisResult.getResolutionRate())).append(".\n\n");
        
        content.append("## Key Findings\n\n");
        content.append("### Issue Distribution\n");
        analysisResult.getTypeDistribution().forEach((type, count) -> 
            content.append("- **").append(type.getDescription()).append(":** ").append(count).append(" issues\n"));
        
        if (!analysisResult.getPatterns().isEmpty()) {
            content.append("\n### Identified Patterns\n");
            analysisResult.getPatterns().forEach(pattern -> 
                content.append("- **").append(pattern.getPatternName()).append(":** ")
                       .append(pattern.getFrequency()).append(" occurrences\n"));
        }
        
        content.append("\n## Trend Analysis\n\n");
        content.append("The overall trend shows: ").append(analysisResult.getTrendAnalysis().getSummary()).append("\n\n");
        
        content.append("## Recommendations\n\n");
        content.append("Based on the analysis, the following actions are recommended:\n");
        content.append("1. Focus on addressing critical and major issues first\n");
        content.append("2. Implement preventive measures for identified patterns\n");
        content.append("3. Strengthen code review processes\n");
        content.append("4. Provide targeted training for common issue types\n");
        
        return content.toString();
    }
    
    /**
     * Generate fallback content
     */
    private String generateFallbackContent(AISummaryRequest request, IssueAnalysisResult analysisResult) {
        return "# Code Review Summary\n\n" +
               "Analysis completed for period: " + request.getStartDate() + " to " + request.getEndDate() + "\n\n" +
               "Total Issues: " + analysisResult.getTotalIssues() + "\n" +
               "Resolution Rate: " + String.format("%.1f%%", analysisResult.getResolutionRate()) + "\n\n" +
               "Please refer to the detailed analysis results for more information.";
    }
    
    /**
     * Extract key insights
     */
    private List<String> extractKeyInsights(IssueAnalysisResult analysisResult) {
        return Arrays.asList(
            "Total of " + analysisResult.getTotalIssues() + " issues identified",
            "Resolution rate of " + String.format("%.1f%%", analysisResult.getResolutionRate()),
            analysisResult.getPatterns().size() + " patterns identified",
            "Trend direction: " + analysisResult.getTrendAnalysis().getOverallDirection().getDescription()
        );
    }
    
    /**
     * Extract recommendations
     */
    private List<String> extractRecommendations(IssueAnalysisResult analysisResult) {
        return Arrays.asList(
            "Prioritize resolution of critical issues",
            "Implement preventive measures for common patterns",
            "Strengthen code review processes",
            "Provide targeted training for frequent issue types"
        );
    }
    
    /**
     * Update summary with results
     */
    private void updateSummaryWithResults(AISummary summary, String content, IssueAnalysisResult analysisResult, 
                                        List<String> keyInsights, List<String> recommendations, long startTime) {
        try {
            summary.setContent(content);
            summary.setKeyInsights(objectMapper.writeValueAsString(keyInsights));
            summary.setRecommendations(objectMapper.writeValueAsString(recommendations));
            
            // Build statistics
            AISummaryResponse.SummaryStatistics stats = AISummaryResponse.SummaryStatistics.builder()
                .totalIssues(analysisResult.getTotalIssues())
                .resolvedIssues(analysisResult.getResolvedIssues())
                .resolutionRate(analysisResult.getResolutionRate())
                .criticalIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.CRITICAL, 0L))
                .majorIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.MAJOR, 0L))
                .minorIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.MINOR, 0L))
                .suggestionIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.SUGGESTION, 0L))
                .patternsIdentified(analysisResult.getPatterns().size())
                .clustersFound(analysisResult.getClusters().size())
                .build();
            
            summary.setStatistics(objectMapper.writeValueAsString(stats));
            summary.setStatus(AISummary.SummaryStatus.COMPLETED);
            summary.setAiModel("gpt-3.5-turbo");
            summary.setConfidence(0.85);
            summary.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            summary.setUpdatedAt(LocalDateTime.now());
            
            summaryRepository.updateById(summary);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize summary data", e);
            summary.setStatus(AISummary.SummaryStatus.FAILED);
            summaryRepository.updateById(summary);
        }
    }
    
    /**
     * Build summary response
     */
    private AISummaryResponse buildSummaryResponse(AISummary summary, IssueAnalysisResult analysisResult, 
                                                 List<String> keyInsights, List<String> recommendations, 
                                                 Long userId, long startTime) {
        return AISummaryResponse.builder()
            .summaryId(summary.getId())
            .title(summary.getTitle())
            .content(summary.getContent())
            .summaryType(summary.getSummaryType())
            .teamId(summary.getTeamId())
            .startDate(summary.getStartDate())
            .endDate(summary.getEndDate())
            .analysisResult(analysisResult)
            .keyInsights(keyInsights)
            .recommendations(recommendations)
            .statistics(buildStatistics(analysisResult))
            .metadata(buildMetadata(userId, startTime))
            .build();
    }
    
    /**
     * Build statistics
     */
    private AISummaryResponse.SummaryStatistics buildStatistics(IssueAnalysisResult analysisResult) {
        return AISummaryResponse.SummaryStatistics.builder()
            .totalIssues(analysisResult.getTotalIssues())
            .resolvedIssues(analysisResult.getResolvedIssues())
            .resolutionRate(analysisResult.getResolutionRate())
            .criticalIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.CRITICAL, 0L))
            .majorIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.MAJOR, 0L))
            .minorIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.MINOR, 0L))
            .suggestionIssues(analysisResult.getSeverityDistribution().getOrDefault(com.company.codereview.common.enums.Severity.SUGGESTION, 0L))
            .patternsIdentified(analysisResult.getPatterns().size())
            .clustersFound(analysisResult.getClusters().size())
            .build();
    }
    
    /**
     * Build metadata
     */
    private AISummaryResponse.GenerationMetadata buildMetadata(Long userId, long startTime) {
        return AISummaryResponse.GenerationMetadata.builder()
            .generatedAt(LocalDateTime.now())
            .generatedBy(userId)
            .aiModel("gpt-3.5-turbo")
            .confidence(0.85)
            .processingTimeMs(System.currentTimeMillis() - startTime)
            .version("1.0")
            .build();
    }
    
    /**
     * Convert entity to response
     */
    private AISummaryResponse convertToResponse(AISummary summary) {
        try {
            List<String> keyInsights = summary.getKeyInsights() != null ? 
                objectMapper.readValue(summary.getKeyInsights(), List.class) : Arrays.asList();
            List<String> recommendations = summary.getRecommendations() != null ? 
                objectMapper.readValue(summary.getRecommendations(), List.class) : Arrays.asList();
            AISummaryResponse.SummaryStatistics statistics = summary.getStatistics() != null ? 
                objectMapper.readValue(summary.getStatistics(), AISummaryResponse.SummaryStatistics.class) : null;
            
            return AISummaryResponse.builder()
                .summaryId(summary.getId())
                .title(summary.getTitle())
                .content(summary.getContent())
                .summaryType(summary.getSummaryType())
                .teamId(summary.getTeamId())
                .startDate(summary.getStartDate())
                .endDate(summary.getEndDate())
                .keyInsights(keyInsights)
                .recommendations(recommendations)
                .statistics(statistics)
                .metadata(AISummaryResponse.GenerationMetadata.builder()
                    .generatedAt(summary.getCreatedAt())
                    .generatedBy(summary.getCreatedBy())
                    .aiModel(summary.getAiModel())
                    .confidence(summary.getConfidence())
                    .processingTimeMs(summary.getProcessingTimeMs())
                    .version(summary.getVersion())
                    .build())
                .build();
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize summary data", e);
            throw new RuntimeException("Failed to convert summary", e);
        }
    }
    
    /**
     * Generate summary title
     */
    private String generateSummaryTitle(AISummaryRequest request) {
        return request.getSummaryType().getDescription() + " - " + 
               request.getStartDate() + " to " + request.getEndDate();
    }
    
    /**
     * Find improvements between summaries
     */
    private List<String> findImprovements(AISummary current, AISummary previous) {
        // Placeholder implementation
        return Arrays.asList("Improved resolution rate", "Reduced critical issues");
    }
    
    /**
     * Find regressions between summaries
     */
    private List<String> findRegressions(AISummary current, AISummary previous) {
        // Placeholder implementation
        return Arrays.asList("Increased minor issues", "New pattern emerged");
    }
    
    /**
     * Find new patterns between summaries
     */
    private List<String> findNewPatterns(AISummary current, AISummary previous) {
        // Placeholder implementation
        return Arrays.asList("New null pointer pattern", "Performance degradation pattern");
    }
    
    /**
     * Summary Comparison DTO
     */
    @Data
    @Builder
    public static class SummaryComparison {
        private AISummaryResponse currentSummary;
        private AISummaryResponse previousSummary;
        private List<String> improvements;
        private List<String> regressions;
        private List<String> newPatterns;
    }
}