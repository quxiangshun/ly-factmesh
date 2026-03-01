import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/services',
    name: 'Services',
    component: () => import('../views/ServicesView.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;

