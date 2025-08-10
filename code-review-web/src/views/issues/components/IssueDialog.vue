<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="800px"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="问题标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入问题标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="问题类型" prop="issueType">
            <el-select v-model="form.issueType" placeholder="选择问题类型" style="width: 100%">
              <el-option label="功能缺陷" value="FUNCTIONAL_DEFECT" />
              <el-option label="性能问题" value="PERFORMANCE_ISSUE" />
              <el-option label="安全漏洞" value="SECURITY_VULNERABILITY" />
              <el-option label="代码规范" value="CODE_STANDARD" />
              <el-option label="设计问题" value="DESIGN_ISSUE" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="严重级别" prop="severity">
            <el-select v-model="form.severity" placeholder="选择严重级别" style="width: 100%">
              <el-option label="严重" value="CRITICAL" />
              <el-option label="重要" value="MAJOR" />
              <el-option label="一般" value="MINOR" />
              <el-option label="建议" value="SUGGESTION" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="分配给">
            <el-select v-model="form.assignedTo" placeholder="选择分配人" clearable style="width: 100%">
              <el-option
                v-for="user in teamMembers"
                :key="user.id"
                :label="user.username"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="代码行号">
            <el-input-number
              v-model="form.lineNumber"
              :min="1"
              placeholder="代码行号"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="问题描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述问题"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="改进建议">
            <el-input
              v-model="form.suggestion"
              type="textarea"
              :rows="3"
              placeholder="请提供改进建议"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="代码片段">
            <el-input
              v-model="form.codeSnippet"
              type="textarea"
              :rows="6"
              placeholder="请粘贴相关代码片段"
              maxlength="5000"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="参考链接">
            <el-input
              v-model="referenceLinksText"
              type="textarea"
              :rows="2"
              placeholder="请输入参考链接，每行一个"
              @blur="updateReferenceLinks"
            />
            <div class="form-tip">每行输入一个链接，支持多个参考链接</div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 智能分类建议 -->
      <div v-if="classificationSuggestion && mode === 'create'" class="classification-suggestion">
        <el-alert
          :title="`智能分类建议: ${getIssueTypeText(classificationSuggestion.suggestedType)} - ${getSeverityText(classificationSuggestion.suggestedSeverity)}`"
          type="info"
          :description="classificationSuggestion.reason"
          show-icon
          :closable="false"
        >
          <template #default>
            <div class="suggestion-actions">
              <el-button size="small" @click="applySuggestion">采用建议</el-button>
              <el-button size="small" @click="classificationSuggestion = null">忽略</el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 问题模板 -->
      <div v-if="mode === 'create'" class="template-section">
        <el-divider content-position="left">快速创建</el-divider>
        <div class="template-buttons">
          <el-button
            v-for="template in issueTemplates"
            :key="template.id"
            size="small"
            @click="applyTemplate(template)"
          >
            {{ template.name }}
          </el-button>
        </div>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button @click="getClassificationSuggestion" v-if="mode === 'create'">
          获取分类建议
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ mode === 'create' ? '创建' : '更新' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { issueApi } from '@/api/issue'
import { userApi } from '@/api/user'

// Props
const props = defineProps({
  modelValue: Boolean,
  issue: Object,
  mode: {
    type: String,
    default: 'create'
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref()
const submitting = ref(false)
const teamMembers = ref([])
const issueTemplates = ref([])
const classificationSuggestion = ref(null)
const referenceLinksText = ref('')

// 表单数据
const form = reactive({
  title: '',
  issueType: '',
  severity: '',
  description: '',
  suggestion: '',
  codeSnippet: '',
  lineNumber: null,
  assignedTo: null,
  referenceLinks: '',
  reviewRecordId: null
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度应在 5 到 200 个字符', trigger: 'blur' }
  ],
  issueType: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ],
  severity: [
    { required: true, message: '请选择严重级别', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度应在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return props.mode === 'create' ? '创建问题' : '编辑问题'
})

const currentTeamId = computed(() => {
  return 1 // 临时硬编码，实际应从用户状态获取
})

// 监听器
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    initForm()
    loadTeamMembers()
    if (props.mode === 'create') {
      loadIssueTemplates()
    }
  }
})

watch([() => form.title, () => form.description], () => {
  if (props.mode === 'create' && form.title && form.description) {
    // 自动获取分类建议
    debounceGetSuggestion()
  }
})

// 生命周期
onMounted(() => {
  if (props.modelValue) {
    initForm()
    loadTeamMembers()
  }
})

