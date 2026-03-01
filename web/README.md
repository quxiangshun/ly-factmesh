# LY-FactMesh 前端 (web)

Vite + Vue 3 + TypeScript 单页应用，对接网关统一入口。

## 环境

- Node 18+
- 本地开发时需先启动网关（默认 `http://localhost:8080`）

## 开发

```bash
npm install
npm run dev
```

- 前端：<http://localhost:5173>
- 接口请求通过 Vite 代理 `/api` → 网关 `http://localhost:8080`
- 接口文档通过代理 `/swagger-ui.html`、`/v3`、`/webjars`、`/swagger-resources` → 网关，开发时可直接打开 [http://localhost:5173/swagger-ui.html](http://localhost:5173/swagger-ui.html)

## 构建

```bash
npm run build
```

产物在 `dist/`，部署时需将 `/api`、`/swagger-ui.html` 等反向代理到网关。

## 图标

使用 [Iconify](https://icon-sets.iconify.design/) 提供的图标，通过 `@iconify/vue` 引入，例如：`<Icon icon="mdi:factory" />`。
