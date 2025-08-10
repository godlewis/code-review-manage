package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.codereview.user.dto.NotificationRuleDTO;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationRule;
import com.company.codereview.user.repository.NotificationRuleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知规则管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRuleService {
    
    private final NotificationRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 分页查询通知规则
     */
    @Cacheable(value = "notification-rules-page", key = "#page + '-' + #size + '-' + #notificationType + '-' + #enabled")
    public IPage<NotificationRuleDTO> getRules(int page, int size, 
                                              Notification.NotificationType notificationType,
                                              Boolean enabled) {
        Page<NotificationRule> pageRequest = new Page<>(page, size);
        QueryWrapper<NotificationRule> queryWrapper = new QueryWrapper<>();
        
        if (notificationType != null) {
            queryWrapper.eq("notification_type", notificationType);
        }
        
        if (enabled != null) {
            queryWrapper.eq("is_enabled", enabled);
        }
        
        queryWrapper.orderByDesc("priority").orderByAsc("created_at");
        
        IPage<NotificationRule> rulePage = ruleRepository.selectPage(pageRequest, queryWrapper);
        
        // 转换为DTO
        IPage<NotificationRuleDTO> dtoPage = new Page<>(page, size, rulePage.getTotal());
        List<NotificationRuleDTO> dtoList = rulePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 根据ID获取规则
     */
    @Cacheable(value = "notification-rule", key = "#id")
    public Optional<NotificationRuleDTO> getRule(Long id) {
        Optional<NotificationRule> ruleOpt = Optional.ofNullable(ruleRepository.selectById(id));
        return ruleOpt.map(this::convertToDTO);
    }
    
    /**
     * 创建通知规则
     */
    @Transactional
    @CacheEvict(value = {"notification-rules", "notification-rules-page", "enabled-rules"}, allEntries = true)
    public Long createRule(NotificationRuleDTO ruleDTO) {
        log.info("创建通知规则: {}", ruleDTO.getRuleName());
        
        // 验证规则
        validateRule(ruleDTO);
        
        NotificationRule rule = convertToEntity(ruleDTO);
        ruleRepository.insert(rule);
        
        log.info("通知规则创建成功: ID={}, 名称={}", rule.getId(), rule.getRuleName());
        return rule.getId();
    }
    
    /**
     * 更新通知规则
     */
    @Transactional
    @CacheEvict(value = {"notification-rules", "notification-rules-page", "notification-rule", "enabled-rules"}, allEntries = true)
    public void updateRule(Long id, NotificationRuleDTO ruleDTO) {
        log.info("更新通知规则: ID={}", id);
        
        Optional<NotificationRule> existingOpt = Optional.ofNullable(ruleRepository.selectById(id));
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("规则不存在: " + id);
        }
        
        // 验证规则
        validateRule(ruleDTO);
        
        NotificationRule rule = convertToEntity(ruleDTO);
        rule.setId(id);
        ruleRepository.updateById(rule);
        
        log.info("通知规则更新成功: ID={}, 名称={}", id, rule.getRuleName());
    }
    
    /**
     * 删除通知规则
     */
    @Transactional
    @CacheEvict(value = {"notification-rules", "notification-rules-page", "notification-rule", "enabled-rules"}, allEntries = true)
    public void deleteRule(Long id) {
        log.info("删除通知规则: ID={}", id);
        
        Optional<NotificationRule> existingOpt = Optional.ofNullable(ruleRepository.selectById(id));
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("规则不存在: " + id);
        }
        
        ruleRepository.deleteById(id);
        log.info("通知规则删除成功: ID={}", id);
    }
    
    /**
     * 批量启用/禁用规则
     */
    @Transactional
    @CacheEvict(value = {"notification-rules", "notification-rules-page", "notification-rule", "enabled-rules"}, allEntries = true)
    public void batchUpdateEnabled(List<Long> ids, Boolean enabled) {
        log.info("批量{}规则: IDs={}", enabled ? "启用" : "禁用", ids);
        
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        int count = ruleRepository.batchUpdateEnabled(ids, enabled);
        log.info("批量{}规则完成: 影响{}条记录", enabled ? "启用" : "禁用", count);
    }
    
    /**
     * 获取所有启用的规则
     */
    @Cacheable(value = "enabled-rules")
    public List<NotificationRuleDTO> getEnabledRules() {
        List<NotificationRule> rules = ruleRepository.findAllEnabled();
        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据通知类型获取启用的规则
     */
    @Cacheable(value = "enabled-rules-by-type", key = "#notificationType")
    public List<NotificationRuleDTO> getEnabledRulesByType(Notification.NotificationType notificationType) {
        List<NotificationRule> rules = ruleRepository.findEnabledByType(notificationType);
        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取当前生效的规则
     */
    public List<NotificationRuleDTO> getEffectiveRules() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        List<NotificationRule> rules = ruleRepository.findEffectiveRules(currentTime);
        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 评估规则是否匹配给定条件
     */
    public boolean evaluateRule(NotificationRuleDTO rule, Map<String, Object> context) {
        if (!Boolean.TRUE.equals(rule.getIsEnabled())) {
            return false;
        }
        
        // 检查生效时间
        if (!isRuleEffective(rule)) {
            return false;
        }
        
        // 评估触发条件
        return evaluateTriggerConditions(rule.getTriggerConditions(), context);
    }
    
    /**
     * 获取规则统计信息
     */
    public Map<String, Object> getRuleStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总规则数
        QueryWrapper<NotificationRule> totalWrapper = new QueryWrapper<>();
        long totalCount = ruleRepository.selectCount(totalWrapper);
        statistics.put("totalRules", totalCount);
        
        // 启用规则数
        QueryWrapper<NotificationRule> enabledWrapper = new QueryWrapper<>();
        enabledWrapper.eq("is_enabled", true);
        long enabledCount = ruleRepository.selectCount(enabledWrapper);
        statistics.put("enabledRules", enabledCount);
        
        // 各类型规则数量
        List<Map<String, Object>> typeStats = ruleRepository.countByType();
        statistics.put("rulesByType", typeStats);
        
        return statistics;
    }
    
    /**
     * 初始化默认规则
     */
    @Transactional
    public void initializeDefaultRules() {
        log.info("初始化默认通知规则");
        
        // 检查是否已有规则
        QueryWrapper<NotificationRule> queryWrapper = new QueryWrapper<>();
        long count = ruleRepository.selectCount(queryWrapper);
        
        if (count > 0) {
            log.info("已存在通知规则，跳过初始化");
            return;
        }
        
        // 创建默认规则
        createDefaultRules();
        
        log.info("默认通知规则初始化完成");
    }
    
    // 私有辅助方法
    
    private void validateRule(NotificationRuleDTO ruleDTO) {
        if (!StringUtils.hasText(ruleDTO.getRuleName())) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        
        if (ruleDTO.getNotificationType() == null) {
            throw new IllegalArgumentException("通知类型不能为空");
        }
        
        if (ruleDTO.getTargetUserType() == null) {
            throw new IllegalArgumentException("目标用户类型不能为空");
        }
        
        if (ruleDTO.getPriority() == null || ruleDTO.getPriority() < 1 || ruleDTO.getPriority() > 10) {
            throw new IllegalArgumentException("优先级必须在1-10之间");
        }
        
        // 验证目标用户设置
        if (ruleDTO.getTargetUserType() == NotificationRule.TargetUserType.SPECIFIC) {
            if (CollectionUtils.isEmpty(ruleDTO.getTargetUserIds())) {
                throw new IllegalArgumentException("指定用户类型必须提供目标用户ID列表");
            }
        }
        
        // 验证时间格式
        if (StringUtils.hasText(ruleDTO.getEffectiveStartTime())) {
            validateTimeFormat(ruleDTO.getEffectiveStartTime());
        }
        
        if (StringUtils.hasText(ruleDTO.getEffectiveEndTime())) {
            validateTimeFormat(ruleDTO.getEffectiveEndTime());
        }
    }
    
    private void validateTimeFormat(String time) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new IllegalArgumentException("时间格式错误，应为HH:mm格式: " + time);
        }
    }
    
    private boolean isRuleEffective(NotificationRuleDTO rule) {
        if (!StringUtils.hasText(rule.getEffectiveStartTime()) && 
            !StringUtils.hasText(rule.getEffectiveEndTime())) {
            return true;
        }
        
        LocalTime currentTime = LocalTime.now();
        
        if (StringUtils.hasText(rule.getEffectiveStartTime())) {
            LocalTime startTime = LocalTime.parse(rule.getEffectiveStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            if (currentTime.isBefore(startTime)) {
                return false;
            }
        }
        
        if (StringUtils.hasText(rule.getEffectiveEndTime())) {
            LocalTime endTime = LocalTime.parse(rule.getEffectiveEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
            if (currentTime.isAfter(endTime)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean evaluateTriggerConditions(Map<String, Object> conditions, Map<String, Object> context) {
        if (CollectionUtils.isEmpty(conditions)) {
            return true; // 无条件则总是匹配
        }
        
        // 简单的条件评估逻辑
        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            String key = condition.getKey();
            Object expectedValue = condition.getValue();
            Object actualValue = context.get(key);
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void createDefaultRules() {
        // 评审分配规则
        createDefaultRule(
            "评审任务分配通知规则",
            Notification.NotificationType.REVIEW_ASSIGNED,
            NotificationRule.TargetUserType.REVIEWER,
            Map.of("immediate", true),
            0, 0, 0, 8,
            "当评审任务分配给用户时立即发送通知"
        );
        
        // 问题分配规则
        createDefaultRule(
            "问题分配通知规则",
            Notification.NotificationType.ISSUE_ASSIGNED,
            NotificationRule.TargetUserType.ISSUE_ASSIGNEE,
            Map.of("severity", "HIGH"),
            0, 0, 0, 9,
            "当高严重级别问题分配给用户时立即发送通知"
        );
        
        // 截止时间提醒规则
        createDefaultRule(
            "截止时间提醒规则",
            Notification.NotificationType.DEADLINE_REMINDER,
            NotificationRule.TargetUserType.REVIEWER,
            Map.of("hoursBeforeDeadline", 24),
            0, 0, 0, 7,
            "在截止时间前24小时发送提醒通知"
        );
        
        // 整改提交通知规则
        createDefaultRule(
            "整改提交通知规则",
            Notification.NotificationType.FIX_SUBMITTED,
            NotificationRule.TargetUserType.REVIEWER,
            Map.of("requiresVerification", true),
            0, 0, 0, 6,
            "当问题整改提交需要验证时通知原评审者"
        );
    }
    
    private void createDefaultRule(String name, Notification.NotificationType type,
                                 NotificationRule.TargetUserType targetType,
                                 Map<String, Object> conditions,
                                 int delay, int repeatInterval, int maxRepeat, int priority,
                                 String description) {
        NotificationRule rule = new NotificationRule();
        rule.setRuleName(name);
        rule.setNotificationType(type);
        rule.setTargetUserType(targetType);
        rule.setDelayMinutes(delay);
        rule.setRepeatInterval(repeatInterval);
        rule.setMaxRepeatCount(maxRepeat);
        rule.setPriority(priority);
        rule.setIsEnabled(true);
        rule.setDescription(description);
        
        // 序列化触发条件
        try {
            rule.setTriggerConditions(objectMapper.writeValueAsString(conditions));
        } catch (JsonProcessingException e) {
            log.warn("序列化触发条件失败: {}", e.getMessage());
            rule.setTriggerConditions("{}");
        }
        
        ruleRepository.insert(rule);
    }
    
    private NotificationRuleDTO convertToDTO(NotificationRule entity) {
        NotificationRuleDTO dto = new NotificationRuleDTO();
        dto.setId(entity.getId());
        dto.setRuleName(entity.getRuleName());
        dto.setNotificationType(entity.getNotificationType());
        dto.setTargetUserType(entity.getTargetUserType());
        dto.setDelayMinutes(entity.getDelayMinutes());
        dto.setRepeatInterval(entity.getRepeatInterval());
        dto.setMaxRepeatCount(entity.getMaxRepeatCount());
        dto.setPriority(entity.getPriority());
        dto.setIsEnabled(entity.getIsEnabled());
        dto.setDescription(entity.getDescription());
        dto.setEffectiveStartTime(entity.getEffectiveStartTime());
        dto.setEffectiveEndTime(entity.getEffectiveEndTime());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // 反序列化触发条件
        if (StringUtils.hasText(entity.getTriggerConditions())) {
            try {
                Map<String, Object> conditions = objectMapper.readValue(
                    entity.getTriggerConditions(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                dto.setTriggerConditions(conditions);
            } catch (JsonProcessingException e) {
                log.warn("反序列化触发条件失败: {}", e.getMessage());
                dto.setTriggerConditions(new HashMap<>());
            }
        }
        
        // 反序列化目标用户ID列表
        if (StringUtils.hasText(entity.getTargetUserIds())) {
            try {
                List<Long> userIds = objectMapper.readValue(
                    entity.getTargetUserIds(), 
                    new TypeReference<List<Long>>() {}
                );
                dto.setTargetUserIds(userIds);
            } catch (JsonProcessingException e) {
                log.warn("反序列化目标用户ID列表失败: {}", e.getMessage());
                dto.setTargetUserIds(new ArrayList<>());
            }
        }
        
        return dto;
    }
    
    private NotificationRule convertToEntity(NotificationRuleDTO dto) {
        NotificationRule entity = new NotificationRule();
        entity.setRuleName(dto.getRuleName());
        entity.setNotificationType(dto.getNotificationType());
        entity.setTargetUserType(dto.getTargetUserType());
        entity.setDelayMinutes(dto.getDelayMinutes());
        entity.setRepeatInterval(dto.getRepeatInterval());
        entity.setMaxRepeatCount(dto.getMaxRepeatCount());
        entity.setPriority(dto.getPriority());
        entity.setIsEnabled(dto.getIsEnabled());
        entity.setDescription(dto.getDescription());
        entity.setEffectiveStartTime(dto.getEffectiveStartTime());
        entity.setEffectiveEndTime(dto.getEffectiveEndTime());
        
        // 序列化触发条件
        if (dto.getTriggerConditions() != null) {
            try {
                entity.setTriggerConditions(objectMapper.writeValueAsString(dto.getTriggerConditions()));
            } catch (JsonProcessingException e) {
                log.warn("序列化触发条件失败: {}", e.getMessage());
                entity.setTriggerConditions("{}");
            }
        }
        
        // 序列化目标用户ID列表
        if (dto.getTargetUserIds() != null) {
            try {
                entity.setTargetUserIds(objectMapper.writeValueAsString(dto.getTargetUserIds()));
            } catch (JsonProcessingException e) {
                log.warn("序列化目标用户ID列表失败: {}", e.getMessage());
                entity.setTargetUserIds("[]");
            }
        }
        
        return entity;
    }
}