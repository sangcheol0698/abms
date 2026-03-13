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
          <DropdownMenu v-if="canManageCurrentEmployee || canOpenOwnProfileEdit">
            <DropdownMenuTrigger as-child>
              <Button variant="outline" size="sm">
                <MoreHorizontal class="mr-2 h-4 w-4" />
                더보기
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" class="w-44">
              <DropdownMenuItem v-if="canManageCurrentEmployee" @click="openPromotionDialog">
                <TrendingUp class="mr-2 h-4 w-4" />
                승진
              </DropdownMenuItem>
              <DropdownMenuItem v-if="canManageCurrentEmployee" @click="openEditDialog">
                <Pencil class="mr-2 h-4 w-4" />
                직원 편집
              </DropdownMenuItem>
              <DropdownMenuItem v-else-if="canOpenOwnProfileEdit" @click="openOwnProfileDialog">
                <Pencil class="mr-2 h-4 w-4" />
                내 정보 수정
              </DropdownMenuItem>
              <DropdownMenuSeparator v-if="canManageCurrentEmployee" />
              <DropdownMenuItem v-if="canManageCurrentEmployee" class="text-destructive" @click="isDeleteDialogOpen = true">
                <Trash2 class="mr-2 h-4 w-4" />
                직원 삭제
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
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
              :show-management-actions="canManageCurrentEmployee"
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

      <AlertDialog :open="isDeleteDialogOpen" @update:open="isDeleteDialogOpen = $event">
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

    <EmployeePromotionDialog
      :open="isPromotionDialogOpen"
      :employee="employee"
      :grade-options="gradeOptions"
      :position-options="positionOptions"
      @update:open="isPromotionDialogOpen = $event"
      @promoted="handleEmployeePromoted"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
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
} from '@/components/ui/alert-dialog';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { MoreHorizontal, Pencil, Trash2, TrendingUp } from 'lucide-vue-next';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import {
  getEmployeeGradeOptions,
  getEmployeePositionOptions,
  getEmployeeTypeOptions,
} from '@/features/employee/models/employeeFilters';
import HttpError from '@/core/http/HttpError';
import EmployeeDetailHeader from '@/features/employee/components/EmployeeDetailHeader.vue';
import EmployeeOverviewPanel from '@/features/employee/components/EmployeeOverviewPanel.vue';
import EmployeeEmploymentPanel from '@/features/employee/components/EmployeeEmploymentPanel.vue';
import EmployeeSalaryPanel from '@/features/employee/components/EmployeeSalaryPanel.vue';
import EmployeeProjectsPanel from '@/features/employee/components/EmployeeProjectsPanel.vue';
import EmployeeUpdateDialog from '@/features/employee/components/EmployeeUpdateDialog.vue';
import EmployeePromotionDialog from '@/features/employee/components/EmployeePromotionDialog.vue';
import type { DepartmentChartNode } from '@/features/department/models/department';
import { useDepartmentOrganizationChartQuery } from '@/features/department/queries/useDepartmentQueries';
import {
  useActivateEmployeeMutation,
  useDeleteEmployeeMutation,
  useEmployeeDetailQuery,
  useEmployeeGradesQuery,
  useEmployeePositionsQuery,
  useEmployeeTypesQuery,
  useResignEmployeeMutation,
  useTakeLeaveEmployeeMutation,
} from '@/features/employee/queries/useEmployeeQueries';
import { employeeKeys, queryClient } from '@/core/query';
import { canEditOwnProfile, canManageEmployee } from '@/features/employee/permissions';
import { dispatchOpenProfileDialogEvent } from '@/features/auth/profileDialogEvents';

const route = useRoute();
const router = useRouter();
const employeeId = computed(() => {
  const raw = route.params.employeeId;
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : 0;
});

const employeeQuery = useEmployeeDetailQuery(employeeId);
const departmentChartQuery = useDepartmentOrganizationChartQuery();
const employeeTypesQuery = useEmployeeTypesQuery();
const employeeGradesQuery = useEmployeeGradesQuery();
const employeePositionsQuery = useEmployeePositionsQuery();
const resignMutation = useResignEmployeeMutation();
const takeLeaveMutation = useTakeLeaveEmployeeMutation();
const activateMutation = useActivateEmployeeMutation();
const deleteMutation = useDeleteEmployeeMutation();

const employee = computed(() => employeeQuery.data.value ?? null);
const isLoading = computed(() => employeeQuery.isLoading.value);
const errorMessage = computed(() => {
  const error = employeeQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof Error ? error.message : '직원 정보를 불러오는 중 오류가 발생했습니다.';
});

