import axios from 'axios'
import router from '@/router'
import { useUserStore } from '@/stores/user'

// 统一的 Axios 实例：base URL 来自环境变量，避免硬编码
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// 请求拦截器：自动注入 JWT token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：401 时清除 token 并跳转登录页
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.clearToken()
      router.replace('/login')
    }
    return Promise.reject(error)
  },
)

export default request
