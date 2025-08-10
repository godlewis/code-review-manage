<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="分配配置管理"
    width="800px"
    :close-on-click-modal="false"
  >
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基础配置 -->
      <el-tab-pane label="基础配置" name="basic">
        <el-form :model="config" :rules="configRules" ref="configFormRef" label-width="150px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="避重周期" prop="avoidanceWeeks">
                <el-input-number
                  v-model="config.avoidanceWeeks"
                  :min="1"
                  :max="12"
                  placeholder="周"
                />
                <span class="form-tip">同一对评审者在指定周期内不重复配对</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="每周最大任务数" prop="maxAssignmentsPerWeek">
                <el-input-number
                  v-model="config.maxAssignmentsPerWeek"
                  :min="1"
                  :max="10"
                  placeholder="个"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-divider content-position="left">权重配置</el-divider>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="技能匹配权重" prop="skillMatchWeight">
                <el-input-number
                  v-model="config.skillMatchWeight"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  :precision="1"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="负载均衡权重" prop="loadBalanceWeight">
                <el-input-number
                  v-model="config.loadBalanceWeight"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  :precision="1"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="多样性权重" prop="diversityWeight">
                <el-input-number
                  v-model="config.diversityWeight"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  :precision="1"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-alert
            v-if="!isWeightValid"
            title="权重配置无效：三个权重的总和必须等于1.0"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px;"
          />
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="启用自动分配">
                <el-switch v-model="config.enableAutoAssignment" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新人优先策略">
                <el-switch v-model="config.enableNewUserPriority" />
                <span class="form-tip">新人优先分配经验丰富的评审者</span>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="新用户阈值">
                <el-input-number
                  v-model="config.newUserThresholdMonths"
                  :min="1"
                  :max="12"
                  placeholder="月"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="经验用户阈值">
                <el-input-number
                  v-model="config.experiencedUserThresholdMonths"
                  :min="3"
                  :max="60"
                  placeholder="月"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>
      
      <!-- 排除配对 -->
      <el-tab-pane label="排除配对" name="exclude">
        <div class="exclude-pairs-section">
          <div class="section-header">
            <span>排除配对规则</span>
            <el-button type="primary" size="small" @click="showAddExcludeDialog = true">
              <el-icon><Plus /></el-icon>
              添加规则
            </el-button>
          </div>
          
          <el-table :data="config.excludePairs || []" stripe>
            <el-table-column prop="userId1" label="用户1" width="120">
              <template #default="{ row }">
                {{ getUserName(row.userId1) }}
              </template>
            </el-table-column>
            <el-table-column prop="userId2" label="用户2" width="120">
              <template #default="{ row }">
                {{ getUserName(row.userId2) }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="排除原因" />
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column label="操作" width="120">
              <template #default="{ row, $index }">
                <el-button size="small" @click="editExcludePair(row, $index)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeExcludePair($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 强制配对 -->
      <el-tab-pane label="强制配对" name="force">
        <div class="force-pairs-section">
          <div class="section-header">
            <span>强制配对规则</span>
            <el-button type="primary" size="small" @click="showAddForceDialog = true">
              <el-icon><Plus /></el-icon>
              添加规则
            </el-button>
          </div>
          
          <el-table :data="config.forcePairs || []" stripe>
            <el-table-column prop="reviewerId" label="评审者" width="120">
              <template #default="{ row }">
                {{ getUserName(row.reviewerId) }}
              </template>
            </el-table-column>
            <el-table-column prop="revieweeId" label="被评审者" width="120">
              <template #default="{ row }">
                {{ getUserName(row.revieweeId) }}
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="强制原因" />
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">
                  {{ row.priority }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                  {{ row.enabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120" />
            <el-table-column prop="endDate" label="结束日期" width="120" />
            <el-table-column label="操作" width="120">
              <template #default="{ row, $index }">
                <el-button size="small" @click="editForcePair(row, $index)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeForcePair($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 用户特殊配置 -->
      <el-tab-pane label="用户配置" name="users">
        <div class="user-configs-section">
          <div class="section-header">
            <span>用户特殊配置</span>
            <el-button type="primary" size="small" @click="showAddUserConfigDialog = true">
              <el-icon><Plus /></el-icon>
              添加配置
            </el-button>
          </div>
          
          <el-table :data="userConfigList" stripe>
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="maxAssignmentsPerWeek" label="最大任务数" width="100" />
            <el-table-column label="角色限制" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.reviewerOnly" type="info" size="small">仅评审者</el-tag>
                <el-tag v-else-if="row.revieweeOnly" type="warning" size="small">仅被评审者</el-tag>
                <span v-else>无限制</span>
              </template>
            </el-table-column>
            <el-table-column prop="pauseAssignment" label="暂停状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.pauseAssignment ? 'danger' : 'success'" size="small">
                  {{ row.pauseAssignment ? '已暂停' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remarks" label="备注" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="editUserConfig(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeUserConfig(row.userId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="info" @click="validateConfig">验证配置</el-button>
        <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
      </div>
    </template>
    
    <!-- 添加排除配对对话框 -->
    <el-dialog
      v-model="showAddExcludeDialog"
      title="添加排除配对"
      width="500px"
      append-to-body
    >
      <el-form :model="excludeForm" :rules="excludeRules" ref="excludeFormRef">
        <el-form-item label="用户1" prop="userId1">
          <el-select v-model="excludeForm.userId1" placeholder="选择用户">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户2" prop="userId2">
          <el-select v-model="excludeForm.userId2" placeholder="选择用户">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排除原因" prop="reason">
          <el-input v-model="excludeForm.reason" placeholder="请输入排除原因" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="excludeForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="excludeForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddExcludeDialog = false">取消</el-button>
        <el-button type="primary" @click="addExcludePair">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 添加强制配对对话框 -->
    <el-dialog
      v-model="showAddForceDialog"
      title="添加强制配对"
      width="500px"
      append-to-body
    >
      <el-form :model="forceForm" :rules="forceRules" ref="forceFormRef">
        <el-form-item label="评审者" prop="reviewerId">
          <el-select v-model="forceForm.reviewerId" placeholder="选择评审者">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="被评审者" prop="revieweeId">
          <el-select v-model="forceForm.revieweeId" placeholder="选择被评审者">
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="强制原因" prop="reason">
          <el-input v-model="forceForm.reason" placeholder="请输入强制原因" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="forceForm.priority" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="forceForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="forceForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddForceDialog = false">取消</el-button>
        <el-button type="primary" @click="addForcePair">确定</el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { assignmentConfigApi } from '@/api/assignmentConfig'
import { userApi } from '@/api/user'

// Props & Emits
const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'config-updated'])

// 响应式数据
const activeTab = ref('basic')
const saving = ref(false)
const users = ref([])
const config = reactive({
  avoidanceWeeks: 4,
  maxAssignmentsPerWeek: 3,
  skillMatchWeight: 0.4,
  loadBalanceWeight: 0.3,
  diversityWeight: 0.3,
  enableAutoAssignment: true,
  enableNewUserPriority: true,
  newUserThresholdMonths: 3,
  experiencedUserThresholdMonths: 6,
  excludePairs: [],
  forcePairs: [],
  userSpecialConfigs: {}
})

// 对话框状态
const showAddExcludeDialog = ref(false)
const showAddForceDialog = ref(false)
const showAddUserConfigDialog = ref(false)

// 表单数据
const excludeForm = reactive({
  userId1: null,
  userId2: null,
  reason: '',
  dateRange: null,
  enabled: true
})

const forceForm = reactive({
  reviewerId: null,
  revieweeId: null,
  reason: '',
  priority: 1,
  dateRange: null,
  enabled: true
})

// 表单验证规则
const configRules = {
  avoidanceWeeks: [{ required: true, message: '请输入避重周期', trigger: 'blur' }],
  maxAssignmentsPerWeek: [{ required: true, message: '请输入每周最大任务数', trigger: 'blur' }],
  skillMatchWeight: [{ required: true, message: '请输入技能匹配权重', trigger: 'blur' }],
  loadBalanceWeight: [{ required: true, message: '请输入负载均衡权重', trigger: 'blur' }],
  diversityWeight: [{ required: true, message: '请输入多样性权重', trigger: 'blur' }]
}

const excludeRules = {
  userId1: [{ required: true, message: '请选择用户1', trigger: 'change' }],
  userId2: [{ required: true, message: '请选择用户2', trigger: 'change' }],
  reason: [{ required: true, message: '请输入排除原因', trigger: 'blur' }]
}

const forceRules = {
  reviewerId: [{ required: true, message: '请选择评审者', trigger: 'change' }],
  revieweeId: [{ required: true, message: '请选择被评审者', trigger: 'change' }],
  reason: [{ required: true, message: '请输入强制原因', trigger: 'blur' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }]
}

// 表单引用
const configFormRef = ref()
const excludeFormRef = ref()
const forceFormRef = ref()

// 计算属性
const isWeightValid = computed(() => {
  const total = config.skillMatchWeight + config.loadBalanceWeight + config.diversityWeight
  return Math.abs(total - 1.0) < 0.001
})

const userConfigList = computed(() => {
  return Object.values(config.userSpecialConfigs || {})
})

// 监听对话框显示状态
watch(() => props.modelValue, (visible) => {
  if (visible) {
    loadConfig()
    loadUsers()
  }
})

// 生命周期
onMounted(() => {
  if (props.modelValue) {
    loadConfig()
    loadUsers()
  }
})

// 方法
const loadConfig = async () => {
  try {
    const response = await assignmentConfigApi.getCurrentConfig()
    Object.assign(config, response.data)
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

const loadUsers = async () => {
  try {
    const response = await userApi.getAllUsers()
    users.value = response.data || []
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const validateConfig = async () => {
  try {
    const response = await assignmentConfigApi.validateConfig(config)
    const result = response.data
    
    if (result.valid) {
      ElMessage.success('配置验证通过')
    } else {
      ElMessageBox.alert(
        result.errors.join('\n'),
        '配置验证失败',
        { type: 'warning' }
      )
    }
  } catch (error) {
    ElMessage.error('验证配置失败')
  }
}

const saveConfig = async () => {
  if (!configFormRef.value) return
  
  const valid = await configFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (!isWeightValid.value) {
    ElMessage.warning('权重配置无效，请检查权重总和是否等于1.0')
    return
  }

  saving.value = true
  try {
    // 分别保存不同类型的配置
    const promises = []
    
    // 保存排除配对规则
    if (config.excludePairs && config.excludePairs.length > 0) {
      promises.push(assignmentConfigApi.updateExcludePairs(config.excludePairs))
    }
    
    // 保存强制配对规则
    if (config.forcePairs && config.forcePairs.length > 0) {
      promises.push(assignmentConfigApi.updateForcePairs(config.forcePairs))
    }
    
    // 保存用户特殊配置
    if (config.userSpecialConfigs) {
      Object.entries(config.userSpecialConfigs).forEach(([userId, userConfig]) => {
        promises.push(assignmentConfigApi.updateUserSpecialConfig(parseInt(userId), userConfig))
      })
    }
    
    // 保存团队特殊配置
    if (config.teamSpecialConfigs) {
      Object.entries(config.teamSpecialConfigs).forEach(([teamId, teamConfig]) => {
        promises.push(assignmentConfigApi.updateTeamSpecialConfig(parseInt(teamId), teamConfig))
      })
    }
    
    await Promise.all(promises)
    ElMessage.success('配置保存成功')
    emit('config-updated')
    emit('update:modelValue', false)
  } catch (error) {
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

const addExcludePair = async () => {
  if (!excludeFormRef.value) return
  
  const valid = await excludeFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (excludeForm.userId1 === excludeForm.userId2) {
    ElMessage.warning('不能选择相同的用户')
    return
  }
  
  const pair = {
    userId1: excludeForm.userId1,
    userId2: excludeForm.userId2,
    reason: excludeForm.reason,
    enabled: excludeForm.enabled,
    startDate: excludeForm.dateRange?.[0] || null,
    endDate: excludeForm.dateRange?.[1] || null
  }
  
  if (!config.excludePairs) {
    config.excludePairs = []
  }
  config.excludePairs.push(pair)
  
  // 重置表单
  Object.assign(excludeForm, {
    userId1: null,
    userId2: null,
    reason: '',
    dateRange: null,
    enabled: true
  })
  
  showAddExcludeDialog.value = false
  ElMessage.success('添加排除配对成功')
}

const editExcludePair = (pair, index) => {
  // 实现编辑功能
  ElMessage.info('编辑功能待实现')
}

const removeExcludePair = (index) => {
  config.excludePairs.splice(index, 1)
  ElMessage.success('删除成功')
}

const addForcePair = async () => {
  if (!forceFormRef.value) return
  
  const valid = await forceFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (forceForm.reviewerId === forceForm.revieweeId) {
    ElMessage.warning('评审者和被评审者不能是同一个人')
    return
  }
  
  const pair = {
    reviewerId: forceForm.reviewerId,
    revieweeId: forceForm.revieweeId,
    reason: forceForm.reason,
    priority: forceForm.priority,
    enabled: forceForm.enabled,
    startDate: forceForm.dateRange?.[0] || null,
    endDate: forceForm.dateRange?.[1] || null
  }
  
  if (!config.forcePairs) {
    config.forcePairs = []
  }
  config.forcePairs.push(pair)
  
  // 重置表单
  Object.assign(forceForm, {
    reviewerId: null,
    revieweeId: null,
    reason: '',
    priority: 1,
    dateRange: null,
    enabled: true
  })
  
  showAddForceDialog.value = false
  ElMessage.success('添加强制配对成功')
}

const editForcePair = (pair, index) => {
  // 实现编辑功能
  ElMessage.info('编辑功能待实现')
}

const removeForcePair = (index) => {
  config.forcePairs.splice(index, 1)
  ElMessage.success('删除成功')
}

const editUserConfig = (userConfig) => {
  // 实现编辑功能
  ElMessage.info('编辑功能待实现')
}

const removeUserConfig = (userId) => {
  if (config.userSpecialConfigs && config.userSpecialConfigs[userId]) {
    delete config.userSpecialConfigs[userId]
    ElMessage.success('删除成功')
  }
}

// 工具方法
const getUserName = (userId) => {
  const user = users.value.find(u => u.id === userId)
  return user ? user.realName : `用户${userId}`
}

const getPriorityType = (priority) => {
  if (priority >= 8) return 'danger'
  if (priority >= 5) return 'warning'
  return 'info'
}
</script>

<style scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.exclude-pairs-section,
.force-pairs-section,
.user-configs-section {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>