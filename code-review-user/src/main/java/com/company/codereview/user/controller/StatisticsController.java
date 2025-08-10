package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.*;
import com.company.codereview.user.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 统计分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "统计分析", description = "统计分析相关接口")
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * 获取个人统计数据
     */
    @GetMapping("/personal/{userId}")
    @Operation(summary = "获取个人统计数据", description = "获取指定用户的个人统计数据")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<PersonalStatistics> getPersonalStatistics(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取用户 {} 的个人统计数据，时间范围：{} 到 {}", userId, startDate, endDate);
        
        try {
            PersonalStatistics statistics = statisticsService.calculatePersonalStatistics(userId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取个人统计数据失败", e);
            return ResponseResult.error("获取个人统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的个人统计数据
     */
    @GetMapping("/personal/current")
    @Operation(summary = "获取当前用户统计数据", description = "获取当前登录用户的个人统计数据")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<PersonalStatistics> getCurrentUserStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // TODO: 从SecurityContext获取当前用户ID
        Long currentUserId = getCurrentUserId();
        log.info("获取当前用户 {} 的个人统计数据，时间范围：{} 到 {}", currentUserId, startDate, endDate);
        
        try {
            PersonalStatistics statistics = statisticsService.calculatePersonalStatistics(currentUserId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取当前用户统计数据失败", e);
            return ResponseResult.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取团队统计数据
     */
    @GetMapping("/team/{teamId}")
    @Operation(summary = "获取团队统计数据", description = "获取指定团队的统计数据")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<TeamStatistics> getTeamStatistics(
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取团队 {} 的统计数据，时间范围：{} 到 {}", teamId, startDate, endDate);
        
        try {
            TeamStatistics statistics = statisticsService.calculateTeamStatistics(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取团队统计数据失败", e);
            return ResponseResult.error("获取团队统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取全局统计数据
     */
    @GetMapping("/global")
    @Operation(summary = "获取全局统计数据", description = "获取系统全局统计数据")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<GlobalStatistics> getGlobalStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取全局统计数据，时间范围：{} 到 {}", startDate, endDate);
        
        try {
            GlobalStatistics statistics = statisticsService.calculateGlobalStatistics(startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取全局统计数据失败", e);
            return ResponseResult.error("获取全局统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取个人成长趋势
     */
    @GetMapping("/personal/{userId}/growth-trend")
    @Operation(summary = "获取个人成长趋势", description = "获取指定用户的成长趋势数据")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<PersonalStatistics> getPersonalGrowthTrend(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取用户 {} 的成长趋势，时间范围：{} 到 {}", userId, startDate, endDate);
        
        try {
            PersonalStatistics statistics = statisticsService.calculatePersonalStatistics(userId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取个人成长趋势失败", e);
            return ResponseResult.error("获取成长趋势失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取团队质量趋势
     */
    @GetMapping("/team/{teamId}/quality-trend")
    @Operation(summary = "获取团队质量趋势", description = "获取指定团队的质量趋势数据")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<TeamStatistics> getTeamQualityTrend(
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取团队 {} 的质量趋势，时间范围：{} 到 {}", teamId, startDate, endDate);
        
        try {
            TeamStatistics statistics = statisticsService.calculateTeamStatistics(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取团队质量趋势失败", e);
            return ResponseResult.error("获取质量趋势失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取团队成员表现排名
     */
    @GetMapping("/team/{teamId}/member-rankings")
    @Operation(summary = "获取团队成员排名", description = "获取指定团队的成员表现排名")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<TeamStatistics> getTeamMemberRankings(
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取团队 {} 的成员排名，时间范围：{} 到 {}", teamId, startDate, endDate);
        
        try {
            TeamStatistics statistics = statisticsService.calculateTeamStatistics(teamId, startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取团队成员排名失败", e);
            return ResponseResult.error("获取成员排名失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取系统使用趋势
     */
    @GetMapping("/global/usage-trend")
    @Operation(summary = "获取系统使用趋势", description = "获取系统使用趋势数据")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<GlobalStatistics> getSystemUsageTrend(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("获取系统使用趋势，时间范围：{} 到 {}", startDate, endDate);
        
        try {
            GlobalStatistics statistics = statisticsService.calculateGlobalStatistics(startDate, endDate);
            return ResponseResult.success(statistics);
        } catch (Exception e) {
            log.error("获取系统使用趋势失败", e);
            return ResponseResult.error("获取使用趋势失败：" + e.getMessage());
        }
    }
    
    /**
     * 导出个人统计报表
     */
    @GetMapping("/personal/{userId}/export")
    @Operation(summary = "导出个人统计报表", description = "导出指定用户的统计报表")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseEntity<byte[]> exportPersonalStatistics(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format) {
        
        log.info("导出用户 {} 的统计报表，格式：{}，时间范围：{} 到 {}", userId, format, startDate, endDate);
        
        try {
            PersonalStatistics statistics = statisticsService.calculatePersonalStatistics(userId, startDate, endDate);
            
            byte[] reportData;
            String contentType;
            String filename;
            
            if ("pdf".equalsIgnoreCase(format)) {
                reportData = generatePersonalPdfReport(statistics, startDate, endDate);
                contentType = "application/pdf";
                filename = String.format("personal_statistics_%d_%s_%s.pdf", 
                    userId, startDate.format(DateTimeFormatter.BASIC_ISO_DATE), 
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            } else {
                reportData = generatePersonalExcelReport(statistics, startDate, endDate);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = String.format("personal_statistics_%d_%s_%s.xlsx", 
                    userId, startDate.format(DateTimeFormatter.BASIC_ISO_DATE), 
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(reportData);
                    
        } catch (Exception e) {
            log.error("导出个人统计报表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 导出团队统计报表
     */
    @GetMapping("/team/{teamId}/export")
    @Operation(summary = "导出团队统计报表", description = "导出指定团队的统计报表")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseEntity<byte[]> exportTeamStatistics(
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format) {
        
        log.info("导出团队 {} 的统计报表，格式：{}，时间范围：{} 到 {}", teamId, format, startDate, endDate);
        
        try {
            TeamStatistics statistics = statisticsService.calculateTeamStatistics(teamId, startDate, endDate);
            
            byte[] reportData;
            String contentType;
            String filename;
            
            if ("pdf".equalsIgnoreCase(format)) {
                reportData = generateTeamPdfReport(statistics, startDate, endDate);
                contentType = "application/pdf";
                filename = String.format("team_statistics_%d_%s_%s.pdf", 
                    teamId, startDate.format(DateTimeFormatter.BASIC_ISO_DATE), 
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            } else {
                reportData = generateTeamExcelReport(statistics, startDate, endDate);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = String.format("team_statistics_%d_%s_%s.xlsx", 
                    teamId, startDate.format(DateTimeFormatter.BASIC_ISO_DATE), 
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(reportData);
                    
        } catch (Exception e) {
            log.error("导出团队统计报表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 导出全局统计报表
     */
    @GetMapping("/global/export")
    @Operation(summary = "导出全局统计报表", description = "导出系统全局统计报表")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseEntity<byte[]> exportGlobalStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "excel") String format) {
        
        log.info("导出全局统计报表，格式：{}，时间范围：{} 到 {}", format, startDate, endDate);
        
        try {
            GlobalStatistics statistics = statisticsService.calculateGlobalStatistics(startDate, endDate);
            
            byte[] reportData;
            String contentType;
            String filename;
            
            if ("pdf".equalsIgnoreCase(format)) {
                reportData = generateGlobalPdfReport(statistics, startDate, endDate);
                contentType = "application/pdf";
                filename = String.format("global_statistics_%s_%s.pdf", 
                    startDate.format(DateTimeFormatter.BASIC_ISO_DATE), 
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            } else {
                reportData = generateGlobalExcelReport(statistics, startDate, endDate);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = String.format("global_statistics_%s_%s.xlsx", 
                    startDate.format(DateTimeFormatter.BASIC_ISO_DATE), 
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE));
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(reportData);
                    
        } catch (Exception e) {
            log.error("导出全局统计报表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取统计数据概览
     */
    @GetMapping("/overview")
    @Operation(summary = "获取统计概览", description = "获取统计数据概览信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<StatisticsOverview> getStatisticsOverview() {
        log.info("获取统计数据概览");
        
        try {
            // 获取最近30天的数据作为概览
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            
            Long currentUserId = getCurrentUserId();
            PersonalStatistics personalStats = statisticsService.calculatePersonalStatistics(currentUserId, startDate, endDate);
            
            // TODO: 根据用户角色获取相应的团队或全局统计
            StatisticsOverview overview = StatisticsOverview.builder()
                    .personalStats(personalStats)
                    .build();
            
            return ResponseResult.success(overview);
        } catch (Exception e) {
            log.error("获取统计概览失败", e);
            return ResponseResult.error("获取统计概览失败：" + e.getMessage());
        }
    }
    
    // 私有辅助方法
    
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        // SecurityContext context = SecurityContextHolder.getContext();
        // Authentication authentication = context.getAuthentication();
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // return userDetails.getUserId();
        return 1L; // 临时返回固定值
    }
    
    private byte[] generatePersonalExcelReport(PersonalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现Excel报表生成逻辑
        // 使用Apache POI生成Excel文件
        return new byte[0];
    }
    
    private byte[] generatePersonalPdfReport(PersonalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现PDF报表生成逻辑
        // 使用iText或其他PDF库生成PDF文件
        return new byte[0];
    }
    
    private byte[] generateTeamExcelReport(TeamStatistics statistics, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现团队Excel报表生成逻辑
        return new byte[0];
    }
    
    private byte[] generateTeamPdfReport(TeamStatistics statistics, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现团队PDF报表生成逻辑
        return new byte[0];
    }
    
    private byte[] generateGlobalExcelReport(GlobalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现全局Excel报表生成逻辑
        return new byte[0];
    }
    
    private byte[] generateGlobalPdfReport(GlobalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现全局PDF报表生成逻辑
        return new byte[0];
    }
}