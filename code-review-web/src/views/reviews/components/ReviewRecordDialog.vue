<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.prevent
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="评审标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入评审标题"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="代码仓库" prop="codeRepository">
            <el-input
              v-model="form.codeRepository"
              placeholder="请输入代码仓库地址"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="文件路径" prop="codeFilePath">
            <el-input
              v-model="form.codeFilePath"
              placeholder="请输入代码文件路径"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="评审描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入评审描述"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="总体评分" prop="overallScore">
            <el-rate
              v-model="form.overallScore"
              :max="10"
              show-score
              text-color="#ff9900"
              score-template="{value}分"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择状态">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="评审总结" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="4"
              placeholder="请输入评审总结"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 代码截图上传 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="代码截图">
            <div class="screenshot-upload-section">
              <el-upload
                ref="uploadRef"
                :file-list="screenshotList"
                :auto-upload="false"
                :on-change="handleScreenshotChange"
                :on-remove="handleScreenshotRemove"
                :before-upload="beforeScreenshotUpload"
                multiple
                accept="image/*"
                list-type="picture-card"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 jpg/png/gif 格式，单个文件不超过 10MB
                  </div>
                </template>
              </el-upload>
            </div>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 问题列表 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="发现的问题">
            <div class="issues-section">
              <div class="issues-header">
                <span>问题列表 ({{ form.issues.length }})</span>
                <el-button type="primary" size="small" @click="handleAddIssue">
                  添加问题
                </el-button>
              </div>
              
              <div v-if="form.issues.length === 0" class="no-issues">
                <el-empty description="暂无问题" :image-size="80" />
              </div>
              
              <div v-else class="issues-list">
                <div
                  v-for="(issue, index) in form.issues"
                  :key="index"
                  class="issue-item"
                >
                  <div class="issue-header">
                    <div class="issue-title">
                      <el-tag :type="getSeverityType(issue.severity)" size="small">
                        {{ getSeverityText(issue.severity) }}
                      </el-tag>
                      <el-tag type="info" size="small">
                        {{ getIssueTypeText(issue.issueType) }}
                      </el-tag>
                      <span class="title-text">{{ issue.title }}</span>
                    </div>
                    <el-button
                      type="danger"
                      size="small"
                      text
                      @click="handleRemoveIssue(index)"
                    >
                      删除
                    </el-button>
                  </div>
                  <div class="issue-content">
                    <p><strong>描述：</strong>{{ issue.description }}</p>
                    <p v-if="issue.suggestion"><strong>建议：</strong>{{ issue.suggestion }}</p>
                    <p v-if="issue.lineNumber"><strong>行号：</strong>{{ issue.lineNumber }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ mode === 'create' ? '创建' : '更新' }}
        </el-button>
        <el-button
          v-if="mode === 'edit' && form.status === 'DRAFT'"
          type="success"
          @click="handleSubmit"
          :loading="submitting"
        >
          提交评审
        </el-button>
      </div>
    </template>

    <!-- 添加问题对话框 -->
    <IssueDialog
      v-model:visible="issueDialogVisible"
      :issue="currentIssue"
      @success="handleIssueSuccess"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, UploadFile, UploadFiles } from 'element-plus'
import { reviewRecordApi, screenshotApi, type ReviewRecord, type Issue } from '@/api/reviewRecord'
import IssueDialog from './IssueDialog.vue'

// Props
interface Props {
  visible: boolean
  record: ReviewRecord | null
  mode: 'create' | 'edit'
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  record: null,
  mode: 'create'
})

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

// 响应式数据
const formRef = ref<FormInstance>()
const uploadRef = ref()
const saving = ref(false)
const submitting = ref(false)
const screenshotList = ref<UploadFile[]>([])
const issueDialogVisible = ref(false)
const currentIssue = ref<Issue | null>(null)

