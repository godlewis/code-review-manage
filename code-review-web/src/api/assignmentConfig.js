import request from '@/utils/request'

export const assignmentConfigApi = {
  // 获取当前配置
  getCurrentConfig() {
    return request({
      url: '/api/assignment-config',
      method: 'get'
    })
  },

  // 获取配置摘要
  getConfigSummary() {
    return request({
      url: '/api/assignment-config/summary',
      method: 'get'
    })
  },

  // 验证配置
  validateConfig(config) {
    return request({
      url: '/api/assignment-config/validate',
      method: 'post',
      data: config
    })
  },

  // 更新配置
  updateConfig(config) {
    return request({
      url: '/api/assignment-config',
      method: 'put',
      data: config
    })
  },

  // 检查配对规则
  checkPairRules(reviewerId, revieweeId) {
    return request({
      url: '/api/assignment-config/check-pair',
      method: 'get',
      params: {
        reviewerId,
        revieweeId
      }
    })
  },

  // 应用特殊规则过滤用户
  filterUsers(userIds, ruleType) {
    return request({
      url: '/api/assignment-config/filter-users',
      method: 'post',
      data: userIds,
      params: {
        ruleType
      }
    })
  },

  // 获取团队特殊配置
  getTeamSpecialConfig(teamId) {
    return request({
      url: `/api/assignment-config/team/${teamId}`,
      method: 'get'
    })
  },

  // 获取用户特殊配置
  getUserSpecialConfig(userId) {
    return request({
      url: `/api/assignment-config/user/${userId}`,
      method: 'get'
    })
  },

  // 获取排除配对列表
  getExcludePairs() {
    return request({
      url: '/api/assignment-config/exclude-pairs',
      method: 'get'
    })
  },

  // 获取强制配对列表
  getForcePairs() {
    return request({
      url: '/api/assignment-config/force-pairs',
      method: 'get'
    })
  },

  // 获取配置统计信息
  getConfigStatistics() {
    return request({
      url: '/api/assignment-config/statistics',
      method: 'get'
    })
  },

  // 更新团队特殊配置
  updateTeamSpecialConfig(teamId, teamConfig) {
    return request({
      url: `/api/assignment-config/team/${teamId}`,
      method: 'put',
      data: teamConfig
    })
  },

  // 更新用户特殊配置
  updateUserSpecialConfig(userId, userConfig) {
    return request({
      url: `/api/assignment-config/user/${userId}`,
      method: 'put',
      data: userConfig
    })
  },

  // 更新排除配对规则
  updateExcludePairs(excludePairs) {
    return request({
      url: '/api/assignment-config/exclude-pairs',
      method: 'put',
      data: excludePairs
    })
  },

  // 更新强制配对规则
  updateForcePairs(forcePairs) {
    return request({
      url: '/api/assignment-config/force-pairs',
      method: 'put',
      data: forcePairs
    })
  },

  // 删除团队特殊配置
  deleteTeamSpecialConfig(teamId) {
    return request({
      url: `/api/assignment-config/team/${teamId}`,
      method: 'delete'
    })
  },

  // 删除用户特殊配置
  deleteUserSpecialConfig(userId) {
    return request({
      url: `/api/assignment-config/user/${userId}`,
      method: 'delete'
    })
  },

  // 获取所有动态配置
  getAllDynamicConfigs() {
    return request({
      url: '/api/assignment-config/dynamic',
      method: 'get'
    })
  },

  // 根据类型获取动态配置
  getDynamicConfigsByType(configType) {
    return request({
      url: `/api/assignment-config/dynamic/${configType}`,
      method: 'get'
    })
  },

  // 获取合并后的完整配置
  getMergedConfig() {
    return request({
      url: '/api/assignment-config/merged',
      method: 'get'
    })
  }
}