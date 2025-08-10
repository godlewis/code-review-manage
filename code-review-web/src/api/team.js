import request from '@/utils/request'

export const teamApi = {
  // 获取所有团队
  getTeams() {
    return request({
      url: '/api/teams',
      method: 'get'
    })
  },

  // 获取团队详情
  getTeamDetail(teamId) {
    return request({
      url: `/api/teams/${teamId}`,
      method: 'get'
    })
  },

  // 获取团队成员
  getTeamMembers(teamId) {
    return request({
      url: `/api/teams/${teamId}/members`,
      method: 'get'
    })
  },

  // 获取团队统计信息
  getTeamStatistics(teamId, startDate, endDate) {
    return request({
      url: `/api/teams/${teamId}/statistics`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },

  // 创建团队
  createTeam(teamData) {
    return request({
      url: '/api/teams',
      method: 'post',
      data: teamData
    })
  },

  // 更新团队信息
  updateTeam(teamId, teamData) {
    return request({
      url: `/api/teams/${teamId}`,
      method: 'put',
      data: teamData
    })
  },

  // 删除团队
  deleteTeam(teamId) {
    return request({
      url: `/api/teams/${teamId}`,
      method: 'delete'
    })
  },

  // 添加团队成员
  addTeamMember(teamId, userId) {
    return request({
      url: `/api/teams/${teamId}/members`,
      method: 'post',
      data: { userId }
    })
  },

  // 移除团队成员
  removeTeamMember(teamId, userId) {
    return request({
      url: `/api/teams/${teamId}/members/${userId}`,
      method: 'delete'
    })
  }
}