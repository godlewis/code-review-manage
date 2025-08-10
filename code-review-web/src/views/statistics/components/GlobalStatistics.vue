<template>
  <div class="global-statistics">
    <el-row :gutter="20">
      <!-- 左侧全局概览 -->
      <el-col :span="16">
        <el-card class="global-overview">
          <template #header>
            <div class="card-header">
              <span>全局统计概览</span>
              <el-button type="text" @click="$emit('refresh')" :loading="loading">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="4" animated />
          </div>
          
          <div v-else-if="data" class="overview-grid">
            <div class="overview-item">
              <div class="overview-value">{{ data.totalTeams }}</div>
              <div class="overview-label">总团队数</div>
              <div class="overview-desc">活跃团队</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ data.totalUsers }}</div>
              <div class="overview-label">总用户数</div>
              <div class="overview-desc">{{ data.activeUsers }} 活跃用户</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ data.totalReviews }}</div>
              <div class="overview-label">总评审数</div>
              <div class="overview-desc">累计评审</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ data.totalIssues }}</div>
              <div class="overview-label">总问题数</div>
              <div class="overview-desc">累计发现</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ formatPercentage(data.globalResolutionRate) }}</div>
              <div class="overview-label">全局解决率</div>
              <div class="overview-desc">问题解决</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ data.globalAverageScore?.toFixed(1) || '0.0' }}</div>
              <div class="overview-label">全局平均分</div>
              <div class="overview-desc">满分10分</div>
            </div>
          </div>
        </el-card>

        <!-- 系统使用趋势 -->
        <el-card class="usage-trend" style="margin-top: 20px;">
          <template #header>
            <span>系统使用趋势</span>
          </template>
          <div ref="usageChartRef" style="height: 300px;"></div>
        </el-card>

        <!-- 质量改进趋势 -->
        <el-card class="quality-improvement" style="margin-top: 20px;">
          <template #header>
            <span>质量改进趋势</span>
          </template>
          <div ref="qualityChartRef" style="height: 300px;"></div>
        </el-card>

        <!-- 团队表现排名 -->
        <el-card class="team-rankings" style="margin-top: 20px;">
          <template #header>
            <span>团队表现排名</span>
          </template>
          <el-table :data="data?.teamRankings || []" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="60" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.rank <= 3" :type="getRankTagType(scope.row.rank)">
                  {{ scope.row.rank }}
                </el-tag>
                <span v-else>{{ scope.row.rank }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="teamName" label="团队名称" width="150" />
            <el-table-column prop="coverageRate" label="覆盖率" width="80">
              <template #default="scope">
                {{ formatPercentage(scope.row.coverageRate) }}
              </template>
            </el-table-column>
            <el-table-column prop="averageScore" label="平均分数" width="80">
              <template #default="scope">
                {{ scope.row.averageScore?.toFixed(1) || '0.0' }}
              </template>
            </el-table-column>
            <el-table-column prop="resolutionRate" label="解决率" width="80">
              <template #default="scope">
                {{ formatPercentage(scope.row.resolutionRate) }}
              </template>
            </el-table-column>
            <el-table-column prop="memberCount" label="成员数" width="80" />
            <el-table-column prop="improvementTrend" label="改进趋势" width="100">
              <template #default="scope">
                <el-tag :type="getTrendTagType(scope.row.improvementTrend)">
                  {{ scope.row.improvementTrend }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="overallScore" label="综合评分">
              <template #default="scope">
                <el-progress 
                  :percentage="(scope.row.overallScore * 100)" 
                  :stroke-width="8"
                  :show-text="false"
                />
                <span style="margin-left: 8px;">{{ (scope.row.overallScore * 100).toFixed(1) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧分析图表 -->
      <el-col :span="8">
        <el-card class="cross-team-analysis">
          <template #header>
            <span>跨团队问题分析</span>
          </template>
          
          <!-- 跨团队问题分布 -->
          <div class="chart-section">
            <h4>问题类型分布</h4>
            <div ref="crossTeamChartRef" style="height: 250px;"></div>
          </div>
        </el-card>

        <!-- 最佳实践团队 -->
        <el-card class="best-practices" style="margin-top: 20px;">
          <template #header>
            <span>最佳实践团队</span>
          </template>
          <div class="practice-list">
            <div 
              v-for="(practice, index) in data?.bestPracticeTeams || []" 
              :key="index"
              class="practice-item"
            >
              <div class="practice-header">
                <div class="practice-team">{{ practice.teamName }}</div>
                <div class="practice-category">{{ practice.category }}</div>
              </div>
              <div class="practice-content">
                <div class="practice-desc">{{ practice.practiceDescription }}</div>
                <div class="practice-metric">
                  <span class="metric-label">{{ practice.keyMetric }}:</span>
                  <span class="metric-value">{{ practice.metricValue }}</span>
                </div>
                <div class="practice-reason">{{ practice.recommendationReason }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 系统健康度 -->
        <el-card class="system-health" style="margin-top: 20px;">
          <template #header>
            <span>系统健康度</span>
          </template>
          <div class="health-metrics">
            <div class="health-item">
              <div class="health-label">用户活跃度</div>
              <el-progress 
                :percentage="getUserActivityPercentage()" 
                :stroke-width="12"
                :show-text="true"
              />
            </div>
            <div class="health-item">
              <div class="health-label">问题解决效率</div>
              <el-progress 
                :percentage="(data?.globalResolutionRate || 0) * 100" 
                :stroke-width="12"
                :show-text="true"
                :color="getHealthColor((data?.globalResolutionRate || 0) * 100)"
              />
            </div>
            <div class="health-item">
              <div class="health-label">代码质量水平</div>
              <el-progress 
                :percentage="(data?.globalAverageScore || 0) * 10" 
                :stroke-width="12"
                :show-text="true"
                :color="getHealthColor((data?.globalAverageScore || 0) * 10)"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { GlobalStatistics } from '@/api/statistics'

interface Props {
  data: GlobalStatistics | null
  loading: boolean
  dateRange: [string, string] | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  refresh: []
}>()

const usageChartRef = ref<HTMLDivElement>()
const qualityChartRef = ref<HTMLDivElement>()
const crossTeamChartRef = ref<HTMLDivElement>()

let usageChart: echarts.ECharts | null = null
let qualityChart: echarts.ECharts | null = null
let crossTeamChart: echarts.ECharts | null = null

const formatPercentage = (value: number | undefined): string => {
  if (value === undefined || value === null) return '0%'
  return `${(value * 100).toFixed(1)}%`
}

const getRankTagType = (rank: number): string => {
  switch (rank) {
    case 1: return 'danger'
    case 2: return 'warning'
    case 3: return 'success'
    default: return 'info'
  }
}

const getTrendTagType = (trend: string): string => {
  switch (trend) {
    case '上升': return 'success'
    case '下降': return 'danger'
    case '稳定': return 'info'
    default: return 'info'
  }
}

const getUserActivityPercentage = (): number => {
  if (!props.data) return 0
  return (props.data.activeUsers / props.data.totalUsers) * 100
}

const getHealthColor = (percentage: number): string => {
  if (percentage >= 80) return '#67C23A'
  if (percentage >= 60) return '#E6A23C'
  return '#F56C6C'
}

const initUsageChart = () => {
  if (!usageChartRef.value || !props.data?.usageTrends) return

  usageChart = echarts.init(usageChartRef.value)
  
  const dates = props.data.usageTrends.map(item => item.date)
  const activeUsers = props.data.usageTrends.map(item => item.activeUsers)
  const reviewCounts = props.data.usageTrends.map(item => item.reviewCount)
  const usageRates = props.data.usageTrends.map(item => (item.usageRate * 100).toFixed(1))

  const option = {
    title: {
      text: '系统使用趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['活跃用户', '评审数量', '使用率(%)'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 45 }
    },
    yAxis: [
      {
        type: 'value',
        name: '数量',
        position: 'left'
      },
      {
        type: 'value',
        name: '使用率(%)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '活跃用户',
        type: 'line',
        yAxisIndex: 0,
        data: activeUsers,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '评审数量',
        type: 'bar',
        yAxisIndex: 0,
        data: reviewCounts,
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '使用率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: usageRates,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }

  usageChart.setOption(option)
}

const initQualityChart = () => {
  if (!qualityChartRef.value || !props.data?.qualityTrends) return

  qualityChart = echarts.init(qualityChartRef.value)
  
  const dates = props.data.qualityTrends.map(item => item.date)
  const averageScores = props.data.qualityTrends.map(item => item.globalAverageScore)
  const resolutionRates = props.data.qualityTrends.map(item => (item.resolutionRate * 100).toFixed(1))
  const improvementIndexes = props.data.qualityTrends.map(item => (item.qualityImprovementIndex * 100).toFixed(1))

  const option = {
    title: {
      text: '质量改进趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['平均分数', '解决率(%)', '改进指数(%)'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { rotate: 45 }
    },
    yAxis: [
      {
        type: 'value',
        name: '分数',
        position: 'left'
      },
      {
        type: 'value',
        name: '百分比(%)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '平均分数',
        type: 'line',
        yAxisIndex: 0,
        data: averageScores,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '解决率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: resolutionRates,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '改进指数(%)',
        type: 'line',
        yAxisIndex: 1,
        data: improvementIndexes,
        smooth: true,
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }

  qualityChart.setOption(option)
}

const initCrossTeamChart = () => {
  if (!crossTeamChartRef.value || !props.data?.crossTeamIssueDistribution) return

  crossTeamChart = echarts.init(crossTeamChartRef.value)
  
  const data = Object.entries(props.data.crossTeamIssueDistribution).map(([key, value]) => ({
    name: key,
    value
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '问题类型',
        type: 'pie',
        radius: '70%',
        data,
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

  crossTeamChart.setOption(option)
}

const initCharts = async () => {
  await nextTick()
  initUsageChart()
  initQualityChart()
  initCrossTeamChart()
}

const resizeCharts = () => {
  usageChart?.resize()
  qualityChart?.resize()
  crossTeamChart?.resize()
}

watch(() => props.data, () => {
  if (props.data) {
    initCharts()
  }
}, { deep: true })

onMounted(() => {
  window.addEventListener('resize', resizeCharts)
})
</script>

<style scoped>
.global-statistics {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.overview-item {
  text-align: center;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background: #FAFAFA;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.overview-desc {
  font-size: 12px;
  color: #909399;
}

.chart-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.practice-list {
  max-height: 400px;
  overflow-y: auto;
}

.practice-item {
  padding: 16px 0;
  border-bottom: 1px solid #F0F0F0;
}

.practice-item:last-child {
  border-bottom: none;
}

.practice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.practice-team {
  font-weight: bold;
  color: #303133;
}

.practice-category {
  background: #F0F9FF;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.practice-content {
  font-size: 12px;
  line-height: 1.4;
}

.practice-desc {
  color: #606266;
  margin-bottom: 4px;
}

.practice-metric {
  margin-bottom: 4px;
}

.metric-label {
  color: #909399;
}

.metric-value {
  font-weight: bold;
  color: #409EFF;
  margin-left: 4px;
}

.practice-reason {
  color: #909399;
  font-style: italic;
}

.health-metrics {
  padding: 10px 0;
}

.health-item {
  margin-bottom: 20px;
}

.health-item:last-child {
  margin-bottom: 0;
}

.health-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #EBEEF5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>