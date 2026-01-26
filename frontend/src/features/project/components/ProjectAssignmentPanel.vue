<template>
  <div class="flex h-full flex-col gap-6">
    <header class="flex flex-col gap-2">
      <h2 class="text-base font-semibold text-foreground">투입 인력 현황</h2>
      <p class="text-sm text-muted-foreground">
        현재 API 연동이 준비 중이라 샘플 데이터가 표시됩니다. 실제 투입인력 API가 마련되면 자동으로 대체됩니다.
      </p>
    </header>

    <section class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-muted-foreground">현재 투입 중</h3>
        <Badge variant="outline">샘플</Badge>
      </div>
      <div v-if="activeAssignments.length === 0" class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground">
        현재 투입 중인 인력이 없습니다.
      </div>
      <div v-else class="grid gap-4 md:grid-cols-2">
        <Card
          v-for="assignment in activeAssignments"
          :key="assignment.id"
          class="border-border/60"
        >
          <CardHeader>
            <div class="flex items-start justify-between gap-2">
              <CardTitle class="text-base font-semibold text-foreground">{{ assignment.employeeName }}</CardTitle>
              <Badge variant="secondary">{{ assignment.role }}</Badge>
            </div>
            <CardDescription>{{ assignment.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            {{ assignment.department }}
          </CardContent>
        </Card>
      </div>
    </section>

    <section class="space-y-3">
      <h3 class="text-sm font-semibold text-muted-foreground">투입 완료</h3>
      <div v-if="completedAssignments.length === 0" class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground">
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
              <CardTitle class="text-base font-semibold text-foreground">{{ assignment.employeeName }}</CardTitle>
              <Badge variant="outline">{{ assignment.role }}</Badge>
            </div>
            <CardDescription>{{ assignment.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            {{ assignment.department }}
          </CardContent>
        </Card>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';

interface AssignmentSummary {
  id: string;
  employeeName: string;
  role: string;
  department: string;
  period: string;
  status: 'active' | 'completed';
}

interface Props {
  projectId: number;
}

defineProps<Props>();

const sampleAssignments: AssignmentSummary[] = [
  {
    id: 'sample-1',
    employeeName: '홍길동',
    role: 'PM',
    department: '개발1팀',
    period: '2024.03.01 ~ 진행 중',
    status: 'active',
  },
  {
    id: 'sample-2',
    employeeName: '김철수',
    role: '백엔드 개발',
    department: '개발1팀',
    period: '2024.03.15 ~ 진행 중',
    status: 'active',
  },
  {
    id: 'sample-3',
    employeeName: '이영희',
    role: 'UI/UX 디자인',
    department: '디자인팀',
    period: '2024.03.01 ~ 2024.06.30',
    status: 'completed',
  },
];

const activeAssignments = computed(() =>
  sampleAssignments.filter((a) => a.status === 'active'),
);

const completedAssignments = computed(() =>
  sampleAssignments.filter((a) => a.status === 'completed'),
);
</script>
