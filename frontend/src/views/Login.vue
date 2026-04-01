<template>
  <div class="login-page">
    <!-- 背景网格 -->
    <div class="bg-grid"></div>
    <div class="scanline-overlay"></div>

    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-content">
        <div class="brand-logo">
          <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
            <polygon points="32,4 60,18 60,46 32,60 4,46 4,18" stroke="#00d4ff" stroke-width="2" fill="rgba(0,212,255,0.05)"/>
            <polygon points="32,12 52,22 52,42 32,52 12,42 12,22" stroke="#00d4ff" stroke-width="1" fill="rgba(0,212,255,0.03)" opacity="0.6"/>
            <text x="32" y="37" text-anchor="middle" fill="#00d4ff" font-size="16" font-family="Rajdhani" font-weight="700">六性</text>
          </svg>
        </div>
        <h1 class="brand-title">装备通用质量特性<br/>六性分析系统</h1>
        <p class="brand-subtitle">Equipment General Quality Characteristics<br/>Six-Property Analysis System</p>
        <div class="brand-standards">
          <span class="std-tag">GJB 450A</span>
          <span class="std-tag">GJB 368B</span>
          <span class="std-tag">GJB 900</span>
        </div>
        <div class="six-props">
          <div v-for="p in props" :key="p.name" class="prop-item">
            <span class="prop-dot" :style="{ background: p.color }"></span>
            <span>{{ p.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-panel">
      <div class="login-box">
        <h2 class="login-title">系统登录</h2>
        <p class="login-desc">请输入您的账号和密码</p>

        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <el-form-item prop="account">
            <el-input
              v-model="form.account"
              placeholder="账号"
              size="large"
              prefix-icon="User"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <div class="demo-hint">
          <el-icon><InfoFilled /></el-icon>
          演示账号：admin / Admin@123
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ account: 'admin', password: 'Admin@123' })
const rules = {
  account:  [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const props = [
  { name: '可靠性', color: '#00d4ff' },
  { name: '维修性', color: '#4ade80' },
  { name: '测试性', color: '#f59e0b' },
  { name: '保障性', color: '#a78bfa' },
  { name: '安全性', color: '#f87171' },
  { name: '环境适应性', color: '#34d399' },
]

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await auth.login(form.account, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    ElMessage.error('账号或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  height: 100vh;
  background: var(--bg-base);
  position: relative;
  overflow: hidden;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,212,255,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.04) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

.brand-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  border-right: 1px solid var(--bg-border);
  position: relative;
  &::after {
    content: '';
    position: absolute;
    right: 0; top: 20%; bottom: 20%;
    width: 1px;
    background: linear-gradient(transparent, var(--color-accent), transparent);
  }
}

.brand-content { max-width: 420px; }

.brand-logo {
  margin-bottom: 32px;
  animation: pulse-glow 3s ease-in-out infinite;
}

.brand-title {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 12px;
  letter-spacing: 0.05em;
}

.brand-subtitle {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.8;
  margin-bottom: 24px;
}

.brand-standards {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
}
.std-tag {
  padding: 4px 12px;
  border: 1px solid rgba(0,212,255,0.3);
  border-radius: 20px;
  font-size: 11px;
  color: var(--color-accent);
  font-family: var(--font-display);
  letter-spacing: 0.1em;
}

.six-props {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.prop-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.prop-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.login-panel {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.login-box { width: 100%; }

.login-title {
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: 0.08em;
}
.login-desc {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 32px;
}

.login-form {
  :deep(.el-input__wrapper) {
    background: var(--bg-surface) !important;
    border: 1px solid var(--bg-border);
    box-shadow: none !important;
    transition: border-color 0.2s;
    &:hover, &.is-focus {
      border-color: var(--color-accent) !important;
      box-shadow: 0 0 0 2px rgba(0,212,255,0.1) !important;
    }
  }
  :deep(.el-form-item) { margin-bottom: 20px; }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-family: var(--font-display);
  font-size: 16px;
  letter-spacing: 0.2em;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  border: none;
  margin-top: 8px;
  &:hover {
    background: linear-gradient(135deg, var(--color-primary-light), var(--color-accent));
    transform: translateY(-1px);
    box-shadow: 0 8px 24px rgba(0,212,255,0.3);
  }
  transition: all 0.2s;
}

.demo-hint {
  margin-top: 20px;
  padding: 12px 16px;
  background: rgba(0,212,255,0.05);
  border: 1px solid rgba(0,212,255,0.15);
  border-radius: var(--radius-md);
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
