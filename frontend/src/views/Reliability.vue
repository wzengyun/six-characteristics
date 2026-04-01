<template>
  <div class="reliability-page">
    <el-row :gutter="20">
      <!-- 左侧：装备选择 -->
      <el-col :span="5">
        <el-card>
          <template #header><span class="card-title">选择装备</span></template>
          <el-input v-model="searchKey" placeholder="搜索装备" prefix-icon="Search" clearable style="margin-bottom:12px" />
          <div class="equipment-list">
            <div
              v-for="eq in filteredEquipment"
              :key="eq.id"
              class="equipment-item"
              :class="{ active: selectedId === eq.id }"
              @click="selectEquipment(eq)"
            >
              <div class="eq-name">{{ eq.name }}</div>
              <div class="eq-model">{{ eq.model }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：分析内容 -->
      <el-col :span="19">
        <template v-if="selectedEquipment">
          <el-card class="analysis-card">
            <template #header>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span class="card-title">可靠性分析 — {{ selectedEquipment.name }}</span>
                <div>
                  <el-button @click="showCalculator = !showCalculator">
                    <el-icon><Cpu /></el-icon>{{ showCalculator ? '隐藏计算器' : 'MTBF计算器' }}
                  </el-button>
                  <el-button type="primary" @click="saveData"><el-icon><Check /></el-icon>保存</el-button>
                </div>
              </div>
            </template>

            <el-tabs v-model="activeTab">
              <!-- ===== 试验参数 ===== -->
              <el-tab-pane label="试验参数" name="params">
                <el-form :model="testParams" label-width="150px">
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <el-form-item label="总试验时间 T (台时)">
                        <el-input-number v-model="testParams.totalTime" :min="0" :step="100" style="width:100%" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="故障次数 r">
                        <el-input-number v-model="testParams.failureCount" :min="0" :step="1" style="width:100%" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="任务时间 t₀ (h)">
                        <el-input-number v-model="testParams.missionTime" :min="0" :step="100" style="width:100%" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="MTBF 目标 θ₀ (h)">
                        <el-input-number v-model="testParams.mtbf0" :min="0" :step="100" style="width:100%" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="MTBF 门槛 θ₁ (h)">
                        <el-input-number v-model="testParams.mtbf1" :min="0" :step="100" style="width:100%" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="置信度">
                        <el-select v-model="testParams.confidence" style="width:100%">
                          <el-option label="60%" :value="0.6" />
                          <el-option label="70%" :value="0.7" />
                          <el-option label="80%" :value="0.8" />
                          <el-option label="90%" :value="0.9" />
                          <el-option label="95%" :value="0.95" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="分布类型">
                        <el-select v-model="testParams.distributionType" style="width:100%">
                          <el-option label="指数分布" value="exponential" />
                          <el-option label="威布尔分布" value="weibull" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <template v-if="testParams.distributionType === 'weibull'">
                      <el-col :span="8">
                        <el-form-item label="尺度参数 η (h)">
                          <el-input-number v-model="testParams.eta" :min="0" :precision="0" style="width:100%" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="形状参数 β">
                          <el-input-number v-model="testParams.beta" :min="0.1" :precision="2" :step="0.1" style="width:100%" />
                        </el-form-item>
                      </el-col>
                    </template>
                  </el-row>

                  <!-- GJB 899A 试验方案 -->
                  <el-divider content-position="left">
                    <span style="color:var(--color-accent);font-weight:600">GJB 899A 统计试验方案</span>
                  </el-divider>
                  <el-row :gutter="12" align="middle">
                    <el-col :span="4">
                      <el-select v-model="testParams.planType" placeholder="选择方案" style="width:100%">
                        <el-option v-for="p in planOptions" :key="p" :label="'方案' + p" :value="p" />
                      </el-select>
                    </el-col>
                    <el-col :span="4">
                      <el-button type="primary" @click="calcTestPlan">计算方案参数</el-button>
                    </el-col>
                    <el-col :span="16" v-if="testPlanResult">
                      <el-descriptions :column="3" border size="small">
                        <el-descriptions-item label="生产者风险α">{{ testPlanResult.producerRisk }}</el-descriptions-item>
                        <el-descriptions-item label="消费者风险β">{{ testPlanResult.consumerRisk }}</el-descriptions-item>
                        <el-descriptions-item label="鉴别比γ">{{ testPlanResult.discriminationRatio }}</el-descriptions-item>
                        <el-descriptions-item label="允许故障数r">{{ testPlanResult.maxFailures }}</el-descriptions-item>
                        <el-descriptions-item label="总试验时间">
                          <span class="text-accent">{{ testPlanResult.totalTestTimeHours }} 台时</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="MTBF门槛">{{ testPlanResult.mtbf1 }} h</el-descriptions-item>
                      </el-descriptions>
                    </el-col>
                  </el-row>

                  <!-- 计算结果 -->
                  <el-divider content-position="left">
                    <span style="color:var(--color-accent);font-weight:600">GJB 450A / GJB 899A 计算结果</span>
                  </el-divider>
                  <el-row :gutter="20">
                    <el-col :span="6">
                      <div class="result-stat">
                        <div class="result-label">MTBF 点估计</div>
                        <div class="result-value text-accent">{{ mtbfResult.point }} h</div>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="result-stat">
                        <div class="result-label">MTBF 置信下限 θ_L</div>
                        <div class="result-value text-warning">{{ mtbfResult.lower }} h</div>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="result-stat">
                        <div class="result-label">MTBF 置信上限 θ_U</div>
                        <div class="result-value">{{ mtbfResult.upper === '∞' ? '∞' : mtbfResult.upper + ' h' }}</div>
                      </div>
                    </el-col>
                    <el-col :span="6">
                      <div class="result-stat">
                        <div class="result-label">可靠度 R(t₀)</div>
                        <div class="result-value" :style="{ color: reliabilityColor }">{{ (mtbfResult.reliability * 100).toFixed(2) }}%</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="result-stat">
                        <div class="result-label">可靠度置信下限</div>
                        <div class="result-value text-muted">{{ (mtbfResult.reliabilityLower * 100).toFixed(2) }}%</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="result-stat">
                        <div class="result-label">故障率 λ</div>
                        <div class="result-value text-muted">{{ mtbfResult.faultRate }} /h</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="result-stat">
                        <div class="result-label">威布尔 MTBF (若适用)</div>
                        <div class="result-value text-muted">{{ mtbfResult.weibullMtbfs || '-' }} h</div>
                      </div>
                    </el-col>
                  </el-row>

                  <!-- 可靠性框图 -->
                  <el-divider content-position="left">
                    <span style="color:var(--color-accent);font-weight:600">可靠性框图 (RBD)</span>
                  </el-divider>
                  <div class="rbd-container">
                    <div class="rbd-tree">
                      <div class="rbd-node system">
                        <div class="gate-icon">SYS</div>
                        <div class="node-label">系统 R = {{ systemReliability.toFixed(4) }}</div>
                      </div>
                      <div class="rbd-arrow">▼</div>
                      <div class="rbd-branch">
                        <div v-for="(block, i) in rbdBlocks" :key="i" class="rbd-node block">
                          <div class="node-label">{{ block.name }}</div>
                          <div class="block-r">R = {{ block.reliability.toFixed(4) }}</div>
                          <el-input-number v-model="block.reliability" :min="0" :max="1" :step="0.01" size="small" style="width:100px;margin-top:4px" />
                        </div>
                      </div>
                    </div>
                    <div class="rbd-calc">
                      <el-tag type="info">串联系统: R_sys = Π R_i</el-tag>
                      <el-tag type="info">并联系统: R_sys = 1 - Π (1-R_i)</el-tag>
                      <el-tag type="info">混联: 分段计算后合并</el-tag>
                    </div>
                  </div>
                </el-form>
              </el-tab-pane>

              <!-- ===== 可靠性曲线 ===== -->
              <el-tab-pane label="可靠性曲线" name="curve">
                <el-row :gutter="20" style="margin-bottom:16px">
                  <el-col :span="8">
                    <el-form-item label="曲线类型">
                      <el-radio-group v-model="curveType">
                        <el-radio value="exponential">指数分布</el-radio>
                        <el-radio value="weibull">威布尔分布</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="时间范围 (h)">
                      <el-slider v-model="curveRange" range :min="0" :max="10000" :step="100" />
                      <span class="text-muted">{{ curveRange[0] }} ~ {{ curveRange[1] }} h</span>
                    </el-form-item>
                  </el-col>
                </el-row>
                <div class="chart-box">
                  <v-chart :option="curveOption" autoresize />
                </div>
              </el-tab-pane>

              <!-- ===== MTBF 计算器 ===== -->
              <el-tab-pane label="MTBF计算器" name="calculator">
                <el-card shadow="never" style="background:var(--bg-surface)">
                  <el-form :inline="true" :model="calcInputs">
                    <el-form-item label="总试验时间">
                      <el-input-number v-model="calcInputs.totalTime" :min="0" style="width:150px" />
                    </el-form-item>
                    <el-form-item label="故障次数">
                      <el-input-number v-model="calcInputs.failureCount" :min="0" style="width:120px" />
                    </el-form-item>
                    <el-form-item label="置信度">
                      <el-select v-model="calcInputs.confidence" style="width:100px">
                        <el-option label="60%" :value="0.6" />
                        <el-option label="70%" :value="0.7" />
                        <el-option label="80%" :value="0.8" />
                        <el-option label="90%" :value="0.9" />
                        <el-option label="95%" :value="0.95" />
                      </el-select>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="runCalculation">计算</el-button>
                    </el-form-item>
                  </el-form>

                  <el-divider />
                  <el-descriptions :column="2" border v-if="calcResult">
                    <el-descriptions-item label="MTBF 点估计">{{ calcResult.point }} h</el-descriptions-item>
                    <el-descriptions-item label="故障率 λ">{{ calcResult.faultRate }} /h</el-descriptions-item>
                    <el-descriptions-item label="MTBF 单侧置信下限">{{ calcResult.lower }} h</el-descriptions-item>
                    <el-descriptions-item label="MTBF 单侧置信上限">{{ calcResult.upper === '∞' ? '∞' : calcResult.upper + ' h' }}</el-descriptions-item>
                    <el-descriptions-item label="R(t=1000h) 点估计">{{ calcResult.rAt1000 }}%</el-descriptions-item>
                    <el-descriptions-item label="R(t=1000h) 置信下限">{{ calcResult.rLowerAt1000 }}%</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </el-tab-pane>
            </el-tabs>
          </el-card>

          <!-- 评估结果 -->
          <el-card class="result-card">
            <template #header><span class="card-title">可靠性评估结论</span></template>
            <el-descriptions :column="3" border>
              <el-descriptions-item label="MTBF 评估值">{{ testParams.mtbf0 }} h</el-descriptions-item>
              <el-descriptions-item label="MTBF 置信下限">
                <span :class="mtbfResult.lower >= testParams.mtbf1 ? 'text-success' : 'text-danger'">
                  {{ mtbfResult.lower }} h
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="评估等级">
                <el-tag :type="ratingType">{{ ratingText }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="设计标准">GJB 450A-2020</el-descriptions-item>
              <el-descriptions-item label="试验标准">GJB 899A-2009</el-descriptions-item>
              <el-descriptions-item label="推荐方案">{{ testParams.planType }}</el-descriptions-item>
            </el-descriptions>
            <div class="conclusion-text">
              <p><strong>分析结论：</strong>{{ conclusionText }}</p>
              <p><strong>建议：</strong>{{ suggestionText }}</p>
            </div>
          </el-card>
        </template>

        <el-empty v-else description="请从左侧选择装备进行分析" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { listEquipment, saveReliability } from '@/api/equipment'
import { ElMessage } from 'element-plus'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const searchKey = ref('')
const equipmentList = ref([])
const selectedId = ref(null)
const selectedEquipment = ref(null)
const activeTab = ref('params')
const showCalculator = ref(false)
const curveType = ref('exponential')
const curveRange = ref([0, 5000])
const testPlanResult = ref(null)
const calcResult = ref(null)

const planOptions = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X', 'XI', 'XII', 'XIII', 'XIV']

const testParams = reactive({
  totalTime: 10000,
  failureCount: 5,
  missionTime: 1000,
  mtbf0: 2000,
  mtbf1: 1000,
  confidence: 0.9,
  distributionType: 'exponential',
  planType: 'X',
  eta: 3000,
  beta: 2.0,
})

const calcInputs = reactive({ totalTime: 10000, failureCount: 5, confidence: 0.9 })

const rbdBlocks = ref([
  { name: '模块 A', reliability: 0.95 },
  { name: '模块 B', reliability: 0.92 },
  { name: '模块 C', reliability: 0.98 },
])

const filteredEquipment = computed(() =>
  equipmentList.value.filter(e => e.name.includes(searchKey.value) || e.model.includes(searchKey.value))
)

const mtbfResult = computed(() => {
  const T = testParams.totalTime
  const r = testParams.failureCount
  const t = testParams.missionTime
  const conf = testParams.confidence
  const mtbfPt = r > 0 ? T / r : T
  const alpha = 1 - conf
  // 近似 χ² 分位数
  const df_lower = 2 * r + 2
  const df_upper = 2 * r
  const chi2_lower = approximateChi2(1 - alpha, df_lower)
  const chi2_upper = r > 0 ? approximateChi2(alpha, df_upper) : Infinity
  const lower = chi2_lower > 0 ? (2 * T) / chi2_lower : T
  const upper = chi2_upper > 0 && chi2_upper < Infinity ? (2 * T) / chi2_upper : Infinity
  const reliability = mtbfPt > 0 ? Math.exp(-t / mtbfPt) : 1
  const reliabilityLower = upper < Infinity && upper > 0 ? Math.exp(-t / upper) : 0
  const faultRate = mtbfPt > 0 ? (1 / mtbfPt) : 0

  // 威布尔
  const eta = testParams.eta
  const beta = testParams.beta
  const gamma_weibull = gamma(1 + 1 / beta)
  const weibullMtbf = eta > 0 && beta > 0 ? eta * gamma_weibull : null

  return {
    point: Math.round(mtbfPt),
    lower: Math.round(lower),
    upper: upper === Infinity ? '∞' : Math.round(upper),
    reliability,
    reliabilityLower,
    faultRate: faultRate.toExponential(3),
    weibullMtbfs: weibullMtbf ? Math.round(weibullMtbf) : null,
  }
})

const reliabilityColor = computed(() => {
  const r = mtbfResult.value.reliability
  if (r >= 0.95) return '#22c55e'
  if (r >= 0.90) return '#f59e0b'
  return '#ef4444'
})

const ratingType = computed(() => {
  const lower = mtbfResult.value.lower
  if (lower >= testParams.mtbf0) return 'success'
  if (lower >= testParams.mtbf1) return 'warning'
  return 'danger'
})

const ratingText = computed(() => {
  const lower = mtbfResult.value.lower
  if (lower >= testParams.mtbf0) return 'A级 — MTBF置信下限≥MTBF目标'
  if (lower >= testParams.mtbf1) return 'B级 — MTBF置信下限≥MTBF门槛'
  return 'C级 — 未达到最低要求'
})

const conclusionText = computed(() => {
  const lower = mtbfResult.value.lower
  if (lower >= testParams.mtbf0) return `该装备 MTBF 点估计为 ${mtbfResult.value.point}h，置信下限 θ_L=${lower}h ≥ MTBF目标 θ₀=${testParams.mtbf0}h，满足 GJB 899A 方案${testParams.planType}接收准则，可靠性指标合格。`
  if (lower >= testParams.mtbf1) return `该装备 MTBF 点估计为 ${mtbfResult.value.point}h，置信下限 θ_L=${lower}h 介于门槛与目标之间，建议改进。`
  return `该装备 MTBF 置信下限 θ_L=${lower}h < MTBF门槛 θ₁=${testParams.mtbf1}h，未达到最低可接受值，建议开展可靠性增长工作。`
})

const suggestionText = computed(() => {
  const lower = mtbfResult.value.lower
  if (lower >= testParams.mtbf0) return '继续保持现有可靠性维护策略，定期进行可靠性验证试验。'
  return '建议识别主要故障模式，开展 FMEA 分析，针对性改进薄弱环节。'
})

const systemReliability = computed(() => {
  let r = 1
  rbdBlocks.value.forEach(b => { r *= b.reliability })
  return r
})

const curveOption = computed(() => {
  const mtbf = testParams.mtbf0
  const eta = testParams.eta
  const beta = testParams.beta
  const [tMin, tMax] = curveRange.value
  const steps = 50
  const step = (tMax - tMin) / steps
  const data = Array.from({ length: steps + 1 }, (_, i) => {
    const t = tMin + i * step
    const r = curveType.value === 'exponential' && mtbf > 0
      ? Math.exp(-t / mtbf)
      : eta > 0 && beta > 0 ? Math.exp(-Math.pow(t / eta, beta)) : 1
    return [Math.round(t), parseFloat(r.toFixed(4))]
  })
  const label = curveType.value === 'exponential' ? '指数分布 R(t)=e^(-t/θ)' : `威布尔分布 R(t)=exp(-(t/η)^β)`

  return {
    tooltip: { trigger: 'axis', formatter: p => `t=${p[0][0]}h<br/>R(t)=${(p[0][1] * 100).toFixed(2)}%` },
    legend: { data: [label], textStyle: { color: '#8ba8c8' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value', name: '时间 t (h)',
      axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
      axisLabel: { color: '#8ba8c8' },
      splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' } }
    },
    yAxis: [
      {
        type: 'value', min: 0, max: 1, name: '可靠度 R(t)',
        axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
        axisLabel: { color: '#8ba8c8', formatter: v => v.toFixed(1) },
        splitLine: { lineStyle: { color: 'rgba(0,212,255,0.08)' } }
      }
    ],
    series: [{
      name: label, type: 'line', smooth: true,
      data,
      lineStyle: { color: '#00d4ff', width: 2 },
      areaStyle: {
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [{ offset: 0, color: 'rgba(0,212,255,0.3)' }, { offset: 1, color: 'rgba(0,212,255,0.02)' }] }
      },
      itemStyle: { color: '#00d4ff' },
      markLine: {
        silent: true,
        lineStyle: { color: '#f87171', type: 'dashed' },
        data: [{ xAxis: testParams.missionTime, label: { formatter: `t₀=${testParams.missionTime}h`, color: '#f87171' } }]
      },
      markPoint: {
        data: [{ type: 'max', name: '最大可靠度' }, { type: 'min', name: '最小可靠度' }]
      }
    }]
  }
})

// ===== 工具函数 =====
function approximateChi2(p, df) {
  if (df <= 0) return 0
  const z = inverseNormalCDF(p)
  const term1 = 2 / (9 * df)
  const term2 = z * Math.sqrt(term1)
  const term3 = 1 - term1
  const val = df * Math.pow(term3 + term2, 3)
  return Math.max(val, 0.001)
}

function inverseNormalCDF(p) {
  if (p <= 0) return -8
  if (p >= 1) return 8
  if (p <= 0.5) {
    const t = Math.sqrt(-2 * Math.log(p))
    return -(t - (0.010328 * t + 0.802853) * t + 2.515517) /
           ((0.001308 * t + 0.189269) * t + 1.432788) + t
  } else {
    return -inverseNormalCDF(1 - p)
  }
}

function gamma(x) {
  if (x <= 0) return 1
  if (x < 0.5) return Math.PI / (Math.sin(Math.PI * x) * gamma(1 - x))
  x -= 1
  const g = 7, c = [0.99999999999980993, 676.5203681218851, -1259.1392167224028,
    771.32342877765313, -176.61502916214059, 12.507343278686905,
    -0.13857109526572012, 9.9843695780195716e-6, 1.5056327351493116e-7]
  let a = c[0]
  for (let i = 1; i < g + 2; i++) a += c[i] / (x + i)
  const t = x + g + 0.5
  return Math.sqrt(2 * Math.PI) * Math.pow(t, x + 0.5) * Math.exp(-t) * a
}

// ===== 计算函数 =====
function calcTestPlan() {
  // 简化计算，实际从后端获取
  const gamma = testParams.mtbf0 / testParams.mtbf1
  const plans = {
    I: [0.20, 0.20, 1.50, 9.0, 3], II: [0.20, 0.20, 1.50, 18.5, 6],
    III: [0.20, 0.20, 1.50, 30.0, 9], IV: [0.20, 0.20, 1.50, 45.5, 14],
    V: [0.20, 0.20, 1.50, 65.5, 20], VI: [0.30, 0.30, 1.50, 4.3, 1],
    VII: [0.30, 0.30, 1.50, 7.8, 2], VIII: [0.30, 0.30, 1.50, 12.5, 3],
    IX: [0.30, 0.30, 1.50, 18.2, 5], X: [0.30, 0.30, 1.50, 25.3, 7],
    XI: [0.20, 0.20, 2.00, 5.4, 2], XII: [0.20, 0.20, 2.00, 9.9, 3],
    XIII: [0.20, 0.20, 3.00, 4.3, 2], XIV: [0.20, 0.20, 3.00, 7.3, 3],
  }
  const p = plans[testParams.planType] || plans['X']
  const n = p[3]
  const totalTime = Math.round(n * testParams.mtbf1)
  testPlanResult.value = {
    producerRisk: p[0], consumerRisk: p[1],
    discriminationRatio: Math.round(gamma * 100) / 100,
    maxFailures: p[4],
    totalTestTimeHours: totalTime,
    mtbf1: testParams.mtbf1,
  }
}

function runCalculation() {
  const T = calcInputs.totalTime
  const r = calcInputs.failureCount
  const conf = calcInputs.confidence
  const alpha = 1 - conf
  const mtbfPt = r > 0 ? T / r : T
  const df_lower = 2 * r + 2
  const df_upper = 2 * r
  const chi2_lower = approximateChi2(1 - alpha, df_lower)
  const chi2_upper = r > 0 ? approximateChi2(alpha, df_upper) : Infinity
  const lower = chi2_lower > 0 ? (2 * T) / chi2_lower : T
  const upper = chi2_upper > 0 && chi2_upper < Infinity ? (2 * T) / chi2_upper : Infinity
  const rAt1000 = mtbfPt > 0 ? Math.exp(-1000 / mtbfPt) : 1
  const rLowerAt1000 = upper < Infinity && upper > 0 ? Math.exp(-1000 / upper) : 0

  calcResult.value = {
    point: Math.round(mtbfPt),
    faultRate: (1 / mtbfPt).toExponential(3),
    lower: Math.round(lower),
    upper: upper === Infinity ? '∞' : Math.round(upper),
    rAt1000: (rAt1000 * 100).toFixed(2),
    rLowerAt1000: (rLowerAt1000 * 100).toFixed(2),
  }
}

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch {
    equipmentList.value = [
      { id: 1, name: '某型雷达系统', model: 'LD-2000A' },
      { id: 2, name: '某型通信设备', model: 'TX-500B' },
    ]
  }
}

function selectEquipment(eq) {
  selectedId.value = eq.id
  selectedEquipment.value = eq
  calcTestPlan()
}

async function saveData() {
  try {
    await saveReliability({ equipmentId: selectedId.value, mtbf: mtbfResult.value.point })
    ElMessage.success('保存成功')
  } catch { ElMessage.error('保存失败') }
}

onMounted(() => { loadEquipment(); calcTestPlan() })
</script>

<style scoped lang="scss">
.equipment-list { max-height: 500px; overflow-y: auto; }
.equipment-item {
  padding: 12px;
  border: 1px solid var(--bg-border);
  border-radius: var(--radius-md);
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: var(--bg-hover); }
  &.active { border-color: var(--color-accent); background: rgba(0,212,255,0.08); }
}
.eq-name { font-weight: 500; color: var(--text-primary); }
.eq-model { font-size: 12px; color: var(--text-muted); margin-top: 4px; }

