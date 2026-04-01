import http from '@/utils/http'

export const login        = data => http.post('/user/login', data)
export const register     = data => http.post('/user/register', data)
export const getProfile   = ()   => http.get('/user/profile')
export const updateProfile = data => http.put('/user/profile', data)
export const listUsers   = params => http.get('/user/list', { params })
