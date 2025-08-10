<template>
  <div class="assignment-management">
    <div class="page-header">
      <h1>评审任务分配管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showGenerateDialog = true">
          <el-icon><Plus /></el-icon>
          生成分配
        </el-button>
        <el-button @click="showConfigDialog = true">
          <el-icon><Setting /></el-icon>
          分配配置
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
      <el-tab-pane label="当前分配" name="current">
        <current-assignments />
      </el-tab-pane>
      <el-tab-pane label="分配历史" name="history">
        <assignment-history />
      </el-tab-pane>
      <el-tab-pane label="统计分析" name="statistics">
        <assignment-statistics />
      </el-tab-pane>
    </el-tabs>

    <!-- 生成分配对话框 -->
    <el-dialog
      v-model="showGenerateDialog"
      title="生成评审分配"
      width="600px"
    >
      <el-form :model="generateForm" :rules="generateRules" ref="generateFormRef">
        <el-form-item label="团队" prop="teamId">
          <el-select v-model="generateForm.teamId" placeholder="选择团队">
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.name"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="周开始日期" prop="weekStart">
          <el-date-picker
            v-model="generateForm.weekStart"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="生成方式">
          <el-radio-group v-model="generateForm.mode">
            <el-radio label="single">单周生成</el-radio>
            <el-radio label="batch">批量生成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="generateForm.mode === 'batch'" label="生成周数" prop="weekCount">
          <el-input-number
            v-model="generateForm.weekCount"
            :min="1"
            :max="12"
            placeholder="输入周数"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" @click="generateAssignments" :loading="generating">
          生成
        </el-button>
      </template>
    </el-dialog>

    <!-- 配置管理对话框 -->
    <assignment-config-dialog
      v-model="showConfigDialog"
      @config-updated="handleConfigUpdated"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Setting } from '@element-plus/icons-vue'
import CurrentAssignments from './components/CurrentAssignments.vue'
import AssignmentHistory from './components/AssignmentHistory.vue'
import AssignmentStatistics from './components/AssignmentStatistics.vue'
import AssignmentConfigDialog from './components/AssignmentConfigDialog.vue'
import { assignmentApi } from '@/api/assignment'
import { teamApi } from '@/api/team'

// 响应式数据
const activeTab = ref('current')
const generating = ref(false)
const teams = ref([])

// 对话框显示状态
const showGenerateDialog = ref(false)
const showConfigDialog = ref(false)

// 生成分配表单
const generateForm = reactive({
  teamId: null,
  weekStart: null,
  mode: 'single',
  weekCount: 1
})

const generateRules = {
  teamId: [{ required: true, message: '请选择团队', trigger: 'change' }],
  weekStart: [{ required: true, message: '请选择周开始日期', trigger: 'change' }],
  weekCount: [{ required: true, message: '请输入生成周数', trigger: 'blur' }]
}

// 表单引用
const generateFormRef = ref()

// 生命周期
onMounted(() => {
  loadTeams()
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

const generateAssignments = async () => {
  if (!generateFormRef.value) return
  
  const valid = await generateFormRef.value.validate().catch(() => false)
  if (!valid) return

  generating.value = true
  try {
    if (generateForm.mode === 'single') {
      await assignmentApi.generateWeeklyAssignments(generateForm.teamId, generateForm.weekStart)
      ElMessage.success('分配生成成功')
    } else {
      await assignmentApi.generateBatchAssignments(
        generateForm.teamId, 
        generateForm.weekStart, 
        generateForm.weekCount
      )
      ElMessage.success(`批量生成${generateForm.weekCount}周分配成功`)
    }
    
    showGenerateDialog.value = false
    // 刷新当前分配页面
    if (activeTab.value === 'current') {
      // 这里可以通过事件或其他方式通知子组件刷新
    }
  } catch (error) {
    ElMessage.error('生成分配失败')
  } finally {
    generating.value = false
  }
}

const handleConfigUpdated = () => {
  ElMessage.success('配置更新成功')
  // 可以在这里刷新相关数据
}
</script>

<style scoped>
.assignment-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.main-tabs {
  margin-bottom: 20px;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}
</style>