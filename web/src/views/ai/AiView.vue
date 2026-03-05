<template>
  <section class="page">
    <header class="page-header">
      <h1>AI 预测分析</h1>
      <p class="page-desc">mom-ai 支持自定义训练与推理。先获取能力状态，再执行训练或预测；模型输入维度为 10，输出为 2 分类（0/1）。</p>
    </header>

    <div class="section-card">
      <h3 class="section-title">能力状态</h3>
      <div v-if="capLoading" class="loading">加载中…</div>
      <div v-else-if="capError" class="error-msg">{{ capError }}</div>
      <div v-else-if="capabilities" class="cap-wrap">
        <p class="cap-meta">{{ capabilities.module }} | {{ capabilities.version }} | {{ capabilities.lang }}</p>
        <div class="cap-list">
          <div
            v-for="c in capabilities.capabilities"
            :key="c.code"
            class="cap-item"
          >
            <span class="cap-name">{{ c.name }}</span>
            <span class="cap-status" :class="c.status">{{ c.status }}</span>
            <span class="cap-desc">{{ c.description }}</span>
          </div>
        </div>
        <button type="button" class="btn small" @click="loadCapabilities">刷新</button>
      </div>
    </div>

    <div class="grid-2">
      <div class="section-card">
        <h3 class="section-title">自定义训练</h3>
        <p class="hint">训练数据：train_x 每行 10 个数值，train_y 为类别标签（0 或 1）</p>
        <div class="form-row">
          <label>epochs</label>
          <input v-model.number="trainForm.epochs" type="number" min="1" max="500" />
        </div>
        <div class="form-row">
          <label>lr（学习率）</label>
          <input v-model.number="trainForm.lr" type="number" step="0.0001" min="0.00001" />
        </div>
        <div class="form-row">
          <label>train_x（JSON 二维数组）</label>
          <textarea v-model="trainForm.trainXStr" rows="4" placeholder='[[1,1,1,1,1,1,1,1,1,1],[2,2,2,2,2,2,2,2,2,2]]'></textarea>
        </div>
        <div class="form-row">
          <label>train_y（JSON 数组）</label>
          <input v-model="trainForm.trainYStr" type="text" placeholder="[0, 1]" />
        </div>
        <button type="button" class="btn primary" :disabled="trainLoading" @click="runTrain">训练</button>
        <div v-if="trainResult" class="result-box">
          <p><strong>训练完成</strong></p>
          <p>final_loss: {{ trainResult.final_loss }}</p>
          <p>model_path: {{ trainResult.model_path }}</p>
        </div>
        <div v-if="trainError" class="error-msg">{{ trainError }}</div>
      </div>

      <div class="section-card">
        <h3 class="section-title">推理预测</h3>
        <p class="hint">输入每行 10 个数值，返回预测类别（0 或 1）</p>
        <div class="form-row">
          <label>data（JSON 二维数组）</label>
          <textarea v-model="predictForm.dataStr" rows="4" placeholder='[[1.2,1.5,1.1,1,1.3,1.2,1.1,1,1,1]]'></textarea>
        </div>
        <button type="button" class="btn primary" :disabled="predictLoading" @click="runPredict">预测</button>
        <div v-if="predictResult" class="result-box">
          <p><strong>预测结果</strong></p>
          <p>predictions: {{ JSON.stringify(predictResult.predictions) }}</p>
        </div>
        <div v-if="predictError" class="error-msg">{{ predictError }}</div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import {
  getCapabilities,
  train,
  predict,
  type CapabilitiesResponse,
  type TrainResponse,
  type PredictResponse
} from '@/api/ai';

const capabilities = ref<CapabilitiesResponse | null>(null);
const capLoading = ref(false);
const capError = ref('');

const trainForm = ref({
  epochs: 10,
  lr: 0.001,
  trainXStr: '[[1,1,1,1,1,1,1,1,1,1],[2,2,2,2,2,2,2,2,2,2]]',
  trainYStr: '[0, 1]'
});
const trainLoading = ref(false);
const trainResult = ref<TrainResponse | null>(null);
const trainError = ref('');

