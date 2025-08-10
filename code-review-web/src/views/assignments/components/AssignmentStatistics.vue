<template>
  <div class="assignment-statistics">
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
          <el-button type="primary" @click="loadStatistics">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="总分配数"
            :value="statistics.totalAssignments || 0"
            class="stat-item"
          >
            <template #suffix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="已完成"
            :value="statistics.completedAssignments || 0"
            class="stat-item"
          >
            <template #suffix>
              <el-icon><Check /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="完成率"
            :value="statistics.completionRate || 0"
            suffix="%"
            :precision="1"
            class="stat-item"
          >
            <template #suffix>
              <span>%</span>
              <el-icon><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic
            title="平均分数"
            :value="statistics.averageTotalScore || 0"
            :precision="2"
            class="stat-item"
          >
            <template #suffix>
              <el-icon><Star /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="分配趋势分析">
          <div ref="trendChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="状态分布">
          <div ref="statusChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card title="评分分布">
          <div ref="scoreChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="用户参与度">
          <div ref="participationChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计表格 -->
    <el-row :gutter="20" class="tables-row">
      <el-col :span="12">
        <el-card title="团队统计">
          <el-table :data="teamStatistics" size="small">
            <el-table-column prop="teamName" label="团队" />
            <el-table-column prop="totalAssignments" label="总分配" width="80" />
            <el-table-column prop="completedAssignments" label="已完成" width="80" />
            <el-table-column prop="completionRate" label="完成率" width="80">
              <template #default="{ row }">
                {{ row.completionRate?.toFixed(1) }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgScore" label="平均分" width="80">
              <template #default="{ row }">
                {{ row.avgScore?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card title="用户统计">
          <el-table :data="userStatistics" size="small" max-height="300">
            <el-table-column prop="userName" label="用户" />
            <el-table-column prop="reviewerCount" label="评审次数" width="80" />
            <el-table-column prop="revieweeCount" label="被评审次数" width="100" />
            <el-table-column prop="avgScore" label="平均得分" width="80">
              <template #default="{ row }">
                {{ row.avgScore?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="completionRate" label="完成率" width="80">
              <template #default="{ row }">
                {{ row.completionRate?.toFixed(1) }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 算法效果分析 -->
    <el-card title="算法效果分析" class="algorithm-analysis">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic
            title="平均技能匹配分数"
            :value="statistics.avgSkillMatchScore || 0"
            :precision="3"
          />
        </el-col>
        <el-col :span="8">
          <el-statistic
            title="平均负载均衡分数"
            :value="statistics.avgLoadBalanceScore || 0"
            :precision="3"
          />
        </el-col>
        <el-col :span="8">
          <el-statistic
            title="平均多样性分数"
            :value="statistics.avgDiversityScore || 0"
            :precision="3"
          />
        </el-col>
      </el-row>
      
      <el-divider />
      
      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic
            title="手动调整率"
            :value="statistics.manualAdjustmentRate || 0"
            suffix="%"
            :precision="1"
          />
        </el-col>
        <el-col :span="8">
          <el-statistic
            title="避重成功率"
            :value="statistics.avoidanceSuccessRate || 0"
            suffix="%"
            :precision="1"
          />
        </el-col>
        <el-col :span="8">
          <el-statistic
            title="负载均衡度"
            :value="statistics.loadBalanceIndex || 0"
            :precision="3"
          />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Check, TrendCharts, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { assignmentApi } from '@/api/assignment'
import { teamApi } from '@/api/team'

// 响应式数据
const loading = ref(false)
const teams = ref([])
const statistics = ref({})
const teamStatistics = ref([])
const userStatistics = ref([])

// 图表引用
const trendChartRef = ref()
const statusChartRef = ref()
const scoreChartRef = ref()
const participationChartRef = ref()

let trendChart = null
let statusChart = null
let scoreChart = null
let participationChart = null

// 筛选表单
const filterForm = reactive({
  teamId: null,
  dateRange: [
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    new Date().toISOString().split('T')[0]
  ]
})

// 生命周期
onMounted(() => {
  loadTeams()
  loadStatistics()
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

const loadStatistics = async () => {
  loading.value = true
  try {
    const params = {
      teamId: filterForm.teamId,
      startDate: filterForm.dateRange?.[0],
      endDate: filterForm.dateRange?.[1]
    }
    
    const response = await assignmentApi.getAssignmentStatistics(params.teamId, params.startDate, params.endDate)
    statistics.value = response.data || {}
    teamStatistics.value = response.data?.teamStatistics || []
    userStatistics.value = response.data?.userStatistics || []
    
    // 更新图表
    updateCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  Object.assign(filterForm, {
    teamId: null,
    dateRange: [
      new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      new Date().toISOString().split('T')[0]
    ]
  })
  loadStatistics()
}

const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (statusChartRef.value) {
    statusChart = echarts.init(statusChartRef.value)
  }
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value)
  }
  if (participationChartRef.value) {
    participationChart = echarts.init(participationChartRef.value)
  }
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    trendChart?.resize()
    statusChart?.resize()
    scoreChart?.resize()
    participationChart?.resize()
  })
}

const updateCharts = () => {
  updateTrendChart()
  updateStatusChart()
  updateScoreChart()
  updateParticipationChart()
}

const updateTrendChart = () => {
  if (!trendChart || !statistics.value.trendData) return
  
  const trendData = statistics.value.trendData || []
  const dates = trendData.map(item => item.date)
  const totalData = trendData.map(item => item.total)
  const completedData = trendData.map(item => item.completed)
  const completionRates = trendData.map(item => item.completionRate)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['总分配', '已完成', '完成率']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '数量',
        position: 'left'
      },
      {
        type: 'value',
        name: '完成率(%)',
        position: 'right',
        axisLabel: {
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '总分配',
        type: 'bar',
        data: totalData,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '已完成',
        type: 'bar',
        data: completedData,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '完成率',
        type: 'line',
        yAxisIndex: 1,
        data: completionRates,
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const updateStatusChart = () => {
  if (!statusChart || !statistics.value.statusDistribution) return
  
  const statusData = statistics.value.statusDistribution || {}
  const data = Object.entries(statusData).map(([name, value]) => ({
    name: getStatusText(name),
    value
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
        name: '分配状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  statusChart.setOption(option)
}

const updateScoreChart = () => {
  if (!scoreChart || !statistics.value.scoreDistribution) return
  
  const scoreData = statistics.value.scoreDistribution || []
  const ranges = scoreData.map(item => item.range)
  const counts = scoreData.map(item => item.count)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: ranges,
      name: '分数区间'
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '分配数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }
  
  scoreChart.setOption(option)
}

const updateParticipationChart = () => {
  if (!participationChart || !userStatistics.value.length) return
  
  const users = userStatistics.value.slice(0, 10) // 只显示前10个用户
  const userNames = users.map(user => user.userName)
  const reviewerCounts = users.map(user => user.reviewerCount)
  const revieweeCounts = users.map(user => user.revieweeCount)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['作为评审者', '作为被评审者']
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: userNames
    },
    series: [
      {
        name: '作为评审者',
        type: 'bar',
        stack: 'total',
        data: reviewerCounts,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '作为被评审者',
        type: 'bar',
        stack: 'total',
        data: revieweeCounts,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  
  participationChart.setOption(option)
}

// 工具方法
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
</script>

<style scoped>
.assignment-statistics {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px 0;
}

.charts-row {
  margin-bottom: 20px;
}

.tables-row {
  margin-bottom: 20px;
}

.algorithm-analysis {
  margin-bottom: 20px;
}
</style>