<template>
  <section class="login">
    <el-card class="login-card">
      <div class="login-header">
        <Icon icon="mdi:factory" class="logo-icon" />
        <h1>LY-FactMesh MOM</h1>
        <p>企业级制造运营管理平台</p>
      </div>
      <el-form class="login-form" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input
            v-model="username"
            placeholder="请输入用户名"
            :disabled="loading"
            autocomplete="username"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-alert v-if="error" type="error" :title="error" show-icon class="error-alert" />
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Icon } from '@iconify/vue';
import { login } from '@/api/auth';

const router = useRouter();
const route = useRoute();
const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

const redirect = computed(() => {
  const r = route.query.redirect;
  return typeof r === 'string' ? r : '/dashboard';
});

async function handleLogin() {
  error.value = '';
  if (!username.value.trim()) {
    error.value = '请输入用户名';
    return;
  }
  if (!password.value) {
    error.value = '请输入密码';
    return;
  }
  loading.value = true;
  try {
    await login(username.value.trim(), password.value);
    await router.replace(redirect.value || '/');
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login {
  min-height: calc(100vh - 4rem);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.login-card {
  width: 100%;
  max-width: 380px;
}

.login-card :deep(.el-card__body) {
  padding: 2rem;
  background: radial-gradient(circle at top left, #1f2937, #020617);
  border: 1px solid #1f2937;
  border-radius: 1rem;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header .logo-icon {
  font-size: 2.5rem;
  color: #38bdf8;
  margin-bottom: 0.5rem;
}

.login-header h1 {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
  color: #e5e7eb;
}

.login-header p {
  color: #9ca3af;
  font-size: 0.9rem;
}

.login-form :deep(.el-form-item__label) {
  color: #9ca3af;
}

.error-alert {
  margin-bottom: 1rem;
}

.submit-btn {
  width: 100%;
}
</style>
