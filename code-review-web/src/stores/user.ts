import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/user'
import type { LoginRequest, User } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
  }

  const setUser = (userInfo: User) => {
    user.value = userInfo
  }

  const loginAction = async (loginData: LoginRequest) => {
    try {
      const response = await login(loginData)
      setToken(response.data.token)
      setUser(response.data.user)
      return response
    } catch (error) {
      throw error
    }
  }

  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      clearToken()
      user.value = null
    }
  }

  const getUserInfoAction = async () => {
    try {
      const response = await getUserInfo()
      setUser(response.data)
      return response
    } catch (error) {
      clearToken()
      user.value = null
      throw error
    }
  }

  return {
    token,
    user,
    setToken,
    clearToken,
    setUser,
    loginAction,
    logoutAction,
    getUserInfoAction
  }
})