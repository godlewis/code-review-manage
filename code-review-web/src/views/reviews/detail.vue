<template>
  <div class="review-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="handleBack" :icon="ArrowLeft">返回</el-button>
        <div class="title-section">
          <h2>{{ reviewRecord?.title || '评审记录详情' }}</h2>
          <div class="meta-info">
            <el-tag :type="getStatusType(reviewRecord?.status)">
              {{ getStatusText(reviewRecord?.status) }}
            </el-tag>
            <span class="meta-item">
              评审者：{{ reviewRecord?.reviewerName }}
            </span>
            <span class="meta-item">
              被评审者：{{ reviewRecord?.revieweeName }}
            </span>
            <span class="meta-item">
              创建时间：{{ formatDateTime(reviewRecord?.createdAt) }}
            </span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button
          v-if="canEdit"
          type="primary"
          @click="handleEdit"
          :icon="Edit"
        >
          编辑
        </el-button>
        <el-button
          v-if="canSubmit"
          type="success"
          @click="handleSubmit"
          :icon="Check"
        >
          提交评审
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button :icon="More">更多</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="copy" :icon="CopyDocument">
                复制评审
              </el-dropdown-item>
              <el-dropdown-item
                v-if="canMarkReReview"
                command="reReview"
                :icon="RefreshLeft"
              >
                标记重新评审
              </el-dropdown-item>
              <el-dropdown-item
                v-if="canDelete"
                command="delete"
                :icon="Delete"
                divided
              >
                删除
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div> 
   <!-- 主要内容区域 -->
    <div class="content-section">
      <el-row :gutter="20">
        <!-- 左侧主要内容 -->
        <el-col :span="16">
          <!-- 基本信息卡片 -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
              </div>
            </template>
            <div class="info-content">
              <div class="info-row">
                <label>代码仓库：</label>
                <span>{{ reviewRecord?.codeRepository || '-' }}</span>
              </div>
              <div class="info-row">
                <label>文件路径：</label>
                <span>{{ reviewRecord?.codeFilePath || '-' }}</span>
              </div>
              <div class="info-row">
                <label>评审描述：</label>
                <div class="description-content">
                  {{ reviewRecord?.description || '-' }}
                </div>
              </div>
              <div class="info-row">
                <label>总体评分：</label>
                <el-rate
                  v-if="reviewRecord?.overallScore"
                  :model-value="reviewRecord.overallScore"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}分"
                />
                <span v-else>未评分</span>
              </div>
              <div class="info-row">
                <label>评审总结：</label>
                <div class="summary-content">
                  {{ reviewRecord?.summary || '-' }}
                </div>
              </div>
            </div>
          </el-card>

          <!-- 代码截图卡片 -->
          <el-card class="screenshots-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>代码截图 ({{ screenshots.length }})</span>
                <el-button
                  v-if="canEdit"
                  type="primary"
                  size="small"
                  @click="handleUploadScreenshot"
                >
                  上传截图
                </el-button>
              </div>
            </template>
            <div class="screenshots-content">
              <div v-if="screenshots.length === 0" class="no-screenshots">
                <el-empty description="暂无截图" :image-size="80" />
              </div>
              <div v-else class="screenshots-grid">
                <div
                  v-for="screenshot in screenshots"
                  :key="screenshot.id"
                  class="screenshot-item"
                >
                  <el-image
                    :src="screenshot.fileUrl"
                    :alt="screenshot.fileName"
                    fit="cover"
                    class="screenshot-image"
                    :preview-src-list="screenshotUrls"
                    :initial-index="screenshots.findIndex(s => s.id === screenshot.id)"
                  />
                  <div class="screenshot-overlay">
                    <div class="screenshot-actions">
                      <el-button
                        type="primary"
                        size="small"
                        text
                        @click="handlePreviewScreenshot(screenshot)"
                      >
                        预览
                      </el-button>
                      <el-button
                        v-if="canEdit"
                        type="danger"
                        size="small"
                        text
                        @click="handleDeleteScreenshot(screenshot)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                  <div class="screenshot-info">
                    <p class="screenshot-name">{{ screenshot.fileName }}</p>
                    <p class="screenshot-desc">{{ screenshot.description || '无描述' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-card>          <!
-- 问题列表卡片 -->
          <el-card class="issues-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>发现的问题 ({{ issues.length }})</span>
                <el-button
                  v-if="canEdit"
                  type="primary"
                  size="small"
                  @click="handleAddIssue"
                >
                  添加问题
                </el-button>
              </div>
            </template>
            <div class="issues-content">
              <div v-if="issues.length === 0" class="no-issues">
                <el-empty description="暂无问题" :image-size="80" />
              </div>
              <div v-else class="issues-list">
                <div
                  v-for="issue in issues"
                  :key="issue.id"
                  class="issue-item"
                >
                  <div class="issue-header">
                    <div class="issue-tags">
                      <el-tag :type="getSeverityType(issue.severity)" size="small">
                        {{ getSeverityText(issue.severity) }}
                      </el-tag>
                      <el-tag type="info" size="small">
                        {{ getIssueTypeText(issue.issueType) }}
                      </el-tag>
                      <el-tag :type="getIssueStatusType(issue.status)" size="small">
                        {{ getIssueStatusText(issue.status) }}
                      </el-tag>
                    </div>
                    <div class="issue-actions">
                      <el-button
                        type="primary"
                        size="small"
                        text
                        @click="handleViewIssue(issue)"
                      >
                        查看详情
                      </el-button>
                      <el-button
                        v-if="canEditIssue(issue)"
                        type="warning"
                        size="small"
                        text
                        @click="handleEditIssue(issue)"
                      >
                        编辑
                      </el-button>
                    </div>
                  </div>
                  <div class="issue-content">
                    <h4 class="issue-title">{{ issue.title }}</h4>
                    <p class="issue-description">{{ issue.description }}</p>
                    <div v-if="issue.suggestion" class="issue-suggestion">
                      <strong>改进建议：</strong>{{ issue.suggestion }}
                    </div>
                    <div v-if="issue.lineNumber" class="issue-line">
                      <strong>代码行号：</strong>{{ issue.lineNumber }}
                    </div>
                    <div v-if="issue.codeSnippet" class="issue-code">
                      <strong>代码片段：</strong>
                      <pre><code>{{ issue.codeSnippet }}</code></pre>
                    </div>
                    <div v-if="issue.fixRecordCount && issue.fixRecordCount > 0" class="issue-fix-info">
                      <el-tag type="success" size="small">
                        已有 {{ issue.fixRecordCount }} 个整改记录
                      </el-tag>
                      <span v-if="issue.latestFixStatus" class="latest-fix-status">
                        最新状态：{{ getFixStatusText(issue.latestFixStatus) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>  
      <!-- 右侧边栏 -->
        <el-col :span="8">
          <!-- 评审统计卡片 -->
          <el-card class="stats-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>评审统计</span>
              </div>
            </template>
            <div class="stats-content">
              <div class="stat-item">
                <div class="stat-value">{{ screenshots.length }}</div>
                <div class="stat-label">代码截图</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ issues.length }}</div>
                <div class="stat-label">发现问题</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ criticalIssueCount }}</div>
                <div class="stat-label">严重问题</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ resolvedIssueCount }}</div>
                <div class="stat-label">已解决问题</div>
              </div>
            </div>
          </el-card>

          <!-- 操作历史卡片 -->
          <el-card class="history-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>操作历史</span>
              </div>
            </template>
            <div class="history-content">
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in operationHistory"
                  :key="index"
                  :timestamp="item.timestamp"
                  :type="item.type"
                >
                  <div class="history-item">
                    <div class="history-action">{{ item.action }}</div>
                    <div class="history-user">{{ item.user }}</div>
                    <div v-if="item.remark" class="history-remark">{{ item.remark }}</div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 对话框组件 -->
    <ReviewRecordDialog
      v-model:visible="editDialogVisible"
      :record="reviewRecord"
      mode="edit"
      @success="handleEditSuccess"
    />

    <IssueDialog
      v-model:visible="issueDialogVisible"
      :issue="currentIssue"
      @success="handleIssueSuccess"
    />
  </div>
