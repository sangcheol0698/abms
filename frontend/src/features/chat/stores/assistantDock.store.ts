import { defineStore } from 'pinia';
import { ref, watch } from 'vue';

const DOCK_OPEN_STORAGE_KEY = 'assistant_dock_open';

export const useAssistantDockStore = defineStore('assistantDock', () => {
  const activeSessionId = ref<string | null>(null);
  const isDockOpen = ref(true);
  const isMobileOpen = ref(false);

  if (typeof window !== 'undefined') {
    const savedDockOpen = window.localStorage.getItem(DOCK_OPEN_STORAGE_KEY);
    if (savedDockOpen === 'true' || savedDockOpen === 'false') {
      isDockOpen.value = savedDockOpen === 'true';
    }
  }

  watch(isDockOpen, (next) => {
    if (typeof window === 'undefined') return;
    window.localStorage.setItem(DOCK_OPEN_STORAGE_KEY, String(next));
  });

  function setActiveSessionId(sessionId: string | null) {
    activeSessionId.value = sessionId;
  }

  function setDockOpen(open: boolean) {
    isDockOpen.value = open;
  }

  function setMobileOpen(open: boolean) {
    isMobileOpen.value = open;
  }

  return {
    activeSessionId,
    isDockOpen,
    isMobileOpen,
    setActiveSessionId,
    setDockOpen,
    setMobileOpen,
  };
});
