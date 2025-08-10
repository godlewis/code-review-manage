package com.company.codereview.user.util;

import com.company.codereview.user.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件验证工具类
 */
@Slf4j
public class FileValidationUtil {
    
    /**
     * 图片文件魔数
     */
    private static final List<byte[]> IMAGE_MAGIC_NUMBERS = Arrays.asList(
        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}, // JPEG
        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}, // PNG
        new byte[]{0x47, 0x49, 0x46, 0x38}, // GIF
        new byte[]{0x42, 0x4D}, // BMP
        new byte[]{0x49, 0x49, 0x2A, 0x00}, // TIFF (little endian)
        new byte[]{0x4D, 0x4D, 0x00, 0x2A} // TIFF (big endian)
    );
    
    /**
     * PDF文件魔数
     */
    private static final byte[] PDF_MAGIC_NUMBER = {0x25, 0x50, 0x44, 0x46}; // %PDF
    
    /**
     * 最大图片尺寸
     */
    private static final int MAX_IMAGE_WIDTH = 4096;
    private static final int MAX_IMAGE_HEIGHT = 4096;
    
    /**
     * 验证文件是否为有效的图片
     */
    public static boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        try {
            // 检查文件头魔数
            byte[] fileHeader = new byte[8];
            int bytesRead = file.getInputStream().read(fileHeader);
            
            if (bytesRead < 4) {
                return false;
            }
            
            boolean isImage = IMAGE_MAGIC_NUMBERS.stream()
                .anyMatch(magic -> startsWith(fileHeader, magic));
            
            if (!isImage) {
                return false;
            }
            
            // 尝试读取图片验证完整性
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                return false;
            }
            
            // 检查图片尺寸
            if (image.getWidth() > MAX_IMAGE_WIDTH || image.getHeight() > MAX_IMAGE_HEIGHT) {
                log.warn("图片尺寸超过限制: {}x{}, 最大允许: {}x{}", 
                    image.getWidth(), image.getHeight(), MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            log.error("验证图片文件失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证文件是否为有效的PDF
     */
    public static boolean isValidPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        try {
            byte[] fileHeader = new byte[4];
            int bytesRead = file.getInputStream().read(fileHeader);
            
            return bytesRead >= 4 && startsWith(fileHeader, PDF_MAGIC_NUMBER);
            
        } catch (IOException e) {
            log.error("验证PDF文件失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证文件扩展名
     */
    public static boolean hasValidExtension(String fileName, List<String> allowedExtensions) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        return allowedExtensions.stream()
            .anyMatch(allowed -> allowed.toLowerCase().equals(extension));
    }
    
    /**
     * 验证文件大小
     */
    public static boolean isValidSize(MultipartFile file, long maxSize) {
        return file != null && file.getSize() <= maxSize && file.getSize() > 0;
    }
    
    /**
     * 验证文件名
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // 检查文件名长度
        if (fileName.length() > 255) {
            return false;
        }
        
        // 检查非法字符
        String[] illegalChars = {"<", ">", ":", "\"", "|", "?", "*", "\\", "/"};
        for (String illegalChar : illegalChars) {
            if (fileName.contains(illegalChar)) {
                return false;
            }
        }
        
        // 检查保留名称
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
            "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", 
            "LPT6", "LPT7", "LPT8", "LPT9"};
        
        String nameWithoutExtension = fileName.contains(".") ? 
            fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
        
        for (String reserved : reservedNames) {
            if (reserved.equalsIgnoreCase(nameWithoutExtension)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 清理文件名
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed_file";
        }
        
        // 替换非法字符
        String sanitized = fileName.replaceAll("[<>:\"|?*\\\\/]", "_");
        
        // 限制长度
        if (sanitized.length() > 255) {
            String extension = getFileExtension(sanitized);
            String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf("."));
            int maxNameLength = 255 - extension.length();
            sanitized = nameWithoutExt.substring(0, Math.min(nameWithoutExt.length(), maxNameLength)) + extension;
        }
        
        // 确保不为空
        if (sanitized.trim().isEmpty()) {
            sanitized = "unnamed_file";
        }
        
        return sanitized;
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    /**
     * 获取文件名（不含扩展名）
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
    
    /**
     * 检查字节数组是否以指定的字节序列开头
     */
    private static boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 验证图片尺寸
     */
    public static boolean isValidImageDimensions(MultipartFile file, int maxWidth, int maxHeight) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return false;
            }
            
            return image.getWidth() <= maxWidth && image.getHeight() <= maxHeight;
            
        } catch (IOException e) {
            log.error("验证图片尺寸失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取图片尺寸信息
     */
    public static ImageDimensions getImageDimensions(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return null;
            }
            
            return new ImageDimensions(image.getWidth(), image.getHeight());
            
        } catch (IOException e) {
            log.error("获取图片尺寸失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 图片尺寸信息
     */
    public static class ImageDimensions {
        private final int width;
        private final int height;
        
        public ImageDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        
        @Override
        public String toString() {
            return width + "x" + height;
        }
    }
    
    /**
     * 综合验证文件
     */
    public static void validateFile(MultipartFile file, List<String> allowedTypes, 
                                  List<String> allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("文件不能为空");
        }
        
        // 验证文件名
        if (!isValidFileName(file.getOriginalFilename())) {
            throw new FileUploadException("文件名包含非法字符");
        }
        
        // 验证文件大小
        if (!isValidSize(file, maxSize)) {
            throw new FileUploadException("文件大小超过限制，最大允许 " + (maxSize / 1024 / 1024) + "MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new FileUploadException("不支持的文件类型: " + contentType);
        }
        
        // 验证文件扩展名
        if (!hasValidExtension(file.getOriginalFilename(), allowedExtensions)) {
            throw new FileUploadException("不支持的文件扩展名");
        }
        
        // 对于图片文件，进行额外验证
        if (contentType.startsWith("image/")) {
            if (!isValidImage(file)) {
                throw new FileUploadException("无效的图片文件");
            }
        }
        
        // 对于PDF文件，进行额外验证
        if ("application/pdf".equals(contentType)) {
            if (!isValidPdf(file)) {
                throw new FileUploadException("无效的PDF文件");
            }
        }
    }
}