<template>
  <el-dialog
    v-model="dialogVisible"
    title="通知偏好设置"
    width="800px"
    :before-close="handleClose"
  >
    <div class="preferences-content" v-loading="loading">
      <el-alert
        title="通知偏好设置"
        description="您可以根据需要调整不同类型通知的接收方式和时间设置"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 通知类型设置 -->
        <el-tab-pane label="通知类型" name="types">
          <div class="preference-list">
            <div
              v-for="preference in preferences"
              :key="preference.notificationType"
              class="preference-item"
            >
              <div class="preference-header">
                <h4>{{ getNotificationTypeDesc(preference.notificationType) }}</h4>
                <el-switch
                  v-model="preference.inAppEnabled"
                  @change="handlePreferenceChange(preference)"
                />
              </div>
              
              <div class="preference-channels" v-if="preference.inAppEnabled">
                <div class="channel-group">
                  <span class="channel-label">接收渠道：</span>
                  <div class="channel-options">
                    <el-checkbox
                      v-model="preference.inAppEnabled"
                      @change="handlePreferenceChange(preference)"
                    >
                      站内信
                    </el-checkbox>
                    <el-checkbox
                      v-model="preference.emailEnabled"
                      @change="handlePreferenceChange(preference)"
                    >
                      邮件
                    </el-checkbox>
                    <el-checkbox
                      v-model="preference.wechatWorkEnabled"
                      @change="handlePreferenceChange(preference)"
                    >
                      企业微信
                    </el-checkbox>
                    <el-checkbox
                      v-model="preference.smsEnabled"
                      @change="handlePreferenceChange(preference)"
                    >
                      短信
                    </el-checkbox>
                  </div>
                </div>
                
                <div class="frequency-setting">
                  <span class="frequency-label">频率限制：</span>
                  <el-input-number
                    v-model="preference.frequencyLimit"
                    :min="1"
                    :max="1440"
                    size="small"
                    @change="handlePreferenceChange(preference)"
                  />
                  <span class="frequency-unit">分钟内最多一次</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 免打扰设置 -->
        <el-tab-pane label="免打扰" name="quiet">
          <div class="quiet-settings">
            <el-form :model="quietSettings" label-width="120px">
              <el-form-item label="启用免打扰">
                <el-switch
                  v-model="quietSettings.enabled"
                  @change="handleQuietSettingChange"
                />
                <div class="form-item-tip">
                  启用后，在指定时间段内不会收到通知推送
                </div>
              </el-form-item>
              
              <el-form-item label="免打扰时间" v-if="quietSettings.enabled">
                <el-time-picker
                  v-model="quietSettings.startTime"
                  placeholder="开始时间"
                  format="HH:mm"
                  value-format="HH:mm"
                  @change="handleQuietSettingChange"
                />
                <span style="margin: 0 8px;">至</span>
                <el-time-picker
                  v-model="quietSettings.endTime"
                  placeholder="结束时间"
                  format="HH:mm"
                  value-format="HH:mm"
                  @change="handleQuietSettingChange"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 高级设置 -->
        <el-tab-pane label="高级设置" name="advanced">
          <div class="advanced-settings">
            <el-alert
              title="高级设置"
              description="这些设置将影响所有类型的通知"
              type="warning"
              :closable="false"
              style="margin-bottom: 20px;"
            />
            
            <el-form label-width="150px">
              <el-form-item label="通知声音">
                <el-switch v-model="advancedSettings.soundEnabled" />
                <div class="form-item-tip">
                  启用后，收到新通知时会播放提示音
                </div>
              </el-form-item>
              
              <el-form-item label="桌面通知">
                <el-switch v-model="advancedSettings.desktopEnabled" />
                <div class="form-item-tip">
                  启用后，会显示浏览器桌面通知
                </div>
              </el-form-item>
              
              <el-form-item label="通知预览">
                <el-switch v-model="advancedSettings.previewEnabled" />
                <div class="form-item-tip">
                  启用后，通知中会显示详细内容预览
                </div>
              </el-form-item>
              
              <el-form-item label="自动清理">
                <el-select v-model="advancedSettings.autoCleanupDays" style="width: 200px;">
                  <el-option label="不自动清理" :value="0" />
                  <el-option label="7天后" :value="7" />
                  <el-option label="30天后" :value="30" />
                  <el-option label="90天后" :value="90" />
                </el-select>
                <div class="form-item-tip">
                  自动清理指定天数前的已读通知
                </div>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleReset">恢复默认</el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存设置
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { notificationPreferenceApi, type NotificationPreference } from '@/api/notification'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'updated'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const activeTab = ref('types')
const preferences = ref<NotificationPreference[]>([])

