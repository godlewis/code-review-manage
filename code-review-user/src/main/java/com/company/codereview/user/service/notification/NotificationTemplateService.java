package com.company.codereview.user.service.notification;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationTemplate;
import com.company.codereview.user.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {
    
    private final NotificationTemplateRepository templateRepository;
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 根据通知类型和渠道获取模板
     */
    @Cacheable(value = "notification-templates", key = "#notificationType + '-' + #channel")
    public Optional<NotificationTemplate> getTemplate(Notification.NotificationType notificationType,
                                                     NotificationTemplate.NotificationChannel channel) {
        return templateRepository.findByTypeAndChannel(notificationType, channel);
    }
    
    /**
     * 渲染模板内容
     */
    public String renderTemplate(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template) || CollectionUtils.isEmpty(variables)) {
            return template;
        }
        
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            
            if (value != null) {
                String replacement = value.toString();
                result = result.replace("${" + variableName + "}", replacement);
            } else {
                log.warn("模板变量 {} 未找到对应的值", variableName);
                // 保留原始变量标记或替换为空字符串
                result = result.replace("${" + variableName + "}", "");
            }
        }
        
        return result;
    }
    
    /**
     * 使用模板渲染通知内容
     */
    public Notification renderNotificationWithTemplate(Notification notification, 
                                                      NotificationTemplate.NotificationChannel channel,
                                                      Map<String, Object> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplate(notification.getNotificationType(), channel);
        
        if (templateOpt.isEmpty()) {
            log.debug("未找到通知模板: type={}, channel={}", notification.getNotificationType(), channel);
            return notification;
        }
        
        NotificationTemplate template = templateOpt.get();
        
        // 渲染标题
        if (StringUtils.hasText(template.getTitleTemplate())) {
            String renderedTitle = renderTemplate(template.getTitleTemplate(), variables);
            notification.setTitle(renderedTitle);
        }
        
        // 渲染内容
        if (StringUtils.hasText(template.getContentTemplate())) {
            String renderedContent = renderTemplate(template.getContentTemplate(), variables);
            notification.setContent(renderedContent);
        }
        
        return notification;
    }
    
    /**
     * 验证模板语法
     */
    public boolean validateTemplate(String template) {
        if (!StringUtils.hasText(template)) {
            return true;
        }
        
        try {
            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            while (matcher.find()) {
                String variableName = matcher.group(1);
                if (!StringUtils.hasText(variableName)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("模板语法验证失败: {}", template, e);
            return false;
        }
    }
    
    /**
     * 提取模板中的变量
     */
    public java.util.Set<String> extractVariables(String template) {
        java.util.Set<String> variables = new java.util.HashSet<>();
        
        if (!StringUtils.hasText(template)) {
            return variables;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            if (StringUtils.hasText(variableName)) {
                variables.add(variableName);
            }
        }
        
        return variables;
    }
}