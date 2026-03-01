import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 开发时接口文档与 Knife4j 资源走网关，便于同源访问
      '/doc.html': { target: 'http://localhost:8080', changeOrigin: true },
      '/v3': { target: 'http://localhost:8080', changeOrigin: true },
      '/webjars': { target: 'http://localhost:8080', changeOrigin: true },
      '/swagger-resources': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
});

