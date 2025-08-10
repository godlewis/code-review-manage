<template>
  <el-dialog
    :model-value="modelValue"
    title="验证整改记录"
    width="600px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div v-if="fixRecord" class="fix-record-info">
      <h4>整改记录信息</h4>
      <div class="info-item">
        <label>整改描述:</label>
        <p>{{ fixRecord.fixDescription }}</p>
      </div>
      
      <div class="info-item" v-if="fixRecord.beforeCodeUrl">
        <label>整改前代码:</label>
        <el-link :href="fixRecord.beforeCodeUrl" target="_blank" type="primary">
          查看代码
        </el-link>
      </div>
      
      <div class="info-item" v-if="fixRecord.afterCodeUrl">
        <label>整改后代码:</label>
        <el-link :href="fixRecord.afterCodeUrl" target="_blank" type="primary">
          查看代码
        </el-link>
      </div>
    </div>

    <el-divider />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="验证结果" prop="result">
        <el-radio-group v-model="form.result">
          <el-radio label="PASS">通过</el-radio>
          <el-radio label="FAIL">不通过</el-radio>
          <el-radio label="NEED_FURTHER_FIX">需要进一步修改</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="验证备注" prop="remarks">
        <el-input
          v-model="form.remarks"
          type="textarea"
          :rows="4"
          placeholder="请详细说明验证结果和建议"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交验证
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
  fixRecord: Object
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// Store
const userStore = useUserStore()

// 响应式数据
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  result: '',
  remarks: ''
})

const rules = {
  result: [
    { required: true, message: '请选择验证结果', trigger: 'change' }
  ],
  remarks: [
    { required: true, message: '请输入验证备注', trigger: 'blur' },
    { min: 5, max: 1000, message: '备注长度应在 5 到 1000 个字符', trigger: 'blur' }
  ]
}

// 方法
const handleSubmit = async () => {
  if (!formRef.value || !props.fixRecord) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const requestData = {
      ...form,
      verifierId: userStore.user.id
    }
    
    await issueApi.verifyFixRecord(props.fixRecord.id, requestData)
    
    ElMessage.success('验证完成')
    emit('success')
    
    // 重置表单
    form.result = ''
    form.remarks = ''
    
  } catch (error) {
    ElMessage.error('验证失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.fix-record-info {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 20px;
}

.fix-record-info h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.info-item {
  margin-bottom: 10px;
}

.info-item label {
  font-weight: 500;
  color: #606266;
  display: block;
  margin-bottom: 5px;
}

.info-item p {
  margin: 0;
  color: #303133;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.el-radio) {
  margin-right: 0;
}
</style>