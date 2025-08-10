<template>
  <div class="notification-bell">
    <el-popover
      :visible="showPopover"
      placement="bottom-end"
      :width="400"
      trigger="manual"
      popper-class="notification-popover"
    >
      <template #reference>
        <div class="bell-container" @click="togglePopover">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-button
              type="text"
              :class="{ 'bell-active': showPopover }"
              size="large"
            >
              <el-icon :size="20">
                <Bell />
              </el-icon>
            </el-button>
          </el-badge>
        </div>
      </template>

      <div class="notification-popover-content">
        <!-- 头部 -->
        <div class="popover-header">
          <h4>通知中心</h4>
          <div class="header-actions">
            <el-button
              type="text"
              size="small"
              @click="markAllAsRead"
              :disabled="unreadCount === 0"
            >
              全部已读
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="goToNotificationCenter"
            >
              查看全部
            </el-button>
          </div>
        </div>

        <!-- 通知列表 -->
        <div class="popover-body" v-loading="loading">
          <div class="notification-list" v-if="notifications.length > 0">
            <div
              v-for="notification in notifications.slice(0, 5)"
              :key="notification.id"
              class="notification-item"
              :class="{ 'unread': !notification.isRead }"
              @click="handleNotificationClick(notification)"
            >
              <div class="item-icon">
                <el-icon :size="16" :color="getNotificationIconColor(notification.notificationType)">
                  <component :is="getNotificationIcon(notification.notificationType)" />
                </el-icon>
              </div>
              <div class="item-content">
                <div class="item-title">{{ notification.title }}</div>
                <div class="item-description">{{ truncateText(notification.content, 50) }}</div>
                <div class="item-time">{{ formatRelativeTime(notification.createdAt) }}</div>
              </div>
              <div class="item-status" v-if="!notification.isRead">
                <div class="unread-dot"></div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <el-icon :size="48" color="#c0c4cc">
              <Bell />
            </el-icon>
            <p>暂无通知</p>
          </div>
        </div>

        <!-- 底部 -->
        <div class="popover-footer" v-if="notifications.length > 5">
          <el-button type="text" size="small" @click="goToNotificationCenter">
            查看更多通知 ({{ totalCount - 5 }}+)
          </el-button>
        </div>
      </div>
    </el-popover>

    <!-- 连接状态指示器 -->
    <div class="connection-status" v-if="showConnectionStatus">
      <el-tooltip :content="connectionStatusText" placement="bottom">
        <div
          class="status-indicator"
          :class="{ 'connected': isConnected, 'disconnected': !isConnected }"
        ></div>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Bell,
  Document,
  Warning,
  CircleCheck,
  Message,
  User
} from '@element-plus/icons-vue'
import { useNotification } from '@/composables/useNotification'
import type { Notification } from '@/api/notification'

const router = useRouter()
const { notificationState, loading, loadNotifications, markAsRead, markAllAsRead } = useNotification()

// 响应式数据
const showPopover = ref(false)
const showConnectionStatus = ref(false)

// 计算属性
const unreadCount = computed(() => notificationState.unreadCount)
const notifications = computed(() => notificationState.notifications)
const totalCount = computed(() => notificationState.notifications.length)
const isConnected = computed(() => notificationState.isConnected)

const connectionStatusText = computed(() => {
  return isConnected.value ? '实时通知已连接' : '实时通知连接中断'
})

// 方法
const togglePopover = async () => {
  if (!showPopover.value) {
    // 打开时加载最新通知
    try {
      await loadNotifications(1, 10)
    } catch (error) {
      console.error('加载通知失败:', error)
    }
  }
  showPopover.value = !showPopover.value
}

const handleNotificationClick = async (notification: Notification) => {
  // 标记为已读
  if (!notification.isRead) {
    try {
      await markAsRead([notification.id])
    } catch (error) {
      console.error('标记通知为已读失败:', error)
    }
  }

  // 关闭弹窗
  showPopover.value = false

  // 跳转到相关页面
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
    case 'SUMMARY':
      router.push(`/summaries/${notification.relatedId}`)
      break
    default:
      ElMessage.info('暂不支持跳转到该类型的详情页面')
  }
}

const goToNotificationCenter = () => {
  showPopover.value = false
  router.push('/notifications')
}

const handleMarkAllAsRead = async () => {
  try {
    await markAllAsRead()
    ElMessage.success('所有通知已标记为已读')
  } catch (error) {
    ElMessage.error('标记所有通知为已读失败')
  }
}

// 添加键盘快捷键支持
const handleKeydown = (event: KeyboardEvent) => {
  // Ctrl/Cmd + Shift + N 打开通知中心
  if ((event.ctrlKey || event.metaKey) && event.shiftKey && event.key === 'N') {
    event.preventDefault()
    goToNotificationCenter()
  }
  // Escape 关闭弹窗
  if (event.key === 'Escape' && showPopover.value) {
    showPopover.value = false
  }
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

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) {
    return text
  }
  return text.substring(0, maxLength) + '...'
}

const formatRelativeTime = (time: string) => {
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

// 点击外部关闭弹窗
const handleClickOutside = (event: Event) => {
  const target = event.target as Element
  if (!target.closest('.notification-bell')) {
    showPopover.value = false
  }
}

// 监听连接状态变化
watch(isConnected, (connected) => {
  if (!connected) {
    showConnectionStatus.value = true
    // 5秒后隐藏状态指示器
    setTimeout(() => {
      showConnectionStatus.value = false
    }, 5000)
  } else {
    showConnectionStatus.value = false
  }
})

// 生命周期
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  display: inline-block;
}

.bell-container {
  cursor: pointer;
}

.bell-active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.notification-popover-content {
  padding: 0;
}

.popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.popover-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.popover-body {
  max-height: 400px;
  overflow-y: auto;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
}

.item-icon {
  margin-right: 12px;
  margin-top: 2px;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1.4;
}

.item-description {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
  word-break: break-word;
}

.item-time {
  font-size: 11px;
  color: #909399;
}

.item-status {
  margin-left: 8px;
  margin-top: 6px;
  flex-shrink: 0;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #409EFF;
  border-radius: 50%;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-state p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.popover-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  text-align: center;
}

.connection-status {
  position: absolute;
  top: -2px;
  right: -2px;
  z-index: 10;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 1px solid #fff;
}

.status-indicator.connected {
  background-color: #67c23a;
}

.status-indicator.disconnected {
  background-color: #f56c6c;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0.3;
  }
}

:deep(.el-badge__content) {
  border: 1px solid #fff;
  font-size: 11px;
  height: 16px;
  line-height: 16px;
  padding: 0 5px;
  min-width: 16px;
}

:deep(.el-button--text) {
  padding: 8px;
  border-radius: 6px;
}
</style>

<style>
.notification-popover {
  padding: 0 !important;
}

.notification-popover .el-popover__title {
  display: none;
}
</style>