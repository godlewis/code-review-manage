<template>
  <div class="notification-center">
    <div class="page-header">
      <h2>通知中心</h2>
      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-button type="text" @click="refreshNotifications">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-badge>
        <el-button type="primary" @click="markAllAsRead" :disabled="unreadCount === 0">
          全部已读
        </el-button>
        <el-button @click="showPreferences = true">
          <el-icon><Setting /></el-icon>
          通知设置
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-bar">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-select v-model="filters.type" placeholder="通知类型" clearable @change="handleFilterChange">
            <el-option
              v-for="type in notificationTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="阅读状态" clearable @change="handleFilterChange">
            <el-option label="全部" value="" />
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleFilterChange"
          />
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索通知内容"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <el-card v-loading="loading">
        <div class="list-header">
          <el-checkbox
            v-model="selectAll"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选
          </el-checkbox>
          <div class="batch-actions" v-if="selectedNotifications.length > 0">
            <el-button size="small" @click="batchMarkAsRead">
              标记已读 ({{ selectedNotifications.length }})
            </el-button>
            <el-button size="small" type="danger" @click="batchDelete">
              删除 ({{ selectedNotifications.length }})
            </el-button>
          </div>
        </div>

        <div class="notification-items">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.isRead, 'selected': selectedNotifications.includes(notification.id) }"
            @click="handleNotificationClick(notification)"
          >
            <div class="item-checkbox" @click.stop>
              <el-checkbox
                :model-value="selectedNotifications.includes(notification.id)"
                @change="handleNotificationSelect(notification.id, $event)"
              />
            </div>
            
            <div class="item-icon">
              <el-icon :size="20" :color="getNotificationIconColor(notification.notificationType)">
                <component :is="getNotificationIcon(notification.notificationType)" />
              </el-icon>
            </div>
            
            <div class="item-content">
              <div class="item-header">
                <h4 class="item-title">{{ notification.title }}</h4>
                <div class="item-meta">
                  <el-tag :type="getNotificationTypeTagType(notification.notificationType)" size="small">
                    {{ notification.notificationTypeDesc }}
                  </el-tag>
                  <span class="item-time">{{ formatTime(notification.createdAt) }}</span>
                </div>
              </div>
              <div class="item-body">
                <p class="item-description">{{ notification.content }}</p>
              </div>
              <div class="item-footer" v-if="notification.relatedId">
                <el-button type="text" size="small" @click.stop="handleViewRelated(notification)">
                  查看详情
                </el-button>
              </div>
            </div>
            
            <div class="item-actions">
              <el-dropdown @command="handleNotificationAction">
                <el-button type="text" size="small">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      :command="{ action: 'markRead', id: notification.id }"
                      v-if="!notification.isRead"
                    >
                      标记已读
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{ action: 'markUnread', id: notification.id }"
                      v-else
                    >
                      标记未读
                    </el-dropdown-item>
                    <el-dropdown-item
                      :command="{ action: 'delete', id: notification.id }"
                      divided
                    >
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && notifications.length === 0" class="empty-state">
          <el-empty description="暂无通知" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 通知偏好设置对话框 -->
    <NotificationPreferences
      v-model:visible="showPreferences"
      @updated="handlePreferencesUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Setting,
  Search,
  MoreFilled,
  Bell,
  Warning,
  CircleCheck,
  Document,
  User,
  Message
} from '@element-plus/icons-vue'
import { notificationApi, type Notification } from '@/api/notification'
import NotificationPreferences from './components/NotificationPreferences.vue'
import { useRouter } from 'vue-router'
import { useNotification } from '@/composables/useNotification'

const router = useRouter()

// 使用实时通知功能
const { 
  notificationState, 
  loading: notificationLoading, 
  loadNotifications: loadNotificationData,
  markAsRead: markNotificationAsRead,
  markAllAsRead: markAllNotificationAsRead,
  deleteNotifications: deleteNotificationData
} = useNotification()

// 响应式数据
const loading = ref(false)
const notifications = ref<Notification[]>([])
const total = ref(0)
const showPreferences = ref(false)
const selectedNotifications = ref<number[]>([])

const pagination = reactive({
  page: 1,
  size: 20
})

const filters = reactive({
  type: '',
  status: '',
  dateRange: null as [string, string] | null,
  keyword: ''
})

// 计算属性 - 使用实时通知状态
const unreadCount = computed(() => notificationState.unreadCount)

// 通知类型选项
const notificationTypes = ref([
  { value: 'REVIEW_ASSIGNED', label: '评审分配' },
  { value: 'REVIEW_SUBMITTED', label: '评审提交' },
  { value: 'ISSUE_CREATED', label: '问题创建' },
  { value: 'ISSUE_ASSIGNED', label: '问题分配' },
  { value: 'FIX_SUBMITTED', label: '整改提交' },
  { value: 'FIX_VERIFIED', label: '整改验证' },
  { value: 'SUMMARY_GENERATED', label: '汇总生成' },
  { value: 'DEADLINE_REMINDER', label: '截止提醒' },
  { value: 'SYSTEM_ANNOUNCEMENT', label: '系统公告' }
])

