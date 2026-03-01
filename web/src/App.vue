<template>
  <div class="layout">
    <header class="layout-header">
      <div class="logo">
        <Icon icon="mdi:factory" class="logo-icon" />
        <span>LY-FactMesh MOM</span>
      </div>
      <nav class="nav">
        <RouterLink to="/">概览</RouterLink>
        <RouterLink to="/services">微服务</RouterLink>
        <a href="/doc.html" target="_blank" rel="noreferrer">接口文档</a>
      </nav>
    </header>
    <main class="layout-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { Icon } from '@iconify/vue';
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0f172a;
  color: #e5e7eb;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  border-bottom: 1px solid #1f2937;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(10px);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
}

.logo-icon {
  font-size: 1.5rem;
  color: #38bdf8;
}

.nav {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
}

.nav a {
  color: #9ca3af;
  text-decoration: none;
}

.nav a.router-link-active {
  color: #e5e7eb;
}

.layout-main {
  flex: 1;
  padding: 1.5rem;
}
</style>

