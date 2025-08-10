package com.company.codereview.user.service;

import com.company.codereview.user.dto.FileUploadResult;
import com.company.codereview.user.entity.CodeScreenshot;
import com.company.codereview.user.entity.ReviewRecord;
import com.company.codereview.user.exception.FileUploadException;
import com.company.codereview.user.repository.CodeScreenshotRepository;
import com.company.codereview.user.repository.ReviewRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 文件管理服务测试
 */
@ExtendWith(MockitoExtension.class)
class FileManagementServiceTest {
    
    @Mock
    private FileUploadService fileUploadService;
    
    @Mock
    private CodeScreenshotService codeScreenshotService;
    
    @Mock
    private ReviewRecordRepository reviewRecordRepository;
    
    @Mock
    private CodeScreenshotRepository screenshotRepository;
    
    @InjectMocks
    private FileManagementService fileManagementService;
    
    private ReviewRecord mockReviewRecord;
    private MultipartFile mockFile;
    private FileUploadResult mockUploadResult;
    
    @BeforeEach
    void setUp() {
        // 创建模拟的评审记录
        mockReviewRecord = new ReviewRecord();
        mockReviewRecord.setId(1L);
        mockReviewRecord.setStatus(ReviewRecord.ReviewStatus.DRAFT);
        
        // 创建模拟的文件
        mockFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );
        
