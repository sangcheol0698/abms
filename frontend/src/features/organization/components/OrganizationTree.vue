<template>
  <div class="flex h-full flex-col gap-2.5">
    <Input
      v-model="searchTerm"
      type="search"
      placeholder="부서명을 검색하세요"
      class="h-8 text-sm"
    />

    <div class="flex flex-wrap items-center justify-between gap-1.5 text-xs text-muted-foreground">
      <div class="flex items-center gap-1.5">
        <Button
          variant="outline"
          size="sm"
          class="h-7 px-3 text-xs"
          :disabled="isFullyExpanded || isSearching"
          @click="expandAll"
        >
          전체 펼치기
        </Button>
        <Button
          variant="outline"
          size="sm"
          class="h-7 px-3 text-xs"
          :disabled="isFullyCollapsed || isSearching"
          @click="collapseAll"
        >
          전체 접기
        </Button>
      </div>
      <span> 총 부서 {{ totalDepartments }}개 </span>
    </div>

    <div ref="scrollRef" class="flex-1 overflow-y-auto pr-0.5">
      <p
        v-if="!filteredNodes.length"
        class="rounded-md border border-dashed border-border/60 bg-muted/20 p-3 text-xs text-muted-foreground"
      >
        표시할 조직 정보가 없습니다.
      </p>
      <ul v-else class="space-y-1.5" role="tree">
        <OrganizationTreeNode
          v-for="node in filteredNodes"
          :key="node.departmentId"
          :node="node"
          :collapsed-map="collapsedMap"
          :selected-id="selectedNodeId"
          :toggle="handleToggle"
          :select="handleSelect"
          :force-expand="isSearching"
          :search-term="normalizedSearchTerm"
        />
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch, withDefaults } from 'vue';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import OrganizationTreeNode from '@/features/organization/components/OrganizationTreeNode.vue';
import type { OrganizationChartNode } from '@/features/organization/models/organization';

interface Props {
  nodes: OrganizationChartNode[];
  selectedNodeId?: string;
  defaultExpandAll?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  defaultExpandAll: true,
});
const emit = defineEmits<{ (e: 'update:selectedNodeId', value: string): void }>();

const collapsedMap = ref<Record<string, boolean>>({});
const depthMap = ref<Record<string, number>>({});
const parentMap = ref<Record<string, string | null>>({});
const collapsibleIds = ref<string[]>([]);
const hasInitializedCollapse = ref(false);
const scrollRef = ref<HTMLDivElement | null>(null);
const searchTerm = ref('');

const totalDepartments = computed(() => Object.keys(parentMap.value).length);

const collapsibleCount = computed(
  () => collapsibleIds.value.filter((id) => (depthMap.value[id] ?? 0) >= 1).length,
);

const collapsedCount = computed(
  () =>
    Object.keys(collapsedMap.value).filter(
      (id) => (depthMap.value[id] ?? 0) >= 1 && collapsedMap.value[id],
    ).length,
);

const isFullyExpanded = computed(() => Object.keys(collapsedMap.value).length === 0);
const isFullyCollapsed = computed(
  () => collapsibleCount.value > 0 && collapsedCount.value >= collapsibleCount.value,
);

const normalizedSearchTerm = computed(() => searchTerm.value.trim().toLowerCase());
const isSearching = computed(() => normalizedSearchTerm.value.length > 0);

const filteredNodes = computed<OrganizationChartNode[]>(() => {
  if (!isSearching.value) {
    return props.nodes;
  }
  return filterNodes(props.nodes, normalizedSearchTerm.value, props.selectedNodeId);
});

watch(
  () => props.nodes,
  (nodes) => {
    rebuildIndexes(nodes);
    expandAncestors(props.selectedNodeId);
  },
  { deep: true, immediate: true },
);

watch(isSearching, (next) => {
  if (next) {
    collapsedMap.value = {};
  }
});

watch(
  () => props.selectedNodeId,
  (next) => {
    expandAncestors(next);
    scrollIntoView(next);
  },
);

