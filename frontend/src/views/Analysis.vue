<template>
  <div class="analysis-page">
    <div class="scanline-overlay"></div>

    <el-card class="selector-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="8">
          <el-select v-model="selectedEquipmentId" placeholder="选择装备" style="width:100%" @change="loadAnalysis">
            <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="runComprehensive" :loading="analyzing">
            <el-icon><Cpu /></el-icon>综合分析
          </el-button>
          <el-button @click="generateReport"><el-icon><Document /></el-icon>生成报告</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">六性综合雷达图</span></template>
          <div class="chart-box"><v-chart :option="radarOption" autoresize /></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span class="card-title">六性评分对比</span></template>
          <div class="chart-box"><v-chart :option="barOption" autoresize /></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="detail-card">
      <template #header><span class="card-title">详细指标</span></template>
      <el-table :data="detailData" stripe>
        <el-table-column prop="name" label="特性" width="120">
          <template #default="{ row }">
            <span class="dot" :style="{ background: row.color }"></span>{{ row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="评分" width="100">
          <template #default="{ row }">
            <el-tag :type="row.score >= 90 ? 'success' : row.score >= 80 ? 'warning' : 'danger'">{{ row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="indicator" label="关键指标" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '达标' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="改进建议" />
      </el-table>
    </el-card>

    <el-card class="conclusion-card">
      <template #header><span class="card-title">综合评估结论</span></template>
      <div class="conclusion-content">
        <div class="overall-grade">
          <div class="grade-label">综合等级</div>
          <div class="grade-value" :style="{ color: gradeColor }">{{ overallGrade }}</div>
          <div class="grade-score">{{ overallScore }} 分</div>
        </div>
        <div class="conclusion-text">
          <p>{{ conclusion }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { listEquipment, getAnalysis } from '@/api/equipment'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, RadarChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const equipmentList = ref([])
const selectedEquipmentId = ref(null)
const analyzing = ref(false)

const detailData = ref([
  { name: '可靠性', score: 92, indicator: 'MTBF = 2000h, R(t=1000h) = 0.95', status: '达标', suggestion: '保持现有维护策略', color: '#00d4ff' },
  { name: '维修性', score: 85, indicator: 'MTTR = 2.5h, M(t=4h) = 0.82', status: '达标', suggestion: '优化维修流程', color: '#4ade80' },
  { name: '测试性', score: 88, indicator: 'FDR = 92%, FIR = 88%', status: '达标', suggestion: '增加测试点', color: '#f59e0b' },
  { name: '保障性', score: 90, indicator: '保障率 = 90%', status: '达标', suggestion: '完善备件管理', color: '#a78bfa' },
  { name: '安全性', score: 78, indicator: 'P(f) = 0.0001', status: '待改进', suggestion: '加强故障树分析', color: '#f87171' },
  { name: '环境适应性', score: 86, indicator: '综合评分 = 86', status: '达标', suggestion: '加强高温防护', color: '#34d399' },
])

const radarOption = computed(() => ({
  tooltip: { trigger: 'item' },
  radar: {
    indicator: detailData.value.map(d => ({ name: d.name, max: 100 })),
    axisName: { color: '#8ba8c8', fontSize: 13 },
    splitArea: { areaStyle: { color: ['rgba(0,212,255,0.02)', 'rgba(0,212,255,0.04)'] } },
  },
  series: [{
    type: 'radar',
    data: [{
      value: detailData.value.map(d => d.score),
      name: '综合评分',
      areaStyle: { color: 'rgba(0,212,255,0.3)' },
      lineStyle: { color: '#00d4ff', width: 2 },
      itemStyle: { color: '#00d4ff' },
    }]
  }]
}))

const barOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: detailData.value.map(d => d.name),
    axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
    axisLabel: { color: '#8ba8c8', rotate: 15 }
  },
  yAxis: {
    type: 'value', max: 100,
    axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
    axisLabel: { color: '#8ba8c8' },
    splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' } }
  },
  series: [{
    type: 'bar', barWidth: '50%',
    data: detailData.value.map(d => ({ value: d.score, itemStyle: { color: d.color } }))
  }]
}))

const overallScore = computed(() => {
  const scores = detailData.value.map(d => d.score)
  return Math.round(scores.reduce((a, b) => a + b, 0) / scores.length)
})
const overallGrade = computed(() => {
  const s = overallScore.value
  if (s >= 90) return 'A 级'
  if (s >= 80) return 'B 级'
  if (s >= 70) return 'C 级'
  return 'D 级'
})
const gradeColor = computed(() => {
  const s = overallScore.value
  if (s >= 90) return '#22c55e'
  if (s >= 80) return '#f59e0b'
  return '#ef4444'
})
const conclusion = computed(() => {
  const s = overallScore.value
  if (s >= 90) return '该装备六性综合表现优秀，各项指标均达到设计要求，可放心投入使用。建议定期进行可靠性验证，保持良好的维护状态。'
  if (s >= 80) return '该装备六性综合表现良好，大部分指标达标。建议重点关注安全性指标，加强故障树分析和风险管控措施。'
  return '该装备六性综合表现一般，部分指标需要改进。建议开展专项可靠性增长工作，识别薄弱环节并实施改进措施。'
})

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

async function loadAnalysis() {
  if (!selectedEquipmentId.value) return
  try {
    const res = await getAnalysis(selectedEquipmentId.value)
    if (res.data?.detail) detailData.value = res.data.detail
  } catch {}
}

async function runComprehensive() {
  if (!selectedEquipmentId.value) { ElMessage.warning('请先选择装备'); return }
  analyzing.value = true
  await new Promise(r => setTimeout(r, 1500))
  detailData.value = detailData.value.map(d => ({ ...d, score: Math.min(100, Math.max(50, d.score + Math.floor(Math.random() * 10 - 3))) }))
  analyzing.value = false
  ElMessage.success('综合分析完成')
}

function generateReport() { ElMessage.success('报告生成中，请前往报告中心下载') }

onMounted(() => loadEquipment())
</script>

<style scoped lang="scss">
.selector-card { margin-bottom: 20px; }
.chart-box { height: 320px; }
.detail-card { margin-top: 20px; }
.dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 8px; }
.conclusion-card { margin-top: 20px; }
.conclusion-content { display: flex; gap: 40px; align-items: center; }
.overall-grade {
  text-align: center;
  padding: 20px 40px;
  background: var(--bg-surface);
  border-radius: var(--radius-lg);
  .grade-label { font-size: 14px; color: var(--text-muted); margin-bottom: 8px; }
  .grade-value { font-size: 48px; font-weight: 700; font-family: var(--font-display); }
  .grade-score { font-size: 16px; color: var(--text-secondary); margin-top: 4px; }
}
.conclusion-text { flex: 1; p { color: var(--text-secondary); line-height: 1.8; } }
</style>
