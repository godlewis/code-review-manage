<template>
  <div class="analysis-charts">
    <el-row :gutter="20">
      <!-- Issue Type Distribution -->
      <el-col :span="12">
        <div class="chart-container">
          <h4>Issue Type Distribution</h4>
          <div ref="typeChartRef" class="chart"></div>
        </div>
      </el-col>

      <!-- Severity Distribution -->
      <el-col :span="12">
        <div class="chart-container">
          <h4>Severity Distribution</h4>
          <div ref="severityChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- Trend Analysis -->
      <el-col :span="24">
        <div class="chart-container">
          <h4>Issue Trend Analysis</h4>
          <div ref="trendChartRef" class="chart trend-chart"></div>
        </div>
      </el-col>
    </el-row>

    <!-- Patterns and Clusters -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <div class="patterns-section">
          <h4>Identified Patterns</h4>
          <div v-if="analysisResult.patterns && analysisResult.patterns.length > 0">
            <div
              v-for="pattern in analysisResult.patterns"
              :key="pattern.patternId"
              class="pattern-item"
            >
              <div class="pattern-header">
                <span class="pattern-name">{{ pattern.patternName }}</span>
                <el-tag size="small">{{ pattern.frequency }} occurrences</el-tag>
              </div>
              <div class="pattern-description">{{ pattern.description }}</div>
              <div class="pattern-confidence">
                Confidence: {{ formatPercentage(pattern.confidence * 100) }}
              </div>
            </div>
          </div>
          <el-empty v-else description="No patterns identified" />
        </div>
      </el-col>

      <el-col :span="12">
        <div class="clusters-section">
          <h4>Issue Clusters</h4>
          <div v-if="analysisResult.clusters && analysisResult.clusters.length > 0">
            <div
              v-for="cluster in analysisResult.clusters"
              :key="cluster.clusterId"
              class="cluster-item"
            >
              <div class="cluster-header">
                <span class="cluster-name">{{ cluster.clusterName }}</span>
                <el-tag size="small">{{ cluster.issueCount }} issues</el-tag>
              </div>
              <div class="cluster-description">{{ cluster.centerDescription }}</div>
              <div class="cluster-similarity">
                Similarity: {{ formatPercentage(cluster.similarity * 100) }}
              </div>
            </div>
          </div>
          <el-empty v-else description="No clusters found" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// Props
const props = defineProps({
  analysisResult: {
    type: Object,
    required: true
  }
})

// Chart refs
const typeChartRef = ref()
const severityChartRef = ref()
const trendChartRef = ref()

// Chart instances
let typeChart = null
let severityChart = null
let trendChart = null

// Methods
const initTypeChart = () => {
  if (!typeChartRef.value) return
  
  typeChart = echarts.init(typeChartRef.value)
  
  const data = Object.entries(props.analysisResult.typeDistribution || {}).map(([type, count]) => ({
    name: getTypeLabel(type),
    value: count
  }))
  
  const option = {
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
        name: 'Issue Types',
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
  
  typeChart.setOption(option)
}

const initSeverityChart = () => {
  if (!severityChartRef.value) return
  
  severityChart = echarts.init(severityChartRef.value)
  
  const data = Object.entries(props.analysisResult.severityDistribution || {}).map(([severity, count]) => ({
    name: getSeverityLabel(severity),
    value: count
  }))
  
  const option = {
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
        name: 'Severity Levels',
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
  
  severityChart.setOption(option)
}

const initTrendChart = () => {
  if (!trendChartRef.value || !props.analysisResult.trendAnalysis) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  const issueTrend = props.analysisResult.trendAnalysis.issueTrend || []
  const resolutionTrend = props.analysisResult.trendAnalysis.resolutionTrend || []
  
  const dates = issueTrend.map(point => point.date)
  const issueValues = issueTrend.map(point => point.value)
  const resolutionValues = resolutionTrend.map(point => point.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['Issues', 'Resolution Rate']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: 'Issues',
        position: 'left'
      },
      {
        type: 'value',
        name: 'Resolution Rate (%)',
        position: 'right'
      }
    ],
    series: [
      {
        name: 'Issues',
        type: 'line',
        data: issueValues,
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: 'Resolution Rate',
        type: 'line',
        yAxisIndex: 1,
        data: resolutionValues,
        smooth: true,
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const resizeCharts = () => {
  typeChart?.resize()
  severityChart?.resize()
  trendChart?.resize()
}

// Utility functions
const getTypeLabel = (type) => {
  const labels = {
    'FUNCTIONAL_DEFECT': 'Functional Defect',
    'PERFORMANCE_ISSUE': 'Performance Issue',
    'SECURITY_VULNERABILITY': 'Security Vulnerability',
    'CODE_STANDARD': 'Code Standard',
    'DESIGN_ISSUE': 'Design Issue'
  }
  return labels[type] || type
}

const getSeverityLabel = (severity) => {
  const labels = {
    'CRITICAL': 'Critical',
    'MAJOR': 'Major',
    'MINOR': 'Minor',
    'SUGGESTION': 'Suggestion'
  }
  return labels[severity] || severity
}

const formatPercentage = (value) => {
  if (value == null) return 'N/A'
  return `${value.toFixed(1)}%`
}

// Lifecycle
onMounted(() => {
  nextTick(() => {
    initTypeChart()
    initSeverityChart()
    initTrendChart()
    
    // Handle window resize
    window.addEventListener('resize', resizeCharts)
  })
})

// Cleanup
onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  typeChart?.dispose()
  severityChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.analysis-charts {
  padding: 20px 0;
}

.chart-container {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  height: 100%;
}

.chart-container h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart {
  width: 100%;
  height: 300px;
}

.trend-chart {
  height: 400px;
}

.patterns-section,
.clusters-section {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  height: 100%;
}

.patterns-section h4,
.clusters-section h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.pattern-item,
.cluster-item {
  padding: 16px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.pattern-header,
.cluster-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.pattern-name,
.cluster-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.pattern-description,
.cluster-description {
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.pattern-confidence,
.cluster-similarity {
  color: #909399;
  font-size: 12px;
}
</style>