<template>
  <div class="maintainability-page">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">维修性分析 (GJB 2072-2006)</span>
          <el-button type="primary" @click="saveData"><el-icon><Check /></el-icon>保存</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- ===== 维修性参数 ===== -->
        <el-tab-pane label="维修性参数" name="params">
          <el-form :model="formData" label-width="160px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="装备名称">
                  <el-select v-model="formData.equipmentId" placeholder="选择装备" style="width:100%">
                    <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="总维修时间 T_m (h)">
                  <el-input-number v-model="formData.totalTime" :min="0" :precision="2" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="维修次数 r">
                  <el-input-number v-model="formData.repairCount" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="目标维修度 M*">
                  <el-input-number v-model="formData.targetM" :min="0" :max="0.99" :precision="2" style="width:100%" />
                  <span class="form-hint">例: 0.9 表示 90%</span>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="置信度">
                  <el-select v-model="formData.confidence" style="width:100%">
                    <el-option label="80%" :value="0.8" />
                    <el-option label="90%" :value="0.9" />
                    <el-option label="95%" :value="0.95" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <!-- 计算结果 -->
          <el-divider content-position="left">
            <span style="color:#4ade80;font-weight:600">GJB 2072 计算结果</span>
          </el-divider>
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">MTTR 点估计</div>
                <div class="result-value text-success">{{ result.mttrPoint.toFixed(2) }} h</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">MTTR 置信上限</div>
                <div class="result-value text-warning">{{ result.mttrUpper.toFixed(2) }} h</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">修复率 μ</div>
                <div class="result-value">{{ result.repairRate.toFixed(4) }} /h</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">达到 M* 所需时间 t*</div>
                <div class="result-value">{{ result.timeToTarget.toFixed(2) }} h</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-stat">
                <div class="result-label">维修性等级 (GJB 2072)</div>
                <div class="result-value" :style="{ color: levelColor }">{{ result.level }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-stat">
                <div class="result-label">标准依据</div>
                <div class="result-value text-muted">GJB 2072-2006 Table 1</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="result-stat">
                <div class="result-label">M(t*) = {{ formData.targetM }}</div>
                <div class="result-value text-accent">{{ result.maintainabilityAtTarget.toFixed(4) }}</div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- ===== 维修度曲线 ===== -->
        <el-tab-pane label="维修度曲线" name="curve">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="chart-box"><v-chart :option="curveOption" autoresize /></div>
            </el-col>
            <el-col :span="12">
              <el-card shadow="never" style="background:var(--bg-surface);margin-bottom:16px">
                <div class="formula-title">维修度函数 (指数分布假设)</div>
                <div class="formula">M(t) = 1 - e<sup>-t/MTTR</sup></div>
                <div class="formula-title" style="margin-top:16px">修复率</div>
                <div class="formula">μ = 1 / MTTR = {{ result.repairRate.toFixed(4) }} h<sup>-1</sup></div>
                <div class="formula-title" style="margin-top:16px">置信上限 (χ² 方法)</div>
                <div class="formula">θ<sub>U</sub> = 2T<sub>m</sub> / χ²(α; 2r) = {{ result.mttrUpper.toFixed(2) }} h</div>
              </el-card>
              <el-card shadow="never" style="background:var(--bg-surface)">
                <template #header><span class="card-title">维修性等级表 (GJB 2072 Table 1)</span></template>
                <el-table :data="levelTable" size="small" stripe>
                  <el-table-column prop="level" label="等级" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.type" size="small">{{ row.level }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="mttr" label="MTTR (h)" width="120" />
                  <el-table-column prop="desc" label="说明" />
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- ===== 维修性计算器 ===== -->
        <el-tab-pane label="维修性计算器" name="calculator">
          <el-card shadow="never" style="background:var(--bg-surface)">
            <el-form :inline="true" :model="calcInputs">
              <el-form-item label="总维修时间 T_m (h)">
                <el-input-number v-model="calcInputs.totalTime" :min="0" :precision="2" style="width:140px" />
              </el-form-item>
              <el-form-item label="维修次数 r">
                <el-input-number v-model="calcInputs.repairCount" :min="0" style="width:100px" />
              </el-form-item>
              <el-form-item label="目标维修度 M*">
                <el-input-number v-model="calcInputs.targetM" :min="0" :max="0.99" :precision="2" style="width:120px" />
              </el-form-item>
              <el-form-item label="置信度">
                <el-select v-model="calcInputs.confidence" style="width:100px">
                  <el-option label="80%" :value="0.8" />
                  <el-option label="90%" :value="0.9" />
                  <el-option label="95%" :value="0.95" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="runCalc">计算</el-button>
              </el-form-item>
            </el-form>
            <el-divider />
            <el-descriptions :column="2" border v-if="calcResult">
              <el-descriptions-item label="MTTR 点估计">{{ calcResult.mttrPoint.toFixed(2) }} h</el-descriptions-item>
              <el-descriptions-item label="修复率 μ">{{ calcResult.repairRate.toFixed(4) }} /h</el-descriptions-item>
              <el-descriptions-item label="MTTR 置信上限">{{ calcResult.mttrUpper.toFixed(2) }} h</el-descriptions-item>
              <el-descriptions-item label="达到 M* 所需时间">{{ calcResult.timeToTarget.toFixed(2) }} h</el-descriptions-item>
              <el-descriptions-item label="维修性等级">{{ calcResult.level }}</el-descriptions-item>
              <el-descriptions-item label="判定">
                <el-tag :type="calcResult.level.startsWith('Ⅰ') || calcResult.level.startsWith('Ⅱ') ? 'success' : 'warning'">
                  {{ calcResult.level.startsWith('Ⅰ') || calcResult.level.startsWith('Ⅱ') ? '合格' : '需改进' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, MarkLineComponent } from 'echarts/components'
import { listEquipment, saveMaintainability } from '@/api/equipment'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, MarkLineComponent])

