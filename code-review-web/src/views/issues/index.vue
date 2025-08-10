<template>
  <div class="issues-container">
    <div class="page-header">
      <h1>问题管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建问题
        </el-button>
        <el-button @click="exportIssues">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :model="filterForm" inline>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="OPEN" />
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterForm.issueType" placeholder="选择类型" clearable>
            <el-option label="功能缺陷" value="FUNCTIONAL_DEFECT" />
            <el-option label="性能问题" value="PERFORMANCE_ISSUE" />
            <el-option label="安全漏洞" value="SECURITY_VULNERABILITY" />
            <el-option label="代码规范" value="CODE_STANDARD" />
            <el-option label="设计问题" value="DESIGN_ISSUE" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重级别">
          <el-select v-model="filterForm.severity" placeholder="选择严重级别" clearable>
            <el-option label="严重" value="CRITICAL" />
            <el-option label="重要" value="MAJOR" />
            <el-option label="一般" value="MINOR" />
            <el-option label="建议" value="SUGGESTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配给">
          <el-select v-model="filterForm.assignedTo" placeholder="选择分配人" clearable>
            <el-option 
              v-for="user in teamMembers" 
              :key="user.id" 
              :label="user.username" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchIssues">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 问题列表 -->
    <div class="table-section">
      <el-table 
        :data="issues" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link @click="viewIssueDetail(row)" type="primary">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="issueType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getIssueTypeTagType(row.issueType)">
              {{ getIssueTypeText(row.issueType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getSeverityTagType(row.severity)">
              {{ getSeverityText(row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignedTo" label="分配给" width="120">
          <template #default="{ row }">
            {{ getUserName(row.assignedTo) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewIssueDetail(row)">详情</el-button>
            <el-button size="small" @click="editIssue(row)">编辑</el-button>
            <el-dropdown @command="(command) => handleAction(command, row)">
              <el-button size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="assign">分配</el-dropdown-item>
                  <el-dropdown-item command="close" v-if="row.status !== 'CLOSED'">关闭</el-dropdown-item>
                  <el-dropdown-item command="reopen" v-if="row.status === 'CLOSED'">重新打开</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedIssues.length > 0">
        <span>已选择 {{ selectedIssues.length }} 项</span>
        <el-button @click="batchAssign">批量分配</el-button>
        <el-button @click="batchUpdateStatus">批量更新状态</el-button>
        <el-button type="danger" @click="batchDelete">批量删除</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑问题对话框 -->
    <IssueDialog
      v-model="showCreateDialog"
      :issue="currentIssue"
      :mode="dialogMode"
      @success="handleDialogSuccess"
    />

    <!-- 问题详情对话框 -->
    <IssueDetailDialog
      v-model="showDetailDialog"
      :issue-id="currentIssueId"
    />

    <!-- 分配对话框 -->
    <AssignDialog
      v-model="showAssignDialog"
      :issue-ids="assignIssueIds"
      :team-members="teamMembers"
      @success="handleAssignSuccess"
    />

    <!-- 批量状态更新对话框 -->
    <BatchStatusDialog
      v-model="showBatchStatusDialog"
      :issue-ids="selectedIssues.map(item => item.id)"
      @success="handleBatchStatusSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, ArrowDown } from '@element-plus/icons-vue'
import { issueApi } from '@/api/issue'
import { userApi } from '@/api/user'
import IssueDialog from './components/IssueDialog.vue'
import IssueDetailDialog from './components/IssueDetailDialog.vue'
import AssignDialog from './components/AssignDialog.vue'
import BatchStatusDialog from './components/BatchStatusDialog.vue'

// 响应式数据
const loading = ref(false)
const issues = ref([])
const selectedIssues = ref([])
const teamMembers = ref([])

// 对话框状态
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showAssignDialog = ref(false)
const showBatchStatusDialog = ref(false)

// 当前操作的问题
const currentIssue = ref(null)
const currentIssueId = ref(null)
const dialogMode = ref('create')
const assignIssueIds = ref([])

// 筛选表单
const filterForm = reactive({
  status: '',
  issueType: '',
  severity: '',
  assignedTo: '',
  dateRange: []
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 排序
const sortConfig = reactive({
  prop: '',
  order: ''
})

// 计算属性
const currentTeamId = computed(() => {
  // 从用户状态或路由参数获取当前团队ID
  return 1 // 临时硬编码
})

// 生命周期
onMounted(() => {
  loadTeamMembers()
  loadIssues()
})

// 方法
const loadIssues = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filterForm,
      teamId: currentTeamId.value
    }
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    
    if (sortConfig.prop) {
      params.sortBy = sortConfig.prop
      params.sortOrder = sortConfig.order === 'ascending' ? 'asc' : 'desc'
    }
    
    const response = await issueApi.getIssuesByTeam(currentTeamId.value, params)
    issues.value = response.data
    pagination.total = response.total || response.data.length
  } catch (error) {
    ElMessage.error('加载问题列表失败: ' + error.message)
  } finally {
    loading.value = false
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

const searchIssues = () => {
  pagination.page = 1
  loadIssues()
}

const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = key === 'dateRange' ? [] : ''
  })
  pagination.page = 1
  loadIssues()
}

const handleSelectionChange = (selection) => {
  selectedIssues.value = selection
}

const handleSortChange = ({ prop, order }) => {
  sortConfig.prop = prop
  sortConfig.order = order
  loadIssues()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadIssues()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadIssues()
}

const viewIssueDetail = (issue) => {
  currentIssueId.value = issue.id
  showDetailDialog.value = true
}

const editIssue = (issue) => {
  currentIssue.value = { ...issue }
  dialogMode.value = 'edit'
  showCreateDialog.value = true
}

const handleAction = async (command, issue) => {
  switch (command) {
    case 'assign':
      assignIssueIds.value = [issue.id]
      showAssignDialog.value = true
      break
    case 'close':
      await closeIssue(issue)
      break
    case 'reopen':
      await reopenIssue(issue)
      break
    case 'delete':
      await deleteIssue(issue)
      break
  }
}

const closeIssue = async (issue) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入关闭原因', '关闭问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await issueApi.closeIssue(issue.id, reason)
    ElMessage.success('问题已关闭')
    loadIssues()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('关闭问题失败: ' + error.message)
    }
  }
}

const reopenIssue = async (issue) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入重新打开原因', '重新打开问题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    await issueApi.reopenIssue(issue.id, reason)
    ElMessage.success('问题已重新打开')
    loadIssues()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重新打开问题失败: ' + error.message)
    }
  }
}

