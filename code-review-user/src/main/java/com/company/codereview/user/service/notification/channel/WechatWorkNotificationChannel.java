package com.company.codereview.user.service.notification.channel;

import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.UserRepository;
import com.company.codereview.user.service.notification.NotificationChannel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 企业微信通知渠道
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatWorkNotificationChannel implements NotificationChannel {
    
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${notification.wechat.enabled:false}")
    private boolean wechatEnabled;
    
    @Value("${notification.wechat.corpId:}")
    private String corpId;
    
    @Value("${notification.wechat.agentId:}")
    private String agentId;
    
    @Value("${notification.wechat.secret:}")
    private String secret;
    
    @Value("${notification.wechat.tokenUrl:https://qyapi.weixin.qq.com/cgi-bin/gettoken}")
    private String tokenUrl;
    
    @Value("${notification.wechat.sendUrl:https://qyapi.weixin.qq.com/cgi-bin/message/send}")
    private String sendUrl;
    
    @Override
    public void sendNotification(Notification notification) throws Exception {
        if (!validateNotification(notification)) {
            throw new IllegalArgumentException("通知内容验证失败");
        }
        
        // 获取用户信息
        Optional<User> userOpt = userRepository.selectById(notification.getRecipientId());
        if (userOpt.isEmpty()) {
            throw new Exception("用户不存在: " + notification.getRecipientId());
        }
        
        User user = userOpt.get();
        String wechatUserId = getWechatUserId(user);
        if (!StringUtils.hasText(wechatUserId)) {
            throw new Exception("用户企业微信ID为空: " + notification.getRecipientId());
        }
        
        try {
            // 获取访问令牌
            String accessToken = getAccessToken();
            
            // 构建消息内容
            Map<String, Object> message = buildWechatMessage(notification, wechatUserId);
            
            // 发送消息
            sendWechatMessage(accessToken, message);
            
            log.debug("企业微信消息发送成功: 用户={}, 微信ID={}, 通知={}", 
                user.getId(), wechatUserId, notification.getId());
            
        } catch (Exception e) {
            log.error("企业微信消息发送失败: 用户={}, 微信ID={}, 通知={}", 
                user.getId(), wechatUserId, notification.getId(), e);
            throw new Exception("企业微信消息发送失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return wechatEnabled && 
               StringUtils.hasText(corpId) && 
               StringUtils.hasText(agentId) && 
               StringUtils.hasText(secret);
    }
    
    @Override
    public String getChannelName() {
        return "WECHAT_WORK";
    }
    
    @Override
    public String getChannelDescription() {
        return "企业微信通知";
    }
    
    private String getAccessToken() throws Exception {
        String url = tokenUrl + "?corpid=" + corpId + "&corpsecret=" + secret;
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("获取企业微信访问令牌失败: HTTP " + response.getStatusCode());
        }
        
        Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
        Integer errcode = (Integer) result.get("errcode");
        if (errcode != null && errcode != 0) {
            throw new Exception("获取企业微信访问令牌失败: " + result.get("errmsg"));
        }
        
        return (String) result.get("access_token");
    }
    
    private Map<String, Object> buildWechatMessage(Notification notification, String wechatUserId) {
        Map<String, Object> message = new HashMap<>();
        message.put("touser", wechatUserId);
        message.put("msgtype", "textcard");
        message.put("agentid", Integer.parseInt(agentId));
        
        // 构建文本卡片消息
        Map<String, Object> textcard = new HashMap<>();
        textcard.put("title", notification.getTitle());
        textcard.put("description", notification.getContent());
        
        // 如果有关联业务，添加链接
        if (notification.getRelatedId() != null && StringUtils.hasText(notification.getRelatedType())) {
            String linkUrl = buildBusinessLink(notification.getRelatedType(), notification.getRelatedId());
            if (StringUtils.hasText(linkUrl)) {
                textcard.put("url", linkUrl);
                textcard.put("btntxt", "查看详情");
            }
        }
        
        message.put("textcard", textcard);
        
        return message;
    }
    
    private void sendWechatMessage(String accessToken, Map<String, Object> message) throws Exception {
        String url = sendUrl + "?access_token=" + accessToken;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String messageJson = objectMapper.writeValueAsString(message);
        HttpEntity<String> request = new HttpEntity<>(messageJson, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("发送企业微信消息失败: HTTP " + response.getStatusCode());
        }
        
        Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
        Integer errcode = (Integer) result.get("errcode");
        if (errcode != null && errcode != 0) {
            throw new Exception("发送企业微信消息失败: " + result.get("errmsg"));
        }
    }
    
    private String getWechatUserId(User user) {
        // 这里可以根据实际情况获取用户的企业微信ID
        // 可能存储在用户表的扩展字段中，或者通过用户名/邮箱映射
        // 暂时使用用户名作为企业微信ID
        return user.getUsername();
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