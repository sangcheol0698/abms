<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-[420px]">
      <DialogHeader>
        <DialogTitle>투입 종료</DialogTitle>
        <DialogDescription>종료일을 입력해 투입 이력을 종료 처리합니다.</DialogDescription>
      </DialogHeader>

      <Alert v-if="errorMessage" variant="destructive">
        <AlertTitle>투입 종료에 실패했습니다.</AlertTitle>
        <AlertDescription>{{ errorMessage }}</AlertDescription>
      </Alert>

      <div class="grid gap-4 py-4">
        <div class="grid gap-2">
          <Label>직원</Label>
          <p class="text-sm text-foreground">{{ assignment?.employeeName || '—' }}</p>
        </div>
        <div class="grid gap-2">
          <Label for="assignmentEndDate">종료일</Label>
          <DatePicker
            id="assignmentEndDate"
            :model-value="endDateValue"
            :disabled="isSubmitting"
            placeholder="종료일을 선택하세요"
            @update:modelValue="handleEndDateChange"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleOpenChange(false)">취소</Button>
        <Button :disabled="isSubmitDisabled" @click="handleSubmit">
          {{ isSubmitting ? '저장 중...' : '종료 처리' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import type { ProjectAssignmentItem } from '@/features/project/models/projectAssignment';
import { useEndProjectAssignmentMutation } from '@/features/project/queries/useProjectQueries';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { DatePicker } from '@/components/ui/date-picker';
import { Label } from '@/components/ui/label';

interface Props {
  open: boolean;
  projectId: number;
  projectEndDate: string | null;
  assignment?: ProjectAssignmentItem | null;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'ended'): void;
}>();

const endMutation = useEndProjectAssignmentMutation();
const endDate = ref('');
const endDateValue = ref<Date | null>(null);
const errorMessage = ref<string | null>(null);
const isSubmitting = computed(() => endMutation.isPending.value);
const isSubmitDisabled = computed(() => isSubmitting.value || !props.assignment?.id || !endDate.value);

watch(
  () => props.open,
  (open) => {
    if (!open) {
      errorMessage.value = null;
      return;
    }

    errorMessage.value = null;
    endDate.value = props.assignment?.endDate ?? props.projectEndDate ?? new Date().toISOString().slice(0, 10);
    endDateValue.value = toDateValue(endDate.value);
  },
  { immediate: true },
);

function toDateValue(value: string | null): Date | null {
  if (!value) {
    return null;
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

function formatDate(value: Date | null): string {
  if (!value) {
    return '';
  }

  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function handleEndDateChange(value: Date | null) {
  endDateValue.value = value;
  endDate.value = formatDate(value);
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

async function handleSubmit() {
  if (!props.assignment?.id || isSubmitDisabled.value) {
    return;
  }

  if (endDate.value < props.assignment.startDate) {
    errorMessage.value = '투입 종료일은 투입 시작일보다 빠를 수 없습니다.';
    return;
  }
  if (props.projectEndDate && endDate.value > props.projectEndDate) {
    errorMessage.value = '투입 종료일은 프로젝트 종료일보다 늦을 수 없습니다.';
    return;
  }

  errorMessage.value = null;

  try {
    await endMutation.mutateAsync({
      assignmentId: props.assignment.id,
      projectId: props.projectId,
      payload: { endDate: endDate.value },
    });
    toast.success('투입 인력을 종료 처리했습니다.');
    emit('ended');
    handleOpenChange(false);
  } catch (error) {
    errorMessage.value = error instanceof HttpError ? error.message : '투입 종료 처리 중 오류가 발생했습니다.';
  }
}
</script>
