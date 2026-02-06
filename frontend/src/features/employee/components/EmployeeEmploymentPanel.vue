<template>
  <div
    v-if="!employee"
    class="flex h-full items-center justify-center text-sm text-muted-foreground"
  >
    근무 이력을 불러올 직원이 없습니다.
  </div>
  <div v-else class="grid gap-6 lg:grid-cols-[1fr_320px]">
    <section class="rounded-lg border border-border/60 bg-background p-5">
      <h2 class="mb-4 text-sm font-semibold text-muted-foreground">근무 타임라인 (최신순)</h2>

      <div v-if="isLoadingHistory" class="py-4 text-center text-xs text-muted-foreground">
        이력을 불러오는 중...
      </div>

      <ol v-else class="space-y-6">
        <li v-for="(event, index) in timeline" :key="event.id" class="relative pl-6">
          <span
            class="absolute left-0 top-1.5 h-2 w-2 rounded-full"
            :class="index === 0 ? 'bg-primary' : 'bg-muted-foreground/30'"
          />

          <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground mb-0.5">
            {{ event.date }}
          </p>

          <p class="text-sm font-medium text-foreground">
            {{ event.title }}
          </p>

          <p v-if="event.description" class="text-sm text-muted-foreground mt-0.5">
            {{ event.description }}
          </p>
        </li>
      </ol>
    </section>

    <div class="flex flex-col gap-6">
      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="mb-4 text-sm font-semibold text-muted-foreground">관리 메모</h2>
        <p class="text-sm leading-relaxed text-muted-foreground">
          {{ employee.memo || '등록된 메모가 없습니다.' }}
        </p>
        <Alert class="mt-4">
          <AlertTitle>알림</AlertTitle>
          <AlertDescription>
            휴직·복직 이력은 추후 별도 탭으로 분리될 수 있습니다.
          </AlertDescription>
        </Alert>
      </section>

      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="text-sm font-semibold text-muted-foreground">상태 변경</h2>
        <p class="mt-1 text-sm text-muted-foreground">
          현재 상태는 <span class="font-medium text-foreground">{{ employee.status }}</span> 입니다.
        </p>
        <div class="mt-4 flex flex-wrap gap-2">
          <Button variant="outline" :disabled="!canTakeLeave" @click="submitTakeLeave">
            <span v-if="isTakingLeaveAction">처리 중...</span>
            <span v-else>휴직 처리</span>
          </Button>
          <Button variant="secondary" :disabled="!canActivate" @click="submitActivate">
            <span v-if="isActivatingAction">처리 중...</span>
            <span v-else>재직 처리</span>
          </Button>
        </div>
        <p class="mt-2 text-xs text-muted-foreground">
          휴직 처리는 재직 중인 직원에게만 노출되며, 재직 처리는 휴직 또는 퇴사 상태에서도
          가능합니다.
        </p>

        <Alert v-if="takeLeaveSuccessMessage" class="mt-4">
          <AlertTitle>휴직 처리 완료</AlertTitle>
          <AlertDescription>{{ takeLeaveSuccessMessage }}</AlertDescription>
        </Alert>
        <Alert v-if="takeLeaveErrorMessage" class="mt-4" variant="destructive">
          <AlertTitle>휴직 처리 실패</AlertTitle>
          <AlertDescription>{{ takeLeaveErrorMessage }}</AlertDescription>
        </Alert>
        <Alert v-if="activateSuccessMessage" class="mt-4">
          <AlertTitle>재직 처리 완료</AlertTitle>
          <AlertDescription>{{ activateSuccessMessage }}</AlertDescription>
        </Alert>
        <Alert v-if="activateErrorMessage" class="mt-4" variant="destructive">
          <AlertTitle>재직 처리 실패</AlertTitle>
          <AlertDescription>{{ activateErrorMessage }}</AlertDescription>
        </Alert>

        <div class="mt-6 border-t border-border/60 pt-5">
          <h3 class="mb-3 text-sm font-semibold text-muted-foreground">퇴사 처리</h3>
          <p class="text-sm text-muted-foreground">
            퇴사일을 선택하고 퇴사 처리를 진행하세요. 퇴사일은 입사일 이후 날짜여야 합니다.
          </p>
          <div class="mt-4 flex flex-col gap-3">
            <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:gap-3">
              <Input
                v-model="resignationDate"
                type="date"
                class="w-full sm:max-w-[220px]"
                :max="today"
                :min="minResignationDate"
                :disabled="isResigned || isResigningAction"
              />
              <Button variant="destructive" :disabled="!canSubmitResign" @click="submitResignation">
                <span v-if="isResigningAction">처리 중...</span>
                <span v-else>퇴사 처리</span>
              </Button>
            </div>
            <p class="text-xs text-muted-foreground">
              퇴사 처리가 완료되면 직원 상태가 `퇴사`로 변경됩니다. 잘못된 경우 관리자에게
              문의하세요.
            </p>
          </div>
          <Alert v-if="resignSuccessMessage" class="mt-4">
            <AlertTitle>퇴사 처리 완료</AlertTitle>
            <AlertDescription>{{ resignSuccessMessage }}</AlertDescription>
          </Alert>
          <Alert v-if="resignErrorMessage" class="mt-4" variant="destructive">
            <AlertTitle>퇴사 처리 실패</AlertTitle>
            <AlertDescription>{{ resignErrorMessage }}</AlertDescription>
          </Alert>
          <Alert v-if="isResigned" class="mt-4">
            <AlertTitle>이미 퇴사 처리된 직원입니다.</AlertTitle>
            <AlertDescription>
              복구하려면 직원 상세에서 `복구` 기능을 제공할 예정입니다.
            </AlertDescription>
          </Alert>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { PositionHistory } from '@/features/employee/models/positionHistory';