        // 创建模拟的上传结果
        mockUploadResult = FileUploadResult.success(
            "http://localhost:9000/bucket/path/test.jpg",
            "uuid-test.jpg",
            "test.jpg",
            1024L,
            "image/jpeg",
            "code-review/screenshots/2024/01/01/uuid-test.jpg"
        );
    }
    
    @Test
    void testUploadAndSaveScreenshot_Success() {
        // 准备测试数据
        Long reviewRecordId = 1L;
        String description = "Test screenshot";
        
        // 模拟依赖服务
        when(reviewRecordRepository.selectById(reviewRecordId)).thenReturn(mockReviewRecord);
        when(fileUploadService.uploadScreenshot(mockFile, reviewRecordId)).thenReturn(mockUploadResult);
        
        CodeScreenshot savedScreenshot = new CodeScreenshot();
        savedScreenshot.setId(1L);
        savedScreenshot.setReviewRecordId(reviewRecordId);
        savedScreenshot.setFileName("test.jpg");
        savedScreenshot.setFileUrl(mockUploadResult.getFileUrl());
        
        when(codeScreenshotService.addScreenshot(any(CodeScreenshot.class))).thenReturn(savedScreenshot);
        
        // 执行测试
        CodeScreenshot result = fileManagementService.uploadAndSaveScreenshot(mockFile, reviewRecordId, description);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(reviewRecordId, result.getReviewRecordId());
        assertEquals("test.jpg", result.getFileName());
        assertEquals(mockUploadResult.getFileUrl(), result.getFileUrl());
        
        // 验证服务调用
        verify(reviewRecordRepository).selectById(reviewRecordId);
        verify(fileUploadService).uploadScreenshot(mockFile, reviewRecordId);
        verify(codeScreenshotService).addScreenshot(any(CodeScreenshot.class));
    }
    
    @Test
    void testUploadAndSaveScreenshot_ReviewRecordNotFound() {
        // 准备测试数据
        Long reviewRecordId = 999L;
        String description = "Test screenshot";
        
        // 模拟评审记录不存在
        when(reviewRecordRepository.selectById(reviewRecordId)).thenReturn(null);
        
        // 执行测试并验证异常
        FileUploadException exception = assertThrows(
            FileUploadException.class,
            () -> fileManagementService.uploadAndSaveScreenshot(mockFile, reviewRecordId, description)
        );
        
        assertTrue(exception.getMessage().contains("评审记录不存在"));
        
        // 验证没有调用文件上传服务
        verify(fileUploadService, never()).uploadScreenshot(any(), any());
        verify(codeScreenshotService, never()).addScreenshot(any());
    }
    
    @Test
    void testUploadAndSaveScreenshot_CompletedReviewRecord() {
        // 准备测试数据
        Long reviewRecordId = 1L;
        String description = "Test screenshot";
        
        // 设置评审记录为已完成状态
        mockReviewRecord.setStatus(ReviewRecord.ReviewStatus.COMPLETED);
        
        when(reviewRecordRepository.selectById(reviewRecordId)).thenReturn(mockReviewRecord);
        
        // 执行测试并验证异常
        FileUploadException exception = assertThrows(
            FileUploadException.class,
            () -> fileManagementService.uploadAndSaveScreenshot(mockFile, reviewRecordId, description)
        );
        
        assertTrue(exception.getMessage().contains("已完成的评审记录不能上传截图"));
        
        // 验证没有调用文件上传服务
        verify(fileUploadService, never()).uploadScreenshot(any(), any());
        verify(codeScreenshotService, never()).addScreenshot(any());
    }
    
    @Test
    void testBatchUploadScreenshots_Success() {
        // 准备测试数据
        Long reviewRecordId = 1L;
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "content1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "content2".getBytes())
        );
        
        // 模拟依赖服务
        when(reviewRecordRepository.selectById(reviewRecordId)).thenReturn(mockReviewRecord);
        
        // 模拟文件上传结果
        FileUploadResult result1 = FileUploadResult.success(
            "http://localhost:9000/bucket/path/test1.jpg", "uuid-test1.jpg", "test1.jpg", 
            1024L, "image/jpeg", "path1"
        );
        FileUploadResult result2 = FileUploadResult.success(
            "http://localhost:9000/bucket/path/test2.jpg", "uuid-test2.jpg", "test2.jpg", 
            1024L, "image/jpeg", "path2"
        );
        
        when(fileUploadService.uploadScreenshot(files.get(0), reviewRecordId)).thenReturn(result1);
        when(fileUploadService.uploadScreenshot(files.get(1), reviewRecordId)).thenReturn(result2);
        
        when(codeScreenshotService.batchAddScreenshots(any())).thenAnswer(invocation -> {
            List<CodeScreenshot> screenshots = invocation.getArgument(0);
            for (int i = 0; i < screenshots.size(); i++) {
                screenshots.get(i).setId((long) (i + 1));
            }
            return screenshots;
        });
        
        // 执行测试
        List<CodeScreenshot> results = fileManagementService.batchUploadScreenshots(files, reviewRecordId);
        
        // 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("test1.jpg", results.get(0).getFileName());
        assertEquals("test2.jpg", results.get(1).getFileName());
        assertEquals(1, results.get(0).getSortOrder());
        assertEquals(2, results.get(1).getSortOrder());
        
        // 验证服务调用
        verify(fileUploadService, times(2)).uploadScreenshot(any(), eq(reviewRecordId));
        verify(codeScreenshotService).batchAddScreenshots(any());
    }
    
    @Test
    void testDeleteScreenshotWithFile_Success() {
        // 准备测试数据
        Long screenshotId = 1L;
        
        CodeScreenshot screenshot = new CodeScreenshot();
        screenshot.setId(screenshotId);
        screenshot.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test.jpg");
        
        // 模拟依赖服务
        when(codeScreenshotService.getScreenshotById(screenshotId)).thenReturn(screenshot);
        when(fileUploadService.fileExists(any())).thenReturn(true);
        doNothing().when(codeScreenshotService).deleteScreenshot(screenshotId);
        doNothing().when(fileUploadService).deleteFile(any());
        
        // 执行测试
        assertDoesNotThrow(() -> fileManagementService.deleteScreenshotWithFile(screenshotId));
        
        // 验证服务调用
        verify(codeScreenshotService).getScreenshotById(screenshotId);
        verify(codeScreenshotService).deleteScreenshot(screenshotId);
        verify(fileUploadService).deleteFile(any());
    }
    
    @Test
    void testBatchDeleteScreenshotsWithFiles_Success() {
        // 准备测试数据
        List<Long> screenshotIds = Arrays.asList(1L, 2L, 3L);
        
        // 模拟截图数据
        CodeScreenshot screenshot1 = new CodeScreenshot();
        screenshot1.setId(1L);
        screenshot1.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test1.jpg");
        
        CodeScreenshot screenshot2 = new CodeScreenshot();
        screenshot2.setId(2L);
        screenshot2.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test2.jpg");
        
        CodeScreenshot screenshot3 = new CodeScreenshot();
        screenshot3.setId(3L);
        screenshot3.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test3.jpg");
        
        // 模拟依赖服务
        when(codeScreenshotService.getScreenshotById(1L)).thenReturn(screenshot1);
        when(codeScreenshotService.getScreenshotById(2L)).thenReturn(screenshot2);
        when(codeScreenshotService.getScreenshotById(3L)).thenReturn(screenshot3);
        
        doNothing().when(codeScreenshotService).batchDeleteScreenshots(screenshotIds);
        doNothing().when(fileUploadService).batchDeleteFiles(any());
        
        // 执行测试
        assertDoesNotThrow(() -> fileManagementService.batchDeleteScreenshotsWithFiles(screenshotIds));
        
        // 验证服务调用
        verify(codeScreenshotService, times(3)).getScreenshotById(anyLong());
        verify(codeScreenshotService).batchDeleteScreenshots(screenshotIds);
        verify(fileUploadService).batchDeleteFiles(any());
    }
    
    @Test
    void testReplaceScreenshotFile_Success() {
        // 准备测试数据
        Long screenshotId = 1L;
        MultipartFile newFile = new MockMultipartFile(
            "newFile", "new-test.jpg", "image/jpeg", "new content".getBytes()
        );
        
        CodeScreenshot originalScreenshot = new CodeScreenshot();
        originalScreenshot.setId(screenshotId);
        originalScreenshot.setReviewRecordId(1L);
        originalScreenshot.setFileUrl("http://localhost:9000/bucket/old-path/old-test.jpg");
        
        FileUploadResult newUploadResult = FileUploadResult.success(
            "http://localhost:9000/bucket/new-path/new-test.jpg",
            "uuid-new-test.jpg",
            "new-test.jpg",
            2048L,
            "image/jpeg",
            "new-path"
        );
        
        CodeScreenshot updatedScreenshot = new CodeScreenshot();
        updatedScreenshot.setId(screenshotId);
        updatedScreenshot.setFileName("new-test.jpg");
        updatedScreenshot.setFileUrl(newUploadResult.getFileUrl());
        
        // 模拟依赖服务
        when(codeScreenshotService.getScreenshotById(screenshotId)).thenReturn(originalScreenshot);
        when(fileUploadService.uploadScreenshot(newFile, 1L)).thenReturn(newUploadResult);
        when(codeScreenshotService.updateScreenshot(any())).thenReturn(updatedScreenshot);
        when(fileUploadService.fileExists(any())).thenReturn(true);
        doNothing().when(fileUploadService).deleteFile(any());
        
        // 执行测试
        CodeScreenshot result = fileManagementService.replaceScreenshotFile(screenshotId, newFile);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("new-test.jpg", result.getFileName());
        assertEquals(newUploadResult.getFileUrl(), result.getFileUrl());
        
        // 验证服务调用
        verify(codeScreenshotService).getScreenshotById(screenshotId);
        verify(fileUploadService).uploadScreenshot(newFile, 1L);
        verify(codeScreenshotService).updateScreenshot(any());
        verify(fileUploadService).deleteFile(any());
    }
    
    @Test
    void testGetFileDownloadUrl_Success() {
        // 准备测试数据
        Long screenshotId = 1L;
        String expectedDownloadUrl = "http://localhost:9000/bucket/download-path?expires=123456";
        
        CodeScreenshot screenshot = new CodeScreenshot();
        screenshot.setId(screenshotId);
        screenshot.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test.jpg");
        
        // 模拟依赖服务
        when(codeScreenshotService.getScreenshotById(screenshotId)).thenReturn(screenshot);
        when(fileUploadService.getDownloadUrl(any())).thenReturn(expectedDownloadUrl);
        
        // 执行测试
        String result = fileManagementService.getFileDownloadUrl(screenshotId);
        
        // 验证结果
        assertEquals(expectedDownloadUrl, result);
        
        // 验证服务调用
        verify(codeScreenshotService).getScreenshotById(screenshotId);
        verify(fileUploadService).getDownloadUrl(any());
    }
    
    @Test
    void testCheckFileIntegrity_True() {
        // 准备测试数据
        Long screenshotId = 1L;
        
        CodeScreenshot screenshot = new CodeScreenshot();
        screenshot.setId(screenshotId);
        screenshot.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test.jpg");
        
        // 模拟依赖服务
        when(codeScreenshotService.getScreenshotById(screenshotId)).thenReturn(screenshot);
        when(fileUploadService.fileExists(any())).thenReturn(true);
        
        // 执行测试
        boolean result = fileManagementService.checkFileIntegrity(screenshotId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证服务调用
        verify(codeScreenshotService).getScreenshotById(screenshotId);
        verify(fileUploadService).fileExists(any());
    }
    
    @Test
    void testCheckFileIntegrity_False() {
        // 准备测试数据
        Long screenshotId = 1L;
        
        CodeScreenshot screenshot = new CodeScreenshot();
        screenshot.setId(screenshotId);
        screenshot.setFileUrl("http://localhost:9000/bucket/code-review/screenshots/2024/01/01/test.jpg");
        
        // 模拟依赖服务
        when(codeScreenshotService.getScreenshotById(screenshotId)).thenReturn(screenshot);
        when(fileUploadService.fileExists(any())).thenReturn(false);
        
        // 执行测试
        boolean result = fileManagementService.checkFileIntegrity(screenshotId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证服务调用
        verify(codeScreenshotService).getScreenshotById(screenshotId);
        verify(fileUploadService).fileExists(any());
    }
}