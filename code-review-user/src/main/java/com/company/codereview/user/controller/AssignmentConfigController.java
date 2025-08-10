package com.company.codereview.user.controller;

import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.config.AssignmentConfig;
import com.company.codereview.user.entity.AssignmentConfigEntity;
import com.company.codereview.user.service.AssignmentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 分配配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/assignment-config")
@RequiredArgsConstructor
@Tag(name = "分配配置管理", description = "评审分配规则配置相关接口")
public class AssignmentConfigController {
    
    private final AssignmentConfigService configService;
    
    /**
     * 获取当前配置
     */
    @GetMapping
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取当前分配配置", description = "获取当前的评审分配规则配置")
    public ResponseEntity<ResponseResult<AssignmentConfig>> getCurrentConfig() {
        try {
            AssignmentConfig config = configService.getCurrentConfig();
            return ResponseEntity.ok(ResponseResult.success(config));
        } catch (Exception e) {
            log.error("获取分配配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取配置摘要
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取配置摘要", description = "获取分配配置的摘要信息")
    public ResponseEntity<ResponseResult<AssignmentConfigService.ConfigSummary>> getConfigSummary() {
        try {
            AssignmentConfigService.ConfigSummary summary = configService.getConfigSummary();
            return ResponseEntity.ok(ResponseResult.success(summary));
        } catch (Exception e) {
            log.error("获取配置摘要失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 验证配置
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "验证配置", description = "验证分配配置是否有效")
    public ResponseEntity<ResponseResult<AssignmentConfigService.ConfigValidationResult>> validateConfig(
            @Valid @RequestBody AssignmentConfig config) {
        try {
            AssignmentConfigService.ConfigValidationResult result = configService.validateConfig(config);
            return ResponseEntity.ok(ResponseResult.success(result));
        } catch (Exception e) {
            log.error("验证配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 检查配对规则
     */
    @GetMapping("/check-pair")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "检查配对规则", description = "检查两个用户之间的配对规则")
    public ResponseEntity<ResponseResult<AssignmentConfigService.PairRuleResult>> checkPairRules(
            @RequestParam Long reviewerId,
            @RequestParam Long revieweeId) {
        try {
            AssignmentConfigService.PairRuleResult result = configService.checkPairRules(reviewerId, revieweeId);
            return ResponseEntity.ok(ResponseResult.success(result));
        } catch (Exception e) {
            log.error("检查配对规则失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 应用特殊规则过滤用户
     */
    @PostMapping("/filter-users")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "应用特殊规则过滤用户", description = "根据特殊规则过滤用户列表")
    public ResponseEntity<ResponseResult<List<Long>>> filterUsers(
            @RequestBody List<Long> userIds,
            @RequestParam String ruleType) {
        try {
            List<Long> filteredUsers = configService.applySpecialRules(userIds, ruleType);
            return ResponseEntity.ok(ResponseResult.success(filteredUsers));
        } catch (Exception e) {
            log.error("过滤用户失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取团队特殊配置
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取团队特殊配置", description = "获取指定团队的特殊配置")
    public ResponseEntity<ResponseResult<AssignmentConfig.TeamSpecialConfig>> getTeamSpecialConfig(
            @PathVariable Long teamId) {
        try {
            AssignmentConfig config = configService.getCurrentConfig();
            AssignmentConfig.TeamSpecialConfig teamConfig = config.getTeamSpecialConfig(teamId);
            return ResponseEntity.ok(ResponseResult.success(teamConfig));
        } catch (Exception e) {
            log.error("获取团队特殊配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取用户特殊配置
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取用户特殊配置", description = "获取指定用户的特殊配置")
    public ResponseEntity<ResponseResult<AssignmentConfig.UserSpecialConfig>> getUserSpecialConfig(
            @PathVariable Long userId) {
        try {
            AssignmentConfig config = configService.getCurrentConfig();
            AssignmentConfig.UserSpecialConfig userConfig = config.getUserSpecialConfig(userId);
            return ResponseEntity.ok(ResponseResult.success(userConfig));
        } catch (Exception e) {
            log.error("获取用户特殊配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取排除配对列表
     */
    @GetMapping("/exclude-pairs")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取排除配对列表", description = "获取所有排除配对规则")
    public ResponseEntity<ResponseResult<List<AssignmentConfig.ExcludePair>>> getExcludePairs() {
        try {
            AssignmentConfig config = configService.getCurrentConfig();
            List<AssignmentConfig.ExcludePair> excludePairs = config.getExcludePairs();
            return ResponseEntity.ok(ResponseResult.success(excludePairs));
        } catch (Exception e) {
            log.error("获取排除配对列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取强制配对列表
     */
    @GetMapping("/force-pairs")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取强制配对列表", description = "获取所有强制配对规则")
    public ResponseEntity<ResponseResult<List<AssignmentConfig.ForcePair>>> getForcePairs() {
        try {
            AssignmentConfig config = configService.getCurrentConfig();
            List<AssignmentConfig.ForcePair> forcePairs = config.getActiveForcePairs();
            return ResponseEntity.ok(ResponseResult.success(forcePairs));
        } catch (Exception e) {
            log.error("获取强制配对列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取配置统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取配置统计信息", description = "获取分配配置的统计信息")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getConfigStatistics() {
        try {
            AssignmentConfig config = configService.getCurrentConfig();
            
            Map<String, Object> statistics = Map.of(
                "totalTeamConfigs", config.getTeamSpecialConfigs() != null ? config.getTeamSpecialConfigs().size() : 0,
                "totalUserConfigs", config.getUserSpecialConfigs() != null ? config.getUserSpecialConfigs().size() : 0,
                "totalExcludePairs", config.getExcludePairs() != null ? config.getExcludePairs().size() : 0,
                "totalForcePairs", config.getForcePairs() != null ? config.getForcePairs().size() : 0,
                "activeExcludePairs", config.getExcludePairs() != null ? 
                    config.getExcludePairs().stream().mapToInt(p -> p.getEnabled() ? 1 : 0).sum() : 0,
                "activeForcePairs", config.getForcePairs() != null ? 
                    config.getForcePairs().stream().mapToInt(p -> p.getEnabled() ? 1 : 0).sum() : 0,
                "pausedUsers", config.getUserSpecialConfigs() != null ? 
                    config.getUserSpecialConfigs().values().stream()
                        .mapToInt(u -> u.getEnabled() && u.getPauseAssignment() ? 1 : 0).sum() : 0
            );
            
            return ResponseEntity.ok(ResponseResult.success(statistics));
        } catch (Exception e) {
            log.error("获取配置统计信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 更新团队特殊配置
     */
    @PutMapping("/team/{teamId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "更新团队特殊配置", description = "更新指定团队的特殊配置")
    public ResponseEntity<ResponseResult<Void>> updateTeamSpecialConfig(
            @PathVariable Long teamId,
            @Valid @RequestBody AssignmentConfig.TeamSpecialConfig teamConfig) {
        try {
            // 获取当前用户ID（实际项目中应从SecurityContext获取）
            Long currentUserId = getCurrentUserId();
            
            configService.updateTeamSpecialConfig(teamId, teamConfig, currentUserId);
            return ResponseEntity.ok(ResponseResult.success(null));
        } catch (Exception e) {
            log.error("更新团队特殊配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 更新用户特殊配置
     */
    @PutMapping("/user/{userId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "更新用户特殊配置", description = "更新指定用户的特殊配置")
    public ResponseEntity<ResponseResult<Void>> updateUserSpecialConfig(
            @PathVariable Long userId,
            @Valid @RequestBody AssignmentConfig.UserSpecialConfig userConfig) {
        try {
            // 获取当前用户ID（实际项目中应从SecurityContext获取）
            Long currentUserId = getCurrentUserId();
            
            configService.updateUserSpecialConfig(userId, userConfig, currentUserId);
            return ResponseEntity.ok(ResponseResult.success(null));
        } catch (Exception e) {
            log.error("更新用户特殊配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 更新排除配对规则
     */
    @PutMapping("/exclude-pairs")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "更新排除配对规则", description = "批量更新排除配对规则")
    public ResponseEntity<ResponseResult<Void>> updateExcludePairs(
            @Valid @RequestBody List<AssignmentConfig.ExcludePair> excludePairs) {
        try {
            // 获取当前用户ID（实际项目中应从SecurityContext获取）
            Long currentUserId = getCurrentUserId();
            
            configService.updateExcludePairs(excludePairs, currentUserId);
            return ResponseEntity.ok(ResponseResult.success(null));
        } catch (Exception e) {
            log.error("更新排除配对规则失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 更新强制配对规则
     */
    @PutMapping("/force-pairs")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "更新强制配对规则", description = "批量更新强制配对规则")
    public ResponseEntity<ResponseResult<Void>> updateForcePairs(
            @Valid @RequestBody List<AssignmentConfig.ForcePair> forcePairs) {
        try {
            // 获取当前用户ID（实际项目中应从SecurityContext获取）
            Long currentUserId = getCurrentUserId();
            
            configService.updateForcePairs(forcePairs, currentUserId);
            return ResponseEntity.ok(ResponseResult.success(null));
        } catch (Exception e) {
            log.error("更新强制配对规则失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 删除团队特殊配置
     */
    @DeleteMapping("/team/{teamId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "删除团队特殊配置", description = "删除指定团队的特殊配置")
    public ResponseEntity<ResponseResult<Void>> deleteTeamSpecialConfig(@PathVariable Long teamId) {
        try {
            configService.deleteDynamicConfig("team_special_config", "TEAM", teamId);
            return ResponseEntity.ok(ResponseResult.success(null));
        } catch (Exception e) {
            log.error("删除团队特殊配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 删除用户特殊配置
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "删除用户特殊配置", description = "删除指定用户的特殊配置")
    public ResponseEntity<ResponseResult<Void>> deleteUserSpecialConfig(@PathVariable Long userId) {
        try {
            configService.deleteDynamicConfig("user_special_config", "USER", userId);
            return ResponseEntity.ok(ResponseResult.success(null));
        } catch (Exception e) {
            log.error("删除用户特殊配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取所有动态配置
     */
    @GetMapping("/dynamic")
    @PreAuthorize("hasRole('ARCHITECT')")
    @Operation(summary = "获取所有动态配置", description = "获取所有动态配置信息（仅架构师可访问）")
    public ResponseEntity<ResponseResult<List<AssignmentConfigEntity>>> getAllDynamicConfigs() {
        try {
            List<AssignmentConfigEntity> configs = configService.getAllDynamicConfigs();
            return ResponseEntity.ok(ResponseResult.success(configs));
        } catch (Exception e) {
            log.error("获取动态配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 根据类型获取动态配置
     */
    @GetMapping("/dynamic/{configType}")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "根据类型获取动态配置", description = "根据配置类型获取动态配置")
    public ResponseEntity<ResponseResult<List<AssignmentConfigEntity>>> getDynamicConfigsByType(
            @PathVariable String configType) {
        try {
            List<AssignmentConfigEntity> configs = configService.getDynamicConfigsByType(configType);
            return ResponseEntity.ok(ResponseResult.success(configs));
        } catch (Exception e) {
            log.error("获取动态配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取合并后的完整配置
     */
    @GetMapping("/merged")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    @Operation(summary = "获取合并后的完整配置", description = "获取应用了动态配置的完整配置")
    public ResponseEntity<ResponseResult<AssignmentConfig>> getMergedConfig() {
        try {
            AssignmentConfig baseConfig = configService.getCurrentConfig();
            AssignmentConfig mergedConfig = configService.applyDynamicConfigs(baseConfig);
            return ResponseEntity.ok(ResponseResult.success(mergedConfig));
        } catch (Exception e) {
            log.error("获取合并配置失败: {}", e.getMessage());
            return ResponseEntity.ok(ResponseResult.error(500, e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户ID（临时实现，实际项目中应从SecurityContext获取）
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 1L; // 临时返回固定值
    }
}