<template>
  <div class="supportability-page">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">保障性分析</span>
          <el-button type="primary" @click="saveData"><el-icon><Check /></el-icon>保存</el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="formData" label-width="140px">
            <el-form-item label="装备名称">
              <el-select v-model="formData.equipmentId" placeholder="选择装备" style="width:100%">
                <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="保障资源完备度">
              <el-slider v-model="formData.resourceScore" :min="0" :max="100" show-input />
            </el-form-item>
            <el-form-item label="备件满足率">
              <el-slider v-model="formData.spareRate" :min="0" :max="100" :format-tooltip="v => v + '%'" show-input />
            </el-form-item>
            <el-form-item label="人员培训率">
              <el-slider v-model="formData.trainingRate" :min="0" :max="100" :format-tooltip="v => v + '%'" show-input />
            </el-form-item>
            <el-form-item label="技术资料完备度">
              <el-slider v-model="formData.docScore" :min="0" :max="100" show-input />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <div class="chart-box">
            <v-chart :option="chartOption" autoresize />
          </div>
        </el-col>
      </el-row>

      <el-divider />
      <el-descriptions title="保障性评估" :column="2" border>
        <el-descriptions-item label="综合保障率">
          <el-progress :percentage="overallRate" :color="'#a78bfa'" :stroke-width="20" />
        </el-descriptions-item>
        <el-descriptions-item label="评估等级">
          <el-tag :type="levelType" size="large">{{ levelText }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { listEquipment, saveSupportability } from '@/api/equipment'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, RadarChart, TitleComponent, TooltipComponent, LegendComponent])

const equipmentList = ref([])
const formData = reactive({
  equipmentId: null, resourceScore: 85, spareRate: 92,
  trainingRate: 88, docScore: 90
})

const overallRate = computed(() =>
  Math.round((formData.resourceScore + formData.spareRate + formData.trainingRate + formData.docScore) / 4)
)
const levelType = computed(() =>
  overallRate.value >= 90 ? 'success' : overallRate.value >= 80 ? 'warning' : 'danger'
)
const levelText = computed(() =>
  overallRate.value >= 90 ? 'A 级 (优秀)' : overallRate.value >= 80 ? 'B 级 (良好)' : 'C 级 (待改进)'
)

const chartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  radar: {
    indicator: [
      { name: '资源完备度', max: 100 },
      { name: '备件满足率', max: 100 },
      { name: '人员培训率', max: 100 },
      { name: '资料完备度', max: 100 },
    ],
    axisName: { color: '#8ba8c8', fontSize: 12 },
    splitArea: { areaStyle: { color: ['rgba(167,139,250,0.02)', 'rgba(167,139,250,0.04)'] } },
  },
  series: [{
    type: 'radar',
    data: [{
      value: [formData.resourceScore, formData.spareRate, formData.trainingRate, formData.docScore],
      areaStyle: { color: 'rgba(167,139,250,0.3)' },
      lineStyle: { color: '#a78bfa', width: 2 },
      itemStyle: { color: '#a78bfa' },
    }]
  }]
}))

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

async function saveData() {
  try { await saveSupportability(formData); ElMessage.success('保存成功') }
  catch { ElMessage.error('保存失败') }
}

onMounted(() => loadEquipment())
</script>

<style scoped lang="scss">.chart-box { height: 300px; }</style>
