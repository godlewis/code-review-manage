<template>
  <div class="summary-detail">
    <!-- Header -->
    <div class="detail-header">
      <div class="header-info">
        <h2>{{ summary.title }}</h2>
        <div class="meta-info">
          <el-tag :type="getStatusType(summary.metadata?.status)">
            {{ summary.metadata?.status || 'COMPLETED' }}
          </el-tag>
          <span class="meta-item">
            <el-icon><Calendar /></el-icon>
            {{ formatDateRange(summary.startDate, summary.endDate) }}
          </span>
          <span class="meta-item">
            <el-icon><User /></el-icon>
            {{ summary.metadata?.generatedByName || 'System' }}
          </span>
          <span class="meta-item">
            <el-icon><Clock /></el-icon>
            {{ formatDateTime(summary.metadata?.generatedAt) }}
          </span>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="$emit('edit', summary)">
          <el-icon><Edit /></el-icon>
          Edit
        </el-button>
        <el-button type="primary" @click="$emit('publish', summary)">
          <el-icon><Upload /></el-icon>
          Publish
        </el-button>
        <el-dropdown trigger="click">
          <el-button>
            More<el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="exportSummary">
                <el-icon><Download /></el-icon>
                Export PDF
              </el-dropdown-item>
              <el-dropdown-item @click="shareSummary">
                <el-icon><Share /></el-icon>
                Share
              </el-dropdown-item>
              <el-dropdown-item @click="$emit('archive', summary)" divided>
                <el-icon><FolderOpened /></el-icon>
                Archive
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- Statistics Overview -->
    <div class="statistics-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ summary.statistics?.totalIssues || 0 }}</div>
            <div class="stat-label">Total Issues</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ formatPercentage(summary.statistics?.resolutionRate) }}</div>
            <div class="stat-label">Resolution Rate</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ summary.statistics?.patternsIdentified || 0 }}</div>
            <div class="stat-label">Patterns Found</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ summary.statistics?.clustersFound || 0 }}</div>
            <div class="stat-label">Issue Clusters</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Content Tabs -->
    <el-tabs v-model="activeTab" class="content-tabs">
      <!-- Summary Content -->
      <el-tab-pane label="Summary Report" name="content">
        <div class="content-section">
          <div class="markdown-content" v-html="renderMarkdown(summary.content)"></div>
        </div>
      </el-tab-pane>

      <!-- Key Insights -->
      <el-tab-pane label="Key Insights" name="insights">
        <div class="insights-section">
          <div v-if="summary.keyInsights && summary.keyInsights.length > 0">
            <div
              v-for="(insight, index) in summary.keyInsights"
              :key="index"
              class="insight-item"
            >
              <el-icon class="insight-icon"><Lightbulb /></el-icon>
              <span>{{ insight }}</span>
            </div>
          </div>
          <el-empty v-else description="No insights available" />
        </div>
      </el-tab-pane>

      <!-- Recommendations -->
      <el-tab-pane label="Recommendations" name="recommendations">
        <div class="recommendations-section">
          <div v-if="summary.recommendations && summary.recommendations.length > 0">
            <div
              v-for="(recommendation, index) in summary.recommendations"
              :key="index"
              class="recommendation-item"
            >
              <el-icon class="recommendation-icon"><Star /></el-icon>
              <span>{{ recommendation }}</span>
            </div>
          </div>
          <el-empty v-else description="No recommendations available" />
        </div>
      </el-tab-pane>

      <!-- Analysis Details -->
      <el-tab-pane label="Analysis Details" name="analysis">
        <div class="analysis-section">
          <AnalysisCharts
            v-if="summary.analysisResult"
            :analysis-result="summary.analysisResult"
          />
          <el-empty v-else description="No analysis data available" />
        </div>
      </el-tab-pane>

      <!-- Issue Breakdown -->
      <el-tab-pane label="Issue Breakdown" name="breakdown">
        <div class="breakdown-section">
          <IssueBreakdown
            v-if="summary.statistics"
            :statistics="summary.statistics"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Generation Metadata -->
    <div class="metadata-section">
      <el-collapse>
        <el-collapse-item title="Generation Details" name="metadata">
          <div class="metadata-grid">
            <div class="metadata-item">
              <span class="label">AI Model:</span>
              <span class="value">{{ summary.metadata?.aiModel || 'N/A' }}</span>
            </div>
            <div class="metadata-item">
              <span class="label">Confidence:</span>
              <span class="value">{{ formatPercentage(summary.metadata?.confidence * 100) }}</span>
            </div>
            <div class="metadata-item">
              <span class="label">Processing Time:</span>
              <span class="value">{{ formatProcessingTime(summary.metadata?.processingTimeMs) }}</span>
            </div>
            <div class="metadata-item">
              <span class="label">Version:</span>
              <span class="value">{{ summary.metadata?.version || 'N/A' }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  User,
  Clock,
  Edit,
  Upload,
  ArrowDown,
  Download,
  Share,
  FolderOpened,
  Lightbulb,
  Star
} from '@element-plus/icons-vue'
import AnalysisCharts from './AnalysisCharts.vue'
import IssueBreakdown from './IssueBreakdown.vue'

