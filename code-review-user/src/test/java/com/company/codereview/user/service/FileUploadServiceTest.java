package com.company.codereview.user.service;

import com.company.codereview.user.config.FileUploadConfig;
import com.company.codereview.user.config.MinioConfig;
import com.company.codereview.user.dto.FileUploadResult;
import com.company.codereview.user.exception.FileUploadException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 文件上传服务测试
 */
@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {
    
    @Mock
    private MinioClient minioClient;
    
    @Mock
    private MinioConfig minioConfig;
    
    @Mock
    private FileUploadConfig fileUploadConfig;
    
    @InjectMocks
    private FileUploadService fileUploadService;
    
    @BeforeEach
    void setUp() {
        // 配置模拟对象
        when(minioConfig.getBucketName()).thenReturn("test-bucket");
        when(minioConfig.getUrl()).thenReturn("http://localhost:9000");
        
        when(fileUploadConfig.getMaxFileSize()).thenReturn(10485760L); // 10MB
        when(fileUploadConfig.getAllowedTypes()).thenReturn(Arrays.asList("image/jpeg", "image/png", "image/gif"));
        when(fileUploadConfig.getAllowedExtensions()).thenReturn(Arrays.asList(".jpg", ".jpeg", ".png", ".gif"));
        when(fileUploadConfig.getPathPrefix()).thenReturn("code-review");
        when(fileUploadConfig.getScreenshotPath()).thenReturn("screenshots");
        when(fileUploadConfig.getDocumentPath()).thenReturn("documents");
    }
    
    @Test
    void testUploadScreenshot_Success() throws Exception {
        // 准备测试数据
        byte[] imageContent = createTestImageContent();
        MultipartFile file = new MockMultipartFile(
            "file", 
            "test.jpg", 
            "image/jpeg", 
            imageContent
        );
        Long reviewRecordId = 1L;
        
        // 模拟Minio操作
        when(minioClient.bucketExists(any())).thenReturn(true);
        doNothing().when(minioClient).putObject(any(PutObjectArgs.class));
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://localhost:9000/test-bucket/test-path");
        
        // 执行测试
        FileUploadResult result = fileUploadService.uploadScreenshot(file, reviewRecordId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("test.jpg", result.getOriginalFileName());
        assertEquals("image/jpeg", result.getFileType());
        assertEquals(imageContent.length, result.getFileSize());
        assertNotNull(result.getFileUrl());
        
        // 验证Minio调用
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
    
    @Test
    void testUploadScreenshot_InvalidFileType() {
        // 准备测试数据
        MultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "test content".getBytes()
        );
        Long reviewRecordId = 1L;
        
        // 执行测试并验证异常
        FileUploadException exception = assertThrows(
            FileUploadException.class,
            () -> fileUploadService.uploadScreenshot(file, reviewRecordId)
        );
        
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }
    
    @Test
    void testUploadScreenshot_FileSizeExceeded() {
        // 准备测试数据 - 超大文件
        when(fileUploadConfig.getMaxFileSize()).thenReturn(1024L); // 1KB限制
        
        byte[] largeContent = new byte[2048]; // 2KB文件
        MultipartFile file = new MockMultipartFile(
            "file", 
            "large.jpg", 
            "image/jpeg", 
            largeContent
        );
        Long reviewRecordId = 1L;
        
        // 执行测试并验证异常
        FileUploadException exception = assertThrows(
            FileUploadException.class,
            () -> fileUploadService.uploadScreenshot(file, reviewRecordId)
        );
        
        assertTrue(exception.getMessage().contains("文件大小超过限制"));
    }
    
    @Test
    void testUploadDocument_Success() throws Exception {
        // 准备测试数据
        MultipartFile file = new MockMultipartFile(
            "file", 
            "document.pdf", 
            "application/pdf", 
            "PDF content".getBytes()
        );
        String category = "requirements";
        
        // 更新配置以支持PDF
        when(fileUploadConfig.getAllowedTypes()).thenReturn(Arrays.asList("application/pdf"));
        when(fileUploadConfig.getAllowedExtensions()).thenReturn(Arrays.asList(".pdf"));
        
        // 模拟Minio操作
        when(minioClient.bucketExists(any())).thenReturn(true);
        doNothing().when(minioClient).putObject(any(PutObjectArgs.class));
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://localhost:9000/test-bucket/test-path");
        
        // 执行测试
        FileUploadResult result = fileUploadService.uploadDocument(file, category);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("document.pdf", result.getOriginalFileName());
        assertEquals("application/pdf", result.getFileType());
        
        // 验证Minio调用
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }
    
    @Test
    void testDeleteFile_Success() throws Exception {
        // 准备测试数据
        String filePath = "code-review/screenshots/2024/01/01/test.jpg";
        
        // 模拟Minio操作
        doNothing().when(minioClient).removeObject(any());
        
        // 执行测试
        assertDoesNotThrow(() -> fileUploadService.deleteFile(filePath));
        
        // 验证Minio调用
        verify(minioClient).removeObject(any());
    }
    
    @Test
    void testFileExists_True() throws Exception {
        // 准备测试数据
        String filePath = "code-review/screenshots/2024/01/01/test.jpg";
        
        // 模拟Minio操作
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));
        
        // 执行测试
        boolean exists = fileUploadService.fileExists(filePath);
        
        // 验证结果
        assertTrue(exists);
        verify(minioClient).statObject(any(StatObjectArgs.class));
    }
    
    @Test
    void testFileExists_False() throws Exception {
        // 准备测试数据
        String filePath = "code-review/screenshots/2024/01/01/nonexistent.jpg";
        
        // 模拟Minio操作 - 抛出异常表示文件不存在
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("File not found"));
        
        // 执行测试
        boolean exists = fileUploadService.fileExists(filePath);
        
        // 验证结果
        assertFalse(exists);
    }
    
    @Test
    void testGetDownloadUrl_Success() throws Exception {
        // 准备测试数据
        String filePath = "code-review/screenshots/2024/01/01/test.jpg";
        String expectedUrl = "http://localhost:9000/test-bucket/test-path?expires=123456";
        
        // 模拟Minio操作
        when(minioClient.getPresignedObjectUrl(any())).thenReturn(expectedUrl);
        
        // 执行测试
        String downloadUrl = fileUploadService.getDownloadUrl(filePath);
        
        // 验证结果
        assertEquals(expectedUrl, downloadUrl);
        verify(minioClient).getPresignedObjectUrl(any());
    }
    
    @Test
    void testCopyFile_Success() throws Exception {
        // 准备测试数据
        String sourceFilePath = "code-review/screenshots/2024/01/01/source.jpg";
        String targetFilePath = "code-review/screenshots/2024/01/01/target.jpg";
        String expectedUrl = "http://localhost:9000/test-bucket/target-path";
        
        // 模拟Minio操作
        doNothing().when(minioClient).copyObject(any());
        when(minioClient.getPresignedObjectUrl(any())).thenReturn(expectedUrl);
        
        // 执行测试
        String resultUrl = fileUploadService.copyFile(sourceFilePath, targetFilePath);
        
        // 验证结果
        assertEquals(expectedUrl, resultUrl);
        verify(minioClient).copyObject(any());
    }
    
    @Test
    void testBatchDeleteFiles_Success() throws Exception {
        // 准备测试数据
        java.util.List<String> filePaths = Arrays.asList(
            "code-review/screenshots/2024/01/01/file1.jpg",
            "code-review/screenshots/2024/01/01/file2.jpg",
            "code-review/screenshots/2024/01/01/file3.jpg"
        );
        
        // 模拟Minio操作
        doNothing().when(minioClient).removeObject(any());
        
        // 执行测试
        assertDoesNotThrow(() -> fileUploadService.batchDeleteFiles(filePaths));
        
        // 验证Minio调用次数
        verify(minioClient, times(3)).removeObject(any());
    }
    
    @Test
    void testGetFileStream_Success() throws Exception {
        // 准备测试数据
        String filePath = "code-review/screenshots/2024/01/01/test.jpg";
        ByteArrayInputStream expectedStream = new ByteArrayInputStream("test content".getBytes());
        
        // 模拟Minio操作
        when(minioClient.getObject(any())).thenReturn(expectedStream);
        
        // 执行测试
        java.io.InputStream resultStream = fileUploadService.getFileStream(filePath);
        
        // 验证结果
        assertNotNull(resultStream);
        verify(minioClient).getObject(any());
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
}