function rebuildIndexes(nodes: OrganizationChartNode[]) {
  const nextParent: Record<string, string | null> = {};
  const nextDepth: Record<string, number> = {};
  const nextCollapsible: string[] = [];

  const traverse = (node: OrganizationChartNode, parentId: string | null, depth: number) => {
    nextParent[node.departmentId] = parentId;
    nextDepth[node.departmentId] = depth;

    if (node.children.length > 0) {
      nextCollapsible.push(node.departmentId);
      node.children.forEach((child) => traverse(child, node.departmentId, depth + 1));
    }
  };

  nodes.forEach((node) => traverse(node, null, 0));

  const prevState = collapsedMap.value;
  const nextState: Record<string, boolean> = {};

  nextCollapsible.forEach((id) => {
    if (prevState[id] !== undefined) {
      nextState[id] = prevState[id];
    }
  });

  if (!hasInitializedCollapse.value) {
    hasInitializedCollapse.value = true;
    if (!props.defaultExpandAll) {
      nextCollapsible.forEach((id) => {
        if ((nextDepth[id] ?? 0) >= 2) {
          nextState[id] = true;
        }
      });
    }
  }

  collapsedMap.value = nextState;
  parentMap.value = nextParent;
  depthMap.value = nextDepth;
  collapsibleIds.value = nextCollapsible;
}

function expandAncestors(departmentId?: string) {
  if (!departmentId) {
    return;
  }

  const ancestors = new Set<string>();
  let current = parentMap.value[departmentId] ?? null;

  while (current) {
    ancestors.add(current);
    current = parentMap.value[current] ?? null;
  }

  if (ancestors.size === 0) {
    return;
  }

  const nextState = { ...collapsedMap.value };
  let modified = false;

  ancestors.forEach((ancestor) => {
    if (nextState[ancestor]) {
      delete nextState[ancestor];
      modified = true;
    }
  });

  if (modified) {
    collapsedMap.value = nextState;
  }
}

function handleToggle(departmentId: string) {
  if (isSearching.value) {
    return;
  }
  const isCollapsed = Boolean(collapsedMap.value[departmentId]);
  if (isCollapsed) {
    const nextState = { ...collapsedMap.value };
    delete nextState[departmentId];
    collapsedMap.value = nextState;
  } else {
    collapsedMap.value = { ...collapsedMap.value, [departmentId]: true };
  }
}

function handleSelect(departmentId: string) {
  expandAncestors(departmentId);
  emit('update:selectedNodeId', departmentId);
}

function expandAll() {
  collapsedMap.value = {};
}

function collapseAll() {
  const nextState: Record<string, boolean> = {};
  collapsibleIds.value.forEach((id) => {
    if ((depthMap.value[id] ?? 0) >= 1) {
      nextState[id] = true;
    }
  });
  collapsedMap.value = nextState;
}

function scrollIntoView(departmentId?: string) {
  if (!departmentId) {
    return;
  }

  const depth = depthMap.value[departmentId];
  if (depth === 0) {
    return;
  }

  nextTick(() => {
    const container = scrollRef.value;
    if (!container) {
      return;
    }
    const target = container.querySelector<HTMLElement>(`[data-node-id="${departmentId}"]`);
    if (!target) {
      return;
    }

    const containerRect = container.getBoundingClientRect();
    const targetRect = target.getBoundingClientRect();
    const TOP_MARGIN = 8;
    const BOTTOM_MARGIN = 8;

    const isAbove = targetRect.top < containerRect.top + TOP_MARGIN;
    const isBelow =
      targetRect.bottom > containerRect.bottom - BOTTOM_MARGIN &&
      targetRect.top > containerRect.top + TOP_MARGIN;

    if (isAbove || isBelow) {
      target.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  });
}

onBeforeUnmount(() => {
  collapsedMap.value = {};
});

function filterNodes(
  nodes: OrganizationChartNode[],
  term: string,
  selectedId?: string,
): OrganizationChartNode[] {
  const result: OrganizationChartNode[] = [];

  nodes.forEach((node) => {
    const childMatches = filterNodes(node.children ?? [], term, selectedId);
    const matches = matchesNode(node, term) || node.departmentId === selectedId;

    if (matches || childMatches.length > 0) {
      result.push({
        ...node,
        children: childMatches,
      });
    }
  });

  return result;
}

function matchesNode(node: OrganizationChartNode, term: string): boolean {
  const lowerTerm = term.toLowerCase();
  if (node.departmentName.toLowerCase().includes(lowerTerm)) {
    return true;
  }
  if (node.departmentCode.toLowerCase().includes(lowerTerm)) {
    return true;
  }
  if (node.departmentLeader?.employeeName?.toLowerCase().includes(lowerTerm)) {
    return true;
  }
  return false;
}
</script>