.analysis-card { margin-bottom: 20px; }
.chart-box { height: 380px; }

.result-stat {
  background: var(--bg-surface);
  border: 1px solid var(--bg-border);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  margin-bottom: 12px;
  .result-label { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; letter-spacing: 0.05em; }
  .result-value { font-size: 20px; font-weight: 700; font-family: var(--font-display); }
}

.rbd-container {
  background: var(--bg-surface);
  border-radius: var(--radius-md);
  padding: 20px;
  .rbd-tree { display: flex; flex-direction: column; align-items: center; gap: 16px; }
  .rbd-node {
    background: var(--bg-card);
    border: 1px solid var(--bg-border);
    border-radius: var(--radius-md);
    padding: 12px 20px;
    min-width: 120px;
    text-align: center;
    &.system { border-color: #00d4ff; background: rgba(0,212,255,0.08); }
  }
  .gate-icon { font-size: 11px; font-weight: 600; color: var(--color-accent); margin-bottom: 4px; }
  .node-label { font-size: 13px; color: var(--text-primary); }
  .block-r { font-size: 12px; color: var(--color-accent); }
  .rbd-arrow { color: var(--color-accent); font-size: 20px; }
  .rbd-branch { display: flex; gap: 16px; flex-wrap: wrap; justify-content: center; }
  .rbd-calc { display: flex; gap: 8px; justify-content: center; margin-top: 16px; flex-wrap: wrap; }
}

.result-card { margin-top: 20px; }
.conclusion-text {
  margin-top: 16px;
  padding: 12px 16px;
  background: rgba(0,212,255,0.04);
  border-radius: var(--radius-md);
  p { color: var(--text-secondary); line-height: 1.8; font-size: 13px; margin: 4px 0; }
}
</style>
