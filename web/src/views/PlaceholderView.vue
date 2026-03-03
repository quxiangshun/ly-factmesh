<template>
  <section class="placeholder">
    <p class="page-desc">{{ desc }}</p>
    <div class="coming-soon">功能开发中，敬请期待</div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const title = computed(() => (route.meta.title as string) || '页面');
const desc = computed(() => (route.meta.desc as string) || '');
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

.page-desc {
  color: #9ca3af;
  font-size: 0.95rem;
  margin-bottom: 2rem;
}

.coming-soon {
  padding: 2rem;
  text-align: center;
  color: #6b7280;
  background: rgba(31, 41, 55, 0.5);
  border-radius: 0.5rem;
  border: 1px dashed #374151;
}
</style>
