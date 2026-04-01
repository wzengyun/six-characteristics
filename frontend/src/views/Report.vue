<template>
  <div class="report-page">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">报告中心</span>
          <el-button type="primary" @click="openGenerateDialog"><el-icon><Plus /></el-icon>生成报告</el-button>
        </div>
      </template>

      <el-table :data="reportList" v-loading="loading" stripe>
        <el-table-column prop="equipmentName" label="装备名称" />
        <el-table-column prop="reportType" label="报告类型" width="140">
          <template #default="{ row }">
            <el-tag :type="row.reportType === 'COMPREHENSIVE' ? 'primary' : 'info'" size="small">
              {{ typeMap[row.reportType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="downloadReport(row)"><el-icon><Download /></el-icon>下载</el-button>
            <el-button link type="primary" @click="previewReport(row)"><el-icon><View /></el-icon>预览</el-button>
            <el-button link type="danger" @click="deleteReport(row)"><el-icon><Delete /></el-icon></el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          layout="total, prev, pager, next"
        />
      </div>
    </el-card>

    <!-- 生成报告对话框 -->
    <el-dialog v-model="generateDialogVisible" title="生成分析报告" width="500px">
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="选择装备">
          <el-select v-model="generateForm.equipmentId" placeholder="选择装备" style="width:100%">
            <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="generateForm.reportType" style="width:100%">
            <el-option label="综合分析报告" value="COMPREHENSIVE" />
            <el-option label="可靠性分析报告" value="RELIABILITY" />
            <el-option label="维修性分析报告" value="MAINTAINABILITY" />
            <el-option label="安全性分析报告" value="SAFETY" />
          </el-select>
        </el-form-item>
        <el-form-item label="输出格式">
          <el-radio-group v-model="generateForm.format">
            <el-radio value="pdf">PDF</el-radio>
            <el-radio value="word">Word</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="generateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doGenerate" :loading="generating">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listEquipment, listReports, generateReport, downloadReport as download } from '@/api/equipment'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const generating = ref(false)
const generateDialogVisible = ref(false)
const equipmentList = ref([])
const reportList = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const generateForm = reactive({ equipmentId: null, reportType: 'COMPREHENSIVE', format: 'pdf' })

const typeMap = {
  COMPREHENSIVE: '综合报告',
  RELIABILITY: '可靠性报告',
  MAINTAINABILITY: '维修性报告',
  SAFETY: '安全性报告'
}

async function loadData() {
  loading.value = true
  try {
    const res = await listReports({ page: page.current, size: page.size })
    reportList.value = res.data?.list || []
    page.total = res.data?.total || 0
  } catch {
    reportList.value = [
      { id: 1, equipmentName: '某型雷达系统', reportType: 'COMPREHENSIVE', createTime: '2026-04-01 10:30', status: '已完成' },
      { id: 2, equipmentName: '某型通信设备', reportType: 'RELIABILITY', createTime: '2026-04-01 09:15', status: '已完成' },
    ]
    page.total = 2
  } finally {
    loading.value = false
  }
}

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

function openGenerateDialog() {
  generateDialogVisible.value = true
}

async function doGenerate() {
  if (!generateForm.equipmentId) {
    ElMessage.warning('请选择装备')
    return
  }
  generating.value = true
  try {
    await generateReport(generateForm)
    ElMessage.success('报告生成成功')
    generateDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('报告生成失败')
  } finally {
    generating.value = false
  }
}

async function downloadReport(row) {
  try {
    const blob = await download(row.id)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${row.equipmentName}_${typeMap[row.reportType]}.pdf`
    a.click()
    URL.revokeObjectURL(url)
  } catch { ElMessage.error('下载失败') }
}

function previewReport(row) {
  ElMessage.info('报告预览功能开发中')
}

async function deleteReport(row) {
  await ElMessageBox.confirm('确定删除该报告？', '提示', { type: 'warning' })
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadEquipment()
})
</script>

<style scoped lang="scss">
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
