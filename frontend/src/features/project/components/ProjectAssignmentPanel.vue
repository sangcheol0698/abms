<template>
  <div class="flex h-full flex-col gap-6">
    <header class="flex flex-col gap-2">
      <h2 class="text-base font-semibold text-foreground">투입 인력 현황</h2>
      <p class="text-sm text-muted-foreground">프로젝트에 배치된 인력 목록입니다.</p>
    </header>

    <section class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-muted-foreground">현재 투입 중</h3>
        <Badge variant="outline">{{ activeAssignments.length }}</Badge>
      </div>
      <div
        v-if="isLoading"
        class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground"
      >
        투입 인력 정보를 불러오는 중입니다.
      </div>
      <div
        v-else-if="activeAssignments.length === 0"
        class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground"
      >
        현재 투입 중인 인력이 없습니다.
      </div>
      <div v-else class="grid gap-4 md:grid-cols-2">
        <Card v-for="assignment in activeAssignments" :key="assignment.id" class="border-border/60">
          <CardHeader>
            <div class="flex items-start justify-between gap-2">
              <CardTitle class="text-base font-semibold text-foreground">
                {{ assignment.employeeName }}
              </CardTitle>
              <Badge variant="secondary">{{ assignment.role }}</Badge>
            </div>
            <CardDescription>{{ assignment.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            {{ assignment.departmentName || '부서 정보 없음' }}
          </CardContent>
        </Card>
      </div>
    </section>

    <section class="space-y-3">
      <h3 class="text-sm font-semibold text-muted-foreground">투입 완료</h3>
      <div
        v-if="isLoading"
        class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground"
      >
        투입 인력 정보를 불러오는 중입니다.
      </div>
      <div
        v-else-if="completedAssignments.length === 0"
        class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground"
      >
        투입 완료된 인력이 없습니다.
      </div>
      <div v-else class="grid gap-4 md:grid-cols-2">
        <Card
          v-for="assignment in completedAssignments"
          :key="assignment.id"
          class="border-border/60"
        >
          <CardHeader>
            <div class="flex items-start justify-between gap-2">
              <CardTitle class="text-base font-semibold text-foreground">
                {{ assignment.employeeName }}
              </CardTitle>
              <Badge variant="outline">{{ assignment.role }}</Badge>
            </div>
            <CardDescription>{{ assignment.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            {{ assignment.departmentName || '부서 정보 없음' }}
          </CardContent>
        </Card>
      </div>
    </section>

    <Alert v-if="errorMessage" variant="destructive">
      <AlertTitle>투입 인력 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>
  </div>
</template>

<script setup lang="ts">
import { computed, toRef } from 'vue';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { useProjectAssignmentsQuery } from '@/features/project/queries/useProjectQueries';

interface Props {
  projectId: number;
}

const props = defineProps<Props>();
const projectId = toRef(props, 'projectId');

const assignmentsQuery = useProjectAssignmentsQuery(projectId);
const assignments = computed(() => assignmentsQuery.data.value ?? []);
const isLoading = computed(() => assignmentsQuery.isLoading.value);
const errorMessage = computed(() => {
  const error = assignmentsQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof Error ? error.message : '투입 인력 정보를 불러오는 중 오류가 발생했습니다.';
});

interface AssignmentViewModel {
  id: number;
  employeeName: string;
  role: string;
  departmentName?: string;
  period: string;
  endDate?: string;
}

function toDate(value?: string): Date | null {
  if (!value) {
    return null;
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

function formatDate(value?: string): string {
  const parsed = toDate(value);
  if (!parsed) {
    return '미정';
  }
  const y = parsed.getFullYear();
  const m = String(parsed.getMonth() + 1).padStart(2, '0');
  const d = String(parsed.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function toAssignmentViewModel(): AssignmentViewModel[] {
  return assignments.value.map((assignment) => ({
    id: assignment.id,
    employeeName: assignment.employeeName,
    role: assignment.role,
    departmentName: assignment.departmentName,
    period: `${formatDate(assignment.startDate)} ~ ${assignment.endDate ? formatDate(assignment.endDate) : '진행 중'}`,
    endDate: assignment.endDate,
  }));
}

const now = computed(() => new Date());

const activeAssignments = computed(() =>
  toAssignmentViewModel().filter((assignment) => {
    const end = toDate(assignment.endDate);
    return !end || end >= now.value;
  }),
);

const completedAssignments = computed(() =>
  toAssignmentViewModel().filter((assignment) => {
    const end = toDate(assignment.endDate);
    return !!end && end < now.value;
  }),
);
</script>
