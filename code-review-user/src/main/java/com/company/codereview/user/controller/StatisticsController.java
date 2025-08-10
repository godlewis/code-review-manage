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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                // 假设UserDetails实现类中有getUserId方法，或者从用户名解析ID
                // 这里先使用用户名作为临时方案
                String username = userDetails.getUsername();
                // TODO: 根据用户名查询用户ID，这里暂时返回固定值
                return 1L;
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
        }
        return 1L; // 默认返回固定值
    }
    
    private byte[] generatePersonalExcelReport(PersonalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("个人统计报表");
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("个人代码评审统计报表");
            
            // 创建时间范围行
            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("统计时间范围：");
            dateRow.createCell(1).setCellValue(startDate + " 至 " + endDate);
            
            // 创建数据行
            int rowNum = 3;
            
            // 基础统计数据
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("统计项目");
            headerRow.createCell(1).setCellValue("数值");
            headerRow.createCell(2).setCellValue("说明");
            
            // 评审完成率
            Row completionRow = sheet.createRow(rowNum++);
            completionRow.createCell(0).setCellValue("评审完成率");
            completionRow.createCell(1).setCellValue(String.format("%.2f%%", statistics.getCompletionRate() * 100));
            completionRow.createCell(2).setCellValue("已完成评审数量 / 分配评审数量");
            
            // 问题发现数量
            Row issuesRow = sheet.createRow(rowNum++);
            issuesRow.createCell(0).setCellValue("问题发现数量");
            issuesRow.createCell(1).setCellValue(statistics.getIssuesFound());
            issuesRow.createCell(2).setCellValue("评审过程中发现的问题总数");
            
            // 整改及时率
            Row fixRow = sheet.createRow(rowNum++);
            fixRow.createCell(0).setCellValue("整改及时率");
            fixRow.createCell(1).setCellValue(String.format("%.2f%%", statistics.getFixTimeliness() * 100));
            fixRow.createCell(2).setCellValue("按时完成整改的问题比例");
            
            // 平均评审分数
            Row scoreRow = sheet.createRow(rowNum++);
            scoreRow.createCell(0).setCellValue("平均评审分数");
            scoreRow.createCell(1).setCellValue(String.format("%.2f", statistics.getAverageReviewScore()));
            scoreRow.createCell(2).setCellValue("评审给出的平均分数（1-10分）");
            
            // 总评审次数
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(0).setCellValue("总评审次数");
            totalRow.createCell(1).setCellValue(statistics.getTotalReviews());
            totalRow.createCell(2).setCellValue("参与的评审总次数");
            
            // 问题类型分布
            if (statistics.getIssueTypeDistribution() != null && !statistics.getIssueTypeDistribution().isEmpty()) {
                rowNum++; // 空行
                Row typeHeaderRow = sheet.createRow(rowNum++);
                typeHeaderRow.createCell(0).setCellValue("问题类型分布");
                
                for (Map.Entry<String, Long> entry : statistics.getIssueTypeDistribution().entrySet()) {
                    Row typeRow = sheet.createRow(rowNum++);
                    typeRow.createCell(0).setCellValue(entry.getKey());
                    typeRow.createCell(1).setCellValue(entry.getValue());
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("生成个人Excel报表失败", e);
            return new byte[0];
        }
    }
    
    private byte[] generatePersonalPdfReport(PersonalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            // 设置中文字体
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font headerFont = new Font(baseFont, 14, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            
            // 添加标题
            Paragraph title = new Paragraph("个人代码评审统计报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            
            // 添加时间范围
            Paragraph dateRange = new Paragraph("统计时间范围：" + startDate + " 至 " + endDate, normalFont);
            dateRange.setAlignment(Element.ALIGN_CENTER);
            document.add(dateRange);
            document.add(new Paragraph(" "));
            
            // 创建统计数据表格
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 4});
            
            // 表头
            PdfPCell headerCell1 = new PdfPCell(new Phrase("统计项目", headerFont));
            PdfPCell headerCell2 = new PdfPCell(new Phrase("数值", headerFont));
            PdfPCell headerCell3 = new PdfPCell(new Phrase("说明", headerFont));
            headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell1);
            table.addCell(headerCell2);
            table.addCell(headerCell3);
            
            // 数据行
            table.addCell(new PdfPCell(new Phrase("评审完成率", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", statistics.getCompletionRate() * 100), normalFont)));
            table.addCell(new PdfPCell(new Phrase("已完成评审数量 / 分配评审数量", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("问题发现数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getIssuesFound()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("评审过程中发现的问题总数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("整改及时率", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", statistics.getFixTimeliness() * 100), normalFont)));
            table.addCell(new PdfPCell(new Phrase("按时完成整改的问题比例", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("平均评审分数", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", statistics.getAverageReviewScore()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("评审给出的平均分数（1-10分）", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("总评审次数", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getTotalReviews()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("参与的评审总次数", normalFont)));
            
            document.add(table);
            
            // 添加问题类型分布
            if (statistics.getIssueTypeDistribution() != null && !statistics.getIssueTypeDistribution().isEmpty()) {
                document.add(new Paragraph(" "));
                Paragraph typeTitle = new Paragraph("问题类型分布", headerFont);
                document.add(typeTitle);
                
                PdfPTable typeTable = new PdfPTable(2);
                typeTable.setWidthPercentage(60);
                typeTable.addCell(new PdfPCell(new Phrase("问题类型", headerFont)));
                typeTable.addCell(new PdfPCell(new Phrase("数量", headerFont)));
                
                for (Map.Entry<String, Long> entry : statistics.getIssueTypeDistribution().entrySet()) {
                    typeTable.addCell(new PdfPCell(new Phrase(entry.getKey(), normalFont)));
                    typeTable.addCell(new PdfPCell(new Phrase(String.valueOf(entry.getValue()), normalFont)));
                }
                
                document.add(typeTable);
            }
            
            document.close();
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("生成个人PDF报表失败", e);
            return new byte[0];
        }
    }
    
    private byte[] generateTeamExcelReport(TeamStatistics statistics, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("团队统计报表");
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("团队代码评审统计报表");
            
            // 创建时间范围行
            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("统计时间范围：");
            dateRow.createCell(1).setCellValue(startDate + " 至 " + endDate);
            
            // 创建数据行
            int rowNum = 3;
            
            // 基础统计数据
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("统计项目");
            headerRow.createCell(1).setCellValue("数值");
            headerRow.createCell(2).setCellValue("说明");
            
            // 团队评审覆盖率
            Row coverageRow = sheet.createRow(rowNum++);
            coverageRow.createCell(0).setCellValue("评审覆盖率");
            coverageRow.createCell(1).setCellValue(String.format("%.2f%%", statistics.getCoverageRate() * 100));
            coverageRow.createCell(2).setCellValue("参与评审的成员比例");
            
            // 团队平均分数
            Row scoreRow = sheet.createRow(rowNum++);
            scoreRow.createCell(0).setCellValue("团队平均分数");
            scoreRow.createCell(1).setCellValue(String.format("%.2f", statistics.getAverageScore()));
            scoreRow.createCell(2).setCellValue("团队评审的平均分数");
            
            // 总问题数量
            Row totalIssuesRow = sheet.createRow(rowNum++);
            totalIssuesRow.createCell(0).setCellValue("总问题数量");
            totalIssuesRow.createCell(1).setCellValue(statistics.getTotalIssues());
            totalIssuesRow.createCell(2).setCellValue("团队发现的问题总数");
            
            // 问题解决率
            Row resolutionRow = sheet.createRow(rowNum++);
            resolutionRow.createCell(0).setCellValue("问题解决率");
            resolutionRow.createCell(1).setCellValue(String.format("%.2f%%", statistics.getResolutionRate() * 100));
            resolutionRow.createCell(2).setCellValue("已解决问题 / 总问题数量");
            
            // 团队成员数量
            Row memberRow = sheet.createRow(rowNum++);
            memberRow.createCell(0).setCellValue("团队成员数量");
            memberRow.createCell(1).setCellValue(statistics.getMemberCount());
            memberRow.createCell(2).setCellValue("团队总成员数");
            
            // 活跃成员数量
            Row activeRow = sheet.createRow(rowNum++);
            activeRow.createCell(0).setCellValue("活跃成员数量");
            activeRow.createCell(1).setCellValue(statistics.getActiveMemberCount());
            activeRow.createCell(2).setCellValue("在统计期间有评审活动的成员数");
            
            // 问题类型分布
            if (statistics.getIssueDistribution() != null && !statistics.getIssueDistribution().isEmpty()) {
                rowNum++; // 空行
                Row typeHeaderRow = sheet.createRow(rowNum++);
                typeHeaderRow.createCell(0).setCellValue("问题类型分布");
                
                for (Map.Entry<String, Long> entry : statistics.getIssueDistribution().entrySet()) {
                    Row typeRow = sheet.createRow(rowNum++);
                    typeRow.createCell(0).setCellValue(entry.getKey());
                    typeRow.createCell(1).setCellValue(entry.getValue());
                }
            }
            
            // 成员表现排名
            if (statistics.getMemberRankings() != null && !statistics.getMemberRankings().isEmpty()) {
                rowNum++; // 空行
                Row rankHeaderRow = sheet.createRow(rowNum++);
                rankHeaderRow.createCell(0).setCellValue("成员表现排名");
                
                Row rankTitleRow = sheet.createRow(rowNum++);
                rankTitleRow.createCell(0).setCellValue("排名");
                rankTitleRow.createCell(1).setCellValue("姓名");
                rankTitleRow.createCell(2).setCellValue("完成率");
                rankTitleRow.createCell(3).setCellValue("发现问题数");
                rankTitleRow.createCell(4).setCellValue("平均分数");
                rankTitleRow.createCell(5).setCellValue("综合评分");
                
                for (MemberPerformance member : statistics.getMemberRankings()) {
                    Row memberRow2 = sheet.createRow(rowNum++);
                    memberRow2.createCell(0).setCellValue(member.getRank());
                    memberRow2.createCell(1).setCellValue(member.getRealName());
                    memberRow2.createCell(2).setCellValue(String.format("%.2f%%", member.getCompletionRate() * 100));
                    memberRow2.createCell(3).setCellValue(member.getIssuesFound());
                    memberRow2.createCell(4).setCellValue(String.format("%.2f", member.getAverageScore()));
                    memberRow2.createCell(5).setCellValue(String.format("%.2f", member.getOverallScore()));
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("生成团队Excel报表失败", e);
            return new byte[0];
        }
    }
    
    private byte[] generateTeamPdfReport(TeamStatistics statistics, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            // 设置中文字体
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font headerFont = new Font(baseFont, 14, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            
            // 添加标题
            Paragraph title = new Paragraph("团队代码评审统计报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            
            // 添加时间范围
            Paragraph dateRange = new Paragraph("统计时间范围：" + startDate + " 至 " + endDate, normalFont);
            dateRange.setAlignment(Element.ALIGN_CENTER);
            document.add(dateRange);
            document.add(new Paragraph(" "));
            
            // 创建统计数据表格
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 4});
            
            // 表头
            PdfPCell headerCell1 = new PdfPCell(new Phrase("统计项目", headerFont));
            PdfPCell headerCell2 = new PdfPCell(new Phrase("数值", headerFont));
            PdfPCell headerCell3 = new PdfPCell(new Phrase("说明", headerFont));
            headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell1);
            table.addCell(headerCell2);
            table.addCell(headerCell3);
            
            // 数据行
            table.addCell(new PdfPCell(new Phrase("评审覆盖率", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", statistics.getCoverageRate() * 100), normalFont)));
            table.addCell(new PdfPCell(new Phrase("参与评审的成员比例", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("团队平均分数", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f", statistics.getAverageScore()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("团队评审的平均分数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("总问题数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getTotalIssues()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("团队发现的问题总数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("问题解决率", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", statistics.getResolutionRate() * 100), normalFont)));
            table.addCell(new PdfPCell(new Phrase("已解决问题 / 总问题数量", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("团队成员数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getMemberCount()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("团队总成员数", normalFont)));
            
            document.add(table);
            
            // 添加成员表现排名
            if (statistics.getMemberRankings() != null && !statistics.getMemberRankings().isEmpty()) {
                document.add(new Paragraph(" "));
                Paragraph rankTitle = new Paragraph("成员表现排名", headerFont);
                document.add(rankTitle);
                
                PdfPTable rankTable = new PdfPTable(5);
                rankTable.setWidthPercentage(100);
                rankTable.addCell(new PdfPCell(new Phrase("排名", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("姓名", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("完成率", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("发现问题数", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("综合评分", headerFont)));
                
                for (MemberPerformance member : statistics.getMemberRankings()) {
                    rankTable.addCell(new PdfPCell(new Phrase(String.valueOf(member.getRank()), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(member.getRealName(), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", member.getCompletionRate() * 100), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(String.valueOf(member.getIssuesFound()), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(String.format("%.2f", member.getOverallScore()), normalFont)));
                }
                
                document.add(rankTable);
            }
            
            document.close();
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("生成团队PDF报表失败", e);
            return new byte[0];
        }
    }
    
    private byte[] generateGlobalExcelReport(GlobalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 创建工作表
            Sheet sheet = workbook.createSheet("全局统计报表");
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("系统全局代码评审统计报表");
            
            // 创建时间范围行
            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("统计时间范围：");
            dateRow.createCell(1).setCellValue(startDate + " 至 " + endDate);
            
            // 创建数据行
            int rowNum = 3;
            
            // 基础统计数据
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("统计项目");
            headerRow.createCell(1).setCellValue("数值");
            headerRow.createCell(2).setCellValue("说明");
            
            // 总团队数量
            Row teamsRow = sheet.createRow(rowNum++);
            teamsRow.createCell(0).setCellValue("总团队数量");
            teamsRow.createCell(1).setCellValue(statistics.getTotalTeams());
            teamsRow.createCell(2).setCellValue("系统中的团队总数");
            
            // 总用户数量
            Row usersRow = sheet.createRow(rowNum++);
            usersRow.createCell(0).setCellValue("总用户数量");
            usersRow.createCell(1).setCellValue(statistics.getTotalUsers());
            usersRow.createCell(2).setCellValue("系统中的用户总数");
            
            // 活跃用户数量
            Row activeUsersRow = sheet.createRow(rowNum++);
            activeUsersRow.createCell(0).setCellValue("活跃用户数量");
            activeUsersRow.createCell(1).setCellValue(statistics.getActiveUsers());
            activeUsersRow.createCell(2).setCellValue("在统计期间有活动的用户数");
            
            // 总评审数量
            Row reviewsRow = sheet.createRow(rowNum++);
            reviewsRow.createCell(0).setCellValue("总评审数量");
            reviewsRow.createCell(1).setCellValue(statistics.getTotalReviews());
            reviewsRow.createCell(2).setCellValue("系统中的评审总数");
            
            // 总问题数量
            Row issuesRow = sheet.createRow(rowNum++);
            issuesRow.createCell(0).setCellValue("总问题数量");
            issuesRow.createCell(1).setCellValue(statistics.getTotalIssues());
            issuesRow.createCell(2).setCellValue("系统中发现的问题总数");
            
            // 全局问题解决率
            Row resolutionRow = sheet.createRow(rowNum++);
            resolutionRow.createCell(0).setCellValue("全局问题解决率");
            resolutionRow.createCell(1).setCellValue(String.format("%.2f%%", statistics.getGlobalResolutionRate() * 100));
            resolutionRow.createCell(2).setCellValue("全系统问题解决比例");
            
            // 全局平均评审分数
            Row scoreRow = sheet.createRow(rowNum++);
            scoreRow.createCell(0).setCellValue("全局平均评审分数");
            scoreRow.createCell(1).setCellValue(String.format("%.2f", statistics.getGlobalAverageScore()));
            scoreRow.createCell(2).setCellValue("全系统评审的平均分数");
            
            // 跨团队问题分布
            if (statistics.getCrossTeamIssueDistribution() != null && !statistics.getCrossTeamIssueDistribution().isEmpty()) {
                rowNum++; // 空行
                Row typeHeaderRow = sheet.createRow(rowNum++);
                typeHeaderRow.createCell(0).setCellValue("跨团队问题类型分布");
                
                for (Map.Entry<String, Long> entry : statistics.getCrossTeamIssueDistribution().entrySet()) {
                    Row typeRow = sheet.createRow(rowNum++);
                    typeRow.createCell(0).setCellValue(entry.getKey());
                    typeRow.createCell(1).setCellValue(entry.getValue());
                }
            }
            
            // 团队表现排名
            if (statistics.getTeamRankings() != null && !statistics.getTeamRankings().isEmpty()) {
                rowNum++; // 空行
                Row rankHeaderRow = sheet.createRow(rowNum++);
                rankHeaderRow.createCell(0).setCellValue("团队表现排名");
                
                Row rankTitleRow = sheet.createRow(rowNum++);
                rankTitleRow.createCell(0).setCellValue("排名");
                rankTitleRow.createCell(1).setCellValue("团队名称");
                rankTitleRow.createCell(2).setCellValue("覆盖率");
                rankTitleRow.createCell(3).setCellValue("平均分数");
                rankTitleRow.createCell(4).setCellValue("解决率");
                rankTitleRow.createCell(5).setCellValue("成员数");
                rankTitleRow.createCell(6).setCellValue("综合评分");
                
                for (TeamPerformance team : statistics.getTeamRankings()) {
                    Row teamRow = sheet.createRow(rowNum++);
                    teamRow.createCell(0).setCellValue(team.getRank());
                    teamRow.createCell(1).setCellValue(team.getTeamName());
                    teamRow.createCell(2).setCellValue(String.format("%.2f%%", team.getCoverageRate() * 100));
                    teamRow.createCell(3).setCellValue(String.format("%.2f", team.getAverageScore()));
                    teamRow.createCell(4).setCellValue(String.format("%.2f%%", team.getResolutionRate() * 100));
                    teamRow.createCell(5).setCellValue(team.getMemberCount());
                    teamRow.createCell(6).setCellValue(String.format("%.2f", team.getOverallScore()));
                }
            }
            
            // 最佳实践团队
            if (statistics.getBestPracticeTeams() != null && !statistics.getBestPracticeTeams().isEmpty()) {
                rowNum++; // 空行
                Row bestHeaderRow = sheet.createRow(rowNum++);
                bestHeaderRow.createCell(0).setCellValue("最佳实践团队");
                
                Row bestTitleRow = sheet.createRow(rowNum++);
                bestTitleRow.createCell(0).setCellValue("团队名称");
                bestTitleRow.createCell(1).setCellValue("最佳实践类型");
                bestTitleRow.createCell(2).setCellValue("关键指标");
                bestTitleRow.createCell(3).setCellValue("实践描述");
                
                for (BestPracticeTeam team : statistics.getBestPracticeTeams()) {
                    Row bestRow = sheet.createRow(rowNum++);
                    bestRow.createCell(0).setCellValue(team.getTeamName());
                    bestRow.createCell(1).setCellValue(team.getPracticeType());
                    bestRow.createCell(2).setCellValue(team.getKeyMetric());
                    bestRow.createCell(3).setCellValue(team.getDescription());
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("生成全局Excel报表失败", e);
            return new byte[0];
        }
    }
    
    private byte[] generateGlobalPdfReport(GlobalStatistics statistics, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            
            // 设置中文字体
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font headerFont = new Font(baseFont, 14, Font.BOLD);
            Font normalFont = new Font(baseFont, 12, Font.NORMAL);
            
            // 添加标题
            Paragraph title = new Paragraph("系统全局代码评审统计报表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            
            // 添加时间范围
            Paragraph dateRange = new Paragraph("统计时间范围：" + startDate + " 至 " + endDate, normalFont);
            dateRange.setAlignment(Element.ALIGN_CENTER);
            document.add(dateRange);
            document.add(new Paragraph(" "));
            
            // 创建统计数据表格
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2, 4});
            
            // 表头
            PdfPCell headerCell1 = new PdfPCell(new Phrase("统计项目", headerFont));
            PdfPCell headerCell2 = new PdfPCell(new Phrase("数值", headerFont));
            PdfPCell headerCell3 = new PdfPCell(new Phrase("说明", headerFont));
            headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell3.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell1);
            table.addCell(headerCell2);
            table.addCell(headerCell3);
            
            // 数据行
            table.addCell(new PdfPCell(new Phrase("总团队数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getTotalTeams()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("系统中的团队总数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("总用户数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getTotalUsers()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("系统中的用户总数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("活跃用户数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getActiveUsers()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("在统计期间有活动的用户数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("总评审数量", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(statistics.getTotalReviews()), normalFont)));
            table.addCell(new PdfPCell(new Phrase("系统中的评审总数", normalFont)));
            
            table.addCell(new PdfPCell(new Phrase("全局问题解决率", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.format("%.2f%%", statistics.getGlobalResolutionRate() * 100), normalFont)));
            table.addCell(new PdfPCell(new Phrase("全系统问题解决比例", normalFont)));
            
            document.add(table);
            
            // 添加团队表现排名
            if (statistics.getTeamRankings() != null && !statistics.getTeamRankings().isEmpty()) {
                document.add(new Paragraph(" "));
                Paragraph rankTitle = new Paragraph("团队表现排名", headerFont);
                document.add(rankTitle);
                
                PdfPTable rankTable = new PdfPTable(4);
                rankTable.setWidthPercentage(100);
                rankTable.addCell(new PdfPCell(new Phrase("排名", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("团队名称", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("平均分数", headerFont)));
                rankTable.addCell(new PdfPCell(new Phrase("综合评分", headerFont)));
                
                for (TeamPerformance team : statistics.getTeamRankings()) {
                    rankTable.addCell(new PdfPCell(new Phrase(String.valueOf(team.getRank()), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(team.getTeamName(), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(String.format("%.2f", team.getAverageScore()), normalFont)));
                    rankTable.addCell(new PdfPCell(new Phrase(String.format("%.2f", team.getOverallScore()), normalFont)));
                }
                
                document.add(rankTable);
            }
            
            document.close();
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("生成全局PDF报表失败", e);
            return new byte[0];
        }
    }
}