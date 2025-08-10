package com.company.codereview.user.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 匹配度矩阵
 */
@Data
public class MatchingMatrix {
    
    /**
     * 用户列表
     */
    private List<Long> userIds;
    
    /**
     * 匹配度矩阵 [reviewer][reviewee] = score
     */
    private double[][] matrix;
    
    /**
     * 用户ID到矩阵索引的映射
     */
    private Map<Long, Integer> userIndexMap;
    
    /**
     * 矩阵大小
     */
    private int size;
    
    public MatchingMatrix(List<Long> userIds) {
        this.userIds = userIds;
        this.size = userIds.size();
        this.matrix = new double[size][size];
        
        // 构建用户ID到索引的映射
        this.userIndexMap = new java.util.HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            userIndexMap.put(userIds.get(i), i);
        }
    }
    
    /**
     * 设置匹配度分数
     */
    public void setScore(Long reviewerId, Long revieweeId, double score) {
        Integer reviewerIndex = userIndexMap.get(reviewerId);
        Integer revieweeIndex = userIndexMap.get(revieweeId);
        
        if (reviewerIndex != null && revieweeIndex != null) {
            matrix[reviewerIndex][revieweeIndex] = score;
        }
    }
    
    /**
     * 获取匹配度分数
     */
    public double getScore(Long reviewerId, Long revieweeId) {
        Integer reviewerIndex = userIndexMap.get(reviewerId);
        Integer revieweeIndex = userIndexMap.get(revieweeId);
        
        if (reviewerIndex != null && revieweeIndex != null) {
            return matrix[reviewerIndex][revieweeIndex];
        }
        return 0.0;
    }
    
    /**
     * 获取用户在矩阵中的索引
     */
    public Integer getUserIndex(Long userId) {
        return userIndexMap.get(userId);
    }
    
    /**
     * 根据索引获取用户ID
     */
    public Long getUserId(int index) {
        if (index >= 0 && index < userIds.size()) {
            return userIds.get(index);
        }
        return null;
    }
}