package com.company.codereview.user.service.notification.channel;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.UserRepository;
import com.company.codereview.user.service.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 短信通知渠道
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationChannel implements NotificationChannel {
    
    private final UserRepository userRepository;
    
    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${notification.sms.provider:aliyun}")
    private String smsProvider;
    
    @Value("${notification.sms.accessKey:}")
    private String accessKey;
    
    @Value("${notification.sms.accessSecret:}")
    private String accessSecret;
    
    @Value("${notification.sms.signName:代码评审系统}")
    private String signName;
    
    @Value("${notification.sms.templateCode:}")
    private String templateCode;
    
    @Override
    public void sendNotification(Notification notification) throws Exception {
        if (!validateNotification(notification)) {
            throw new IllegalArgumentException("通知内容验证失败");
        }
        
        // 获取用户手机号
        Optional<User> userOpt = userRepository.selectById(notification.getRecipientId());
        if (userOpt.isEmpty()) {
            throw new Exception("用户不存在: " + notification.getRecipientId());
        }
        
        User user = userOpt.get();
        if (!StringUtils.hasText(user.getPhone())) {
            throw new Exception("用户手机号为空: " + notification.getRecipientId());
        }
        
        try {
            // 构建短信内容
            String smsContent = buildSmsContent(notification);
            
            // 发送短信
            sendSms(user.getPhone(), smsContent);
            
            log.debug("短信发送成功: 用户={}, 手机号={}, 通知={}", 
                user.getId(), maskPhone(user.getPhone()), notification.getId());
            
        } catch (Exception e) {
            log.error("短信发送失败: 用户={}, 手机号={}, 通知={}", 
                user.getId(), maskPhone(user.getPhone()), notification.getId(), e);
            throw new Exception("短信发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return smsEnabled && 
               StringUtils.hasText(accessKey) && 
               StringUtils.hasText(accessSecret) && 
               StringUtils.hasText(templateCode);
    }
    
    @Override
    public String getChannelName() {
        return "SMS";
    }
    
    @Override
    public String getChannelDescription() {
        return "短信通知";
    }
    
    private String buildSmsContent(Notification notification) {
        // 短信内容需要简洁，通常有字数限制
        String content = notification.getContent();
        if (content.length() > 60) {
            content = content.substring(0, 57) + "...";
        }
        
        return String.format("【%s】%s：%s", signName, notification.getTitle(), content);
    }
    
    private void sendSms(String phoneNumber, String content) throws Exception {
        // 这里应该集成实际的短信服务提供商API
        // 比如阿里云短信、腾讯云短信等
        
        switch (smsProvider.toLowerCase()) {
            case "aliyun":
                sendAliyunSms(phoneNumber, content);
                break;
            case "tencent":
                sendTencentSms(phoneNumber, content);
                break;
            case "mock":
                // 模拟发送，用于测试
                mockSendSms(phoneNumber, content);
                break;
            default:
                throw new Exception("不支持的短信服务提供商: " + smsProvider);
        }
    }
    
    private void sendAliyunSms(String phoneNumber, String content) throws Exception {
        // TODO: 集成阿里云短信服务
        // 这里应该使用阿里云短信SDK发送短信
        log.info("使用阿里云发送短信到 {}: {}", maskPhone(phoneNumber), content);
        
        // 模拟发送
        if (Math.random() > 0.1) { // 90%成功率
            log.debug("阿里云短信发送成功");
        } else {
            throw new Exception("阿里云短信发送失败: 网络错误");
        }
    }
    
    private void sendTencentSms(String phoneNumber, String content) throws Exception {
        // TODO: 集成腾讯云短信服务
        // 这里应该使用腾讯云短信SDK发送短信
        log.info("使用腾讯云发送短信到 {}: {}", maskPhone(phoneNumber), content);
        
        // 模拟发送
        if (Math.random() > 0.1) { // 90%成功率
            log.debug("腾讯云短信发送成功");
        } else {
            throw new Exception("腾讯云短信发送失败: 网络错误");
        }
    }
    
    private void mockSendSms(String phoneNumber, String content) throws Exception {
        // 模拟短信发送，用于开发和测试
        log.info("模拟发送短信到 {}: {}", maskPhone(phoneNumber), content);
        
        // 模拟网络延迟
        Thread.sleep(100);
        
        // 模拟发送结果
        if (Math.random() > 0.05) { // 95%成功率
            log.debug("模拟短信发送成功");
        } else {
            throw new Exception("模拟短信发送失败: 随机失败");
        }
    }
    
    private String maskPhone(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}