package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 存储的文件名
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalFileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 上传是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 成功结果构造器
     */
    public static FileUploadResult success(String fileUrl, String fileName, String originalFileName, 
                                         Long fileSize, String fileType, String filePath) {
        FileUploadResult result = new FileUploadResult();
        result.setFileUrl(fileUrl);
        result.setFileName(fileName);
        result.setOriginalFileName(originalFileName);
        result.setFileSize(fileSize);
        result.setFileType(fileType);
        result.setFilePath(filePath);
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 失败结果构造器
     */
    public static FileUploadResult failure(String errorMessage) {
        FileUploadResult result = new FileUploadResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}