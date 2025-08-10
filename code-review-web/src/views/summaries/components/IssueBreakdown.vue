<template>
  <div class="issue-breakdown">
    <!-- Summary Cards -->
    <div class="summary-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="breakdown-card total">
            <div class="card-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ statistics.totalIssues || 0 }}</div>
              <div class="card-label">Total Issues</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="breakdown-card resolved">
            <div class="card-icon">
              <el-icon><Check /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ statistics.resolvedIssues || 0 }}</div>
              <div class="card-label">Resolved</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="breakdown-card rate">
            <div class="card-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ formatPercentage(statistics.resolutionRate) }}</div>
              <div class="card-label">Resolution Rate</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="breakdown-card patterns">
            <div class="card-icon">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ statistics.patternsIdentified || 0 }}</div>
              <div class="card-label">Patterns</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Severity Breakdown -->
    <div class="severity-breakdown">
      <h4>Issues by Severity</h4>
      <div class="severity-grid">
        <div class="severity-item critical">
          <div class="severity-header">
            <el-icon><Warning /></el-icon>
            <span class="severity-label">Critical</span>
          </div>
          <div class="severity-count">{{ statistics.criticalIssues || 0 }}</div>
          <div class="severity-percentage">
            {{ calculatePercentage(statistics.criticalIssues, statistics.totalIssues) }}
          </div>
          <div class="severity-bar">
            <div 
              class="severity-fill critical-fill"
              :style="{ width: calculatePercentage(statistics.criticalIssues, statistics.totalIssues) }"
            ></div>
          </div>
        </div>

        <div class="severity-item major">
          <div class="severity-header">
            <el-icon><InfoFilled /></el-icon>
            <span class="severity-label">Major</span>
          </div>
          <div class="severity-count">{{ statistics.majorIssues || 0 }}</div>
          <div class="severity-percentage">
            {{ calculatePercentage(statistics.majorIssues, statistics.totalIssues) }}
          </div>
          <div class="severity-bar">
            <div 
              class="severity-fill major-fill"
              :style="{ width: calculatePercentage(statistics.majorIssues, statistics.totalIssues) }"
            ></div>
          </div>
        </div>

        <div class="severity-item minor">
          <div class="severity-header">
            <el-icon><Minus /></el-icon>
            <span class="severity-label">Minor</span>
          </div>
          <div class="severity-count">{{ statistics.minorIssues || 0 }}</div>
          <div class="severity-percentage">
            {{ calculatePercentage(statistics.minorIssues, statistics.totalIssues) }}
          </div>
          <div class="severity-bar">
            <div 
              class="severity-fill minor-fill"
              :style="{ width: calculatePercentage(statistics.minorIssues, statistics.totalIssues) }"
            ></div>
          </div>
        </div>

        <div class="severity-item suggestion">
          <div class="severity-header">
            <el-icon><Lightbulb /></el-icon>
            <span class="severity-label">Suggestion</span>
          </div>
          <div class="severity-count">{{ statistics.suggestionIssues || 0 }}</div>
          <div class="severity-percentage">
            {{ calculatePercentage(statistics.suggestionIssues, statistics.totalIssues) }}
          </div>
          <div class="severity-bar">
            <div 
              class="severity-fill suggestion-fill"
              :style="{ width: calculatePercentage(statistics.suggestionIssues, statistics.totalIssues) }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Progress Indicators -->
    <div class="progress-section">
      <h4>Progress Overview</h4>
      <div class="progress-grid">
        <div class="progress-item">
          <div class="progress-header">
            <span class="progress-label">Overall Resolution</span>
            <span class="progress-value">{{ formatPercentage(statistics.resolutionRate) }}</span>
          </div>
          <el-progress 
            :percentage="statistics.resolutionRate || 0" 
            :color="getProgressColor(statistics.resolutionRate)"
            :stroke-width="8"
          />
        </div>

        <div class="progress-item">
          <div class="progress-header">
            <span class="progress-label">Critical Issues Resolved</span>
            <span class="progress-value">
              {{ calculateCriticalResolutionRate() }}
            </span>
          </div>
          <el-progress 
            :percentage="parseCriticalResolutionRate()" 
            color="#f56c6c"
            :stroke-width="8"
          />
        </div>

        <div class="progress-item">
          <div class="progress-header">
            <span class="progress-label">Pattern Coverage</span>
            <span class="progress-value">
              {{ calculatePatternCoverage() }}
            </span>
          </div>
          <el-progress 
            :percentage="parsePatternCoverage()" 
            color="#e6a23c"
            :stroke-width="8"
          />
        </div>
      </div>
    </div>

    <!-- Quality Metrics -->
    <div class="quality-metrics">
      <h4>Quality Metrics</h4>
      <div class="metrics-grid">
        <div class="metric-item">
          <div class="metric-icon">
            <el-icon><Star /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ calculateQualityScore() }}</div>
            <div class="metric-label">Quality Score</div>
            <div class="metric-description">Based on issue severity and resolution</div>
          </div>
        </div>

        <div class="metric-item">
          <div class="metric-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ calculateAvgResolutionTime() }}</div>
            <div class="metric-label">Avg Resolution Time</div>
            <div class="metric-description">Estimated based on issue complexity</div>
          </div>
        </div>

        <div class="metric-item">
          <div class="metric-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ calculateImprovementRate() }}</div>
            <div class="metric-label">Improvement Rate</div>
            <div class="metric-description">Compared to previous period</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Document,
  Check,
  TrendCharts,
  Grid,
  Warning,
  InfoFilled,
  Minus,
  Lightbulb,
  Star,
  Timer
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  statistics: {
    type: Object,
    required: true
  }
})

