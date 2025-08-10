package com.company.codereview.user.service.notification.channel;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.UserRepository;
import com.company.codereview.user.service.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.util.Optional;

/**
 * 邮件通知渠道
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {
    
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    
    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${notification.email.from:noreply@company.com}")
    private String fromEmail;
    
    @Value("${notification.email.fromName:代码评审系统}")
    private String fromName;
    
    @Override
    public void sendNotification(Notification notification) throws Exception {
        if (!validateNotification(notification)) {
            throw new IllegalArgumentException("通知内容验证失败");
        }
        
        // 获取用户邮箱
        User user = userRepository.selectById(notification.getRecipientId());
        if (user == null) {
            throw new Exception("用户不存在: " + notification.getRecipientId());
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new Exception("用户邮箱为空: " + notification.getRecipientId());
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // 设置发件人
            helper.setFrom(fromEmail, fromName);
            
            // 设置收件人
            helper.setTo(user.getEmail());
            
            // 设置主题
            helper.setSubject(notification.getTitle());
            
            // 设置邮件内容
            String htmlContent = buildEmailContent(notification, user);
            helper.setText(htmlContent, true);
            
            // 发送邮件
            mailSender.send(message);
            
            log.debug("邮件发送成功: 用户={}, 邮箱={}, 通知={}", 
                user.getId(), user.getEmail(), notification.getId());
            
        } catch (Exception e) {
            log.error("邮件发送失败: 用户={}, 邮箱={}, 通知={}", 
                user.getId(), user.getEmail(), notification.getId(), e);
            throw new Exception("邮件发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return emailEnabled;
    }
    
    @Override
    public String getChannelName() {
        return "EMAIL";
    }
    
    @Override
    public String getChannelDescription() {
        return "邮件通知";
    }
    
    private String buildEmailContent(Notification notification, User user) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>").append(notification.getTitle()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #409EFF; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background-color: #f9f9f9; }");
        html.append(".footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }");
        html.append(".button { display: inline-block; padding: 10px 20px; background-color: #409EFF; color: white; text-decoration: none; border-radius: 4px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");
        
        // 头部
        html.append("<div class='header'>");
        html.append("<h1>代码评审管理系统</h1>");
        html.append("</div>");
        
        // 内容
        html.append("<div class='content'>");
        html.append("<h2>").append(notification.getTitle()).append("</h2>");
        html.append("<p>亲爱的 ").append(user.getRealName()).append("：</p>");
        html.append("<p>").append(notification.getContent().replace("\n", "<br>")).append("</p>");
        
        // 如果有关联业务，添加链接
        if (notification.getRelatedId() != null && StringUtils.hasText(notification.getRelatedType())) {
            String linkUrl = buildBusinessLink(notification.getRelatedType(), notification.getRelatedId());
            if (StringUtils.hasText(linkUrl)) {
                html.append("<p>");
                html.append("<a href='").append(linkUrl).append("' class='button'>查看详情</a>");
                html.append("</p>");
            }
        }
        
        html.append("</div>");
        
        // 底部
        html.append("<div class='footer'>");
        html.append("<p>此邮件由系统自动发送，请勿回复。</p>");
        html.append("<p>如有问题，请联系系统管理员。</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
    
    private String buildBusinessLink(String relatedType, Long relatedId) {
        // 根据业务类型构建链接
        String baseUrl = "http://localhost:3000"; // 应该从配置中获取
        
        switch (relatedType) {
            case "REVIEW_RECORD":
                return baseUrl + "/reviews/" + relatedId;
            case "ISSUE":
                return baseUrl + "/issues/" + relatedId;
            case "ASSIGNMENT":
                return baseUrl + "/assignments/" + relatedId;
            default:
                return baseUrl;
        }
    }
}