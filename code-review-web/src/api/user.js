import request from '@/utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/api/auth/login',
    method: 'post',
    data
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/api/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/api/users/profile',
    method: 'get'
  })
}

export const userApi = {
  // 获取所有用户
  getAllUsers() {
    return request({
      url: '/api/users',
      method: 'get'
    })
  },

  // 获取用户详情
  getUserDetail(userId) {
    return request({
      url: `/api/users/${userId}`,
      method: 'get'
    })
  },

  // 获取团队成员
  getTeamMembers(teamId) {
    return request({
      url: `/api/users/team/${teamId}/members`,
      method: 'get'
    })
  },

  // 获取用户统计信息
  getUserStatistics(userId, startDate, endDate) {
    return request({
      url: `/api/users/${userId}/statistics`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },

  // 搜索用户
  searchUsers(keyword) {
    return request({
      url: '/api/users/search',
      method: 'get',
      params: {
        keyword
      }
    })
  },

  // 获取用户技能信息
  getUserSkills(userId) {
    return request({
      url: `/api/users/${userId}/skills`,
      method: 'get'
    })
  },

  // 更新用户技能
  updateUserSkills(userId, skills) {
    return request({
      url: `/api/users/${userId}/skills`,
      method: 'put',
      data: skills
    })
  },

  // 获取用户工作负载
  getUserWorkload(userId, startDate, endDate) {
    return request({
      url: `/api/users/${userId}/workload`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  }
}