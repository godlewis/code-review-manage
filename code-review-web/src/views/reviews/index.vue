<template>
  <div class="review-records-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="header-left">
        <h2>评审记录</h2>
        <p class="page-description">管理和查看代码评审记录</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate" :icon="Plus">
          创建评审记录
        </el-button>
        <el-button @click="handleRefresh" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="标题">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入评审标题"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="评审者">
          <el-input
            v-model="searchForm.reviewerName"
            placeholder="请输入评审者姓名"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">
            搜索
          </el-button>
          <el-button @click="handleReset" :icon="RefreshRight">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="评审标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reviewerName" label="评审者" width="120" />
        <el-table-column prop="revieweeName" label="被评审者" width="120" />
        <el-table-column prop="overallScore" label="评分" width="80" align="center">
          <template #default="{ row }">
            <el-rate
              v-if="row.overallScore"
              :model-value="row.overallScore"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}分"
            />
            <span v-else class="text-gray-400">未评分</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="needsReReview" label="重新评审" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.needsReReview" type="warning" size="small">
              需要
            </el-tag>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleView(row)"
              :icon="View"
            >
              查看
            </el-button>
            <el-button
              v-if="canEdit(row)"
              type="warning"
              size="small"
              @click="handleEdit(row)"
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-dropdown @command="(command) => handleMoreAction(command, row)">
              <el-button size="small" :icon="More">
                更多
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="canSubmit(row)"
                    command="submit"
                    :icon="Check"
                  >
                    提交
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canMarkReReview(row)"
                    command="reReview"
                    :icon="RefreshLeft"
                  >
                    标记重新评审
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="copy"
                    :icon="CopyDocument"
                  >
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="canDelete(row)"
                    command="delete"
                    :icon="Delete"
                    divided
                  >
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedRows.length > 0" class="batch-actions">
        <span class="selected-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button type="warning" size="small" @click="handleBatchUpdateStatus">
          批量更新状态
        </el-button>
        <el-button type="danger" size="small" @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <ReviewRecordDialog
      v-model:visible="dialogVisible"
      :record="currentRecord"
      :mode="dialogMode"
      @success="handleDialogSuccess"
    />

    <!-- 批量更新状态对话框 -->
    <el-dialog
      v-model="batchStatusDialogVisible"
      title="批量更新状态"
      width="400px"
    >
      <el-form :model="batchStatusForm" label-width="80px">
        <el-form-item label="新状态">
          <el-select v-model="batchStatusForm.status" placeholder="请选择状态">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchStatusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBatchUpdateStatus">确定</el-button>
      </template>
    </el-dialog>

    <!-- 标记重新评审对话框 -->
    <el-dialog
      v-model="reReviewDialogVisible"
      title="标记重新评审"
      width="500px"
    >
      <el-form :model="reReviewForm" label-width="100px">
        <el-form-item label="重新评审原因" required>
          <el-input
            v-model="reReviewForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入重新评审的原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reReviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMarkReReview">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  RefreshRight,
  View,
  Edit,
  More,
  Check,
  RefreshLeft,
  CopyDocument,
  Delete
} from '@element-plus/icons-vue'
import { reviewRecordApi, type ReviewRecord } from '@/api/reviewRecord'
import ReviewRecordDialog from './components/ReviewRecordDialog.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<ReviewRecord[]>([])
const selectedRows = ref<ReviewRecord[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentRecord = ref<ReviewRecord | null>(null)
const batchStatusDialogVisible = ref(false)
const reReviewDialogVisible = ref(false)
const currentReReviewRecord = ref<ReviewRecord | null>(null)

// 搜索表单
const searchForm = reactive({
  title: '',
  status: '',
  reviewerName: '',
  dateRange: null as [string, string] | null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 批量状态更新表单
const batchStatusForm = reactive({
  status: ''
})

// 重新评审表单
const reReviewForm = reactive({
  reason: ''
})

// 生命周期
onMounted(() => {
  loadData()
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    // 这里应该调用实际的API，暂时使用模拟数据
    const mockData = [
      {
        id: 1,
        title: '用户登录功能代码评审',
        reviewerName: '张三',
        revieweeName: '李四',
        overallScore: 8,
        status: 'COMPLETED',
        needsReReview: false,
        createdAt: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        title: '订单管理模块评审',
        reviewerName: '王五',
        revieweeName: '赵六',
        overallScore: null,
        status: 'IN_PROGRESS',
        needsReReview: false,
        createdAt: '2024-01-14 14:20:00'
      }
    ]
    
    tableData.value = mockData
    pagination.total = mockData.length
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    title: '',
    status: '',
    reviewerName: '',
    dateRange: null
  })
  handleSearch()
}

const handleRefresh = () => {
  loadData()
}

const handleCreate = () => {
  currentRecord.value = null
  dialogMode.value = 'create'
  dialogVisible.value = true
}

const handleView = (record: ReviewRecord) => {
  // 跳转到详情页面
  console.log('查看评审记录:', record)
}

const handleEdit = (record: ReviewRecord) => {
  currentRecord.value = { ...record }
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

const handleMoreAction = async (command: string, record: ReviewRecord) => {
  switch (command) {
    case 'submit':
      await handleSubmit(record)
      break
    case 'reReview':
      handleMarkReReview(record)
      break
    case 'copy':
      await handleCopy(record)
      break
    case 'delete':
      await handleDelete(record)
      break
  }
}

const handleSubmit = async (record: ReviewRecord) => {
  try {
    await ElMessageBox.confirm('确定要提交这个评审记录吗？', '确认提交', {
      type: 'warning'
    })
    
    // await reviewRecordApi.submit(record.id!)
    ElMessage.success('提交成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

const handleMarkReReview = (record: ReviewRecord) => {
  currentReReviewRecord.value = record
  reReviewForm.reason = ''
  reReviewDialogVisible.value = true
}

const confirmMarkReReview = async () => {
  if (!reReviewForm.reason.trim()) {
    ElMessage.warning('请输入重新评审原因')
    return
  }
  
  try {
    // await reviewRecordApi.markReReview(currentReReviewRecord.value!.id!, reReviewForm.reason)
    ElMessage.success('标记成功')
    reReviewDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('标记失败')
  }
}

const handleCopy = async (record: ReviewRecord) => {
  try {
    await ElMessageBox.confirm('确定要复制这个评审记录吗？', '确认复制', {
      type: 'info'
    })
    
    // 这里需要选择新的分配ID，暂时跳过
    ElMessage.success('复制成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('复制失败')
    }
  }
}

const handleDelete = async (record: ReviewRecord) => {
  try {
    await ElMessageBox.confirm('确定要删除这个评审记录吗？删除后无法恢复！', '确认删除', {
      type: 'warning'
    })
    
    // await reviewRecordApi.delete(record.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection: ReviewRecord[]) => {
  selectedRows.value = selection
}

const handleBatchUpdateStatus = () => {
  batchStatusForm.status = ''
  batchStatusDialogVisible.value = true
}

const confirmBatchUpdateStatus = async () => {
  if (!batchStatusForm.status) {
    ElMessage.warning('请选择状态')
    return
  }
  
  try {
    const ids = selectedRows.value.map(row => row.id!)
    // await reviewRecordApi.batchUpdateStatus(ids, batchStatusForm.status, currentUserId)
    ElMessage.success('批量更新成功')
    batchStatusDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('批量更新失败')
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个评审记录吗？删除后无法恢复！`, '确认删除', {
      type: 'warning'
    })
    
    // 批量删除逻辑
    ElMessage.success('批量删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadData()
}

const handleDialogSuccess = () => {
  dialogVisible.value = false
  loadData()
}

// 工具方法
const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: 'info',
    SUBMITTED: 'warning',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const canEdit = (record: ReviewRecord) => {
  return record.status === 'DRAFT' || record.status === 'SUBMITTED'
}

const canSubmit = (record: ReviewRecord) => {
  return record.status === 'DRAFT'
}

const canMarkReReview = (record: ReviewRecord) => {
  return record.status === 'COMPLETED'
}

const canDelete = (record: ReviewRecord) => {
  return record.status === 'DRAFT' || record.status === 'CANCELLED'
}
</script>

<style scoped>
.review-records-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.selected-info {
  color: #606266;
  font-size: 14px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.text-gray-400 {
  color: #c0c4cc;
}
</style>