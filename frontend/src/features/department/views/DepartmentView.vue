<template>
  <section class="flex h-full min-h-0 flex-1 flex-col gap-6 overflow-hidden">
    <div
      v-if="isLoading"
      class="flex min-h-[10rem] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/20 p-6 text-sm text-muted-foreground"
      role="status"
      aria-live="polite"
    >
      부서 정보를 불러오는 중입니다...
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>부서 정보를 불러오지 못했습니다</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <FeatureSplitLayout
      v-else
      :sidebar-default-size="20"
      :sidebar-min-size="14"
      :sidebar-max-size="32"
      :content-min-size="52"
    >
      <template #sidebar="{ pane }">
        <div class="flex h-full min-h-0 flex-col shadow-sm">
          <OrganizationTree
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
              <OrganizationDetailPanel
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
import { computed, onMounted, ref, watch } from 'vue';
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import type { FeatureSplitPaneContext } from '@/core/composables/useFeatureSplitPane';
import { useQuerySync } from '@/core/composables/useQuerySync';
import { appContainer } from '@/core/di/container';
import OrganizationRepository from '@/features/department/repository/OrganizationRepository';
import type {
  OrganizationChartNode,
  OrganizationDepartmentDetail,
  OrganizationDepartmentSummary,
} from '@/features/department/models/organization';
import OrganizationTree from '@/features/department/components/OrganizationTree.vue';
import OrganizationDetailPanel from '@/features/department/components/OrganizationDetailPanel.vue';
import HttpError from '@/core/http/HttpError';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
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

const repository = appContainer.resolve(OrganizationRepository);
const chart = ref<OrganizationChartNode[]>([]);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const selectedDepartmentId = ref<string | undefined>();
const departmentDetail = ref<OrganizationDepartmentDetail | null>(null);
const isDepartmentLoading = ref(false);

// URL 쿼리와 부서 ID 동기화
useQuerySync({
  state: selectedDepartmentId,
  queryKey: 'departmentId',
  serialize: (value) => value,
  deserialize: (value) => {
    if (Array.isArray(value)) {
      const first = value.find((v) => typeof v === 'string' && v.trim().length > 0);
      return first ? first.trim() : undefined;
    }
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
    return undefined;
  },
});

const selectedBreadcrumb = computed(() =>
  buildDepartmentPath(chart.value, selectedDepartmentId.value).map((node) => ({
    id: node.departmentId,
    name: node.departmentName,
  })),
);

interface HeaderBreadcrumb {
  id: string;
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

let detailRequestToken = 0;

const selectedDepartment = computed(() => {
  if (!selectedDepartmentId.value) {
    return null;
  }
  const node = findDepartment(chart.value, selectedDepartmentId.value);
  if (!node) {
    return null;
  }

  const detail = departmentDetail.value;

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
  } satisfies OrganizationDepartmentSummary;
});

async function loadOrganizationChart() {
  isLoading.value = true;
  errorMessage.value = null;

  try {
    chart.value = await repository.fetchOrganizationChart();
    ensureSelectedDepartmentExists();
  } catch (error) {
    if (error instanceof HttpError) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = '부서 정보를 불러오지 못했습니다.';
    }
  } finally {
    isLoading.value = false;
  }
}

async function loadDepartmentDetail(departmentId: string) {
  const token = ++detailRequestToken;
  isDepartmentLoading.value = true;

  try {
    const detail = await repository.fetchDepartmentDetail(departmentId);

    if (token !== detailRequestToken) {
      return;
    }

    departmentDetail.value = detail;
  } catch (error) {
    if (token !== detailRequestToken) {
      return;
    }

    departmentDetail.value = null;
    if (error instanceof HttpError) {
      console.warn('부서 상세 정보를 불러오지 못했습니다.', error.message);
    } else {
      console.warn('부서 상세 정보를 불러오는 중 오류가 발생했습니다.', error);
    }
  } finally {
    if (token === detailRequestToken) {
      isDepartmentLoading.value = false;
    }
  }
}

onMounted(() => {
  void loadOrganizationChart();
});

watch(chart, (nodes) => {
  if (!nodes.length) {
    selectedDepartmentId.value = undefined;
    departmentDetail.value = null;
    return;
  }

  if (selectedDepartmentId.value) {
    const exists = findDepartment(nodes, selectedDepartmentId.value);
    if (exists) {
      return;
    }
  }

  selectedDepartmentId.value = nodes[0]?.departmentId;
});

watch(
  selectedDepartmentId,
  (next, previous) => {
    if (!next) {
      departmentDetail.value = null;
      return;
    }

    if (next === previous && departmentDetail.value) {
      return;
    }

    const node = findDepartment(chart.value, next);
    if (!node) {
      departmentDetail.value = null;
      return;
    }

    departmentDetail.value = null;
    void loadDepartmentDetail(next);
  },
  { immediate: false },
);

function handleTreeSelection(departmentId: string, pane?: FeatureSplitPaneContext) {
  if (!departmentId) {
    return;
  }
  selectedDepartmentId.value = departmentId;
  if (pane && !pane.isLargeScreen.value) {
    pane.closeSidebar();
  }
}

function ensureSelectedDepartmentExists() {
  if (!selectedDepartmentId.value) {
    selectedDepartmentId.value = chart.value[0]?.departmentId;
    return;
  }

  const exists = findDepartment(chart.value, selectedDepartmentId.value);
  if (!exists) {
    selectedDepartmentId.value = chart.value[0]?.departmentId;
  }
}

function handleBreadcrumbSelect(departmentId: string) {
  if (!departmentId || departmentId === selectedDepartmentId.value) {
    return;
  }
  selectedDepartmentId.value = departmentId;
}

function findDepartment(
  nodes: OrganizationChartNode[],
  targetId: string,
): OrganizationChartNode | null {
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
  nodes: OrganizationChartNode[],
  targetId?: string,
  currentPath: OrganizationChartNode[] = [],
): OrganizationChartNode[] {
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
  void loadOrganizationChart();
  if (selectedDepartmentId.value) {
    void loadDepartmentDetail(selectedDepartmentId.value);
  }
}
</script>
