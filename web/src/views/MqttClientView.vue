<template>
  <section class="page">
    <div class="toolbar">
      <div class="toolbar-actions">
        <el-tooltip content="MQTT 客户端，默认连接项目 EMQX 容器（ws://localhost:8087），支持订阅、发布与消息接收" placement="bottom">
          <el-icon class="tip-icon"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
    </div>
    <div class="mqtt-grid">
      <!-- 设置 -->
      <div class="mqtt-section">
        <div class="section-header">
          <h3 class="section-title">设置</h3>
          <div class="section-actions">
            <el-button type="primary" size="small" :disabled="connected" @click="connect">连接</el-button>
            <el-button v-if="connected" size="small" type="danger" @click="disconnect">断开</el-button>
          </div>
        </div>
        <div class="form-grid form-grid-settings">
          <el-form-item label="协议">
            <el-select v-model="config.protocol">
              <el-option value="ws://" label="ws://" />
              <el-option value="wss://" label="wss://" />
              <el-option value="mqtt://" label="mqtt://" />
              <el-option value="mqtts://" label="mqtts://" />
            </el-select>
          </el-form-item>
          <el-form-item label="主机">
            <el-input v-model="config.host" placeholder="localhost" />
          </el-form-item>
          <el-form-item label="端口">
            <el-input v-model="config.port" placeholder="8087" />
          </el-form-item>
          <el-form-item label="客户端ID">
            <el-input v-model="config.clientId" placeholder="自动生成" />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="config.username" placeholder="可选" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="config.password" type="password" placeholder="可选" show-password />
          </el-form-item>
        </div>
      </div>

      <!-- 订阅 -->
      <div class="mqtt-section">
        <div class="section-header">
          <h3 class="section-title">订阅</h3>
          <el-button type="primary" size="small" :disabled="!connected" @click="subscribe">订阅</el-button>
        </div>
        <div class="form-grid form-grid-sub">
          <el-form-item label="主题">
            <el-input v-model="subTopic" placeholder="ly/factmesh/test" />
          </el-form-item>
          <el-form-item label="服务质量">
            <el-select v-model="subQos">
              <el-option :value="0" label="0" />
              <el-option :value="1" label="1" />
              <el-option :value="2" label="2" />
            </el-select>
          </el-form-item>
        </div>
        <p class="hint">订阅 '#' 可全局订阅，接收所有 MQTT 消息</p>
      </div>

      <!-- 发布 -->
      <div class="mqtt-section">
        <div class="section-header">
          <h3 class="section-title">发布</h3>
          <el-button type="primary" size="small" :disabled="!connected" @click="publish">发布</el-button>
        </div>
        <div class="form-grid form-grid-pub">
          <el-form-item label="主题">
            <el-input v-model="pubTopic" placeholder="ly/factmesh/test" />
          </el-form-item>
          <el-form-item label="服务质量">
            <el-select v-model="pubQos">
              <el-option :value="0" label="0" />
              <el-option :value="1" label="1" />
              <el-option :value="2" label="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="有效载荷" class="full">
            <el-input v-model="pubPayload" type="textarea" :rows="4" placeholder='{"msg": "Hello"}' />
          </el-form-item>
        </div>
      </div>

      <!-- 接收 -->
      <div class="mqtt-section receive-section">
        <div class="receive-header">
          <h3 class="section-title">接收</h3>
          <el-button size="small" :disabled="!messages.length" @click="clearMessages">清除</el-button>
        </div>
        <div class="receive-log" ref="logRef">
          <div v-if="!messages.length" class="log-empty">暂无接收消息，订阅主题后可在此查看</div>
          <template v-else>
            <div v-for="(m, i) in messages" :key="i" class="log-line">
              <span class="log-num">{{ i + 1 }}</span>
              <span class="log-content">
                <span class="log-time">{{ m.time }}</span>
                <span class="log-topic">{{ m.topic }}</span>
                <span class="log-payload">{{ m.payload }}</span>
              </span>
            </div>
          </template>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
import mqtt from 'mqtt';

// 默认值与 tools/docker-compose-base.yml 中 EMQX 容器配置一致：WebSocket 8087
const config = reactive({
  protocol: 'ws://',
  host: 'localhost',
  port: 8087,
  clientId: '',
  username: '',
  password: ''
});

const subTopic = ref('ly/factmesh/test');
const subQos = ref(0);
const pubTopic = ref('ly/factmesh/test');
const pubQos = ref(0);
const pubPayload = ref('{"msg": "Hello from LY-FactMesh", "client": "web"}');

const connected = ref(false);
const messages = ref<Array<{ time: string; topic: string; payload: string }>>([]);
const logRef = ref<HTMLElement | null>(null);

let client: ReturnType<typeof mqtt.connect> | null = null;

function buildUrl() {
  const { protocol, host, port } = config;
  const url = `${protocol}${host}:${port}`;
  return protocol.startsWith('ws') ? `${url}/mqtt` : url;
}

