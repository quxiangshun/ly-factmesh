<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <Icon icon="mdi:factory" class="logo-icon" />
        <span class="logo-text">LY-FactMesh MOM</span>
      </div>
      <nav class="sidebar-nav">
        <template v-for="group in menuConfig" :key="group.id">
          <div class="nav-group">
            <div class="nav-group-title" @click="toggleGroup(group.id)">
              <Icon v-if="group.icon" :icon="group.icon" class="nav-icon" />
              <span>{{ group.name }}</span>
              <Icon
                :icon="expanded[group.id] ? 'mdi:chevron-up' : 'mdi:chevron-down'"
                class="nav-chevron"
              />
            </div>
            <div v-show="expanded[group.id]" class="nav-children">
              <template v-for="item in group.children" :key="item.id">
                <a
                  v-if="item.external"
                  :href="item.path"
                  target="_blank"
                  rel="noreferrer"
                  class="nav-item"
                >
                  {{ item.name }}
                </a>
                <RouterLink
                  v-else
                  :to="item.path"
                  class="nav-item"
                  active-class="active"
                >
                  {{ item.name }}
                </RouterLink>
              </template>
            </div>
          </div>
        </template>
      </nav>
    </aside>
    <div class="main-wrap">
      <header class="header">
        <span class="header-title">{{ currentTitle }}</span>
        <div class="header-actions">
          <a href="/doc.html" target="_blank" rel="noreferrer" class="header-link">接口文档</a>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </header>
      <main class="main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Icon } from '@iconify/vue';
import { logout } from '@/api/auth';
import { menuConfig } from '@/config/menu';

const router = useRouter();
const route = useRoute();

const expanded = ref<Record<string, boolean>>({});

onMounted(() => {
  const currentPath = route.path;
  menuConfig.forEach((g) => {
    expanded.value[g.id] = !!g.children?.some((c) => !c.external && (currentPath === c.path || currentPath.startsWith(c.path + '/')));
  });
});

function toggleGroup(id: string) {
  expanded.value[id] = !expanded.value[id];
}

const currentTitle = computed(() => {
  const path = route.path;
  for (const g of menuConfig) {
    const found = g.children?.find((c) => !c.external && (path === c.path || path.startsWith(c.path + '/')));
    if (found) return found.name;
  }
  return 'LY-FactMesh MOM';
});

function handleLogout() {
  logout();
  router.replace('/login');
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: #0f172a;
  color: #e5e7eb;
}

.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #020617;
  border-right: 1px solid #1f2937;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-bottom: 1px solid #1f2937;
}

.logo-icon {
  font-size: 1.5rem;
  color: #38bdf8;
}

.logo-text {
  font-weight: 600;
  font-size: 0.95rem;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.nav-group {
  margin-bottom: 0.25rem;
}

.nav-group-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  cursor: pointer;
  font-size: 0.875rem;
  color: #9ca3af;
  user-select: none;
}

.nav-group-title:hover {
  color: #e5e7eb;
  background: rgba(255, 255, 255, 0.05);
}

.nav-icon {
  font-size: 1.1rem;
  color: #38bdf8;
}

.nav-chevron {
  margin-left: auto;
  font-size: 1rem;
  opacity: 0.7;
}

.nav-children {
  padding-left: 0.5rem;
}

.nav-item {
  display: block;
  padding: 0.4rem 1rem 0.4rem 2rem;
  font-size: 0.85rem;
  color: #9ca3af;
  text-decoration: none;
  border-left: 2px solid transparent;
}

.nav-item:hover {
  color: #e5e7eb;
  background: rgba(255, 255, 255, 0.05);
}

.nav-item.active {
  color: #38bdf8;
  border-left-color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
}

.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  border-bottom: 1px solid #1f2937;
  background: rgba(15, 23, 42, 0.95);
}

.header-title {
  font-size: 1.1rem;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-link {
  font-size: 0.9rem;
  color: #9ca3af;
  text-decoration: none;
}

.header-link:hover {
  color: #e5e7eb;
}

.btn-logout {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 0.9rem;
  cursor: pointer;
}

.btn-logout:hover {
  color: #e5e7eb;
}

.main {
  flex: 1;
  padding: 1.5rem;
  overflow: auto;
}
</style>
