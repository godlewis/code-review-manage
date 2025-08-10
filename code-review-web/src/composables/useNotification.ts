import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElNotification } from 'element-plus'
import { notificationApi, type Notification } from '@/api/notification'

// 通知状态管理
const notificationState = reactive({
  unreadCount: 0,
  notifications: [] as Notification[],
  isConnected: false,
  lastUpdateTime: null as Date | null
})

// WebSocket连接
let websocket: WebSocket | null = null
let heartbeatTimer: NodeJS.Timeout | null = null
let reconnectTimer: NodeJS.Timeout | null = null
let reconnectAttempts = 0
const maxReconnectAttempts = 5

/**
 * 实时通知组合式函数
 */
export function useNotification() {
  const loading = ref(false)

  // 初始化WebSocket连接
  const initWebSocket = () => {
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      return
    }

    try {
      // 获取WebSocket URL（根据环境配置）
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsHost = process.env.NODE_ENV === 'development' 
        ? 'localhost:8080' 
        : window.location.host
      const wsUrl = `${wsProtocol}//${wsHost}/ws/notifications`

      websocket = new WebSocket(wsUrl)

      websocket.onopen = () => {
        console.log('WebSocket连接已建立')
        notificationState.isConnected = true
        reconnectAttempts = 0
        
        // 发送认证信息
        const token = localStorage.getItem('token')
        if (token) {
          websocket?.send(JSON.stringify({
            type: 'auth',
            token: token
          }))
        }

        // 启动心跳
        startHeartbeat()
      }

      websocket.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          handleWebSocketMessage(message)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      websocket.onclose = (event) => {
        console.log('WebSocket连接已关闭:', event.code, event.reason)
        notificationState.isConnected = false
        stopHeartbeat()
        
        // 如果不是主动关闭，尝试重连
        if (event.code !== 1000 && reconnectAttempts < maxReconnectAttempts) {
          scheduleReconnect()
        }
      }

      websocket.onerror = (error) => {
        console.error('WebSocket连接错误:', error)
        notificationState.isConnected = false
      }

    } catch (error) {
      console.error('初始化WebSocket失败:', error)
    }
  }

  // 处理WebSocket消息
  const handleWebSocketMessage = (message: any) => {
    switch (message.type) {
      case 'notification':
        handleNewNotification(message.data)
        break
      case 'unread_count':
        notificationState.unreadCount = message.data
        break
      case 'notification_read':
        handleNotificationRead(message.data)
        break
      case 'pong':
        // 心跳响应，无需处理
        break
      default:
        console.log('未知的WebSocket消息类型:', message.type)
    }
  }

  // 处理新通知
  const handleNewNotification = (notification: Notification) => {
    // 更新通知列表
    notificationState.notifications.unshift(notification)
    notificationState.unreadCount++
    notificationState.lastUpdateTime = new Date()

    // 显示桌面通知
    showDesktopNotification(notification)

    // 显示页面内通知
    showInPageNotification(notification)

    // 播放通知声音
    playNotificationSound()
  }

  // 处理通知已读
  const handleNotificationRead = (data: { notificationIds: number[] }) => {
    data.notificationIds.forEach(id => {
      const notification = notificationState.notifications.find(n => n.id === id)
      if (notification && !notification.isRead) {
        notification.isRead = true
        notification.readAt = new Date().toISOString()
        notificationState.unreadCount = Math.max(0, notificationState.unreadCount - 1)
      }
    })
  }

  // 显示桌面通知
  const showDesktopNotification = (notification: Notification) => {
    if (!('Notification' in window)) {
      return
    }

    if (Notification.permission === 'granted') {
      const desktopNotification = new Notification(notification.title, {
        body: notification.content,
        icon: '/favicon.ico',
        tag: `notification-${notification.id}`,
        requireInteraction: false
      })

      desktopNotification.onclick = () => {
        window.focus()
        // 可以在这里添加跳转到相关页面的逻辑
        desktopNotification.close()
      }

      // 5秒后自动关闭
      setTimeout(() => {
        desktopNotification.close()
      }, 5000)
    }
  }

  // 显示页面内通知
  const showInPageNotification = (notification: Notification) => {
    const notificationTypeMap: Record<string, any> = {
      'REVIEW_ASSIGNED': { type: 'info', title: '新的评审任务' },
      'ISSUE_CREATED': { type: 'warning', title: '发现新问题' },
      'FIX_VERIFIED': { type: 'success', title: '整改已验证' },
      'DEADLINE_REMINDER': { type: 'error', title: '截止提醒' },
      'SYSTEM_ANNOUNCEMENT': { type: 'info', title: '系统公告' }
    }

    const config = notificationTypeMap[notification.notificationType] || 
                  { type: 'info', title: '新通知' }

    ElNotification({
      title: config.title,
      message: notification.content,
      type: config.type,
      duration: 4000,
      showClose: true,
      onClick: () => {
        // 可以在这里添加点击通知的处理逻辑
      }
    })
  }

  // 播放通知声音
  const playNotificationSound = () => {
    try {
      const audio = new Audio('/sounds/notification.mp3')
      audio.volume = 0.3
      audio.play().catch(error => {
        console.log('播放通知声音失败:', error)
      })
    } catch (error) {
      console.log('创建音频对象失败:', error)
    }
  }

  // 启动心跳
  const startHeartbeat = () => {
    heartbeatTimer = setInterval(() => {
      if (websocket && websocket.readyState === WebSocket.OPEN) {
        websocket.send(JSON.stringify({ type: 'ping' }))
      }
    }, 30000) // 30秒心跳
  }

  // 停止心跳
  const stopHeartbeat = () => {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  // 计划重连
  const scheduleReconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }

    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000) // 指数退避，最大30秒
    reconnectAttempts++

    console.log(`${delay / 1000}秒后尝试重连 (第${reconnectAttempts}次)`)

    reconnectTimer = setTimeout(() => {
      initWebSocket()
    }, delay)
  }

  // 关闭WebSocket连接
  const closeWebSocket = () => {
    if (websocket) {
      websocket.close(1000, '主动关闭')
      websocket = null
    }
    stopHeartbeat()
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  // 请求桌面通知权限
  const requestNotificationPermission = async () => {
    if (!('Notification' in window)) {
      console.log('此浏览器不支持桌面通知')
      return false
    }

    if (Notification.permission === 'granted') {
      return true
    }

    if (Notification.permission !== 'denied') {
      const permission = await Notification.requestPermission()
      return permission === 'granted'
    }

    return false
  }

  // 加载通知列表
  const loadNotifications = async (page: number = 1, size: number = 20, unreadOnly: boolean = false) => {
    loading.value = true
    try {
      const { data } = await notificationApi.getCurrentUserNotifications(page, size, unreadOnly)
      if (page === 1) {
        notificationState.notifications = data.records
      } else {
        notificationState.notifications.push(...data.records)
      }
      return data
    } catch (error) {
      console.error('加载通知列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 加载未读通知数量
  const loadUnreadCount = async () => {
    try {
      const { data } = await notificationApi.getCurrentUserUnreadCount()
      notificationState.unreadCount = data
    } catch (error) {
      console.error('加载未读通知数量失败:', error)
    }
  }

  // 标记通知为已读
  const markAsRead = async (notificationIds: number[]) => {
    try {
      await notificationApi.markAsRead(notificationIds)
      
      // 更新本地状态
      notificationIds.forEach(id => {
        const notification = notificationState.notifications.find(n => n.id === id)
        if (notification && !notification.isRead) {
          notification.isRead = true
          notification.readAt = new Date().toISOString()
          notificationState.unreadCount = Math.max(0, notificationState.unreadCount - 1)
        }
      })

      // 通过WebSocket通知其他客户端
      if (websocket && websocket.readyState === WebSocket.OPEN) {
        websocket.send(JSON.stringify({
          type: 'mark_read',
          data: { notificationIds }
        }))
      }
    } catch (error) {
      console.error('标记通知为已读失败:', error)
      throw error
    }
  }

  // 标记所有通知为已读
  const markAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead()
      
      // 更新本地状态
      notificationState.notifications.forEach(notification => {
        if (!notification.isRead) {
          notification.isRead = true
          notification.readAt = new Date().toISOString()
        }
      })
      notificationState.unreadCount = 0

      // 通过WebSocket通知其他客户端
      if (websocket && websocket.readyState === WebSocket.OPEN) {
        websocket.send(JSON.stringify({
          type: 'mark_all_read'
        }))
      }
    } catch (error) {
      console.error('标记所有通知为已读失败:', error)
      throw error
    }
  }

  // 删除通知
  const deleteNotifications = async (notificationIds: number[]) => {
    try {
      await notificationApi.deleteNotifications(notificationIds)
      
      // 更新本地状态
      notificationState.notifications = notificationState.notifications.filter(
        n => !notificationIds.includes(n.id)
      )
    } catch (error) {
      console.error('删除通知失败:', error)
      throw error
    }
  }

  // 初始化
  const initialize = async () => {
    await Promise.all([
      loadUnreadCount(),
      requestNotificationPermission()
    ])
    initWebSocket()
  }

  // 清理
  const cleanup = () => {
    closeWebSocket()
  }

  return {
    // 状态
    notificationState,
    loading,

    // 方法
    initialize,
    cleanup,
    loadNotifications,
    loadUnreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotifications,
    requestNotificationPermission,

    // WebSocket相关
    initWebSocket,
    closeWebSocket
  }
}

// 全局通知管理器
export const globalNotificationManager = {
  instance: null as ReturnType<typeof useNotification> | null,

  getInstance() {
    if (!this.instance) {
      this.instance = useNotification()
    }
    return this.instance
  },

  async initialize() {
    const instance = this.getInstance()
    await instance.initialize()
  },

  cleanup() {
    if (this.instance) {
      this.instance.cleanup()
      this.instance = null
    }
  }
}