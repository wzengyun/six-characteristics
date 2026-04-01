<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="40" height="40" viewBox="0 0 64 64" fill="none">
            <polygon points="32,4 60,18 60,46 32,60 4,46 4,18" stroke="#00d4ff" stroke-width="2" fill="rgba(0,212,255,0.08)"/>
            <text x="32" y="38" text-anchor="middle" fill="#00d4ff" font-size="14" font-family="Rajdhani" font-weight="700">六</text>
          </svg>
        </div>
        <span class="logo-text">六性分析</span>
      </div>

      <el-menu :default-active="$route.path" router class="sidebar-menu">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/equipment">
          <el-icon><Box /></el-icon>
          <span>装备管理</span>
        </el-menu-item>
        <el-divider />
        <el-menu-item-group title="六性分析">
          <el-menu-item index="/reliability">
            <span class="dot" style="background:#00d4ff"></span>
            <span>可靠性</span>
          </el-menu-item>
          <el-menu-item index="/maintainability">
            <span class="dot" style="background:#4ade80"></span>
            <span>维修性</span>
          </el-menu-item>
          <el-menu-item index="/testability">
            <span class="dot" style="background:#f59e0b"></span>
            <span>测试性</span>
          </el-menu-item>
          <el-menu-item index="/supportability">
            <span class="dot" style="background:#a78bfa"></span>
            <span>保障性</span>
          </el-menu-item>
          <el-menu-item index="/safety">
            <span class="dot" style="background:#f87171"></span>
            <span>安全性</span>
          </el-menu-item>
          <el-menu-item index="/environment">
            <span class="dot" style="background:#34d399"></span>
            <span>环境适应性</span>
          </el-menu-item>
        </el-menu-item-group>
        <el-divider />
        <el-menu-item index="/analysis">
          <el-icon><TrendCharts /></el-icon>
          <span>综合分析</span>
        </el-menu-item>
        <el-menu-item index="/report">
          <el-icon><Document /></el-icon>
          <span>报告中心</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main">
      <!-- 顶栏 -->
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="avatar">{{ auth.userInfo?.name?.[0] || 'U' }}</el-avatar>
              <span class="user-name">{{ auth.userInfo?.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon>个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleCommand(cmd) {
  if (cmd === 'logout') {
    auth.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/user')
  }
}
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;
  background: var(--bg-base);
}

.sidebar {
  width: 220px;
  background: var(--bg-surface);
  border-right: 1px solid var(--bg-border);
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid var(--bg-border);
}
.logo { animation: pulse-glow 3s ease-in-out infinite; }
.logo-text {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.05em;
}
.sidebar-menu {
  flex: 1;
  padding: 8px 12px;
  overflow-y: auto;
  :deep(.el-menu-item) {
    height: 40px;
    border-radius: var(--radius-md);
    margin-bottom: 2px;
    display: flex;
    align-items: center;
    gap: 8px;
    &.is-active {
      background: rgba(0,212,255,0.1) !important;
      color: var(--color-accent) !important;
    }
  }
  :deep(.el-menu-item-group__title) {
    color: var(--text-muted);
    font-size: 11px;
    padding-left: 12px;
    letter-spacing: 0.1em;
  }
  :deep(.el-divider--horizontal) { margin: 8px 0; border-color: var(--bg-border); }
}
.dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.header {
  height: 64px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--bg-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.header-left {
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner { color: var(--text-secondary); }
    &:last-child .el-breadcrumb__inner { color: var(--text-primary); }
    .el-breadcrumb__separator { color: var(--text-muted); }
  }
}
.header-right { display: flex; align-items: center; gap: 16px; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: background 0.2s;
  &:hover { background: var(--bg-hover); }
}
.avatar { background: linear-gradient(135deg, var(--color-primary), var(--color-accent)); font-size: 14px; }
.user-name { color: var(--text-primary); font-size: 13px; }

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: var(--bg-base);
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