const activeTab = ref('params')
const equipmentList = ref([])
const formData = reactive({
  equipmentId: null, totalTime: 500, repairCount: 50,
  targetM: 0.90, confidence: 0.90,
})
const calcInputs = reactive({ totalTime: 500, repairCount: 50, targetM: 0.90, confidence: 0.9 })
const calcResult = ref(null)

const levelTable = [
  { level: 'Ⅰ级', mttr: '≤ 0.5', desc: '优秀', type: 'success' },
  { level: 'Ⅱ级', mttr: '≤ 1.0', desc: '良好', type: 'success' },
  { level: 'Ⅲ级', mttr: '≤ 2.0', desc: '中等', type: 'warning' },
  { level: 'Ⅳ级', mttr: '≤ 4.0', desc: '一般', type: 'warning' },
  { level: 'Ⅴ级', mttr: '> 4.0', desc: '较差', type: 'danger' },
]

const result = computed(() => calcMaintainability(formData.totalTime, formData.repairCount, formData.targetM, formData.confidence))

function calcMaintainability(totalTime, repairCount, targetM, confidence) {
  const mttr = repairCount > 0 ? totalTime / repairCount : totalTime
  const mu = repairCount > 0 ? repairCount / totalTime : 0
  const alpha = 1 - confidence
  const df = 2 * repairCount
  const chi2 = df > 0 ? approximateChi2(alpha, df) : 0
  const mttrUpper = chi2 > 0 ? (2 * totalTime) / chi2 : totalTime
  const t_star = mttr > 0 && targetM < 1 ? -mttr * Math.log(1 - targetM) : 0
  const M_at_t = mttr > 0 ? 1 - Math.exp(-t_star / mttr) : 0
  let level, desc
  if (mttr <= 0.5) { level = 'Ⅰ级 — 优秀'; desc = '优秀' }
  else if (mttr <= 1.0) { level = 'Ⅱ级 — 良好'; desc = '良好' }
  else if (mttr <= 2.0) { level = 'Ⅲ级 — 中等'; desc = '中等' }
  else if (mttr <= 4.0) { level = 'Ⅳ级 — 一般'; desc = '一般' }
  else { level = 'Ⅴ级 — 较差'; desc = '较差' }
  return { mttrPoint: mttr, repairRate: mu, mttrUpper, timeToTarget: t_star, maintainabilityAtTarget: M_at_t, level }
}

