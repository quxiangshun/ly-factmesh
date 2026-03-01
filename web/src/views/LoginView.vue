<template>
  <section class="login">
    <div class="login-card">
      <div class="login-header">
        <Icon icon="mdi:factory" class="logo-icon" />
        <h1>LY-FactMesh MOM</h1>
        <p>企业级制造运营管理平台</p>
      </div>
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="field">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="username"
            type="text"
            autocomplete="username"
            placeholder="请输入用户名"
            :disabled="loading"
          />
        </div>
        <div class="field">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
            :disabled="loading"
          />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn primary" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
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
  padding: 2rem;
  border-radius: 1rem;
  background: radial-gradient(circle at top left, #1f2937, #020617);
  border: 1px solid #1f2937;
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
}

.login-header p {
  color: #9ca3af;
  font-size: 0.9rem;
}

.login-form .field {
  margin-bottom: 1.25rem;
}

.login-form label {
  display: block;
  font-size: 0.875rem;
  color: #9ca3af;
  margin-bottom: 0.4rem;
}

.login-form input {
  width: 100%;
  padding: 0.6rem 0.9rem;
  border-radius: 0.5rem;
  border: 1px solid #374151;
  background: #111827;
  color: #e5e7eb;
  font-size: 1rem;
}

.login-form input:focus {
  outline: none;
  border-color: #38bdf8;
}

.login-form input::placeholder {
  color: #6b7280;
}

.login-form .error {
  color: #f87171;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.login-form .btn {
  width: 100%;
  padding: 0.7rem;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
}

.login-form .btn.primary {
  background: linear-gradient(90deg, #38bdf8, #22c55e);
  color: #0f172a;
}

.login-form .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
