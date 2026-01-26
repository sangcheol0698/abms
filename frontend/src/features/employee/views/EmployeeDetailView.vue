<template>
  <section class="flex h-full flex-col gap-6">
    <div
      v-if="isLoading"
      class="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10"
    >
      <span class="text-sm text-muted-foreground">직원 정보를 불러오는 중입니다...</span>
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>직원 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <template v-else>
      <EmployeeDetailHeader
        :employee="employee"
        :employee-initials="employeeInitials"
        @department-click="goToDepartment"
      >
        <template #actions>
          <AlertDialog>
            <AlertDialogTrigger as-child>
              <Button variant="outline" size="sm" class="text-destructive hover:text-destructive">
                <Trash2 class="mr-2 h-4 w-4" />
                삭제
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>직원을 삭제하시겠습니까?</AlertDialogTitle>
                <AlertDialogDescription>
                  {{ employee?.name }} 직원 정보가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>취소</AlertDialogCancel>
                <AlertDialogAction
                  class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  :disabled="isDeleting"
                  @click="handleDelete"
                >
                  {{ isDeleting ? '삭제 중...' : '삭제' }}
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
          <Button variant="outline" size="sm" @click="openEditDialog">
            <Pencil class="mr-2 h-4 w-4" />
            직원 편집
          </Button>
        </template>
      </EmployeeDetailHeader>

      <Tabs default-value="overview" class="flex min-h-[320px] flex-1 flex-col gap-4">
        <TabsList class="flex-wrap">
          <TabsTrigger value="overview">개요</TabsTrigger>
          <TabsTrigger value="employment">근무 정보</TabsTrigger>
          <TabsTrigger value="salary">연봉</TabsTrigger>
          <TabsTrigger value="projects">프로젝트</TabsTrigger>
        </TabsList>

        <div class="flex flex-1 flex-col gap-4">
          <TabsContent value="overview" class="flex-1">
            <EmployeeOverviewPanel
              :employee="employee"
              :format-date="formatDate"
              @department-click="goToDepartment"
            />
          </TabsContent>
          <TabsContent value="employment" class="flex-1">
            <EmployeeEmploymentPanel
              :employee="employee"
              :format-date="formatDate"
              :is-resigning="isResigning"
              :resign-error="resignError"
              :resign-success="resignSuccess"
              :is-taking-leave="isTakingLeave"
              :take-leave-error="takeLeaveError"
              :take-leave-success="takeLeaveSuccess"
              :is-activating="isActivating"
              :activate-error="activateError"
              :activate-success="activateSuccess"
              @resign="handleResign"
              @take-leave="handleTakeLeave"
              @activate="handleActivate"
            />
          </TabsContent>
          <TabsContent value="salary" class="flex-1">
            <EmployeeSalaryPanel :employee="employee" />
          </TabsContent>
          <TabsContent value="projects" class="flex-1">
            <EmployeeProjectsPanel :employee-id="employee?.employeeId ?? 0" />
          </TabsContent>
        </div>
      </Tabs>
    </template>

    <EmployeeUpdateDialog
      :open="isEmployeeUpdateDialogOpen"
      :department-options="departmentOptions"
      :grade-options="gradeOptions"
      :position-options="positionOptions"
      :type-options="typeOptions"
      :employee="employee ?? undefined"
      @update:open="isEmployeeUpdateDialogOpen = $event"
      @updated="handleEmployeeUpdated"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { Pencil, Trash2 } from 'lucide-vue-next';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import DepartmentRepository from '@/features/department/repository/DepartmentRepository';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import type { DepartmentChartNode } from '@/features/department/models/department';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';

import HttpError from '@/core/http/HttpError';
import EmployeeDetailHeader from '@/features/employee/components/EmployeeDetailHeader.vue';
import EmployeeOverviewPanel from '@/features/employee/components/EmployeeOverviewPanel.vue';
import EmployeeEmploymentPanel from '@/features/employee/components/EmployeeEmploymentPanel.vue';
import EmployeeSalaryPanel from '@/features/employee/components/EmployeeSalaryPanel.vue';
import EmployeeProjectsPanel from '@/features/employee/components/EmployeeProjectsPanel.vue';
import EmployeeUpdateDialog from '@/features/employee/components/EmployeeUpdateDialog.vue';

const route = useRoute();
const router = useRouter();

const repository = appContainer.resolve(EmployeeRepository);
const departmentRepository = appContainer.resolve(DepartmentRepository);

const employee = ref<EmployeeSummary | null>(null);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const isResigning = ref(false);
const resignError = ref<string | null>(null);
const resignSuccess = ref<string | null>(null);
const isTakingLeave = ref(false);
const takeLeaveError = ref<string | null>(null);
const takeLeaveSuccess = ref<string | null>(null);
const isActivating = ref(false);
const activateError = ref<string | null>(null);
const activateSuccess = ref<string | null>(null);
const isDeleting = ref(false);
const employeeInitials = computed(() => {
  const name = employee.value?.name ?? '';
  return name.trim().slice(0, 2).toUpperCase() || '??';
});

const isEmployeeUpdateDialogOpen = ref(false);
const departmentOptions = ref<{ label: string; value: number }[]>([]);
const statusOptions = ref<EmployeeFilterOption[]>([]);
const typeOptions = ref<EmployeeFilterOption[]>([]);
const gradeOptions = ref<EmployeeFilterOption[]>([]);
const positionOptions = ref<EmployeeFilterOption[]>([]);

