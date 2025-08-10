package com.company.codereview.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 文件上传进度跟踪服务
 */
@Slf4j
@Service
public class FileUploadProgressService {
    
    /**
     * 上传进度缓存
     * Key: uploadId, Value: UploadProgress
     */
    private final ConcurrentMap<String, UploadProgress> progressCache = new ConcurrentHashMap<>();
    
    /**
     * 上传进度信息
     */
    public static class UploadProgress {
        private String uploadId;
        private String fileName;
        private long totalSize;
        private long uploadedSize;
        private int percentage;
        private UploadStatus status;
        private String errorMessage;
        private long startTime;
        private long endTime;
        
        public UploadProgress(String uploadId, String fileName, long totalSize) {
            this.uploadId = uploadId;
            this.fileName = fileName;
            this.totalSize = totalSize;
            this.uploadedSize = 0;
            this.percentage = 0;
            this.status = UploadStatus.UPLOADING;
            this.startTime = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getUploadId() { return uploadId; }
        public void setUploadId(String uploadId) { this.uploadId = uploadId; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        
        public long getUploadedSize() { return uploadedSize; }
        public void setUploadedSize(long uploadedSize) { 
            this.uploadedSize = uploadedSize;
            this.percentage = totalSize > 0 ? (int) ((uploadedSize * 100) / totalSize) : 0;
        }
        
        public int getPercentage() { return percentage; }
        public void setPercentage(int percentage) { this.percentage = percentage; }
        
        public UploadStatus getStatus() { return status; }
        public void setStatus(UploadStatus status) { this.status = status; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        
        public long getDuration() {
            return endTime > startTime ? endTime - startTime : System.currentTimeMillis() - startTime;
        }
        
        public double getUploadSpeed() {
            long duration = getDuration();
            return duration > 0 ? (double) uploadedSize / duration * 1000 : 0; // bytes per second
        }
    }
    
    /**
     * 上传状态枚举
     */
    public enum UploadStatus {
        UPLOADING("上传中"),
        COMPLETED("已完成"),
        FAILED("失败"),
        CANCELLED("已取消");
        
        private final String description;
        
        UploadStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 开始上传跟踪
     */
    public UploadProgress startUpload(String uploadId, String fileName, long totalSize) {
        log.debug("开始上传跟踪: uploadId={}, fileName={}, totalSize={}", uploadId, fileName, totalSize);
        
        UploadProgress progress = new UploadProgress(uploadId, fileName, totalSize);
        progressCache.put(uploadId, progress);
        
        return progress;
    }
    
    /**
     * 更新上传进度
     */
    public void updateProgress(String uploadId, long uploadedSize) {
        UploadProgress progress = progressCache.get(uploadId);
        if (progress != null) {
            progress.setUploadedSize(uploadedSize);
            log.debug("更新上传进度: uploadId={}, progress={}%", uploadId, progress.getPercentage());
        }
    }
    
    /**
     * 标记上传完成
     */
    public void markCompleted(String uploadId) {
        UploadProgress progress = progressCache.get(uploadId);
        if (progress != null) {
            progress.setStatus(UploadStatus.COMPLETED);
            progress.setEndTime(System.currentTimeMillis());
            progress.setUploadedSize(progress.getTotalSize());
            
            log.info("上传完成: uploadId={}, fileName={}, duration={}ms", 
                uploadId, progress.getFileName(), progress.getDuration());
        }
    }
    
    /**
     * 标记上传失败
     */
    public void markFailed(String uploadId, String errorMessage) {
        UploadProgress progress = progressCache.get(uploadId);
        if (progress != null) {
            progress.setStatus(UploadStatus.FAILED);
            progress.setErrorMessage(errorMessage);
            progress.setEndTime(System.currentTimeMillis());
            
            log.warn("上传失败: uploadId={}, fileName={}, error={}", 
                uploadId, progress.getFileName(), errorMessage);
        }
    }
    
    /**
     * 标记上传取消
     */
    public void markCancelled(String uploadId) {
        UploadProgress progress = progressCache.get(uploadId);
        if (progress != null) {
            progress.setStatus(UploadStatus.CANCELLED);
            progress.setEndTime(System.currentTimeMillis());
            
            log.info("上传取消: uploadId={}, fileName={}", uploadId, progress.getFileName());
        }
    }
    
    /**
     * 获取上传进度
     */
    public UploadProgress getProgress(String uploadId) {
        return progressCache.get(uploadId);
    }
    
    /**
     * 移除上传进度记录
     */
    public void removeProgress(String uploadId) {
        UploadProgress removed = progressCache.remove(uploadId);
        if (removed != null) {
            log.debug("移除上传进度记录: uploadId={}", uploadId);
        }
    }
    
    /**
     * 清理过期的进度记录
     */
    public void cleanupExpiredProgress() {
        long currentTime = System.currentTimeMillis();
        long expireTime = 24 * 60 * 60 * 1000; // 24小时
        
        progressCache.entrySet().removeIf(entry -> {
            UploadProgress progress = entry.getValue();
            boolean expired = (currentTime - progress.getStartTime()) > expireTime;
            
            if (expired) {
                log.debug("清理过期上传进度记录: uploadId={}, fileName={}", 
                    entry.getKey(), progress.getFileName());
            }
            
            return expired;
        });
    }
    
    /**
     * 获取所有进行中的上传
     */
    public java.util.List<UploadProgress> getActiveUploads() {
        return progressCache.values().stream()
            .filter(progress -> progress.getStatus() == UploadStatus.UPLOADING)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取用户的上传进度（通过uploadId前缀匹配）
     */
    public java.util.List<UploadProgress> getUserUploads(String userIdPrefix) {
        return progressCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(userIdPrefix))
            .map(java.util.Map.Entry::getValue)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 生成上传ID
     */
    public String generateUploadId(Long userId, String fileName) {
        return String.format("%d_%s_%d", userId, 
            fileName.replaceAll("[^a-zA-Z0-9]", "_"), 
            System.currentTimeMillis());
    }
    
    /**
     * 获取上传统计信息
     */
    public UploadStatistics getUploadStatistics() {
        int totalUploads = progressCache.size();
        int activeUploads = 0;
        int completedUploads = 0;
        int failedUploads = 0;
        long totalSize = 0;
        long uploadedSize = 0;
        
        for (UploadProgress progress : progressCache.values()) {
            totalSize += progress.getTotalSize();
            uploadedSize += progress.getUploadedSize();
            
            switch (progress.getStatus()) {
                case UPLOADING:
                    activeUploads++;
                    break;
                case COMPLETED:
                    completedUploads++;
                    break;
                case FAILED:
                    failedUploads++;
                    break;
            }
        }
        
        return new UploadStatistics(totalUploads, activeUploads, completedUploads, 
            failedUploads, totalSize, uploadedSize);
    }
    
    /**
     * 上传统计信息
     */
    public static class UploadStatistics {
        private int totalUploads;
        private int activeUploads;
        private int completedUploads;
        private int failedUploads;
        private long totalSize;
        private long uploadedSize;
        
        public UploadStatistics(int totalUploads, int activeUploads, int completedUploads, 
                              int failedUploads, long totalSize, long uploadedSize) {
            this.totalUploads = totalUploads;
            this.activeUploads = activeUploads;
            this.completedUploads = completedUploads;
            this.failedUploads = failedUploads;
            this.totalSize = totalSize;
            this.uploadedSize = uploadedSize;
        }
        
        // Getters
        public int getTotalUploads() { return totalUploads; }
        public int getActiveUploads() { return activeUploads; }
        public int getCompletedUploads() { return completedUploads; }
        public int getFailedUploads() { return failedUploads; }
        public long getTotalSize() { return totalSize; }
        public long getUploadedSize() { return uploadedSize; }
        
        public int getOverallPercentage() {
            return totalSize > 0 ? (int) ((uploadedSize * 100) / totalSize) : 0;
        }
    }
}