</template><script
 setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Check,
  More,
  CopyDocument,
  RefreshLeft,
  Delete
} from '@element-plus/icons-vue'
import {
  reviewRecordApi,
  screenshotApi,
  issueApi,
  type ReviewRecord,
  type CodeScreenshot,
  type Issue
} from '@/api/reviewRecord'
import ReviewRecordDialog from './components/ReviewRecordDialog.vue'
import IssueDialog from './components/IssueDialog.vue'

// 路由
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const reviewRecord = ref<ReviewRecord | null>(null)
const screenshots = ref<CodeScreenshot[]>([])
const issues = ref<Issue[]>([])
const editDialogVisible = ref(false)
const issueDialogVisible = ref(false)
const currentIssue = ref<Issue | null>(null)

// 模拟操作历史数据
const operationHistory = ref([
  {
    timestamp: '2024-01-15 10:30:00',
    type: 'primary',
    action: '创建评审记录',
    user: '张三',
    remark: ''
  },
  {
    timestamp: '2024-01-15 14:20:00',
    type: 'success',
    action: '提交评审',
    user: '张三',
    remark: '完成初步评审'
  }
])

// 计算属性
const screenshotUrls = computed(() => screenshots.value.map(s => s.fileUrl))

const criticalIssueCount = computed(() => 
  issues.value.filter(issue => issue.severity === 'CRITICAL').length
)

