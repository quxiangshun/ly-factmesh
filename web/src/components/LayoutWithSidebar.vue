<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <Icon icon="mdi:factory" class="logo-icon" />
        <span v-show="!sidebarCollapsed" class="logo-text">LY-FactMesh MOM</span>
      </div>
      <nav class="sidebar-nav">
        <template v-for="group in menuConfig" :key="group.id">
          <div
            class="nav-group"
            @mouseenter="sidebarCollapsed && onGroupEnter($event, group)"
            @mouseleave="sidebarCollapsed && onGroupLeave(group.id)"
          >
            <div
              class="nav-group-title"
              :ref="(el) => { if (group.id && el) titleRefs[group.id] = el as HTMLElement }"
              @click="!sidebarCollapsed && toggleGroup(group.id)"
              :title="sidebarCollapsed ? group.name : undefined"
            >
              <Icon v-if="group.icon" :icon="group.icon" class="nav-icon" />
              <span v-show="!sidebarCollapsed">{{ group.name }}</span>
              <Icon
                v-show="!sidebarCollapsed"
                :icon="expanded[group.id] ? 'mdi:chevron-up' : 'mdi:chevron-down'"
                class="nav-chevron"
              />
            </div>
            <div v-show="!sidebarCollapsed && expanded[group.id]" class="nav-children">
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
      <Teleport to="body">
        <Transition name="nav-popover">
          <div
            v-show="sidebarCollapsed && hoverGroupId && popoverGroup && popoverGroup.children?.length"
            class="nav-popover"
            :style="popoverStyle"
            @mouseenter="keepPopover = true; clearTimers()"
            @mouseleave="onPopoverLeave"
          >
            <template v-for="item in popoverGroup?.children" :key="item.id">
              <a
                v-if="item.external"
                :href="item.path"
                target="_blank"
                rel="noreferrer"
                class="nav-popover-item"
                @click="closePopover"
              >
                {{ item.name }}
              </a>
              <RouterLink
                v-else
                :to="item.path"
                class="nav-popover-item"
                :class="{ active: isRouteActive(item.path) }"
                @click="closePopover"
              >
                {{ item.name }}
              </RouterLink>
            </template>
          </div>
        </Transition>
      </Teleport>
      <button type="button" class="sidebar-toggle" :title="sidebarCollapsed ? '展开菜单' : '折叠菜单'" @click="toggleSidebar">
        <Icon :icon="sidebarCollapsed ? 'mdi:chevron-right' : 'mdi:chevron-left'" />
      </button>
    </aside>
    <div class="main-wrap" :style="{ marginLeft: sidebarCollapsed ? '64px' : '220px' }">
      <header class="header">
        <TagsView />
        <div class="header-actions">
          <RouterLink to="/help#user-guide" class="header-link header-icon" title="使用说明">
            <Icon icon="mdi:help-circle-outline" />
          </RouterLink>
          <a href="/doc.html" target="_blank" rel="noreferrer" class="header-link">接口文档</a>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
      </header>
      <main class="main">
        <div class="main-inner" :class="{ 'main-inner-fullbleed': route.path === '/dashboard/bigscreen', 'main-inner-no-scroll': route.path.includes('/simulator/') }">
          <RouterView />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, watch, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Icon } from '@iconify/vue';
import { logout } from '@/api/auth';
import { menuConfig } from '@/config/menu';
import TagsView from './TagsView.vue';
import { useTagsView } from '@/composables/useTagsView';

const router = useRouter();
const route = useRoute();

const SIDEBAR_COLLAPSED_KEY = 'ly-factmesh-sidebar-collapsed';
const expanded = ref<Record<string, boolean>>({});
const sidebarCollapsed = ref(localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === 'true');
const hoverGroupId = ref<string | null>(null);
const titleRefs = reactive<Record<string, HTMLElement>>({});
const popoverStyle = ref<{ left: string; top: string }>({ left: '0', top: '0' });
const keepPopover = ref(false);
let showTimer: ReturnType<typeof setTimeout> | null = null;
let hideTimer: ReturnType<typeof setTimeout> | null = null;
const POPOVER_DELAY = 300;

const popoverGroup = computed(() =>
  hoverGroupId.value ? menuConfig.find((g) => g.id === hoverGroupId.value) ?? null : null
);

function clearTimers() {
  if (showTimer) {
    clearTimeout(showTimer);
    showTimer = null;
  }
  if (hideTimer) {
    clearTimeout(hideTimer);
    hideTimer = null;
  }
}

function updatePopoverPosition(groupId: string) {
  nextTick(() => {
    const el = titleRefs[groupId];
    if (!el) return;
    const rect = el.getBoundingClientRect();
    popoverStyle.value = {
      left: `${rect.right + 2}px`,
      top: `${rect.top}px`,
    };
  });
}