const isResigning = computed(() => resignMutation.isPending.value);
const resignError = ref<string | null>(null);
const resignSuccess = ref<string | null>(null);
const isTakingLeave = computed(() => takeLeaveMutation.isPending.value);
const takeLeaveError = ref<string | null>(null);
const takeLeaveSuccess = ref<string | null>(null);
const isActivating = computed(() => activateMutation.isPending.value);
const activateError = ref<string | null>(null);
const activateSuccess = ref<string | null>(null);
const isDeleting = computed(() => deleteMutation.isPending.value);

const employeeInitials = computed(() => {
  const name = employee.value?.name ?? '';
  return name.trim().slice(0, 2).toUpperCase() || '??';
});
const employeePermissionContext = computed(() => ({
  departmentChart: departmentChartQuery.data.value ?? [],
}));
const canManageCurrentEmployee = computed(() => {
  if (!employee.value) {
    return false;
  }

  return canManageEmployee(employee.value, employeePermissionContext.value);
});
const canOpenOwnProfileEdit = computed(() => {
  if (!employee.value) {
    return false;
  }
  return !canManageCurrentEmployee.value && canEditOwnProfile(employee.value);
});

const isEmployeeUpdateDialogOpen = ref(false);
const isPromotionDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);

const departmentOptions = computed<{ label: string; value: number }[]>(() => {
  const chart = departmentChartQuery.data.value ?? [];
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
  return Array.from(map.entries()).map(([id, name]) => ({ label: name, value: id }));
});

const typeOptions = computed<EmployeeFilterOption[]>(
  () => employeeTypesQuery.data.value ?? getEmployeeTypeOptions(),
);
const gradeOptions = computed<EmployeeFilterOption[]>(
  () => employeeGradesQuery.data.value ?? getEmployeeGradeOptions(),
);
const positionOptions = computed<EmployeeFilterOption[]>(
  () => employeePositionsQuery.data.value ?? getEmployeePositionOptions(),
);

watch(employeeId, () => {
  resignError.value = null;
  resignSuccess.value = null;
  takeLeaveError.value = null;
  takeLeaveSuccess.value = null;
  activateError.value = null;
  activateSuccess.value = null;
});

function resolveErrorMessage(error: unknown, fallback: string) {
  return error instanceof HttpError ? error.message : fallback;
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

  resignError.value = null;
  resignSuccess.value = null;
  takeLeaveSuccess.value = null;
  activateSuccess.value = null;

  try {
    await resignMutation.mutateAsync({ employeeId: employee.value.employeeId, resignationDate });
    await employeeQuery.refetch();
    resignSuccess.value = '퇴사 처리가 완료되었습니다.';
  } catch (error) {
    const message = resolveErrorMessage(error, '퇴사 처리 중 오류가 발생했습니다.');
    resignError.value = message;
  }
}

async function handleTakeLeave() {
  if (!employee.value?.employeeId) {
    return;
  }

  takeLeaveError.value = null;
  takeLeaveSuccess.value = null;
  resignSuccess.value = null;
  activateSuccess.value = null;

  try {
    await takeLeaveMutation.mutateAsync(employee.value.employeeId);
    await employeeQuery.refetch();
    takeLeaveSuccess.value = '휴직 처리가 완료되었습니다.';
  } catch (error) {
    const message = resolveErrorMessage(error, '휴직 처리 중 오류가 발생했습니다.');
    takeLeaveError.value = message;
  }
}

async function handleActivate() {
  if (!employee.value?.employeeId) {
    return;
  }

  activateError.value = null;
  activateSuccess.value = null;
  resignSuccess.value = null;
  takeLeaveSuccess.value = null;

  try {
    await activateMutation.mutateAsync(employee.value.employeeId);
    await employeeQuery.refetch();
    activateSuccess.value = '재직 처리가 완료되었습니다.';
  } catch (error) {
    const message = resolveErrorMessage(error, '재직 처리 중 오류가 발생했습니다.');
    activateError.value = message;
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

function openEditDialog() {
  isEmployeeUpdateDialogOpen.value = true;
}

function openPromotionDialog() {
  isPromotionDialogOpen.value = true;
}

function openOwnProfileDialog() {
  dispatchOpenProfileDialogEvent({ openSelfProfileEditor: true });
}

async function handleEmployeeUpdated() {
  isEmployeeUpdateDialogOpen.value = false;
  await queryClient.invalidateQueries({ queryKey: employeeKeys.all });
  await employeeQuery.refetch();
}

async function handleEmployeePromoted() {
  isPromotionDialogOpen.value = false;
  await queryClient.invalidateQueries({ queryKey: employeeKeys.all });
  await employeeQuery.refetch();
}

async function handleDelete() {
  if (!employee.value?.employeeId) return;

  try {
    await deleteMutation.mutateAsync(employee.value.employeeId);
    router.push('/employees');
  } catch {
    // Error state is surfaced by query/mutation error handling and toast in caller scope.
  }
}
</script>
