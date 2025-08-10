import request from '@/utils/request'

// 通知相关类型定义
export interface Notification {
  id: number
  recipientId: number
  recipientName?: string
  notificationType: string
  notificationTypeDesc: string
  title: string
  content: string
  relatedId?: number
  relatedType?: string
  channels: string
  status: string
  statusDesc: string
  isRead: boolean
  readAt?: string
  sentAt?: string
  createdAt: string
  retryCount: number
  errorMessage?: string
  extraData?: Record<string, any>
}

export interface NotificationRequest {
  recipientIds: number[]
  notificationType: string
  title: string
  content: string
  relatedId?: number
  relatedType?: string
  channels?: string[]
  templateVariables?: Record<string, any>
  immediate?: boolean
  delayMinutes?: number
}

export interface NotificationPreference {
  id?: number
  userId: number
  notificationType: string
  notificationTypeDesc?: string
  inAppEnabled: boolean
  emailEnabled: boolean
  wechatWorkEnabled: boolean
  smsEnabled: boolean
  quietEnabled: boolean
  quietStartTime: string
  quietEndTime: string
  frequencyLimit: number
}

export interface NotificationTemplate {
  id?: number
  templateName: string
  notificationType: string
  notificationTypeDesc?: string
  channel: string
  channelDesc?: string
  titleTemplate: string
  contentTemplate: string
  variables?: string
  isEnabled: boolean
  description?: string
  createdAt?: string
  updatedAt?: string
  renderedTitle?: string
  renderedContent?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 通知API
export const notificationApi = {
  // 发送通知
  sendNotification(notificationRequest: NotificationRequest) {
    return request.post('/notifications/send', notificationRequest)
  },

  // 获取用户通知列表
  getUserNotifications(userId: number, page: number = 1, size: number = 20, unreadOnly: boolean = false) {
    return request.get<PageResult<Notification>>(`/notifications/user/${userId}`, {
      params: { page, size, unreadOnly }
    })
  },

  // 获取当前用户通知列表
  getCurrentUserNotifications(page: number = 1, size: number = 20, unreadOnly: boolean = false) {
    return request.get<PageResult<Notification>>('/notifications/current', {
      params: { page, size, unreadOnly }
    })
  },

  // 获取用户未读通知数量
  getUnreadCount(userId: number) {
    return request.get<number>(`/notifications/unread-count/${userId}`)
  },

  // 获取当前用户未读通知数量
  getCurrentUserUnreadCount() {
    return request.get<number>('/notifications/unread-count')
  },

  // 标记通知为已读
  markAsRead(notificationIds: number[]) {
    return request.put('/notifications/mark-read', notificationIds)
  },

  // 标记所有通知为已读
  markAllAsRead() {
    return request.put('/notifications/mark-all-read')
  },

  // 删除通知
  deleteNotifications(notificationIds: number[]) {
    return request.delete('/notifications/delete', { data: notificationIds })
  },

  // 获取通知统计信息
  getNotificationStatistics(startDate: string, endDate: string) {
    return request.get('/notifications/statistics', {
      params: { startDate, endDate }
    })
  },

  // 重试失败的通知
  retryFailedNotifications() {
    return request.post('/notifications/retry-failed')
  },

  // 清理过期通知
  cleanupExpiredNotifications(daysToKeep: number = 30) {
    return request.post('/notifications/cleanup', null, {
      params: { daysToKeep }
    })
  }
}

// 通知偏好设置API
export const notificationPreferenceApi = {
  // 获取用户通知偏好设置
  getUserPreferences(userId: number) {
    return request.get<NotificationPreference[]>(`/notification-preferences/user/${userId}`)
  },

  // 获取当前用户通知偏好设置
  getCurrentUserPreferences() {
    return request.get<NotificationPreference[]>('/notification-preferences/current')
  },

  // 获取用户特定类型的通知偏好
  getUserPreference(userId: number, notificationType: string) {
    return request.get<NotificationPreference>(`/notification-preferences/user/${userId}/type/${notificationType}`)
  },

  // 更新用户通知偏好设置
  updateUserPreferences(userId: number, preferences: NotificationPreference[]) {
    return request.put(`/notification-preferences/user/${userId}`, preferences)
  },

  // 更新当前用户通知偏好设置
  updateCurrentUserPreferences(preferences: NotificationPreference[]) {
    return request.put('/notification-preferences/current', preferences)
  },

  // 更新单个通知偏好设置
  updateUserPreference(userId: number, preference: NotificationPreference) {
    return request.put(`/notification-preferences/user/${userId}/preference`, preference)
  },

  // 更新当前用户单个通知偏好设置
  updateCurrentUserPreference(preference: NotificationPreference) {
    return request.put('/notification-preferences/current/preference', preference)
  },

  // 重置用户通知偏好为默认值
  resetToDefault(userId: number) {
    return request.post(`/notification-preferences/user/${userId}/reset`)
  },

  // 重置当前用户通知偏好为默认值
  resetCurrentUserToDefault() {
    return request.post('/notification-preferences/current/reset')
  },

  // 检查用户是否启用了特定渠道的通知
  isChannelEnabled(userId: number, notificationType: string, channel: string) {
    return request.get<boolean>(`/notification-preferences/user/${userId}/channel-enabled`, {
      params: { notificationType, channel }
    })
  },

  // 获取启用了特定渠道的用户列表
  getUsersWithChannelEnabled(notificationType: string, channel: string) {
    return request.get<number[]>('/notification-preferences/users-with-channel-enabled', {
      params: { notificationType, channel }
    })
  }
}

// 通知模板API
export const notificationTemplateApi = {
  // 分页查询通知模板
  getTemplates(page: number = 1, size: number = 20, notificationType?: string, channel?: string) {
    return request.get<PageResult<NotificationTemplate>>('/notification-templates', {
      params: { page, size, notificationType, channel }
    })
  },

  // 根据ID获取模板
  getTemplate(id: number) {
    return request.get<NotificationTemplate>(`/notification-templates/${id}`)
  },

  // 创建通知模板
  createTemplate(template: NotificationTemplate) {
    return request.post<number>('/notification-templates', template)
  },

  // 更新通知模板
  updateTemplate(id: number, template: NotificationTemplate) {
    return request.put(`/notification-templates/${id}`, template)
  },

  // 删除通知模板
  deleteTemplate(id: number) {
    return request.delete(`/notification-templates/${id}`)
  },

  // 批量启用/禁用模板
  batchUpdateEnabled(ids: number[], enabled: boolean) {
    return request.put('/notification-templates/batch-update-enabled', null, {
      params: { ids: ids.join(','), enabled }
    })
  },

  // 获取所有启用的模板
  getEnabledTemplates() {
    return request.get<NotificationTemplate[]>('/notification-templates/enabled')
  },

  // 根据通知类型获取启用的模板
  getEnabledTemplatesByType(notificationType: string) {
    return request.get<NotificationTemplate[]>(`/notification-templates/enabled/type/${notificationType}`)
  },

  // 预览模板渲染效果
  previewTemplate(template: NotificationTemplate, variables: Record<string, any>) {
    return request.post<NotificationTemplate>('/notification-templates/preview', template, {
      params: variables
    })
  },

  // 初始化默认模板
  initializeDefaultTemplates() {
    return request.post('/notification-templates/initialize-defaults')
  },

  // 获取通知类型列表
  getNotificationTypes() {
    return request.get<Array<{ value: string; label: string }>>('/notification-templates/notification-types')
  },

  // 获取通知渠道列表
  getNotificationChannels() {
    return request.get<Array<{ value: string; label: string }>>('/notification-templates/notification-channels')
  }
}