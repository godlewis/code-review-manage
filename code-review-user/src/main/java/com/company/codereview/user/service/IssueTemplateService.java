package com.company.codereview.user.service;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 问题模板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueTemplateService {
    
    /**
     * 获取问题模板列表
     */
    public List<IssueTemplate> getIssueTemplates() {
        List<IssueTemplate> templates = new ArrayList<>();
        
        // 功能缺陷模板
        templates.addAll(getFunctionalDefectTemplates());
        
        // 性能问题模板
        templates.addAll(getPerformanceIssueTemplates());
        
        // 安全漏洞模板
        templates.addAll(getSecurityVulnerabilityTemplates());
        
        // 代码规范模板
        templates.addAll(getCodeStandardTemplates());
        
        // 设计问题模板
        templates.addAll(getDesignIssueTemplates());
        
        return templates;
    }
    
    /**
     * 根据类型获取问题模板
     */
    public List<IssueTemplate> getIssueTemplatesByType(IssueType issueType) {
        return getIssueTemplates().stream()
                .filter(template -> template.getIssueType() == issueType)
                .toList();
    }
    
    /**
     * 根据ID获取问题模板
     */
    public IssueTemplate getIssueTemplateById(String templateId) {
        return getIssueTemplates().stream()
                .filter(template -> template.getId().equals(templateId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据模板创建问题
     */
    public Issue createIssueFromTemplate(String templateId, Long reviewRecordId, Map<String, String> parameters) {
        IssueTemplate template = getIssueTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("问题模板不存在: " + templateId);
        }
        
        Issue issue = new Issue();
        issue.setReviewRecordId(reviewRecordId);
        issue.setIssueType(template.getIssueType());
        issue.setSeverity(template.getSeverity());
        issue.setTitle(replacePlaceholders(template.getTitle(), parameters));
        issue.setDescription(replacePlaceholders(template.getDescription(), parameters));
        issue.setSuggestion(replacePlaceholders(template.getSuggestion(), parameters));
        issue.setReferenceLinks(template.getReferenceLinks());
        issue.setStatus(Issue.IssueStatus.OPEN);
        
        return issue;
    }
    
    /**
     * 替换模板中的占位符
     */
    private String replacePlaceholders(String template, Map<String, String> parameters) {
        if (template == null || parameters == null) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        
        return result;
    }
    
    /**
     * 获取功能缺陷模板
     */
    private List<IssueTemplate> getFunctionalDefectTemplates() {
        List<IssueTemplate> templates = new ArrayList<>();
        
        templates.add(new IssueTemplate(
                "FUNC_001",
                "空指针异常",
                IssueType.FUNCTIONAL_DEFECT,
                Severity.MAJOR,
                "${methodName}方法存在空指针异常风险",
                "在${methodName}方法中，${variableName}变量可能为null，但代码中没有进行null检查，" +
                "这可能导致NullPointerException异常。\\n\\n" +
                "问题位置：第${lineNumber}行\\n" +
                "风险代码：${codeSnippet}",
                "建议在使用${variableName}变量前进行null检查：\\n" +
                "```java\\n" +
                "if (${variableName} != null) {\\n" +
                "    // 使用变量\\n" +
                "}\\n" +
                "```\\n" +
                "或者使用Optional类来避免空指针异常。",
                "https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html"
        ));
        
        templates.add(new IssueTemplate(
                "FUNC_002",
                "资源未正确关闭",
                IssueType.FUNCTIONAL_DEFECT,
                Severity.MAJOR,
                "${resourceType}资源未正确关闭",
                "在${methodName}方法中，${resourceType}资源（${resourceName}）打开后没有在finally块中关闭，" +
                "这可能导致资源泄露。\\n\\n" +
                "问题位置：第${lineNumber}行",
                "建议使用try-with-resources语句自动管理资源：\\n" +
                "```java\\n" +
                "try (${resourceType} ${resourceName} = ...) {\\n" +
                "    // 使用资源\\n" +
                "} catch (Exception e) {\\n" +
                "    // 异常处理\\n" +
                "}\\n" +
                "```",
                "https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"
        ));
        
        templates.add(new IssueTemplate(
                "FUNC_003",
                "数组越界风险",
                IssueType.FUNCTIONAL_DEFECT,
                Severity.MAJOR,
                "${arrayName}数组存在越界访问风险",
                "在${methodName}方法中，访问数组${arrayName}时没有检查索引${indexName}是否在有效范围内，" +
                "这可能导致ArrayIndexOutOfBoundsException异常。\\n\\n" +
                "问题位置：第${lineNumber}行",
                "建议在访问数组前检查索引范围：\\n" +
                "```java\\n" +
                "if (${indexName} >= 0 && ${indexName} < ${arrayName}.length) {\\n" +
                "    // 安全访问数组\\n" +
                "    ${arrayName}[${indexName}]\\n" +
                "}\\n" +
                "```",
                ""
        ));
        
        return templates;
    }
    
    /**
     * 获取性能问题模板
     */
    private List<IssueTemplate> getPerformanceIssueTemplates() {
        List<IssueTemplate> templates = new ArrayList<>();
        
        templates.add(new IssueTemplate(
                "PERF_001",
                "循环中的数据库查询",
                IssueType.PERFORMANCE_ISSUE,
                Severity.CRITICAL,
                "${methodName}方法中存在循环数据库查询",
                "在${methodName}方法的循环中执行数据库查询操作，这会导致N+1查询问题，" +
                "严重影响性能。\\n\\n" +
                "问题位置：第${lineNumber}行\\n" +
                "循环次数可能达到：${loopCount}",
                "建议使用批量查询或JOIN查询来优化：\\n" +
                "1. 将循环中的查询改为批量查询\\n" +
                "2. 使用IN查询一次性获取所有数据\\n" +
                "3. 考虑使用缓存减少数据库访问",
                "https://vladmihalcea.com/n-plus-1-query-problem/"
        ));
        
        templates.add(new IssueTemplate(
                "PERF_002",
                "字符串拼接性能问题",
                IssueType.PERFORMANCE_ISSUE,
                Severity.MINOR,
                "${methodName}方法中字符串拼接效率低下",
                "在${methodName}方法中使用+操作符在循环中拼接字符串，" +
                "这会创建大量临时String对象，影响性能。\\n\\n" +
                "问题位置：第${lineNumber}行",
                "建议使用StringBuilder进行字符串拼接：\\n" +
                "```java\\n" +
                "StringBuilder sb = new StringBuilder();\\n" +
                "for (...) {\\n" +
                "    sb.append(...);\\n" +
                "}\\n" +
                "String result = sb.toString();\\n" +
                "```",
                ""
        ));
        
        return templates;
    }
    
    /**
     * 获取安全漏洞模板
     */
    private List<IssueTemplate> getSecurityVulnerabilityTemplates() {
        List<IssueTemplate> templates = new ArrayList<>();
        
        templates.add(new IssueTemplate(
                "SEC_001",
                "SQL注入风险",
                IssueType.SECURITY_VULNERABILITY,
                Severity.CRITICAL,
                "${methodName}方法存在SQL注入风险",
                "在${methodName}方法中，直接将用户输入拼接到SQL语句中，" +
                "这存在SQL注入攻击的风险。\\n\\n" +
                "问题位置：第${lineNumber}行\\n" +
                "风险参数：${parameterName}",
                "建议使用预编译语句（PreparedStatement）：\\n" +
                "```java\\n" +
                "String sql = \\\"SELECT * FROM users WHERE id = ?\\\";\\n" +
                "PreparedStatement pstmt = conn.prepareStatement(sql);\\n" +
                "pstmt.setLong(1, userId);\\n" +
                "```\\n" +
                "或使用ORM框架的参数化查询。",
                "https://owasp.org/www-community/attacks/SQL_Injection"
        ));
        
        templates.add(new IssueTemplate(
                "SEC_002",
                "密码明文存储",
                IssueType.SECURITY_VULNERABILITY,
                Severity.CRITICAL,
                "${fieldName}字段存储明文密码",
                "在${className}类中，密码字段${fieldName}以明文形式存储，" +
                "这存在严重的安全风险。",
                "建议对密码进行加密存储：\\n" +
                "1. 使用BCrypt、PBKDF2或Argon2等安全的哈希算法\\n" +
                "2. 添加盐值增强安全性\\n" +
                "3. 永远不要存储明文密码",
                "https://owasp.org/www-project-cheat-sheets/cheatsheets/Password_Storage_Cheat_Sheet.html"
        ));
        
        return templates;
    }
    
    /**
     * 获取代码规范模板
     */
    private List<IssueTemplate> getCodeStandardTemplates() {
        List<IssueTemplate> templates = new ArrayList<>();
        
        templates.add(new IssueTemplate(
                "STD_001",
                "命名规范问题",
                IssueType.CODE_STANDARD,
                Severity.MINOR,
                "${elementType}命名不符合规范",
                "${elementType} ${elementName} 的命名不符合Java命名规范。\\n\\n" +
                "问题位置：第${lineNumber}行\\n" +
                "当前命名：${currentName}\\n" +
                "建议命名：${suggestedName}",
                "请按照Java命名规范修改：\\n" +
                "- 类名：使用大驼峰命名法（PascalCase）\\n" +
                "- 方法名和变量名：使用小驼峰命名法（camelCase）\\n" +
                "- 常量：使用全大写字母，单词间用下划线分隔\\n" +
                "- 包名：使用全小写字母",
                "https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html"
        ));
        
        templates.add(new IssueTemplate(
                "STD_002",
                "缺少注释",
                IssueType.CODE_STANDARD,
                Severity.SUGGESTION,
                "${elementType}缺少必要的注释",
                "${elementType} ${elementName} 缺少必要的注释说明，" +
                "影响代码的可读性和可维护性。\\n\\n" +
                "问题位置：第${lineNumber}行",
                "建议添加适当的注释：\\n" +
                "- 类注释：说明类的用途和功能\\n" +
                "- 方法注释：使用JavaDoc格式说明方法功能、参数和返回值\\n" +
                "- 复杂逻辑注释：对复杂的业务逻辑进行说明",
                "https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html"
        ));
        
        return templates;
    }
    
    /**
     * 获取设计问题模板
     */
    private List<IssueTemplate> getDesignIssueTemplates() {
        List<IssueTemplate> templates = new ArrayList<>();
        
        templates.add(new IssueTemplate(
                "DES_001",
                "违反单一职责原则",
                IssueType.DESIGN_ISSUE,
                Severity.MAJOR,
                "${className}类违反单一职责原则",
                "${className}类承担了多个职责，违反了单一职责原则（SRP），" +
                "这会导致类的复杂度增加，难以维护和测试。\\n\\n" +
                "发现的职责：\\n${responsibilities}",
                "建议将类拆分为多个职责单一的类：\\n" +
                "1. 识别类中的不同职责\\n" +
                "2. 为每个职责创建独立的类\\n" +
                "3. 通过组合或依赖注入的方式协调各个类",
                "https://en.wikipedia.org/wiki/Single-responsibility_principle"
        ));
        
        templates.add(new IssueTemplate(
                "DES_002",
                "过长的方法",
                IssueType.DESIGN_ISSUE,
                Severity.MINOR,
                "${methodName}方法过长",
                "${methodName}方法有${lineCount}行代码，超过了建议的方法长度限制，" +
                "这会影响代码的可读性和可维护性。",
                "建议将长方法拆分为多个小方法：\\n" +
                "1. 识别方法中的逻辑块\\n" +
                "2. 将每个逻辑块提取为独立的方法\\n" +
                "3. 使用有意义的方法名描述功能",
                ""
        ));
        
        return templates;
    }
    
    /**
     * 问题模板类
     */
    public static class IssueTemplate {
        private String id;
        private String name;
        private IssueType issueType;
        private Severity severity;
        private String title;
        private String description;
        private String suggestion;
        private String referenceLinks;
        
        public IssueTemplate(String id, String name, IssueType issueType, Severity severity,
                           String title, String description, String suggestion, String referenceLinks) {
            this.id = id;
            this.name = name;
            this.issueType = issueType;
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.suggestion = suggestion;
            this.referenceLinks = referenceLinks;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public IssueType getIssueType() { return issueType; }
        public void setIssueType(IssueType issueType) { this.issueType = issueType; }
        
        public Severity getSeverity() { return severity; }
        public void setSeverity(Severity severity) { this.severity = severity; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
        
        public String getReferenceLinks() { return referenceLinks; }
        public void setReferenceLinks(String referenceLinks) { this.referenceLinks = referenceLinks; }
    }
}