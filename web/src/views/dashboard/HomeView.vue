<template>
  <section class="home">
    <div class="hero">
      <h1>企业级 MOM 平台</h1>
      <p>
        LY-FactMesh 提供设备物联、生产执行、仓储管理、质量管理全流程能力，
        通过微服务 + 前后端分离架构打造现代化数字工厂。
      </p>
      <div class="hero-actions">
        <RouterLink to="/services" class="btn primary">查看微服务</RouterLink>
        <a href="/swagger-ui.html" target="_blank" rel="noreferrer" class="btn ghost">
          打开接口文档
        </a>
      </div>
    </div>
    <div class="grid">
      <div class="card">
        <h2>领域驱动设计</h2>
        <p>严格拆分 admin / iot / mes / qms / wms 领域，代码结构与业务模型一致。</p>
      </div>
      <div class="card">
        <h2>统一接口规范</h2>
        <p>所有微服务基于 OpenAPI3 + Springdoc 输出接口文档，由 Gateway 聚合统一入口。</p>
      </div>
      <div class="card">
        <h2>开源友好</h2>
        <p>完整文档体系，清晰贡献指南，适合作为企业级 MOM 脚手架或最佳实践参考。</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts"></script>

<style scoped>
.home {
  max-width: 1120px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 2rem;
}

.hero h1 {
  font-size: 2.2rem;
  margin-bottom: 0.75rem;
}

.hero p {
  color: #9ca3af;
  max-width: 680px;
}

.hero-actions {
  margin-top: 1.5rem;
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.55rem 1.2rem;
  border-radius: 999px;
  font-size: 0.9rem;
  text-decoration: none;
}

.btn.primary {
  background: linear-gradient(90deg, #38bdf8, #22c55e);
  color: #0f172a;
}

.btn.ghost {
  border: 1px solid #374151;
  color: #e5e7eb;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
}

.card {
  padding: 1.25rem;
  border-radius: 0.75rem;
  background: radial-gradient(circle at top left, #1f2937, #020617);
  border: 1px solid #111827;
}

.card h2 {
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.card p {
  color: #9ca3af;
  font-size: 0.85rem;
}
</style>

