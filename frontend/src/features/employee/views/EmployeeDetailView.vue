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
      />

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
            <EmployeeProjectsPanel :employee-id="employee?.employeeId ?? ''" />
          </TabsContent>
        </div>
      </Tabs>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import HttpError from '@/core/http/HttpError';
import EmployeeDetailHeader from '@/features/employee/components/EmployeeDetailHeader.vue';
import EmployeeOverviewPanel from '@/features/employee/components/EmployeeOverviewPanel.vue';
import EmployeeEmploymentPanel from '@/features/employee/components/EmployeeEmploymentPanel.vue';
import EmployeeSalaryPanel from '@/features/employee/components/EmployeeSalaryPanel.vue';
import EmployeeProjectsPanel from '@/features/employee/components/EmployeeProjectsPanel.vue';

const route = useRoute();
const router = useRouter();
const repository = appContainer.resolve(EmployeeRepository);

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
const employeeInitials = computed(() => {
  const name = employee.value?.name ?? '';
  return name.trim().slice(0, 2).toUpperCase() || '??';
});

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
      fetchEmployee(next);
    }
  },
  { immediate: true },
);

function resolveErrorMessage(error: unknown, fallback: string) {
  return error instanceof HttpError ? error.message : fallback;
}

async function fetchEmployee(employeeId: string, options: { showLoading?: boolean } = {}) {
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
  if (!employee.value?.departmentId) {
    return;
  }
  router
    .push({ name: 'organization', query: { departmentId: employee.value.departmentId } })
    .catch(() => {});
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
</script>
