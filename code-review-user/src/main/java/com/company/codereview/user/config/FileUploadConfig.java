package com.company.codereview.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 文件上传配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    
    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize;
    
    /**
     * 允许的文件类型
     */
    private List<String> allowedTypes;
    
    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions;
    
    /**
     * 文件路径前缀
     */
    private String pathPrefix;
    
    /**
     * 截图文件路径
     */
    private String screenshotPath;
    
    /**
     * 文档文件路径
     */
    private String documentPath;
    
    /**
     * 临时文件路径
     */
    private String tempPath;
}