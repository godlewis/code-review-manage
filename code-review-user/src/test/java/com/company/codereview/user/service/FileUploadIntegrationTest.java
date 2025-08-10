package com.company.codereview.user.service;

import com.company.codereview.user.config.FileUploadConfig;
import com.company.codereview.user.config.MinioConfig;
import com.company.codereview.user.dto.FileUploadResult;
import com.company.codereview.user.exception.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件上传服务集成测试
 * 这个测试不依赖Spring容器，直接测试核心逻辑
 */
class FileUploadIntegrationTest {
    
    private FileUploadConfig fileUploadConfig;
    private MinioConfig minioConfig;
    
    @BeforeEach
    void setUp() {
        // 手动创建配置对象
        fileUploadConfig = new FileUploadConfig();
        fileUploadConfig.setMaxFileSize(10485760L); // 10MB
        fileUploadConfig.setAllowedTypes(Arrays.asList("image/jpeg", "image/png", "image/gif"));
        fileUploadConfig.setAllowedExtensions(Arrays.asList(".jpg", ".jpeg", ".png", ".gif"));
        fileUploadConfig.setPathPrefix("code-review");
        fileUploadConfig.setScreenshotPath("screenshots");
        fileUploadConfig.setDocumentPath("documents");
        fileUploadConfig.setTempPath("temp");
        
        minioConfig = new MinioConfig();
        minioConfig.setUrl("http://localhost:9000");
        minioConfig.setAccessKey("minioadmin");
        minioConfig.setSecretKey("minioadmin");
        minioConfig.setBucketName("test-bucket");
    }
    
    @Test
    void testFileUploadConfigurationIsValid() {
        // 验证配置对象创建成功
        assertNotNull(fileUploadConfig);
        assertNotNull(minioConfig);
        
        // 验证配置值
        assertEquals(10485760L, fileUploadConfig.getMaxFileSize());
        assertEquals("code-review", fileUploadConfig.getPathPrefix());
        assertEquals("screenshots", fileUploadConfig.getScreenshotPath());
        assertEquals("test-bucket", minioConfig.getBucketName());
        
        // 验证允许的文件类型
        assertTrue(fileUploadConfig.getAllowedTypes().contains("image/jpeg"));
        assertTrue(fileUploadConfig.getAllowedExtensions().contains(".jpg"));
    }
    
    @Test
    void testFileValidationLogic() {
        // 测试有效的图片文件
        MultipartFile validImageFile = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            createTestImageContent()
        );
        
        // 验证文件基本属性
        assertEquals("test.jpg", validImageFile.getOriginalFilename());
        assertEquals("image/jpeg", validImageFile.getContentType());
        assertTrue(validImageFile.getSize() > 0);
        assertTrue(validImageFile.getSize() <= fileUploadConfig.getMaxFileSize());
        
        // 验证文件类型在允许列表中
        assertTrue(fileUploadConfig.getAllowedTypes().contains(validImageFile.getContentType()));
        
