<template>
  <div class="tags-view" ref="tagsViewRef">
    <div class="tags-scroll">
      <RouterLink
        v-for="tag in visitedTags"
        :key="tag.path"
        :to="tag.path"
        class="tag-item"
        :class="{ active: isActive(tag.path) }"
        @contextmenu.prevent="(e) => showContextMenu(e, tag)"
      >
        <span class="tag-title">{{ tag.title }}</span>
        <button
          v-if="!tag.affix"
          type="button"
          class="tag-close"
          title="关闭"
          @click.prevent="handleClose(tag.path)"
        >
          <Icon icon="mdi:close" />
        </button>
      </RouterLink>
    </div>
    <!-- 右键菜单 -->
    <Teleport to="body">
      <div
        v-show="contextMenu.visible"
        class="tags-context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        ref="contextMenuRef"
      >
        <button type="button" :disabled="!canCloseCurrent" @click="doCloseCurrent">关闭当前</button>
        <button type="button" :disabled="!canCloseLeft" @click="doCloseLeft">关闭左侧</button>
        <button type="button" :disabled="!canCloseRight" @click="doCloseRight">关闭右侧</button>
        <button type="button" :disabled="!canCloseOther" @click="doCloseOtherFromMenu">关闭其他</button>
        <button type="button" :disabled="!canCloseAll" @click="doCloseAllFromMenu">关闭所有</button>
      </div>
    </Teleport>
    <div v-if="visitedTags.length > 0" class="tags-actions">
      <div class="tags-dropdown" ref="dropdownRef">
        <button type="button" class="tags-more" title="更多操作" @click.stop="showDropdown = !showDropdown">
          <Icon icon="mdi:dots-vertical" />
        </button>
        <div v-show="showDropdown" class="tags-dropdown-menu" @click.stop>
          <button type="button" @click="doCloseOther(); showDropdown = false">关闭其他</button>
          <button type="button" @click="doCloseAll(); showDropdown = false">关闭全部</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Icon } from '@iconify/vue';
import { useTagsView } from '@/composables/useTagsView';

const route = useRoute();
const router = useRouter();
const { visitedTags, closeTag, closeOther, closeAll, closeLeft, closeRight } = useTagsView();
const showDropdown = ref(false);
const dropdownRef = ref<HTMLElement | null>(null);

// 右键菜单
const contextMenu = ref({ visible: false, x: 0, y: 0, path: '' });
const contextMenuRef = ref<HTMLElement | null>(null);
const tagsViewRef = ref<HTMLElement | null>(null);

const canCloseCurrent = computed(() => {
  const tag = visitedTags.value.find((t) => t.path === contextMenu.value.path);
  return tag && !tag.affix;
});
const canCloseLeft = computed(() => {
  const idx = visitedTags.value.findIndex((t) => t.path === contextMenu.value.path);
  if (idx <= 0) return false;
  return visitedTags.value.some((t, i) => i < idx && !t.affix);
});
const canCloseRight = computed(() => {
  const idx = visitedTags.value.findIndex((t) => t.path === contextMenu.value.path);
  if (idx < 0) return false;
  return visitedTags.value.some((t, i) => i > idx);
});
const canCloseOther = computed(() => {
  const list = visitedTags.value.filter((t) => t.path !== contextMenu.value.path && !t.affix);
  return list.length > 0;
});
const canCloseAll = computed(() => {
  const list = visitedTags.value.filter((t) => !t.affix);
  return list.length > 0;
});

function showContextMenu(e: MouseEvent, tag: { path: string }) {
  contextMenu.value = { visible: true, x: e.clientX, y: e.clientY, path: tag.path };
}

function hideContextMenu() {
  contextMenu.value = { ...contextMenu.value, visible: false };
}

function doCloseCurrent() {
  const path = contextMenu.value.path;
  hideContextMenu();
  handleClose(path);
}

function doCloseLeft() {
  const path = contextMenu.value.path;
  hideContextMenu();
  closeLeft(path);
  if (route.path !== path && !visitedTags.value.some((t) => t.path === route.path)) {
    router.push(path);
  }
}

function doCloseRight() {
  const path = contextMenu.value.path;
  hideContextMenu();
  closeRight(path);
  if (route.path !== path && !visitedTags.value.some((t) => t.path === route.path)) {
    router.push(path);
  }
}