// 表单数据
const form = reactive<ReviewRecord>({
  assignmentId: 0,
  title: '',
  codeRepository: '',
  codeFilePath: '',
  description: '',
  overallScore: 0,
  summary: '',
  status: 'DRAFT',
  issues: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入评审标题', trigger: 'blur' },
    { max: 200, message: '标题长度不能超过200字符', trigger: 'blur' }
  ],
  codeRepository: [
    { max: 500, message: '代码仓库地址长度不能超过500字符', trigger: 'blur' }
  ],
  codeFilePath: [
    { max: 1000, message: '代码文件路径长度不能超过1000字符', trigger: 'blur' }
  ],
  description: [
    { max: 2000, message: '评审描述长度不能超过2000字符', trigger: 'blur' }
  ],
  summary: [
    { max: 1000, message: '评审总结长度不能超过1000字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

const dialogTitle = computed(() => {
  return props.mode === 'create' ? '创建评审记录' : '编辑评审记录'
})

// 监听器
watch(() => props.visible, (visible) => {
  if (visible) {
    initForm()
  }
})

watch(() => props.record, (record) => {
  if (record && props.visible) {
    initForm()
  }
})

// 方法
const initForm = () => {
  if (props.record) {
    Object.assign(form, {
      ...props.record,
      issues: props.record.issues || []
    })
    
    // 初始化截图列表
    if (props.record.screenshots) {
      screenshotList.value = props.record.screenshots.map(screenshot => ({
        name: screenshot.fileName,
        url: screenshot.fileUrl,
        uid: screenshot.id!,
        status: 'success'
      }))
    }
  } else {
    // 重置表单
    Object.assign(form, {
      assignmentId: 0,
      title: '',
      codeRepository: '',
      codeFilePath: '',
      description: '',
      overallScore: 0,
      summary: '',
      status: 'DRAFT',
      issues: []
    })
    screenshotList.value = []
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
    saving.value = true
    
    if (props.mode === 'create') {
      // await reviewRecordApi.create(form)
      ElMessage.success('创建成功')
    } else {
      // await reviewRecordApi.update(form.id!, form)
      ElMessage.success('更新成功')
    }
    
    emit('success')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    await ElMessageBox.confirm('确定要提交这个评审记录吗？提交后将无法修改。', '确认提交', {
      type: 'warning'
    })
    
    submitting.value = true
    
    // 先保存，再提交
    if (props.mode === 'edit') {
      // await reviewRecordApi.update(form.id!, form)
      // await reviewRecordApi.submit(form.id!)
    }
    
    ElMessage.success('提交成功')
    emit('success')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleScreenshotChange = (file: UploadFile, fileList: UploadFiles) => {
  screenshotList.value = fileList
}

const handleScreenshotRemove = (file: UploadFile, fileList: UploadFiles) => {
  screenshotList.value = fileList
}

const beforeScreenshotUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const handleAddIssue = () => {
  currentIssue.value = null
  issueDialogVisible.value = true
}

const handleRemoveIssue = (index: number) => {
  form.issues.splice(index, 1)
}

const handleIssueSuccess = (issue: Issue) => {
  form.issues.push(issue)
  issueDialogVisible.value = false
}

// 工具方法
const getSeverityType = (severity: string) => {
  const severityMap: Record<string, string> = {
    CRITICAL: 'danger',
    MAJOR: 'warning',
    MINOR: 'info',
    SUGGESTION: 'success'
  }
  return severityMap[severity] || 'info'
}

const getSeverityText = (severity: string) => {
  const severityMap: Record<string, string> = {
    CRITICAL: '严重',
    MAJOR: '一般',
    MINOR: '轻微',
    SUGGESTION: '建议'
  }
  return severityMap[severity] || severity
}

const getIssueTypeText = (issueType: string) => {
  const typeMap: Record<string, string> = {
    FUNCTIONAL_DEFECT: '功能缺陷',
    PERFORMANCE_ISSUE: '性能问题',
    SECURITY_VULNERABILITY: '安全漏洞',
    CODE_STANDARD: '代码规范',
    DESIGN_ISSUE: '设计问题'
  }
  return typeMap[issueType] || issueType
}
</script>

<style scoped>
.screenshot-upload-section {
  width: 100%;
}

.issues-section {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
}

.issues-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 500;
}

.no-issues {
  text-align: center;
  padding: 20px;
}

.issues-list {
  space-y: 12px;
}

.issue-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 12px;
}

.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.issue-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  font-weight: 500;
  color: #303133;
}

.issue-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.issue-content p {
  margin: 4px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>