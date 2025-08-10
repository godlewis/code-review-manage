# 通知中心实现文档

## 概述

通知中心是代码评审管理系统的核心功能模块，提供完整的通知管理、实时推送和用户偏好设置功能。

## 功能特性

### 1. 通知中心页面 (`/notifications`)

#### 主要功能
- **通知列表展示**: 分页显示用户的所有通知
- **实时更新**: 通过WebSocket实现通知的实时推送
- **筛选和搜索**: 支持按类型、状态、时间范围和关键词筛选
- **批量操作**: 支持批量标记已读和删除
- **移动端适配**: 响应式设计，支持移动设备访问

#### 筛选功能
- **通知类型**: 评审分配、问题创建、整改提交等
- **阅读状态**: 全部、未读、已读
- **时间范围**: 日期范围选择器
- **关键词搜索**: 实时搜索通知标题和内容

#### 批量操作
- **全选/取消全选**: 支持选择所有通知
- **批量标记已读**: 一键标记选中通知为已读
- **批量删除**: 删除选中的通知

### 2. 通知偏好设置

#### 通知类型设置
- **接收渠道配置**: 站内信、邮件、企业微信、短信
- **频率限制**: 设置通知发送的最小间隔时间
- **开关控制**: 可以完全关闭某类通知

#### 免打扰设置
- **时间段设置**: 配置免打扰的开始和结束时间
- **全局开关**: 启用/禁用免打扰功能

#### 高级设置
- **通知声音**: 控制是否播放提示音
- **桌面通知**: 控制浏览器桌面通知
- **通知预览**: 控制通知内容预览
- **自动清理**: 设置已读通知的自动清理时间

### 3. 实时通知功能

#### WebSocket连接
- **自动连接**: 页面加载时自动建立WebSocket连接
- **心跳检测**: 30秒间隔的心跳保持连接活跃
- **断线重连**: 指数退避算法实现自动重连
- **连接状态指示**: 显示连接状态给用户

#### 实时推送
- **新通知推送**: 实时接收新通知
- **状态同步**: 多端同步已读状态
- **桌面通知**: 支持浏览器原生桌面通知
- **声音提醒**: 可配置的通知提示音

### 4. 通知铃铛组件

#### 功能特性
- **未读数量显示**: 实时显示未读通知数量
- **快速预览**: 悬浮窗显示最近5条通知
- **快速操作**: 支持快速标记已读和跳转
- **键盘快捷键**: Ctrl+Shift+N 快速打开通知中心

#### 交互设计
- **点击展开**: 点击铃铛图标展开通知预览
- **外部点击关闭**: 点击其他区域自动关闭
- **连接状态指示**: 显示WebSocket连接状态

## 技术实现

### 1. 组件结构

```
src/views/notifications/
├── index.vue                          # 通知中心主页面
├── components/
│   └── NotificationPreferences.vue    # 通知偏好设置组件
└── __tests__/                         # 测试文件
    ├── NotificationCenter.test.ts
    └── components/
        └── NotificationPreferences.test.ts
```

### 2. API接口

#### 通知API (`notificationApi`)
- `getCurrentUserNotifications()`: 获取当前用户通知列表
- `getCurrentUserUnreadCount()`: 获取未读通知数量
- `markAsRead()`: 标记通知为已读
- `markAllAsRead()`: 标记所有通知为已读
- `deleteNotifications()`: 删除通知

#### 通知偏好API (`notificationPreferenceApi`)
- `getCurrentUserPreferences()`: 获取用户偏好设置
- `updateCurrentUserPreferences()`: 更新偏好设置
- `resetCurrentUserToDefault()`: 重置为默认设置

### 3. 状态管理

#### 全局通知状态 (`useNotification`)
```typescript
const notificationState = reactive({
  unreadCount: 0,                    // 未读数量
  notifications: [],                 // 通知列表
  isConnected: false,               // WebSocket连接状态
  lastUpdateTime: null              // 最后更新时间
})
```

