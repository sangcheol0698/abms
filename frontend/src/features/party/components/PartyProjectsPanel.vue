<template>
  <div class="flex h-full flex-col gap-6">
    <header class="flex flex-col gap-2">
      <h2 class="text-base font-semibold text-foreground">관련 프로젝트</h2>
      <p class="text-sm text-muted-foreground">
        이 협력사와 관련된 프로젝트 목록입니다.
      </p>
    </header>

    <div
      v-if="isLoading"
      class="flex h-[180px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 text-sm text-muted-foreground"
    >
      프로젝트 정보를 불러오는 중입니다...
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>프로젝트 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <template v-else>
      <section class="space-y-3">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-semibold text-muted-foreground">진행 중</h3>
          <Badge variant="outline">{{ ongoingProjects.length }}</Badge>
        </div>
        <div v-if="ongoingProjects.length === 0" class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground">
          진행 중인 프로젝트가 없습니다.
        </div>
        <div v-else class="grid gap-4 md:grid-cols-2">
          <Card
            v-for="project in ongoingProjects"
            :key="project.projectId"
            class="border-border/60"
          >
            <CardHeader>
              <div class="flex items-start justify-between gap-2">
                <CardTitle class="text-base font-semibold text-foreground">{{ project.name }}</CardTitle>
                <Badge variant="outline">{{ project.statusLabel }}</Badge>
              </div>
              <CardDescription>{{ toProjectPeriod(project) }}</CardDescription>
            </CardHeader>
            <CardContent class="text-sm text-muted-foreground">
              계약금액: {{ toContractAmount(project) }}
            </CardContent>
          </Card>
        </div>
      </section>

      <section class="space-y-3">
        <div class="flex items-center justify-between">
          <h3 class="text-sm font-semibold text-muted-foreground">완료</h3>
          <Badge variant="outline">{{ completedProjects.length }}</Badge>
        </div>
        <div v-if="completedProjects.length === 0" class="rounded-lg border border-dashed border-border/60 p-4 text-sm text-muted-foreground">
          완료된 프로젝트가 없습니다.
        </div>
        <div v-else class="grid gap-4 md:grid-cols-2">
          <Card
            v-for="project in completedProjects"
            :key="project.projectId"
            class="border-border/60"
          >
            <CardHeader>
              <div class="flex items-start justify-between gap-2">
                <CardTitle class="text-base font-semibold text-foreground">{{ project.name }}</CardTitle>
                <Badge variant="outline">{{ project.statusLabel }}</Badge>
              </div>
              <CardDescription>{{ toProjectPeriod(project) }}</CardDescription>
            </CardHeader>
            <CardContent class="text-sm text-muted-foreground">
              계약금액: {{ toContractAmount(project) }}
            </CardContent>
          </Card>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, toRef } from 'vue';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import type { ProjectListItem } from '@/features/project/models/projectListItem';
import { formatCurrency, formatProjectPeriod } from '@/features/project/models/projectListItem';
import { usePartyProjectsQuery } from '@/features/party/queries/usePartyQueries';

interface Props {
  partyId: number;
}

const props = defineProps<Props>();
const partyId = toRef(props, 'partyId');
const projectsQuery = usePartyProjectsQuery(partyId);
const projects = computed(() => projectsQuery.data.value ?? []);
const isLoading = computed(() => projectsQuery.isLoading.value);
const errorMessage = computed(() => {
  const error = projectsQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof Error ? error.message : '프로젝트 정보를 불러오는 중 오류가 발생했습니다.';
});

const ongoingStatuses = new Set(['SCHEDULED', 'IN_PROGRESS', 'ON_HOLD']);
const completedStatuses = new Set(['COMPLETED', 'CANCELLED']);

const ongoingProjects = computed(() =>
  sortProjects(projects.value.filter((project) => ongoingStatuses.has(project.status))),
);

const completedProjects = computed(() =>
  sortProjects(projects.value.filter((project) => completedStatuses.has(project.status))),
);

function sortProjects(items: ProjectListItem[]) {
  return [...items].sort((a, b) => {
    const left = Date.parse(a.startDate);
    const right = Date.parse(b.startDate);
    if (Number.isNaN(left) && Number.isNaN(right)) {
      return 0;
    }
    if (Number.isNaN(left)) {
      return 1;
    }
    if (Number.isNaN(right)) {
      return -1;
    }
    return right - left;
  });
}

function toProjectPeriod(project: ProjectListItem) {
  if (!project.startDate) {
    return '기간 정보 없음';
  }
  return formatProjectPeriod(project.startDate, project.endDate);
}

function toContractAmount(project: ProjectListItem) {
  if (!project.contractAmount || project.contractAmount <= 0) {
    return '—';
  }
  return formatCurrency(project.contractAmount);
}

</script>
