<template>
  <div class="summary-comparison">
    <!-- Header -->
    <div class="comparison-header">
      <h3>Summary Comparison</h3>
      <div class="header-actions">
        <el-select
          v-model="selectedPreviousSummary"
          placeholder="Select summary to compare with"
          @change="loadComparison"
        >
          <el-option
            v-for="summary in availableSummaries"
            :key="summary.summaryId"
            :label="summary.title"
            :value="summary.summaryId"
          />
        </el-select>
        <el-button @click="$emit('close')">Close</el-button>
      </div>
    </div>

    <!-- Comparison Content -->
    <div v-if="comparisonData" class="comparison-content">
      <!-- Statistics Comparison -->
      <div class="stats-comparison">
        <h4>Statistics Comparison</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="comparison-card current">
              <div class="card-header">
                <h5>{{ comparisonData.currentSummary.title }}</h5>
                <el-tag type="primary">Current</el-tag>
              </div>
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="label">Total Issues:</span>
                  <span class="value">{{ comparisonData.currentSummary.statistics?.totalIssues || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">Resolution Rate:</span>
                  <span class="value">{{ formatPercentage(comparisonData.currentSummary.statistics?.resolutionRate) }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">Critical Issues:</span>
                  <span class="value">{{ comparisonData.currentSummary.statistics?.criticalIssues || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">Patterns Found:</span>
                  <span class="value">{{ comparisonData.currentSummary.statistics?.patternsIdentified || 0 }}</span>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="comparison-card previous">
              <div class="card-header">
                <h5>{{ comparisonData.previousSummary.title }}</h5>
                <el-tag type="info">Previous</el-tag>
              </div>
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="label">Total Issues:</span>
                  <span class="value">{{ comparisonData.previousSummary.statistics?.totalIssues || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">Resolution Rate:</span>
                  <span class="value">{{ formatPercentage(comparisonData.previousSummary.statistics?.resolutionRate) }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">Critical Issues:</span>
                  <span class="value">{{ comparisonData.previousSummary.statistics?.criticalIssues || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="label">Patterns Found:</span>
                  <span class="value">{{ comparisonData.previousSummary.statistics?.patternsIdentified || 0 }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Changes Overview -->
      <div class="changes-overview">
        <el-row :gutter="20">
          <!-- Improvements -->
          <el-col :span="8">
            <div class="change-section improvements">
              <div class="section-header">
                <el-icon class="section-icon"><TrendCharts /></el-icon>
                <h4>Improvements</h4>
              </div>
              <div class="change-list">
                <div
                  v-for="(improvement, index) in comparisonData.improvements"
                  :key="index"
                  class="change-item positive"
                >
                  <el-icon><Check /></el-icon>
                  <span>{{ improvement }}</span>
                </div>
                <div v-if="comparisonData.improvements.length === 0" class="no-changes">
                  No improvements identified
                </div>
              </div>
            </div>
          </el-col>

          <!-- Regressions -->
          <el-col :span="8">
            <div class="change-section regressions">
              <div class="section-header">
                <el-icon class="section-icon"><Warning /></el-icon>
                <h4>Regressions</h4>
              </div>
              <div class="change-list">
                <div
                  v-for="(regression, index) in comparisonData.regressions"
                  :key="index"
                  class="change-item negative"
                >
                  <el-icon><Close /></el-icon>
                  <span>{{ regression }}</span>
                </div>
                <div v-if="comparisonData.regressions.length === 0" class="no-changes">
                  No regressions identified
                </div>
              </div>
            </div>
          </el-col>

          <!-- New Patterns -->
          <el-col :span="8">
            <div class="change-section new-patterns">
              <div class="section-header">
                <el-icon class="section-icon"><Star /></el-icon>
                <h4>New Patterns</h4>
              </div>
              <div class="change-list">
                <div
                  v-for="(pattern, index) in comparisonData.newPatterns"
                  :key="index"
                  class="change-item neutral"
                >
                  <el-icon><InfoFilled /></el-icon>
                  <span>{{ pattern }}</span>
                </div>
                <div v-if="comparisonData.newPatterns.length === 0" class="no-changes">
                  No new patterns identified
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Detailed Comparison -->
      <div class="detailed-comparison">
        <el-tabs v-model="activeTab">
          <!-- Content Comparison -->
          <el-tab-pane label="Content Comparison" name="content">
            <div class="content-comparison">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="content-section">
                    <h5>Current Summary</h5>
                    <div class="content-preview" v-html="renderMarkdown(comparisonData.currentSummary.content)"></div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="content-section">
                    <h5>Previous Summary</h5>
                    <div class="content-preview" v-html="renderMarkdown(comparisonData.previousSummary.content)"></div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- Insights Comparison -->
          <el-tab-pane label="Insights Comparison" name="insights">
            <div class="insights-comparison">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="insights-section">
                    <h5>Current Insights</h5>
                    <div
                      v-for="(insight, index) in comparisonData.currentSummary.keyInsights"
                      :key="index"
                      class="insight-item"
                    >
                      <el-icon><Lightbulb /></el-icon>
                      <span>{{ insight }}</span>
                    </div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="insights-section">
                    <h5>Previous Insights</h5>
                    <div
                      v-for="(insight, index) in comparisonData.previousSummary.keyInsights"
                      :key="index"
                      class="insight-item"
                    >
                      <el-icon><Lightbulb /></el-icon>
                      <span>{{ insight }}</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- Recommendations Comparison -->
          <el-tab-pane label="Recommendations Comparison" name="recommendations">
            <div class="recommendations-comparison">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="recommendations-section">
                    <h5>Current Recommendations</h5>
                    <div
                      v-for="(recommendation, index) in comparisonData.currentSummary.recommendations"
                      :key="index"
                      class="recommendation-item"
                    >
                      <el-icon><Star /></el-icon>
                      <span>{{ recommendation }}</span>
                    </div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="recommendations-section">
                    <h5>Previous Recommendations</h5>
                    <div
                      v-for="(recommendation, index) in comparisonData.previousSummary.recommendations"
                      :key="index"
                      class="recommendation-item"
                    >
                      <el-icon><Star /></el-icon>
                      <span>{{ recommendation }}</span>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Empty State -->
    <div v-if="!comparisonData && !loading" class="empty-state">
      <el-empty description="Select a summary to compare with" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts,
  Warning,
  Star,
  Check,
  Close,
  InfoFilled,
  Lightbulb
} from '@element-plus/icons-vue'
import { compareSummaries, getSummariesByDateRange } from '@/api/aiSummary'

// Props
const props = defineProps({
  currentSummary: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['close'])

// Data
const loading = ref(false)
const activeTab = ref('content')
const selectedPreviousSummary = ref(null)
const availableSummaries = ref([])
const comparisonData = ref(null)

// Methods
const loadAvailableSummaries = async () => {
  try {
    const params = {
      teamId: props.currentSummary.teamId,
      startDate: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // Last 90 days
      endDate: new Date().toISOString().split('T')[0]
    }
    
    const response = await getSummariesByDateRange(params)
    availableSummaries.value = (response.data || [])
      .filter(summary => summary.summaryId !== props.currentSummary.summaryId)
      .sort((a, b) => new Date(b.metadata?.generatedAt) - new Date(a.metadata?.generatedAt))
  } catch (error) {
    console.error('Failed to load available summaries:', error)
    ElMessage.error('Failed to load available summaries')
  }
}

const loadComparison = async () => {
  if (!selectedPreviousSummary.value) return
  
  loading.value = true
  try {
    const response = await compareSummaries(
      props.currentSummary.summaryId,
      selectedPreviousSummary.value
    )
    comparisonData.value = response.data
  } catch (error) {
    console.error('Failed to load comparison:', error)
    ElMessage.error('Failed to load comparison')
  } finally {
    loading.value = false
  }
}

const renderMarkdown = (content) => {
  if (!content) return ''
  
  // Simple markdown to HTML conversion
  return content
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^\* (.*$)/gim, '<li>$1</li>')
    .replace(/^\- (.*$)/gim, '<li>$1</li>')
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

const formatPercentage = (value) => {
  if (value == null) return 'N/A'
  return `${value.toFixed(1)}%`
}

// Lifecycle
onMounted(() => {
  loadAvailableSummaries()
})
</script>

<style scoped>
.summary-comparison {
  max-height: 80vh;
  overflow-y: auto;
}

.comparison-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.comparison-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.stats-comparison {
  margin-bottom: 32px;
}

.stats-comparison h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.comparison-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background: white;
}

.comparison-card.current {
  border-color: #409eff;
  background: #f0f9ff;
}

.comparison-card.previous {
  border-color: #909399;
  background: #f8f9fa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h5 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.stat-item .label {
  color: #606266;
  font-weight: 500;
}

.stat-item .value {
  color: #303133;
  font-weight: 600;
}

.changes-overview {
  margin-bottom: 32px;
}

.change-section {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  height: 100%;
}

.change-section.improvements {
  border-color: #67c23a;
  background: #f0f9ff;
}

.change-section.regressions {
  border-color: #f56c6c;
  background: #fef0f0;
}

.change-section.new-patterns {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-icon {
  font-size: 18px;
}

.improvements .section-icon {
  color: #67c23a;
}

.regressions .section-icon {
  color: #f56c6c;
}

.new-patterns .section-icon {
  color: #e6a23c;
}

.change-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.change-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  line-height: 1.5;
}

.change-item.positive {
  color: #67c23a;
}

.change-item.negative {
  color: #f56c6c;
}

.change-item.neutral {
  color: #e6a23c;
}

.no-changes {
  color: #909399;
  font-style: italic;
  text-align: center;
  padding: 20px 0;
}

.detailed-comparison {
  margin-top: 32px;
}

.content-comparison,
.insights-comparison,
.recommendations-comparison {
  padding: 20px 0;
}

.content-section,
.insights-section,
.recommendations-section {
  height: 100%;
}

.content-section h5,
.insights-section h5,
.recommendations-section h5 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.content-preview {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 16px;
  min-height: 300px;
  line-height: 1.6;
  color: #303133;
}

.insight-item,
.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  margin-bottom: 8px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.5;
}

.loading-state,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

:deep(.el-tabs__content) {
  padding: 0;
}
</style>