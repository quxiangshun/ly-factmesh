/**
 * 标签页（TagsView）状态管理
 */
import { ref } from 'vue';
import type { RouteLocationNormalizedLoaded } from 'vue-router';

export interface TagView {
  path: string;
  name?: string;
  title: string;
  affix?: boolean;
}

const TAGS_STORAGE_KEY = 'ly-factmesh-visited-tags';

function loadFromStorage(): TagView[] {
  try {
    const s = localStorage.getItem(TAGS_STORAGE_KEY);
    if (s) {
      const arr = JSON.parse(s) as TagView[];
      return Array.isArray(arr) ? arr : [];
    }
  } catch {
    /* ignore */
  }
  return [];
}

function saveToStorage(tags: TagView[]) {
  try {
    localStorage.setItem(TAGS_STORAGE_KEY, JSON.stringify(tags));
  } catch {
    /* ignore */
  }
}

function ensureDefaults(tags: TagView[]): TagView[] {
  const hasHome = tags.some((t) => t.path === '/dashboard' || t.path === '/');
  if (!hasHome) {
    return [{ path: '/dashboard', title: '首页概览', affix: true }, ...tags];
  }
  return tags;
}

const visitedTags = ref<TagView[]>(ensureDefaults(loadFromStorage()));

export function useTagsView() {
  function getTitle(route: RouteLocationNormalizedLoaded): string {
    const t = route.meta?.title;
    if (typeof t === 'string' && t) return t;
    if (route.name) return String(route.name);
    return route.path || '未命名';
  }

  function addTag(route: RouteLocationNormalizedLoaded) {
    const path = route.path;
    const title = getTitle(route);
    const affix = path === '/dashboard' || path === '/';
    if (visitedTags.value.some((t) => t.path === path)) return;
    const tag: TagView = { path, name: route.name as string, title, affix };
    const list = [...visitedTags.value];
    const lastAffix = list.map((t, i) => (t.affix ? i : -1)).filter((i) => i >= 0).pop();
    const insertAt = lastAffix != null ? lastAffix + 1 : 0;
    list.splice(insertAt, 0, tag);
    visitedTags.value = list;
    saveToStorage(list);
  }

  function closeTag(path: string) {
    visitedTags.value = visitedTags.value.filter((t) => t.path !== path);
    saveToStorage(visitedTags.value);
  }

  function closeOther(path: string) {
    visitedTags.value = visitedTags.value.filter((t) => t.path === path || t.affix);
    saveToStorage(visitedTags.value);
  }

  function closeAll() {
    visitedTags.value = visitedTags.value.filter((t) => t.affix);
    saveToStorage(visitedTags.value);
  }

  function closeLeft(path: string) {
    const idx = visitedTags.value.findIndex((t) => t.path === path);
    if (idx <= 0) return;
    visitedTags.value = visitedTags.value.filter((t, i) => i >= idx || t.affix);
    saveToStorage(visitedTags.value);
  }

  function closeRight(path: string) {
    const idx = visitedTags.value.findIndex((t) => t.path === path);
    if (idx < 0) return;
    visitedTags.value = visitedTags.value.filter((t, i) => i <= idx || t.affix);
    saveToStorage(visitedTags.value);
  }

  return { visitedTags, addTag, closeTag, closeOther, closeAll, closeLeft, closeRight, getTitle };
}
