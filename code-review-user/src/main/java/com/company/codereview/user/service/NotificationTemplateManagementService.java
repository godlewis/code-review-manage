package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.codereview.user.dto.NotificationTemplateDTO;
import com.company.codereview.user.entity.Notification;
import com.company.codereview.user.entity.NotificationTemplate;
import com.company.codereview.user.repository.NotificationTemplateRepository;
import com.company.codereview.user.service.notification.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知模板管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateManagementService {
    
    private final NotificationTemplateRepository templateRepository;
    private final NotificationTemplateService templateService;
    
    /**
     * 分页查询通知模板
     */
    @Cacheable(value = "notification-templates-page", key = "#page + '-' + #size + '-' + #notificationType + '-' + #channel")
    public IPage<NotificationTemplateDTO> getTemplates(int page, int size, 
                                                      Notification.NotificationType notificationType,
                                                      NotificationTemplate.NotificationChannel channel) {
        Page<NotificationTemplate> pageRequest = new Page<>(page, size);
        QueryWrapper<NotificationTemplate> queryWrapper = new QueryWrapper<>();
        
        if (notificationType != null) {
            queryWrapper.eq("notification_type", notificationType);
        }
        
        if (channel != null) {
            queryWrapper.eq("channel", channel);
        }
        
        queryWrapper.orderByDesc("created_at");
        
        IPage<NotificationTemplate> templatePage = templateRepository.selectPage(pageRequest, queryWrapper);
        
        // 转换为DTO
        IPage<NotificationTemplateDTO> dtoPage = new Page<>(page, size, templatePage.getTotal());
        List<NotificationTemplateDTO> dtoList = templatePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    /**
     * 根据ID获取模板
     */
    @Cacheable(value = "notification-template", key = "#id")
    public Optional<NotificationTemplateDTO> getTemplate(Long id) {
        Optional<NotificationTemplate> templateOpt = Optional.ofNullable(templateRepository.selectById(id));
        return templateOpt.map(this::convertToDTO);
    }
    
    /**
     * 创建通知模板
     */
    @Transactional
    @CacheEvict(value = {"notification-templates", "notification-templates-page"}, allEntries = true)
    public Long createTemplate(NotificationTemplateDTO templateDTO) {
        log.info("创建通知模板: {}", templateDTO.getTemplateName());
        
        // 验证模板内容
        validateTemplate(templateDTO);
        
        // 检查是否已存在相同类型和渠道的模板
        Optional<NotificationTemplate> existingOpt = templateRepository
                .findByTypeAndChannel(templateDTO.getNotificationType(), templateDTO.getChannel());
        
        if (existingOpt.isPresent()) {
            throw new IllegalArgumentException("该通知类型和渠道的模板已存在");
        }
        
        NotificationTemplate template = convertToEntity(templateDTO);
        templateRepository.insert(template);
        
        log.info("通知模板创建成功: ID={}, 名称={}", template.getId(), template.getTemplateName());
        return template.getId();
    }
    
    /**
     * 更新通知模板
     */
    @Transactional
    @CacheEvict(value = {"notification-templates", "notification-templates-page", "notification-template"}, allEntries = true)
    public void updateTemplate(Long id, NotificationTemplateDTO templateDTO) {
        log.info("更新通知模板: ID={}", id);
        
        Optional<NotificationTemplate> existingOpt = Optional.ofNullable(templateRepository.selectById(id));
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("模板不存在: " + id);
        }
        
        // 验证模板内容
        validateTemplate(templateDTO);
        
        NotificationTemplate template = convertToEntity(templateDTO);
        template.setId(id);
        templateRepository.updateById(template);
        
        log.info("通知模板更新成功: ID={}, 名称={}", id, template.getTemplateName());
    }
    
    /**
     * 删除通知模板
     */
    @Transactional
    @CacheEvict(value = {"notification-templates", "notification-templates-page", "notification-template"}, allEntries = true)
    public void deleteTemplate(Long id) {
        log.info("删除通知模板: ID={}", id);
        
        Optional<NotificationTemplate> existingOpt = Optional.ofNullable(templateRepository.selectById(id));
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("模板不存在: " + id);
        }
        
        templateRepository.deleteById(id);
        log.info("通知模板删除成功: ID={}", id);
    }
    
    /**
     * 批量启用/禁用模板
     */
    @Transactional
    @CacheEvict(value = {"notification-templates", "notification-templates-page", "notification-template"}, allEntries = true)
    public void batchUpdateEnabled(List<Long> ids, Boolean enabled) {
        log.info("批量{}模板: IDs={}", enabled ? "启用" : "禁用", ids);
        
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        int count = templateRepository.batchUpdateEnabled(ids, enabled);
        log.info("批量{}模板完成: 影响{}条记录", enabled ? "启用" : "禁用", count);
    }
    
    /**
     * 获取所有启用的模板
     */
    @Cacheable(value = "enabled-templates")
    public List<NotificationTemplateDTO> getEnabledTemplates() {
        List<NotificationTemplate> templates = templateRepository.findAllEnabled();
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据通知类型获取启用的模板
     */
    @Cacheable(value = "enabled-templates-by-type", key = "#notificationType")
    public List<NotificationTemplateDTO> getEnabledTemplatesByType(Notification.NotificationType notificationType) {
        List<NotificationTemplate> templates = templateRepository.findEnabledByType(notificationType);
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 预览模板渲染效果
     */
    public NotificationTemplateDTO previewTemplate(NotificationTemplateDTO templateDTO, 
                                                  java.util.Map<String, Object> variables) {
        // 验证模板语法
        validateTemplate(templateDTO);
        
        // 渲染模板
        String renderedTitle = templateService.renderTemplate(templateDTO.getTitleTemplate(), variables);
        String renderedContent = templateService.renderTemplate(templateDTO.getContentTemplate(), variables);
        
        // 创建预览结果
        NotificationTemplateDTO preview = new NotificationTemplateDTO();
        preview.setTemplateName(templateDTO.getTemplateName());
        preview.setNotificationType(templateDTO.getNotificationType());
        preview.setChannel(templateDTO.getChannel());
        preview.setTitleTemplate(templateDTO.getTitleTemplate());
        preview.setContentTemplate(templateDTO.getContentTemplate());
        preview.setRenderedTitle(renderedTitle);
        preview.setRenderedContent(renderedContent);
        
        return preview;
    }
    
    /**
     * 初始化默认模板
     */
    @Transactional
    public void initializeDefaultTemplates() {
        log.info("初始化默认通知模板");
        
        // 检查是否已有模板
        QueryWrapper<NotificationTemplate> queryWrapper = new QueryWrapper<>();
        long count = templateRepository.selectCount(queryWrapper);
        
        if (count > 0) {
            log.info("已存在通知模板，跳过初始化");
            return;
        }
        
        // 创建默认模板
        createDefaultTemplates();
        
        log.info("默认通知模板初始化完成");
    }
    
    // 私有辅助方法
    
    private void validateTemplate(NotificationTemplateDTO templateDTO) {
        if (!StringUtils.hasText(templateDTO.getTemplateName())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        
        if (templateDTO.getNotificationType() == null) {
            throw new IllegalArgumentException("通知类型不能为空");
        }
        
        if (templateDTO.getChannel() == null) {
            throw new IllegalArgumentException("通知渠道不能为空");
        }
        
        if (!StringUtils.hasText(templateDTO.getTitleTemplate())) {
            throw new IllegalArgumentException("标题模板不能为空");
        }
        
        if (!StringUtils.hasText(templateDTO.getContentTemplate())) {
            throw new IllegalArgumentException("内容模板不能为空");
        }
        
        // 验证模板语法
        if (!templateService.validateTemplate(templateDTO.getTitleTemplate())) {
            throw new IllegalArgumentException("标题模板语法错误");
        }
        
        if (!templateService.validateTemplate(templateDTO.getContentTemplate())) {
            throw new IllegalArgumentException("内容模板语法错误");
        }
    }
    
    private void createDefaultTemplates() {
        // 评审分配通知模板
        createDefaultTemplate(
            Notification.NotificationType.REVIEW_ASSIGNED,
            NotificationTemplate.NotificationChannel.IN_APP,
            "评审任务分配通知",
            "您有新的评审任务",
            "您被分配了一个新的代码评审任务：${reviewTitle}，请及时处理。"
        );
        
        createDefaultTemplate(
            Notification.NotificationType.REVIEW_ASSIGNED,
            NotificationTemplate.NotificationChannel.EMAIL,
            "评审任务分配邮件通知",
            "代码评审任务分配 - ${reviewTitle}",
            "您好，${userName}！\\n\\n您被分配了一个新的代码评审任务：\\n\\n任务标题：${reviewTitle}\\n提交人：${submitter}\\n截止时间：${deadline}\\n\\n请登录系统查看详情并及时完成评审。"
        );
        
        // 问题分配通知模板
        createDefaultTemplate(
            Notification.NotificationType.ISSUE_ASSIGNED,
            NotificationTemplate.NotificationChannel.IN_APP,
            "问题分配通知",
            "您有新的问题需要处理",
            "问题：${issueTitle}，严重级别：${severity}，请及时处理。"
        );
        
        // 整改提交通知模板
        createDefaultTemplate(
            Notification.NotificationType.FIX_SUBMITTED,
            NotificationTemplate.NotificationChannel.IN_APP,
            "整改提交通知",
            "问题整改已提交",
            "问题\"${issueTitle}\"的整改已提交，请查看并验证。"
        );
        
        // 系统公告模板
        createDefaultTemplate(
            Notification.NotificationType.SYSTEM_ANNOUNCEMENT,
            NotificationTemplate.NotificationChannel.IN_APP,
            "系统公告通知",
            "系统公告",
            "${announcementContent}"
        );
    }
    
    private void createDefaultTemplate(Notification.NotificationType type,
                                     NotificationTemplate.NotificationChannel channel,
                                     String name, String titleTemplate, String contentTemplate) {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateName(name);
        template.setNotificationType(type);
        template.setChannel(channel);
        template.setTitleTemplate(titleTemplate);
        template.setContentTemplate(contentTemplate);
        template.setIsEnabled(true);
        template.setDescription("系统默认模板");
        
        // 设置模板变量说明
        Set<String> titleVars = templateService.extractVariables(titleTemplate);
        Set<String> contentVars = templateService.extractVariables(contentTemplate);
        titleVars.addAll(contentVars);
        
        if (!titleVars.isEmpty()) {
            template.setVariables(String.join(",", titleVars));
        }
        
        templateRepository.insert(template);
    }
    
    private NotificationTemplateDTO convertToDTO(NotificationTemplate entity) {
        NotificationTemplateDTO dto = new NotificationTemplateDTO();
        dto.setId(entity.getId());
        dto.setTemplateName(entity.getTemplateName());
        dto.setNotificationType(entity.getNotificationType());
        dto.setChannel(entity.getChannel());
        dto.setTitleTemplate(entity.getTitleTemplate());
        dto.setContentTemplate(entity.getContentTemplate());
        dto.setVariables(entity.getVariables());
        dto.setIsEnabled(entity.getIsEnabled());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    private NotificationTemplate convertToEntity(NotificationTemplateDTO dto) {
        NotificationTemplate entity = new NotificationTemplate();
        entity.setTemplateName(dto.getTemplateName());
        entity.setNotificationType(dto.getNotificationType());
        entity.setChannel(dto.getChannel());
        entity.setTitleTemplate(dto.getTitleTemplate());
        entity.setContentTemplate(dto.getContentTemplate());
        entity.setVariables(dto.getVariables());
        entity.setIsEnabled(dto.getIsEnabled());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}