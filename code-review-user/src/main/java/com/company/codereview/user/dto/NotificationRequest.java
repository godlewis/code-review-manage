package com.company.codereview.user.dto;

import com.company.codereview.user.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 通知发送请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    /**
     * 接收者用户ID列表
     */
    @NotNull(message = "接收者不能为空")
    private List<Long> recipientIds;
    
    /**
     * 通知类型
     */
    @NotNull(message = "通知类型不能为空")
    private Notification.NotificationType notificationType;
    
    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;
    
    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;
    
    /**
     * 关联的业务ID
     */
    private Long relatedId;
    
    /**
     * 关联的业务类型
     */
    private String relatedType;
    
    /**
     * 指定的通知渠道（如果为空则使用用户偏好设置）
     */
    private List<String> channels;
    
    /**
     * 模板变量（用于模板渲染）
     */
    private Map<String, Object> templateVariables;
    
    /**
     * 是否立即发送（否则加入队列异步发送）
     */
    private Boolean immediate;
    
    /**
     * 发送延迟（分钟）
     */
    private Integer delayMinutes;
}