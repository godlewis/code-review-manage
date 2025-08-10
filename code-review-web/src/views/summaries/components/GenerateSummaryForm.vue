<template>
  <div class="generate-summary-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="Summary Type" prop="summaryType">
        <el-select v-model="form.summaryType" placeholder="Select summary type">
          <el-option label="Team Weekly" value="TEAM_WEEKLY" />
          <el-option label="Team Monthly" value="TEAM_MONTHLY" />
          <el-option label="Architect Weekly" value="ARCHITECT_WEEKLY" />
          <el-option label="Architect Monthly" value="ARCHITECT_MONTHLY" />
          <el-option label="Custom" value="CUSTOM" />
        </el-select>
      </el-form-item>

      <el-form-item 
        v-if="form.summaryType && !form.summaryType.includes('ARCHITECT')"
        label="Team" 
        prop="teamId"
      >
        <el-select v-model="form.teamId" placeholder="Select team">
          <el-option
            v-for="team in teams"
            :key="team.id"
            :label="team.name"
            :value="team.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Date Range" prop="dateRange">
        <el-date-picker
          v-model="form.dateRange"
          type="daterange"
          range-separator="To"
          start-placeholder="Start date"
          end-placeholder="End date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
        />
      </el-form-item>

      <el-form-item label="Include Analysis">
        <el-switch v-model="form.includeAnalysis" />
        <span class="form-help">Include detailed issue analysis in the summary</span>
      </el-form-item>

      <el-form-item label="Include Recommendations">
        <el-switch v-model="form.includeRecommendations" />
        <span class="form-help">Include actionable recommendations</span>
      </el-form-item>

      <el-form-item label="Custom Prompt" prop="customPrompt">
        <el-input
          v-model="form.customPrompt"
          type="textarea"
          :rows="4"
          placeholder="Enter custom instructions for AI analysis (optional)"
        />
      </el-form-item>

      <!-- Preview Section -->
      <el-form-item v-if="previewData" label="Preview">
        <div class="preview-section">
          <div class="preview-item">
            <span class="label">Period:</span>
            <span class="value">{{ formatDateRange(form.dateRange) }}</span>
          </div>
          <div class="preview-item">
            <span class="label">Type:</span>
            <span class="value">{{ getSummaryTypeLabel(form.summaryType) }}</span>
          </div>
          <div class="preview-item" v-if="form.teamId">
            <span class="label">Team:</span>
            <span class="value">{{ getTeamName(form.teamId) }}</span>
          </div>
          <div class="preview-item">
            <span class="label">Estimated Issues:</span>
            <span class="value">{{ previewData.estimatedIssues || 'Loading...' }}</span>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <div class="form-actions">
      <el-button @click="$emit('cancel')">Cancel</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        Generate Summary
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTeams } from '@/api/team'

// Emits
const emit = defineEmits(['submit', 'cancel'])

// Data
const formRef = ref()
const submitting = ref(false)
const teams = ref([])
const previewData = ref(null)

// Form data
const form = reactive({
  summaryType: '',
  teamId: null,
  dateRange: [],
  includeAnalysis: true,
  includeRecommendations: true,
  customPrompt: ''
})

// Form rules
const rules = {
  summaryType: [
    { required: true, message: 'Please select summary type', trigger: 'change' }
  ],
  teamId: [
    { 
      required: true, 
      message: 'Please select team', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (form.summaryType && !form.summaryType.includes('ARCHITECT') && !value) {
          callback(new Error('Please select team'))
        } else {
          callback()
        }
      }
    }
  ],
  dateRange: [
    { required: true, message: 'Please select date range', trigger: 'change' }
  ]
}

// Methods
const loadTeams = async () => {
  try {
    const response = await getTeams()
    teams.value = response.data || []
  } catch (error) {
    console.error('Failed to load teams:', error)
  }
}

const handleDateRangeChange = (dateRange) => {
  if (dateRange && dateRange.length === 2) {
    loadPreviewData()
  }
}

const loadPreviewData = async () => {
  if (!form.dateRange || form.dateRange.length !== 2) return
  
  try {
    // Mock preview data - in real implementation, this would call an API
    previewData.value = {
      estimatedIssues: Math.floor(Math.random() * 100) + 20,
      estimatedPatterns: Math.floor(Math.random() * 5) + 1,
      estimatedClusters: Math.floor(Math.random() * 3) + 1
    }
  } catch (error) {
    console.error('Failed to load preview data:', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const submitData = {
      summaryType: form.summaryType,
      teamId: form.summaryType.includes('ARCHITECT') ? null : form.teamId,
      startDate: form.dateRange[0],
      endDate: form.dateRange[1],
      includeAnalysis: form.includeAnalysis,
      includeRecommendations: form.includeRecommendations,
      customPrompt: form.customPrompt || null
    }
    
    emit('submit', submitData)
  } catch (error) {
    console.error('Form validation failed:', error)
  } finally {
    submitting.value = false
  }
}

// Utility functions
const getSummaryTypeLabel = (type) => {
  const labels = {
    'TEAM_WEEKLY': 'Team Weekly',
    'TEAM_MONTHLY': 'Team Monthly',
    'ARCHITECT_WEEKLY': 'Architect Weekly',
    'ARCHITECT_MONTHLY': 'Architect Monthly',
    'CUSTOM': 'Custom'
  }
  return labels[type] || type
}

const getTeamName = (teamId) => {
  const team = teams.value.find(t => t.id === teamId)
  return team ? team.name : 'Unknown'
}

const formatDateRange = (dateRange) => {
  if (!dateRange || dateRange.length !== 2) return 'Not selected'
  return `${dateRange[0]} ~ ${dateRange[1]}`
}

// Watchers
watch(() => form.summaryType, (newType) => {
  if (newType && newType.includes('ARCHITECT')) {
    form.teamId = null
  }
})

watch(() => [form.summaryType, form.teamId, form.dateRange], () => {
  if (form.dateRange && form.dateRange.length === 2) {
    loadPreviewData()
  }
}, { deep: true })

// Lifecycle
onMounted(() => {
  loadTeams()
  
  // Set default date range based on current date
  const today = new Date()
  const lastWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  form.dateRange = [
    lastWeek.toISOString().split('T')[0],
    today.toISOString().split('T')[0]
  ]
})
</script>

<style scoped>
.generate-summary-form {
  max-width: 600px;
}

.form-help {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.preview-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.preview-item:last-child {
  margin-bottom: 0;
}

.preview-item .label {
  color: #909399;
  font-weight: 500;
}

.preview-item .value {
  color: #303133;
  font-weight: 600;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-textarea__inner) {
  resize: vertical;
}
</style>