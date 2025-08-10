<template>
  <div class="current-assignments">
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="团队">
          <el-select v-model="filterForm.teamId" placeholder="选择团队" clearable>
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="周开始日期">
          <el-date-picker
            v-model="filterForm.weekStart"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable>
            <el-option label="待分配" value="PENDING" />
            <el-option label="已分配" value="ASSIGNED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAssignments">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分配列表 -->
    <el-card class="assignment-list">
      <template #header>
        <div class="card-header">
          <span>分配列表</span>
          <div class="header-actions">
            <el-button size="small" @click="loadStatistics">
              <el-icon><DataAnalysis /></el-icon>
              统计信息
            </el-button>
            <el-button size="small" @click="validateAssignments">
              <el-icon><Check /></el-icon>
              验证分配
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="assignments"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="teamName" label="团队" width="120" />
        <el-table-column prop="reviewerName" label="评审者" width="120" />
        <el-table-column prop="revieweeName" label="被评审者" width="120" />
        <el-table-column prop="weekStartDate" label="周开始日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分" width="80">
          <template #default="{ row }">
            <span v-if="row.totalScore">{{ row.totalScore.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isManualAdjusted" label="手动调整" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isManualAdjusted" type="warning" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showAdjustDialog(row)">调整</el-button>
            <el-button size="small" type="info" @click="showDetails(row)">详情</el-button>
            <el-popconfirm
              title="确定删除这个分配吗？"
              @confirm="deleteAssignment(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadAssignments"
        @current-change="loadAssignments"
      />
    </el-card>

    <!-- 调整分配对话框 -->
    <el-dialog
      v-model="showAdjustDialogVisible"
      title="调整分配"
      width="500px"
    >
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef">
        <el-form-item label="当前评审者">
          <el-input :value="currentAssignment?.reviewerName" disabled />
        </el-form-item>
        <el-form-item label="当前被评审者">
          <el-input :value="currentAssignment?.revieweeName" disabled />
        </el-form-item>
        <el-form-item label="新被评审者" prop="newRevieweeId">
          <el-select v-model="adjustForm.newRevieweeId" placeholder="选择新的被评审者">
            <el-option
              v-for="user in teamMembers"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整原因" prop="remarks">
          <el-input
            v-model="adjustForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="adjustAssignment" :loading="adjusting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配详情对话框 -->
    <el-dialog
      v-model="showDetailsDialog"
      title="分配详情"
      width="700px"
    >
      <div v-if="currentAssignment" class="assignment-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分配ID">{{ currentAssignment.id }}</el-descriptions-item>
          <el-descriptions-item label="团队">{{ currentAssignment.teamName }}</el-descriptions-item>
          <el-descriptions-item label="评审者">{{ currentAssignment.reviewerName }}</el-descriptions-item>
          <el-descriptions-item label="被评审者">{{ currentAssignment.revieweeName }}</el-descriptions-item>
          <el-descriptions-item label="周开始日期">{{ currentAssignment.weekStartDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentAssignment.status)">
              {{ getStatusText(currentAssignment.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="技能匹配分数">
            {{ currentAssignment.skillMatchScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="负载均衡分数">
            {{ currentAssignment.loadBalanceScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="多样性分数">
            {{ currentAssignment.diversityScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="总分数">
            {{ currentAssignment.totalScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手动调整">
            {{ currentAssignment.isManualAdjusted ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentAssignment.remarks || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentAssignment.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(currentAssignment.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog
      v-model="showStatisticsDialog"
      title="分配统计信息"
      width="600px"
    >
      <div v-if="statistics" class="statistics-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-statistic title="总分配数" :value="statistics.totalAssignments" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="已完成" :value="statistics.completedAssignments" />
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-statistic title="进行中" :value="statistics.inProgressAssignments" />
          </el-col>
          <el-col :span="12">
            <el-statistic title="手动调整" :value="statistics.manualAdjustedCount" />
          </el-col>
        </el-row>
        <el-row style="margin-top: 20px;">
          <el-col :span="24">
            <el-statistic 
              title="平均总分" 
              :value="statistics.averageTotalScore" 
              :precision="2"
            />
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataAnalysis, Check } from '@element-plus/icons-vue'
import { assignmentApi } from '@/api/assignment'
import { teamApi } from '@/api/team'
import { userApi } from '@/api/user'

// 响应式数据
const loading = ref(false)
const adjusting = ref(false)
const assignments = ref([])
const teams = ref([])
const teamMembers = ref([])
const statistics = ref(null)
const currentAssignment = ref(null)

// 对话框显示状态
const showAdjustDialogVisible = ref(false)
const showDetailsDialog = ref(false)
const showStatisticsDialog = ref(false)

// 筛选表单
const filterForm = reactive({
  teamId: null,
  weekStart: null,
  status: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 调整分配表单
const adjustForm = reactive({
  newRevieweeId: null,
  remarks: ''
})

const adjustRules = {
  newRevieweeId: [{ required: true, message: '请选择新的被评审者', trigger: 'change' }],
  remarks: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

// 表单引用
const adjustFormRef = ref()

// 生命周期
onMounted(() => {
  loadTeams()
  loadAssignments()
})

// 方法
const loadTeams = async () => {
  try {
    const response = await teamApi.getTeams()
    teams.value = response.data || []
  } catch (error) {
    ElMessage.error('加载团队列表失败')
  }
}

const loadAssignments = async () => {
  loading.value = true
  try {
    const params = {
      ...filterForm,
      page: pagination.page,
      size: pagination.size
    }
    const response = await assignmentApi.getAssignments(params)
    assignments.value = response.data?.list || []
    pagination.total = response.data?.total || 0
  } catch (error) {
    ElMessage.error('加载分配列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  Object.assign(filterForm, {
    teamId: null,
    weekStart: null,
    status: null
  })
  pagination.page = 1
  loadAssignments()
}

const showAdjustDialog = async (assignment) => {
  currentAssignment.value = assignment
  
  // 加载团队成员
  try {
    const response = await userApi.getTeamMembers(assignment.teamId)
    teamMembers.value = response.data || []
  } catch (error) {
    ElMessage.error('加载团队成员失败')
    return
  }
  
  // 重置表单
  Object.assign(adjustForm, {
    newRevieweeId: null,
    remarks: ''
  })
  
  showAdjustDialogVisible.value = true
}

const adjustAssignment = async () => {
  if (!adjustFormRef.value) return
  
  const valid = await adjustFormRef.value.validate().catch(() => false)
  if (!valid) return

  adjusting.value = true
  try {
    await assignmentApi.adjustAssignment(currentAssignment.value.id, adjustForm)
    ElMessage.success('分配调整成功')
    showAdjustDialogVisible.value = false
    loadAssignments()
  } catch (error) {
    ElMessage.error('调整分配失败')
  } finally {
    adjusting.value = false
  }
}

const showDetails = (assignment) => {
  currentAssignment.value = assignment
  showDetailsDialog.value = true
}

const deleteAssignment = async (id) => {
  try {
    await assignmentApi.deleteAssignment(id)
    ElMessage.success('删除成功')
    loadAssignments()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const loadStatistics = async () => {
  try {
    const params = {
      teamId: filterForm.teamId,
      startDate: filterForm.weekStart || new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      endDate: new Date().toISOString().split('T')[0]
    }
    const response = await assignmentApi.getStatistics(params)
    statistics.value = response.data
    showStatisticsDialog.value = true
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  }
}

const validateAssignments = async () => {
  if (!filterForm.teamId || !filterForm.weekStart) {
    ElMessage.warning('请先选择团队和周开始日期')
    return
  }
  
  try {
    const response = await assignmentApi.validateAssignments(filterForm.teamId, filterForm.weekStart)
    const result = response.data
    
    if (result.valid) {
      ElMessage.success('分配验证通过')
    } else {
      ElMessageBox.alert(
        result.errors.join('\n'),
        '分配验证失败',
        { type: 'warning' }
      )
    }
  } catch (error) {
    ElMessage.error('验证分配失败')
  }
}

// 工具方法
const getStatusType = (status) => {
  const typeMap = {
    PENDING: '',
    ASSIGNED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    PENDING: '待分配',
    ASSIGNED: '已分配',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return textMap[status] || status
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.current-assignments {
  padding: 0;
}

.filter-card {
  margin-bottom: 20px;
}

.assignment-list {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignment-details {
  padding: 10px 0;
}

.statistics-content {
  padding: 20px 0;
}
</style>