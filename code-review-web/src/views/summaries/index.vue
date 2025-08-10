<template>
  <div class="ai-summaries">
    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <h1>AI Summary Reports</h1>
        <p>Intelligent analysis and insights from code review data</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showGenerateDialog = true">
          <el-icon><Plus /></el-icon>
          Generate Summary
        </el-button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters">
      <el-form :model="filters" inline>
        <el-form-item label="Team">
          <el-select v-model="filters.teamId" placeholder="Select team" clearable>
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Type">
          <el-select v-model="filters.summaryType" placeholder="Select type" clearable>
            <el-option label="Team Weekly" value="TEAM_WEEKLY" />
            <el-option label="Team Monthly" value="TEAM_MONTHLY" />
            <el-option label="Architect Weekly" value="ARCHITECT_WEEKLY" />
            <el-option label="Architect Monthly" value="ARCHITECT_MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-select v-model="filters.status" placeholder="Select status" clearable>
            <el-option label="Completed" value="COMPLETED" />
            <el-option label="Published" value="PUBLISHED" />
            <el-option label="Draft" value="DRAFT" />
            <el-option label="Generating" value="GENERATING" />
          </el-select>
        </el-form-item>
        <el-form-item label="Date Range">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="To"
            start-placeholder="Start date"
            end-placeholder="End date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSummaries">Search</el-button>
          <el-button @click="resetFilters">Reset</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Summary List -->
    <div class="summary-list">
      <el-row :gutter="20">
        <el-col
          v-for="summary in summaries"
          :key="summary.summaryId"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <el-card class="summary-card" @click="viewSummary(summary)">
            <template #header>
              <div class="card-header">
                <span class="title">{{ summary.title }}</span>
                <el-tag :type="getStatusType(summary.metadata?.status)">
                  {{ summary.metadata?.status || 'COMPLETED' }}
                </el-tag>
              </div>
            </template>
            
            <div class="summary-info">
              <div class="info-item">
                <span class="label">Type:</span>
                <span class="value">{{ getSummaryTypeLabel(summary.summaryType) }}</span>
              </div>
              <div class="info-item">
                <span class="label">Period:</span>
                <span class="value">{{ formatDateRange(summary.startDate, summary.endDate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">Issues:</span>
                <span class="value">{{ summary.statistics?.totalIssues || 0 }}</span>
              </div>
              <div class="info-item">
                <span class="label">Resolution Rate:</span>
                <span class="value">{{ formatPercentage(summary.statistics?.resolutionRate) }}</span>
              </div>
              <div class="info-item">
                <span class="label">Generated:</span>
                <span class="value">{{ formatDateTime(summary.metadata?.generatedAt) }}</span>
              </div>
            </div>

            <div class="card-actions">
              <el-button size="small" @click.stop="viewSummary(summary)">
                View
              </el-button>
              <el-button size="small" @click.stop="editSummary(summary)">
                Edit
              </el-button>
              <el-dropdown @click.stop trigger="click">
                <el-button size="small">
                  More<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="publishSummary(summary)">
                      Publish
                    </el-dropdown-item>
                    <el-dropdown-item @click="archiveSummary(summary)">
                      Archive
                    </el-dropdown-item>
                    <el-dropdown-item @click="compareSummary(summary)" divided>
                      Compare
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteSummary(summary)" class="danger">
                      Delete
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Empty State -->
      <div v-if="summaries.length === 0 && !loading" class="empty-state">
        <el-empty description="No summaries found">
          <el-button type="primary" @click="showGenerateDialog = true">
            Generate First Summary
          </el-button>
        </el-empty>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
    </div>

    <!-- Generate Summary Dialog -->
    <el-dialog
      v-model="showGenerateDialog"
      title="Generate AI Summary"
      width="600px"
    >
      <GenerateSummaryForm
        @submit="handleGenerateSummary"
        @cancel="showGenerateDialog = false"
      />
    </el-dialog>

    <!-- Summary Detail Dialog -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedSummary?.title"
      width="80%"
      top="5vh"
    >
      <SummaryDetail
        v-if="selectedSummary"
        :summary="selectedSummary"
        @edit="editSummary"
        @publish="publishSummary"
        @archive="archiveSummary"
      />
    </el-dialog>

    <!-- Edit Summary Dialog -->
    <el-dialog
      v-model="showEditDialog"
      title="Edit Summary"
      width="80%"
      top="5vh"
    >
      <SummaryEditor
        v-if="editingSummary"
        :summary="editingSummary"
        @save="handleSaveSummary"
        @cancel="showEditDialog = false"
      />
    </el-dialog>

    <!-- Compare Summary Dialog -->
    <el-dialog
      v-model="showCompareDialog"
      title="Compare Summaries"
      width="90%"
      top="5vh"
    >
      <SummaryComparison
        v-if="comparingSummary"
        :current-summary="comparingSummary"
        @close="showCompareDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import {
  getSummariesByDateRange,
  publishSummary as publishSummaryApi,
  archiveSummary as archiveSummaryApi,
  deleteSummary as deleteSummaryApi,
  generateSummary
} from '@/api/aiSummary'
import { getTeams } from '@/api/team'
import GenerateSummaryForm from './components/GenerateSummaryForm.vue'
import SummaryDetail from './components/SummaryDetail.vue'
import SummaryEditor from './components/SummaryEditor.vue'
import SummaryComparison from './components/SummaryComparison.vue'

// Data
const loading = ref(false)
const summaries = ref([])
const teams = ref([])

// Filters
const filters = reactive({
  teamId: null,
  summaryType: null,
  status: null,
  dateRange: null
})

// Dialogs
const showGenerateDialog = ref(false)
const showDetailDialog = ref(false)
const showEditDialog = ref(false)
const showCompareDialog = ref(false)

// Selected items
const selectedSummary = ref(null)
const editingSummary = ref(null)
const comparingSummary = ref(null)

// Computed
const summaryTypeLabels = {
  'TEAM_WEEKLY': 'Team Weekly',
  'TEAM_MONTHLY': 'Team Monthly',
  'ARCHITECT_WEEKLY': 'Architect Weekly',
  'ARCHITECT_MONTHLY': 'Architect Monthly',
  'CUSTOM': 'Custom'
}

// Methods
const loadSummaries = async () => {
  loading.value = true
  try {
    const params = {
      teamId: filters.teamId,
      status: filters.status
    }
    
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    
    const response = await getSummariesByDateRange(params)
    summaries.value = response.data || []
  } catch (error) {
    console.error('Failed to load summaries:', error)
    ElMessage.error('Failed to load summaries')
  } finally {
    loading.value = false
  }
}

const loadTeams = async () => {
  try {
    const response = await getTeams()
    teams.value = response.data || []
  } catch (error) {
    console.error('Failed to load teams:', error)
  }
}

const resetFilters = () => {
  Object.keys(filters).forEach(key => {
    filters[key] = null
  })
  loadSummaries()
}

const viewSummary = (summary) => {
  selectedSummary.value = summary
  showDetailDialog.value = true
}

const editSummary = (summary) => {
  editingSummary.value = summary
  showEditDialog.value = true
  showDetailDialog.value = false
}

const compareSummary = (summary) => {
  comparingSummary.value = summary
  showCompareDialog.value = true
}

const handleGenerateSummary = async (formData) => {
  try {
    const response = await generateSummary(formData)
    ElMessage.success('Summary generation started')
    showGenerateDialog.value = false
    
    // Refresh the list after a short delay
    setTimeout(() => {
      loadSummaries()
    }, 2000)
  } catch (error) {
    console.error('Failed to generate summary:', error)
    ElMessage.error('Failed to generate summary')
  }
}

const handleSaveSummary = async (summaryData) => {
  try {
    // Update summary logic would go here
    ElMessage.success('Summary updated successfully')
    showEditDialog.value = false
    loadSummaries()
  } catch (error) {
    console.error('Failed to update summary:', error)
    ElMessage.error('Failed to update summary')
  }
}

const publishSummary = async (summary) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to publish this summary?',
      'Confirm Publish',
      {
        confirmButtonText: 'Publish',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await publishSummaryApi(summary.summaryId)
    ElMessage.success('Summary published successfully')
    loadSummaries()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to publish summary:', error)
      ElMessage.error('Failed to publish summary')
    }
  }
}

