package com.company.codereview.user.service;

import com.company.codereview.user.config.AssignmentConfig;
import com.company.codereview.user.entity.AssignmentConfigEntity;
import com.company.codereview.user.repository.AssignmentConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * 分配配置服务测试
 */
@ExtendWith(MockitoExtension.class)
class AssignmentConfigServiceTest {
    
    @Mock
    private AssignmentConfig assignmentConfig;
    
    @Mock
    private AssignmentConfigRepository configRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private Validator validator;
    
    @InjectMocks
    private AssignmentConfigService configService;
    
    @BeforeEach
    void setUp() {
        // 设置默认配置
        when(assignmentConfig.getAvoidanceWeeks()).thenReturn(4);
        when(assignmentConfig.getMaxAssignmentsPerWeek()).thenReturn(3);
        when(assignmentConfig.getSkillMatchWeight()).thenReturn(0.4);
        when(assignmentConfig.getLoadBalanceWeight()).thenReturn(0.3);
        when(assignmentConfig.getDiversityWeight()).thenReturn(0.3);
        when(assignmentConfig.getEnableAutoAssignment()).thenReturn(true);
        when(assignmentConfig.getEnableNewUserPriority()).thenReturn(true);
    }
    
    @Test
    void testGetCurrentConfig() {
        // 执行测试
        AssignmentConfig result = configService.getCurrentConfig();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(assignmentConfig, result);
    }
    
    @Test
    void testValidateConfig_Success() {
        // 准备测试数据
        AssignmentConfig config = createValidConfig();
        when(validator.validate(any(AssignmentConfig.class))).thenReturn(Collections.emptySet());
        
        // 执行测试
        AssignmentConfigService.ConfigValidationResult result = configService.validateConfig(config);
        
        // 验证结果
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void testValidateConfig_InvalidWeights() {
        // 准备测试数据 - 权重总和不等于1.0
        AssignmentConfig config = createValidConfig();
        config.setSkillMatchWeight(0.5);
        config.setLoadBalanceWeight(0.4);
        config.setDiversityWeight(0.2); // 总和 = 1.1
        
        when(validator.validate(any(AssignmentConfig.class))).thenReturn(Collections.emptySet());
        
        // 执行测试
        AssignmentConfigService.ConfigValidationResult result = configService.validateConfig(config);
        
        // 验证结果
        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("权重配置无效"));
    }
    
