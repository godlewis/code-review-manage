package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.codereview.user.config.AssignmentConfig;
import com.company.codereview.user.entity.AssignmentConfigEntity;
import com.company.codereview.user.repository.AssignmentConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分配配置管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentConfigService {
    
    private final AssignmentConfig assignmentConfig;
    private final AssignmentConfigRepository configRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    
    /**
     * 获取当前配置
     */
    public AssignmentConfig getCurrentConfig() {
        return assignmentConfig;
    }
    
    /**
     * 验证配置是否有效
     */
    public ConfigValidationResult validateConfig(AssignmentConfig config) {
        ConfigValidationResult result = new ConfigValidationResult();
        
        // 使用Bean Validation验证
        Set<ConstraintViolation<AssignmentConfig>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            List<String> errors = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
            result.setValid(false);
            result.setErrors(errors);
            return result;
        }
        
        // 自定义验证逻辑
        List<String> customErrors = new ArrayList<>();
        
        // 验证权重配置
        if (!config.isWeightConfigValid()) {
            customErrors.add("权重配置无效：技能匹配权重 + 负载均衡权重 + 多样性权重 必须等于 1.0");
        }
        
        // 验证团队特殊配置
        if (config.getTeamSpecialConfigs() != null) {
            for (AssignmentConfig.TeamSpecialConfig teamConfig : config.getTeamSpecialConfigs().values()) {
                if (teamConfig.getEnabled()) {
                    validateTeamSpecialConfig(teamConfig, customErrors);
                }
            }
        }
        
        // 验证用户特殊配置
        if (config.getUserSpecialConfigs() != null) {
            for (AssignmentConfig.UserSpecialConfig userConfig : config.getUserSpecialConfigs().values()) {
                if (userConfig.getEnabled()) {
                    validateUserSpecialConfig(userConfig, customErrors);
                }
            }
        }
        
        // 验证排除配对
        if (config.getExcludePairs() != null) {
            validateExcludePairs(config.getExcludePairs(), customErrors);
        }
        
        // 验证强制配对
        if (config.getForcePairs() != null) {
            validateForcePairs(config.getForcePairs(), customErrors);
        }
        
        result.setValid(customErrors.isEmpty());
        result.setErrors(customErrors);
        
        return result;
    }
    
    /**
     * 验证团队特殊配置
     */
    private void validateTeamSpecialConfig(AssignmentConfig.TeamSpecialConfig config, List<String> errors) {
        if (config.getAvoidanceWeeks() != null && (config.getAvoidanceWeeks() < 1 || config.getAvoidanceWeeks() > 12)) {
            errors.add("团队 " + config.getTeamId() + " 的避重周期必须在1-12周之间");
        }
        
        if (config.getMaxAssignmentsPerWeek() != null && (config.getMaxAssignmentsPerWeek() < 1 || config.getMaxAssignmentsPerWeek() > 10)) {
            errors.add("团队 " + config.getTeamId() + " 的每周最大任务数必须在1-10之间");
        }
        
        // 验证权重配置
        if (config.getSkillMatchWeight() != null && config.getLoadBalanceWeight() != null && config.getDiversityWeight() != null) {
            double totalWeight = config.getSkillMatchWeight() + config.getLoadBalanceWeight() + config.getDiversityWeight();
            if (Math.abs(totalWeight - 1.0) > 0.001) {
                errors.add("团队 " + config.getTeamId() + " 的权重配置无效：总和必须等于1.0");
            }
        }
    }
    
    /**
     * 验证用户特殊配置
     */
    private void validateUserSpecialConfig(AssignmentConfig.UserSpecialConfig config, List<String> errors) {
        if (config.getMaxAssignmentsPerWeek() != null && (config.getMaxAssignmentsPerWeek() < 0 || config.getMaxAssignmentsPerWeek() > 10)) {
            errors.add("用户 " + config.getUserId() + " 的每周最大任务数必须在0-10之间");
        }
        
        if (config.getReviewerOnly() && config.getRevieweeOnly()) {
            errors.add("用户 " + config.getUserId() + " 不能同时设置为只能作为评审者和只能作为被评审者");
        }
        
        if (config.getPauseAssignment() && (config.getPauseReason() == null || config.getPauseReason().trim().isEmpty())) {
            errors.add("用户 " + config.getUserId() + " 暂停分配时必须提供暂停原因");
        }
    }
    
    /**
     * 验证排除配对
     */
    private void validateExcludePairs(List<AssignmentConfig.ExcludePair> excludePairs, List<String> errors) {
        for (AssignmentConfig.ExcludePair pair : excludePairs) {
            if (pair.getUserId1().equals(pair.getUserId2())) {
                errors.add("排除配对中用户不能是同一个人：" + pair.getUserId1());
            }
            
            if (pair.getStartDate() != null && pair.getEndDate() != null) {
                try {
                    LocalDate startDate = LocalDate.parse(pair.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate endDate = LocalDate.parse(pair.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    if (startDate.isAfter(endDate)) {
                        errors.add("排除配对的开始时间不能晚于结束时间：" + pair.getUserId1() + " - " + pair.getUserId2());
                    }
                } catch (Exception e) {
                    errors.add("排除配对的日期格式无效：" + pair.getUserId1() + " - " + pair.getUserId2());
                }
            }
        }
    }
    
    /**
     * 验证强制配对
     */
    private void validateForcePairs(List<AssignmentConfig.ForcePair> forcePairs, List<String> errors) {
        for (AssignmentConfig.ForcePair pair : forcePairs) {
            if (pair.getReviewerId().equals(pair.getRevieweeId())) {
                errors.add("强制配对中评审者和被评审者不能是同一个人：" + pair.getReviewerId());
            }
            
            if (pair.getPriority() < 1 || pair.getPriority() > 10) {
                errors.add("强制配对的优先级必须在1-10之间：" + pair.getReviewerId() + " -> " + pair.getRevieweeId());
            }
            
            if (pair.getStartDate() != null && pair.getEndDate() != null) {
                try {
                    LocalDate startDate = LocalDate.parse(pair.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate endDate = LocalDate.parse(pair.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                    if (startDate.isAfter(endDate)) {
                        errors.add("强制配对的开始时间不能晚于结束时间：" + pair.getReviewerId() + " -> " + pair.getRevieweeId());
                    }
                } catch (Exception e) {
                    errors.add("强制配对的日期格式无效：" + pair.getReviewerId() + " -> " + pair.getRevieweeId());
                }
            }
        }
    }
    
    /**
     * 应用特殊规则过滤用户列表
     */
    public List<Long> applySpecialRules(List<Long> userIds, String ruleType) {
        return userIds.stream()
            .filter(userId -> !assignmentConfig.isUserPaused(userId))
            .filter(userId -> {
                AssignmentConfig.UserSpecialConfig config = assignmentConfig.getUserSpecialConfig(userId);
                if (config == null || !config.getEnabled()) {
                    return true;
                }
                
                // 根据规则类型过滤
                switch (ruleType) {
                    case "reviewer":
                        return !config.getRevieweeOnly();
                    case "reviewee":
                        return !config.getReviewerOnly();
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 检查配对是否被特殊规则影响
     */
    public PairRuleResult checkPairRules(Long reviewerId, Long revieweeId) {
        PairRuleResult result = new PairRuleResult();
        result.setAllowed(true);
        
        // 检查排除配对
        if (assignmentConfig.isExcludedPair(reviewerId, revieweeId)) {
            result.setAllowed(false);
            result.setReason("该配对在排除列表中");
            result.setRuleType("EXCLUDE");
            return result;
        }
        
        // 检查强制配对
        List<AssignmentConfig.ForcePair> forcePairs = assignmentConfig.getActiveForcePairs();
        for (AssignmentConfig.ForcePair forcePair : forcePairs) {
            if (forcePair.getReviewerId().equals(reviewerId) && forcePair.getRevieweeId().equals(revieweeId)) {
                result.setForced(true);
                result.setPriority(forcePair.getPriority());
                result.setReason("该配对在强制列表中，优先级：" + forcePair.getPriority());
                result.setRuleType("FORCE");
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 获取有效的配置摘要
     */
    public ConfigSummary getConfigSummary() {
        ConfigSummary summary = new ConfigSummary();
        summary.setAvoidanceWeeks(assignmentConfig.getAvoidanceWeeks());
        summary.setMaxAssignmentsPerWeek(assignmentConfig.getMaxAssignmentsPerWeek());
        summary.setSkillMatchWeight(assignmentConfig.getSkillMatchWeight());
        summary.setLoadBalanceWeight(assignmentConfig.getLoadBalanceWeight());
        summary.setDiversityWeight(assignmentConfig.getDiversityWeight());
        summary.setEnableAutoAssignment(assignmentConfig.getEnableAutoAssignment());
        summary.setEnableNewUserPriority(assignmentConfig.getEnableNewUserPriority());
        
        // 统计特殊配置数量
        summary.setTeamSpecialConfigCount(
            assignmentConfig.getTeamSpecialConfigs() != null ? 
            assignmentConfig.getTeamSpecialConfigs().size() : 0
        );
        summary.setUserSpecialConfigCount(
            assignmentConfig.getUserSpecialConfigs() != null ? 
            assignmentConfig.getUserSpecialConfigs().size() : 0
        );
        summary.setExcludePairCount(
            assignmentConfig.getExcludePairs() != null ? 
            assignmentConfig.getExcludePairs().size() : 0
        );
        summary.setForcePairCount(
            assignmentConfig.getForcePairs() != null ? 
            assignmentConfig.getForcePairs().size() : 0
        );
        
        return summary;
    }
    
    /**
     * 动态更新配置
     */
    @Transactional
    @CacheEvict(value = "assignment-config", allEntries = true)
    public void updateDynamicConfig(String configKey, Object configValue, String configType, Long relatedId, Long updatedBy) {
        try {
            String jsonValue = objectMapper.writeValueAsString(configValue);
            
            // 查找现有配置
            LambdaQueryWrapper<AssignmentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AssignmentConfigEntity::getConfigKey, configKey)
                       .eq(AssignmentConfigEntity::getConfigType, configType);
            
            if (relatedId != null) {
                queryWrapper.eq(AssignmentConfigEntity::getRelatedId, relatedId);
            }
            
            AssignmentConfigEntity existingConfig = configRepository.selectOne(queryWrapper);
            
            if (existingConfig != null) {
                // 更新现有配置
                existingConfig.setConfigValue(jsonValue);
                existingConfig.setUpdatedBy(updatedBy);
                configRepository.updateById(existingConfig);
                log.info("更新动态配置成功: configKey={}, configType={}, relatedId={}", configKey, configType, relatedId);
            } else {
                // 创建新配置
                AssignmentConfigEntity newConfig = new AssignmentConfigEntity();
                newConfig.setConfigKey(configKey);
                newConfig.setConfigValue(jsonValue);
                newConfig.setConfigType(configType);
                newConfig.setRelatedId(relatedId);
                newConfig.setEnabled(true);
                newConfig.setCreatedBy(updatedBy);
                newConfig.setUpdatedBy(updatedBy);
                configRepository.insert(newConfig);
                log.info("创建动态配置成功: configKey={}, configType={}, relatedId={}", configKey, configType, relatedId);
            }
        } catch (JsonProcessingException e) {
            log.error("序列化配置值失败: configKey={}, error={}", configKey, e.getMessage());
            throw new RuntimeException("配置值序列化失败", e);
        }
    }
    
    /**
     * 获取动态配置
     */
    @Cacheable(value = "assignment-config", key = "#configKey + '-' + #configType + '-' + (#relatedId ?: 'null')")
    public <T> T getDynamicConfig(String configKey, String configType, Long relatedId, Class<T> valueType) {
        try {
            LambdaQueryWrapper<AssignmentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AssignmentConfigEntity::getConfigKey, configKey)
                       .eq(AssignmentConfigEntity::getConfigType, configType)
                       .eq(AssignmentConfigEntity::getEnabled, true);
            
            if (relatedId != null) {
                queryWrapper.eq(AssignmentConfigEntity::getRelatedId, relatedId);
            }
            
            AssignmentConfigEntity config = configRepository.selectOne(queryWrapper);
            
            if (config != null) {
                return objectMapper.readValue(config.getConfigValue(), valueType);
            }
            
            return null;
        } catch (JsonProcessingException e) {
            log.error("反序列化配置值失败: configKey={}, error={}", configKey, e.getMessage());
            throw new RuntimeException("配置值反序列化失败", e);
        }
    }
    
    /**
     * 删除动态配置
     */
    @Transactional
    @CacheEvict(value = "assignment-config", allEntries = true)
    public void deleteDynamicConfig(String configKey, String configType, Long relatedId) {
        LambdaQueryWrapper<AssignmentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssignmentConfigEntity::getConfigKey, configKey)
                   .eq(AssignmentConfigEntity::getConfigType, configType);
        
        if (relatedId != null) {
            queryWrapper.eq(AssignmentConfigEntity::getRelatedId, relatedId);
        }
        
        AssignmentConfigEntity config = configRepository.selectOne(queryWrapper);
        if (config != null) {
            config.setEnabled(false);
            configRepository.updateById(config);
            log.info("删除动态配置成功: configKey={}, configType={}, relatedId={}", configKey, configType, relatedId);
        }
    }
    
    /**
     * 批量更新团队特殊配置
     */
    @Transactional
    @CacheEvict(value = "assignment-config", allEntries = true)
    public void updateTeamSpecialConfig(Long teamId, AssignmentConfig.TeamSpecialConfig teamConfig, Long updatedBy) {
        updateDynamicConfig("team_special_config", teamConfig, "TEAM", teamId, updatedBy);
    }
    
    /**
     * 批量更新用户特殊配置
     */
    @Transactional
    @CacheEvict(value = "assignment-config", allEntries = true)
    public void updateUserSpecialConfig(Long userId, AssignmentConfig.UserSpecialConfig userConfig, Long updatedBy) {
        updateDynamicConfig("user_special_config", userConfig, "USER", userId, updatedBy);
    }
    
    /**
     * 批量更新排除配对规则
     */
    @Transactional
    @CacheEvict(value = "assignment-config", allEntries = true)
    public void updateExcludePairs(List<AssignmentConfig.ExcludePair> excludePairs, Long updatedBy) {
        updateDynamicConfig("exclude_pairs", excludePairs, "GLOBAL", null, updatedBy);
    }
    
    /**
     * 批量更新强制配对规则
     */
    @Transactional
    @CacheEvict(value = "assignment-config", allEntries = true)
    public void updateForcePairs(List<AssignmentConfig.ForcePair> forcePairs, Long updatedBy) {
        updateDynamicConfig("force_pairs", forcePairs, "GLOBAL", null, updatedBy);
    }
    
    /**
     * 获取所有动态配置
     */
    public List<AssignmentConfigEntity> getAllDynamicConfigs() {
        LambdaQueryWrapper<AssignmentConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssignmentConfigEntity::getEnabled, true)
                   .orderByDesc(AssignmentConfigEntity::getUpdatedAt);
        return configRepository.selectList(queryWrapper);
    }
    
    /**
     * 根据配置类型获取动态配置
     */
    public List<AssignmentConfigEntity> getDynamicConfigsByType(String configType) {
        return configRepository.findByConfigType(configType);
    }
    
    /**
     * 应用动态配置到当前配置对象
     */
    public AssignmentConfig applyDynamicConfigs(AssignmentConfig baseConfig) {
        try {
            // 获取所有动态配置
            List<AssignmentConfigEntity> dynamicConfigs = getAllDynamicConfigs();
            
            // 创建配置副本
            AssignmentConfig mergedConfig = objectMapper.readValue(
                objectMapper.writeValueAsString(baseConfig), 
                AssignmentConfig.class
            );
            
            // 应用动态配置
            for (AssignmentConfigEntity configEntity : dynamicConfigs) {
                applyDynamicConfigToObject(mergedConfig, configEntity);
            }
            
            return mergedConfig;
        } catch (JsonProcessingException e) {
            log.error("应用动态配置失败: {}", e.getMessage());
            return baseConfig; // 返回原始配置作为降级方案
        }
    }
    
    /**
     * 将单个动态配置应用到配置对象
     */
    private void applyDynamicConfigToObject(AssignmentConfig config, AssignmentConfigEntity configEntity) {
        try {
            String configKey = configEntity.getConfigKey();
            String configValue = configEntity.getConfigValue();
            String configType = configEntity.getConfigType();
            Long relatedId = configEntity.getRelatedId();
            
            switch (configKey) {
                case "team_special_config":
                    if ("TEAM".equals(configType) && relatedId != null) {
                        AssignmentConfig.TeamSpecialConfig teamConfig = 
                            objectMapper.readValue(configValue, AssignmentConfig.TeamSpecialConfig.class);
                        if (config.getTeamSpecialConfigs() == null) {
                            config.setTeamSpecialConfigs(new HashMap<>());
                        }
                        config.getTeamSpecialConfigs().put(relatedId, teamConfig);
                    }
                    break;
                    
                case "user_special_config":
                    if ("USER".equals(configType) && relatedId != null) {
                        AssignmentConfig.UserSpecialConfig userConfig = 
                            objectMapper.readValue(configValue, AssignmentConfig.UserSpecialConfig.class);
                        if (config.getUserSpecialConfigs() == null) {
                            config.setUserSpecialConfigs(new HashMap<>());
                        }
                        config.getUserSpecialConfigs().put(relatedId, userConfig);
                    }
                    break;
                    
                case "exclude_pairs":
                    if ("GLOBAL".equals(configType)) {
                        List<AssignmentConfig.ExcludePair> excludePairs = 
                            objectMapper.readValue(configValue, 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, AssignmentConfig.ExcludePair.class));
                        config.setExcludePairs(excludePairs);
                    }
                    break;
                    
                case "force_pairs":
                    if ("GLOBAL".equals(configType)) {
                        List<AssignmentConfig.ForcePair> forcePairs = 
                            objectMapper.readValue(configValue, 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, AssignmentConfig.ForcePair.class));
                        config.setForcePairs(forcePairs);
                    }
                    break;
                    
                default:
                    log.warn("未知的配置键: {}", configKey);
                    break;
            }
        } catch (JsonProcessingException e) {
            log.error("应用动态配置失败: configKey={}, error={}", configEntity.getConfigKey(), e.getMessage());
        }
    }
    
    /**
     * 配置验证结果
     */
    public static class ConfigValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        
        // getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
    
    /**
     * 配对规则检查结果
     */
    public static class PairRuleResult {
        private boolean allowed;
        private boolean forced = false;
        private Integer priority = 0;
        private String reason;
        private String ruleType;
        
        // getters and setters
        public boolean isAllowed() { return allowed; }
        public void setAllowed(boolean allowed) { this.allowed = allowed; }
        public boolean isForced() { return forced; }
        public void setForced(boolean forced) { this.forced = forced; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    }
    
    /**
     * 配置摘要
     */
    public static class ConfigSummary {
        private Integer avoidanceWeeks;
        private Integer maxAssignmentsPerWeek;
        private Double skillMatchWeight;
        private Double loadBalanceWeight;
        private Double diversityWeight;
        private Boolean enableAutoAssignment;
        private Boolean enableNewUserPriority;
        private Integer teamSpecialConfigCount;
        private Integer userSpecialConfigCount;
        private Integer excludePairCount;
        private Integer forcePairCount;
        
        // getters and setters
        public Integer getAvoidanceWeeks() { return avoidanceWeeks; }
        public void setAvoidanceWeeks(Integer avoidanceWeeks) { this.avoidanceWeeks = avoidanceWeeks; }
        public Integer getMaxAssignmentsPerWeek() { return maxAssignmentsPerWeek; }
        public void setMaxAssignmentsPerWeek(Integer maxAssignmentsPerWeek) { this.maxAssignmentsPerWeek = maxAssignmentsPerWeek; }
        public Double getSkillMatchWeight() { return skillMatchWeight; }
        public void setSkillMatchWeight(Double skillMatchWeight) { this.skillMatchWeight = skillMatchWeight; }
        public Double getLoadBalanceWeight() { return loadBalanceWeight; }
        public void setLoadBalanceWeight(Double loadBalanceWeight) { this.loadBalanceWeight = loadBalanceWeight; }
        public Double getDiversityWeight() { return diversityWeight; }
        public void setDiversityWeight(Double diversityWeight) { this.diversityWeight = diversityWeight; }
        public Boolean getEnableAutoAssignment() { return enableAutoAssignment; }
        public void setEnableAutoAssignment(Boolean enableAutoAssignment) { this.enableAutoAssignment = enableAutoAssignment; }
        public Boolean getEnableNewUserPriority() { return enableNewUserPriority; }
        public void setEnableNewUserPriority(Boolean enableNewUserPriority) { this.enableNewUserPriority = enableNewUserPriority; }
        public Integer getTeamSpecialConfigCount() { return teamSpecialConfigCount; }
        public void setTeamSpecialConfigCount(Integer teamSpecialConfigCount) { this.teamSpecialConfigCount = teamSpecialConfigCount; }
        public Integer getUserSpecialConfigCount() { return userSpecialConfigCount; }
        public void setUserSpecialConfigCount(Integer userSpecialConfigCount) { this.userSpecialConfigCount = userSpecialConfigCount; }
        public Integer getExcludePairCount() { return excludePairCount; }
        public void setExcludePairCount(Integer excludePairCount) { this.excludePairCount = excludePairCount; }
        public Integer getForcePairCount() { return forcePairCount; }
        public void setForcePairCount(Integer forcePairCount) { this.forcePairCount = forcePairCount; }
    }
}