export interface User {
  id: number
  username: string
  email: string
  realName: string
  phone?: string
  role: 'DEVELOPER' | 'TEAM_LEADER' | 'ARCHITECT'
  teamId?: number
  teamName?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
}

export interface Team {
  id: number
  name: string
  description?: string
  leaderId: number
  leaderName?: string
  memberCount?: number
  active: boolean
  createdAt: string
  updatedAt: string
}