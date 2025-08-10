package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.CodeScreenshotDTO;
import com.company.codereview.user.entity.CodeScreenshot;
import com.company.codereview.user.service.CodeScreenshotService;
import com.company.codereview.user.service.FileManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 代码截图控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/screenshots")
@RequiredArgsConstructor
@Validated
@Api(tags = "代码截图管理")
public class CodeScreenshotController {
    
    private final CodeScreenshotService codeScreenshotService;
    private final FileManagementService fileManagementService;
    
    /**
     * 上传代码截图
     */
    @PostMapping("/upload")
    @ApiOperation("上传代码截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshotDTO> uploadScreenshot(
            @RequestParam @ApiParam("文件") MultipartFile file,
            @RequestParam @ApiParam("评审记录ID") Long reviewRecordId,
            @RequestParam(required = false) @ApiParam("描述") String description) {
        
        log.info("上传代码截图: reviewRecordId={}, fileName={}", reviewRecordId, file.getOriginalFilename());
        
        CodeScreenshot screenshot = fileManagementService.uploadAndSaveScreenshot(file, reviewRecordId, description);
        
        return ResponseResult.success(convertToDTO(screenshot));
    }
    
    /**
     * 批量上传代码截图
     */
    @PostMapping("/batch-upload")
    @ApiOperation("批量上传代码截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<CodeScreenshotDTO>> batchUploadScreenshots(
            @RequestParam @ApiParam("文件列表") List<MultipartFile> files,
            @RequestParam @ApiParam("评审记录ID") Long reviewRecordId) {
        
        log.info("批量上传代码截图: reviewRecordId={}, fileCount={}", reviewRecordId, files.size());
        
        List<CodeScreenshot> screenshots = fileManagementService.batchUploadScreenshots(files, reviewRecordId);
        List<CodeScreenshotDTO> dtoList = screenshots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 添加截图记录
     */
    @PostMapping
    @ApiOperation("添加截图记录")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshotDTO> addScreenshot(@Valid @RequestBody CodeScreenshotDTO screenshotDTO) {
        
        log.info("添加截图记录: reviewRecordId={}, fileName={}", 
                screenshotDTO.getReviewRecordId(), screenshotDTO.getFileName());
        
        CodeScreenshot screenshot = convertToEntity(screenshotDTO);
        screenshot = codeScreenshotService.addScreenshot(screenshot);
        
        return ResponseResult.success(convertToDTO(screenshot));
    }
    
    /**
     * 更新截图信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新截图信息")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshotDTO> updateScreenshot(
            @PathVariable Long id,
            @Valid @RequestBody CodeScreenshotDTO screenshotDTO) {
        
        log.info("更新截图信息: id={}", id);
        
        screenshotDTO.setId(id);
        CodeScreenshot screenshot = convertToEntity(screenshotDTO);
        screenshot = codeScreenshotService.updateScreenshot(screenshot);
        
        return ResponseResult.success(convertToDTO(screenshot));
    }
    
    /**
     * 删除截图
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteScreenshot(@PathVariable Long id) {
        
        log.info("删除截图: id={}", id);
        
        fileManagementService.deleteScreenshotWithFile(id);
        
        return ResponseResult.success();
    }
    
    /**
     * 根据ID查询截图
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshotDTO> getScreenshot(@PathVariable Long id) {
        
        CodeScreenshot screenshot = codeScreenshotService.getScreenshotById(id);
        
        return ResponseResult.success(convertToDTO(screenshot));
    }
    
    /**
     * 根据评审记录ID查询截图列表
     */
    @GetMapping("/review-record/{reviewRecordId}")
    @ApiOperation("根据评审记录ID查询截图列表")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<CodeScreenshotDTO>> getScreenshotsByReviewRecord(@PathVariable Long reviewRecordId) {
        
        List<CodeScreenshot> screenshots = codeScreenshotService.getScreenshotsByReviewRecordId(reviewRecordId);
        List<CodeScreenshotDTO> dtoList = screenshots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseResult.success(dtoList);
    }
    
    /**
     * 更新截图排序
     */
    @PutMapping("/{id}/sort-order")
    @ApiOperation("更新截图排序")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> updateScreenshotOrder(
            @PathVariable Long id,
            @RequestParam @ApiParam("排序序号") Integer sortOrder) {
        
        log.info("更新截图排序: id={}, sortOrder={}", id, sortOrder);
        
        codeScreenshotService.updateScreenshotOrder(id, sortOrder);
        
        return ResponseResult.success();
    }
    
    /**
     * 重新排序截图
     */
    @PutMapping("/review-record/{reviewRecordId}/reorder")
    @ApiOperation("重新排序截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> reorderScreenshots(
            @PathVariable Long reviewRecordId,
            @RequestParam @ApiParam("截图ID列表") List<Long> screenshotIds) {
        
        log.info("重新排序截图: reviewRecordId={}, screenshotIds={}", reviewRecordId, screenshotIds);
        
        codeScreenshotService.reorderScreenshots(reviewRecordId, screenshotIds);
        
        return ResponseResult.success();
    }
    
    /**
     * 批量删除截图
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除截图")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Void> batchDeleteScreenshots(@RequestParam List<Long> ids) {
        
        log.info("批量删除截图: ids={}", ids);
        
        fileManagementService.batchDeleteScreenshotsWithFiles(ids);
        
        return ResponseResult.success();
    }
    
    /**
     * 替换截图文件
     */
    @PostMapping("/{id}/replace")
    @ApiOperation("替换截图文件")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<CodeScreenshotDTO> replaceScreenshotFile(
            @PathVariable Long id,
            @RequestParam @ApiParam("新文件") MultipartFile newFile) {
        
        log.info("替换截图文件: id={}, newFileName={}", id, newFile.getOriginalFilename());
        
        CodeScreenshot screenshot = fileManagementService.replaceScreenshotFile(id, newFile);
        
        return ResponseResult.success(convertToDTO(screenshot));
    }
    
    /**
     * 获取文件下载链接
     */
    @GetMapping("/{id}/download-url")
    @ApiOperation("获取文件下载链接")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<String> getFileDownloadUrl(@PathVariable Long id) {
        
        String downloadUrl = fileManagementService.getFileDownloadUrl(id);
        
        return ResponseResult.success(downloadUrl);
    }
    
    /**
     * 检查文件完整性
     */
    @GetMapping("/{id}/integrity")
    @ApiOperation("检查文件完整性")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Boolean> checkFileIntegrity(@PathVariable Long id) {
        
        boolean isIntact = fileManagementService.checkFileIntegrity(id);
        
        return ResponseResult.success(isIntact);
    }
    
    /**
     * 获取截图数量
     */
    @GetMapping("/review-record/{reviewRecordId}/count")
    @ApiOperation("获取评审记录的截图数量")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<Long> getScreenshotCount(@PathVariable Long reviewRecordId) {
        
        Long count = codeScreenshotService.getScreenshotCount(reviewRecordId);
        
        return ResponseResult.success(count);
    }
    
    /**
     * 转换实体为DTO
     */
    private CodeScreenshotDTO convertToDTO(CodeScreenshot screenshot) {
        if (screenshot == null) {
            return null;
        }
        
        CodeScreenshotDTO dto = new CodeScreenshotDTO();
        dto.setId(screenshot.getId());
        dto.setReviewRecordId(screenshot.getReviewRecordId());
        dto.setFileName(screenshot.getFileName());
        dto.setFileUrl(screenshot.getFileUrl());
        dto.setFileSize(screenshot.getFileSize());
        dto.setFileType(screenshot.getFileType());
        dto.setDescription(screenshot.getDescription());
        dto.setSortOrder(screenshot.getSortOrder());
        dto.setCreatedAt(screenshot.getCreatedAt());
        dto.setUpdatedAt(screenshot.getUpdatedAt());
        dto.setCreatedBy(screenshot.getCreatedBy());
        dto.setUpdatedBy(screenshot.getUpdatedBy());
        
        return dto;
    }
    
    /**
     * 转换DTO为实体
     */
    private CodeScreenshot convertToEntity(CodeScreenshotDTO dto) {
        if (dto == null) {
            return null;
        }
        
        CodeScreenshot screenshot = new CodeScreenshot();
        screenshot.setId(dto.getId());
        screenshot.setReviewRecordId(dto.getReviewRecordId());
        screenshot.setFileName(dto.getFileName());
        screenshot.setFileUrl(dto.getFileUrl());
        screenshot.setFileSize(dto.getFileSize());
        screenshot.setFileType(dto.getFileType());
        screenshot.setDescription(dto.getDescription());
        screenshot.setSortOrder(dto.getSortOrder());
        
        return screenshot;
    }
}