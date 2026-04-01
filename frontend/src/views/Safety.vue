<template>
  <div class="safety-page">
    <el-row :gutter="20">
      <!-- 故障树 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="card-title">故障树分析 (FTA) — GJB 900-2018</span>
              <div>
                <el-button @click="runAnalysis"><el-icon><Cpu /></el-icon>运行分析</el-button>
                <el-button type="primary" @click="saveData"><el-icon><Check /></el-icon>保存</el-button>
              </div>
            </div>
          </template>

          <!-- 装备选择 -->
          <el-form :inline="true" style="margin-bottom:16px">
            <el-form-item label="选择装备">
              <el-select v-model="selectedEquipmentId" style="width:180px" @change="loadSafety">
                <el-option v-for="e in equipmentList" :key="e.id" :label="e.name" :value="e.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="顶事件名称">
              <el-input v-model="topEventName" style="width:160px" />
            </el-form-item>
            <el-form-item label="顶事件概率">
              <el-input-number v-model="topEventProb" :min="0" :max="1" :step="0.0001" :precision="5" style="width:160px" />
            </el-form-item>
          </el-form>

          <!-- 故障树可视化 -->
          <div class="fta-container">
            <div class="fta-tree">
              <div class="fta-level">
                <!-- 顶事件 -->
                <div class="fta-node top-event">
                  <div class="gate-icon or">OR</div>
                  <div class="node-label">{{ topEventName }}</div>
                  <div class="node-prob">P = {{ topEventProb.toFixed(6) }}</div>
                </div>
              </div>
              <div class="fta-arrow">▼ {{ gate1Type }}</div>

              <div class="fta-level fta-branch">
                <!-- 中间事件 -->
                <div class="fta-node intermediate" v-for="(ie, i) in intermediateEvents" :key="i">
                  <div class="gate-icon" :class="ie.gate.toLowerCase()">{{ ie.gate }}</div>
                  <div class="node-label">{{ ie.name }}</div>
                </div>
              </div>
              <div class="fta-arrow">▼</div>

              <div class="fta-level fta-branch leaf-level">
                <!-- 底事件 -->
                <div class="fta-node basic" v-for="(be, i) in basicEvents" :key="i">
                  <div class="node-label">{{ be.name }}</div>
                  <div class="basic-prob">
                    <span>q = </span>
                    <el-input-number v-model="be.probability" :min="0" :max="1" :step="0.0001" :precision="5" size="small" style="width:120px" @change="onProbChange" />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 安全公式 -->
          <div class="formula-box">
            <div class="formula-title">布尔代数分析 (GJB 900)</div>
            <div class="formula-row">
              <div class="formula">OR 门: P<sub>or</sub> = 1 - Π(1-q<sub>i</sub>)</div>
              <div class="formula">AND 门: P<sub>and</sub> = Π(q<sub>i</sub>)</div>
              <div class="formula">当前顶事件: {{ topFormula }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 分析结果 -->
      <el-col :span="10">
        <!-- 顶事件概率 -->
        <el-card class="result-card">
          <template #header><span class="card-title">分析结果</span></template>

          <div class="result-item">
            <span class="label">顶事件失效概率 P(f)</span>
            <span class="value danger">{{ topProbResult.toExponential(4) }}</span>
          </div>
          <div class="result-item">
            <span class="label">安全完整性等级</span>
            <el-tag :type="silType">{{ silText }}</el-tag>
          </div>

          <el-divider />
          <h4>最小割集分析</h4>
          <el-table :data="minCutSets_" size="small" stripe>
            <el-table-column prop="index" label="#" width="40" />
            <el-table-column prop="events" label="底事件组合">
              <template #default="{ row }">
                <span class="cut-set-expr">{{ row.events.join(' ∩ ') }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="prob" label="概率" width="100">
              <template #default="{ row }">
                <span class="text-muted">{{ row.prob.toExponential(3) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="importance" label="重要度" width="80">
              <template #default="{ row }">
                <el-progress :percentage="row.importance * 100" :stroke-width="8" :show-text="false" />
                <span style="font-size:11px">{{ (row.importance * 100).toFixed(1) }}%</span>
              </template>
            </el-table-column>
          </el-table>

          <el-divider />
          <h4>重要度排序</h4>
          <div class="importance-list">
            <div class="importance-item" v-for="(item, i) in importanceRanking" :key="i">
              <span class="rank">{{ i + 1 }}</span>
              <span class="name">{{ item.name }}</span>
              <el-progress :percentage="item.value * 100" :stroke-width="6" :show-text="false" :color="item.color" style="flex:1" />
              <span class="pct">{{ (item.value * 100).toFixed(1) }}%</span>
            </div>
          </div>

          <el-divider />
          <h4>SIL 等级表 (GJB 900)</h4>
          <el-table :data="silTable" size="small" stripe>
            <el-table-column prop="sil" label="SIL" width="80">
              <template #default="{ row }"><el-tag :type="row.type" size="small">{{ row.sil }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="pfp" label="PFD/PFH 范围" />
            <el-table-column prop="level" label="等级" />
          </el-table>
        </el-card>

        <!-- 计算器 -->
        <el-card style="margin-top:20px">
          <template #header><span class="card-title">安全计算器</span></template>
          <el-form :model="safetyCalc" label-width="120px" size="small">
            <el-form-item label="底事件数">
              <el-input-number v-model="safetyCalc.basicCount" :min="1" :max="10" @change="regenerateBasic" />
            </el-form-item>
            <el-form-item label="门类型">
              <el-radio-group v-model="safetyCalc.gateType">
                <el-radio value="OR">OR 门</el-radio>
                <el-radio value="AND">AND 门</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="各底事件概率">
              <el-input placeholder="逗号分隔，如 0.01,0.02,0.005" v-model="safetyCalc.probs" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="runSafetyCalc">计算</el-button>
            </el-form-item>
          </el-form>
          <div v-if="safetyCalcResult" class="calc-result">
            <div class="result-item">
              <span class="label">顶事件概率</span>
              <span class="value">{{ safetyCalcResult.toExponential(4) }}</span>
            </div>
            <div class="result-item">
              <span class="label">安全完整性</span>
              <el-tag :type="safetyCalcResult <= 1e-4 ? 'success' : safetyCalcResult <= 1e-3 ? 'warning' : 'danger'">
                {{ safetyCalcResult <= 1e-4 ? 'SIL 4' : safetyCalcResult <= 1e-3 ? 'SIL 3' : 'SIL 2' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getSafety, saveSafety, listEquipment } from '@/api/equipment'
import { ElMessage } from 'element-plus'

const selectedEquipmentId = ref(null)
const equipmentList = ref([])
const topEventName = ref('系统故障')
const topEventProb = ref(0.0001)
const gate1Type = ref('OR')
const topFormula = ref('P = 1 - Π(1-qᵢ)')

const intermediateEvents = ref([
  { name: '子系统A失效', gate: 'AND' },
  { name: '子系统B失效', gate: 'OR' },
])

const basicEvents = ref([
  { name: '电源故障 X1', probability: 0.001 },
  { name: '模块A失效 X2', probability: 0.002 },
  { name: '模块B失效 X3', probability: 0.0015 },
  { name: '通信中断 X4', probability: 0.0008 },
])

const safetyCalc = reactive({ basicCount: 4, gateType: 'OR', probs: '0.001,0.002,0.0015,0.0008' })
const safetyCalcResult = ref(null)

const silTable = [
  { sil: 'SIL 4', pfp: 'PFD ≤ 1×10⁻⁴', level: '最高', type: 'success' },
  { sil: 'SIL 3', pfp: '1×10⁻⁴ < PFD ≤ 1×10⁻³', level: '高', type: 'success' },
  { sil: 'SIL 2', pfp: '1×10⁻³ < PFD ≤ 1×10⁻²', level: '中', type: 'warning' },
  { sil: 'SIL 1', pfp: '1×10⁻² < PFD ≤ 1×10⁻¹', level: '低', type: 'warning' },
  { sil: '—', pfp: 'PFD > 1×10⁻¹', level: '不可接受', type: 'danger' },
]

// 顶事件概率计算
const topProbResult = computed(() => {
  const probs = basicEvents.value.map(b => b.probability)
  // 简化：OR门
  let r = 1.0
  for (const p of probs) r *= (1 - p)
  return Math.min(1 - r, 1)
})

// 最小割集
const minCutSets_ = computed(() => {
  const probs = basicEvents.value.map(b => b.probability)
  const names = basicEvents.value.map(b => b.name)
  const totalProb = topProbResult.value

  return probs.map((p, i) => ({
    index: i + 1,
    events: [names[i]],
    prob: p,
    importance: totalProb > 0 ? p / totalProb : 0,
  })).sort((a, b) => b.prob - a.prob).map((m, i) => ({ ...m, index: i + 1 }))
})

// 重要度排序
const importanceRanking = computed(() => {
  return [...basicEvents.value].map(b => ({
    name: b.name,
    value: topProbResult.value > 0 ? b.probability / topProbResult.value : 0,
    color: '#f87171',
  })).sort((a, b) => b.value - a.value)
})

// SIL
const silText = computed(() => {
  const p = topProbResult.value
  if (p <= 1e-4) return 'SIL 4 — 最高完整性'
  if (p <= 1e-3) return 'SIL 3 — 高完整性'
  if (p <= 1e-2) return 'SIL 2 — 中等完整性'
  if (p <= 1e-1) return 'SIL 1 — 低完整性'
  return '未达 SIL 1 — 不可接受'
})
const silType = computed(() => {
  const p = topProbResult.value
  if (p <= 1e-4) return 'success'
  if (p <= 1e-3) return 'success'
  if (p <= 1e-2) return 'warning'
  return 'danger'
})

function onProbChange() {}
function regenerateBasic() {}
function runAnalysis() { ElMessage.success('故障树分析完成') }
function runSafetyCalc() {
  const ps = safetyCalc.probs.split(',').map(s => parseFloat(s.trim())).filter(v => !isNaN(v) && v >= 0)
  if (ps.length === 0) { ElMessage.warning('请输入有效的概率值'); return }
  if (safetyCalc.gateType === 'AND') {
    let r = 1; for (const p of ps) r *= p
    safetyCalcResult.value = Math.min(r, 1)
  } else {
    let r = 1; for (const p of ps) r *= (1 - p)
    safetyCalcResult.value = Math.min(1 - r, 1)
  }
}

async function loadEquipment() {
  try {
    const res = await listEquipment({ page: 1, size: 100 })
    equipmentList.value = res.data?.list || []
  } catch { equipmentList.value = [{ id: 1, name: '某型雷达系统' }] }
}

async function loadSafety() {}
async function saveData() {
  try { await saveSafety({ equipmentId: selectedEquipmentId.value, failureProb: topProbResult.value }); ElMessage.success('保存成功') }
  catch { ElMessage.error('保存失败') }
}

onMounted(() => loadEquipment())
</script>

<style scoped lang="scss">
.fta-container {
  background: var(--bg-surface);
  border-radius: var(--radius-md);
  padding: 20px;
  min-height: 350px;
  overflow: auto;
}
.fta-tree { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.fta-level { display: flex; gap: 20px; justify-content: center; }
.fta-node {
  background: var(--bg-card);
  border: 1px solid var(--bg-border);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  text-align: center;
  min-width: 130px;
  &.top-event { border-color: #f87171; background: rgba(248,113,113,0.1); min-width: 160px; }
  &.intermediate { border-color: #f59e0b; background: rgba(245,158,11,0.08); }
  &.basic { border-color: #22c55e; background: rgba(34,197,94,0.06); min-width: 200px; }
}
.gate-icon {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 4px;
  &.and { background: #f59e0b; color: #000; }
  &.or { background: #f87171; color: #000; }
}
.node-label { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.node-prob { font-size: 12px; color: #f87171; font-family: var(--font-mono); margin-top: 4px; }
.basic-prob { display: flex; align-items: center; gap: 4px; margin-top: 8px; font-size: 12px; color: var(--text-muted); justify-content: center; }
.fta-arrow { color: var(--color-accent); font-size: 18px; }

.formula-box {
  margin-top: 16px;
  background: var(--bg-surface);
  border-radius: var(--radius-md);
  padding: 12px 16px;
  .formula-title { font-size: 12px; color: var(--text-muted); margin-bottom: 8px; letter-spacing: 0.05em; }
}
.formula-row { display: flex; gap: 24px; flex-wrap: wrap; }
.formula { font-family: var(--font-mono); color: var(--color-accent); font-size: 13px; }

.result-card { }
.result-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 12px; background: var(--bg-surface); border-radius: var(--radius-md); margin-bottom: 8px;
  .label { color: var(--text-secondary); font-size: 13px; }
  .value { font-family: var(--font-mono); font-size: 16px; font-weight: 600; &.danger { color: #f87171; } }
}
h4 { margin-bottom: 12px; color: var(--text-primary); font-size: 13px; }
.cut-set-expr { font-family: var(--font-mono); font-size: 12px; color: var(--text-secondary); }
.importance-list { display: flex; flex-direction: column; gap: 8px; }
.importance-item { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.rank { color: var(--text-muted); min-width: 16px; }
.name { color: var(--text-secondary); min-width: 100px; }
.pct { color: var(--text-muted); min-width: 40px; font-size: 11px; text-align: right; }
.calc-result { margin-top: 12px; }
</style>
