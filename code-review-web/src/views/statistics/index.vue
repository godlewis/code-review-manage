<template>
  <div class="statistics-page">
    <div class="page-header">
      <h2>统计分析</h2>
      <div class="header-controls">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
        />
        <el-select v-model="viewType" @change="handleViewTypeChange" style="margin-left: 12px;">
          <el-option label="个人统计" value="personal" />
          <el-option label="团队统计" value="team" v-if="canViewTeamStats" />
          <el-option label="全局统计" value="global" v-if="canViewGlobalStats" />
        </el-select>
        <el-button type="primary" @click="exportReport" style="margin-left: 12px;">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="overview-cards" v-if="overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon size="24" color="#409EFF"><Document /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ overview.quickMetrics?.todayReviews || 0 }}</div>
                <div class="card-label">今日评审</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon size="24" color="#E6A23C"><Warning /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ overview.quickMetrics?.pendingIssues || 0 }}</div>
                <div class="card-label">待处理问题</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon size="24" color="#67C23A"><CircleCheck /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ formatPercentage(overview.quickMetrics?.weeklyCompletionRate) }}</div>
                <div class="card-label">本周完成率</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="card-content">
              <div class="card-icon">
                <el-icon size="24" color="#F56C6C"><TrendCharts /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-value">{{ overview.quickMetrics?.qualityTrend || '稳定' }}</div>
                <div class="card-label">质量趋势</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 个人统计视图 -->
    <div v-if="viewType === 'personal'" class="statistics-content">
      <PersonalStatistics 
        :data="personalStats" 
        :loading="loading"
        :date-range="dateRange"
        @refresh="loadPersonalStatistics"
      />
    </div>

    <!-- 团队统计视图 -->
    <div v-if="viewType === 'team'" class="statistics-content">
      <TeamStatistics 
        :data="teamStats" 
        :loading="loading"
        :date-range="dateRange"
        @refresh="loadTeamStatistics"
      />
    </div>

    <!-- 全局统计视图 -->
    <div v-if="viewType === 'global'" class="statistics-content">
      <GlobalStatistics 
        :data="globalStats" 
        :loading="loading"
        :date-range="dateRange"
        @refresh="loadGlobalStatistics"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Warning, CircleCheck, TrendCharts, Download } from '@element-plus/icons-vue'
import { statisticsApi, type PersonalStatistics as PersonalStatsType, type TeamStatistics as TeamStatsType, type GlobalStatistics as GlobalStatsType, type StatisticsOverview } from '@/api/statistics'
import PersonalStatistics from './components/PersonalStatistics.vue'
import TeamStatistics from './components/TeamStatistics.vue'
import GlobalStatistics from './components/GlobalStatistics.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const viewType = ref('personal')
const dateRange = ref<[string, string]>([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  new Date().toISOString().split('T')[0]
])

const overview = ref<StatisticsOverview | null>(null)
const personalStats = ref<PersonalStatsType | null>(null)
const teamStats = ref<TeamStatsType | null>(null)
const globalStats = ref<GlobalStatsType | null>(null)

// 计算属性
const canViewTeamStats = computed(() => {
  return userStore.user?.role === 'TEAM_LEADER' || userStore.user?.role === 'ARCHITECT'
})

const canViewGlobalStats = computed(() => {
  return userStore.user?.role === 'ARCHITECT'
})

// 方法
const formatPercentage = (value: number | undefined): string => {
  if (value === undefined || value === null) return '0%'
  return `${(value * 100).toFixed(1)}%`
}

const handleDateRangeChange = () => {
  loadCurrentViewData()
}

const handleViewTypeChange = () => {
  loadCurrentViewData()
}

const loadCurrentViewData = async () => {
  switch (viewType.value) {
    case 'personal':
      await loadPersonalStatistics()
      break
    case 'team':
      await loadTeamStatistics()
      break
    case 'global':
      await loadGlobalStatistics()
      break
  }
}

const loadOverview = async () => {
  try {
    const { data } = await statisticsApi.getStatisticsOverview()
    overview.value = data
  } catch (error) {
    console.error('加载统计概览失败:', error)
  }
}

const loadPersonalStatistics = async () => {
  if (!dateRange.value) return
  
  loading.value = true
  try {
    const { data } = await statisticsApi.getCurrentUserStatistics(
      dateRange.value[0],
      dateRange.value[1]
    )
    personalStats.value = data
  } catch (error) {
    ElMessage.error('加载个人统计数据失败')
    console.error('加载个人统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadTeamStatistics = async () => {
  if (!dateRange.value || !userStore.user?.teamId) return
  
  loading.value = true
  try {
    const { data } = await statisticsApi.getTeamStatistics(
      userStore.user.teamId,
      dateRange.value[0],
      dateRange.value[1]
    )
    teamStats.value = data
  } catch (error) {
    ElMessage.error('加载团队统计数据失败')
    console.error('加载团队统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadGlobalStatistics = async () => {
  if (!dateRange.value) return
  
  loading.value = true
  try {
    const { data } = await statisticsApi.getGlobalStatistics(
      dateRange.value[0],
      dateRange.value[1]
    )
    globalStats.value = data
  } catch (error) {
    ElMessage.error('加载全局统计数据失败')
    console.error('加载全局统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const exportReport = async () => {
  if (!dateRange.value) {
    ElMessage.warning('请选择日期范围')
    return
  }

  const exportLoading = ElMessage({
    message: '正在导出报表，请稍候...',
    type: 'info',
    duration: 0
  })

  try {
    let response
    const [startDate, endDate] = dateRange.value

    switch (viewType.value) {
      case 'personal':
        if (userStore.user?.id) {
          response = await statisticsApi.exportPersonalStatistics(
            userStore.user.id,
            startDate,
            endDate,
            'excel'
          )
        } else {
          throw new Error('用户信息不完整')
        }
        break
      case 'team':
        if (userStore.user?.teamId) {
          response = await statisticsApi.exportTeamStatistics(
            userStore.user.teamId,
            startDate,
            endDate,
            'excel'
          )
        } else {
          throw new Error('团队信息不完整')
        }
        break
      case 'global':
        response = await statisticsApi.exportGlobalStatistics(
          startDate,
          endDate,
          'excel'
        )
        break
    }

    exportLoading.close()

    if (response?.data) {
      // 创建下载链接
      const blob = new Blob([response.data], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${viewType.value}_statistics_${startDate}_${endDate}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('报表导出成功')
    } else {
      throw new Error('导出数据为空')
    }
  } catch (error) {
    exportLoading.close()
    ElMessage.error(`导出报表失败: ${error.message || '未知错误'}`)
    console.error('导出报表失败:', error)
  }
}

// 生命周期
onMounted(async () => {
  await loadOverview()
  await loadCurrentViewData()
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-controls {
  display: flex;
  align-items: center;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 100px;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  margin-right: 16px;
}

.card-info {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.statistics-content {
  margin-top: 20px;
}

:deep(.el-card__body) {
  padding: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-page {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-controls {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }
  
  .header-controls .el-date-picker,
  .header-controls .el-select,
  .header-controls .el-button {
    width: 100%;
  }
  
  .overview-cards .el-col {
    margin-bottom: 12px;
  }
  
  .card-value {
    font-size: 20px;
  }
}
</style>