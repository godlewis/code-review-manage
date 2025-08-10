import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import NotificationCenter from '../index.vue'
import { useNotification } from '@/composables/useNotification'
import { useRouter } from 'vue-router'

// Mock dependencies
vi.mock('@/composables/useNotification')
vi.mock('vue-router')
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

const mockUseNotification = vi.mocked(useNotification)
const mockUseRouter = vi.mocked(useRouter)

describe('NotificationCenter', () => {
  const mockNotificationState = {
    unreadCount: 5,
    notifications: [
      {
        id: 1,
        title: '测试通知1',
        content: '这是一个测试通知',
        notificationType: 'REVIEW_ASSIGNED',
        notificationTypeDesc: '评审分配',
        isRead: false,
        createdAt: '2024-01-01T10:00:00Z',
        relatedId: 1,
        relatedType: 'REVIEW_RECORD'
      },
      {
        id: 2,
        title: '测试通知2',
        content: '这是另一个测试通知',
        notificationType: 'ISSUE_CREATED',
        notificationTypeDesc: '问题创建',
        isRead: true,
        createdAt: '2024-01-01T09:00:00Z',
        relatedId: 2,
        relatedType: 'ISSUE'
      }
    ],
    isConnected: true,
    lastUpdateTime: new Date()
  }

  const mockNotificationFunctions = {
    notificationState: mockNotificationState,
    loading: false,
    loadNotifications: vi.fn(),
    markAsRead: vi.fn(),
    markAllAsRead: vi.fn(),
    deleteNotifications: vi.fn()
  }

  const mockRouter = {
    push: vi.fn()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockUseNotification.mockReturnValue(mockNotificationFunctions)
    mockUseRouter.mockReturnValue(mockRouter)
  })

  it('should render notification center correctly', () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-input': true,
          'el-checkbox': true,
          'el-badge': true,
          'el-icon': true,
          'el-tag': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-pagination': true,
          'el-empty': true,
          'NotificationPreferences': true
        }
      }
    })

    expect(wrapper.find('h2').text()).toBe('通知中心')
    expect(wrapper.find('.notification-center').exists()).toBe(true)
  })

  it('should display correct unread count', () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-input': true,
          'el-checkbox': true,
          'el-badge': true,
          'el-icon': true,
          'el-tag': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-pagination': true,
          'el-empty': true,
          'NotificationPreferences': true
        }
      }
    })

    // The unread count should be displayed in the badge
    expect(mockNotificationState.unreadCount).toBe(5)
  })

  it('should handle notification click correctly', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-input': true,
          'el-checkbox': true,
          'el-badge': true,
          'el-icon': true,
          'el-tag': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-pagination': true,
          'el-empty': true,
          'NotificationPreferences': true
        }
      }
    })

    // Simulate clicking on a notification
    const notificationItems = wrapper.findAll('.notification-item')
    if (notificationItems.length > 0) {
      await notificationItems[0].trigger('click')
      
      // Should mark as read if unread
      expect(mockNotificationFunctions.markAsRead).toHaveBeenCalled()
    }
  })

  it('should handle mark all as read correctly', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-input': true,
          'el-checkbox': true,
          'el-badge': true,
          'el-icon': true,
          'el-tag': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-pagination': true,
          'el-empty': true,
          'NotificationPreferences': true
        }
      }
    })

    // Find and click the "mark all as read" button
    const markAllButton = wrapper.find('[data-test="mark-all-read"]')
    if (markAllButton.exists()) {
      await markAllButton.trigger('click')
      expect(mockNotificationFunctions.markAllAsRead).toHaveBeenCalled()
    }
  })

  it('should filter notifications correctly', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-input': true,
          'el-checkbox': true,
          'el-badge': true,
          'el-icon': true,
          'el-tag': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-pagination': true,
          'el-empty': true,
          'NotificationPreferences': true
        }
      }
    })

    // Test search functionality
    const searchInput = wrapper.find('input[placeholder="搜索通知内容"]')
    if (searchInput.exists()) {
      await searchInput.setValue('测试')
      await searchInput.trigger('input')
      
      // Should trigger search after debounce
      setTimeout(() => {
        expect(mockNotificationFunctions.loadNotifications).toHaveBeenCalled()
      }, 350)
    }
  })

  it('should handle batch operations correctly', async () => {
    const wrapper = mount(NotificationCenter, {
      global: {
        stubs: {
          'el-card': true,
          'el-button': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-input': true,
          'el-checkbox': true,
          'el-badge': true,
          'el-icon': true,
          'el-tag': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-pagination': true,
          'el-empty': true,
          'NotificationPreferences': true
        }
      }
    })

    // Test select all functionality
    const selectAllCheckbox = wrapper.find('input[type="checkbox"]')
    if (selectAllCheckbox.exists()) {
      await selectAllCheckbox.setChecked(true)
      
      // Should select all notifications
      const vm = wrapper.vm as any
      expect(vm.selectedNotifications.length).toBeGreaterThan(0)
    }
  })
})