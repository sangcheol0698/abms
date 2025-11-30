<template>
  <section class="flex h-full min-h-0 flex-1 flex-col gap-6 overflow-hidden">
    <div v-if="isLoading"
      class="flex min-h-[10rem] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/20 p-6 text-sm text-muted-foreground"
      role="status" aria-live="polite">
      조직도를 불러오는 중입니다...
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>조직도를 불러오지 못했습니다</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <FeatureSplitLayout v-else :sidebar-default-size="20" :sidebar-min-size="14" :sidebar-max-size="32"
      :content-min-size="52">
      <template #sidebar="{ pane }">
        <div class="flex h-full min-h-0 flex-col shadow-sm">
          <div class="flex items-center justify-between px-4 py-3 text-sm">
            <span class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">조직도</span>
          </div>
          <OrganizationTree class="flex-1" :nodes="chart" v-model:selectedNodeId="selectedDepartmentId"
            @update:selectedNodeId="handleTreeSelection($event, pane)" />
        </div>
      </template>

      <template #default="{ pane }">
        <div class="flex h-full min-h-0 flex-col">
          <header class="flex flex-col px-4 py-4">
            <div class="flex items-center">
              <Button variant="ghost" size="icon"
                class="-ml-1 h-8 w-8 text-muted-foreground transition hover:text-foreground" aria-label="조직도 사이드바 토글"
                @click="pane.toggleSidebar()">
                <PanelLeft class="h-4 w-4 transition" :class="pane.isSidebarCollapsed.value ? 'rotate-180' : ''" />
              </Button>
              <Breadcrumb class="flex flex-wrap text-sm text-muted-foreground">
                <BreadcrumbList>
                  <template v-for="(segment, index) in headerBreadcrumbs" :key="segment.id">
                    <BreadcrumbItem>
                      <template v-if="segment.clickable">
                        <BreadcrumbLink as-child>
                          <router-link v-if="segment.to" :to="segment.to" class="transition hover:text-foreground">
                            {{ segment.name }}
                          </router-link>
                          <button v-else type="button" class="transition hover:text-foreground"
                            @click="handleBreadcrumbSelect(segment.id)">
                            {{ segment.name }}
                          </button>
                        </BreadcrumbLink>
                      </template>
                      <BreadcrumbPage v-else>{{ segment.name }}</BreadcrumbPage>
                    </BreadcrumbItem>
                    <BreadcrumbSeparator v-if="index < headerBreadcrumbs.length - 1" />
                  </template>
                </BreadcrumbList>
              </Breadcrumb>
            </div>
          </header>

          <div class="flex h-full min-h-0 flex-col overflow-hidden">
            <div class="flex-1 min-h-0 overflow-y-auto pl-4">
              <OrganizationDetailPanel :department="selectedDepartment" :isLoading="isDepartmentLoading" />
            </div>
          </div>
        </div>
      </template>
    </FeatureSplitLayout>


  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { LocationQueryRaw } from 'vue-router';
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import type { FeatureSplitPaneContext } from '@/core/composables/useFeatureSplitPane';
import { appContainer } from '@/core/di/container';
import OrganizationRepository from '@/features/organization/repository/OrganizationRepository';
import type {
  OrganizationChartNode,
  OrganizationDepartmentDetail,
  OrganizationDepartmentSummary,
} from '@/features/organization/models/organization';
import OrganizationTree from '@/features/organization/components/OrganizationTree.vue';
import OrganizationDetailPanel from '@/features/organization/components/OrganizationDetailPanel.vue';
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
import { PanelLeft } from 'lucide-vue-next';

const repository = appContainer.resolve(OrganizationRepository);
const chart = ref<OrganizationChartNode[]>([]);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const selectedDepartmentId = ref<string | undefined>();
const departmentDetail = ref<OrganizationDepartmentDetail | null>(null);
const isDepartmentLoading = ref(false);

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
const route = useRoute();
const router = useRouter();
let isUpdatingRoute = false;
let isApplyingRoute = false;

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
      errorMessage.value = '조직도 정보를 불러오지 못했습니다.';
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
  const initialDepartmentId = extractDepartmentId(route.query.departmentId);
  if (initialDepartmentId) {
    selectedDepartmentId.value = initialDepartmentId;
  }
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
    if (!isApplyingRoute) {
      updateRouteQuery(next);
    }
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

watch(
  () => route.query.departmentId,
  (value) => {
    if (isUpdatingRoute) {
      return;
    }

    const nextId = extractDepartmentId(value);

    if (nextId === selectedDepartmentId.value) {
      return;
    }

    isApplyingRoute = true;
    selectedDepartmentId.value = nextId;
    isApplyingRoute = false;
  },
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

function updateRouteQuery(departmentId?: string) {
  const existing = extractDepartmentId(route.query.departmentId);

  if (departmentId === existing) {
    return;
  }

  const nextQuery: LocationQueryRaw = {};

  Object.entries(route.query).forEach(([key, value]) => {
    if (key === 'departmentId') {
      return;
    }
    if (Array.isArray(value)) {
      const filtered = value.filter((item): item is string => typeof item === 'string');
      if (filtered.length > 0) {
        nextQuery[key] = filtered;
      }
      return;
    }
    if (typeof value === 'string' && value.length > 0) {
      nextQuery[key] = value;
    }
  });

  if (departmentId) {
    nextQuery.departmentId = departmentId;
  }

  isUpdatingRoute = true;
  router
    .replace({ query: nextQuery })
    .finally(() => {
      isUpdatingRoute = false;
    })
    .catch((error) => {
      console.warn('부서 선택 정보를 URL에 반영하지 못했습니다.', error);
    });
}

function extractDepartmentId(raw: unknown): string | undefined {
  if (Array.isArray(raw)) {
    const first = raw.find((value) => typeof value === 'string' && value.trim().length > 0);
    return first ? first.trim() : undefined;
  }
  if (typeof raw === 'string') {
    const trimmed = raw.trim();
    return trimmed.length > 0 ? trimmed : undefined;
  }
  return undefined;
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
</script>