const archiveSummary = async (summary) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to archive this summary?',
      'Confirm Archive',
      {
        confirmButtonText: 'Archive',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )
    
    await archiveSummaryApi(summary.summaryId)
    ElMessage.success('Summary archived successfully')
    loadSummaries()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to archive summary:', error)
      ElMessage.error('Failed to archive summary')
    }
  }
}

const deleteSummary = async (summary) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to delete this summary? This action cannot be undone.',
      'Confirm Delete',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'error'
      }
    )
    
    await deleteSummaryApi(summary.summaryId)
    ElMessage.success('Summary deleted successfully')
    loadSummaries()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete summary:', error)
      ElMessage.error('Failed to delete summary')
    }
  }
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

const getSummaryTypeLabel = (type) => {
  return summaryTypeLabels[type] || type
}

const formatDateRange = (startDate, endDate) => {
  if (!startDate || !endDate) return 'N/A'
  return `${startDate} ~ ${endDate}`
}

const formatPercentage = (value) => {
  if (value == null) return 'N/A'
  return `${value.toFixed(1)}%`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return 'N/A'
  return new Date(dateTime).toLocaleString()
}

// Lifecycle
onMounted(() => {
  loadSummaries()
  loadTeams()
})
</script>

<style scoped>
.ai-summaries {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.filters {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.summary-list {
  min-height: 400px;
}

.summary-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.summary-info {
  margin: 16px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #909399;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
  font-weight: 600;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.empty-state,
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.danger {
  color: #f56c6c !important;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>