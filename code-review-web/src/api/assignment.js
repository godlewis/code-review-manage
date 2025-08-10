import request from '@/utils/request'

export const assignmentApi = {
  // 获取分配列表
  getAssignments(params) {
    return request({
      url: '/api/review-assignments',
      method: 'get',
      params
    })
  },

  // 获取分配统计信息
  getStatistics(params) {
    return request({
      url: '/api/review-assignments/statistics',
      method: 'get',
      params
    })
  },

  // 生成周度分配
  generateWeeklyAssignments(teamId, weekStart) {
    return request({
      url: '/api/review-assignments/generate',
      method: 'post',
      params: {
        teamId,
        weekStart
      }
    })
  },

  // 批量生成多周分配
  generateBatchAssignments(teamId, startWeek, weekCount) {
    return request({
      url: '/api/review-assignments/generate-batch',
      method: 'post',
      params: {
        teamId,
        startWeek,
        weekCount
      }
    })
  },

  // 预览分配结果
  previewAssignments(teamId, weekStart) {
    return request({
      url: '/api/review-assignments/preview',
      method: 'post',
      params: {
        teamId,
        weekStart
      }
    })
  },

  // 查询团队分配历史
  getTeamAssignmentHistory(teamId, startDate, endDate) {
    return request({
      url: `/api/review-assignments/team/${teamId}/history`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },

  // 查询用户分配历史
  getUserAssignmentHistory(userId, startDate, endDate) {
    return request({
      url: `/api/review-assignments/user/${userId}/history`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },

  // 查询当前周分配
  getCurrentWeekAssignments(teamId) {
    return request({
      url: '/api/review-assignments/current-week',
      method: 'get',
      params: {
        teamId
      }
    })
  },

  // 获取我的当前分配
  getMyCurrentAssignments() {
    return request({
      url: '/api/review-assignments/my-current',
      method: 'get'
    })
  },

  // 获取分配详情
  getAssignmentDetail(assignmentId) {
    return request({
      url: `/api/review-assignments/${assignmentId}`,
      method: 'get'
    })
  },

  // 手动调整分配
  adjustAssignment(assignmentId, data) {
    return request({
      url: `/api/review-assignments/${assignmentId}/adjust`,
      method: 'put',
      data
    })
  },

  // 批量调整分配
  batchAdjustAssignments(requests) {
    return request({
      url: '/api/review-assignments/batch-adjust',
      method: 'put',
      data: requests
    })
  },

  // 更新分配状态
  updateAssignmentStatus(assignmentId, status) {
    return request({
      url: `/api/review-assignments/${assignmentId}/status`,
      method: 'put',
      params: {
        status
      }
    })
  },

  // 删除分配
  deleteAssignment(assignmentId) {
    return request({
      url: `/api/review-assignments/${assignmentId}`,
      method: 'delete'
    })
  },

  // 批量删除分配
  batchDeleteAssignments(assignmentIds) {
    return request({
      url: '/api/review-assignments/batch',
      method: 'delete',
      data: assignmentIds
    })
  },

  // 获取分配统计信息
  getAssignmentStatistics(teamId, startDate, endDate) {
    return request({
      url: '/api/review-assignments/statistics',
      method: 'get',
      params: {
        teamId,
        startDate,
        endDate
      }
    })
  },

  // 获取用户分配统计
  getUserAssignmentStatistics(userId, startDate, endDate) {
    return request({
      url: `/api/review-assignments/user/${userId}/statistics`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },

  // 验证分配结果
  validateAssignments(teamId, weekStart) {
    return request({
      url: '/api/review-assignments/validate',
      method: 'post',
      params: {
        teamId,
        weekStart
      }
    })
  },

  // 检查分配冲突
  checkAssignmentConflicts(teamId, weekStart) {
    return request({
      url: '/api/review-assignments/conflicts',
      method: 'get',
      params: {
        teamId,
        weekStart
      }
    })
  }
}