import { getEmployeePositionOptions } from '@/features/employee/models/employeeFilters';

interface TimelineEvent {
  id: string;
  date: string;
  rawDate: number;
  title: string;
  description: string;
}

const props = defineProps<{
  employee: EmployeeSummary | null;
  formatDate: (value?: string | null) => string;
  isResigning: boolean;
  resignError: string | null;
  resignSuccess: string | null;
  isTakingLeave: boolean;
  takeLeaveError: string | null;
  takeLeaveSuccess: string | null;
  isActivating: boolean;
  activateError: string | null;
  activateSuccess: string | null;
}>();

const emit = defineEmits<{
  resign: [string];
  'take-leave': [];
  activate: [];
}>();

const repository = appContainer.resolve(EmployeeRepository);

const today = computed(() => new Date().toISOString().slice(0, 10));
const resignationDate = ref<string>(today.value);
const positionHistories = ref<PositionHistory[]>([]);
const isLoadingHistory = ref(false);

watch(
  () => props.employee?.employeeId,
  async (newId) => {
    resignationDate.value = today.value;
    if (newId) {
      isLoadingHistory.value = true;
      try {
        positionHistories.value = await repository.fetchPositionHistory(newId);
      } catch (e) {
        console.error('직급 이력 조회 실패', e);
        positionHistories.value = [];
      } finally {
        isLoadingHistory.value = false;
      }
    } else {
      positionHistories.value = [];
    }
  },
  { immediate: true },
);

function getPositionLabel(code: string) {
  const options = getEmployeePositionOptions();
  const found = options.find((opt) => opt.value === code);
  return found ? found.label : code;
}

function normalizeDate(dateStr: string) {
  return dateStr.slice(0, 10);
}

const timeline = computed<TimelineEvent[]>(() => {
  if (!props.employee) {
    return [];
  }

  const events: TimelineEvent[] = [];
  const joinDateStr = props.employee.joinDate ?? '';
  const joinTimestamp = joinDateStr ? new Date(joinDateStr).getTime() : 0;
  const deptName = props.employee.departmentName ?? '—';

  // 1. [기본] 입사 이벤트
  const joinedEvent: TimelineEvent = {
    id: 'joined',
    // ✅ 입사일만 표시
    date: props.formatDate(joinDateStr),
    rawDate: joinTimestamp,
    title: '입사',
    description: `${deptName} 부서에 입사했습니다.`,
  };

  // 2. 직급 이력 순회
  positionHistories.value.forEach((history, index) => {
    const startDate = history.period.startDate;
    const timestamp = new Date(startDate).getTime();

    // 직급 명칭
    const koPosition = getPositionLabel(history.position);

    if (normalizeDate(startDate) === normalizeDate(joinDateStr)) {
      // 입사일과 동일하면 입사 정보 구체화
      joinedEvent.description = `${deptName} 부서에 ${koPosition} 직급으로 입사했습니다.`;
    } else {
      // ✅ 직급 변경 이벤트: 시작일만 표시
      events.push({
        id: `pos-${history.id || index}`,
        date: props.formatDate(startDate), // 종료일(~현재) 제거
        rawDate: timestamp,
        title: '직급 변경',
        description: `${koPosition} 직급으로 변경되었습니다.`,
      });
    }
  });

  events.push(joinedEvent);
  events.sort((a, b) => b.rawDate - a.rawDate);

  return events;
});

const isResigned = computed(() => props.employee?.statusCode === 'RESIGNED');
const isOnLeave = computed(() => props.employee?.statusCode === 'ON_LEAVE');
const isActive = computed(() => props.employee?.statusCode === 'ACTIVE');
const minResignationDate = computed(() => props.employee?.joinDate ?? undefined);
const isResigningAction = computed(() => props.isResigning);
const isTakingLeaveAction = computed(() => props.isTakingLeave);
const isActivatingAction = computed(() => props.isActivating);
const canSubmitResign = computed(() => {
  if (isResigned.value || isResigningAction.value) {
    return false;
  }
  return Boolean(resignationDate.value);
});
const canTakeLeave = computed(() => {
  if (isResigned.value || isOnLeave.value) {
    return false;
  }
  return Boolean(isActive.value) && !isTakingLeaveAction.value && !isResigningAction.value;
});
const canActivate = computed(() => {
  if (isResigningAction.value || isActivatingAction.value) {
    return false;
  }
  if (!props.employee) {
    return false;
  }
  return props.employee.statusCode !== 'ACTIVE';
});

function submitResignation() {
  if (!resignationDate.value || !canSubmitResign.value) {
    return;
  }
  emit('resign', resignationDate.value);
}
function submitTakeLeave() {
  if (!canTakeLeave.value) {
    return;
  }
  emit('take-leave');
}
function submitActivate() {
  if (!canActivate.value) {
    return;
  }
  emit('activate');
}

const resignErrorMessage = computed(() => props.resignError);
const resignSuccessMessage = computed(() => props.resignSuccess);
const takeLeaveErrorMessage = computed(() => props.takeLeaveError);
const takeLeaveSuccessMessage = computed(() => props.takeLeaveSuccess);
const activateErrorMessage = computed(() => props.activateError);
const activateSuccessMessage = computed(() => props.activateSuccess);
</script>
