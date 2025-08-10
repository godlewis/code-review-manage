<template>
  <div class="team-statistics">
    <el-row :gutter="20">
      <!-- 左侧团队概览 -->
      <el-col :span="16">
        <el-card class="team-overview">
          <template #header>
            <div class="card-header">
              <span>团队表现概览</span>
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
              <div class="overview-value">{{ formatPercentage(data.coverageRate) }}</div>
              <div class="overview-label">评审覆盖率</div>
              <div class="overview-desc">{{ data.activeMemberCount }}/{{ data.memberCount }} 成员参与</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ data.averageScore?.toFixed(1) || '0.0' }}</div>
              <div class="overview-label">平均质量分数</div>
              <div class="overview-desc">满分10分</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ formatPercentage(data.resolutionRate) }}</div>
              <div class="overview-label">问题解决率</div>
              <div class="overview-desc">{{ data.resolvedIssues }}/{{ data.totalIssues }}</div>
            </div>
            <div class="overview-item">
              <div class="overview-value">{{ data.totalIssues }}</div>
              <div class="overview-label">总问题数</div>
              <div class="overview-desc">累计发现</div>
            </div>
          </div>
        </el-card>

        <!-- 质量趋势图表 -->
        <el-card class="quality-trend" style="margin-top: 20px;">
          <template #header>
            <span>团队质量趋势</span>
          </template>
          <div ref="qualityChartRef" style="height: 300px;"></div>
        </el-card>

        <!-- 成员表现排名 -->
        <el-card class="member-rankings" style="margin-top: 20px;">
          <template #header>
            <span>成员表现排名</span>
          </template>
          <el-table :data="data?.memberRankings || []" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="60" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.rank <= 3" :type="getRankTagType(scope.row.rank)">
                  {{ scope.row.rank }}
                </el-tag>
                <span v-else>{{ scope.row.rank }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="completionRate" label="完成率" width="80">
              <template #default="scope">
                {{ formatPercentage(scope.row.completionRate) }}
              </template>
            </el-table-column>
            <el-table-column prop="issuesFound" label="发现问题" width="80" />
            <el-table-column prop="averageScore" label="平均分数" width="80">
              <template #default="scope">
                {{ scope.row.averageScore?.toFixed(1) || '0.0' }}
              </template>
            </el-table-column>
            <el-table-column prop="fixTimeliness" label="整改及时率" width="100">
              <template #default="scope">
                {{ formatPercentage(scope.row.fixTimeliness) }}
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
        <el-card class="issue-distribution">
          <template #header>
            <span>问题分布分析</span>
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

        <!-- 高频问题 -->
        <el-card class="frequent-issues" style="margin-top: 20px;">
          <template #header>
            <span>高频问题</span>
          </template>
          <div class="frequent-list">
            <div 
              v-for="(issue, index) in data?.frequentIssues?.slice(0, 5) || []" 
              :key="index"
              class="frequent-item"
            >
              <div class="frequent-rank">{{ index + 1 }}</div>
              <div class="frequent-content">
                <div class="frequent-desc">{{ issue.issueDescription }}</div>
                <div class="frequent-meta">
                  <span class="frequent-type">{{ issue.issueType }}</span>
                  <span class="frequent-count">{{ issue.occurrenceCount }}次</span>
                  <span class="frequent-percent">{{ formatPercentage(issue.percentage / 100) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 月度对比 -->
        <el-card class="monthly-comparison" style="margin-top: 20px;">
          <template #header>
            <span>月度对比</span>
          </template>
          <div class="comparison-list">
            <div 
              v-for="comparison in data?.monthlyComparisons?.slice(-3) || []" 
              :key="comparison.yearMonth"
              class="comparison-item"
            >
              <div class="comparison-month">{{ comparison.yearMonth }}</div>
              <div class="comparison-metrics">
                <div class="comparison-metric">
                  <span>评审数量:</span>
                  <span :class="getChangeClass(comparison.reviewCountChangeRate)">
                    {{ comparison.currentMonth.reviewCount }}
                    <el-icon v-if="comparison.reviewCountChangeRate > 0"><ArrowUp /></el-icon>
                    <el-icon v-else-if="comparison.reviewCountChangeRate < 0"><ArrowDown /></el-icon>
                  </span>
                </div>
                <div class="comparison-metric">
                  <span>问题数量:</span>
                  <span :class="getChangeClass(comparison.issueCountChangeRate)">
                    {{ comparison.currentMonth.issueCount }}
                    <el-icon v-if="comparison.issueCountChangeRate > 0"><ArrowUp /></el-icon>
                    <el-icon v-else-if="comparison.issueCountChangeRate < 0"><ArrowDown /></el-icon>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted } from 'vue'
import { Refresh, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { TeamStatistics } from '@/api/statistics'

interface Props {
  data: TeamStatistics | null
  loading: boolean
  dateRange: [string, string] | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  refresh: []
}>()

const qualityChartRef = ref<HTMLDivElement>()
const issueTypeChartRef = ref<HTMLDivElement>()
const severityChartRef = ref<HTMLDivElement>()

let qualityChart: echarts.ECharts | null = null
let issueTypeChart: echarts.ECharts | null = null
let severityChart: echarts.ECharts | null = null

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

const getChangeClass = (changeRate: number): string => {
  if (changeRate > 0) return 'change-up'
  if (changeRate < 0) return 'change-down'
  return 'change-stable'
}

const initQualityChart = () => {
  if (!qualityChartRef.value || !props.data?.qualityTrend) return

  qualityChart = echarts.init(qualityChartRef.value)
  
  const dates = props.data.qualityTrend.map(item => item.date)
  const qualityScores = props.data.qualityTrend.map(item => item.averageQualityScore)
  const issueDensity = props.data.qualityTrend.map(item => item.issueDensity)
  const resolutionRates = props.data.qualityTrend.map(item => (item.resolutionRate * 100).toFixed(1))

  const option = {
    title: {
      text: '团队质量趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['质量分数', '问题密度', '解决率(%)'],
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
        name: '分数/密度',
        position: 'left'
      },
      {
        type: 'value',
        name: '解决率(%)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '质量分数',
        type: 'line',
        yAxisIndex: 0,
        data: qualityScores,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '问题密度',
        type: 'line',
        yAxisIndex: 0,
        data: issueDensity,
        smooth: true,
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '解决率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: resolutionRates,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }

  qualityChart.setOption(option)
}

const initIssueTypeChart = () => {
  if (!issueTypeChartRef.value || !props.data?.issueDistribution) return

  issueTypeChart = echarts.init(issueTypeChartRef.value)
  
  const data = Object.entries(props.data.issueDistribution).map(([key, value]) => ({
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
  initQualityChart()
  initIssueTypeChart()
  initSeverityChart()
}

const resizeCharts = () => {
  qualityChart?.resize()
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
  
  if (qualityChartRef.value) resizeObserver.observe(qualityChartRef.value)
  if (issueTypeChartRef.value) resizeObserver.observe(issueTypeChartRef.value)
  if (severityChartRef.value) resizeObserver.observe(severityChartRef.value)
})
</script>

<style scoped>
.team-statistics {
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
  grid-template-columns: repeat(2, 1fr);
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
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 16px;
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

.frequent-list {
  max-height: 300px;
  overflow-y: auto;
}

.frequent-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 0;
  border-bottom: 1px solid #F0F0F0;
}

.frequent-item:last-child {
  border-bottom: none;
}

.frequent-rank {
  width: 24px;
  height: 24px;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  margin-right: 12px;
  flex-shrink: 0;
}

.frequent-content {
  flex: 1;
}

.frequent-desc {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.frequent-meta {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.frequent-type {
  background: #F0F9FF;
  color: #409EFF;
  padding: 2px 6px;
  border-radius: 2px;
}

.comparison-list {
  max-height: 200px;
  overflow-y: auto;
}

.comparison-item {
  padding: 12px 0;
  border-bottom: 1px solid #F0F0F0;
}

.comparison-item:last-child {
  border-bottom: none;
}

.comparison-month {
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.comparison-metric {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
}

.change-up {
  color: #F56C6C;
}

.change-down {
  color: #67C23A;
}

.change-stable {
  color: #909399;
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
  .team-statistics .el-row {
    flex-direction: column;
  }
  
  .team-statistics .el-col {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .overview-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .overview-value {
    font-size: 24px;
  }
  
  .overview-item {
    padding: 16px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
  
  :deep(.el-table .el-table__cell) {
    padding: 8px 4px;
  }
  
  .frequent-item {
    padding: 8px 0;
  }
  
  .frequent-rank {
    width: 20px;
    height: 20px;
    font-size: 10px;
  }
  
  .comparison-item {
    padding: 8px 0;
  }
  
  :deep(.el-card__body) {
    padding: 12px;
  }
}
</style>