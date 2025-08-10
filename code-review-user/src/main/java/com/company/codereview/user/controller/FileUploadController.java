package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.FileUploadResult;
import com.company.codereview.user.entity.CodeScreenshot;
import com.company.codereview.user.service.FileManagementService;
import com.company.codereview.user.service.FileUploadProgressService;
import com.company.codereview.user.service.FileUploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Api(tags = "文件上传管理")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    private final FileManagementService fileManagementService;
    private final FileUploadProgressService progressService;
    
    /**
     * 上传代码截图
     */
    @PostMapping("/screenshots/upload")
    @ApiOperation("上传代码截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshot> uploadScreenshot(
            @ApiParam("截图文件") @RequestParam("file") MultipartFile file,
            @ApiParam("评审记录ID") @RequestParam("reviewRecordId") Long reviewRecordId,
            @ApiParam("截图描述") @RequestParam(value = "description", required = false) String description) {
        
        log.info("上传代码截图请求: reviewRecordId={}, fileName={}", reviewRecordId, file.getOriginalFilename());
        
        try {
            CodeScreenshot screenshot = fileManagementService.uploadAndSaveScreenshot(file, reviewRecordId, description);
            return ResponseResult.success(screenshot);
            
        } catch (Exception e) {
            log.error("上传代码截图失败: reviewRecordId={}, fileName={}, error={}", 
                reviewRecordId, file.getOriginalFilename(), e.getMessage(), e);
            return ResponseResult.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传代码截图
     */
    @PostMapping("/screenshots/batch-upload")
    @ApiOperation("批量上传代码截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<CodeScreenshot>> batchUploadScreenshots(
            @ApiParam("截图文件列表") @RequestParam("files") List<MultipartFile> files,
            @ApiParam("评审记录ID") @RequestParam("reviewRecordId") Long reviewRecordId) {
        
        log.info("批量上传代码截图请求: reviewRecordId={}, fileCount={}", reviewRecordId, files.size());
        
        try {
            List<CodeScreenshot> screenshots = fileManagementService.batchUploadScreenshots(files, reviewRecordId);
            return ResponseResult.success(screenshots);
            
        } catch (Exception e) {
            log.error("批量上传代码截图失败: reviewRecordId={}, fileCount={}, error={}", 
                reviewRecordId, files.size(), e.getMessage(), e);
            return ResponseResult.error("批量上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传文档文件
     */
    @PostMapping("/documents/upload")
    @ApiOperation("上传文档文件")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FileUploadResult> uploadDocument(
            @ApiParam("文档文件") @RequestParam("file") MultipartFile file,
            @ApiParam("文档分类") @RequestParam(value = "category", required = false) String category) {
        
        log.info("上传文档文件请求: fileName={}, category={}", file.getOriginalFilename(), category);
        
        try {
            FileUploadResult result = fileUploadService.uploadDocument(file, category);
            return ResponseResult.success(result);
            
        } catch (Exception e) {
            log.error("上传文档文件失败: fileName={}, error={}", file.getOriginalFilename(), e.getMessage(), e);
            return ResponseResult.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除截图文件
     */
    @DeleteMapping("/screenshots/{screenshotId}")
    @ApiOperation("删除截图文件")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteScreenshot(
            @ApiParam("截图ID") @PathVariable Long screenshotId) {
        
        log.info("删除截图文件请求: screenshotId={}", screenshotId);
        
        try {
            fileManagementService.deleteScreenshotWithFile(screenshotId);
            return ResponseResult.success();
            
        } catch (Exception e) {
            log.error("删除截图文件失败: screenshotId={}, error={}", screenshotId, e.getMessage(), e);
            return ResponseResult.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除截图文件
     */
    @DeleteMapping("/screenshots/batch")
    @ApiOperation("批量删除截图文件")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> batchDeleteScreenshots(
            @ApiParam("截图ID列表") @RequestBody List<Long> screenshotIds) {
        
        log.info("批量删除截图文件请求: screenshotIds={}", screenshotIds);
        
        try {
            fileManagementService.batchDeleteScreenshotsWithFiles(screenshotIds);
            return ResponseResult.success();
            
        } catch (Exception e) {
            log.error("批量删除截图文件失败: screenshotIds={}, error={}", screenshotIds, e.getMessage(), e);
            return ResponseResult.error("批量删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 替换截图文件
     */
    @PutMapping("/screenshots/{screenshotId}/replace")
    @ApiOperation("替换截图文件")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshot> replaceScreenshot(
            @ApiParam("截图ID") @PathVariable Long screenshotId,
            @ApiParam("新截图文件") @RequestParam("file") MultipartFile newFile) {
        
        log.info("替换截图文件请求: screenshotId={}, newFileName={}", screenshotId, newFile.getOriginalFilename());
        
        try {
            CodeScreenshot screenshot = fileManagementService.replaceScreenshotFile(screenshotId, newFile);
            return ResponseResult.success(screenshot);
            
        } catch (Exception e) {
            log.error("替换截图文件失败: screenshotId={}, error={}", screenshotId, e.getMessage(), e);
            return ResponseResult.error("替换失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件下载链接
     */
    @GetMapping("/screenshots/{screenshotId}/download-url")
    @ApiOperation("获取截图下载链接")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<String> getDownloadUrl(
            @ApiParam("截图ID") @PathVariable Long screenshotId) {
        
        log.info("获取截图下载链接请求: screenshotId={}", screenshotId);
        
        try {
            String downloadUrl = fileManagementService.getFileDownloadUrl(screenshotId);
            return ResponseResult.success(downloadUrl);
            
        } catch (Exception e) {
            log.error("获取截图下载链接失败: screenshotId={}, error={}", screenshotId, e.getMessage(), e);
            return ResponseResult.error("获取下载链接失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/download/{filePath:.+}")
    @ApiOperation("下载文件")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public void downloadFile(
            @ApiParam("文件路径") @PathVariable String filePath,
            HttpServletResponse response) {
        
        log.info("下载文件请求: filePath={}", filePath);
        
        try (InputStream inputStream = fileUploadService.getFileStream(filePath)) {
            
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + extractFileName(filePath) + "\"");
            
            // 复制文件流到响应
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
            
            log.info("文件下载成功: filePath={}", filePath);
            
        } catch (IOException e) {
            log.error("文件下载失败: filePath={}, error={}", filePath, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取上传进度
     */
    @GetMapping("/upload-progress/{uploadId}")
    @ApiOperation("获取上传进度")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FileUploadProgressService.UploadProgress> getUploadProgress(
            @ApiParam("上传ID") @PathVariable String uploadId) {
        
        FileUploadProgressService.UploadProgress progress = progressService.getProgress(uploadId);
        
        if (progress == null) {
            return ResponseResult.error("上传进度不存在");
        }
        
        return ResponseResult.success(progress);
    }
    
    /**
     * 取消上传
     */
    @PostMapping("/upload-progress/{uploadId}/cancel")
    @ApiOperation("取消上传")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> cancelUpload(
            @ApiParam("上传ID") @PathVariable String uploadId) {
        
        log.info("取消上传请求: uploadId={}", uploadId);
        
        progressService.markCancelled(uploadId);
        
        return ResponseResult.success();
    }
    
    /**
     * 获取上传统计信息
     */
    @GetMapping("/upload-statistics")
    @ApiOperation("获取上传统计信息")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<FileUploadProgressService.UploadStatistics> getUploadStatistics() {
        
        FileUploadProgressService.UploadStatistics statistics = progressService.getUploadStatistics();
        
        return ResponseResult.success(statistics);
    }
    
    /**
     * 检查文件完整性
     */
    @GetMapping("/screenshots/{screenshotId}/integrity")
    @ApiOperation("检查截图文件完整性")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Boolean> checkFileIntegrity(
            @ApiParam("截图ID") @PathVariable Long screenshotId) {
        
        log.info("检查文件完整性请求: screenshotId={}", screenshotId);
        
        try {
            boolean isIntact = fileManagementService.checkFileIntegrity(screenshotId);
            return ResponseResult.success(isIntact);
            
        } catch (Exception e) {
            log.error("检查文件完整性失败: screenshotId={}, error={}", screenshotId, e.getMessage(), e);
            return ResponseResult.error("检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文件路径中提取文件名
     */
    private String extractFileName(String filePath) {
        if (filePath == null || !filePath.contains("/")) {
            return filePath;
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}