// Props
const props = defineProps({
  summary: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['edit', 'publish', 'archive'])

// Data
const activeTab = ref('content')

// Methods
const renderMarkdown = (content) => {
  if (!content) return ''
  
  // Simple markdown to HTML conversion
  return content
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^\* (.*$)/gim, '<li>$1</li>')
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/\n/g, '<br>')
}

const exportSummary = () => {
  // Export functionality would be implemented here
  ElMessage.info('Export functionality coming soon')
}

const shareSummary = () => {
  // Share functionality would be implemented here
  ElMessage.info('Share functionality coming soon')
}

// Utility functions
const getStatusType = (status) => {
  const statusTypes = {
    'COMPLETED': 'success',
    'PUBLISHED': 'primary',
    'DRAFT': 'info',
    'GENERATING': 'warning',
    'FAILED': 'danger',
    'ARCHIVED': 'info'
  }
  return statusTypes[status] || 'info'
}

const formatDateRange = (startDate, endDate) => {
  if (!startDate || !endDate) return 'N/A'
  return `${startDate} ~ ${endDate}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return 'N/A'
  return new Date(dateTime).toLocaleString()
}

const formatPercentage = (value) => {
  if (value == null) return 'N/A'
  return `${value.toFixed(1)}%`
}

const formatProcessingTime = (timeMs) => {
  if (!timeMs) return 'N/A'
  if (timeMs < 1000) return `${timeMs}ms`
  return `${(timeMs / 1000).toFixed(1)}s`
}
</script>

<style scoped>
.summary-detail {
  max-height: 80vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-info h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.statistics-overview {
  margin-bottom: 24px;
}

.stat-card {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #e4e7ed;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.content-tabs {
  margin-bottom: 24px;
}

.content-section {
  padding: 20px 0;
}

.markdown-content {
  line-height: 1.6;
  color: #303133;
}

.markdown-content :deep(h1) {
  font-size: 24px;
  margin: 20px 0 16px 0;
  color: #303133;
}

.markdown-content :deep(h2) {
  font-size: 20px;
  margin: 18px 0 14px 0;
  color: #303133;
}

.markdown-content :deep(h3) {
  font-size: 16px;
  margin: 16px 0 12px 0;
  color: #303133;
}

.markdown-content :deep(li) {
  margin: 8px 0;
  list-style: disc;
  margin-left: 20px;
}

.insights-section,
.recommendations-section {
  padding: 20px 0;
}

.insight-item,
.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  margin-bottom: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.insight-icon,
.recommendation-icon {
  color: #409eff;
  margin-top: 2px;
  flex-shrink: 0;
}

.analysis-section,
.breakdown-section {
  padding: 20px 0;
}

.metadata-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.metadata-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 16px 0;
}

.metadata-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metadata-item .label {
  font-weight: 500;
  color: #909399;
}

.metadata-item .value {
  color: #303133;
  font-weight: 600;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-collapse-item__header) {
  font-weight: 500;
}
</style>