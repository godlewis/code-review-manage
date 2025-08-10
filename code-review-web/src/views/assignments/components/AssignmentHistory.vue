<template>
  <div class="assignment-history">
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
        <el-form-item label="用户">
          <el-select v-model="filterForm.userId" placeholder="选择用户" clearable filterable>
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
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
          <el-button type="primary" @click="loadHistory">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button @click="exportHistory">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史统计图表 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="分配趋势图">
          <div ref="trendChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="分配分布图">
          <div ref="distributionChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 历史记录表格 -->
    <el-card class="history-table">
      <template #header>
        <div class="card-header">
          <span>分配历史</span>
          <div class="header-actions">
            <el-button size="small" @click="showStatistics">
              <el-icon><DataAnalysis /></el-icon>
              统计分析
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="historyData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="weekStartDate" label="周开始日期" width="120" sortable />
        <el-table-column prop="teamName" label="团队" width="120" />
        <el-table-column prop="reviewerName" label="评审者" width="120" />
        <el-table-column prop="revieweeName" label="被评审者" width="120" />
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
        <el-table-column prop="skillMatchScore" label="技能匹配" width="100">
          <template #default="{ row }">
            <span v-if="row.skillMatchScore">{{ row.skillMatchScore.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="loadBalanceScore" label="负载均衡" width="100">
          <template #default="{ row }">
            <span v-if="row.loadBalanceScore">{{ row.loadBalanceScore.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="diversityScore" label="多样性" width="100">
          <template #default="{ row }">
            <span v-if="row.diversityScore">{{ row.diversityScore.toFixed(2) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="isManualAdjusted" label="手动调整" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isManualAdjusted" type="warning" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetails(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadHistory"
        @current-change="loadHistory"
      />
    </el-card>

    <!-- 统计分析对话框 -->
    <el-dialog
      v-model="showStatisticsDialog"
      title="历史统计分析"
      width="800px"
    >
      <div v-if="statisticsData" class="statistics-content">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总分配数" :value="statisticsData.totalAssignments" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="已完成" :value="statisticsData.completedAssignments" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="完成率" :value="statisticsData.completionRate" suffix="%" :precision="1" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="手动调整率" :value="statisticsData.adjustmentRate" suffix="%" :precision="1" />
          </el-col>
        </el-row>
        
        <el-divider />
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic 
              title="平均技能匹配分数" 
              :value="statisticsData.avgSkillMatchScore" 
              :precision="2"
            />
          </el-col>
          <el-col :span="8">
            <el-statistic 
              title="平均负载均衡分数" 
              :value="statisticsData.avgLoadBalanceScore" 
              :precision="2"
            />
          </el-col>
          <el-col :span="8">
            <el-statistic 
              title="平均多样性分数" 
              :value="statisticsData.avgDiversityScore" 
              :precision="2"
            />
          </el-col>
        </el-row>

        <el-divider />

        <!-- 用户参与度统计 -->
        <div class="user-participation">
          <h4>用户参与度统计</h4>
          <el-table :data="statisticsData.userParticipation" size="small">
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column prop="reviewerCount" label="作为评审者" width="100" />
            <el-table-column prop="revieweeCount" label="作为被评审者" width="120" />
            <el-table-column prop="totalCount" label="总参与次数" width="120" />
            <el-table-column prop="avgScore" label="平均得分" width="100">
              <template #default="{ row }">
                {{ row.avgScore?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailsDialog"
      title="分配详情"
      width="700px"
    >
      <div v-if="currentRecord" class="assignment-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分配ID">{{ currentRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="团队">{{ currentRecord.teamName }}</el-descriptions-item>
          <el-descriptions-item label="评审者">{{ currentRecord.reviewerName }}</el-descriptions-item>
          <el-descriptions-item label="被评审者">{{ currentRecord.revieweeName }}</el-descriptions-item>
          <el-descriptions-item label="周开始日期">{{ currentRecord.weekStartDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="技能匹配分数">
            {{ currentRecord.skillMatchScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="负载均衡分数">
            {{ currentRecord.loadBalanceScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="多样性分数">
            {{ currentRecord.diversityScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="总分数">
            {{ currentRecord.totalScore?.toFixed(2) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手动调整">
            {{ currentRecord.isManualAdjusted ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentRecord.remarks || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentRecord.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(currentRecord.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { assignmentApi } from '@/api/assignment'
import { teamApi } from '@/api/team'
import { userApi } from '@/api/user'

// 响应式数据
const loading = ref(false)
const historyData = ref([])
const teams = ref([])
const users = ref([])
const statisticsData = ref(null)
const currentRecord = ref(null)

// 对话框显示状态
const showStatisticsDialog = ref(false)
const showDetailsDialog = ref(false)

// 图表引用
const trendChartRef = ref()
const distributionChartRef = ref()
let trendChart = null
let distributionChart = null

// 筛选表单
const filterForm = reactive({
  teamId: null,
  userId: null,
  dateRange: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 生命周期
onMounted(() => {
  loadTeams()
  loadUsers()
  loadHistory()
  nextTick(() => {
    initCharts()
  })
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

const loadUsers = async () => {
  try {
    const response = await userApi.getAllUsers()
    users.value = response.data || []
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    const params = {
      teamId: filterForm.teamId,
      userId: filterForm.userId,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1],
      page: pagination.page,
      size: pagination.size
    }
    
    let response
    if (filterForm.teamId) {
      response = await assignmentApi.getTeamAssignmentHistory(
        filterForm.teamId,
        params.startDate,
        params.endDate
      )
    } else if (filterForm.userId) {
      response = await assignmentApi.getUserAssignmentHistory(
        filterForm.userId,
        params.startDate,
        params.endDate
      )
    } else {
      response = await assignmentApi.getAssignments(params)
    }
    
    historyData.value = response.data?.list || []
    pagination.total = response.data?.total || 0
    
    // 更新图表
    updateCharts()
  } catch (error) {
    ElMessage.error('加载历史记录失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  Object.assign(filterForm, {
    teamId: null,
    userId: null,
    dateRange: null
  })
  pagination.page = 1
  loadHistory()
}

const exportHistory = async () => {
  try {
    const params = {
      teamId: filterForm.teamId,
      userId: filterForm.userId,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1],
      export: true
    }
    
    // 这里应该调用导出API，暂时用消息提示
    ElMessage.success('导出功能开发中...')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const showStatistics = async () => {
  try {
    const params = {
      teamId: filterForm.teamId,
      startDate: filterForm.dateRange?.[0] || new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      endDate: filterForm.dateRange?.[1] || new Date().toISOString().split('T')[0]
    }
    
    const response = await assignmentApi.getAssignmentStatistics(params.teamId, params.startDate, params.endDate)
    statisticsData.value = response.data
    showStatisticsDialog.value = true
  } catch (error) {
    ElMessage.error('加载统计信息失败')
  }
}

const showDetails = (record) => {
  currentRecord.value = record
  showDetailsDialog.value = true
}

const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (distributionChartRef.value) {
    distributionChart = echarts.init(distributionChartRef.value)
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    trendChart?.resize()
    distributionChart?.resize()
  })
}

const updateCharts = () => {
  updateTrendChart()
  updateDistributionChart()
}

const updateTrendChart = () => {
  if (!trendChart || !historyData.value.length) return
  
  // 按周统计分配数量
  const weeklyData = {}
  historyData.value.forEach(item => {
    const week = item.weekStartDate
    if (!weeklyData[week]) {
      weeklyData[week] = { total: 0, completed: 0 }
    }
    weeklyData[week].total++
    if (item.status === 'COMPLETED') {
      weeklyData[week].completed++
    }
  })
  
  const weeks = Object.keys(weeklyData).sort()
  const totalData = weeks.map(week => weeklyData[week].total)
  const completedData = weeks.map(week => weeklyData[week].completed)
  
  const option = {
    title: {
      text: '分配趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['总分配', '已完成'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: weeks
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '总分配',
        type: 'line',
        data: totalData,
        smooth: true
      },
      {
        name: '已完成',
        type: 'line',
        data: completedData,
        smooth: true
      }
    ]
  }
  
  trendChart.setOption(option)
}

const updateDistributionChart = () => {
  if (!distributionChart || !historyData.value.length) return
  
  // 统计状态分布
  const statusCount = {}
  historyData.value.forEach(item => {
    const status = getStatusText(item.status)
    statusCount[status] = (statusCount[status] || 0) + 1
  })
  
  const data = Object.entries(statusCount).map(([name, value]) => ({ name, value }))
  
  const option = {
    title: {
      text: '状态分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '分配状态',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  distributionChart.setOption(option)
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
.assignment-history {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.charts-row {
  margin-bottom: 20px;
}

.history-table {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-content {
  padding: 20px 0;
}

.user-participation {
  margin-top: 20px;
}

.user-participation h4 {
  margin-bottom: 10px;
  color: #303133;
}

.assignment-details {
  padding: 10px 0;
}
</style>