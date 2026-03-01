<template>
  <section class="services">
    <h1>微服务拓扑</h1>
    <p class="subtitle">
      LY-FactMesh 后端由多个独立的业务域服务组成，通过 Nacos 注册中心发现，
      由 Spring Cloud Gateway 统一对外暴露，并在 Swagger UI 中聚合接口文档。
    </p>

    <div class="grid">
      <div class="svc-card">
        <h2>Gateway</h2>
        <p>统一入口，路由、鉴权、限流，并聚合所有微服务 OpenAPI3 文档。</p>
      </div>
      <div class="svc-card">
        <h2>Admin</h2>
        <p>用户、角色、权限、字典等系统级能力，所有域服务的基础。</p>
      </div>
      <div class="svc-card">
        <h2>IoT</h2>
        <p>设备台账、状态监控、采集与事件，是 MES/QMS/WMS 的数据入口。</p>
      </div>
      <div class="svc-card">
        <h2>MES</h2>
        <p>工单、工序、报工、产线看板，覆盖生产执行全流程。</p>
      </div>
      <div class="svc-card">
        <h2>WMS</h2>
        <p>入库、出库、盘点、批次与库存预警，支撑生产物料流转。</p>
      </div>
      <div class="svc-card">
        <h2>QMS</h2>
        <p>质检任务、不合格处理与质量追溯，保证产品合规与稳定。</p>
      </div>
    </div>

    <div class="hint">
      前端通过 <code>/api</code> 代理访问网关，网关再转发至具体微服务，
      所有接口均在 <code>/swagger-ui.html</code> 中统一对外呈现。
    </div>
  </section>
</template>

<script setup lang="ts"></script>

<style scoped>
.services {
  max-width: 1120px;
  margin: 0 auto;
}

h1 {
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #9ca3af;
  margin-bottom: 1.5rem;
  max-width: 820px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.svc-card {
  padding: 1.1rem;
  border-radius: 0.75rem;
  background: radial-gradient(circle at top left, #1f2937, #020617);
  border: 1px solid #111827;
}

.svc-card h2 {
  font-size: 1rem;
  margin-bottom: 0.45rem;
}

.svc-card p {
  color: #9ca3af;
  font-size: 0.85rem;
}

.hint {
  margin-top: 1.5rem;
  font-size: 0.85rem;
  color: #9ca3af;
}

code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New",
    monospace;
  background: #111827;
  padding: 0.1rem 0.4rem;
  border-radius: 0.25rem;
}
</style>

