import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(account, password) {
    const res = await loginApi({ account, password })
    token.value = res.data.token
    userInfo.value = res.data.user
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, login, logout }
})
