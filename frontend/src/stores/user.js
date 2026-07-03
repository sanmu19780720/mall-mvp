import { ref } from 'vue'
import { defineStore } from 'pinia'

// 用户信息 store：持有 token 与 userInfo
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function clearToken() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  return { token, userInfo, setToken, clearToken, setUserInfo }
})
