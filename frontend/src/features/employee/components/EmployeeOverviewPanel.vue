<template>
  <div
    v-if="!employee"
    class="flex h-full items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-6 text-sm text-muted-foreground"
  >
    직원 정보를 불러오지 못했습니다.
  </div>
  <div v-else class="grid gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
    <div class="space-y-6">
      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="mb-4 text-sm font-semibold text-muted-foreground">인사 정보</h2>
        <dl class="grid gap-3 text-sm">
          <div class="grid grid-cols-[90px_1fr] items-center gap-2">
            <dt class="text-muted-foreground">부서</dt>
            <dd>
              <button
                type="button"
                class="font-medium text-primary underline underline-offset-4 transition hover:text-primary/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:text-muted-foreground"
                :disabled="!employee.departmentId"
                @click="handleDepartmentClick"
              >
                {{ employee.departmentName || '—' }}
              </button>
            </dd>
          </div>
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">직책</dt>
            <dd class="font-medium text-foreground">{{ employee.position || '—' }}</dd>
          </div>
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">등급</dt>
            <dd class="font-medium text-foreground">{{ employee.grade || '—' }}</dd>
          </div>
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">상태</dt>
            <dd class="font-medium text-foreground">{{ employee.status || '—' }}</dd>
          </div>
        </dl>
      </section>

      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="mb-4 text-sm font-semibold text-muted-foreground">기본 정보</h2>
        <dl class="grid gap-3 text-sm">
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">근무 유형</dt>
            <dd class="text-foreground">{{ employee.type || '—' }}</dd>
          </div>
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">입사일</dt>
            <dd class="text-foreground">{{ formatDate(employee.joinDate) }}</dd>
          </div>
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">생년월일</dt>
            <dd class="text-foreground">{{ formatDate(employee.birthDate) }}</dd>
          </div>
          <div class="grid grid-cols-[90px_1fr] gap-2">
            <dt class="text-muted-foreground">이메일</dt>
            <dd class="text-foreground">{{ employee.email || '—' }}</dd>
          </div>
        </dl>
      </section>
    </div>

    <section class="rounded-lg border border-border/60 bg-background p-5">
      <h2 class="mb-4 text-sm font-semibold text-muted-foreground">업무 메모</h2>
      <p class="text-sm leading-relaxed text-muted-foreground">
        {{ employee.memo || '등록된 메모가 없습니다.' }}
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { EmployeeSummary } from '@/features/employee/models/employee';

interface Props {
  employee: EmployeeSummary | null;
  formatDate: (value?: string | null) => string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  'department-click': [];
}>();

function handleDepartmentClick() {
  if (!props.employee?.departmentId) {
    return;
  }
  emit('department-click');
}
</script>