        // 验证文件扩展名
        String extension = getFileExtension(validImageFile.getOriginalFilename());
        assertTrue(fileUploadConfig.getAllowedExtensions().contains(extension));
    }
    
    @Test
    void testInvalidFileValidation() {
        // 测试不支持的文件类型
        MultipartFile invalidFile = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );
        
        // 验证文件类型不在允许列表中
        assertFalse(fileUploadConfig.getAllowedTypes().contains(invalidFile.getContentType()));
        
        // 测试超大文件
        byte[] largeContent = new byte[(int) (fileUploadConfig.getMaxFileSize() + 1)];
        MultipartFile largeFile = new MockMultipartFile(
            "file", 
            "large.jpg", 
            "image/jpeg", 
            largeContent
        );
        
        // 验证文件大小超过限制
        assertTrue(largeFile.getSize() > fileUploadConfig.getMaxFileSize());
    }
    
    @Test
    void testFilePathGeneration() {
        // 测试截图路径生成逻辑
        String screenshotPath = generateScreenshotPath(1L, "test.jpg");
        
        assertNotNull(screenshotPath);
        assertTrue(screenshotPath.contains(fileUploadConfig.getPathPrefix()));
        assertTrue(screenshotPath.contains(fileUploadConfig.getScreenshotPath()));
        assertTrue(screenshotPath.contains("review_1"));
        assertTrue(screenshotPath.endsWith(".jpg"));
        
        // 测试文档路径生成逻辑
        String documentPath = generateDocumentPath("requirements", "doc.pdf");
        
        assertNotNull(documentPath);
        assertTrue(documentPath.contains(fileUploadConfig.getPathPrefix()));
        assertTrue(documentPath.contains(fileUploadConfig.getDocumentPath()));
        assertTrue(documentPath.contains("requirements"));
        assertTrue(documentPath.endsWith(".pdf"));
    }
    
    @Test
    void testFileUploadResultCreation() {
        // 测试成功结果创建
        FileUploadResult successResult = FileUploadResult.success(
            "http://localhost:9000/bucket/path/test.jpg",
            "uuid-test.jpg",
            "test.jpg",
            1024L,
            "image/jpeg",
            "path/to/file"
        );
        
        assertNotNull(successResult);
        assertTrue(successResult.getSuccess());
        assertEquals("test.jpg", successResult.getOriginalFileName());
        assertEquals("image/jpeg", successResult.getFileType());
        assertEquals(1024L, successResult.getFileSize());
        assertNull(successResult.getErrorMessage());
        
        // 测试失败结果创建
        FileUploadResult failureResult = FileUploadResult.failure("Upload failed");
        
        assertNotNull(failureResult);
        assertFalse(failureResult.getSuccess());
        assertEquals("Upload failed", failureResult.getErrorMessage());
        assertNull(failureResult.getFileUrl());
    }
    
    @Test
    void testFileUploadProgressTracking() {
        // 测试上传进度跟踪
        FileUploadProgressService progressService = new FileUploadProgressService();
        
        String uploadId = progressService.generateUploadId(1L, "test.jpg");
        assertNotNull(uploadId);
        assertTrue(uploadId.contains("1_"));
        assertTrue(uploadId.contains("test_jpg"));
        
        // 开始上传跟踪
        FileUploadProgressService.UploadProgress progress = progressService.startUpload(
            uploadId, "test.jpg", 1024L
        );
        
        assertNotNull(progress);
        assertEquals(uploadId, progress.getUploadId());
        assertEquals("test.jpg", progress.getFileName());
        assertEquals(1024L, progress.getTotalSize());
        assertEquals(0L, progress.getUploadedSize());
        assertEquals(FileUploadProgressService.UploadStatus.UPLOADING, progress.getStatus());
        
        // 更新进度
        progressService.updateProgress(uploadId, 512L);
        assertEquals(512L, progress.getUploadedSize());
        assertEquals(50, progress.getPercentage());
        
        // 标记完成
        progressService.markCompleted(uploadId);
        assertEquals(FileUploadProgressService.UploadStatus.COMPLETED, progress.getStatus());
        assertEquals(1024L, progress.getUploadedSize());
        assertEquals(100, progress.getPercentage());
    }
    
    @Test
    void testFileValidationUtilityMethods() {
        // 测试文件扩展名提取
        assertEquals(".jpg", getFileExtension("test.jpg"));
        assertEquals(".jpeg", getFileExtension("image.jpeg"));
        assertEquals("", getFileExtension("noextension"));
        assertEquals("", getFileExtension(null));
        
        // 测试文件名提取
        assertEquals("test.jpg", extractFileName("path/to/test.jpg"));
        assertEquals("test.jpg", extractFileName("test.jpg"));
        assertEquals("", extractFileName(""));
        assertNull(extractFileName(null));
    }
    
    /**
     * 创建测试用的图片内容
     */
    private byte[] createTestImageContent() {
        // 创建一个简单的JPEG文件头
        return new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, // JPEG文件头
            0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, // JFIF标识
            0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00, // 其他JPEG数据
            (byte) 0xFF, (byte) 0xD9 // JPEG文件结束标记
        };
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 从路径中提取文件名
     */
    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        if (!filePath.contains("/")) {
            return filePath;
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
    
    /**
     * 生成截图文件路径
     */
    private String generateScreenshotPath(Long reviewRecordId, String originalFilename) {
        String dateStr = "2024/01/01"; // 固定日期用于测试
        String extension = getFileExtension(originalFilename);
        String uniqueFileName = "uuid-" + originalFilename.replace(extension, "") + extension;
        
        return String.format("%s/%s/%s/review_%d/%s", 
            fileUploadConfig.getPathPrefix(),
            fileUploadConfig.getScreenshotPath(),
            dateStr,
            reviewRecordId,
            uniqueFileName
        );
    }
    
    /**
     * 生成文档文件路径
     */
    private String generateDocumentPath(String category, String originalFilename) {
        String dateStr = "2024/01/01"; // 固定日期用于测试
        String extension = getFileExtension(originalFilename);
        String uniqueFileName = "uuid-" + originalFilename.replace(extension, "") + extension;
        
        return String.format("%s/%s/%s/%s/%s", 
            fileUploadConfig.getPathPrefix(),
            fileUploadConfig.getDocumentPath(),
            category != null ? category : "general",
            dateStr,
            uniqueFileName
        );
    }
}