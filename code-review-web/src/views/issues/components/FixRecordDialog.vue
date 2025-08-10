<template>
  <el-dialog
    :model-value="modelValue"
    title="提交整改记录"
    width="700px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="整改描述" prop="fixDescription">
        <el-input
          v-model="form.fixDescription"
          type="textarea"
          :rows="4"
          placeholder="请详细描述整改内容和方法"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="整改前代码链接">
        <el-input
          v-model="form.beforeCodeUrl"
          placeholder="整改前的代码链接（可选）"
        />
        <div class="form-tip">可以是Git提交链接、代码仓库链接等</div>
      </el-form-item>
      
      <el-form-item label="整改后代码链接" prop="afterCodeUrl">
        <el-input
          v-model="form.afterCodeUrl"
          placeholder="整改后的代码链接"
        />
        <div class="form-tip">请提供整改后的代码链接，便于验证</div>
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input
          v-model="form.remarks"
          type="textarea"
          :rows="2"
          placeholder="其他说明（可选）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交整改
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
  issueId: [String, Number]
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// Store
const userStore = useUserStore()

// 响应式数据
const formRef = ref()
const submitting = ref(false)

const form = reactive({
  fixDescription: '',
  beforeCodeUrl: '',
  afterCodeUrl: '',
  remarks: ''
})

const rules = {
  fixDescription: [
    { required: true, message: '请输入整改描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '描述长度应在 10 到 2000 个字符', trigger: 'blur' }
  ],
  afterCodeUrl: [
    { required: true, message: '请提供整改后的代码链接', trigger: 'blur' }
  ]
}

// 方法
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const requestData = {
      ...form,
      fixerId: userStore.user.id
    }
    
    await issueApi.submitFixRecord(props.issueId, requestData)
    
    ElMessage.success('整改记录提交成功')
    emit('success')
    
    // 重置表单
    Object.keys(form).forEach(key => {
      form[key] = ''
    })
    
  } catch (error) {
    ElMessage.error('提交失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>