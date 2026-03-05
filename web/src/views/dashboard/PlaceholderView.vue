<template>
  <section class="placeholder">
    <div class="toolbar">
      <div class="toolbar-actions">
        <div class="title-with-tip">
          <span class="tip-trigger" title="功能说明" @click.stop="showTip = !showTip">
            <Icon icon="mdi:information-outline" class="tip-icon" />
          </span>
          <div v-if="showTip && desc" class="tip-popover" @click.stop>
            <div class="tip-content">{{ desc }}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="coming-soon">功能开发中，敬请期待</div>
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { Icon } from '@iconify/vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const title = computed(() => (route.meta.title as string) || '页面');
const desc = computed(() => (route.meta.desc as string) || '');
const showTip = ref(false);
function closeTipOnClickOutside(e: MouseEvent) {
  const el = (e.target as HTMLElement).closest('.title-with-tip');
  if (!el) showTip.value = false;
}
onMounted(() => document.addEventListener('click', closeTipOnClickOutside));
onUnmounted(() => document.removeEventListener('click', closeTipOnClickOutside));
</script>

<style scoped>
.placeholder {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem 0;
}

.page-title {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; align-items: center; }

.coming-soon {
  padding: 2rem;
  text-align: center;
  color: #6b7280;
  background: rgba(31, 41, 55, 0.5);
  border-radius: 0.5rem;
  border: 1px dashed #374151;
}
</style>
