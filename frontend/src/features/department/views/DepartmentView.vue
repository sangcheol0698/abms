<template>
  <section class="flex h-full min-h-0 flex-1 flex-col gap-6 overflow-hidden">
    <div
      v-if="isLoading"
      class="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10"
    >
      <span class="text-sm text-muted-foreground">부서 정보를 불러오는 중입니다...</span>
    </div>

    <FeatureSplitLayout
      v-if="!isLoading"
      :sidebar-default-size="20"
      :sidebar-min-size="14"
      :sidebar-max-size="32"
      :content-min-size="52"
    >
      <template #sidebar="{ pane }">
        <div class="flex h-full min-h-0 flex-col">
          <DepartmentTree
            class="flex-1"
            :nodes="chart"
            v-model:selectedNodeId="selectedDepartmentId"
            @update:selectedNodeId="handleTreeSelection($event, pane)"
          />
        </div>
      </template>

      <template #default="{ pane }">
        <div class="flex h-full min-h-0 flex-col">
          <header class="flex flex-col px-4 pt-4 pb-2 gap-4">
            <div class="flex items-center">
              <Button
                variant="ghost"
                size="icon"
                class="-ml-1 h-8 w-8 text-muted-foreground transition hover:text-foreground"
                aria-label="부서 사이드바 토글"
                @click="pane.toggleSidebar()"
              >
                <Menu
                  class="h-4 w-4 transition"
                  :class="pane.isSidebarCollapsed.value ? 'rotate-180' : ''"
                />
              </Button>
              <Breadcrumb class="flex flex-wrap text-sm text-muted-foreground">
                <BreadcrumbList>
                  <template v-for="(segment, index) in headerBreadcrumbs" :key="segment.id">
                    <BreadcrumbItem>
                      <template v-if="segment.clickable">
                        <BreadcrumbLink as-child>
                          <router-link
                            v-if="segment.to"
                            :to="segment.to"
                            class="transition hover:text-foreground"
                          >
                            {{ segment.name }}
                          </router-link>
                          <button
                            v-else
                            type="button"
                            class="transition hover:text-foreground"
                            @click="handleBreadcrumbSelect(segment.id)"
                          >
                            {{ segment.name }}
                          </button>
                        </BreadcrumbLink>
                      </template>
                      <BreadcrumbPage v-else class="font-semibold">{{
                        segment.name
                      }}</BreadcrumbPage>
                    </BreadcrumbItem>
                    <BreadcrumbSeparator v-if="index < headerBreadcrumbs.length - 1">
                      <Slash />
                    </BreadcrumbSeparator>
                  </template>
                </BreadcrumbList>
              </Breadcrumb>
            </div>
            <Separator />
            <h2 class="text-2xl font-bold tracking-tight pl-2" v-if="selectedDepartment">
              {{ selectedDepartment.departmentName }}
            </h2>
          </header>

          <div class="flex h-full min-h-0 flex-col overflow-hidden">
            <div class="flex-1 min-h-0 overflow-y-auto pl-2">
              <DepartmentDetailPanel
                :department="selectedDepartment"
                :isLoading="isDepartmentLoading"
                @refresh="handleRefresh"
              />
            </div>
          </div>
        </div>
      </template>
    </FeatureSplitLayout>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import type { FeatureSplitPaneContext } from '@/core/composables/useFeatureSplitPane';
import type {
  DepartmentChartNode,
  DepartmentSummary,
} from '@/features/department/models/department';
import DepartmentTree from '@/features/department/components/DepartmentTree.vue';
import DepartmentDetailPanel from '@/features/department/components/DepartmentDetailPanel.vue';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Menu, Slash } from 'lucide-vue-next';
import {
  useDepartmentDetailQuery,
  useDepartmentOrganizationChartQuery,
} from '@/features/department/queries/useDepartmentQueries';
import { departmentKeys, queryClient } from '@/core/query';
import { toast } from 'vue-sonner';

const chartQuery = useDepartmentOrganizationChartQuery();
const chart = computed(() => chartQuery.data.value ?? []);
const isLoading = computed(() => chartQuery.isLoading.value);

const selectedDepartmentId = ref<number | undefined>();
const router = useRouter();
const route = useRoute();

watch(
  () => route.params.departmentId,
  (newId) => {
    if (newId) {
      const parsed = Number(newId);
      selectedDepartmentId.value = Number.isFinite(parsed) ? parsed : undefined;
    } else {
      selectedDepartmentId.value = undefined;
    }
  },
  { immediate: true },
);

const selectedBreadcrumb = computed(() =>
  buildDepartmentPath(chart.value, selectedDepartmentId.value).map((node) => ({
    id: node.departmentId,
    name: node.departmentName,
  })),
);