// 方法
const initForm = () => {
  if (props.issue && props.mode === 'edit') {
    Object.keys(form).forEach(key => {
      if (props.issue[key] !== undefined) {
        form[key] = props.issue[key]
      }
    })
    
    // 处理参考链接
    if (props.issue.referenceLinks) {
      try {
        const links = JSON.parse(props.issue.referenceLinks)
        referenceLinksText.value = Array.isArray(links) ? links.join('\n') : ''
      } catch (e) {
        referenceLinksText.value = props.issue.referenceLinks || ''
      }
    }
  } else {
    // 重置表单
    Object.keys(form).forEach(key => {
      form[key] = key === 'lineNumber' ? null : ''
    })
    referenceLinksText.value = ''
    classificationSuggestion.value = null
  }
}

const loadTeamMembers = async () => {
  try {
    const response = await userApi.getTeamMembers(currentTeamId.value)
    teamMembers.value = response.data
  } catch (error) {
    console.error('加载团队成员失败:', error)
  }
}

const loadIssueTemplates = async () => {
  try {
    const response = await issueApi.getIssueTemplates()
    issueTemplates.value = response.data.slice(0, 6) // 只显示前6个模板
  } catch (error) {
    console.error('加载问题模板失败:', error)
  }
}

const updateReferenceLinks = () => {
  const links = referenceLinksText.value
    .split('\n')
    .map(link => link.trim())
    .filter(link => link)
  
  form.referenceLinks = JSON.stringify(links)
}

// 防抖获取分类建议
let suggestionTimer = null
const debounceGetSuggestion = () => {
  if (suggestionTimer) {
    clearTimeout(suggestionTimer)
  }
  suggestionTimer = setTimeout(() => {
    getClassificationSuggestion()
  }, 1000)
}

const getClassificationSuggestion = async () => {
  if (!form.title || !form.description) {
    ElMessage.warning('请先输入标题和描述')
    return
  }
  
  try {
    const response = await issueApi.getClassificationSuggestion(form.title, form.description)
    classificationSuggestion.value = response.data
  } catch (error) {
    console.error('获取分类建议失败:', error)
  }
}

const applySuggestion = () => {
  if (classificationSuggestion.value) {
    form.issueType = classificationSuggestion.value.suggestedType
    form.severity = classificationSuggestion.value.suggestedSeverity
    classificationSuggestion.value = null
    ElMessage.success('已应用分类建议')
  }
}

const applyTemplate = async (template) => {
  try {
    // 这里可以弹出参数输入对话框，简化处理直接应用模板
    const parameters = {
      methodName: 'exampleMethod',
      variableName: 'exampleVariable',
      lineNumber: '100'
    }
    
    const response = await issueApi.createIssueFromTemplate(template.id, null, parameters)
    const templateIssue = response.data
    
    // 应用模板内容到表单
    form.title = templateIssue.title
    form.issueType = templateIssue.issueType
    form.severity = templateIssue.severity
    form.description = templateIssue.description
    form.suggestion = templateIssue.suggestion
    form.referenceLinks = templateIssue.referenceLinks
    
    ElMessage.success('已应用问题模板')
  } catch (error) {
    ElMessage.error('应用模板失败: ' + error.message)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 更新参考链接
    updateReferenceLinks()
    
    submitting.value = true
    
    if (props.mode === 'create') {
      await issueApi.createIssue(form)
      ElMessage.success('问题创建成功')
    } else {
      await issueApi.updateIssue(form.id, form)
      ElMessage.success('问题更新成功')
    }
    
    emit('success')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
  classificationSuggestion.value = null
}

// 辅助方法
const getIssueTypeText = (type) => {
  const typeMap = {
    FUNCTIONAL_DEFECT: '功能缺陷',
    PERFORMANCE_ISSUE: '性能问题',
    SECURITY_VULNERABILITY: '安全漏洞',
    CODE_STANDARD: '代码规范',
    DESIGN_ISSUE: '设计问题'
  }
  return typeMap[type] || type
}

const getSeverityText = (severity) => {
  const severityMap = {
    CRITICAL: '严重',
    MAJOR: '重要',
    MINOR: '一般',
    SUGGESTION: '建议'
  }
  return severityMap[severity] || severity
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.classification-suggestion {
  margin: 20px 0;
}

.suggestion-actions {
  margin-top: 10px;
}

.template-section {
  margin-top: 20px;
}

.template-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-alert__content) {
  flex: 1;
}

:deep(.el-divider__text) {
  font-weight: 500;
}
</style>