// 计算属性
const selectAll = computed({
  get: () => selectedNotifications.value.length === notifications.value.length && notifications.value.length > 0,
  set: (value: boolean) => {
    if (value) {
      selectedNotifications.value = notifications.value.map(n => n.id)
    } else {
      selectedNotifications.value = []
    }
  }
})

const isIndeterminate = computed(() => {
  const selected = selectedNotifications.value.length
  return selected > 0 && selected < notifications.value.length
})

// 方法
const loadNotifications = async () => {
  loading.value = true
  try {
    const unreadOnly = filters.status === 'unread'
    const { data } = await notificationApi.getCurrentUserNotifications(
      pagination.page,
      pagination.size,
      unreadOnly
    )
    
    notifications.value = data.records
    total.value = data.total
    
    // 如果有筛选条件，需要在前端进一步过滤
    if (filters.type || filters.keyword || filters.dateRange) {
      notifications.value = filterNotifications(notifications.value)
    }
    
  } catch (error) {
    ElMessage.error('加载通知列表失败')
    console.error('加载通知列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  // 未读数量现在由实时通知状态管理，无需单独加载
  // 但为了兼容性，保留这个方法
}

const filterNotifications = (list: Notification[]) => {
  return list.filter(notification => {
    // 类型筛选
    if (filters.type && notification.notificationType !== filters.type) {
      return false
    }
    
    // 状态筛选
    if (filters.status === 'read' && !notification.isRead) {
      return false
    }
    if (filters.status === 'unread' && notification.isRead) {
      return false
    }
    
    // 关键词搜索
    if (filters.keyword) {
      const keyword = filters.keyword.toLowerCase()
      if (!notification.title.toLowerCase().includes(keyword) &&
          !notification.content.toLowerCase().includes(keyword)) {
        return false
      }
    }
    
    // 日期范围筛选
    if (filters.dateRange && filters.dateRange.length === 2) {
      const notificationDate = new Date(notification.createdAt).toISOString().split('T')[0]
      if (notificationDate < filters.dateRange[0] || notificationDate > filters.dateRange[1]) {
        return false
      }
    }
    
    return true
  })
}

const refreshNotifications = async () => {
  await Promise.all([loadNotifications(), loadUnreadCount()])
}

const handleFilterChange = () => {
  pagination.page = 1
  loadNotifications()
}

const handleSearch = debounce(() => {
  pagination.page = 1
  loadNotifications()
}, 300)

const handlePageChange = (page: number) => {
  pagination.page = page
  loadNotifications()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadNotifications()
}

const handleSelectAll = (value: boolean) => {
  selectAll.value = value
}

const handleNotificationSelect = (id: number, selected: boolean) => {
  if (selected) {
    selectedNotifications.value.push(id)
  } else {
    const index = selectedNotifications.value.indexOf(id)
    if (index > -1) {
      selectedNotifications.value.splice(index, 1)
    }
  }
}

const handleNotificationClick = async (notification: Notification) => {
  // 如果未读，标记为已读
  if (!notification.isRead) {
    try {
      await markNotificationAsRead([notification.id])
      notification.isRead = true
      notification.readAt = new Date().toISOString()
    } catch (error) {
      console.error('标记通知为已读失败:', error)
    }
  }
  
  // 如果有关联业务，跳转到相关页面
  if (notification.relatedId && notification.relatedType) {
    handleViewRelated(notification)
  }
}

const handleViewRelated = (notification: Notification) => {
  if (!notification.relatedId || !notification.relatedType) return
  
  switch (notification.relatedType) {
    case 'REVIEW_RECORD':
      router.push(`/reviews/${notification.relatedId}`)
      break
    case 'ISSUE':
      router.push(`/issues/${notification.relatedId}`)
      break
    case 'ASSIGNMENT':
      router.push(`/assignments/${notification.relatedId}`)
      break
    default:
      ElMessage.info('暂不支持跳转到该类型的详情页面')
  }
}

const handleNotificationAction = async (command: { action: string; id: number }) => {
  const { action, id } = command
  
  try {
    switch (action) {
      case 'markRead':
        await notificationApi.markAsRead([id])
        const notification = notifications.value.find(n => n.id === id)
        if (notification) {
          notification.isRead = true
          notification.readAt = new Date().toISOString()
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
        ElMessage.success('已标记为已读')
        break
        
      case 'markUnread':
        // TODO: 实现标记为未读的API
        ElMessage.info('标记为未读功能暂未实现')
        break
        
      case 'delete':
        await ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', {
          type: 'warning'
        })
        await notificationApi.deleteNotifications([id])
        notifications.value = notifications.value.filter(n => n.id !== id)
        total.value--
        ElMessage.success('通知已删除')
        break
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('通知操作失败:', error)
    }
  }
}

const markAllAsRead = async () => {
  try {
    await markAllNotificationAsRead()
    notifications.value.forEach(notification => {
      if (!notification.isRead) {
        notification.isRead = true
        notification.readAt = new Date().toISOString()
      }
    })
    ElMessage.success('所有通知已标记为已读')
  } catch (error) {
    ElMessage.error('标记所有通知为已读失败')
    console.error('标记所有通知为已读失败:', error)
  }
}

const batchMarkAsRead = async () => {
  try {
    await markNotificationAsRead(selectedNotifications.value)
    notifications.value.forEach(notification => {
      if (selectedNotifications.value.includes(notification.id) && !notification.isRead) {
        notification.isRead = true
        notification.readAt = new Date().toISOString()
      }
    })
    selectedNotifications.value = []
    ElMessage.success('选中的通知已标记为已读')
  } catch (error) {
    ElMessage.error('批量标记已读失败')
    console.error('批量标记已读失败:', error)
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedNotifications.value.length} 条通知吗？`,
      '确认删除',
      { type: 'warning' }
    )
    
    await deleteNotificationData(selectedNotifications.value)
    notifications.value = notifications.value.filter(n => !selectedNotifications.value.includes(n.id))
    total.value -= selectedNotifications.value.length
    selectedNotifications.value = []
    ElMessage.success('选中的通知已删除')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error('批量删除失败:', error)
    }
  }
}

const handlePreferencesUpdated = () => {
  ElMessage.success('通知偏好设置已更新')
}

// 辅助方法
const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'REVIEW_ASSIGNED':
    case 'REVIEW_SUBMITTED':
      return Document
    case 'ISSUE_CREATED':
    case 'ISSUE_ASSIGNED':
      return Warning
    case 'FIX_SUBMITTED':
    case 'FIX_VERIFIED':
      return CircleCheck
    case 'SUMMARY_GENERATED':
      return Message
    case 'DEADLINE_REMINDER':
      return Bell
    case 'SYSTEM_ANNOUNCEMENT':
      return User
    default:
      return Bell
  }
}

const getNotificationIconColor = (type: string) => {
  switch (type) {
    case 'REVIEW_ASSIGNED':
    case 'REVIEW_SUBMITTED':
      return '#409EFF'
    case 'ISSUE_CREATED':
    case 'ISSUE_ASSIGNED':
      return '#E6A23C'
    case 'FIX_SUBMITTED':
    case 'FIX_VERIFIED':
      return '#67C23A'
    case 'SUMMARY_GENERATED':
      return '#909399'
    case 'DEADLINE_REMINDER':
      return '#F56C6C'
    case 'SYSTEM_ANNOUNCEMENT':
      return '#606266'
    default:
      return '#909399'
  }
}

const getNotificationTypeTagType = (type: string) => {
  switch (type) {
    case 'REVIEW_ASSIGNED':
    case 'REVIEW_SUBMITTED':
      return 'primary'
    case 'ISSUE_CREATED':
    case 'ISSUE_ASSIGNED':
      return 'warning'
    case 'FIX_SUBMITTED':
    case 'FIX_VERIFIED':
      return 'success'
    case 'DEADLINE_REMINDER':
      return 'danger'
    default:
      return 'info'
  }
}

const formatTime = (time: string) => {
  const now = new Date()
  const notificationTime = new Date(time)
  const diff = now.getTime() - notificationTime.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return notificationTime.toLocaleDateString()
  }
}

// 防抖函数
function debounce<T extends (...args: any[]) => any>(func: T, wait: number): T {
  let timeout: NodeJS.Timeout
  return ((...args: any[]) => {
    clearTimeout(timeout)
    timeout = setTimeout(() => func.apply(this, args), wait)
  }) as T
}

// 生命周期
onMounted(() => {
  refreshNotifications()
})

// 监听筛选条件变化
watch(() => filters.status, () => {
  pagination.page = 1
  loadNotifications()
})
</script>

<style scoped>
.notification-center {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-badge {
  margin-right: 8px;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.notification-list {
  background: white;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #ebeef5;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

.notification-items {
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f8f9fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409EFF;
}

.notification-item.selected {
  background-color: #e6f7ff;
}

.notification-item:last-child {
  border-bottom: none;
}

.item-checkbox {
  margin-right: 12px;
  margin-top: 4px;
}

.item-icon {
  margin-right: 12px;
  margin-top: 4px;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.item-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

.item-body {
  margin-bottom: 8px;
}

.item-description {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  word-break: break-word;
}

.item-footer {
  margin-top: 8px;
}

.item-actions {
  margin-left: 12px;
  margin-top: 4px;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-top: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-badge__content) {
  border: 1px solid #fff;
}
</style>