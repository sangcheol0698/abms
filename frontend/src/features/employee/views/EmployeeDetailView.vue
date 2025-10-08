<template>
  <section class="flex h-full flex-col gap-6">
    <header class="flex items-center justify-between">
      <div class="space-y-1">
        <h1 class="text-2xl font-semibold tracking-tight">구성원 상세</h1>
        <p class="text-sm text-muted-foreground">
          구성원 정보를 확인하고 필요한 경우 조직도나 목록으로 이동할 수 있습니다.
        </p>
      </div>
      <div class="flex items-center gap-2">
        <Button variant="outline" @click="goToList">목록으로</Button>
        <Button
          v-if="employee?.departmentId"
          variant="secondary"
          @click="goToDepartment"
        >
          조직도에서 보기
        </Button>
      </div>
    </header>

    <div
      v-if="isLoading"
      class="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10"
    >
      <span class="text-sm text-muted-foreground">구성원 정보를 불러오는 중입니다...</span>
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>구성원 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <Card v-else class="border-border/70 shadow-sm">
      <CardHeader>
        <CardTitle class="text-xl font-semibold text-foreground">{{ employee?.name }}</CardTitle>
        <CardDescription>{{ employee?.email }}</CardDescription>
      </CardHeader>
      <CardContent class="grid gap-6 md:grid-cols-2">
        <div class="space-y-2">
          <h2 class="text-sm font-semibold text-muted-foreground">조직 정보</h2>
          <dl class="grid gap-2 text-sm">
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">부서</dt>
              <dd>{{ employee?.departmentName }}</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">직책</dt>
              <dd>{{ employee?.position }}</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">등급</dt>
              <dd>{{ employee?.grade }}</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">근무 유형</dt>
              <dd>{{ employee?.type }}</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">상태</dt>
              <dd>
                <Badge variant="outline">{{ employee?.status }}</Badge>
              </dd>
            </div>
          </dl>
        </div>

        <div class="space-y-2">
          <h2 class="text-sm font-semibold text-muted-foreground">기타 정보</h2>
          <dl class="grid gap-2 text-sm">
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">입사일</dt>
              <dd>{{ formatDate(employee?.joinDate) }}</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">생년월일</dt>
              <dd>{{ formatDate(employee?.birthDate) }}</dd>
            </div>
            <div class="grid grid-cols-[100px_1fr] gap-2">
              <dt class="text-muted-foreground">메모</dt>
              <dd>{{ employee?.memo || '등록된 메모가 없습니다.' }}</dd>
            </div>
          </dl>
        </div>
      </CardContent>
    </Card>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import HttpError from '@/core/http/HttpError';

const route = useRoute();
const router = useRouter();
const repository = appContainer.resolve(EmployeeRepository);

const employee = ref<EmployeeSummary | null>(null);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);

watch(
  () => route.params.employeeId,
  (next) => {
    if (typeof next === 'string' && next.trim().length > 0) {
      fetchEmployee(next);
    }
  },
  { immediate: true },
);

async function fetchEmployee(employeeId: string) {
  isLoading.value = true;
  errorMessage.value = null;
  employee.value = null;
  try {
    employee.value = await repository.findById(employeeId);
  } catch (error) {
    const message =
      error instanceof HttpError
        ? error.message
        : '구성원 정보를 불러오는 중 오류가 발생했습니다.';
    errorMessage.value = message;
  } finally {
    isLoading.value = false;
  }
}

function goToList() {
  router.push({ name: 'employees' }).catch(() => {});
}

function goToDepartment() {
  if (!employee.value?.departmentId) {
    return;
  }
  router
    .push({ name: 'organization', query: { departmentId: employee.value.departmentId } })
    .catch(() => {});
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
