import request from '@/utils/request'
import type { LoginRequest, LoginResponse, User, Team } from '@/types/user'

// 用户登录
export const login = (data: LoginRequest) => {
  return request.post<LoginResponse>('/users/login', data)
}

// 用户登出
export const logout = () => {
  return request.post('/users/logout')
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get<User>('/users/profile')
}

// 更新用户信息
export const updateUserInfo = (data: Partial<User>) => {
  return request.put<User>('/users/profile', data)
}

// 获取团队成员列表
export const getTeamMembers = (teamId: number) => {
  return request.get<User[]>(`/users/team/${teamId}/members`)
}

// 获取团队信息
export const getTeamInfo = (teamId: number) => {
  return request.get<Team>(`/teams/${teamId}`)
}