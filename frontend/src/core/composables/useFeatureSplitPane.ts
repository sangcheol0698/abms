import { computed, inject, provide, ref, watch } from 'vue';
import type { ComputedRef, CSSProperties, Ref } from 'vue';
import { useBreakpoints, useWindowSize } from '@vueuse/core';

const FEATURE_SPLIT_PANE_KEY = Symbol('featureSplitPane');

export interface FeatureSplitPaneOptions {
  breakpointPx?: number;
  headerOffset?: number;
  minHeight?: number;
}

export interface FeatureSplitPaneContext {
  isLargeScreen: ComputedRef<boolean>;
  isSidebarCollapsed: Ref<boolean>;
  isSidebarOpen: Ref<boolean>;
  openSidebar: () => void;
  closeSidebar: () => void;
  toggleSidebar: () => void;
  collapseSidebar: () => void;
  expandSidebar: () => void;
  sectionStyle: ComputedRef<CSSProperties | undefined>;
}

export function useProvideFeatureSplitPane(
  options: FeatureSplitPaneOptions = {},
): FeatureSplitPaneContext {
  const breakpointPx = options.breakpointPx ?? 1024;
  const headerOffset = options.headerOffset ?? 64;
  const minHeight = options.minHeight ?? 320;

  const breakpoints = useBreakpoints({ lg: breakpointPx });
  const isLargeScreen = breakpoints.greater('lg');
  const isSidebarCollapsed = ref(false);
  const isSidebarOpen = ref(false);
  const { height: windowHeight } = useWindowSize();

  watch(
    isLargeScreen,
    (value) => {
      if (value) {
        isSidebarOpen.value = false;
      } else {
        isSidebarCollapsed.value = false;
      }
    },
    { immediate: true },
  );

  function collapseSidebar() {
    if (!isLargeScreen.value) {
      return;
    }
    isSidebarCollapsed.value = true;
  }

  function expandSidebar() {
    if (!isLargeScreen.value) {
      return;
    }
    isSidebarCollapsed.value = false;
  }

  function openSidebar() {
    if (isLargeScreen.value) {
      expandSidebar();
    } else {
      isSidebarOpen.value = true;
    }
  }

  function closeSidebar() {
    if (isLargeScreen.value) {
      collapseSidebar();
    } else {
      isSidebarOpen.value = false;
    }
  }

  function toggleSidebar() {
    if (isLargeScreen.value) {
      if (isSidebarCollapsed.value) {
        expandSidebar();
      } else {
        collapseSidebar();
      }
    } else {
      isSidebarOpen.value = !isSidebarOpen.value;
    }
  }

  const sectionStyle = computed(() => {
    const viewportHeight = windowHeight.value || 0;
    if (viewportHeight === 0) {
      return undefined;
    }
    const available = Math.max(viewportHeight - headerOffset, minHeight);
    return {
      minHeight: `${available}px`,
      height: `${available}px`,
      maxHeight: `${available}px`,
    };
  });

  const context: FeatureSplitPaneContext = {
    isLargeScreen,
    isSidebarCollapsed,
    isSidebarOpen,
    openSidebar,
    closeSidebar,
    toggleSidebar,
    collapseSidebar,
    expandSidebar,
    sectionStyle,
  };

  provide(FEATURE_SPLIT_PANE_KEY, context);

  return context;
}

export function useFeatureSplitPane(): FeatureSplitPaneContext {
  const context = inject<FeatureSplitPaneContext | null>(FEATURE_SPLIT_PANE_KEY, null);
  if (!context) {
    throw new Error('useFeatureSplitPane must be used inside a FeatureSplitLayout.');
  }
  return context;
}
