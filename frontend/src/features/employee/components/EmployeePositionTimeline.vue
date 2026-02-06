<template>
  <div class="rounded-lg border bg-card text-card-foreground shadow-sm">
    <div class="p-6 pb-4">
      <h3 class="text-lg font-semibold leading-none tracking-tight">직급 이력</h3>
      <p class="text-sm text-muted-foreground mt-1.5">이 직원의 직급 변동 내역입니다.</p>
    </div>

    <div class="p-6 pt-0">
      <div v-if="isLoading" class="py-4 text-center text-sm text-muted-foreground">
        데이터를 불러오는 중...
      </div>

      <div
        v-else-if="histories.length === 0"
        class="py-4 text-center text-sm text-muted-foreground"
      >
        직급 이력이 존재하지 않습니다.
      </div>

      <div v-else class="relative ml-2 border-l border-border pl-6 space-y-6 my-2">
        <div
          v-for="(history, index) in sortedHistories"
          :key="history.id || index"
          class="relative"
        >
          <span
            class="absolute -left-[29px] top-1 flex h-3 w-3 rounded-full ring-4 ring-background"
            :class="!history.period.endDate ? 'bg-primary' : 'bg-muted-foreground/30'"
          ></span>

          <div class="flex flex-col gap-1">
            <span class="text-xs font-medium text-muted-foreground">
              {{ formatDate(history.period.startDate) }} ~
              {{ history.period.endDate ? formatDate(history.period.endDate) : '현재' }}
            </span>

            <div class="flex items-center gap-2">
              <span class="font-semibold text-sm">
                {{ PositionLabel[history.position] || history.position }}
              </span>
              <span
                v-if="!history.period.endDate"
                class="inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-semibold transition-colors border-transparent bg-primary text-primary-foreground hover:bg-primary/80"
              >
                Current
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import { type PositionHistory, PositionLabel } from '@/features/employee/models/positionHistory';

const props = defineProps<{
  employeeId: number;
}>();

const repository = appContainer.resolve(EmployeeRepository);
const histories = ref<PositionHistory[]>([]);
const isLoading = ref(false);

// 최신순(시작일 내림차순) 정렬
const sortedHistories = computed(() => {
  return [...histories.value].sort((a, b) => {
    return new Date(b.period.startDate).getTime() - new Date(a.period.startDate).getTime();
  });
});

function formatDate(dateString: string) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}. ${date.getMonth() + 1}. ${date.getDate()}.`;
}

async function fetchHistory() {
  if (!props.employeeId) return;

  isLoading.value = true;
  try {
    const data = await repository.fetchPositionHistory(props.employeeId);
    histories.value = data;
  } catch (error) {
    console.error('직급 이력 조회 실패:', error);
  } finally {
    isLoading.value = false;
  }
}

// employeeId가 변경될 때마다 데이터 다시 조회
watch(
  () => props.employeeId,
  (newId) => {
    if (newId) fetchHistory();
  },
  { immediate: true },
);
</script>
