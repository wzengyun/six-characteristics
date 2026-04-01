<template>
  <div class="environment-page">
    <el-tabs v-model="activeTab">

    <!-- ========== 参数输入页 ========== -->
    <el-tab-pane label="环境适应性参数" name="params" class="env-tab-pane">
      <el-card shadow="never">
        <!-- 顶部导航 -->
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span class="card-title">环境适应性参数 (GJB 150A-2009)</span>
            <el-button type="primary" @click="saveData"><el-icon><Check /></el-icon>保存</el-button>
          </div>
        </template>

        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="8">
            <el-select v-model="formData.equipmentId" placeholder="选择装备" style="width:100%">
              <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
            </el-select>
          </el-col>
        </el-row>

        <!-- 三个环境维度卡片 -->
        <el-row :gutter="16">
          <!-- 温度 -->
          <el-col :span="8">
            <el-card shadow="never" class="env-dim-card">
              <template #header>
                <div class="env-dim-header">
                  <span class="env-dim-title">🌡️ 温度适应性</span>
                  <el-tag type="info" size="small">GJB 150A</el-tag>
                </div>
              </template>
              <el-form label-position="top" :model="formData.temp">
                <el-form-item label="最低工作温度 (℃)">
                  <el-input-number v-model="formData.temp.workLow" :min="-80" :max="80" style="width:100%" />
                </el-form-item>
                <el-form-item label="最高工作温度 (℃)">
                  <el-input-number v-model="formData.temp.workHigh" :min="-80" :max="200" style="width:100%" />
                </el-form-item>
                <el-form-item label="极限低温 (℃)">
                  <el-input-number v-model="formData.temp.survivalLow" :min="-100" :max="80" style="width:100%" />
                </el-form-item>
                <el-form-item label="极限高温 (℃)">
                  <el-input-number v-model="formData.temp.survivalHigh" :min="-100" :max="300" style="width:100%" />
                </el-form-item>
              </el-form>
              <div class="env-dim-result">
                <div class="env-score" :style="{ color: '#f59e0b' }">
                  {{ tempScore.total }}
                </div>
                <div class="env-score-label">温度适应性评分</div>
                <el-tag :type="tempScore.level === '优秀' ? 'success' : tempScore.level === '良好' ? 'warning' : 'info'" size="small">
                  {{ tempScore.level }}
                </el-tag>
                <div class="env-formula">GJB 150A 温度试验</div>
              </div>
            </el-card>
          </el-col>

          <!-- 振动 -->
          <el-col :span="8">
            <el-card shadow="never" class="env-dim-card">
              <template #header>
                <div class="env-dim-header">
                  <span class="env-dim-title">📳 振动适应性</span>
                  <el-tag type="info" size="small">GJB 150A</el-tag>
                </div>
              </template>
              <el-form label-position="top" :model="formData.vibration">
                <el-form-item label="频率下限 (Hz)">
                  <el-input-number v-model="formData.vibration.freqMin" :min="0" :max="1000" style="width:100%" />
                </el-form-item>
                <el-form-item label="频率上限 (Hz)">
                  <el-input-number v-model="formData.vibration.freqMax" :min="0" :max="5000" style="width:100%" />
                </el-form-item>
                <el-form-item label="峰值加速度 (g)">
                  <el-input-number v-model="formData.vibration.accel" :min="0" :max="100" :precision="1" style="width:100%" />
                </el-form-item>
                <el-form-item label="扫频速率 (oct/min)">
                  <el-input-number v-model="formData.vibration.sweepRate" :min="0.1" :max="10" :precision="1" style="width:100%" />
                </el-form-item>
              </el-form>
              <div class="env-dim-result">
                <div class="env-score" :style="{ color: '#a78bfa' }">
                  {{ vibScore.total }}
                </div>
                <div class="env-score-label">振动适应性评分</div>
                <el-tag :type="vibScore.level === '优秀' ? 'success' : vibScore.level === '良好' ? 'warning' : 'info'" size="small">
                  {{ vibScore.level }}
                </el-tag>
                <div class="env-formula">GJB 150A 振动试验</div>
              </div>
            </el-card>
          </el-col>

          <!-- 湿度 -->
          <el-col :span="8">
            <el-card shadow="never" class="env-dim-card">
              <template #header>
                <div class="env-dim-header">
                  <span class="env-dim-title">💧 湿度适应性</span>
                  <el-tag type="info" size="small">GJB 150A</el-tag>
                </div>
              </template>
              <el-form label-position="top" :model="formData.humidity">
                <el-form-item label="最低相对湿度 (%RH)">
                  <el-input-number v-model="formData.humidity.low" :min="0" :max="100" style="width:100%" />
                </el-form-item>
                <el-form-item label="最高相对湿度 (%RH)">
                  <el-input-number v-model="formData.humidity.high" :min="0" :max="100" style="width:100%" />
                </el-form-item>
                <div style="height:80px"></div>
              </el-form>
              <div class="env-dim-result">
                <div class="env-score" :style="{ color: '#34d399' }">
                  {{ humScore.total }}
                </div>
                <div class="env-score-label">湿度适应性评分</div>
                <el-tag :type="humScore.level === '优秀' ? 'success' : humScore.level === '良好' ? 'warning' : 'info'" size="small">
                  {{ humScore.level }}
                </el-tag>
                <div class="env-formula">GJB 150A 湿热试验</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 综合评分 -->
        <el-card shadow="never" class="overall-card" style="margin-top:16px">
          <template #header><span class="card-title">综合评分</span></template>
          <el-row :gutter="24" align="middle">
            <el-col :span="6">
              <div class="overall-score-display">
                <el-progress type="dashboard" :percentage="overallScore" :width="100" :color="overallColor">
                  <template #default="{ percentage }">
                    <span class="overall-value">{{ percentage }}</span>
                  </template>
                </el-progress>
                <div class="overall-label">综合评分</div>
              </div>
            </el-col>
            <el-col :span="18">
              <el-descriptions :column="3" border size="small">
                <el-descriptions-item label="温度适应性">
                  <el-progress :percentage="parseFloat(tempScore.total)" :stroke-width="10" :show-text="false" color="#f59e0b" />
                  <span>{{ tempScore.total }}分 ({{ tempScore.level }})</span>
                </el-descriptions-item>
                <el-descriptions-item label="振动适应性">
                  <el-progress :percentage="parseFloat(vibScore.total)" :stroke-width="10" :show-text="false" color="#a78bfa" />
                  <span>{{ vibScore.total }}分 ({{ vibScore.level }})</span>
                </el-descriptions-item>
                <el-descriptions-item label="湿度适应性">
                  <el-progress :percentage="parseFloat(humScore.total)" :stroke-width="10" :show-text="false" color="#34d399" />
                  <span>{{ humScore.total }}分 ({{ humScore.level }})</span>
                </el-descriptions-item>
                <el-descriptions-item label="加权方式">温度35% + 振动35% + 湿度30%</el-descriptions-item>
                <el-descriptions-item label="评定标准">GJB 150A-2009</el-descriptions-item>
                <el-descriptions-item label="适应性等级">
                  <el-tag :type="overallScore >= 85 ? 'success' : overallScore >= 70 ? 'warning' : 'danger'">
                    {{ overallScore >= 85 ? '优秀' : overallScore >= 70 ? '良好' : overallScore >= 55 ? '一般' : '较差' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
        </el-card>
      </el-card>
    </el-tab-pane>

    <!-- ========== 雷达图页 ========== -->
    <el-tab-pane label="环境适应性雷达图" name="radar" class="env-tab-pane">
      <el-card shadow="never">
        <template #header><span class="card-title">三维适应性雷达图</span></template>
        <div class="chart-box"><v-chart :option="radarOption" autoresize /></div>
      </el-card>
    </el-tab-pane>

    <!-- ========== 计算器页 ========== -->
    <el-tab-pane label="环境适应性计算器" name="calculator" class="env-tab-pane">
      <el-card shadow="never" style="background:var(--bg-surface)">
        <el-form :inline="true" :model="calcInputs" style="margin-bottom:16px">
          <el-form-item label="工作温度范围 (℃)">
            <el-input-number v-model="calcInputs.tempRange" :min="0" :max="300" style="width:120px" />
          </el-form-item>
          <el-form-item label="加速度 (g)">
            <el-input-number v-model="calcInputs.accel" :min="0" :max="100" style="width:120px" />
          </el-form-item>
          <el-form-item label="频率范围 (Hz)">
            <el-input-number v-model="calcInputs.freqRange" :min="0" :max="5000" style="width:120px" />
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="runCalc">计算评分</el-button>
          </el-form-item>
        </el-form>
        <el-descriptions :column="3" border v-if="calcResult">
          <el-descriptions-item label="温度评分">{{ calcResult.tempScore }}分</el-descriptions-item>
          <el-descriptions-item label="振动评分">{{ calcResult.vibScore }}分</el-descriptions-item>
          <el-descriptions-item label="湿度评分">{{ calcResult.humScore }}分</el-descriptions-item>
          <el-descriptions-item label="综合评分">
            <span style="color:var(--color-accent);font-weight:700;font-size:16px">{{ calcResult.overall }}分</span>
          </el-descriptions-item>
          <el-descriptions-item label="适应性等级">
            <el-tag :type="calcResult.level === '优秀' ? 'success' : calcResult.level === '良好' ? 'warning' : 'info'">
              {{ calcResult.level }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标准依据">GJB 150A-2009</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { RadarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent } from 'echarts/components'
import { listEquipment, saveEnvironment } from '@/api/equipment'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, RadarChart, TitleComponent, TooltipComponent])

const activeTab = ref('params')
const equipmentList = ref([])
const calcInputs = reactive({ tempRange: 100, accel: 5, freqRange: 1980 })
const calcResult = ref(null)

const formData = reactive({
  equipmentId: null,
  temp: { workLow: -40, workHigh: 60, survivalLow: -55, survivalHigh: 85 },
  vibration: { freqMin: 20, freqMax: 2000, accel: 5, sweepRate: 1 },
  humidity: { low: 10, high: 95 },
})

function calcTemp(wLow, wHigh, sLow, sHigh) {
  const workRange = wHigh - wLow
  const workScore = Math.min(50, workRange * 0.5)
  const lowMargin = wLow - sLow
  const highMargin = sHigh - wHigh
  const marginScore = Math.min(50, (lowMargin + highMargin) * 0.4)
  const total = Math.min(100, workScore + marginScore)
  return {
    workScore: workScore.toFixed(1),
    marginScore: marginScore.toFixed(1),
    total: total.toFixed(1),
    level: total >= 85 ? '优秀' : total >= 70 ? '良好' : total >= 55 ? '一般' : '较差'
  }
}

function calcVib(fMin, fMax, accel, sweep) {
  const range = fMax - fMin
  const freqScore = Math.min(40, range * 0.2)
  const accScore = Math.min(40, accel * 2)
  const sweepScore = sweep <= 1 ? 20 : sweep <= 2 ? 15 : Math.max(5, 20 - sweep * 3)
  const total = Math.min(100, freqScore + accScore + sweepScore)
  return {
    freqScore: freqScore.toFixed(1),
    accScore: accScore.toFixed(1),
    sweepScore: sweepScore.toFixed(1),
    total: total.toFixed(1),
    level: total >= 85 ? '优秀' : total >= 70 ? '良好' : total >= 55 ? '一般' : '较差'
  }
}

function calcHum(low, high) {
  const range = high - low
  const score = Math.min(100, range * 0.8)
  return {
    score: score.toFixed(1),
    level: score >= 85 ? '优秀' : score >= 70 ? '良好' : score >= 55 ? '一般' : '较差'
  }
}

const tempScore = computed(() =>
  calcTemp(formData.temp.workLow, formData.temp.workHigh, formData.temp.survivalLow, formData.temp.survivalHigh)
)
const vibScore = computed(() =>
  calcVib(formData.vibration.freqMin, formData.vibration.freqMax, formData.vibration.accel, formData.vibration.sweepRate)
)
const humScore = computed(() => calcHum(formData.humidity.low, formData.humidity.high))

const overallScore = computed(() => {
  const t = parseFloat(tempScore.value.total)
  const v = parseFloat(vibScore.value.total)
  const h = parseFloat(humScore.value.score)
  return Math.round(t * 0.35 + v * 0.35 + h * 0.30)
})

const overallColor = computed(() =>
  overallScore.value >= 85 ? '#22c55e' : overallScore.value >= 70 ? '#f59e0b' : '#ef4444'
)

const radarOption = computed(() => ({
  tooltip: { trigger: 'item' },
  radar: {
    indicator: [
      { name: '温度适应性', max: 100 },
      { name: '振动适应性', max: 100 },
      { name: '湿度适应性', max: 100 },
      { name: '综合评分', max: 100 },
    ],
    axisName: { color: '#8ba8c8', fontSize: 13 },
    splitArea: { areaStyle: { color: ['rgba(52,211,153,0.02)', 'rgba(52,211,153,0.05)'] } },
  },
  series: [{
    type: 'radar',
    data: [{
      value: [
        parseFloat(tempScore.value.total),
        parseFloat(vibScore.value.total),
        parseFloat(humScore.value.score),
        overallScore.value,
      ],
      name: '环境适应性评分',
      areaStyle: { color: 'rgba(52,211,153,0.3)' },
      lineStyle: { color: '#34d399', width: 2 },
      itemStyle: { color: '#34d399' },
    }]
  }]
}))

function runCalc() {
  const t = calcTemp(-40, -40 + calcInputs.tempRange, -55, 85)
  const v = calcVib(20, 20 + calcInputs.freqRange, calcInputs.accel, 1)
  const h = calcHum(10, 95)
  const o = Math.round(parseFloat(t.total) * 0.35 + parseFloat(v.total) * 0.35 + parseFloat(h.score) * 0.30)
  calcResult.value = {
    tempScore: t.total, vibScore: v.total, humScore: h.score,
    overall: o, level: o >= 85 ? '优秀' : o >= 70 ? '良好' : o >= 55 ? '一般' : '较差'
  }
}

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

async function saveData() {
  try {
    await saveEnvironment({ equipmentId: formData.equipmentId, adaptScore: overallScore.value })
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
}

onMounted(() => loadEquipment())
</script>

<style scoped lang="scss">
.env-tab-pane {
  display: block;
}

.env-dim-card {
  height: 100%;
  :deep(.el-card__header) {
    padding: 10px 16px;
    background: var(--bg-surface);
  }
}

.env-dim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.env-dim-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.env-dim-result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--bg-border);
  text-align: center;
}

.env-score {
  font-size: 36px;
  font-weight: 700;
  font-family: var(--font-display);
  line-height: 1;
}

.env-score-label {
  font-size: 11px;
  color: var(--text-muted);
  margin: 4px 0 8px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.env-formula {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.overall-card {
  :deep(.el-card__header) { padding: 10px 16px; }
}

.overall-score-display {
  text-align: center;
}

.overall-value {
  font-size: 22px;
  font-weight: 700;
  font-family: var(--font-display);
  color: var(--text-primary);
}

.overall-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.chart-box {
  height: 420px;
}
</style>
