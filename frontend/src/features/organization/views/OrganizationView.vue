<template>
  <section
    class="flex h-full min-h-0 flex-1 flex-col gap-6 overflow-hidden"
    :style="containerStyle"
  >
    <div
      v-if="isLoading"
      class="flex min-h-[10rem] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/20 p-6 text-sm text-muted-foreground"
      role="status"
      aria-live="polite"
    >
      조직도를 불러오는 중입니다...
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>조직도를 불러오지 못했습니다</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <div v-else class="flex-1 min-h-0 overflow-hidden">
      <template v-if="isLargeScreen">
        <ResizablePanelGroup direction="horizontal" class="flex h-full min-h-0 overflow-hidden">
          <ResizablePanel :default-size="20" :min-size="14" :max-size="32" :collapsed-size="14">
            <div class="flex h-full min-h-0 flex-col overflow-hidden rounded-xl bg-card shadow-sm">
              <OrganizationTree :nodes="chart" v-model:selectedNodeId="selectedDepartmentId" />
            </div>
          </ResizablePanel>

          <ResizableHandle with-handle class="bg-border/70" />

          <ResizablePanel :default-size="76" :min-size="52">
            <div class="flex h-full min-h-0 flex-col overflow-hidden rounded-xl bg-card/90 shadow-sm">
              <div
                v-if="selectedBreadcrumb.length"
                class="border-b border-border/60 bg-background/60 px-4 py-2 text-xs"
              >
                <Breadcrumb class="flex flex-wrap gap-1 text-muted-foreground">
                  <BreadcrumbList>
                    <template v-for="(segment, index) in selectedBreadcrumb" :key="segment.id">
                      <BreadcrumbItem>
                        <template v-if="index < selectedBreadcrumb.length - 1">
                          <BreadcrumbLink as-child>
                            <button
                              type="button"
                              class="transition hover:text-foreground"
                              @click="handleBreadcrumbSelect(segment.id)"
                            >
                              {{ segment.name }}
                            </button>
                          </BreadcrumbLink>
                        </template>
                        <BreadcrumbPage v-else>{{ segment.name }}</BreadcrumbPage>
                      </BreadcrumbItem>
                      <BreadcrumbSeparator v-if="index < selectedBreadcrumb.length - 1" />
                    </template>
                  </BreadcrumbList>
                </Breadcrumb>
              </div>
              <div class="flex-1 min-h-0 overflow-y-auto p-4">
                <OrganizationDetailPanel
                  :department="selectedDepartment"
                  :isLoading="isDepartmentLoading"
                />
              </div>
            </div>
          </ResizablePanel>
        </ResizablePanelGroup>
      </template>

      <template v-else>
        <div class="flex h-full min-h-0 flex-col gap-4">
          <div
            class="flex max-h-[360px] flex-col overflow-hidden rounded-xl border border-border/60 bg-card shadow-sm"
          >
            <OrganizationTree :nodes="chart" v-model:selectedNodeId="selectedDepartmentId" />
          </div>
          <div class="flex flex-1 min-h-0 flex-col overflow-hidden rounded-xl border border-border/60 bg-card/90 shadow-sm">
            <div
              v-if="selectedBreadcrumb.length"
              class="border-b border-border/60 bg-background/60 px-4 py-2 text-xs"
            >
              <Breadcrumb class="flex flex-wrap gap-1 text-muted-foreground">
                <BreadcrumbList>
                  <template v-for="(segment, index) in selectedBreadcrumb" :key="segment.id">
                    <BreadcrumbItem>
                      <template v-if="index < selectedBreadcrumb.length - 1">
                        <BreadcrumbLink as-child>
                          <button
                            type="button"
                            class="transition hover:text-foreground"
                            @click="handleBreadcrumbSelect(segment.id)"
                          >
                            {{ segment.name }}
                          </button>
                        </BreadcrumbLink>
                      </template>
                      <BreadcrumbPage v-else>{{ segment.name }}</BreadcrumbPage>
                    </BreadcrumbItem>
                    <BreadcrumbSeparator v-if="index < selectedBreadcrumb.length - 1" />
                  </template>
                </BreadcrumbList>
              </Breadcrumb>
            </div>
            <div class="flex-1 overflow-y-auto p-4">
              <OrganizationDetailPanel
                :department="selectedDepartment"
                :isLoading="isDepartmentLoading"
              />
            </div>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { LocationQueryRaw } from 'vue-router';
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
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '@/components/ui/resizable';
import { useBreakpoints, useWindowSize } from '@vueuse/core';

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

let detailRequestToken = 0;
const route = useRoute();
const router = useRouter();
let isUpdatingRoute = false;
let isApplyingRoute = false;
const breakpoints = useBreakpoints({ lg: 1024 });
const isLargeScreen = breakpoints.greater('lg');
const { height: windowHeight } = useWindowSize();

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

const containerStyle = computed(() => {
  const SAFE_MIN_HEIGHT = 320;
  const HEADER_OFFSET = 64;
  const viewportHeight = windowHeight.value || 0;
  if (viewportHeight === 0) {
    return undefined;
  }
  const available = Math.max(viewportHeight - HEADER_OFFSET, SAFE_MIN_HEIGHT);
  return {
    minHeight: `${available}px`,
    height: `${available}px`,
    maxHeight: `${available}px`,
  };
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