const deleteIssue = async (issue) => {
  try {
    await ElMessageBox.confirm('确定要删除这个问题吗？', '删除确认', {
      type: 'warning'
    })
    
    await issueApi.deleteIssue(issue.id)
    ElMessage.success('问题已删除')
    loadIssues()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除问题失败: ' + error.message)
    }
  }
}

const batchAssign = () => {
  assignIssueIds.value = selectedIssues.value.map(item => item.id)
  showAssignDialog.value = true
}

const batchUpdateStatus = () => {
  showBatchStatusDialog.value = true
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIssues.value.length} 个问题吗？`, '批量删除确认', {
      type: 'warning'
    })
    
    const ids = selectedIssues.value.map(item => item.id)
    await issueApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    selectedIssues.value = []
    loadIssues()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败: ' + error.message)
    }
  }
}

const exportIssues = async () => {
  try {
    const params = { ...filterForm, teamId: currentTeamId.value }
    await issueApi.exportIssues(params)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败: ' + error.message)
  }
}

const handleDialogSuccess = () => {
  showCreateDialog.value = false
  currentIssue.value = null
  dialogMode.value = 'create'
  loadIssues()
}

const handleAssignSuccess = () => {
  showAssignDialog.value = false
  assignIssueIds.value = []
  loadIssues()
}

const handleBatchStatusSuccess = () => {
  showBatchStatusDialog.value = false
  selectedIssues.value = []
  loadIssues()
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

const getUserName = (userId) => {
  if (!userId) return '-'
  const user = teamMembers.value.find(u => u.id === userId)
  return user ? user.username : `用户${userId}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.issues-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.table-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.batch-actions {
  padding: 15px 20px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-section {
  padding: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-table) {
  border-radius: 8px 8px 0 0;
}

:deep(.el-table__header) {
  background: #fafafa;
}
</style>