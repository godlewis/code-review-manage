package com.company.codereview.user.service;

import com.company.codereview.user.dto.FileUploadResult;
import com.company.codereview.user.entity.CodeScreenshot;
import com.company.codereview.user.entity.ReviewRecord;
import com.company.codereview.user.exception.FileUploadException;
import com.company.codereview.user.repository.CodeScreenshotRepository;
import com.company.codereview.user.repository.ReviewRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理服务
 * 整合文件上传和数据库记录管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagementService {
    
    private final FileUploadService fileUploadService;
    private final CodeScreenshotService codeScreenshotService;
    private final ReviewRecordRepository reviewRecordRepository;
    private final CodeScreenshotRepository screenshotRepository;
    
    /**
     * 上传代码截图并保存记录
     */
    @Transactional
    public CodeScreenshot uploadAndSaveScreenshot(MultipartFile file, Long reviewRecordId, String description) {
        log.info("上传并保存代码截图: reviewRecordId={}, fileName={}", reviewRecordId, file.getOriginalFilename());
        
        try {
            // 验证评审记录是否存在
            ReviewRecord reviewRecord = reviewRecordRepository.selectById(reviewRecordId);
            if (reviewRecord == null) {
                throw new FileUploadException("评审记录不存在");
            }
            
            // 检查评审记录状态
            if (reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED) {
                throw new FileUploadException("已完成的评审记录不能上传截图");
            }
            
            // 上传文件
            FileUploadResult uploadResult = fileUploadService.uploadScreenshot(file, reviewRecordId);
            
            if (!uploadResult.getSuccess()) {
                throw new FileUploadException("文件上传失败: " + uploadResult.getErrorMessage());
            }
            
            // 创建截图记录
            CodeScreenshot screenshot = new CodeScreenshot();
            screenshot.setReviewRecordId(reviewRecordId);
            screenshot.setFileName(uploadResult.getOriginalFileName());
            screenshot.setFileUrl(uploadResult.getFileUrl());
            screenshot.setFileSize(uploadResult.getFileSize());
            screenshot.setFileType(uploadResult.getFileType());
            screenshot.setDescription(description);
            
            // 保存到数据库
            screenshot = codeScreenshotService.addScreenshot(screenshot);
            
            log.info("代码截图上传并保存成功: screenshotId={}, fileUrl={}", 
                screenshot.getId(), uploadResult.getFileUrl());
            
            return screenshot;
            
        } catch (Exception e) {
            log.error("上传并保存代码截图失败: reviewRecordId={}, fileName={}, error={}", 
                reviewRecordId, file.getOriginalFilename(), e.getMessage(), e);
            throw new FileUploadException("上传并保存截图失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量上传代码截图
     */
    @Transactional
    public List<CodeScreenshot> batchUploadScreenshots(List<MultipartFile> files, Long reviewRecordId) {
        log.info("批量上传代码截图: reviewRecordId={}, fileCount={}", reviewRecordId, files.size());
        
        List<CodeScreenshot> screenshots = new ArrayList<>();
        List<String> uploadedFilePaths = new ArrayList<>();
        
        try {
            // 验证评审记录
            ReviewRecord reviewRecord = reviewRecordRepository.selectById(reviewRecordId);
            if (reviewRecord == null) {
                throw new FileUploadException("评审记录不存在");
            }
            
            if (reviewRecord.getStatus() == ReviewRecord.ReviewStatus.COMPLETED) {
                throw new FileUploadException("已完成的评审记录不能上传截图");
            }
            
            // 逐个上传文件
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                
                try {
                    // 上传文件
                    FileUploadResult uploadResult = fileUploadService.uploadScreenshot(file, reviewRecordId);
                    
                    if (!uploadResult.getSuccess()) {
                        throw new FileUploadException("文件上传失败: " + uploadResult.getErrorMessage());
                    }
                    
                    uploadedFilePaths.add(uploadResult.getFilePath());
                    
                    // 创建截图记录
                    CodeScreenshot screenshot = new CodeScreenshot();
                    screenshot.setReviewRecordId(reviewRecordId);
                    screenshot.setFileName(uploadResult.getOriginalFileName());
                    screenshot.setFileUrl(uploadResult.getFileUrl());
                    screenshot.setFileSize(uploadResult.getFileSize());
                    screenshot.setFileType(uploadResult.getFileType());
                    screenshot.setSortOrder(i + 1);
                    
                    screenshots.add(screenshot);
                    
                } catch (Exception e) {
                    log.error("上传文件失败: fileName={}, error={}", file.getOriginalFilename(), e.getMessage());
                    // 如果某个文件上传失败，清理已上传的文件
                    cleanupUploadedFiles(uploadedFilePaths);
                    throw new FileUploadException("批量上传失败，文件: " + file.getOriginalFilename() + ", 错误: " + e.getMessage());
                }
            }
            
            // 批量保存截图记录
            codeScreenshotService.batchAddScreenshots(screenshots);
            
            log.info("批量上传代码截图成功: reviewRecordId={}, successCount={}", reviewRecordId, screenshots.size());
            return screenshots;
            
        } catch (Exception e) {
            // 清理已上传的文件
            cleanupUploadedFiles(uploadedFilePaths);
            log.error("批量上传代码截图失败: reviewRecordId={}, error={}", reviewRecordId, e.getMessage(), e);
            throw new FileUploadException("批量上传截图失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除截图及其文件
     */
    @Transactional
    public void deleteScreenshotWithFile(Long screenshotId) {
        log.info("删除截图及其文件: screenshotId={}", screenshotId);
        
        try {
            // 获取截图记录
            CodeScreenshot screenshot = codeScreenshotService.getScreenshotById(screenshotId);
            
            // 从文件路径中提取存储路径
            String filePath = extractFilePathFromUrl(screenshot.getFileUrl());
            
            // 删除数据库记录
            codeScreenshotService.deleteScreenshot(screenshotId);
            
            // 删除文件
            if (filePath != null && fileUploadService.fileExists(filePath)) {
                fileUploadService.deleteFile(filePath);
            }
            
            log.info("截图及其文件删除成功: screenshotId={}", screenshotId);
            
        } catch (Exception e) {
            log.error("删除截图及其文件失败: screenshotId={}, error={}", screenshotId, e.getMessage(), e);
            throw new FileUploadException("删除截图失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量删除截图及其文件
     */
    @Transactional
    public void batchDeleteScreenshotsWithFiles(List<Long> screenshotIds) {
        log.info("批量删除截图及其文件: screenshotIds={}", screenshotIds);
        
        List<String> filePaths = new ArrayList<>();
        
        try {
            // 收集所有文件路径
            for (Long screenshotId : screenshotIds) {
                try {
                    CodeScreenshot screenshot = codeScreenshotService.getScreenshotById(screenshotId);
                    String filePath = extractFilePathFromUrl(screenshot.getFileUrl());
                    if (filePath != null) {
                        filePaths.add(filePath);
                    }
                } catch (Exception e) {
                    log.warn("获取截图信息失败: screenshotId={}, error={}", screenshotId, e.getMessage());
                }
            }
            
            // 删除数据库记录
            codeScreenshotService.batchDeleteScreenshots(screenshotIds);
            
            // 批量删除文件
            if (!filePaths.isEmpty()) {
                fileUploadService.batchDeleteFiles(filePaths);
            }
            
            log.info("批量删除截图及其文件成功: count={}", screenshotIds.size());
            
        } catch (Exception e) {
            log.error("批量删除截图及其文件失败: screenshotIds={}, error={}", screenshotIds, e.getMessage(), e);
            throw new FileUploadException("批量删除截图失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 清理评审记录的所有截图文件
     */
    @Transactional
    public void cleanupReviewRecordFiles(Long reviewRecordId) {
        log.info("清理评审记录的所有截图文件: reviewRecordId={}", reviewRecordId);
        
        try {
            // 获取所有截图记录
            List<CodeScreenshot> screenshots = codeScreenshotService.getScreenshotsByReviewRecordId(reviewRecordId);
            
            List<String> filePaths = new ArrayList<>();
            for (CodeScreenshot screenshot : screenshots) {
                String filePath = extractFilePathFromUrl(screenshot.getFileUrl());
                if (filePath != null) {
                    filePaths.add(filePath);
                }
            }
            
            // 删除数据库记录
            codeScreenshotService.deleteScreenshotsByReviewRecordId(reviewRecordId);
            
            // 批量删除文件
            if (!filePaths.isEmpty()) {
                fileUploadService.batchDeleteFiles(filePaths);
            }
            
            log.info("清理评审记录截图文件成功: reviewRecordId={}, fileCount={}", reviewRecordId, filePaths.size());
            
        } catch (Exception e) {
            log.error("清理评审记录截图文件失败: reviewRecordId={}, error={}", reviewRecordId, e.getMessage(), e);
            throw new FileUploadException("清理截图文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 替换截图文件
     */
    @Transactional
    public CodeScreenshot replaceScreenshotFile(Long screenshotId, MultipartFile newFile) {
        log.info("替换截图文件: screenshotId={}, newFileName={}", screenshotId, newFile.getOriginalFilename());
        
        try {
            // 获取原截图记录
            CodeScreenshot screenshot = codeScreenshotService.getScreenshotById(screenshotId);
            String oldFilePath = extractFilePathFromUrl(screenshot.getFileUrl());
            
            // 上传新文件
            FileUploadResult uploadResult = fileUploadService.uploadScreenshot(newFile, screenshot.getReviewRecordId());
            
            if (!uploadResult.getSuccess()) {
                throw new FileUploadException("新文件上传失败: " + uploadResult.getErrorMessage());
            }
            
            // 更新截图记录
            screenshot.setFileName(uploadResult.getOriginalFileName());
            screenshot.setFileUrl(uploadResult.getFileUrl());
            screenshot.setFileSize(uploadResult.getFileSize());
            screenshot.setFileType(uploadResult.getFileType());
            
            screenshot = codeScreenshotService.updateScreenshot(screenshot);
            
            // 删除旧文件
            if (oldFilePath != null && fileUploadService.fileExists(oldFilePath)) {
                fileUploadService.deleteFile(oldFilePath);
            }
            
            log.info("截图文件替换成功: screenshotId={}, newFileUrl={}", screenshotId, uploadResult.getFileUrl());
            return screenshot;
            
        } catch (Exception e) {
            log.error("替换截图文件失败: screenshotId={}, error={}", screenshotId, e.getMessage(), e);
            throw new FileUploadException("替换截图文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从URL中提取文件路径
     */
    private String extractFilePathFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        try {
            // 处理预签名URL，提取对象路径
            if (fileUrl.contains("?")) {
                fileUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
            }
            
            // 简化处理，假设URL格式为 http://host:port/bucket/path
            // 查找bucket名称后的路径
            String bucketName = "codeview"; // 从配置中获取，这里简化处理
            int bucketIndex = fileUrl.indexOf("/" + bucketName + "/");
            if (bucketIndex != -1) {
                return fileUrl.substring(bucketIndex + bucketName.length() + 2);
            }
            
            // 备用方案：按/分割，取bucket后的部分
            String[] parts = fileUrl.split("/");
            if (parts.length >= 5) {
                // 跳过协议、空字符串、主机:端口、bucket名称
                StringBuilder pathBuilder = new StringBuilder();
                for (int i = 4; i < parts.length; i++) {
                    if (i > 4) pathBuilder.append("/");
                    pathBuilder.append(parts[i]);
                }
                return pathBuilder.toString();
            }
            
        } catch (Exception e) {
            log.warn("从URL提取文件路径失败: url={}, error={}", fileUrl, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 清理已上传的文件
     */
    private void cleanupUploadedFiles(List<String> filePaths) {
        if (filePaths.isEmpty()) {
            return;
        }
        
        log.info("清理已上传的文件: count={}", filePaths.size());
        
        for (String filePath : filePaths) {
            try {
                if (fileUploadService.fileExists(filePath)) {
                    fileUploadService.deleteFile(filePath);
                }
            } catch (Exception e) {
                log.warn("清理文件失败: filePath={}, error={}", filePath, e.getMessage());
            }
        }
    }
    
    /**
     * 获取文件下载链接
     */
    public String getFileDownloadUrl(Long screenshotId) {
        try {
            CodeScreenshot screenshot = codeScreenshotService.getScreenshotById(screenshotId);
            String filePath = extractFilePathFromUrl(screenshot.getFileUrl());
            
            if (filePath != null) {
                return fileUploadService.getDownloadUrl(filePath);
            }
            
            return screenshot.getFileUrl();
            
        } catch (Exception e) {
            log.error("获取文件下载链接失败: screenshotId={}, error={}", screenshotId, e.getMessage());
            throw new FileUploadException("获取下载链接失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件完整性
     */
    public boolean checkFileIntegrity(Long screenshotId) {
        try {
            CodeScreenshot screenshot = codeScreenshotService.getScreenshotById(screenshotId);
            String filePath = extractFilePathFromUrl(screenshot.getFileUrl());
            
            if (filePath == null) {
                return false;
            }
            
            return fileUploadService.fileExists(filePath);
            
        } catch (Exception e) {
            log.error("检查文件完整性失败: screenshotId={}, error={}", screenshotId, e.getMessage());
            return false;
        }
    }
}