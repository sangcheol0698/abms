<template>
  <Card>
    <CardHeader>
      <CardTitle>다가오는 마감일</CardTitle>
      <CardDescription>마감 기한이 임박한 프로젝트입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div
        v-if="isLoading && projects.length === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        데이터를 불러오는 중입니다...
      </div>
      <div
        v-else-if="projects.length === 0"
        class="flex h-[240px] items-center justify-center text-sm text-muted-foreground"
      >
        30일 이내 마감 프로젝트가 없습니다.
      </div>
      <div v-else class="space-y-4">
        <div
          v-for="project in projects"
          :key="project.id"
          class="flex items-center justify-between border-b border-border/40 pb-3 last:border-0 last:pb-0"
        >
          <div class="space-y-1">
            <p class="text-sm font-medium leading-none">{{ project.name }}</p>
            <p class="text-xs text-muted-foreground">
              {{ project.partyName }} · {{ project.statusDescription }}
            </p>
          </div>
          <div class="text-right">
            <div class="text-sm font-medium" :style="getDeadlineStyle(project.daysLeft)">
              {{ formatDeadlineLabel(project.daysLeft) }}
            </div>
            <p class="text-xs text-muted-foreground">{{ formatProjectDate(project.endDate) }}</p>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { statusColorVars } from '@/core/theme/theme';
import { useDashboardUpcomingDeadlinesQuery } from '@/features/dashboard/queries/useDashboardQueries';
import { formatProjectDate } from '@/features/project/models/projectListItem';

const upcomingDeadlinesQuery = useDashboardUpcomingDeadlinesQuery();
const isLoading = computed(() => upcomingDeadlinesQuery.isLoading.value);
const projects = computed(() =>
  (upcomingDeadlinesQuery.data.value ?? []).map((project) => ({
    id: project.projectId,
    name: project.name,
    partyName: project.partyName,
    statusDescription: project.statusDescription,
    endDate: project.endDate,
    daysLeft: project.daysLeft,
  })),
);

function getDeadlineStyle(days: number) {
  if (days <= 0) {
    return { color: statusColorVars.danger, fontWeight: '700' };
  }

  if (days <= 7) {
    return { color: statusColorVars.warning, fontWeight: '600' };
  }

  return { color: 'var(--foreground)' };
}

function formatDeadlineLabel(days: number) {
  if (days < 0) {
    return `지연 ${Math.abs(days)}일`;
  }

  if (days === 0) {
    return 'D-Day';
  }

  return `D-${days}`;
}
</script>
