<template>
  <el-dialog
    :model-value="modelValue"
    title="分配问题"
    width="500px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="分配给" prop="assignedTo">
        <el-select v-model="form.assignedTo" placeholder="选择分配人" style="width: 100%">
          <el-option
            v-for="member in teamMembers"
            :key="member.id"
            :label="member.username"
            :value="member.id"
          >
            <div class="member-option">
              <span>{{ member.username }}</span>
              <span class="member-role">{{ member.role }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input
          v-model="form.remarks"
          type="textarea"
          :rows="3"
          placeholder="分配备注（可选）"
        />
      </el-form-item>
      
      <div class="assign-info">
        <p>将分配 {{ issueIds.length }} 个问题给选中的用户</p>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定分配
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { issueApi } from '@/api/issue'

// Props
const props = defineProps({
  modelValue: Boolean,
  issueIds: {
    type: Array,
    default: () => []
  },
  teamMembers: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  assignedTo: null,
  remarks: ''
})

const rules = {
  assignedTo: [
    { required: true, message: '请选择分配人', trigger: 'change' }
  ]
}

// 方法
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 批量分配问题
    for (const issueId of props.issueIds) {
      await issueApi.updateIssue(issueId, {
        assignedTo: form.assignedTo
      })
    }
    
    ElMessage.success('分配成功')
    emit('success')
    
    // 重置表单
    form.assignedTo = null
    form.remarks = ''
    
  } catch (error) {
    ElMessage.error('分配失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.member-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-role {
  font-size: 12px;
  color: #909399;
}

.assign-info {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
}

.assign-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>