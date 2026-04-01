<template>
  <div class="user-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="user-profile">
            <el-avatar :size="100" class="avatar-large">{{ auth.userInfo?.name?.[0] || 'U' }}</el-avatar>
            <h2>{{ auth.userInfo?.name }}</h2>
            <p class="role">{{ auth.userInfo?.role }}</p>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="账号">{{ auth.userInfo?.account }}</el-descriptions-item>
              <el-descriptions-item label="角色">{{ roleMap[auth.userInfo?.role] }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ auth.userInfo?.createTime || '2026-04-01' }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header><span class="card-title">修改密码</span></template>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px" style="max-width:400px">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top:20px">
          <template #header><span class="card-title">操作日志</span></template>
          <el-timeline>
            <el-timeline-item v-for="log in logs" :key="log.time" :timestamp="log.time" placement="top">
              <div class="log-item">
                <el-icon :style="{ color: log.color }"><component :is="log.icon" /></el-icon>
                <span>{{ log.action }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/store/auth'
import { updateProfile } from '@/api/user'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const pwdRef = ref()

const roleMap = { ADMIN: '系统管理员', ANALYST: '分析工程师', USER: '普通用户' }

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{
    validator: (_, v, cb) => v === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')),
    trigger: 'blur'
  }]
}

const logs = ref([
  { time: '2026-04-01 10:30', action: '完成"某型雷达系统"可靠性分析', icon: 'SuccessFilled', color: '#22c55e' },
  { time: '2026-04-01 09:45', action: '新增装备"某型通信设备"', icon: 'Plus', color: '#00d4ff' },
  { time: '2026-03-31 16:20', action: '生成综合分析报告', icon: 'Document', color: '#a78bfa' },
  { time: '2026-03-31 14:00', action: '登录系统', icon: 'User', color: '#8ba8c8' },
])

async function changePassword() {
  await pwdRef.value.validate()
  try {
    await updateProfile({ password: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch { ElMessage.error('修改失败') }
}
</script>

<style scoped lang="scss">
.user-profile {
  text-align: center;
  padding: 20px 0;
  .avatar-large {
    background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
    font-size: 36px;
    margin-bottom: 16px;
  }
  h2 { margin: 0 0 8px; color: var(--text-primary); font-size: 20px; }
  .role { color: var(--text-muted); margin-bottom: 20px; }
}
.log-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