function doCloseOtherFromMenu() {
  const path = contextMenu.value.path;
  hideContextMenu();
  closeOther(path);
  if (route.path !== path) {
    router.push(path);
  }
}

function doCloseAllFromMenu() {
  hideContextMenu();
  closeAll();
  router.push('/dashboard');
}

function handleContextMenuClickOutside(e: MouseEvent) {
  if (contextMenuRef.value && !contextMenuRef.value.contains(e.target as Node)) {
    hideContextMenu();
  }
}

function handleClickOutside(e: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    showDropdown.value = false;
  }
  handleContextMenuClickOutside(e);
}

function handleContextMenuAnywhere(e: MouseEvent) {
  if (!contextMenu.value.visible) return;
  const target = e.target as Node;
  if (tagsViewRef.value?.contains(target) || contextMenuRef.value?.contains(target)) return;
  hideContextMenu();
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') hideContextMenu();
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  document.addEventListener('contextmenu', handleContextMenuAnywhere);
  document.addEventListener('keydown', handleKeydown);
});
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
  document.removeEventListener('contextmenu', handleContextMenuAnywhere);
  document.removeEventListener('keydown', handleKeydown);
});

const currentPath = computed(() => route.path);

function isActive(path: string) {
  return route.path === path || (path !== '/' && path !== '/dashboard' && route.path.startsWith(path));
}

function handleClose(path: string) {
  if (route.path === path) {
    const list = visitedTags.value;
    const idx = list.findIndex((t) => t.path === path);
    const target = idx > 0 ? list[idx - 1] : list[idx + 1];
    closeTag(path);
    if (target) {
      router.push(target.path);
    } else {
      router.push('/dashboard');
    }
  } else {
    closeTag(path);
  }
}

function doCloseOther() {
  const cur = route.path;
  closeOther(cur);
  const kept = visitedTags.value.find((t) => t.path === cur);
  if (!kept && visitedTags.value.length > 0) {
    router.push(visitedTags.value[visitedTags.value.length - 1].path);
  }
}

function doCloseAll() {
  closeAll();
  router.push('/dashboard');
}
</script>

<style scoped>
.tags-view {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  min-width: 0;
}
.tags-scroll {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: thin;
  scrollbar-color: #475569 transparent;
}
.tags-scroll::-webkit-scrollbar {
  height: 4px;
}
.tags-scroll::-webkit-scrollbar-track {
  background: transparent;
}
.tags-scroll::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 2px;
}
.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.35rem 0.6rem;
  font-size: 0.8rem;
  color: #94a3b8;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 4px;
  text-decoration: none;
  white-space: nowrap;
  flex-shrink: 0;
}
.tag-item:hover {
  color: #e5e7eb;
  background: #334155;
}
.tag-item.active {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.15);
  border-color: #38bdf8;
}
.tag-title {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tag-close {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  margin: 0;
  background: none;
  border: none;
  color: inherit;
  opacity: 0.6;
  cursor: pointer;
  font-size: 0.9rem;
}
.tag-close:hover {
  opacity: 1;
}
.tag-item.affix .tag-close {
  display: none;
}
.tags-actions {
  flex-shrink: 0;
}
.tags-dropdown {
  position: relative;
}
.tags-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.35rem;
  background: none;
  border: 1px solid transparent;
  border-radius: 4px;
  color: #94a3b8;
  cursor: pointer;
}
.tags-more:hover {
  color: #e5e7eb;
  background: rgba(255, 255, 255, 0.05);
}
.tags-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 0.25rem;
  padding: 0.25rem;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 100;
}
.tags-dropdown-menu button {
  display: block;
  width: 100%;
  padding: 0.35rem 0.75rem;
  font-size: 0.8rem;
  color: #e5e7eb;
  background: none;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-align: left;
}
.tags-dropdown-menu button:hover {
  background: rgba(56, 189, 248, 0.2);
}

/* 右键菜单 */
.tags-context-menu {
  position: fixed;
  padding: 0.25rem;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 9999;
  min-width: 120px;
}
.tags-context-menu button {
  display: block;
  width: 100%;
  padding: 0.35rem 0.75rem;
  font-size: 0.8rem;
  color: #e5e7eb;
  background: none;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-align: left;
}
.tags-context-menu button:hover:not(:disabled) {
  background: rgba(56, 189, 248, 0.2);
}
.tags-context-menu button:disabled {
  color: #64748b;
  cursor: not-allowed;
}
</style>