const predictForm = ref({
  dataStr: '[[1.2,1.5,1.1,1,1.3,1.2,1.1,1,1,1]]'
});
const predictLoading = ref(false);
const predictResult = ref<PredictResponse | null>(null);
const predictError = ref('');

async function loadCapabilities() {
  capLoading.value = true;
  capError.value = '';
  try {
    capabilities.value = await getCapabilities();
  } catch (e) {
    capError.value = e instanceof Error ? e.message : '获取能力失败';
    capabilities.value = null;
  } finally {
    capLoading.value = false;
  }
}

async function runTrain() {
  trainLoading.value = true;
  trainResult.value = null;
  trainError.value = '';
  try {
    const train_x = JSON.parse(trainForm.value.trainXStr) as number[][];
    const train_y = JSON.parse(trainForm.value.trainYStr) as number[];
    trainResult.value = await train({
      epochs: trainForm.value.epochs,
      lr: trainForm.value.lr,
      train_x,
      train_y
    });
  } catch (e) {
    trainError.value = e instanceof Error ? e.message : '训练失败';
  } finally {
    trainLoading.value = false;
  }
}

async function runPredict() {
  predictLoading.value = true;
  predictResult.value = null;
  predictError.value = '';
  try {
    const data = JSON.parse(predictForm.value.dataStr) as number[][];
    predictResult.value = await predict({ data });
  } catch (e) {
    predictError.value = e instanceof Error ? e.message : '预测失败';
  } finally {
    predictLoading.value = false;
  }
}

onMounted(() => {
  loadCapabilities();
});
</script>

<style scoped>
.page { padding: 0 0 1.5rem; }
.page-header { margin-bottom: 1.5rem; }
.page-header h1 { font-size: 1.5rem; color: #e5e7eb; margin: 0 0 0.5rem; }
.page-desc { font-size: 0.9rem; color: #94a3b8; margin: 0; line-height: 1.5; max-width: 640px; }

.section-card {
  padding: 1.25rem;
  border-radius: 0.75rem;
  background: radial-gradient(circle at top left, #1f2937, #020617);
  border: 1px solid #334155;
  margin-bottom: 1.5rem;
}
.section-title { font-size: 1rem; color: #38bdf8; margin: 0 0 0.75rem; }
.hint { font-size: 0.8rem; color: #64748b; margin: 0 0 0.75rem; }

.cap-meta { font-size: 0.85rem; color: #94a3b8; margin-bottom: 0.5rem; }
.cap-list { display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 0.75rem; }
.cap-item { display: flex; gap: 0.75rem; align-items: baseline; flex-wrap: wrap; }
.cap-name { font-weight: 500; color: #e5e7eb; }
.cap-status { font-size: 0.8rem; padding: 0.15rem 0.4rem; border-radius: 4px; }
.cap-status.implemented { background: #22c55e33; color: #22c55e; }
.cap-status.delegated { background: #64748b33; color: #94a3b8; }
.cap-desc { font-size: 0.85rem; color: #64748b; }

.grid-2 { display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 1.5rem; }
.form-row { margin-bottom: 0.75rem; }
.form-row label { display: block; margin-bottom: 0.25rem; color: #94a3b8; font-size: 0.875rem; }
.form-row input, .form-row textarea {
  width: 100%; padding: 0.5rem; border: 1px solid #475569; border-radius: 6px;
  background: #0f172a; color: #e5e7eb; font-family: ui-monospace, monospace; font-size: 0.85rem;
}
.form-row textarea { resize: vertical; min-height: 60px; }

.btn { padding: 0.4rem 0.75rem; font-size: 0.875rem; border-radius: 6px; cursor: pointer; border: 1px solid #475569; background: #1e293b; color: #e5e7eb; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.8rem; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

.result-box { margin-top: 1rem; padding: 1rem; background: #0f172a; border-radius: 6px; font-size: 0.9rem; color: #94a3b8; }
.result-box p { margin: 0.25rem 0; }
.error-msg { color: #f87171; margin-top: 0.5rem; font-size: 0.9rem; }
.loading { color: #94a3b8; }
</style>