const resolvedIssueCount = computed(() => 
  issues.value.filter(issue => issue.status === 'RESOLVED' || issue.status === 'CLOSED').length
)

const canEdit = computed(() => 
  reviewRecord.value?.status === 'DRAFT' || reviewRecord.value?.status === 'SUBMITTED'
)

const canSubmit = computed(() => 
  reviewRecord.value?.status === 'DRAFT'
)

const canMarkReReview = computed(() => 
  reviewRecord.value?.status === 'COMPLETED'
)

const canDelete = computed(() => 
  reviewRecord.value?.status === 'DRAFT' || reviewRecord.value?.status === 'CANCELLED'
)

// 生命周期
onMounted(() => {
  loadData()
})

// 方法
const loadData = async () => {
  const id = route.params.id as string
  if (!id) return
  
  loading.value = true
  try {
    // 加载评审记录详情
    // const response = await reviewRecordApi.getDetails(Number(id))
    // reviewRecord.value = response.data
    
    // 模拟数据
    reviewRecord.value = {
      id: Number(id),
      assignmentId: 1,
      title: '用户登录功能代码评审',
      codeRepository: 'https://github.com/company/project.git',
      codeFilePath: 'src/main/java/com/company/service/UserService.java',
      description: '对用户登录功能进行全面的代码评审，包括安全性、性能和代码规范等方面。',
      overallScore: 8,
      summary: '整体代码质量良好，但在异常处理和日志记录方面需要改进。',
      status: 'COMPLETED',
      needsReReview: false,
      reviewerName: '张三',
      revieweeName: '李四',
      createdAt: '2024-01-15 10:30:00'
    }
    
    // 加载截图
    screenshots.value = [
      {
        id: 1,
        reviewRecordId: Number(id),
        fileName: 'login-method.png',
        fileUrl: 'https://via.placeholder.com/400x300',
        description: '登录方法实现'
      }
    ]
    
    // 加载问题
    issues.value = [
      {
        id: 1,
        reviewRecordId: Number(id),
        issueType: 'SECURITY_VULNERABILITY',
        severity: 'CRITICAL',
        title: '密码明文传输安全风险',
        description: '用户密码在传输过程中未进行加密处理，存在安全风险。',
        suggestion: '建议使用HTTPS协议并对密码进行加密处理。',
        status: 'OPEN',
        lineNumber: 45,
        fixRecordCount: 1,
        latestFixStatus: 'SUBMITTED'
      }
    ]
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}co
nst handleBack = () => {
  router.back()
}

const handleEdit = () => {
  editDialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('确定要提交这个评审记录吗？', '确认提交', {
      type: 'warning'
    })
    
    // await reviewRecordApi.submit(reviewRecord.value!.id!)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

const handleMoreAction = async (command: string) => {
  switch (command) {
    case 'copy':
      await handleCopy()
      break
    case 'reReview':
      await handleMarkReReview()
      break
    case 'delete':
      await handleDelete()
      break
  }
}

const handleCopy = async () => {
  try {
    await ElMessageBox.confirm('确定要复制这个评审记录吗？', '确认复制', {
      type: 'info'
    })
    ElMessage.success('复制成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('复制失败')
    }
  }
}

const handleMarkReReview = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入重新评审的原因', '标记重新评审', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea'
    })
    
    // await reviewRecordApi.markReReview(reviewRecord.value!.id!, reason)
    ElMessage.success('标记成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('标记失败')
    }
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个评审记录吗？删除后无法恢复！', '确认删除', {
      type: 'warning'
    })
    
    // await reviewRecordApi.delete(reviewRecord.value!.id!)
    ElMessage.success('删除成功')
    router.push('/reviews')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleUploadScreenshot = () => {
  // 实现截图上传逻辑
  ElMessage.info('截图上传功能开发中')
}

