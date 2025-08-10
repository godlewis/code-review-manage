import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import NotificationPreferences from '../NotificationPreferences.vue'
import { notificationPreferenceApi } from '@/api/notification'

// Mock dependencies
vi.mock('@/api/notification')
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

const mockNotificationPreferenceApi = vi.mocked(notificationPreferenceApi)

describe('NotificationPreferences', () => {
  const mockPreferences = [
    {
      id: 1,
      userId: 1,
      notificationType: 'REVIEW_ASSIGNED',
      notificationTypeDesc: '评审分配通知',
      inAppEnabled: true,
      emailEnabled: true,
      wechatWorkEnabled: false,
      smsEnabled: false,
      quietEnabled: false,
      quietStartTime: '22:00',
      quietEndTime: '08:00',
      frequencyLimit: 60
    },
    {
      id: 2,
      userId: 1,
      notificationType: 'ISSUE_CREATED',
      notificationTypeDesc: '问题创建通知',
      inAppEnabled: true,
      emailEnabled: false,
      wechatWorkEnabled: true,
      smsEnabled: false,
      quietEnabled: false,
      quietStartTime: '22:00',
      quietEndTime: '08:00',
      frequencyLimit: 30
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    mockNotificationPreferenceApi.getCurrentUserPreferences.mockResolvedValue({
      data: mockPreferences
    })
    mockNotificationPreferenceApi.updateCurrentUserPreferences.mockResolvedValue({})
    mockNotificationPreferenceApi.resetCurrentUserToDefault.mockResolvedValue({})
  })

  it('should render notification preferences dialog correctly', () => {
    const wrapper = mount(NotificationPreferences, {
      props: {
        visible: true
      },
      global: {
        stubs: {
          'el-dialog': true,
          'el-alert': true,
          'el-tabs': true,
          'el-tab-pane': true,
          'el-switch': true,
          'el-checkbox': true,
          'el-input-number': true,
          'el-time-picker': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
          'el-button': true
        }
      }
    })

    expect(wrapper.find('.preferences-content').exists()).toBe(true)
  })

  it('should load preferences when dialog opens', async () => {
    const wrapper = mount(NotificationPreferences, {
      props: {
        visible: false
      },
      global: {
        stubs: {
          'el-dialog': true,
          'el-alert': true,
          'el-tabs': true,
          'el-tab-pane': true,
          'el-switch': true,
          'el-checkbox': true,
          'el-input-number': true,
          'el-time-picker': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
          'el-button': true
        }
      }
    })

    // Change visible prop to true
    await wrapper.setProps({ visible: true })

    expect(mockNotificationPreferenceApi.getCurrentUserPreferences).toHaveBeenCalled()
  })

  it('should handle preference changes correctly', async () => {
    const wrapper = mount(NotificationPreferences, {
      props: {
        visible: true
      },
      global: {
        stubs: {
          'el-dialog': true,
          'el-alert': true,
          'el-tabs': true,
          'el-tab-pane': true,
          'el-switch': true,
          'el-checkbox': true,
          'el-input-number': true,
          'el-time-picker': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
          'el-button': true
        }
      }
    })

    // Wait for preferences to load
    await wrapper.vm.$nextTick()

    const vm = wrapper.vm as any
    
    // Test preference change validation
    const testPreference = {
      ...mockPreferences[0],
      inAppEnabled: false,
      emailEnabled: false,
      wechatWorkEnabled: false,
      smsEnabled: false
    }

    vm.handlePreferenceChange(testPreference)

    // Should ensure at least one channel is enabled
    expect(testPreference.inAppEnabled).toBe(true)
  })

  it('should save preferences correctly', async () => {
    const wrapper = mount(NotificationPreferences, {
      props: {
        visible: true
      },
      global: {
        stubs: {
          'el-dialog': true,
          'el-alert': true,
          'el-tabs': true,
          'el-tab-pane': true,
          'el-switch': true,
          'el-checkbox': true,
          'el-input-number': true,
          'el-time-picker': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
          'el-button': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Simulate saving preferences
    await vm.handleSave()

    expect(mockNotificationPreferenceApi.updateCurrentUserPreferences).toHaveBeenCalled()
    expect(ElMessage.success).toHaveBeenCalledWith('通知偏好设置已保存')
  })

  it('should reset preferences to default correctly', async () => {
    const wrapper = mount(NotificationPreferences, {
      props: {
        visible: true
      },
      global: {
        stubs: {
          'el-dialog': true,
          'el-alert': true,
          'el-tabs': true,
          'el-tab-pane': true,
          'el-switch': true,
          'el-checkbox': true,
          'el-input-number': true,
          'el-time-picker': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
          'el-button': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Mock the confirmation dialog
    const mockConfirm = vi.fn().mockResolvedValue(true)
    vi.doMock('element-plus', () => ({
      ElMessageBox: {
        confirm: mockConfirm
      }
    }))

    await vm.handleReset()

    expect(mockNotificationPreferenceApi.resetCurrentUserToDefault).toHaveBeenCalled()
  })

  it('should handle quiet time settings correctly', async () => {
    const wrapper = mount(NotificationPreferences, {
      props: {
        visible: true
      },
      global: {
        stubs: {
          'el-dialog': true,
          'el-alert': true,
          'el-tabs': true,
          'el-tab-pane': true,
          'el-switch': true,
          'el-checkbox': true,
          'el-input-number': true,
          'el-time-picker': true,
          'el-select': true,
          'el-option': true,
          'el-form': true,
          'el-form-item': true,
          'el-button': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Test quiet time setting change
    vm.quietSettings.enabled = true
    vm.quietSettings.startTime = '23:00'
    vm.quietSettings.endTime = '07:00'
    
    vm.handleQuietSettingChange()

    // Should update all preferences with quiet time settings
    expect(vm.preferences.every((p: any) => p.quietEnabled === true)).toBe(true)
    expect(vm.preferences.every((p: any) => p.quietStartTime === '23:00')).toBe(true)
    expect(vm.preferences.every((p: any) => p.quietEndTime === '07:00')).toBe(true)
  })
})