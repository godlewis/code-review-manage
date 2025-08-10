<template>
  <div class="summary-editor">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="Title" prop="title">
        <el-input v-model="form.title" placeholder="Enter summary title" />
      </el-form-item>

      <el-form-item label="Content" prop="content">
        <div class="editor-container">
          <div class="editor-toolbar">
            <el-button-group>
              <el-button size="small" @click="insertMarkdown('**', '**')">
                <strong>B</strong>
              </el-button>
              <el-button size="small" @click="insertMarkdown('*', '*')">
                <em>I</em>
              </el-button>
              <el-button size="small" @click="insertMarkdown('# ', '')">
                H1
              </el-button>
              <el-button size="small" @click="insertMarkdown('## ', '')">
                H2
              </el-button>
              <el-button size="small" @click="insertMarkdown('- ', '')">
                List
              </el-button>
            </el-button-group>
            <el-button size="small" @click="showPreview = !showPreview">
              {{ showPreview ? 'Edit' : 'Preview' }}
            </el-button>
          </div>
          
          <div class="editor-content">
            <el-input
              v-if="!showPreview"
              ref="contentEditor"
              v-model="form.content"
              type="textarea"
              :rows="20"
              placeholder="Enter summary content in Markdown format..."
              class="content-textarea"
            />
            <div
              v-else
              class="content-preview"
              v-html="renderMarkdown(form.content)"
            ></div>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="Key Insights">
        <div class="insights-editor">
          <div
            v-for="(insight, index) in form.keyInsights"
            :key="index"
            class="insight-item"
          >
            <el-input
              v-model="form.keyInsights[index]"
              placeholder="Enter key insight"
            />
            <el-button
              type="danger"
              size="small"
              @click="removeInsight(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button @click="addInsight" type="primary" plain>
            <el-icon><Plus /></el-icon>
            Add Insight
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="Recommendations">
        <div class="recommendations-editor">
          <div
            v-for="(recommendation, index) in form.recommendations"
            :key="index"
            class="recommendation-item"
          >
            <el-input
              v-model="form.recommendations[index]"
              placeholder="Enter recommendation"
            />
            <el-button
              type="danger"
              size="small"
              @click="removeRecommendation(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button @click="addRecommendation" type="primary" plain>
            <el-icon><Plus /></el-icon>
            Add Recommendation
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <div class="editor-actions">
      <el-button @click="$emit('cancel')">Cancel</el-button>
      <el-button @click="saveDraft" :loading="saving">Save Draft</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">
        Save Changes
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  summary: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['save', 'cancel'])

// Data
const formRef = ref()
const contentEditor = ref()
const saving = ref(false)
const showPreview = ref(false)

// Form data
const form = reactive({
  title: '',
  content: '',
  keyInsights: [],
  recommendations: []
})

// Form rules
const rules = {
  title: [
    { required: true, message: 'Please enter title', trigger: 'blur' }
  ],
  content: [
    { required: true, message: 'Please enter content', trigger: 'blur' }
  ]
}

// Methods
const initializeForm = () => {
  form.title = props.summary.title || ''
  form.content = props.summary.content || ''
  form.keyInsights = [...(props.summary.keyInsights || [])]
  form.recommendations = [...(props.summary.recommendations || [])]
}

const insertMarkdown = (before, after) => {
  if (!contentEditor.value) return
  
  const textarea = contentEditor.value.textarea
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = form.content.substring(start, end)
  
  const newText = before + selectedText + after
  form.content = form.content.substring(0, start) + newText + form.content.substring(end)
  
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  })
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

const addInsight = () => {
  form.keyInsights.push('')
}

const removeInsight = (index) => {
  form.keyInsights.splice(index, 1)
}

const addRecommendation = () => {
  form.recommendations.push('')
}

const removeRecommendation = (index) => {
  form.recommendations.splice(index, 1)
}

const saveDraft = async () => {
  try {
    saving.value = true
    
    // Save as draft logic would go here
    ElMessage.success('Draft saved successfully')
  } catch (error) {
    console.error('Failed to save draft:', error)
    ElMessage.error('Failed to save draft')
  } finally {
    saving.value = false
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    
    saving.value = true
    
    const saveData = {
      title: form.title,
      content: form.content,
      keyInsights: form.keyInsights.filter(insight => insight.trim()),
      recommendations: form.recommendations.filter(rec => rec.trim())
    }
    
    emit('save', saveData)
  } catch (error) {
    console.error('Form validation failed:', error)
  } finally {
    saving.value = false
  }
}

// Lifecycle
onMounted(() => {
  initializeForm()
})
</script>

<style scoped>
.summary-editor {
  max-height: 80vh;
  overflow-y: auto;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.editor-content {
  position: relative;
}

.content-textarea :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  resize: none;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.content-preview {
  padding: 12px;
  min-height: 400px;
  background: white;
  line-height: 1.6;
  color: #303133;
}

.content-preview :deep(h1) {
  font-size: 24px;
  margin: 20px 0 16px 0;
  color: #303133;
}

.content-preview :deep(h2) {
  font-size: 20px;
  margin: 18px 0 14px 0;
  color: #303133;
}

.content-preview :deep(h3) {
  font-size: 16px;
  margin: 16px 0 12px 0;
  color: #303133;
}

.content-preview :deep(li) {
  margin: 8px 0;
  list-style: disc;
  margin-left: 20px;
}

.insights-editor,
.recommendations-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 12px;
  background: #fafafa;
}

.insight-item,
.recommendation-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.insight-item:last-of-type,
.recommendation-item:last-of-type {
  margin-bottom: 12px;
}

.editor-actions {
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

:deep(.el-button-group .el-button) {
  padding: 4px 8px;
  font-size: 12px;
}
</style>