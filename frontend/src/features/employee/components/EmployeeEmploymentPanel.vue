<template>
  <div
    v-if="!employee"
    class="flex h-full items-center justify-center text-sm text-muted-foreground"
  >
    근무 이력을 불러올 구성원이 없습니다.
  </div>
  <div v-else class="grid gap-6 lg:grid-cols-[1fr_320px]">
    <section class="rounded-lg border border-border/60 bg-background p-5">
      <h2 class="mb-4 text-sm font-semibold text-muted-foreground">근무 타임라인</h2>
      <ol class="space-y-4">
        <li v-for="event in timeline" :key="event.id" class="relative pl-6">
          <span class="absolute left-0 top-1.5 h-2 w-2 rounded-full bg-primary" />
          <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            {{ event.date }}
          </p>
          <p class="text-sm font-medium text-foreground">{{ event.title }}</p>
          <p class="text-sm text-muted-foreground">{{ event.description }}</p>
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
          <AlertTitle>이력 확장 예정</AlertTitle>
          <AlertDescription>
            휴직·복직·직무 변경과 같은 세부 근무 이력은 별도 API 연동 후 표시될 예정입니다.
          </AlertDescription>
        </Alert>
      </section>

      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="text-sm font-semibold text-muted-foreground">상태 변경</h2>
        <p class="mt-1 text-sm text-muted-foreground">
          현재 상태는 <span class="font-medium text-foreground">{{ employee.status }}</span> 입니다.
        </p>

        <div class="mt-4 flex flex-wrap gap-2">
          <Button
            variant="outline"
            :disabled="!canTakeLeave"
            @click="submitTakeLeave"
          >
            <span v-if="isTakingLeaveAction">처리 중...</span>
            <span v-else>휴직 처리</span>
          </Button>
          <Button
            variant="secondary"
            :disabled="!canActivate"
            @click="submitActivate"
          >
            <span v-if="isActivatingAction">처리 중...</span>
            <span v-else>재직 처리</span>
          </Button>
        </div>

        <p class="mt-2 text-xs text-muted-foreground">
          휴직 처리는 재직 중인 구성원에게만 노출되며, 재직 처리는 휴직 또는 퇴사 상태에서도
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
              <Button
                variant="destructive"
                :disabled="!canSubmitResign"
                @click="submitResignation"
              >
                <span v-if="isResigningAction">처리 중...</span>
                <span v-else>퇴사 처리</span>
              </Button>
            </div>
            <p class="text-xs text-muted-foreground">
              퇴사 처리가 완료되면 구성원 상태가 `퇴사`로 변경됩니다. 잘못된 경우 관리자에게
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
            <AlertTitle>이미 퇴사 처리된 구성원입니다.</AlertTitle>
            <AlertDescription>
              복구하려면 구성원 상세에서 `복구` 기능을 제공할 예정입니다.
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

interface TimelineEvent {
  id: string;
  date: string;
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

const today = computed(() => new Date().toISOString().slice(0, 10));
const resignationDate = ref<string>(today.value);

watch(
  () => props.employee?.employeeId,
  () => {
    resignationDate.value = today.value;
  },
  { immediate: true },
);

const timeline = computed<TimelineEvent[]>(() => {
  if (!props.employee) {
    return [];
  }

  const events: TimelineEvent[] = [
    {
      id: 'joined',
      date: props.formatDate(props.employee.joinDate),
      title: '입사',
      description: `${props.employee.departmentName ?? '—'} 부서에 합류했습니다.`,
    },
  ];

  if (props.employee.statusCode === 'ON_LEAVE') {
    events.push({
      id: 'on-leave',
      date: '진행 중',
      title: '휴직',
      description: '현재 휴직 중입니다. 복귀 일정은 추후 등록됩니다.',
    });
  }

  if (props.employee.statusCode === 'RESIGNED') {
    events.push({
      id: 'resigned',
      date: '연동 예정',
      title: '퇴사',
      description: '퇴사 날짜는 추가 API에서 제공됩니다.',
    });
  } else {
    events.push({
      id: 'active',
      date: '현재',
      title: props.employee.status,
      description: '최신 재직 상태입니다.',
    });
  }

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
