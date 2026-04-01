<template>
  <div class="testability-page">
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="card-title">测试性分析 (GJB 2547A-2012)</span>
          <el-button type="primary" @click="saveData"><el-icon><Check /></el-icon>保存</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- ===== 测试性参数 ===== -->
        <el-tab-pane label="测试性参数" name="params">
          <el-form :model="formData" label-width="160px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="装备名称">
                  <el-select v-model="formData.equipmentId" style="width:100%">
                    <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="注入故障总数 N">
                  <el-input-number v-model="formData.totalFaults" :min="1" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="检测到故障数 r">
                  <el-input-number v-model="formData.detectedFaults" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="隔离到≤L个单元数">
                  <el-input-number v-model="formData.isolatedFaults" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="隔离深度 L">
                  <el-input-number v-model="formData.isolationDepth" :min="1" :max="10" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="虚警次数">
                  <el-input-number v-model="formData.falseAlarms" :min="0" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="总报警次数">
                  <el-input-number v-model="formData.totalAlarms" :min="1" style="width:100%" />
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
            <span style="color:#f59e0b;font-weight:600">GJB 2547A 计算结果</span>
          </el-divider>
          <el-row :gutter="16">
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">FDR 点估计</div>
                <div class="result-value" :style="{ color: fdrColor }">{{ result.fdr.toFixed(2) }}%</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">FDR 置信下限</div>
                <div class="result-value text-warning">{{ result.fdrLower.toFixed(2) }}%</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">FIR 点估计</div>
                <div class="result-value" :style="{ color: firColor }">{{ result.fir.toFixed(2) }}%</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">FIR 置信下限</div>
                <div class="result-value text-warning">{{ result.firLower.toFixed(2) }}%</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">虚警率 FAR</div>
                <div class="result-value text-muted">{{ result.far.toFixed(2) }}%</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">FDR 等级</div>
                <div class="result-value" :style="{ color: fdrColor }">{{ fdrLevelText }}</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="result-stat">
                <div class="result-label">综合评级</div>
                <div class="result-value">{{ overallLevelText }}</div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- ===== 抽样方案 ===== -->
        <el-tab-pane label="测试性抽样方案" name="sample">
          <el-form :model="samplePlan" label-width="180px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="目标 FDR₀ (%)">
                  <el-input-number v-model="samplePlan.targetFdr" :min="0" :max="100" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="门槛 FDR₁ (%)">
                  <el-input-number v-model="samplePlan.thresholdFdr" :min="0" :max="100" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="生产者风险 α">
                  <el-select v-model="samplePlan.alpha" style="width:100%">
                    <el-option label="10%" :value="0.1" />
                    <el-option label="20%" :value="0.2" />
                    <el-option label="30%" :value="0.3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="消费者风险 β">
                  <el-select v-model="samplePlan.beta" style="width:100%">
                    <el-option label="10%" :value="0.1" />
                    <el-option label="20%" :value="0.2" />
                    <el-option label="30%" :value="0.3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item>
                  <el-button type="warning" @click="calcSamplePlan">计算抽样方案</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <template v-if="sampleResult">
            <el-divider content-position="left">
              <span style="color:#f59e0b;font-weight:600">GJB 2547A Table B1 抽样方案</span>
            </el-divider>
            <el-descriptions :column="2" border style="margin-bottom:16px">
              <el-descriptions-item label="样本量（检测故障数）">
                <span class="text-accent" style="font-weight:700;font-size:18px">{{ sampleResult.sampleSize }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="接收判定值 a">
                <span class="text-accent" style="font-weight:700;font-size:18px">{{ sampleResult.acceptNumber }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="目标 FDR₀">{{ sampleResult.targetFdr }}%</el-descriptions-item>
              <el-descriptions-item label="门槛 FDR₁">{{ sampleResult.thresholdFdr }}%</el-descriptions-item>
              <el-descriptions-item label="生产者风险 α">{{ (sampleResult.producerRisk * 100).toFixed(0) }}%</el-descriptions-item>
              <el-descriptions-item label="消费者风险 β">{{ (sampleResult.consumerRisk * 100).toFixed(0) }}%</el-descriptions-item>
            </el-descriptions>

            <el-alert type="success" :closable="false" style="margin-bottom:12px">
              <template #title>
                <strong>接收条件：</strong>注入 {{ sampleResult.sampleSize }} 个故障，检测成功数 ≥ {{ sampleResult.acceptNumber }}
                → FDR ≥ {{ sampleResult.targetFdr }}%，判定合格
              </template>
            </el-alert>
            <el-alert type="danger" :closable="false">
              <template #title>
                <strong>拒收条件：</strong>检测成功数 &lt; {{ sampleResult.acceptNumber }}
                → FDR &lt; {{ sampleResult.thresholdFdr }}%，判定不合格
              </template>
            </el-alert>

            <div class="oc-curve-box">
              <div class="chart-box"><v-chart :option="ocCurveOption" autoresize /></div>
            </div>
          </template>
        </el-tab-pane>

        <!-- ===== 测试性计算器 ===== -->
        <el-tab-pane label="FDR/FIR计算器" name="calculator">
          <el-card shadow="never" style="background:var(--bg-surface)">
            <el-form :inline="true" :model="calcInputs">
              <el-form-item label="检测成功数 r"><el-input-number v-model="calcInputs.detected" :min="0" style="width:120px" /></el-form-item>
              <el-form-item label="注入总数 N"><el-input-number v-model="calcInputs.total" :min="1" style="width:120px" /></el-form-item>
              <el-form-item label="置信度"><el-select v-model="calcInputs.confidence" style="width:100px"><el-option label="80%" :value="0.8" /><el-option label="90%" :value="0.9" /><el-option label="95%" :value="0.95" /></el-select></el-form-item>
              <el-form-item><el-button type="warning" @click="runCalc">计算</el-button></el-form-item>
            </el-form>
            <el-divider />
            <el-descriptions :column="2" border v-if="calcResult_">
              <el-descriptions-item label="FDR 点估计">{{ calcResult_.fdr.toFixed(2) }}%</el-descriptions-item>
              <el-descriptions-item label="FDR 单侧置信下限">{{ calcResult_.fdrLower.toFixed(2) }}%</el-descriptions-item>
              <el-descriptions-item label="鉴别比 DR">{{ calcResult_.dr.toFixed(4) }}</el-descriptions-item>
              <el-descriptions-item label="检测率等级">{{ calcResult_.level }}</el-descriptions-item>
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
import { GridComponent, TooltipComponent } from 'echarts/components'
import { listEquipment, saveTestability } from '@/api/equipment'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const activeTab = ref('params')
const equipmentList = ref([])
const formData = reactive({
  equipmentId: null, totalFaults: 100, detectedFaults: 92,
  isolatedFaults: 88, isolationDepth: 1,
  falseAlarms: 5, totalAlarms: 100, confidence: 0.9,
})
const samplePlan = reactive({ targetFdr: 90, thresholdFdr: 80, alpha: 0.2, beta: 0.2 })
const sampleResult = ref(null)
const calcInputs = reactive({ detected: 92, total: 100, confidence: 0.9 })
const calcResult_ = ref(null)

// GJB 2547A χ² 方法 FDR 置信下限
// FDR_L = 1 - χ²(α; 2(n-r+1)) / (2(n-r+1))
// 其中 n=总注入数, r=检测成功数, α=1-置信度
function approximateChi2(p, df) {
  if (df <= 0) return 0
  const z = inverseNormalCDF(p)
  const term1 = 2 / (9 * df)
  const term2 = z * Math.sqrt(term1)
  const term3 = 1 - term1
  return Math.max(df * Math.pow(term3 + term2, 3), 0.001)
}

function fdrLowerLimitChi2(detected, total, confidence) {
  if (total <= 0) return 0
  if (detected >= total) return 100.0
  const alpha = 1 - confidence
  const undetected = total - detected  // 未检测到故障数
  const df = 2 * (undetected + 1)
  const chi2 = approximateChi2(1 - alpha, df)
  return Math.max(0, (1 - chi2 / (2 * (undetected + 1))) * 100)
}

function firLowerLimitChi2(isolated, detected, confidence) {
  if (detected <= 0) return 0
  if (isolated >= detected) return 100.0
  const alpha = 1 - confidence
  const notIsolated = detected - isolated
  const df = 2 * (notIsolated + 1)
  const chi2 = approximateChi2(1 - alpha, df)
  return Math.max(0, (1 - chi2 / (2 * (notIsolated + 1))) * 100)
}

const result = computed(() => {
  const N = formData.totalFaults
  const r = Math.min(formData.detectedFaults, N)
  const fdr = N > 0 ? (r / N) * 100 : 0
  const fdrLower = fdrLowerLimitChi2(r, N, formData.confidence)
  const fir = r > 0 ? (formData.isolatedFaults / r) * 100 : 0
  const firLower = firLowerLimitChi2(formData.isolatedFaults, r, formData.confidence)
  const far = formData.totalAlarms > 0 ? (formData.falseAlarms / formData.totalAlarms) * 100 : 0
  return { fdr, fdrLower, fir, firLower, far }
})

const fdrColor = computed(() => result.value.fdr >= 95 ? '#22c55e' : result.value.fdr >= 90 ? '#f59e0b' : '#ef4444')
const firColor = computed(() => result.value.fir >= 90 ? '#22c55e' : result.value.fir >= 80 ? '#f59e0b' : '#ef4444')
const fdrLevelText = computed(() => result.value.fdr >= 95 ? 'A级 (≥95%)' : result.value.fdr >= 90 ? 'B级 (≥90%)' : 'C级 (<90%)')
const overallLevelText = computed(() => {
  const avg = (result.value.fdr + result.value.fir) / 2
  return avg >= 92 ? '优秀 — 满足 GJB 2547A' : avg >= 85 ? '良好 — 基本满足' : '待改进 — 未达标'
})

function inverseNormalCDF(p) {
  if (p <= 0) return -8
  if (p >= 1) return 8
  if (p <= 0.5) {
    const t = Math.sqrt(-2 * Math.log(p))
    return -(t - (0.010328 * t + 0.802853) * t + 2.515517) /
           ((0.001308 * t + 0.189269) * t + 1.432788) + t
  } else return -inverseNormalCDF(1 - p)
}

function calcSamplePlan() {
  const { targetFdr, thresholdFdr, alpha, beta } = samplePlan
  const p1 = targetFdr / 100
  const p2 = thresholdFdr / 100
  const p_bar = (p1 + p2) / 2
  const za = inverseNormalCDF(1 - alpha)
  const zb = inverseNormalCDF(1 - beta)
  const num = Math.pow(za + zb, 2) * p_bar * (1 - p_bar)
  const den = Math.pow(p1 - p2, 2)
  const n_approx = Math.max(10, Math.ceil(num / Math.max(den, 0.001)))

  const plans = [[10, 1], [20, 2], [30, 3], [50, 4], [60, 5], [80, 6], [100, 8]]
  let best = plans[0], minDiff = 9999
  for (const [n, a] of plans) {
    const d = Math.abs(n - n_approx)
    if (d < minDiff) { minDiff = d; best = [n, a] }
  }

  sampleResult.value = {
    sampleSize: best[0], acceptNumber: best[1],
    targetFdr, thresholdFdr, producerRisk: alpha, consumerRisk: beta,
  }
}

const ocCurveOption = computed(() => {
  if (!sampleResult.value) return {}
  const { targetFdr, thresholdFdr } = sampleResult.value
  const p1 = targetFdr / 100
  const p2 = thresholdFdr / 100
  const n = sampleResult.value.sampleSize
  const a = sampleResult.value.acceptNumber
  const data = Array.from({ length: 101 }, (_, i) => {
    const p = i / 100
    const r_thresh = a / n
    const prob = p >= r_thresh ? 0.8 + 0.2 * Math.min(1, (p - p2) / (p1 - p2 + 0.001)) : 0.2 * Math.max(0, p / p2)
    return [i, Math.min(100, prob * 100)]
  })
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value', name: 'FDR (%)', min: 50, max: 100,
      axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
      axisLabel: { color: '#8ba8c8' },
      splitLine: { lineStyle: { color: 'rgba(0,212,255,0.06)' } }
    },
    yAxis: {
      type: 'value', name: '接收概率 P(%)', min: 0, max: 100,
      axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
      axisLabel: { color: '#8ba8c8' },
      splitLine: { lineStyle: { color: 'rgba(0,212,255,0.06)' } }
    },
    series: [{
      type: 'line', smooth: true, data,
      lineStyle: { color: '#f59e0b', width: 2 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(245,158,11,0.3)' }, { offset: 1, color: 'rgba(245,158,11,0.02)' }] } },
      itemStyle: { color: '#f59e0b' },
      markLine: {
        silent: true,
        lineStyle: { color: '#22c55e', type: 'dashed' },
        data: [{ xAxis: targetFdr, label: { formatter: `FDR₀=${targetFdr}%`, color: '#22c55e' } }, { xAxis: thresholdFdr, label: { formatter: `FDR₁=${thresholdFdr}%`, color: '#ef4444' } }]
      }
    }]
  }
})

function runCalc() {
  const r = calcInputs.detected
  const n = calcInputs.total
  const fdr = n > 0 ? (r / n) * 100 : 0
  const fdrLower = fdrLowerLimitChi2(r, n, calcInputs.confidence)
  const dr = fdrLower > 0 ? (fdr / fdrLower) : 0
  let level
  if (fdr >= 95) level = 'A级 (优秀)'
  else if (fdr >= 90) level = 'B级 (良好)'
  else level = 'C级 (待改进)'
  calcResult_.value = { fdr, fdrLower, dr, level }
}

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

async function saveData() {
  try { await saveTestability({ equipmentId: formData.equipmentId, fdr: result.value.fdr, fir: result.value.fir }); ElMessage.success('保存成功') }
  catch { ElMessage.error('保存失败') }
}

onMounted(() => { loadEquipment(); calcSamplePlan() })
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
.chart-box { height: 300px; }
.oc-curve-box { margin-top: 16px; }
</style>