const handlePreviewScreenshot = (screenshot: CodeScreenshot) => {
  // 预览截图
  console.log('预览截图:', screenshot)
}

const handleDeleteScreenshot = async (screenshot: CodeScreenshot) => {
  try {
    await ElMessageBox.confirm('确定要删除这个截图吗？', '确认删除', {
      type: 'warning'
    })
    
    // await screenshotApi.delete(screenshot.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}c
onst handleAddIssue = () => {
  currentIssue.value = null
  issueDialogVisible.value = true
}

const handleViewIssue = (issue: Issue) => {
  // 跳转到问题详情页面
  router.push(`/issues/${issue.id}`)
}

const handleEditIssue = (issue: Issue) => {
  currentIssue.value = { ...issue }
  issueDialogVisible.value = true
}

const canEditIssue = (issue: Issue) => {
  return issue.status === 'OPEN' && canEdit.value
}

const handleEditSuccess = () => {
  editDialogVisible.value = false
  loadData()
}

const handleIssueSuccess = () => {
  issueDialogVisible.value = false
  loadData()
}

// 工具方法
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getStatusType = (status?: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: 'info',
    SUBMITTED: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return statusMap[status || ''] || 'info'
}

const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status || ''] || status || '-'
}

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

const getIssueStatusType = (status?: string) => {
  const statusMap: Record<string, string> = {
    OPEN: 'warning',
    IN_PROGRESS: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info',
    REJECTED: 'danger'
  }
  return statusMap[status || ''] || 'info'
}

const getIssueStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    OPEN: '待处理',
    IN_PROGRESS: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭',
    REJECTED: '已拒绝'
  }
  return statusMap[status || ''] || status || '-'
}

const getFixStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    SUBMITTED: '已提交',
    UNDER_REVIEW: '审核中',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    NEED_REVISION: '需要修改'
  }
  return statusMap[status] || status
}
</script><style
 scoped>
.review-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.title-section h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.content-section {
  margin-top: 20px;
}

.info-card,
.screenshots-card,
.issues-card,
.stats-card,
.history-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.info-content {
  space-y: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 16px;
}

.info-row label {
  min-width: 100px;
  font-weight: 500;
  color: #606266;
}

.description-content,
.summary-content {
  flex: 1;
  line-height: 1.6;
  white-space: pre-wrap;
}

.screenshots-content {
  min-height: 200px;
}

.no-screenshots,
.no-issues {
  text-align: center;
  padding: 40px;
}

.screenshots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.screenshot-item {
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.screenshot-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.screenshot-image {
  width: 100%;
  height: 150px;
}

.screenshot-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.screenshot-item:hover .screenshot-overlay {
  opacity: 1;
}

.screenshot-actions {
  display: flex;
  gap: 8px;
}

.screenshot-info {
  padding: 12px;
}

.screenshot-name {
  margin: 0 0 4px 0;
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.screenshot-desc {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.issues-list {
  space-y: 16px;
}

.issue-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.issue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.issue-tags {
  display: flex;
  gap: 8px;
}

.issue-actions {
  display: flex;
  gap: 8px;
}

.issue-content {
  space-y: 8px;
}

.issue-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.issue-description {
  margin: 0 0 8px 0;
  color: #606266;
  line-height: 1.6;
}

.issue-suggestion,
.issue-line {
  margin: 8px 0;
  font-size: 14px;
  color: #909399;
}

.issue-code {
  margin: 8px 0;
}

.issue-code pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0 0 0;
}

.issue-fix-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.latest-fix-status {
  font-size: 14px;
  color: #909399;
}

.stats-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.history-content {
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  padding: 8px 0;
}

.history-action {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.history-user {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.history-remark {
  font-size: 14px;
  color: #606266;
  font-style: italic;
}
</style>