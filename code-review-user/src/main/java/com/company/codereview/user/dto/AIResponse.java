package com.company.codereview.user.dto;

import lombok.Data;

/**
 * AI Response DTO
 */
@Data
public class AIResponse {
    
    /**
     * Response content
     */
    private String content;
    
    /**
     * Model used
     */
    private String model;
    
    /**
     * Tokens used
     */
    private Integer tokensUsed;
    
    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;
    
    /**
     * Success flag
     */
    private Boolean success;
    
    /**
     * Error message if any
     */
    private String errorMessage;
    
    /**
     * Response time in milliseconds
     */
    private Long responseTimeMs;
    
    /**
     * Create a successful response
     */
    public static AIResponse success(String content, String model) {
        AIResponse response = new AIResponse();
        response.setContent(content);
        response.setModel(model);
        response.setSuccess(true);
        return response;
    }
    
    /**
     * Create an error response
     */
    public static AIResponse error(String errorMessage) {
        AIResponse response = new AIResponse();
        response.setErrorMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
    
    /**
     * Check if the response is successful
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
} 