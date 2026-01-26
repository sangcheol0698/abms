<template>
  <div class="flex h-full flex-col gap-6">
    <header class="flex flex-col gap-2">
      <h2 class="text-base font-semibold text-foreground">관련 프로젝트</h2>
      <p class="text-sm text-muted-foreground">
        이 협력사와 관련된 프로젝트 목록입니다.
      </p>
    </header>

    <section class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-sm font-semibold text-muted-foreground">진행 중</h3>
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
            <CardTitle class="text-base font-semibold text-foreground">{{ project.name }}</CardTitle>
            <CardDescription>{{ project.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            계약금액: {{ project.amount }}
          </CardContent>
        </Card>
      </div>
    </section>

    <section class="space-y-3">
      <h3 class="text-sm font-semibold text-muted-foreground">완료</h3>
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
            <CardTitle class="text-base font-semibold text-foreground">{{ project.name }}</CardTitle>
            <CardDescription>{{ project.period }}</CardDescription>
          </CardHeader>
          <CardContent class="text-sm text-muted-foreground">
            계약금액: {{ project.amount }}
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
  period: string;
  amount: string;
  status: 'ongoing' | 'completed';
}

interface Props {
  partyId: number;
}

defineProps<Props>();

const sampleProjects: ProjectSummary[] = [
  {
    id: 'sample-1',
    name: 'ABMS 시스템 구축',
    period: '2024.03 ~ 진행 중',
    amount: '5억원',
    status: 'ongoing',
  },
  {
    id: 'sample-2',
    name: '레거시 마이그레이션',
    period: '2023.01 ~ 2023.12',
    amount: '3억원',
    status: 'completed',
  },
];

const ongoingProjects = computed(() =>
  sampleProjects.filter((p) => p.status === 'ongoing'),
);

const completedProjects = computed(() =>
  sampleProjects.filter((p) => p.status === 'completed'),
);
</script>
