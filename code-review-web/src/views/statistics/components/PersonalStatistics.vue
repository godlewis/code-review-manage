<template>
  <div class="personal-statistics">
    <el-row :gutter="20">
      <!-- 左侧主要指标 -->
      <el-col :span="16">
        <el-card class="main-metrics">
          <template #header>
            <div class="card-header">
              <span>个人表现指标</span>
              <el-button type="text" @click="$emit('refresh')" :loading="loading">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="4" animated />
          </div>
          
          <div v-else-if="data" class="metrics-grid">
            <div class="metric-item">
              <div class="metric-value">{{ formatPercentage(data.completionRate) }}</div>
              <div class="metric-label">评审完成率</div>
              <div class="metric-desc">{{ data.completedReviews }}/{{ data.totalReviews }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ data.issuesFound }}</div>
              <div class="metric-label">发现问题数</div>
              <div class="metric-desc">累计发现</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ formatPercentage(data.fixTimeliness) }}</div>
              <div class="metric-label">整改及时率</div>
              <div class="metric-desc">{{ data.resolvedIssues }}/{{ data.pendingIssues + data.resolvedIssues }}</div>
            </div>
            <div class="metric-item">
              <div class="metric-value">{{ data.averageReviewScore?.toFixed(1) || '0.0' }}</div>
              <div class="metric-label">平均评审分数</div>
              <div class="metric-desc">满分10分</div>
            </div>
          </div>
        </el-card>

        <!-- 成长趋势图表 -->
        <el-card class="trend-chart" style="margin-top: 20px;">
          <template #header>
            <span>个人成长趋势</span>
          </template>
          <div ref="growthChartRef" style="height: 300px;"></div>
        </el-card>

        <!-- 月度统计表格 -->
        <el-card class="monthly-stats" style="margin-top: 20px;">
          <template #header>
            <span>月度统计</span>
          </template>
          <el-table :data="data?.monthlyStats || []" style="width: 100%">
            <el-table-column prop="yearMonth" label="月份" width="120" />
            <el-table-column prop="reviewCount" label="评审数量" width="100" />
            <el-table-column prop="issueCount" label="问题数量" width="100" />
            <el-table-column prop="averageScore" label="平均分数" width="100">
              <template #default="scope">
                {{ scope.row.averageScore?.toFixed(1) || '0.0' }}
              </template>
            </el-table-column>
            <el-table-column prop="completionRate" label="完成率" width="100">
              <template #default="scope">
                {{ formatPercentage(scope.row.completionRate) }}
              </template>
            </el-table-column>
            <el-table-column prop="fixTimeliness" label="整改及时率">
              <template #default="scope">
                {{ formatPercentage(scope.row.fixTimeliness) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧分布图表 -->
      <el-col :span="8">
        <el-card class="distribution-charts">
          <template #header>
            <span>问题分布</span>
          </template>
          
          <!-- 问题类型分布 -->
          <div class="chart-section">
            <h4>问题类型分布</h4>
            <div ref="issueTypeChartRef" style="height: 200px;"></div>
          </div>

          <!-- 严重级别分布 -->
          <div class="chart-section" style="margin-top: 20px;">
            <h4>严重级别分布</h4>
            <div ref="severityChartRef" style="height: 200px;"></div>
          </div>
        </el-card>

        <!-- 待处理问题 -->
        <el-card class="pending-issues" style="margin-top: 20px;">
          <template #header>
            <span>待处理问题</span>
          </template>
          <div class="issue-summary">
            <div class="summary-item">
              <span class="summary-label">待处理:</span>
              <span class="summary-value">{{ data?.pendingIssues || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">已解决:</span>
              <span class="summary-value">{{ data?.resolvedIssues || 0 }}</span>
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
import type { PersonalStatistics } from '@/api/statistics'

interface Props {
  data: PersonalStatistics | null
  loading: boolean
  dateRange: [string, string] | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  refresh: []
}>()

const growthChartRef = ref<HTMLDivElement>()
const issueTypeChartRef = ref<HTMLDivElement>()
const severityChartRef = ref<HTMLDivElement>()

let growthChart: echarts.ECharts | null = null
let issueTypeChart: echarts.ECharts | null = null
let severityChart: echarts.ECharts | null = null

const formatPercentage = (value: number | undefined): string => {
  if (value === undefined || value === null) return '0%'
  return `${(value * 100).toFixed(1)}%`
}

const initGrowthChart = () => {
  if (!growthChartRef.value || !props.data?.growthTrend) return

  growthChart = echarts.init(growthChartRef.value)
  
  const dates = props.data.growthTrend.map(item => item.date)
  const completionRates = props.data.growthTrend.map(item => (item.completionRate * 100).toFixed(1))
  const issuesFound = props.data.growthTrend.map(item => item.issuesFound)
  const averageScores = props.data.growthTrend.map(item => item.averageScore)

  const option = {
    title: {
      text: '成长趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['完成率(%)', '发现问题数', '平均分数'],
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
        name: '完成率(%)',
        position: 'left'
      },
      {
        type: 'value',
        name: '数量/分数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '完成率(%)',
        type: 'line',
        yAxisIndex: 0,
        data: completionRates,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '发现问题数',
        type: 'bar',
        yAxisIndex: 1,
        data: issuesFound,
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '平均分数',
        type: 'line',
        yAxisIndex: 1,
        data: averageScores,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }

  growthChart.setOption(option)
}

const initIssueTypeChart = () => {
  if (!issueTypeChartRef.value || !props.data?.issueTypeDistribution) return

  issueTypeChart = echarts.init(issueTypeChartRef.value)
  
  const data = Object.entries(props.data.issueTypeDistribution).map(([key, value]) => ({
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

  issueTypeChart.setOption(option)
}

const initSeverityChart = () => {
  if (!severityChartRef.value || !props.data?.severityDistribution) return

  severityChart = echarts.init(severityChartRef.value)
  
  const data = Object.entries(props.data.severityDistribution).map(([key, value]) => ({
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
        name: '严重级别',
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

  severityChart.setOption(option)
}

const initCharts = async () => {
  await nextTick()
  initGrowthChart()
  initIssueTypeChart()
  initSeverityChart()
}

const resizeCharts = () => {
  growthChart?.resize()
  issueTypeChart?.resize()
  severityChart?.resize()
}

watch(() => props.data, () => {
  if (props.data) {
    initCharts()
  }
}, { deep: true })

onMounted(() => {
  window.addEventListener('resize', resizeCharts)
  // 监听容器大小变化
  const resizeObserver = new ResizeObserver(() => {
    setTimeout(resizeCharts, 100)
  })
  
  if (growthChartRef.value) resizeObserver.observe(growthChartRef.value)
  if (issueTypeChartRef.value) resizeObserver.observe(issueTypeChartRef.value)
  if (severityChartRef.value) resizeObserver.observe(severityChartRef.value)
})
</script>

<style scoped>
.personal-statistics {
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

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.metric-item {
  text-align: center;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  background: #FAFAFA;
}

.metric-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.metric-label {
  font-size: 16px;
  color: #303133;
  margin-bottom: 4px;
}

.metric-desc {
  font-size: 12px;
  color: #909399;
}

.chart-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

.issue-summary {
  padding: 10px 0;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.summary-label {
  color: #606266;
}

.summary-value {
  font-weight: bold;
  color: #303133;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #EBEEF5;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .personal-statistics .el-row {
    flex-direction: column;
  }
  
  .personal-statistics .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .metric-value {
    font-size: 24px;
  }
  
  .metric-item {
    padding: 16px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-card__body) {
    padding: 12px;
  }
}
</style>