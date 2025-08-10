package com.company.codereview.user.service;

import com.company.codereview.user.config.FileUploadConfig;
import com.company.codereview.user.config.MinioConfig;
import com.company.codereview.user.dto.FileUploadResult;
import com.company.codereview.user.exception.FileUploadException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {
    
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final FileUploadConfig fileUploadConfig;
    
    /**
     * 上传代码截图
     */
    public FileUploadResult uploadScreenshot(MultipartFile file, Long reviewRecordId) {
        log.info("上传代码截图: fileName={}, reviewRecordId={}, size={}", 
                file.getOriginalFilename(), reviewRecordId, file.getSize());
        
        try {
            // 验证文件
            validateFile(file);
            
            // 确保存储桶存在
            ensureBucketExists();
            
            // 生成文件路径
            String filePath = generateScreenshotPath(reviewRecordId, file.getOriginalFilename());
            
            // 上传文件
            uploadFileToMinio(file, filePath);
            
            // 生成访问URL
            String fileUrl = generateFileUrl(filePath);
            
            FileUploadResult result = FileUploadResult.success(
                fileUrl, 
                extractFileName(filePath),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                filePath
            );
            
            log.info("代码截图上传成功: fileUrl={}", fileUrl);
            return result;
            
        } catch (Exception e) {
            log.error("代码截图上传失败: fileName={}, error={}", file.getOriginalFilename(), e.getMessage(), e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 上传文档文件
     */
    public FileUploadResult uploadDocument(MultipartFile file, String category) {
        log.info("上传文档文件: fileName={}, category={}, size={}", 
                file.getOriginalFilename(), category, file.getSize());
        
        try {
            // 验证文件
            validateFile(file);
            
            // 确保存储桶存在
            ensureBucketExists();
            
            // 生成文件路径
            String filePath = generateDocumentPath(category, file.getOriginalFilename());
            
            // 上传文件
            uploadFileToMinio(file, filePath);
            
            // 生成访问URL
            String fileUrl = generateFileUrl(filePath);
            
            FileUploadResult result = FileUploadResult.success(
                fileUrl,
                extractFileName(filePath),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                filePath
            );
            
            log.info("文档文件上传成功: fileUrl={}", fileUrl);
            return result;
            
        } catch (Exception e) {
            log.error("文档文件上传失败: fileName={}, error={}", file.getOriginalFilename(), e.getMessage(), e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        com.company.codereview.user.util.FileValidationUtil.validateFile(
            file, 
            fileUploadConfig.getAllowedTypes(),
            fileUploadConfig.getAllowedExtensions(),
            fileUploadConfig.getMaxFileSize()
        );
    }
    
    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build()
            );
            
            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build()
                );
                log.info("创建存储桶: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            throw new FileUploadException("检查或创建存储桶失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成截图文件路径
     */
    private String generateScreenshotPath(Long reviewRecordId, String originalFilename) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
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
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String extension = getFileExtension(originalFilename);
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        
        return String.format("%s/%s/%s/%s/%s", 
            fileUploadConfig.getPathPrefix(),
            fileUploadConfig.getDocumentPath(),
            category != null ? category : "general",
            dateStr,
            uniqueFileName
        );
    }
    
    /**
     * 上传文件到Minio
     */
    private void uploadFileToMinio(MultipartFile file, String filePath) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("上传文件到存储服务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String filePath) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .expiry(7 * 24 * 60 * 60) // 7天有效期
                    .build()
            );
        } catch (Exception e) {
            // 如果生成预签名URL失败，返回直接访问URL
            return String.format("%s/%s/%s", minioConfig.getUrl(), minioConfig.getBucketName(), filePath);
        }
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
        if (filePath == null || !filePath.contains("/")) {
            return filePath;
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String filePath) {
        log.info("删除文件: filePath={}", filePath);
        
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build()
            );
            
            log.info("文件删除成功: filePath={}", filePath);
        } catch (Exception e) {
            log.error("文件删除失败: filePath={}, error={}", filePath, e.getMessage(), e);
            throw new FileUploadException("删除文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String filePath) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取文件信息
     */
    public StatObjectResponse getFileInfo(String filePath) {
        try {
            return minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("获取文件信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件下载URL
     */
    public String getDownloadUrl(String filePath) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .expiry(24 * 60 * 60) // 24小时有效期
                    .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("生成下载链接失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 复制文件
     */
    public String copyFile(String sourceFilePath, String targetFilePath) {
        log.info("复制文件: source={}, target={}", sourceFilePath, targetFilePath);
        
        try {
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(targetFilePath)
                    .source(
                        CopySource.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(sourceFilePath)
                            .build()
                    )
                    .build()
            );
            
            String fileUrl = generateFileUrl(targetFilePath);
            log.info("文件复制成功: target={}, url={}", targetFilePath, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("文件复制失败: source={}, target={}, error={}", 
                sourceFilePath, targetFilePath, e.getMessage(), e);
            throw new FileUploadException("复制文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量删除文件
     */
    public void batchDeleteFiles(java.util.List<String> filePaths) {
        log.info("批量删除文件: count={}", filePaths.size());
        
        for (String filePath : filePaths) {
            try {
                deleteFile(filePath);
            } catch (Exception e) {
                log.warn("删除文件失败: filePath={}, error={}", filePath, e.getMessage());
            }
        }
        
        log.info("批量删除文件完成");
    }
    
    /**
     * 获取文件流
     */
    public InputStream getFileStream(String filePath) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(filePath)
                    .build()
            );
        } catch (Exception e) {
            throw new FileUploadException("获取文件流失败: " + e.getMessage(), e);
        }
    }
}