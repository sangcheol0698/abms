<template>
  <li :data-node-id="node.departmentId" class="space-y-1">
    <div
      class="group flex cursor-pointer items-center gap-1.5 rounded-lg px-2 py-1.5 text-sm transition"
      :class="{
        'bg-primary text-primary-foreground font-medium shadow-sm': isSelected,
        'hover:bg-muted/50 text-foreground': !isSelected,
      }"
      @click="selectNode(node.departmentId)"
    >
      <button
        v-if="showToggle"
        type="button"
        class="inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-md transition"
        :class="
          isSelected
            ? 'text-primary-foreground/90 hover:text-primary-foreground'
            : 'text-muted-foreground hover:text-foreground'
        "
        :aria-expanded="!isCollapsed"
        :aria-label="isCollapsed ? '하위 조직 펼치기' : '하위 조직 접기'"
        @click.stop="toggleNode(node.departmentId)"
      >
        <ChevronRight v-if="isCollapsed" class="h-3 w-3" />
        <ChevronDown v-else class="h-3 w-3" />
      </button>
      <span v-else class="inline-flex h-5 w-5 shrink-0" aria-hidden="true" />

      <div class="flex flex-1 items-center gap-2 truncate text-left">
        <component
          :is="departmentIcon"
          class="h-4 w-4 shrink-0 transition"
          :class="
            isSelected
              ? 'text-primary-foreground'
              : 'text-muted-foreground group-hover:text-foreground'
          "
          aria-hidden="true"
        />
        <span
          class="truncate text-[13px] font-medium"
          :class="isSelected ? 'text-primary-foreground' : 'text-foreground'"
        >
          <template v-for="(segment, index) in nameSegments" :key="index">
            <mark
              v-if="segment.matched"
              class="rounded px-0.5"
              :class="
                isSelected
                  ? 'bg-primary-foreground/20 text-primary-foreground'
                  : 'bg-primary/20 text-primary'
              "
            >
              {{ segment.text }}
            </mark>
            <span v-else>{{ segment.text }}</span>
          </template>
        </span>
      </div>

      <div
        class="ms-2 flex shrink-0 items-center gap-1 text-[11px] font-medium"
        :class="isSelected ? 'text-primary-foreground/80' : 'text-muted-foreground'"
      >
        <component :is="headcountIcon" class="h-3 w-3" aria-hidden="true" />
        <span class="whitespace-nowrap">{{ displayHeadcount }}</span>
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
import type { Component } from 'vue';
import {
  Building2,
  BriefcaseBusiness,
  ChevronDown,
  ChevronRight,
  FlaskConical,
  Folder,
  FolderOpen,
  Layers,
  UserRound,
  UsersRound,
} from 'lucide-vue-next';
import type { OrganizationChartNode } from '@/features/department/models/organization';

interface Props {
  node: OrganizationChartNode;
  collapsedMap: Record<string, boolean>;
  selectedId?: number;
  toggle: (departmentId: number) => void;
  select: (departmentId: number) => void;
  forceExpand: boolean;
  searchTerm?: string;
}

defineOptions({ name: 'OrganizationTreeNode' });

const props = defineProps<Props>();

const ICON_MATCHERS: Array<{ pattern: RegExp; icon: Component }> = [
  { pattern: /(hq|head|본부)/, icon: Building2 },
  { pattern: /(division|사업부|group|본사)/, icon: Layers },
  { pattern: /(department|dept|부서|실)/, icon: BriefcaseBusiness },
  { pattern: /(team|스쿼드|squad|chapter|셀|팀)/, icon: UsersRound },
  { pattern: /(unit|center|센터|lab|연구)/, icon: FlaskConical },
];

const hasChildren = computed(() => props.node.children.length > 0);
const isCollapsed = computed(() =>
  props.forceExpand ? false : Boolean(props.collapsedMap[String(props.node.departmentId)]),
);
const isSelected = computed(() => props.node.departmentId === props.selectedId);
const showToggle = computed(() => hasChildren.value && !props.forceExpand);
const departmentIcon = computed<Component>(() => {
  const rawType = (props.node.departmentType ?? '').trim().toLowerCase();

  if (rawType.length > 0) {
    const matched = ICON_MATCHERS.find(({ pattern }) => pattern.test(rawType));
    if (matched) {
      return matched.icon;
    }
  }

  return hasChildren.value && !isCollapsed.value ? FolderOpen : Folder;
});
const displayHeadcount = computed(() => {
  const count = props.node.employeeCount;
  if (typeof count === 'number' && count >= 0) {
    return `${count}명`;
  }
  return '—';
});
const headcountIcon = computed<Component>(() =>
  displayHeadcount.value === '—' ? UserRound : UsersRound,
);
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

function toggleNode(departmentId: number) {
  props.toggle(departmentId);
}

function selectNode(departmentId: number) {
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
