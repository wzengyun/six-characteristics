import http from '@/utils/http'

// 装备 CRUD
export const listEquipment   = params => http.get('/equipment', { params })
export const getEquipment    = id     => http.get(`/equipment/${id}`)
export const createEquipment = data   => http.post('/equipment', data)
export const updateEquipment = (id,d) => http.put(`/equipment/${id}`, d)
export const deleteEquipment = id     => http.delete(`/equipment/${id}`)

// 可靠性
export const getReliability     = id => http.get(`/equipment/reliability/${id}`)
export const saveReliability    = d  => http.post('/equipment/reliability', d)

// 维修性
export const getMaintainability  = id => http.get(`/equipment/maintainability/${id}`)
export const saveMaintainability = d  => http.post('/equipment/maintainability', d)

// 测试性
export const getTestability     = id => http.get(`/equipment/testability/${id}`)
export const saveTestability    = d  => http.post('/equipment/testability', d)

// 保障性
export const getSupportability  = id => http.get(`/equipment/supportability/${id}`)
export const saveSupportability = d  => http.post('/equipment/supportability', d)

// 安全性
export const getSafety          = id => http.get(`/equipment/safety/${id}`)
export const saveSafety         = d  => http.post('/equipment/safety', d)

// 环境适应性
export const getEnvironment     = id => http.get(`/equipment/environment/${id}`)
export const saveEnvironment    = d  => http.post('/equipment/environment', d)

// 综合分析（独立 API 路径）
export const getAnalysis        = id => http.get(`/analysis/${id}`)

// 报告
export const listReports        = params => http.get('/report', { params })
export const generateReport    = d      => http.post('/report/generate', d)
export const downloadReport    = id     => http.get(`/report/download/${id}`, { responseType: 'blob' })
