<template>
  <el-dialog
    :model-value="modelValue"
    title="问题详情"
    width="1000px"
    @update:model-value="$emit('update:modelValue', $event)"
    @open="loadIssueDetail"
  >
    <div v-loading="loading" class="issue-detail">
      <div v-if="issue" class="detail-content">
        <!-- 基本信息 -->
        <div class="info-section">
          <h3>基本信息</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>问题ID:</label>
                <span>{{ issue.id }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>创建时间:</label>
                <span>{{ formatDateTime(issue.createdAt) }}</span>
              </div>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>问题类型:</label>
                <el-tag :type="getIssueTypeTagType(issue.issueType)">
                  {{ getIssueTypeText(issue.issueType) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>严重级别:</label>
                <el-tag :type="getSeverityTagType(issue.severity)">
                  {{ getSeverityText(issue.severity) }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>当前状态:</label>
                <el-tag :type="getStatusTagType(issue.status)">
                  {{ getStatusText(issue.status) }}
                </el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>分配给:</label>
                <span>{{ getUserName(issue.assignedTo) }}</span>
              </div>
            </el-col>
          </el-row>
          
          <div class="info-item" v-if="issue.lineNumber">
            <label>代码行号:</label>
            <span>{{ issue.lineNumber }}</span>
          </div>
        </div>

        <!-- 问题内容 -->
        <div class="content-section">
          <h3>{{ issue.title }}</h3>
          
          <div class="description">
            <h4>问题描述</h4>
            <div class="text-content">{{ issue.description }}</div>
          </div>
          
          <div v-if="issue.suggestion" class="suggestion">
            <h4>改进建议</h4>
            <div class="text-content">{{ issue.suggestion }}</div>
          </div>
          
          <div v-if="issue.codeSnippet" class="code-snippet">
            <h4>代码片段</h4>
            <pre class="code-block">{{ issue.codeSnippet }}</pre>
          </div>
          
          <div v-if="referenceLinks.length > 0" class="reference-links">
            <h4>参考链接</h4>
            <ul>
              <li v-for="(link, index) in referenceLinks" :key="index">
                <el-link :href="link" target="_blank" type="primary">{{ link }}</el-link>
              </li>
            </ul>
          </div>
        </div>

        <!-- 整改记录 -->
        <div class="fix-records-section">
          <div class="section-header">
            <h3>整改记录</h3>
            <el-button 
              v-if="canSubmitFix" 
              type="primary" 
              size="small" 
              @click="showFixDialog = true"
            >
              提交整改
            </el-button>
          </div>
          
          <div v-if="fixRecords.length === 0" class="no-records">
            暂无整改记录
          </div>
          
          <div v-else class="fix-records-list">
            <div 
              v-for="record in fixRecords" 
              :key="record.id" 
              class="fix-record-item"
            >
              <div class="record-header">
                <div class="record-info">
                  <span class="record-id">#{{ record.id }}</span>
                  <el-tag :type="getFixStatusTagType(record.status)">
                    {{ getFixStatusText(record.status) }}
                  </el-tag>
                  <span class="record-time">{{ formatDateTime(record.createdAt) }}</span>
                </div>
                <div class="record-actions">
                  <el-button 
                    v-if="canVerifyFix(record)" 
                    size="small" 
                    @click="showVerifyDialog(record)"
                  >
                    验证
                  </el-button>
                </div>
              </div>
              
              <div class="record-content">
                <div class="fix-description">
                  <strong>整改描述:</strong>
                  <p>{{ record.fixDescription }}</p>
                </div>
                
                <div v-if="record.beforeCodeUrl || record.afterCodeUrl" class="code-links">
                  <div v-if="record.beforeCodeUrl">
                    <strong>整改前代码:</strong>
                    <el-link :href="record.beforeCodeUrl" target="_blank" type="primary">
                      查看代码
                    </el-link>
                  </div>
                  <div v-if="record.afterCodeUrl">
                    <strong>整改后代码:</strong>
                    <el-link :href="record.afterCodeUrl" target="_blank" type="primary">
                      查看代码
                    </el-link>
                  </div>
                </div>
                
                <div v-if="record.verificationResult" class="verification-result">
                  <strong>验证结果:</strong>
                  <el-tag :type="getVerificationResultTagType(record.verificationResult)">
                    {{ getVerificationResultText(record.verificationResult) }}
                  </el-tag>
                  <div v-if="record.verificationRemarks" class="verification-remarks">
                    <strong>验证备注:</strong>
                    <p>{{ record.verificationRemarks }}</p>
                  </div>
                  <div class="verification-time">
                    验证时间: {{ formatDateTime(record.verifiedAt) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作历史 -->
        <div class="history-section">
          <h3>操作历史</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(history, index) in operationHistory"
              :key="index"
              :timestamp="formatDateTime(history.timestamp)"
              :type="history.type"
            >
              {{ history.description }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>

    <!-- 提交整改对话框 -->
    <FixRecordDialog
      v-model="showFixDialog"
      :issue-id="issueId"
      @success="handleFixSuccess"
    />

    <!-- 验证整改对话框 -->
    <VerifyFixDialog
      v-model="showVerifyFixDialog"
      :fix-record="currentFixRecord"
      @success="handleVerifySuccess"
    />

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
        <el-button type="primary" @click="editIssue" v-if="canEdit">编辑问题</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { issueApi } from '@/api/issue'
import { useUserStore } from '@/stores/user'
import FixRecordDialog from './FixRecordDialog.vue'
import VerifyFixDialog from './VerifyFixDialog.vue'

// Props
const props = defineProps({
  modelValue: Boolean,
  issueId: [String, Number]
})

// Emits
const emit = defineEmits(['update:modelValue', 'edit'])

// Store
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const issue = ref(null)
const fixRecords = ref([])
const showFixDialog = ref(false)
const showVerifyFixDialog = ref(false)
const currentFixRecord = ref(null)

// 计算属性
const referenceLinks = computed(() => {
  if (!issue.value?.referenceLinks) return []
  try {
    const links = JSON.parse(issue.value.referenceLinks)
    return Array.isArray(links) ? links : []
  } catch (e) {
    return issue.value.referenceLinks ? [issue.value.referenceLinks] : []
  }
})

const canEdit = computed(() => {
  if (!issue.value) return false
  const user = userStore.user
  return user && (
    user.role === 'TEAM_LEADER' || 
    user.role === 'ARCHITECT' ||
    issue.value.createdBy === user.id
  )
})

const canSubmitFix = computed(() => {
  if (!issue.value) return false
  const user = userStore.user
  return user && (
    issue.value.assignedTo === user.id ||
    user.role === 'TEAM_LEADER' ||
    user.role === 'ARCHITECT'
  ) && (
    issue.value.status === 'OPEN' || 
    issue.value.status === 'IN_PROGRESS'
  )
})

const operationHistory = computed(() => {
  const history = []
  
  if (issue.value) {
    history.push({
      timestamp: issue.value.createdAt,
      type: 'primary',
      description: `问题创建 - ${getUserName(issue.value.createdBy)}`
    })
    
    if (issue.value.assignedTo) {
      history.push({
        timestamp: issue.value.updatedAt,
        type: 'success',
        description: `分配给 ${getUserName(issue.value.assignedTo)}`
      })
    }
  }
  
  fixRecords.value.forEach(record => {
    history.push({
      timestamp: record.createdAt,
      type: 'warning',
      description: `${getUserName(record.fixerId)} 提交了整改记录`
    })
    
    if (record.verifiedAt) {
      history.push({
        timestamp: record.verifiedAt,
        type: record.verificationResult === 'PASS' ? 'success' : 'danger',
        description: `${getUserName(record.verifierId)} 验证了整改记录 - ${getVerificationResultText(record.verificationResult)}`
      })
    }
  })
  
  return history.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
})

// 监听器
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.issueId) {
    loadIssueDetail()
  }
})

// 方法
const loadIssueDetail = async () => {
  if (!props.issueId) return
  
  loading.value = true
  try {
    const response = await issueApi.getIssueWithFixRecords(props.issueId)
    issue.value = response.data
    fixRecords.value = response.data.fixRecords || []
  } catch (error) {
    ElMessage.error('加载问题详情失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const canVerifyFix = (record) => {
  const user = userStore.user
  return user && (
    record.status === 'SUBMITTED' || 
    record.status === 'UNDER_REVIEW'
  ) && (
    user.role === 'TEAM_LEADER' ||
    user.role === 'ARCHITECT' ||
    issue.value.createdBy === user.id
  )
}

const showVerifyDialog = (record) => {
  currentFixRecord.value = record
  showVerifyFixDialog.value = true
}

const editIssue = () => {
  emit('edit', issue.value)
}

const handleFixSuccess = () => {
  showFixDialog.value = false
  loadIssueDetail()
  ElMessage.success('整改记录提交成功')
}

const handleVerifySuccess = () => {
  showVerifyFixDialog.value = false
  currentFixRecord.value = null
  loadIssueDetail()
  ElMessage.success('整改验证完成')
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

const getIssueTypeTagType = (type) => {
  const typeMap = {
    FUNCTIONAL_DEFECT: 'danger',
    PERFORMANCE_ISSUE: 'warning',
    SECURITY_VULNERABILITY: 'danger',
    CODE_STANDARD: 'info',
    DESIGN_ISSUE: 'primary'
  }
  return typeMap[type] || 'info'
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

const getSeverityTagType = (severity) => {
  const severityMap = {
    CRITICAL: 'danger',
    MAJOR: 'warning',
    MINOR: 'info',
    SUGGESTION: 'success'
  }
  return severityMap[severity] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    OPEN: '待处理',
    IN_PROGRESS: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
    REJECTED: '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const statusMap = {
    OPEN: 'info',
    IN_PROGRESS: 'warning',
    RESOLVED: 'success',
    CLOSED: 'info',
    REJECTED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getFixStatusText = (status) => {
  const statusMap = {
    SUBMITTED: '已提交',
    UNDER_REVIEW: '审核中',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    NEED_REVISION: '需要修改'
  }
  return statusMap[status] || status
}

const getFixStatusTagType = (status) => {
  const statusMap = {
    SUBMITTED: 'info',
    UNDER_REVIEW: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    NEED_REVISION: 'warning'
  }
  return statusMap[status] || 'info'
}

const getVerificationResultText = (result) => {
  const resultMap = {
    PASS: '通过',
    FAIL: '不通过',
    NEED_FURTHER_FIX: '需要进一步修改'
  }
  return resultMap[result] || result
}

const getVerificationResultTagType = (result) => {
  const resultMap = {
    PASS: 'success',
    FAIL: 'danger',
    NEED_FURTHER_FIX: 'warning'
  }
  return resultMap[result] || 'info'
}

const getUserName = (userId) => {
  if (!userId) return '-'
  // 这里应该从用户缓存或API获取用户名
  return `用户${userId}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.issue-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-content {
  padding: 0 10px;
}

.info-section,
.content-section,
.fix-records-section,
.history-section {
  margin-bottom: 30px;
}

.info-section h3,
.content-section h3,
.fix-records-section h3,
.history-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
  min-width: 80px;
}

.content-section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
}

.description,
.suggestion,
.code-snippet,
.reference-links {
  margin-bottom: 20px;
}

.description h4,
.suggestion h4,
.code-snippet h4,
.reference-links h4 {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin: 0 0 8px 0;
}

.text-content {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
}

.code-block {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  overflow-x: auto;
  color: #303133;
}

.reference-links ul {
  margin: 0;
  padding-left: 20px;
}

.reference-links li {
  margin-bottom: 5px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
}

.no-records {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  background: #fafafa;
  border-radius: 4px;
}

.fix-records-list {
  space-y: 15px;
}

.fix-record-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fafafa;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.record-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-id {
  font-weight: 600;
  color: #606266;
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.record-content {
  margin-top: 15px;
}

.fix-description,
.code-links,
.verification-result {
  margin-bottom: 15px;
}

.fix-description p,
.verification-remarks p {
  margin: 5px 0 0 0;
  color: #303133;
  line-height: 1.5;
}

.code-links > div {
  margin-bottom: 8px;
}

.verification-result {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  padding: 10px;
}

.verification-time {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-timeline-item__timestamp) {
  font-size: 12px;
}
</style>