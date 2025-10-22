<template>
  <section class="relative flex min-h-0 flex-1 flex-col overflow-hidden" :style="pane.sectionStyle.value">
    <template v-if="pane.isLargeScreen.value">
      <ResizablePanelGroup direction="horizontal" class="flex min-h-0 flex-1 overflow-hidden">
        <ResizablePanel
          :default-size="sidebarPanelSize"
          :min-size="pane.isSidebarCollapsed.value ? sidebarCollapsedSize : sidebarMinSize"
          :max-size="sidebarMaxSize"
          :collapsed="pane.isSidebarCollapsed.value"
          :collapsed-size="sidebarCollapsedSize"
        >
          <div class="flex h-full min-h-0 flex-col overflow-hidden">
            <slot name="sidebar" :pane="pane" />
          </div>
        </ResizablePanel>

        <ResizableHandle with-handle class="bg-border/70" />

        <ResizablePanel
          :default-size="contentPanelSize"
          :min-size="contentMinSize"
        >
          <div class="flex h-full min-h-0 flex-col overflow-hidden">
            <slot :pane="pane" />
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </template>

    <template v-else>
      <div class="relative flex h-full min-h-0 flex-1 flex-col overflow-hidden">
        <slot :pane="pane" />

        <Transition name="fade">
          <div
            v-if="pane.isSidebarOpen.value"
            class="absolute inset-0 z-30 flex bg-background/80 backdrop-blur-sm"
          >
            <div class="w-10 flex-shrink-0" @click="pane.closeSidebar"></div>
            <aside class="flex h-full w-full max-w-xs flex-col border-l border-border/60 bg-background">
              <slot name="sidebar" :pane="pane" />
            </aside>
          </div>
        </Transition>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '@/components/ui/resizable';
import { useProvideFeatureSplitPane } from '@/core/composables/useFeatureSplitPane';

const props = withDefaults(
  defineProps<{
    breakpointPx?: number;
    headerOffset?: number;
    minHeight?: number;
    sidebarDefaultSize?: number;
    sidebarMinSize?: number;
    sidebarMaxSize?: number;
    sidebarCollapsedSize?: number;
    contentMinSize?: number;
  }>(),
  {
    breakpointPx: 1024,
    headerOffset: 64,
    minHeight: 320,
    sidebarDefaultSize: 22,
    sidebarMinSize: 14,
    sidebarMaxSize: 32,
    sidebarCollapsedSize: 0,
    contentMinSize: 60,
  },
);

const pane = useProvideFeatureSplitPane({
  breakpointPx: props.breakpointPx,
  headerOffset: props.headerOffset,
  minHeight: props.minHeight,
});

const sidebarPanelSize = computed(() =>
  pane.isSidebarCollapsed.value ? props.sidebarCollapsedSize : props.sidebarDefaultSize,
);

const contentPanelSize = computed(() =>
  Math.max(100 - sidebarPanelSize.value, props.contentMinSize),
);
</script>