function connect() {
  if (client) return;
  const url = buildUrl();
  const options: Record<string, unknown> = {
    clientId: config.clientId || `ly_factmesh_web_${Date.now().toString(36)}`,
    reconnectPeriod: 0
  };
  if (config.username) options.username = config.username;
  if (config.password) options.password = config.password;

  try {
    client = mqtt.connect(url, options);
    client.on('connect', () => {
      connected.value = true;
      appendMessage('_sys', `已连接 ${url}`);
      if (subTopic.value) {
        client.subscribe(subTopic.value, { qos: subQos.value as 0 | 1 | 2 }, (err) => {
          if (!err) appendMessage('_sys', `已订阅: ${subTopic.value}`);
        });
      }
    });
    client.on('error', (err) => {
      appendMessage('_sys', `错误: ${err.message}`);
    });
    client.on('close', () => {
      connected.value = false;
      client = null;
      appendMessage('_sys', '连接已关闭');
    });
    client.on('message', (topic, payload) => {
      const str = payload.toString();
      appendMessage(topic, str);
    });
  } catch (e) {
    appendMessage('_sys', `连接失败: ${e instanceof Error ? e.message : String(e)}`);
  }
}

function disconnect() {
  if (client) {
    client.end();
    client = null;
  }
}

function subscribe() {
  if (!client?.connected || !subTopic.value) return;
  client.subscribe(subTopic.value, { qos: subQos.value as 0 | 1 | 2 }, (err) => {
    if (err) appendMessage('_sys', `订阅失败: ${err.message}`);
    else appendMessage('_sys', `已订阅: ${subTopic.value}`);
  });
}

function publish() {
  if (!client?.connected || !pubTopic.value) return;
  client.publish(pubTopic.value, pubPayload.value, { qos: pubQos.value as 0 | 1 | 2 }, (err) => {
    if (err) appendMessage('_sys', `发布失败: ${err.message}`);
  });
}

function appendMessage(topic: string, payload: string) {
  const time = new Date().toLocaleTimeString('zh-CN', { hour12: false });
  messages.value.push({ time, topic, payload });
  if (messages.value.length > 500) messages.value = messages.value.slice(-400);
  setTimeout(() => logRef.value?.scrollTo({ top: logRef.value.scrollHeight, behavior: 'smooth' }), 0);
}

function clearMessages() {
  messages.value = [];
}

onUnmounted(() => disconnect());
</script>

<style scoped>
.page {
  padding: 0;
  min-width: 0;
  overflow-x: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
.toolbar { margin-bottom: 0.5rem; flex-shrink: 0; }
.toolbar-actions { display: flex; align-items: center; }

.mqtt-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  grid-template-rows: auto 1fr;
  gap: 0.75rem;
  flex: 1;
  min-height: 0;
  min-width: 0;
}
.mqtt-section {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 6px;
  padding: 0.6rem 0.75rem;
  min-width: 0;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
  gap: 0.5rem;
}
.section-header .section-title { margin: 0; }
.section-actions { display: flex; gap: 0.35rem; flex-shrink: 0; }
.receive-section {
  grid-column: 1 / -1;
  grid-row: 2;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.receive-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem; flex-shrink: 0; }
.receive-header .section-title { margin: 0; }
.section-title { margin: 0; font-size: 0.9rem; color: #38bdf8; }

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.4rem 0.6rem;
  min-width: 0;
}
.form-grid-settings { margin-bottom: 0; }
.form-grid-sub,
.form-grid-pub {
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
}
.form-grid-pub .form-row.full {
  grid-column: 1 / -1;
}
.form-row { display: flex; flex-direction: column; gap: 0.25rem; min-width: 0; }
.form-row.full { grid-column: 1 / -1; }
.form-row-topic { grid-column: 1; }
.form-row label { font-size: 0.75rem; color: #94a3b8; }
.form-input, .form-textarea {
  padding: 0.3rem 0.5rem;
  border: 1px solid #475569;
  border-radius: 4px;
  background: #0f172a;
  color: #e5e7eb;
  font-size: 0.8rem;
  min-width: 0;
  width: 100%;
  box-sizing: border-box;
}
.form-textarea {
  resize: none;
  min-height: 80px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.form-textarea::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.hint { font-size: 0.7rem; color: #64748b; margin: 0.25rem 0 0; }

.btn {
  padding: 0.35rem 0.6rem;
  font-size: 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #475569;
  background: #1e293b;
  color: #e5e7eb;
}
.btn.small { padding: 0.25rem 0.5rem; font-size: 0.75rem; }
.btn.primary { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
.btn.danger { background: #ef4444; color: #fff; border-color: #ef4444; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

.receive-log {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 4px;
  padding: 0.35rem 0.5rem;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  font-family: ui-monospace, monospace;
  font-size: 0.75rem;
  min-width: 0;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.receive-log::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}
.log-empty { color: #64748b; padding: 1rem; }
.log-line {
  display: flex;
  gap: 0.5rem;
  padding: 0.15rem 0;
  border-bottom: 1px solid #1e293b;
  min-width: 0;
}
.log-num {
  flex-shrink: 0;
  width: 2em;
  text-align: right;
  color: #64748b;
  user-select: none;
}
.log-content {
  flex: 1;
  min-width: 0;
  word-break: break-all;
  overflow-wrap: break-word;
}
.log-time { color: #64748b; margin-right: 0.5rem; white-space: nowrap; }
.log-topic { color: #38bdf8; margin-right: 0.5rem; }
.log-payload { color: #e5e7eb; }
</style>