const quietSettings = reactive({
  enabled: false,
  startTime: '22:00',
  endTime: '08:00'
})

const advancedSettings = reactive({
  soundEnabled: true,
  desktopEnabled: true,
  previewEnabled: true,
  autoCleanupDays: 30
})

// 计算属性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value)
})

// 通知类型描述映射
const notificationTypeDescMap: Record<string, string> = {
  'REVIEW_ASSIGNED': '评审分配通知',
  'REVIEW_SUBMITTED': '评审提交通知',
  'ISSUE_CREATED': '问题创建通知',
  'ISSUE_ASSIGNED': '问题分配通知',
  'FIX_SUBMITTED': '整改提交通知',
  'FIX_VERIFIED': '整改验证通知',
  'SUMMARY_GENERATED': '汇总生成通知',
  'DEADLINE_REMINDER': '截止提醒通知',
  'SYSTEM_ANNOUNCEMENT': '系统公告通知'
}

// 方法
const loadPreferences = async () => {
  loading.value = true
  try {
    const { data } = await notificationPreferenceApi.getCurrentUserPreferences()
    preferences.value = data
    
    // 设置免打扰时间（从第一个偏好设置中获取）
    if (data.length > 0) {
      const firstPreference = data[0]
      quietSettings.enabled = firstPreference.quietEnabled || false
      quietSettings.startTime = firstPreference.quietStartTime || '22:00'
      quietSettings.endTime = firstPreference.quietEndTime || '08:00'
    }
  } catch (error) {
    ElMessage.error('加载通知偏好设置失败')
    console.error('加载通知偏好设置失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePreferenceChange = (preference: NotificationPreference) => {
  // 如果所有渠道都被禁用，至少保留站内信
  if (!preference.inAppEnabled && !preference.emailEnabled && 
      !preference.wechatWorkEnabled && !preference.smsEnabled) {
    preference.inAppEnabled = true
    ElMessage.warning('至少需要启用一种通知渠道')
  }
}

const handleQuietSettingChange = () => {
  // 更新所有偏好设置的免打扰时间
  preferences.value.forEach(preference => {
    preference.quietEnabled = quietSettings.enabled
    preference.quietStartTime = quietSettings.startTime
    preference.quietEndTime = quietSettings.endTime
  })
}

const handleSave = async () => {
  saving.value = true
  try {
    // 更新免打扰设置到所有偏好中
    preferences.value.forEach(preference => {
      preference.quietEnabled = quietSettings.enabled
      preference.quietStartTime = quietSettings.startTime
      preference.quietEndTime = quietSettings.endTime
    })
    
    await notificationPreferenceApi.updateCurrentUserPreferences(preferences.value)
    
    ElMessage.success('通知偏好设置已保存')
    emit('updated')
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error('保存通知偏好设置失败')
    console.error('保存通知偏好设置失败:', error)
  } finally {
    saving.value = false
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要恢复默认设置吗？这将清除您的所有自定义设置。',
      '确认重置',
      { type: 'warning' }
    )
    
    await notificationPreferenceApi.resetCurrentUserToDefault()
    await loadPreferences()
    
    ElMessage.success('已恢复默认设置')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('恢复默认设置失败')
      console.error('恢复默认设置失败:', error)
    }
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

const getNotificationTypeDesc = (type: string): string => {
  return notificationTypeDescMap[type] || type
}

// 监听对话框显示状态
watch(() => props.visible, (visible) => {
  if (visible) {
    loadPreferences()
  }
})
</script>

<style scoped>
.preferences-content {
  min-height: 400px;
}

.preference-list {
  max-height: 500px;
  overflow-y: auto;
}

.preference-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 12px;
}

.preference-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.preference-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.preference-channels {
  padding-left: 16px;
  border-left: 2px solid #e4e7ed;
}

.channel-group {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.channel-label {
  font-size: 14px;
  color: #606266;
  margin-right: 12px;
  min-width: 80px;
}

.channel-options {
  display: flex;
  gap: 16px;
}

.frequency-setting {
  display: flex;
  align-items: center;
  gap: 8px;
}

.frequency-label {
  font-size: 14px;
  color: #606266;
  min-width: 80px;
}

.frequency-unit {
  font-size: 14px;
  color: #909399;
}

.quiet-settings {
  padding: 20px;
}

.advanced-settings {
  padding: 20px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-checkbox) {
  margin-right: 16px;
}

:deep(.el-input-number) {
  width: 120px;
}
</style>