import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

http.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  res => res.data,
  err => {
    const msg = err.response?.data?.message || '请求失败'
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      router.push('/login')
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(err)
  }
)

export default http
