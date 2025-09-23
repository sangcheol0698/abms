<template>
  <section class="flex h-full flex-col gap-4">
    <header class="flex flex-col gap-1">
      <h1 class="text-2xl font-semibold tracking-tight">조직도</h1>
      <p class="text-sm text-muted-foreground">
        부서 구조와 리더, 구성원 정보를 확인할 수 있습니다.
      </p>
    </header>

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

    <div v-else class="grid gap-6 lg:grid-cols-[minmax(0,3fr)_minmax(0,2fr)]">
      <div class="rounded-xl border border-border/60 bg-card p-4 shadow-sm overflow-hidden">
        <OrganizationGojsDiagram :nodes="chart" v-model:selectedNodeId="selectedDepartmentId" />
      </div>

      <div
        class="rounded-xl border border-border/60 bg-card/90 p-4 shadow-sm"
        :class="{ 'max-h-[720px] overflow-y-auto': layoutBreakpoint === 'desktop' }"
      >
        <OrganizationDetailPanel :department="selectedDepartment" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { appContainer } from '@/core/di/container';
import OrganizationRepository from '@/features/organization/repository/OrganizationRepository';
import type { OrganizationChartWithEmployeesNode } from '@/features/organization/models/organization';
import OrganizationGojsDiagram from '@/features/organization/components/OrganizationGojsDiagram.vue';
import OrganizationDetailPanel from '@/features/organization/components/OrganizationDetailPanel.vue';
import HttpError from '@/core/http/HttpError';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

const repository = appContainer.resolve(OrganizationRepository);
const chart = ref<OrganizationChartWithEmployeesNode[]>([]);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const selectedDepartmentId = ref<string | undefined>();
const layoutBreakpoint = ref<'desktop' | 'mobile'>('desktop');

const selectedDepartment = computed(() => {
  if (!selectedDepartmentId.value) {
    return null;
  }
  return findDepartment(chart.value, selectedDepartmentId.value) ?? null;
});

async function loadOrganizationChart() {
  isLoading.value = true;
  errorMessage.value = null;

  try {
    chart.value = await repository.fetchOrganizationChartWithEmployees();
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

onMounted(() => {
  void loadOrganizationChart();
  updateBreakpoint();
  window.addEventListener('resize', updateBreakpoint);
});

watch(chart, (nodes) => {
  if (!nodes.length) {
    selectedDepartmentId.value = undefined;
  }
});

function findDepartment(
  nodes: OrganizationChartWithEmployeesNode[],
  targetId: string,
): OrganizationChartWithEmployeesNode | null {
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

function updateBreakpoint() {
  layoutBreakpoint.value = window.innerWidth >= 1024 ? 'desktop' : 'mobile';
}

onUnmounted(() => {
  window.removeEventListener('resize', updateBreakpoint);
});
</script>
