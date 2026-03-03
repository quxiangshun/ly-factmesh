<template>
  <div class="swimlane-diagram">
    <div ref="mermaidRef" class="mermaid-container" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import mermaid from 'mermaid';

const mermaidRef = ref<HTMLElement | null>(null);

const SWIMLANE_DIAGRAM = `
flowchart TB
    subgraph USER["用户/设备"]
        U1[访问前端]
        U2[输入账号密码]
        U3[携带Token访问业务]
        U4[创建工单/报工]
        D1[设备上报遥测]
    end

    subgraph GW["网关 mom-gateway"]
        G1[Path匹配]
        G2[lb转发]
    end

    subgraph ADM["Admin mom-admin"]
        A1[校验用户]
        A2[BCrypt校验]
        A3[签发JWT]
    end

    subgraph IOT["IoT mom-iot"]
        I1[reportTelemetry]
        I2[写入InfluxDB]
        I3[规则引擎评估]
        I4{匹配规则?}
        I5[冷却期检查]
        I6[创建告警]
        I7[人工resolve]
    end

    subgraph MES["MES mom-mes"]
        M1[创建工单]
        M2[工单下发]
        M3[报工录入]
        M4[工单完成]
    end

    subgraph WMS["WMS mom-wms"]
        W1[createRequisition]
        W2[创建领料单]
    end

    subgraph QMS["QMS mom-qms"]
        Q1[createInspectionTask]
        Q2[创建质检任务]
    end

    U1 --> U2 --> G1 --> G2 --> A1 --> A2 --> A3 --> U3
    D1 --> G1 --> G2 --> I1 --> I2 --> I3 --> I4
    I4 -->|是| I5 --> I6 --> I7
    I4 -->|否| I3
    U3 --> U4 --> G1 --> G2 --> M1 --> M2
    M2 -.->|Feign| W1 --> W2
    M2 --> M3 --> M4
    M4 -.->|Feign| Q1 --> Q2
`;

onMounted(async () => {
  if (!mermaidRef.value) return;
  mermaid.initialize({
    startOnLoad: false,
    theme: 'dark',
    flowchart: {
      useMaxWidth: true,
      htmlLabels: true,
      curve: 'basis'
    }
  });
  try {
    const id = 'swimlane-' + Date.now();
    const { svg } = await mermaid.render(id, SWIMLANE_DIAGRAM);
    mermaidRef.value.innerHTML = svg;
  } catch (e) {
    console.error('Mermaid render error:', e);
    mermaidRef.value.innerHTML = '<p class="mermaid-error">流程图渲染失败，请刷新页面重试</p>';
  }
});
</script>

<style scoped>
.swimlane-diagram {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 8px;
  padding: 1.5rem;
  margin: 1rem 0;
  overflow-x: auto;
}
.mermaid-container {
  display: flex;
  justify-content: center;
  min-height: 400px;
}
.mermaid-container :deep(svg) {
  max-width: 100%;
  height: auto;
}
.mermaid-error {
  color: #f87171;
  padding: 2rem;
}
</style>
