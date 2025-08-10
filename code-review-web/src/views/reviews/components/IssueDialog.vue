<template>
  <el-dialog
    v-model="dialogVisible"
    title="添加问题"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent
    >
      <el-form-item label="问题类型" prop="issueType">
        <el-select v-model="form.issueType" placeholder="请选择问题类型">
          <el-option label="功能缺陷" value="FUNCTIONAL_DEFECT" />
          <el-option label="性能问题" value="PERFORMANCE_ISSUE" />
          <el-option label="安全漏洞" value="SECURITY_VULNERABILITY" />
          <el-option label="代码规范" value="CODE_STANDARD" />
          <el-option label="设计问题" value="DESIGN_ISSUE" />
        </el-select>
      </el-form-item>

      <el-form-item label="严重级别" prop="severity">
        <el-select v-model="form.severity" placeholder="请选择严重级别">
          <el-option label="严重" value="CRITICAL" />
          <el-option label="一般" value="MAJOR" />
          <el-option label="轻微" value="MINOR" />
          <el-option label="建议" value="SUGGESTION" />
        </el-select>
      </el-form-item>

      <el-form-item label="问题标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入问题标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="问题描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请详细描述发现的问题"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="改进建议" prop="suggestion">
        <el-input
          v-model="form.suggestion"
          type="textarea"
          :rows="3"
          placeholder="请提供改进建议（可选）"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="代码行号" prop="lineNumber">
            <el-input-number
              v-model="form.lineNumber"
              :min="1"
              placeholder="代码行号"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="参考链接" prop="referenceLinks">
            <el-input
              v-model="form.referenceLinks"
              placeholder="相关文档或参考链接"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="代码片段" prop="codeSnippet">
        <el-input
          v-model="form.codeSnippet"
          type="textarea"
          :rows="6"
          placeholder="请粘贴相关的代码片段（可选）"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave">添加问题</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { Issue } from '@/api/reviewRecord'

// Props
interface Props {
  visible: boolean
  issue: Issue | null
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  issue: null
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: [issue: Issue]
}>()

// 响应式数据
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<Issue>({
  reviewRecordId: 0,
  issueType: '',
  severity: '',
  title: '',
  description: '',
  suggestion: '',
  referenceLinks: '',
  lineNumber: undefined,
  codeSnippet: ''
})

// 表单验证规则
const rules = {
  issueType: [
    { required: true, message: '请选择问题类型', trigger: 'change' }
  ],
  severity: [
    { required: true, message: '请选择严重级别', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { max: 2000, message: '描述长度不能超过2000字符', trigger: 'blur' }
  ],
  suggestion: [
    { max: 1000, message: '建议长度不能超过1000字符', trigger: 'blur' }
  ],
  codeSnippet: [
    { max: 1000, message: '代码片段长度不能超过1000字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 监听器
watch(() => props.visible, (visible) => {
  if (visible) {
    initForm()
  }
})

watch(() => props.issue, (issue) => {
  if (issue && props.visible) {
    initForm()
  }
})

// 方法
const initForm = () => {
  if (props.issue) {
    Object.assign(form, props.issue)
  } else {
    // 重置表单
    Object.assign(form, {
      reviewRecordId: 0,
      issueType: '',
      severity: '',
      title: '',
      description: '',
      suggestion: '',
      referenceLinks: '',
      lineNumber: undefined,
      codeSnippet: ''
    })
  }
  
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleClose = () => {
  dialogVisible.value = false
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 创建问题对象
    const issue: Issue = {
      ...form,
      status: 'OPEN'
    }
    
    emit('success', issue)
    ElMessage.success('问题添加成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>