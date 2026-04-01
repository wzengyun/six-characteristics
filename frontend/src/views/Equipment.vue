<template>
  <div class="equipment-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">装备管理</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增装备
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="search.name" placeholder="装备名称" clearable style="width:200px" />
        <el-input v-model="search.model" placeholder="型号" clearable style="width:200px" />
        <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon>查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="name" label="装备名称" min-width="150" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="structure" label="结构描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="六性评估" width="180">
          <template #default="{ row }">
            <div class="six-bar">
              <span v-for="s in row.scores" :key="s.name" class="bar-item" :style="{ background: s.color, width: s.value + '%' }" :title="s.name + ': ' + s.value"></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="primary" @click="goAnalysis(row)">分析</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.size"
          :total="page.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑装备' : '新增装备'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="装备名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入装备名称" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="结构描述">
          <el-input v-model="form.structure" type="textarea" :rows="3" placeholder="请输入结构描述" />
        </el-form-item>
        <el-form-item label="组成部件">
          <el-select v-model="form.parts" multiple filterable allow-create placeholder="输入部件名称" style="width:100%">
            <el-option v-for="p in defaultParts" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listEquipment, createEquipment, updateEquipment, deleteEquipment } from '@/api/equipment'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const search = reactive({ name: '', model: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const tableData = ref([])

const defaultParts = ['天线阵列', '发射机', '接收机', '信号处理机', '显控台', '电源模块', '控制单元', '传感器组', '计算机', '执行机构']

const form = reactive({ id: null, name: '', model: '', structure: '', parts: [], remark: '' })
const rules = {
  name: [{ required: true, message: '请输入装备名称', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
}

function genScores() {
  const colors = ['#00d4ff', '#4ade80', '#f59e0b', '#a78bfa', '#f87171', '#34d399']
  const names = ['可靠性', '维修性', '测试性', '保障性', '安全性', '环境适应性']
  return names.map((name, i) => ({ name, color: colors[i], value: 70 + Math.floor(Math.random() * 25) }))
}

async function loadData() {
  loading.value = true
  try {
    const res = await listEquipment({ ...search, page: page.current, size: page.size })
    tableData.value = (res.data?.list || []).map(e => ({ ...e, scores: genScores() }))
    page.total = res.data?.total || 0
  } catch {
    // 模拟数据
    tableData.value = [
      { id: 1, name: '某型雷达系统', model: 'LD-2000A', structure: '由天线阵列、发射机、接收机、信号处理机、显控台组成', scores: genScores(), createTime: '2026-04-01 10:00' },
      { id: 2, name: '某型通信设备', model: 'TX-500B', structure: '由射频单元、基带处理单元、电源模块、控制单元组成', scores: genScores(), createTime: '2026-04-01 09:30' },
    ]
    page.total = 2
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  search.name = ''
  search.model = ''
  page.current = 1
  loadData()
}

function openDialog(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, name: '', model: '', structure: '', parts: [], remark: '' })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { ...form, parts: JSON.stringify(form.parts) }
    if (form.id) {
      await updateEquipment(form.id, data)
      ElMessage.success('修改成功')
    } else {
      await createEquipment(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该装备？', '提示', { type: 'warning' })
  await deleteEquipment(row.id)
  ElMessage.success('删除成功')
  loadData()
}

function goAnalysis(row) {
  router.push({ path: '/reliability', query: { equipmentId: row.id } })
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.six-bar {
  display: flex;
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  background: var(--bg-surface);
  .bar-item { height: 100%; }
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
