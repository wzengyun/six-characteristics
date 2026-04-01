<template>
  <div class="dashboard">
    <div class="scanline-overlay"></div>

    <!-- 顶部统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="s in stats" :key="s.key">
        <div class="stat-icon" :style="{ background: s.bg }">
          <el-icon :size="24"><component :is="s.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value count-up">{{ s.value }}</div>
          <div class="stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- 六性雷达图 -->
    <div class="main-row">
      <el-card class="radar-card">
        <template #header>
          <span class="card-title">六性综合评估</span>
        </template>
        <div class="chart-container">
          <v-chart :option="radarOption" autoresize />
        </div>
      </el-card>

      <el-card class="trend-card">
        <template #header>
          <span class="card-title">可靠性趋势分析</span>
        </template>
        <div class="chart-container">
          <v-chart :option="lineOption" autoresize />
        </div>
      </el-card>
    </div>

    <!-- 装备列表 & 活动日志 -->
    <div class="bottom-row">
      <el-card class="equipment-card">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="card-title">装备列表</span>
            <el-button type="primary" size="small" @click="$router.push('/equipment')">查看全部</el-button>
          </div>
        </template>
        <el-table :data="equipmentList" size="small" v-loading="loading">
          <el-table-column prop="name" label="装备名称" />
          <el-table-column prop="model" label="型号" />
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === '正常' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="reliability" label="可靠度">
            <template #default="{ row }">
              <span :class="row.reliability > 0.95 ? 'text-success' : 'text-warning'">
                {{ (row.reliability * 100).toFixed(1) }}%
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="activity-card">
        <template #header>
          <span class="card-title">系统活动</span>
        </template>
        <el-timeline>
          <el-timeline-item v-for="a in activities" :key="a.time" :timestamp="a.time" placement="top">
            <div class="activity-item">
              <el-icon :style="{ color: a.color }"><component :is="a.icon" /></el-icon>
              <span>{{ a.text }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart, LineChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { listEquipment } from '@/api/equipment'

use([CanvasRenderer, RadarChart, LineChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const loading = ref(false)
const equipmentList = ref([])

const stats = ref([
  { key: 'equipment', label: '装备总数', value: 12, icon: 'Box', bg: 'linear-gradient(135deg,#0d6efd,#00d4ff)' },
  { key: 'analysis', label: '分析次数', value: 156, icon: 'TrendCharts', bg: 'linear-gradient(135deg,#22c55e,#4ade80)' },
  { key: 'report', label: '生成报告', value: 48, icon: 'Document', bg: 'linear-gradient(135deg,#a78bfa,#c084fc)' },
  { key: 'warning', label: '风险预警', value: 3, icon: 'Warning', bg: 'linear-gradient(135deg,#f59e0b,#fbbf24)' },
])

const activities = ref([
  { time: '10:32', text: '完成"某型雷达系统"可靠性分析', icon: 'SuccessFilled', color: '#22c55e' },
  { time: '09:45', text: '新增装备"某型通信设备"', icon: 'Plus', color: '#00d4ff' },
  { time: '昨天', text: '生成综合分析报告', icon: 'Document', color: '#a78bfa' },
  { time: '昨天', text: '安全性预警：故障概率超标', icon: 'Warning', color: '#f87171' },
])

const radarOption = ref({
  tooltip: { trigger: 'item' },
  radar: {
    indicator: [
      { name: '可靠性', max: 100 },
      { name: '维修性', max: 100 },
      { name: '测试性', max: 100 },
      { name: '保障性', max: 100 },
      { name: '安全性', max: 100 },
      { name: '环境适应性', max: 100 },
    ],
    axisName: { color: '#8ba8c8', fontSize: 12 },
    splitArea: { areaStyle: { color: ['rgba(0,212,255,0.02)', 'rgba(0,212,255,0.04)'] } },
    axisLine: { lineStyle: { color: 'rgba(0,212,255,0.15)' } },
    splitLine: { lineStyle: { color: 'rgba(0,212,255,0.1)' } },
  },
  series: [{
    type: 'radar',
    data: [{
      value: [92, 85, 88, 90, 78, 86],
      name: '综合评分',
      areaStyle: { color: 'rgba(0,212,255,0.3)' },
      lineStyle: { color: '#00d4ff', width: 2 },
      itemStyle: { color: '#00d4ff' },
    }]
  }]
})

const lineOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['1月', '2月', '3月', '4月', '5月', '6月'],
    axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
    axisLabel: { color: '#8ba8c8' },
  },
  yAxis: {
    type: 'value',
    min: 0.8,
    max: 1,
    axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
    axisLabel: { color: '#8ba8c8', formatter: '{value}' },
    splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' } },
  },
  series: [{
    name: '可靠度',
    type: 'line',
    smooth: true,
    data: [0.92, 0.94, 0.91, 0.95, 0.93, 0.96],
    lineStyle: { color: '#00d4ff', width: 2 },
    areaStyle: {
      color: {
        type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: 'rgba(0,212,255,0.4)' }, { offset: 1, color: 'rgba(0,212,255,0.05)' }]
      }
    },
    itemStyle: { color: '#00d4ff' },
  }]
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await listEquipment({ page: 1, size: 5 })
    equipmentList.value = (res.data?.list || []).map(e => ({
      ...e,
      status: '正常',
      reliability: 0.92 + Math.random() * 0.07
    }))
    stats.value[0].value = res.data?.total || 12
  } catch {
    // 使用模拟数据
    equipmentList.value = [
      { id: 1, name: '某型雷达系统', model: 'LD-2000A', status: '正常', reliability: 0.96 },
      { id: 2, name: '某型通信设备', model: 'TX-500B', status: '正常', reliability: 0.94 },
      { id: 3, name: '某型火控系统', model: 'FC-300C', status: '维护中', reliability: 0.91 },
    ]
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.dashboard { position: relative; }

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}
.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--bg-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s, box-shadow 0.2s;
  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-glow);
  }
}
.stat-icon {
  width: 56px; height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}
.stat-info {
  .stat-value { font-size: 28px; }
}

.main-row {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-container { height: 320px; }

.bottom-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
