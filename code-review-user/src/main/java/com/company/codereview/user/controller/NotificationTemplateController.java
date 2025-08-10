package com.company.codereview.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.company.codereview.common.response.ResponseResult;
import com.company.codereview.user.dto.NotificationTemplateDTO;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationTemplate;
import com.company.codereview.user.service.NotificationTemplateManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 通知模板管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification-templates")
@RequiredArgsConstructor
@Tag(name = "通知模板管理", description = "通知模板管理相关接口")
public class NotificationTemplateController {
    
    private final NotificationTemplateManagementService templateManagementService;
    
    /**
     * 分页查询通知模板
     */
    @GetMapping
    @Operation(summary = "分页查询通知模板", description = "分页查询通知模板列表")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<IPage<NotificationTemplateDTO>> getTemplates(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "通知类型") @RequestParam(required = false) Notification.NotificationType notificationType,
            @Parameter(description = "通知渠道") @RequestParam(required = false) NotificationTemplate.NotificationChannel channel) {
        
        log.info("分页查询通知模板，页码：{}，大小：{}，类型：{}，渠道：{}", page, size, notificationType, channel);
        
        try {
            IPage<NotificationTemplateDTO> templates = templateManagementService.getTemplates(page, size, notificationType, channel);
            return ResponseResult.success(templates);
        } catch (Exception e) {
            log.error("分页查询通知模板失败", e);
            return ResponseResult.error("查询通知模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取模板
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取模板", description = "根据模板ID获取模板详情")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<NotificationTemplateDTO> getTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("获取通知模板详情: {}", id);
        
        try {
            Optional<NotificationTemplateDTO> templateOpt = templateManagementService.getTemplate(id);
            if (templateOpt.isPresent()) {
                return ResponseResult.success(templateOpt.get());
            } else {
                return ResponseResult.error("模板不存在");
            }
        } catch (Exception e) {
            log.error("获取通知模板详情失败", e);
            return ResponseResult.error("获取模板详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建通知模板
     */
    @PostMapping
    @Operation(summary = "创建通知模板", description = "创建新的通知模板")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Long> createTemplate(@Valid @RequestBody NotificationTemplateDTO templateDTO) {
        log.info("创建通知模板: {}", templateDTO.getTemplateName());
        
        try {
            Long templateId = templateManagementService.createTemplate(templateDTO);
            return ResponseResult.success(templateId);
        } catch (Exception e) {
            log.error("创建通知模板失败", e);
            return ResponseResult.error("创建通知模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新通知模板
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新通知模板", description = "更新指定的通知模板")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> updateTemplate(
            @Parameter(description = "模板ID") @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateDTO templateDTO) {
        
        log.info("更新通知模板: {}", id);
        
        try {
            templateManagementService.updateTemplate(id, templateDTO);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("更新通知模板失败", e);
            return ResponseResult.error("更新通知模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除通知模板
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知模板", description = "删除指定的通知模板")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> deleteTemplate(@Parameter(description = "模板ID") @PathVariable Long id) {
        log.info("删除通知模板: {}", id);
        
        try {
            templateManagementService.deleteTemplate(id);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("删除通知模板失败", e);
            return ResponseResult.error("删除通知模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量启用/禁用模板
     */
    @PutMapping("/batch-update-enabled")
    @Operation(summary = "批量启用/禁用模板", description = "批量启用或禁用指定的通知模板")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> batchUpdateEnabled(
            @Parameter(description = "模板ID列表") @RequestParam List<Long> ids,
            @Parameter(description = "是否启用") @RequestParam Boolean enabled) {
        
        log.info("批量{}模板: {}", enabled ? "启用" : "禁用", ids);
        
        try {
            templateManagementService.batchUpdateEnabled(ids, enabled);
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("批量更新模板状态失败", e);
            return ResponseResult.error("批量更新模板状态失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的模板
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的模板", description = "获取所有启用状态的通知模板")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<NotificationTemplateDTO>> getEnabledTemplates() {
        log.info("获取所有启用的通知模板");
        
        try {
            List<NotificationTemplateDTO> templates = templateManagementService.getEnabledTemplates();
            return ResponseResult.success(templates);
        } catch (Exception e) {
            log.error("获取启用的通知模板失败", e);
            return ResponseResult.error("获取启用的模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据通知类型获取启用的模板
     */
    @GetMapping("/enabled/type/{notificationType}")
    @Operation(summary = "根据通知类型获取启用的模板", description = "根据通知类型获取启用状态的模板列表")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<NotificationTemplateDTO>> getEnabledTemplatesByType(
            @Parameter(description = "通知类型") @PathVariable Notification.NotificationType notificationType) {
        
        log.info("获取通知类型 {} 的启用模板", notificationType);
        
        try {
            List<NotificationTemplateDTO> templates = templateManagementService.getEnabledTemplatesByType(notificationType);
            return ResponseResult.success(templates);
        } catch (Exception e) {
            log.error("获取指定类型的启用模板失败", e);
            return ResponseResult.error("获取启用的模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 预览模板渲染效果
     */
    @PostMapping("/preview")
    @Operation(summary = "预览模板渲染效果", description = "使用指定变量预览模板的渲染效果")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<NotificationTemplateDTO> previewTemplate(
            @Valid @RequestBody NotificationTemplateDTO templateDTO,
            @Parameter(description = "模板变量") @RequestParam Map<String, Object> variables) {
        
        log.info("预览通知模板渲染效果: {}", templateDTO.getTemplateName());
        
        try {
            NotificationTemplateDTO preview = templateManagementService.previewTemplate(templateDTO, variables);
            return ResponseResult.success(preview);
        } catch (Exception e) {
            log.error("预览模板渲染效果失败", e);
            return ResponseResult.error("预览模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 初始化默认模板
     */
    @PostMapping("/initialize-defaults")
    @Operation(summary = "初始化默认模板", description = "初始化系统默认的通知模板")
    @PreAuthorize("hasRole('ARCHITECT')")
    public ResponseResult<Void> initializeDefaultTemplates() {
        log.info("初始化默认通知模板");
        
        try {
            templateManagementService.initializeDefaultTemplates();
            return ResponseResult.success();
        } catch (Exception e) {
            log.error("初始化默认模板失败", e);
            return ResponseResult.error("初始化默认模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知类型列表
     */
    @GetMapping("/notification-types")
    @Operation(summary = "获取通知类型列表", description = "获取所有可用的通知类型")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, String>>> getNotificationTypes() {
        log.info("获取通知类型列表");
        
        try {
            List<Map<String, String>> types = java.util.Arrays.stream(Notification.NotificationType.values())
                    .map(type -> Map.of(
                            "value", type.name(),
                            "label", type.getDescription()
                    ))
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseResult.success(types);
        } catch (Exception e) {
            log.error("获取通知类型列表失败", e);
            return ResponseResult.error("获取通知类型列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取通知渠道列表
     */
    @GetMapping("/notification-channels")
    @Operation(summary = "获取通知渠道列表", description = "获取所有可用的通知渠道")
    @PreAuthorize("hasRole('TEAM_LEADER') or hasRole('ARCHITECT')")
    public ResponseResult<List<Map<String, String>>> getNotificationChannels() {
        log.info("获取通知渠道列表");
        
        try {
            List<Map<String, String>> channels = java.util.Arrays.stream(NotificationTemplate.NotificationChannel.values())
                    .map(channel -> Map.of(
                            "value", channel.name(),
                            "label", channel.getDescription()
                    ))
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseResult.success(channels);
        } catch (Exception e) {
            log.error("获取通知渠道列表失败", e);
            return ResponseResult.error("获取通知渠道列表失败：" + e.getMessage());
        }
    }
}