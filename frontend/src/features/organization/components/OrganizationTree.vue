<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="sticky top-0 z-10 flex flex-col gap-1.5 border-b border-border/60 bg-card/95 px-3.5 pt-3 pb-2.5">
      <div class="relative">
        <Input v-model="searchTerm" type="text" placeholder="부서명, 코드, 리더를 검색하세요" class="h-9 pe-9 text-sm" />
        <button v-if="isSearching" type="button"
          class="absolute inset-y-0 right-2 flex items-center text-muted-foreground transition hover:text-foreground"
          @click="clearSearch" aria-label="검색어 지우기">
          <X class="h-4 w-4" />
        </button>
      </div>

      <div class="flex flex-wrap items-center justify-between gap-2 text-[11px] text-muted-foreground">
        <div class="flex items-center gap-1.5">
          <Button variant="outline" size="sm" class="h-5 px-3 text-xs" :disabled="isFullyExpanded || isSearching"
            @click="expandAll">
            전체 펼치기
          </Button>
          <Button variant="outline" size="sm" class="h-5 px-3 text-xs" :disabled="isFullyCollapsed || isSearching"
            @click="collapseAll">
            전체 접기
          </Button>
          <Button v-if="isSearching" variant="ghost" size="sm" class="h-7 px-2 text-xs" @click="clearSearch">
            검색 초기화
          </Button>
        </div>
        <span v-if="isSearching">검색 결과 {{ visibleDepartmentCount }}개</span>
        <span v-else>총 부서 {{ totalDepartments }}개</span>
      </div>
    </div>

    <div ref="scrollRef" class="flex-1 min-h-0 overflow-y-auto bg-card p-3.5 pt-2">
      <p v-if="!filteredNodes.length"
        class="rounded-md border border-dashed border-border/60 bg-muted/20 p-3 text-xs text-muted-foreground">
        {{ emptyStateMessage }}
      </p>
      <ul v-else class="space-y-1.5" role="tree">
        <OrganizationTreeNode v-for="node in filteredNodes" :key="node.departmentId" :node="node"
          :collapsed-map="collapsedMap" :selected-id="selectedNodeId" :toggle="handleToggle" :select="handleSelect"
          :force-expand="isSearching" :search-term="normalizedSearchTerm" />
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import OrganizationTreeNode from '@/features/organization/components/OrganizationTreeNode.vue';
import type { OrganizationChartNode } from '@/features/organization/models/organization';
import { X } from 'lucide-vue-next';

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
const visibleDepartmentCount = computed(() => countDepartments(filteredNodes.value));
const emptyStateMessage = computed(() =>
  isSearching.value ? '검색어와 일치하는 조직이 없습니다.' : '표시할 조직 정보가 없습니다.',
);

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

function clearSearch() {
  searchTerm.value = '';
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

function countDepartments(nodes: OrganizationChartNode[]): number {
  let count = 0;
  nodes.forEach((node) => {
    count += 1;
    if (node.children?.length) {
      count += countDepartments(node.children);
    }
  });
  return count;
}
</script>
