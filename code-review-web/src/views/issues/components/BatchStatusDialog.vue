<template>
  <el-dialog
    :model-value="modelValue"
    title="批量更新状态"
    width="500px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="新状态" prop="status">
        <el-select v-model="form.status" placeholder="选择新状态" style="width: 100%">
          <el-option label="待处理" value="OPEN" />
          <el-option label="处理中" value="IN_PROGRESS" />
          <el-option label="已解决" value="RESOLVED" />
          <el-option label="已关闭" value="CLOSED" />
          <el-option label="已拒绝" value="REJECTED" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input
          v-model="form.remarks"
          type="textarea"
          :rows="3"
          placeholder="状态更新备注（可选）"
        />
      </el-form-item>
      
      <div class="update-info">
        <p>将更新 {{ issueIds.length }} 个问题的状态</p>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定更新
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { issueApi } from '@/api/issue'
import { useUserStore } from '@/stores/user'

// Props
const props = defineProps({
  modelValue: Boolean,
  issueIds: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// Store
const userStore = useUserStore()

// 响应式数据
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  status: '',
  remarks: ''
})

const rules = {
  status: [
    { required: true, message: '请选择新状态', trigger: 'change' }
  ]
}

// 方法
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 批量更新状态
    await issueApi.batchUpdateStatus(props.issueIds, form.status, userStore.user.id)
    
    ElMessage.success('状态更新成功')
    emit('success')
    
    // 重置表单
    form.status = ''
    form.remarks = ''
    
  } catch (error) {
    ElMessage.error('状态更新失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.update-info {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 4px;
  padding: 10px;
  margin-top: 10px;
}

.update-info p {
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