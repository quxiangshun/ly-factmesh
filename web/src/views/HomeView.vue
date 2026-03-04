<template>
  <section class="home">
    <div class="hero">
      <h1>企业级 MOM 平台</h1>
      <p>
        LY-FactMesh 提供设备物联、生产执行、仓储管理、质量管理全流程能力，
        通过微服务 + 前后端分离架构打造现代化数字工厂。
      </p>
      <div class="hero-actions">
        <RouterLink to="/services">
          <el-button type="primary">查看微服务</el-button>
        </RouterLink>
        <a href="/swagger-ui.html" target="_blank" rel="noreferrer">
          <el-button>打开接口文档</el-button>
        </a>
      </div>
    </div>
    <div class="grid">
      <el-card shadow="never" class="card">
        <template #header>
          <span>领域驱动设计</span>
        </template>
        <p>严格拆分 admin / iot / mes / qms / wms 领域，代码结构与业务模型一致。</p>
      </el-card>
      <el-card shadow="never" class="card">
        <template #header>
          <span>统一接口规范</span>
        </template>
        <p>所有微服务基于 OpenAPI3 + Springdoc 输出接口文档，由 Gateway 聚合统一入口。</p>
      </el-card>
      <el-card shadow="never" class="card">
        <template #header>
          <span>开源友好</span>
        </template>
        <p>完整文档体系，清晰贡献指南，适合作为企业级 MOM 脚手架或最佳实践参考。</p>
      </el-card>
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

.hero-actions a {
  text-decoration: none;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
}

.card {
  background: radial-gradient(circle at top left, #1f2937, #020617);
  border: 1px solid #334155;
}

.card :deep(.el-card__header) {
  color: #e5e7eb;
  font-size: 1rem;
  border-bottom-color: #334155;
}

.card p {
  color: #9ca3af;
  font-size: 0.85rem;
  margin: 0;
}
</style>