watch(
  () => route.params.employeeId,
  (next) => {
    if (typeof next === 'string' && next.trim().length > 0) {
      resignError.value = null;
      resignSuccess.value = null;
      takeLeaveError.value = null;
      takeLeaveSuccess.value = null;
      activateError.value = null;
      activateSuccess.value = null;
      isResigning.value = false;
      isTakingLeave.value = false;
      isActivating.value = false;
      const employeeId = Number(next);
      if (!isNaN(employeeId)) {
        fetchEmployee(employeeId);
      }
    }
  },
  { immediate: true },
);

function resolveErrorMessage(error: unknown, fallback: string) {
  return error instanceof HttpError ? error.message : fallback;
}

async function fetchEmployee(employeeId: number, options: { showLoading?: boolean } = {}) {
  const { showLoading = true } = options;

  if (showLoading) {
    isLoading.value = true;
    errorMessage.value = null;
    employee.value = null;
  }

  try {
    const result = await repository.findById(employeeId);
    employee.value = result;
    return result;
  } catch (error) {
    const message = resolveErrorMessage(error, '직원 정보를 불러오는 중 오류가 발생했습니다.');
    if (showLoading) {
      errorMessage.value = message;
      employee.value = null;
      return null;
    }
    throw error instanceof Error ? error : new Error(message);
  } finally {
    if (showLoading) {
      isLoading.value = false;
    }
  }
}

function goToDepartment() {
  const id = employee.value?.departmentId;
  if (!id) return;

  router.push(`/departments/${id}`).catch(() => {});
}

async function handleResign(resignationDate: string) {
  if (!employee.value?.employeeId) {
    return;
  }

  isResigning.value = true;
  resignError.value = null;
  resignSuccess.value = null;
  takeLeaveSuccess.value = null;
  activateSuccess.value = null;

  try {
    await repository.resign(employee.value.employeeId, resignationDate);
    await fetchEmployee(employee.value.employeeId, { showLoading: false });
    resignSuccess.value = '퇴사 처리가 완료되었습니다.';
  } catch (error) {
    const message = resolveErrorMessage(error, '퇴사 처리 중 오류가 발생했습니다.');
    resignError.value = message;
  } finally {
    isResigning.value = false;
  }
}

async function handleTakeLeave() {
  if (!employee.value?.employeeId) {
    return;
  }

  isTakingLeave.value = true;
  takeLeaveError.value = null;
  takeLeaveSuccess.value = null;
  resignSuccess.value = null;
  activateSuccess.value = null;

  try {
    await repository.takeLeave(employee.value.employeeId);
    await fetchEmployee(employee.value.employeeId, { showLoading: false });
    takeLeaveSuccess.value = '휴직 처리가 완료되었습니다.';
  } catch (error) {
    const message = resolveErrorMessage(error, '휴직 처리 중 오류가 발생했습니다.');
    takeLeaveError.value = message;
  } finally {
    isTakingLeave.value = false;
  }
}

async function handleActivate() {
  if (!employee.value?.employeeId) {
    return;
  }

  isActivating.value = true;
  activateError.value = null;
  activateSuccess.value = null;
  resignSuccess.value = null;
  takeLeaveSuccess.value = null;

  try {
    await repository.activate(employee.value.employeeId);
    await fetchEmployee(employee.value.employeeId, { showLoading: false });
    activateSuccess.value = '재직 처리가 완료되었습니다.';
  } catch (error) {
    const message = resolveErrorMessage(error, '재직 처리 중 오류가 발생했습니다.');
    activateError.value = message;
  } finally {
    isActivating.value = false;
  }
}

function formatDate(value?: string | null) {
  if (!value) {
    return '—';
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleDateString();
}

async function loadOptions() {
  try {
    const [chart, statuses, types, grades, positions] = await Promise.all([
      departmentRepository.fetchOrganizationChart(),
      repository.fetchStatuses(),
      repository.fetchTypes(),
      repository.fetchGrades(),
      repository.fetchPositions(),
    ]);

    const map = new Map<number, string>();
    const traverse = (nodes: DepartmentChartNode[]) => {
      nodes.forEach((node) => {
        if (!map.has(node.departmentId)) {
          map.set(node.departmentId, node.departmentName);
        }
        if (Array.isArray(node.children) && node.children.length > 0) {
          traverse(node.children);
        }
      });
    };
    traverse(chart);
    departmentOptions.value = Array.from(map.entries()).map(([id, name]) => ({
      label: name,
      value: id,
    }));

    statusOptions.value = statuses;
    typeOptions.value = types;
    gradeOptions.value = grades;
    positionOptions.value = positions;
  } catch (error) {
    console.error('옵션 정보를 불러오지 못했습니다.', error);
  }
}

function openEditDialog() {
  isEmployeeUpdateDialogOpen.value = true;
}

function handleEmployeeUpdated() {
  isEmployeeUpdateDialogOpen.value = false;
  if (employee.value?.employeeId) {
    fetchEmployee(employee.value.employeeId, { showLoading: false });
  }
}

async function handleDelete() {
  if (!employee.value?.employeeId) return;
  
  isDeleting.value = true;
  try {
    await repository.delete(employee.value.employeeId);
    router.push('/employees');
  } catch (error) {
    const message = resolveErrorMessage(error, '직원 삭제 중 오류가 발생했습니다.');
    errorMessage.value = message;
  } finally {
    isDeleting.value = false;
  }
}

onMounted(() => {
  loadOptions();
});
</script>