const levelColor = computed(() => {
  const l = result.value.level
  if (l.startsWith('Ⅰ')) return '#22c55e'
  if (l.startsWith('Ⅱ')) return '#4ade80'
  if (l.startsWith('Ⅲ')) return '#f59e0b'
  return '#ef4444'
})

const curveOption = computed(() => {
  const mttr = result.value.mttrPoint
  const maxT = Math.max(8, mttr * 6)
  const steps = 50
  const step = maxT / steps
  const data = Array.from({ length: steps + 1 }, (_, i) => {
    const t = i * step
    const m = mttr > 0 ? 1 - Math.exp(-t / mttr) : 0
    return [parseFloat(t.toFixed(1)), parseFloat(m.toFixed(4))]
  })
  const t_star = result.value.timeToTarget

  return {
    tooltip: { trigger: 'axis', formatter: p => `t=${p[0][0]}h<br/>M(t)=${(p[0][1] * 100).toFixed(2)}%` },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value', name: '时间 t (h)',
      axisLine: { lineStyle: { color: 'rgba(74,222,128,0.2)' } },
      axisLabel: { color: '#8ba8c8' },
      splitLine: { lineStyle: { color: 'rgba(0,212,255,0.06)' } }
    },
    yAxis: {
      type: 'value', min: 0, max: 1, name: '维修度 M(t)',
      axisLine: { lineStyle: { color: 'rgba(74,222,128,0.2)' } },
      axisLabel: { color: '#8ba8c8', formatter: v => (v * 100).toFixed(0) + '%' },
      splitLine: { lineStyle: { color: 'rgba(0,212,255,0.06)' } }
    },
    series: [{
      type: 'line', smooth: true,
      data,
      lineStyle: { color: '#4ade80', width: 2 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(74,222,128,0.3)' }, { offset: 1, color: 'rgba(74,222,128,0.02)' }] } },
      itemStyle: { color: '#4ade80' },
      markLine: {
        silent: true,
        lineStyle: { color: '#f87171', type: 'dashed' },
        data: [
          { xAxis: t_star, label: { formatter: `t*=${t_star.toFixed(1)}h`, color: '#f87171' } },
          { yAxis: formData.targetM, label: { formatter: `M*=${(formData.targetM * 100).toFixed(0)}%`, color: '#f87171' } }
        ]
      },
      markPoint: { data: [{ coord: [t_star, formData.targetM], name: '目标点' }] }
    }]
  }
})

function approximateChi2(p, df) {
  if (df <= 0) return 0
  const z = inverseNormalCDF(p)
  const term1 = 2 / (9 * df)
  const term2 = z * Math.sqrt(term1)
  const term3 = 1 - term1
  return Math.max(df * Math.pow(term3 + term2, 3), 0.001)
}

function inverseNormalCDF(p) {
  if (p <= 0) return -8
  if (p >= 1) return 8
  if (p <= 0.5) {
    const t = Math.sqrt(-2 * Math.log(p))
    return -(t - (0.010328 * t + 0.802853) * t + 2.515517) /
           ((0.001308 * t + 0.189269) * t + 1.432788) + t
  } else return -inverseNormalCDF(1 - p)
}

function runCalc() {
  calcResult.value = calcMaintainability(calcInputs.totalTime, calcInputs.repairCount, calcInputs.targetM, calcInputs.confidence)
}

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

async function saveData() {
  try { await saveMaintainability({ equipmentId: formData.equipmentId, mttr: result.value.mttrPoint }); ElMessage.success('保存成功') }
  catch { ElMessage.error('保存失败') }
}

onMounted(() => loadEquipment())
</script>

<style scoped lang="scss">
.result-stat {
  background: var(--bg-surface);
  border: 1px solid var(--bg-border);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  margin-bottom: 12px;
  .result-label { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }
  .result-value { font-size: 20px; font-weight: 700; font-family: var(--font-display); }
}
.chart-box { height: 380px; }
.formula-title { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; letter-spacing: 0.05em; }
.formula { font-family: var(--font-mono); color: var(--color-accent); font-size: 15px; background: var(--bg-card); padding: 8px 12px; border-radius: 4px; }
.form-hint { font-size: 11px; color: var(--text-muted); margin-left: 8px; }
</style>