interface HeaderBreadcrumb {
  id: number;
  name: string;
  clickable: boolean;
  to?: string;
}

const headerBreadcrumbs = computed<HeaderBreadcrumb[]>(() => {
  const segments = selectedBreadcrumb.value;
  const crumbs: HeaderBreadcrumb[] = [];

  segments.forEach((segment, index) => {
    crumbs.push({
      id: segment.id,
      name: segment.name,
      clickable: index < segments.length - 1,
    });
  });

  if (crumbs.length === 1 && crumbs[0]) {
    crumbs[0].clickable = false;
  }

  return crumbs;
});

const departmentDetailQuery = useDepartmentDetailQuery(
  computed(() => selectedDepartmentId.value ?? null),
);
const isDepartmentLoading = computed(
  () => departmentDetailQuery.isLoading.value || departmentDetailQuery.isFetching.value,
);

const selectedDepartment = computed(() => {
  if (!selectedDepartmentId.value) {
    return null;
  }

  const node = findDepartment(chart.value, selectedDepartmentId.value);
  if (!node) {
    return null;
  }

  const detail = departmentDetailQuery.data.value;
  const employees = detail?.employees ?? [];
  const employeeCount = detail?.employeeCount ?? node.employeeCount ?? employees.length;

  return {
    departmentId: node.departmentId,
    departmentName: node.departmentName,
    departmentCode: node.departmentCode,
    departmentType: node.departmentType,
    departmentLeader: detail?.departmentLeader ?? node.departmentLeader ?? null,
    employees,
    employeeCount,
    childDepartmentCount: node.children.length,
  } satisfies DepartmentSummary;
});

watch(
  () => chartQuery.error.value,
  (error) => {
    if (!error) {
      return;
    }
    const message = error instanceof Error ? error.message : '부서 정보를 불러오지 못했습니다.';
    toast.error('부서 정보를 불러오지 못했습니다.', {
      description: message,
    });
  },
);

watch(chart, (nodes) => {
  if (!nodes.length) {
    selectedDepartmentId.value = undefined;
    return;
  }

  if (selectedDepartmentId.value) {
    const exists = findDepartment(nodes, selectedDepartmentId.value);
    if (!exists) {
      selectedDepartmentId.value = undefined;
    }
  }

  ensureSelectedDepartmentExists();
});

function handleTreeSelection(departmentId: number, pane?: FeatureSplitPaneContext) {
  if (!departmentId) {
    return;
  }

  router.push({ name: 'department', params: { departmentId } });

  if (pane && !pane.isLargeScreen.value) {
    pane.closeSidebar();
  }
}

function ensureSelectedDepartmentExists() {
  if (!selectedDepartmentId.value) {
    if (chart.value[0]?.departmentId) {
      router.replace({ name: 'department', params: { departmentId: chart.value[0].departmentId } });
    }
    return;
  }

  const exists = findDepartment(chart.value, selectedDepartmentId.value);
  if (!exists && chart.value[0]?.departmentId) {
    router.replace({ name: 'department', params: { departmentId: chart.value[0].departmentId } });
  }
}

function handleBreadcrumbSelect(departmentId: number) {
  if (!departmentId || departmentId === selectedDepartmentId.value) {
    return;
  }
  router.push({ name: 'department', params: { departmentId } });
}

function findDepartment(
  nodes: DepartmentChartNode[],
  targetId: number,
): DepartmentChartNode | null {
  for (const node of nodes) {
    if (node.departmentId === targetId) {
      return node;
    }
    const child = findDepartment(node.children ?? [], targetId);
    if (child) {
      return child;
    }
  }
  return null;
}

function buildDepartmentPath(
  nodes: DepartmentChartNode[],
  targetId?: number,
  currentPath: DepartmentChartNode[] = [],
): DepartmentChartNode[] {
  if (!targetId) {
    return [];
  }

  for (const node of nodes) {
    const nextPath = [...currentPath, node];
    if (node.departmentId === targetId) {
      return nextPath;
    }

    if (node.children?.length) {
      const childResult = buildDepartmentPath(node.children, targetId, nextPath);
      if (childResult.length) {
        return childResult;
      }
    }
  }

  return [];
}

function handleRefresh() {
  void queryClient.invalidateQueries({ queryKey: departmentKeys.organizationChart() });
  if (selectedDepartmentId.value) {
    void queryClient.invalidateQueries({
      queryKey: departmentKeys.detail(selectedDepartmentId.value),
    });
    void queryClient.invalidateQueries({
      queryKey: departmentKeys.employeesRoot(selectedDepartmentId.value),
    });
  }
}
</script>
