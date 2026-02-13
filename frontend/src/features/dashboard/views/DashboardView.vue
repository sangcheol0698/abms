<template>
  <section class="h-full">
    <div class="grid h-full grid-cols-1 gap-6 lg:grid-cols-3 xl:grid-cols-4">
      <!-- 메인 콘텐츠 영역 (왼쪽) -->
      <div class="flex flex-col gap-6 lg:col-span-2 xl:col-span-3">
        <!-- 로딩 상태 처리 -->
        <div v-if="isLoading" class="grid gap-4 md:grid-cols-3">
          <div
            v-for="i in 3"
            :key="i"
            class="h-[300px] rounded-xl border border-border/60 bg-muted/10 animate-pulse"
          />
        </div>

        <div v-else class="grid gap-4 md:grid-cols-3">
          <DashboardEmployeeDistributionPanel :total-count="summary?.totalEmployeesCount" />
          <DashboardEmployeeStatusPanel
            :total-count="summary?.totalEmployeesCount"
            :on-leave-count="summary?.onLeaveEmployeesCount"
          />
          <DashboardJobLevelPanel />
        </div>

        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
          <DashboardMonthlyFinancialPanel class="col-span-4" />
          <DashboardDepartmentFinancialPanel class="col-span-3" />
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <DashboardProjectStatusPanel />
          <DashboardUpcomingDeadlinesPanel />
        </div>
      </div>

      <!-- 사이드바 영역 (오른쪽) -->
      <div class="lg:col-span-1 xl:col-span-1">
        <Card class="h-full shadow-none">
          <CardHeader>
            <CardTitle class="text-base">최근 알림</CardTitle>
            <CardDescription>시스템 전반에서 발생한 주요 이벤트입니다.</CardDescription>
          </CardHeader>
          <CardContent class="space-y-3">
            <div
              v-for="notification in notifications"
              :key="notification.id"
              class="flex items-start justify-between rounded-md border border-border/70 p-3 text-sm"
            >
              <div>
                <p class="font-medium text-foreground">{{ notification.title }}</p>
                <p class="text-xs text-muted-foreground">{{ notification.description }}</p>
              </div>
              <Badge variant="outline" class="text-[11px] uppercase tracking-tight">{{
                notification.time
              }}</Badge>
            </div>
            <p v-if="!notifications.length" class="text-sm text-muted-foreground">
              최근 알림이 없습니다.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { appContainer } from '@/core/di/container';
import {
  DashboardRepository,
  type DashboardSummary,
} from '@/features/dashboard/repository/DashboardRepository';
import DashboardMonthlyFinancialPanel from '@/features/dashboard/components/DashboardMonthlyFinancialPanel.vue';
import DashboardDepartmentFinancialPanel from '@/features/dashboard/components/DashboardDepartmentFinancialPanel.vue';
import DashboardProjectStatusPanel from '@/features/dashboard/components/DashboardProjectStatusPanel.vue';
import DashboardUpcomingDeadlinesPanel from '@/features/dashboard/components/DashboardUpcomingDeadlinesPanel.vue';
import DashboardEmployeeDistributionPanel from '@/features/dashboard/components/DashboardEmployeeDistributionPanel.vue';
import DashboardEmployeeStatusPanel from '@/features/dashboard/components/DashboardEmployeeStatusPanel.vue';
import DashboardJobLevelPanel from '@/features/dashboard/components/DashboardJobLevelPanel.vue';

const repository = appContainer.resolve(DashboardRepository);

const summary = ref<DashboardSummary | null>(null);
const isLoading = ref(true);

const notifications = computed(() => [
  {
    id: 'n-1',
    title: '조직 개편 완료',
    description: '경영기획실이 신설되어 부서 정보가 업데이트되었습니다.',
    time: '2시간 전',
  },
  {
    id: 'n-2',
    title: '연말 정산 안내',
    description: '전 직원 대상 연말 정산 일정이 공지되었습니다.',
    time: '어제',
  },
  {
    id: 'n-3',
    title: '교육 일정 등록',
    description: '1월 신규 입사자 교육 일정이 추가되었습니다.',
    time: '3일 전',
  },
]);

async function fetchDashboardData() {
  isLoading.value = true;
  try {
    summary.value = await repository.fetchSummary();
  } finally {
    isLoading.value = false;
  }
}

onMounted(() => {
  fetchDashboardData();
});
</script>