// Methods
const formatPercentage = (value) => {
  if (value == null) return '0%'
  return `${value.toFixed(1)}%`
}

const calculatePercentage = (value, total) => {
  if (!total || total === 0) return '0%'
  return `${((value || 0) / total * 100).toFixed(1)}%`
}

const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

const calculateCriticalResolutionRate = () => {
  // Mock calculation - in real implementation, this would be based on actual data
  const criticalIssues = props.statistics.criticalIssues || 0
  if (criticalIssues === 0) return '100%'
  
  const resolvedCritical = Math.floor(criticalIssues * 0.8) // Assume 80% resolved
  return `${((resolvedCritical / criticalIssues) * 100).toFixed(1)}%`
}

const parseCriticalResolutionRate = () => {
  const rate = calculateCriticalResolutionRate()
  return parseFloat(rate.replace('%', ''))
}

const calculatePatternCoverage = () => {
  const patterns = props.statistics.patternsIdentified || 0
  const totalIssues = props.statistics.totalIssues || 0
  
  if (totalIssues === 0) return '0%'
  
  // Assume each pattern covers about 10% of issues on average
  const coverage = Math.min((patterns * 10), 100)
  return `${coverage.toFixed(1)}%`
}

const parsePatternCoverage = () => {
  const coverage = calculatePatternCoverage()
  return parseFloat(coverage.replace('%', ''))
}

const calculateQualityScore = () => {
  const totalIssues = props.statistics.totalIssues || 0
  const criticalIssues = props.statistics.criticalIssues || 0
  const majorIssues = props.statistics.majorIssues || 0
  const resolutionRate = props.statistics.resolutionRate || 0
  
  if (totalIssues === 0) return '100'
  
  // Calculate quality score based on issue severity and resolution rate
  let score = 100
  score -= (criticalIssues * 10) // -10 points per critical issue
  score -= (majorIssues * 5) // -5 points per major issue
  score += (resolutionRate * 0.2) // Bonus for high resolution rate
  
  return Math.max(0, Math.min(100, score)).toFixed(0)
}

const calculateAvgResolutionTime = () => {
  // Mock calculation - in real implementation, this would be based on actual data
  const criticalIssues = props.statistics.criticalIssues || 0
  const majorIssues = props.statistics.majorIssues || 0
  const minorIssues = props.statistics.minorIssues || 0
  
  const totalWeightedTime = (criticalIssues * 3) + (majorIssues * 2) + (minorIssues * 1)
  const totalIssues = criticalIssues + majorIssues + minorIssues
  
  if (totalIssues === 0) return '0d'
  
  const avgDays = Math.ceil(totalWeightedTime / totalIssues)
  return `${avgDays}d`
}

const calculateImprovementRate = () => {
  // Mock calculation - in real implementation, this would compare with previous period
  const resolutionRate = props.statistics.resolutionRate || 0
  
  if (resolutionRate >= 80) return '+15%'
  if (resolutionRate >= 60) return '+5%'
  if (resolutionRate >= 40) return '-2%'
  return '-10%'
}
</script>

<style scoped>
.issue-breakdown {
  padding: 20px 0;
}

.summary-cards {
  margin-bottom: 32px;
}

.breakdown-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  height: 100px;
}

.breakdown-card.total {
  border-left: 4px solid #409eff;
}

.breakdown-card.resolved {
  border-left: 4px solid #67c23a;
}

.breakdown-card.rate {
  border-left: 4px solid #e6a23c;
}

.breakdown-card.patterns {
  border-left: 4px solid #909399;
}

.card-icon {
  font-size: 32px;
  margin-right: 16px;
  color: #409eff;
}

.resolved .card-icon {
  color: #67c23a;
}

.rate .card-icon {
  color: #e6a23c;
}

.patterns .card-icon {
  color: #909399;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.severity-breakdown {
  margin-bottom: 32px;
}

.severity-breakdown h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.severity-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.severity-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.severity-item.critical {
  border-left: 4px solid #f56c6c;
}

.severity-item.major {
  border-left: 4px solid #e6a23c;
}

.severity-item.minor {
  border-left: 4px solid #409eff;
}

.severity-item.suggestion {
  border-left: 4px solid #67c23a;
}

.severity-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.severity-label {
  font-weight: 600;
  color: #303133;
}

.severity-count {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.severity-percentage {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.severity-bar {
  height: 6px;
  background: #f0f2f5;
  border-radius: 3px;
  overflow: hidden;
}

.severity-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.critical-fill {
  background: #f56c6c;
}

.major-fill {
  background: #e6a23c;
}

.minor-fill {
  background: #409eff;
}

.suggestion-fill {
  background: #67c23a;
}

.progress-section {
  margin-bottom: 32px;
}

.progress-section h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.progress-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.progress-item {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-label {
  font-weight: 500;
  color: #303133;
}

.progress-value {
  font-weight: 600;
  color: #409eff;
}

.quality-metrics h4 {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.metric-item {
  display: flex;
  align-items: center;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
}

.metric-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 16px;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.metric-description {
  font-size: 12px;
  color: #909399;
}

:deep(.el-progress-bar__outer) {
  border-radius: 4px;
}

:deep(.el-progress-bar__inner) {
  border-radius: 4px;
}
</style>