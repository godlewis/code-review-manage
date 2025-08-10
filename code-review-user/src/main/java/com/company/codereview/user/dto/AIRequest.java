package com.company.codereview.user.dto;

import lombok.Data;

/**
 * AI Request DTO
 */
@Data
public class AIRequest {
    
    /**
     * Request prompt
     */
    private String prompt;
    
    /**
     * Model to use
     */
    private String model;
    
    /**
     * Maximum tokens
     */
    private Integer maxTokens;
    
    /**
     * Temperature
     */
    private Double temperature;
    
    /**
     * System message
     */
    private String systemMessage;
    
    /**
     * Messages list
     */
    private java.util.List<Message> messages;
    
    /**
     * System prompt
     */
    private String systemPrompt;
    
    /**
     * Message class
     */
    @Data
    public static class Message {
        private String role;
        private String content;
        
        public Message() {}
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public static Message user(String content) {
            return new Message("user", content);
        }
        
        public static Message system(String content) {
            return new Message("system", content);
        }
        
        public static Message assistant(String content) {
            return new Message("assistant", content);
        }
    }
} 