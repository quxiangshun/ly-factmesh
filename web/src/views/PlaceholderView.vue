<template>
  <section class="placeholder">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip v-if="desc" :content="desc" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>
    <el-empty description="功能开发中，敬请期待" />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const desc = computed(() => (route.meta.desc as string) || '');
</script>

<style scoped>
.placeholder {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem 0;
}

.toolbar { margin-bottom: 1rem; }
.toolbar-actions { display: flex; align-items: center; }
.tip-icon { font-size: 1.2rem; color: #94a3b8; cursor: help; }
</style>