    @Test
    void testValidateConfig_BeanValidationErrors() {
        // 准备测试数据
        AssignmentConfig config = createValidConfig();
        
        // Mock Bean Validation错误
        Set<ConstraintViolation<AssignmentConfig>> violations = new HashSet<>();
        ConstraintViolation<AssignmentConfig> violation = createMockViolation("avoidanceWeeks", "必须在1-12之间");
        violations.add(violation);
        
        when(validator.validate(any(AssignmentConfig.class))).thenReturn(violations);
        
        // 执行测试
        AssignmentConfigService.ConfigValidationResult result = configService.validateConfig(config);
        
        // 验证结果
        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("avoidanceWeeks"));
    }
    
    @Test
    void testApplySpecialRules_FilterPausedUsers() {
        // 准备测试数据
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Mock用户暂停状态
        when(assignmentConfig.isUserPaused(1L)).thenReturn(false);
        when(assignmentConfig.isUserPaused(2L)).thenReturn(true); // 用户2被暂停
        when(assignmentConfig.isUserPaused(3L)).thenReturn(false);
        
        // Mock用户特殊配置
        when(assignmentConfig.getUserSpecialConfig(any())).thenReturn(null);
        
        // 执行测试
        List<Long> result = configService.applySpecialRules(userIds, "reviewer");
        
        // 验证结果
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertFalse(result.contains(2L)); // 被暂停的用户应该被过滤掉
        assertTrue(result.contains(3L));
    }
    
    @Test
    void testApplySpecialRules_FilterReviewerOnly() {
        // 准备测试数据
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        
        // Mock用户暂停状态
        when(assignmentConfig.isUserPaused(any())).thenReturn(false);
        
        // Mock用户特殊配置
        AssignmentConfig.UserSpecialConfig config1 = new AssignmentConfig.UserSpecialConfig();
        config1.setEnabled(true);
        config1.setRevieweeOnly(false);
        
        AssignmentConfig.UserSpecialConfig config2 = new AssignmentConfig.UserSpecialConfig();
        config2.setEnabled(true);
        config2.setRevieweeOnly(true); // 用户2只能作为被评审者
        
        when(assignmentConfig.getUserSpecialConfig(1L)).thenReturn(config1);
        when(assignmentConfig.getUserSpecialConfig(2L)).thenReturn(config2);
        when(assignmentConfig.getUserSpecialConfig(3L)).thenReturn(null);
        
        // 执行测试 - 过滤评审者
        List<Long> result = configService.applySpecialRules(userIds, "reviewer");
        
        // 验证结果
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertFalse(result.contains(2L)); // 只能作为被评审者的用户应该被过滤掉
        assertTrue(result.contains(3L));
    }
    
    @Test
    void testCheckPairRules_ExcludedPair() {
        // 准备测试数据
        Long reviewerId = 1L;
        Long revieweeId = 2L;
        
        // Mock排除配对
        when(assignmentConfig.isExcludedPair(reviewerId, revieweeId)).thenReturn(true);
        
        // 执行测试
        AssignmentConfigService.PairRuleResult result = configService.checkPairRules(reviewerId, revieweeId);
        
        // 验证结果
        assertFalse(result.isAllowed());
        assertEquals("EXCLUDE", result.getRuleType());
        assertNotNull(result.getReason());
    }
    
    @Test
    void testCheckPairRules_ForcedPair() {
        // 准备测试数据
        Long reviewerId = 1L;
        Long revieweeId = 2L;
        
        // Mock强制配对
        when(assignmentConfig.isExcludedPair(reviewerId, revieweeId)).thenReturn(false);
        
        AssignmentConfig.ForcePair forcePair = new AssignmentConfig.ForcePair();
        forcePair.setReviewerId(reviewerId);
        forcePair.setRevieweeId(revieweeId);
        forcePair.setPriority(5);
        forcePair.setEnabled(true);
        
        when(assignmentConfig.getActiveForcePairs()).thenReturn(Arrays.asList(forcePair));
        
        // 执行测试
        AssignmentConfigService.PairRuleResult result = configService.checkPairRules(reviewerId, revieweeId);
        
        // 验证结果
        assertTrue(result.isAllowed());
        assertTrue(result.isForced());
        assertEquals(5, result.getPriority());
        assertEquals("FORCE", result.getRuleType());
    }
    
    @Test
    void testGetConfigSummary() {
        // 执行测试
        AssignmentConfigService.ConfigSummary summary = configService.getConfigSummary();
        
        // 验证结果
        assertNotNull(summary);
        assertEquals(4, summary.getAvoidanceWeeks());
        assertEquals(3, summary.getMaxAssignmentsPerWeek());
        assertEquals(0.4, summary.getSkillMatchWeight());
        assertEquals(0.3, summary.getLoadBalanceWeight());
        assertEquals(0.3, summary.getDiversityWeight());
        assertTrue(summary.getEnableAutoAssignment());
        assertTrue(summary.getEnableNewUserPriority());
    }
    
    @Test
    void testUpdateDynamicConfig() {
        // 准备测试数据
        AssignmentConfig.TeamSpecialConfig teamConfig = new AssignmentConfig.TeamSpecialConfig();
        teamConfig.setTeamId(1L);
        teamConfig.setTeamName("测试团队");
        teamConfig.setAvoidanceWeeks(6);
        teamConfig.setEnabled(true);
        
        // Mock repository行为
        when(configRepository.selectOne(any())).thenReturn(null);
        when(configRepository.insert(any())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> {
            configService.updateDynamicConfig("team_special_config", teamConfig, "TEAM", 1L, 1L);
        });
        
        // 验证repository调用
        verify(configRepository, times(1)).selectOne(any());
        verify(configRepository, times(1)).insert(any());
    }
    
    @Test
    void testUpdateTeamSpecialConfig() {
        // 准备测试数据
        AssignmentConfig.TeamSpecialConfig teamConfig = new AssignmentConfig.TeamSpecialConfig();
        teamConfig.setTeamId(1L);
        teamConfig.setTeamName("测试团队");
        teamConfig.setAvoidanceWeeks(6);
        teamConfig.setEnabled(true);
        
        // Mock repository行为
        when(configRepository.selectOne(any())).thenReturn(null);
        when(configRepository.insert(any())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> {
            configService.updateTeamSpecialConfig(1L, teamConfig, 1L);
        });
        
        // 验证repository调用
        verify(configRepository, times(1)).selectOne(any());
        verify(configRepository, times(1)).insert(any());
    }
    
    @Test
    void testUpdateUserSpecialConfig() {
        // 准备测试数据
        AssignmentConfig.UserSpecialConfig userConfig = new AssignmentConfig.UserSpecialConfig();
        userConfig.setUserId(1L);
        userConfig.setUsername("testuser");
        userConfig.setMaxAssignmentsPerWeek(5);
        userConfig.setEnabled(true);
        
        // Mock repository行为
        when(configRepository.selectOne(any())).thenReturn(null);
        when(configRepository.insert(any())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> {
            configService.updateUserSpecialConfig(1L, userConfig, 1L);
        });
        
        // 验证repository调用
        verify(configRepository, times(1)).selectOne(any());
        verify(configRepository, times(1)).insert(any());
    }
    
    @Test
    void testUpdateExcludePairs() {
        // 准备测试数据
        AssignmentConfig.ExcludePair excludePair = new AssignmentConfig.ExcludePair();
        excludePair.setUserId1(1L);
        excludePair.setUserId2(2L);
        excludePair.setReason("测试排除");
        excludePair.setEnabled(true);
        
        List<AssignmentConfig.ExcludePair> excludePairs = Arrays.asList(excludePair);
        
        // Mock repository行为
        when(configRepository.selectOne(any())).thenReturn(null);
        when(configRepository.insert(any())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> {
            configService.updateExcludePairs(excludePairs, 1L);
        });
        
        // 验证repository调用
        verify(configRepository, times(1)).selectOne(any());
        verify(configRepository, times(1)).insert(any());
    }
    
    @Test
    void testUpdateForcePairs() {
        // 准备测试数据
        AssignmentConfig.ForcePair forcePair = new AssignmentConfig.ForcePair();
        forcePair.setReviewerId(1L);
        forcePair.setRevieweeId(2L);
        forcePair.setReason("测试强制");
        forcePair.setPriority(5);
        forcePair.setEnabled(true);
        
        List<AssignmentConfig.ForcePair> forcePairs = Arrays.asList(forcePair);
        
        // Mock repository行为
        when(configRepository.selectOne(any())).thenReturn(null);
        when(configRepository.insert(any())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> {
            configService.updateForcePairs(forcePairs, 1L);
        });
        
        // 验证repository调用
        verify(configRepository, times(1)).selectOne(any());
        verify(configRepository, times(1)).insert(any());
    }
    
    @Test
    void testDeleteDynamicConfig() {
        // 准备测试数据
        AssignmentConfigEntity existingConfig = new AssignmentConfigEntity();
        existingConfig.setId(1L);
        existingConfig.setConfigKey("test_config");
        existingConfig.setEnabled(true);
        
        // Mock repository行为
        when(configRepository.selectOne(any())).thenReturn(existingConfig);
        when(configRepository.updateById(any())).thenReturn(1);
        
        // 执行测试
        assertDoesNotThrow(() -> {
            configService.deleteDynamicConfig("test_config", "GLOBAL", null);
        });
        
        // 验证repository调用
        verify(configRepository, times(1)).selectOne(any());
        verify(configRepository, times(1)).updateById(any());
        
        // 验证配置被禁用
        assertFalse(existingConfig.getEnabled());
    }
    
    @Test
    void testGetAllDynamicConfigs() {
        // 准备测试数据
        List<AssignmentConfigEntity> mockConfigs = Arrays.asList(
            createMockConfigEntity("config1", "GLOBAL"),
            createMockConfigEntity("config2", "TEAM")
        );
        
        // Mock repository行为
        when(configRepository.selectList(any())).thenReturn(mockConfigs);
        
        // 执行测试
        List<AssignmentConfigEntity> result = configService.getAllDynamicConfigs();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(configRepository, times(1)).selectList(any());
    }
    
    @Test
    void testGetDynamicConfigsByType() {
        // 准备测试数据
        List<AssignmentConfigEntity> mockConfigs = Arrays.asList(
            createMockConfigEntity("team_config1", "TEAM"),
            createMockConfigEntity("team_config2", "TEAM")
        );
        
        // Mock repository行为
        when(configRepository.findByConfigType("TEAM")).thenReturn(mockConfigs);
        
        // 执行测试
        List<AssignmentConfigEntity> result = configService.getDynamicConfigsByType("TEAM");
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(configRepository, times(1)).findByConfigType("TEAM");
    }
    
    /**
     * 创建有效的配置对象
     */
    private AssignmentConfig createValidConfig() {
        AssignmentConfig config = new AssignmentConfig();
        config.setAvoidanceWeeks(4);
        config.setMaxAssignmentsPerWeek(3);
        config.setSkillMatchWeight(0.4);
        config.setLoadBalanceWeight(0.3);
        config.setDiversityWeight(0.3);
        config.setEnableAutoAssignment(true);
        config.setEnableNewUserPriority(true);
        config.setNewUserThresholdMonths(3);
        config.setExperiencedUserThresholdMonths(6);
        return config;
    }
    
    /**
     * 创建Mock的ConstraintViolation
     */
    @SuppressWarnings("unchecked")
    private ConstraintViolation<AssignmentConfig> createMockViolation(String propertyPath, String message) {
        ConstraintViolation<AssignmentConfig> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(javax.validation.Path.of(propertyPath));
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }
    
    /**
     * 创建Mock的配置实体
     */
    private AssignmentConfigEntity createMockConfigEntity(String configKey, String configType) {
        AssignmentConfigEntity entity = new AssignmentConfigEntity();
        entity.setId(1L);
        entity.setConfigKey(configKey);
        entity.setConfigValue("{}");
        entity.setConfigType(configType);
        entity.setEnabled(true);
        return entity;
    }
}