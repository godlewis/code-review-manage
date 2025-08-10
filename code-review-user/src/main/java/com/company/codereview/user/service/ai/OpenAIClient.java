package com.company.codereview.user.service.ai;

import com.company.codereview.user.config.AIServiceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * OpenAI客户端实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIClient implements AIClient {
    
    private final AIServiceConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public CompletableFuture<AIResponse> chatCompletion(AIRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                // 构建OpenAI请求
                OpenAIRequest openAIRequest = buildOpenAIRequest(request);
                
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(config.getApiKey());
                
                HttpEntity<OpenAIRequest> entity = new HttpEntity<>(openAIRequest, headers);
                
                // 发送请求
                String url = config.getBaseUrl() + "/chat/completions";
                ResponseEntity<OpenAIResponse> response = restTemplate.postForEntity(url, entity, OpenAIResponse.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    OpenAIResponse openAIResponse = response.getBody();
                    
                    if (openAIResponse.getChoices() != null && !openAIResponse.getChoices().isEmpty()) {
                        String content = openAIResponse.getChoices().get(0).getMessage().getContent();
                        
                        AIResponse aiResponse = AIResponse.success(content, request.getModel());
                        aiResponse.setResponseTimeMs(System.currentTimeMillis() - startTime);
                        
                        if (openAIResponse.getUsage() != null) {
                            aiResponse.setTokensUsed(openAIResponse.getUsage().getTotalTokens());
                        }
                        
                        log.info("OpenAI请求成功: model={}, tokens={}, time={}ms", 
                                request.getModel(), aiResponse.getTokensUsed(), aiResponse.getResponseTimeMs());
                        
                        return aiResponse;
                    }
                }
                
                log.error("OpenAI响应格式错误: {}", response.getBody());
                return AIResponse.error("AI服务响应格式错误");
                
            } catch (Exception e) {
                log.error("OpenAI请求失败", e);
                return AIResponse.error("AI服务请求失败: " + e.getMessage());
            }
        });
    }
    
    @Override
    public boolean isHealthy() {
        try {
            // 发送简单的健康检查请求
            AIRequest testRequest = new AIRequest();
            testRequest.setModel(config.getDefaultModel());
            testRequest.setMessages(Arrays.asList(Message.user("Hello")));
            testRequest.setMaxTokens(10);
            
            AIResponse response = chatCompletion(testRequest).get();
            return response.isSuccess();
        } catch (Exception e) {
            log.warn("AI服务健康检查失败", e);
            return false;
        }
    }
    
    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList(
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-16k",
            "gpt-4",
            "gpt-4-32k"
        );
    }
    
    private OpenAIRequest buildOpenAIRequest(AIRequest request) {
        OpenAIRequest openAIRequest = new OpenAIRequest();
        openAIRequest.setModel(request.getModel());
        openAIRequest.setTemperature(request.getTemperature());
        openAIRequest.setMaxTokens(request.getMaxTokens());
        
        // 转换消息格式
        List<OpenAIMessage> messages = request.getMessages().stream()
                .map(msg -> new OpenAIMessage(msg.getRole(), msg.getContent()))
                .collect(Collectors.toList());
        
        // 如果有系统提示词，添加到消息开头
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
            messages.add(0, new OpenAIMessage("system", request.getSystemPrompt()));
        }
        
        openAIRequest.setMessages(messages);
        
        return openAIRequest;
    }
    
    // OpenAI API 数据结构
    @Data
    public static class OpenAIRequest {
        private String model;
        private List<OpenAIMessage> messages;
        private double temperature;
        @JsonProperty("max_tokens")
        private int maxTokens;
    }
    
    @Data
    public static class OpenAIResponse {
        private String id;
        private String object;
        private long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;
        
        @Data
        public static class Choice {
            private int index;
            private OpenAIMessage message;
            @JsonProperty("finish_reason")
            private String finishReason;
        }
        
        @Data
        public static class Usage {
            @JsonProperty("prompt_tokens")
            private int promptTokens;
            @JsonProperty("completion_tokens")
            private int completionTokens;
            @JsonProperty("total_tokens")
            private int totalTokens;
        }
    }
    
    @Data
    public static class OpenAIMessage {
        private String role;
        private String content;
        
        public OpenAIMessage() {}
        
        public OpenAIMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}