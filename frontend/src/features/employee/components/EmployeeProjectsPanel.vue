<template>
  <div class="flex h-full flex-col gap-6">
    <header class="flex flex-col gap-2">
      <h2 class="text-base font-semibold text-foreground">프로젝트 참여 현황</h2>
      <p class="text-sm text-muted-foreground">
        현재 API 연동이 준비 중이라 샘플 데이터가 표시됩니다. 실제 프로젝트 API가 마련되면 자동으로 대체됩니다.
      </p>
    </header>

    <section class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-muted-foreground">참여 중</h3>
        <Badge variant="outline">샘플</Badge>
      </div>
      <div v-if="ongoingProjects.length === 0" class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground">
        진행 중인 프로젝트가 없습니다.
      </div>
      <div v-else class="grid gap-4 md:grid-cols-2">
        <Card
          v-for="project in ongoingProjects"
          :key="project.id"
          class="border-border/60"
        >
          <CardHeader>
            <div class="flex items-start justify-between gap-2">
              <CardTitle class="text-base font-semibold text-foreground">{{ project.name }}</CardTitle>
              <Badge variant="secondary">{{ project.role }}</Badge>
            </div>
            <CardDescription>{{ project.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            {{ project.description }}
          </CardContent>
        </Card>
      </div>
    </section>

    <section class="space-y-3">
      <h3 class="text-sm font-semibold text-muted-foreground">참여 완료</h3>
      <div v-if="completedProjects.length === 0" class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground">
        완료된 프로젝트가 없습니다.
      </div>
      <div v-else class="grid gap-4 md:grid-cols-2">
        <Card
          v-for="project in completedProjects"
          :key="project.id"
          class="border-border/60"
        >
          <CardHeader>
            <div class="flex items-start justify-between gap-2">
              <CardTitle class="text-base font-semibold text-foreground">{{ project.name }}</CardTitle>
              <Badge variant="outline">{{ project.role }}</Badge>
            </div>
            <CardDescription>{{ project.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            {{ project.description }}
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

interface ProjectSummary {
  id: string;
  name: string;
  role: string;
  period: string;
  description: string;
  status: 'ongoing' | 'completed';
}

interface Props {
  employeeId: number;
}

defineProps<Props>();

const sampleProjects: ProjectSummary[] = [
  {
    id: 'sample-1',
    name: 'ABMS 차세대 구축',
    role: '백엔드 개발',
    period: '2024.03 ~ 진행 중',
    description: '인사/조직 관리 고도화를 위한 차세대 백엔드 플랫폼 구축 프로젝트입니다.',
    status: 'ongoing',
  },
  {
    id: 'sample-2',
    name: '사내 데이터 포털',
    role: 'API 설계',
    period: '2023.05 ~ 2023.12',
    description: '사내 업무 데이터를 통합하는 데이터 포털 MVP를 설계했습니다.',
    status: 'completed',
  },
];

const ongoingProjects = computed(() =>
  sampleProjects.filter((project) => project.status === 'ongoing'),
);

const completedProjects = computed(() =>
  sampleProjects.filter((project) => project.status === 'completed'),
);
</script>