function onGroupEnter(_event: MouseEvent, group: { id: string; children?: unknown[] }) {
  if (!group.children?.length) return;
  clearTimers();
  showTimer = setTimeout(() => {
    showTimer = null;
    hoverGroupId.value = group.id;
    keepPopover.value = false;
    updatePopoverPosition(group.id);
  }, POPOVER_DELAY);
}

function onGroupLeave(groupId: string) {
  clearTimers();
  hideTimer = setTimeout(() => {
    if (!keepPopover.value) hoverGroupId.value = null;
    hideTimer = null;
  }, POPOVER_DELAY);
}

function onPopoverLeave() {
  keepPopover.value = false;
  clearTimers();
  hideTimer = setTimeout(() => {
    hoverGroupId.value = null;
    hideTimer = null;
  }, POPOVER_DELAY);
}

function closePopover() {
  keepPopover.value = false;
  hoverGroupId.value = null;
  clearTimers();
}

function syncExpandedToRoute() {
  const currentPath = route.path;
  let openedId: string | null = null;
  for (const g of menuConfig) {
    const isActive = !!g.children?.some((c) => !c.external && (currentPath === c.path || currentPath.startsWith(c.path + '/')));
    if (isActive && !openedId) openedId = g.id;
  }
  const next: Record<string, boolean> = {};
  for (const g of menuConfig) {
    next[g.id] = g.id === openedId;
  }
  expanded.value = next;
}

const { addTag } = useTagsView();
onMounted(() => {
  syncExpandedToRoute();
  addTag(route);
});
watch(
  () => route.fullPath,
  (path) => {
    syncExpandedToRoute();
    if (path && path !== '/login') addTag(route);
  }
);
watch(sidebarCollapsed, (collapsed) => {
  if (!collapsed) closePopover();
});

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value;
  localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(sidebarCollapsed.value));
}

function toggleGroup(id: string) {
  const next: Record<string, boolean> = {};
  for (const g of menuConfig) {
    next[g.id] = g.id === id ? !expanded.value[g.id] : false;
  }
  expanded.value = next;
}

function isRouteActive(path: string) {
  const p = route.path;
  return p === path || (path !== '/' && path !== '/dashboard' && p.startsWith(path + '/'));
}

function handleLogout() {
  logout();
  router.replace('/login');
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #0f172a;
  color: #e5e7eb;
}

.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 220px;
  flex-shrink: 0;
  background: #020617;
  border-right: 1px solid #1f2937;
  display: flex;
  flex-direction: column;
  z-index: 10;
  transition: width 0.2s ease;
}
.sidebar.collapsed {
  width: 64px;
}
.sidebar.collapsed .sidebar-header {
  justify-content: center;
  padding: 1rem;
}
.sidebar.collapsed .nav-group-title {
  justify-content: center;
  padding: 0.5rem;
}
.sidebar.collapsed .nav-chevron {
  display: none;
}

.sidebar-header {
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border-bottom: 1px solid #1f2937;
  flex-shrink: 0;
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
  position: relative;
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

.nav-popover {
  position: fixed;
  min-width: 160px;
  padding: 0.35rem 0;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.35);
  z-index: 2000;
}
.nav-popover-enter-active,
.nav-popover-leave-active {
  transition: opacity 0.15s ease;
}
.nav-popover-enter-from,
.nav-popover-leave-to {
  opacity: 0;
}
.nav-popover-item {
  display: block;
  padding: 0.4rem 1rem;
  font-size: 0.85rem;
  color: #9ca3af;
  text-decoration: none;
}
.nav-popover-item:hover {
  color: #e5e7eb;
  background: rgba(255, 255, 255, 0.05);
}
.nav-popover-item.active {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
}

.sidebar-toggle {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 0.5rem;
  background: rgba(15, 23, 42, 0.5);
  border: none;
  border-top: 1px solid #1f2937;
  color: #9ca3af;
  cursor: pointer;
  font-size: 1.25rem;
}
.sidebar-toggle:hover {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
}

.main-wrap {
  flex: 1;
  min-height: 0;
  transition: margin-left 0.2s ease;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1.25rem;
  border-bottom: 1px solid #1f2937;
  background: rgba(15, 23, 42, 0.95);
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

.header-icon {
  display: inline-flex;
  align-items: center;
  font-size: 1.25rem;
  padding: 0.25rem;
}

.header-icon .iconify {
  font-size: 1.25rem;
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
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.main-inner {
  flex: 1;
  min-height: 0;
  padding: 1rem 1.5rem;
  overflow: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.main-inner-fullbleed {
  padding: 0;
  overflow: hidden;
}
.main-inner-no-scroll {
  overflow: hidden;
}
.main-inner::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}
</style>