#### 主要方法
- `initialize()`: 初始化通知功能
- `loadNotifications()`: 加载通知列表
- `markAsRead()`: 标记已读
- `deleteNotifications()`: 删除通知

### 4. WebSocket实现

#### 连接管理
```typescript
// WebSocket URL配置
const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
const wsUrl = `${wsProtocol}//${wsHost}/ws/notifications`

// 消息类型处理
switch (message.type) {
  case 'notification': handleNewNotification(message.data)
  case 'unread_count': updateUnreadCount(message.data)
  case 'notification_read': handleNotificationRead(message.data)
}
```

#### 重连机制
- **指数退避**: 重连间隔逐渐增加，最大30秒
- **最大重试**: 最多重试5次
- **状态指示**: 显示连接状态给用户

### 5. 移动端适配

#### 响应式断点
- **768px以下**: 平板适配
- **480px以下**: 手机适配

#### 适配特性
- **布局调整**: 垂直布局，优化触摸操作
- **字体大小**: 适配小屏幕的字体大小
- **交互优化**: 增大点击区域，优化手势操作

## 使用方法

### 1. 基本使用

```vue
<template>
  <!-- 在布局中使用通知铃铛 -->
  <NotificationBell />
</template>

<script setup>
import NotificationBell from '@/components/NotificationBell.vue'
</script>
```

### 2. 编程式操作

```typescript
import { useNotification } from '@/composables/useNotification'

const { 
  notificationState, 
  loadNotifications, 
  markAsRead, 
  markAllAsRead 
} = useNotification()

// 加载通知
await loadNotifications(1, 20)

// 标记已读
await markAsRead([1, 2, 3])

// 标记所有已读
await markAllAsRead()
```

### 3. 路由配置

```typescript
{
  path: '/notifications',
  name: 'Notifications',
  component: () => import('@/views/notifications/index.vue'),
  meta: { title: '通知中心', icon: 'Bell' }
}
```

## 键盘快捷键

- **Ctrl+Shift+N**: 打开通知中心
- **Escape**: 关闭通知弹窗

## 浏览器兼容性

- **Chrome**: 60+
- **Firefox**: 55+
- **Safari**: 12+
- **Edge**: 79+

## 性能优化

### 1. 虚拟滚动
- 大量通知时使用虚拟滚动提升性能

### 2. 防抖搜索
- 搜索输入使用300ms防抖，减少API调用

### 3. 缓存策略
- 通知列表使用内存缓存
- 偏好设置使用localStorage缓存

### 4. 懒加载
- 通知详情按需加载
- 图片懒加载

## 安全考虑

### 1. XSS防护
- 通知内容进行HTML转义
- 使用v-text而非v-html显示用户内容

### 2. CSRF防护
- API请求包含CSRF token
- WebSocket连接验证用户身份

### 3. 权限控制
- 用户只能访问自己的通知
- 敏感操作需要权限验证

## 错误处理

### 1. 网络错误
- API调用失败时显示友好错误信息
- 支持手动重试

### 2. WebSocket错误
- 连接失败时自动重连
- 显示连接状态给用户

### 3. 数据错误
- 数据格式错误时使用默认值
- 异常数据不影响整体功能

## 测试

### 1. 单元测试
- 组件渲染测试
- 用户交互测试
- API调用测试

### 2. 集成测试
- WebSocket连接测试
- 端到端流程测试

### 3. 性能测试
- 大量通知加载测试
- 内存泄漏检测

## 部署注意事项

### 1. WebSocket配置
- 确保服务器支持WebSocket
- 配置正确的WebSocket路径

### 2. 静态资源
- 通知声音文件需要正确部署
- 图标资源需要可访问

### 3. 环境变量
- 配置正确的API地址
- 配置WebSocket服务地址

## 后续优化

### 1. 功能增强
- 通知分组功能
- 通知模板自定义
- 通知统计分析

### 2. 性能优化
- 服务端推送优化
- 客户端缓存策略
- 数据库查询优化

### 3. 用户体验
- 动画效果优化
- 交互反馈改进
- 无障碍访问支持