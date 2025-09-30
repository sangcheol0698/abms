<template>
  <li :data-node-id="node.departmentId" class="space-y-1">
    <div
      class="flex items-start gap-1.5 rounded-md p-1.5 transition hover:bg-accent/50"
      :class="{
        'bg-primary/10 border border-primary/40 shadow-sm': isSelected,
      }"
    >
      <button
        v-if="showToggle"
        type="button"
        class="mt-0.5 inline-flex h-5 w-5 items-center justify-center rounded-md border border-border/70 bg-background text-muted-foreground transition hover:text-foreground"
        :aria-expanded="!isCollapsed"
        :aria-label="isCollapsed ? '하위 조직 펼치기' : '하위 조직 접기'"
        @click.stop="toggleNode(node.departmentId)"
      >
        <ChevronRight v-if="isCollapsed" class="h-3 w-3" />
        <ChevronDown v-else class="h-3 w-3" />
      </button>
      <span v-else class="mt-0.5 inline-flex h-5 w-5" aria-hidden="true" />
      <div class="flex flex-1 flex-col gap-1">
        <button
          type="button"
          class="flex flex-col items-start gap-0.5 text-left"
          @click="selectNode(node.departmentId)"
        >
          <span class="text-[13px] font-semibold text-foreground">
            <template v-for="(segment, index) in nameSegments" :key="index">
              <mark v-if="segment.matched" class="rounded bg-primary/20 px-0.5 text-primary">
                {{ segment.text }}
              </mark>
              <span v-else>{{ segment.text }}</span>
            </template>
          </span>
          <div class="flex flex-wrap items-center gap-1.5 text-[11px] text-muted-foreground">
            <Badge variant="outline" class="px-1.5 py-0 text-[11px] font-medium">
              {{ node.departmentCode }}
            </Badge>
            <span class="inline-flex items-center gap-1">
              <Users class="h-3 w-3" />
              {{ node.employeeCount }}명
            </span>
            <span v-if="node.departmentLeader" class="inline-flex items-center gap-1">
              <UserRound class="h-3 w-3" />
              {{ node.departmentLeader.employeeName }}
            </span>
          </div>
        </button>
        <p v-if="node.children.length && isCollapsed" class="text-[11px] text-muted-foreground/80">
          하위 조직 {{ node.children.length }}개가 숨겨져 있습니다
        </p>
      </div>
    </div>

    <Transition name="tree-collapse">
      <ul
        v-if="hasChildren && !isCollapsed"
        class="ml-4 border-l border-border/60 pl-2.5"
        role="group"
      >
        <OrganizationTreeNode
          v-for="child in node.children"
          :key="child.departmentId"
          :node="child"
          :collapsed-map="props.collapsedMap"
          :selected-id="props.selectedId"
          :toggle="props.toggle"
          :select="props.select"
          :force-expand="props.forceExpand"
          :search-term="props.searchTerm"
        />
      </ul>
    </Transition>
  </li>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ChevronDown, ChevronRight, UserRound, Users } from 'lucide-vue-next';
import { Badge } from '@/components/ui/badge';
import type { OrganizationChartNode } from '@/features/organization/models/organization';

interface Props {
  node: OrganizationChartNode;
  collapsedMap: Record<string, boolean>;
  selectedId?: string;
  toggle: (departmentId: string) => void;
  select: (departmentId: string) => void;
  forceExpand: boolean;
  searchTerm?: string;
}

defineOptions({ name: 'OrganizationTreeNode' });

const props = defineProps<Props>();

const hasChildren = computed(() => props.node.children.length > 0);
const isCollapsed = computed(() =>
  props.forceExpand ? false : Boolean(props.collapsedMap[props.node.departmentId]),
);
const isSelected = computed(() => props.node.departmentId === props.selectedId);
const showToggle = computed(() => hasChildren.value && !props.forceExpand);
const normalizedSearch = computed(() => props.searchTerm?.toLowerCase() ?? '');
const hasSearch = computed(() => normalizedSearch.value.length > 0);

const nameSegments = computed(() => {
  const name = props.node.departmentName ?? '';
  if (!hasSearch.value) {
    return [{ text: name, matched: false }];
  }
  const lowerName = name.toLowerCase();
  const term = normalizedSearch.value;
  const segments: Array<{ text: string; matched: boolean }> = [];

  let startIndex = 0;
  let matchIndex = lowerName.indexOf(term, startIndex);

  while (matchIndex !== -1) {
    if (matchIndex > startIndex) {
      segments.push({ text: name.slice(startIndex, matchIndex), matched: false });
    }
    segments.push({ text: name.slice(matchIndex, matchIndex + term.length), matched: true });
    startIndex = matchIndex + term.length;
    matchIndex = lowerName.indexOf(term, startIndex);
  }

  if (startIndex < name.length) {
    segments.push({ text: name.slice(startIndex), matched: false });
  }

  return segments.length > 0 ? segments : [{ text: name, matched: false }];
});

function toggleNode(departmentId: string) {
  props.toggle(departmentId);
}

function selectNode(departmentId: string) {
  props.select(departmentId);
}
</script>

<style scoped>
.tree-collapse-enter-active,
.tree-collapse-leave-active {
  transition: all 0.18s ease;
}

.tree-collapse-enter-from,
.tree-collapse-leave-to {
  opacity: 0;
  max-height: 0;
}

.tree-collapse-enter-to,
.tree-collapse-leave-from {
  opacity: 1;
  max-height: 999px;
}
</style>
