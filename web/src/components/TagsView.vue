<template>
  <div class="tags-view">
    <div class="tags-scroll">
      <RouterLink
        v-for="tag in visitedTags"
        :key="tag.path"
        :to="tag.path"
        class="tag-item"
        :class="{ active: isActive(tag.path) }"
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
const { visitedTags, closeTag, closeOther, closeAll } = useTagsView();
const showDropdown = ref(false);
const dropdownRef = ref<HTMLElement | null>(null);

function handleClickOutside(e: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    showDropdown.value = false;
  }
}
onMounted(() => document.addEventListener('click', handleClickOutside));
onUnmounted(() => document.removeEventListener('click', handleClickOutside));

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
</style>
