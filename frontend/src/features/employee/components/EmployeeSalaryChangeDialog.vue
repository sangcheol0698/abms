<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-[480px]">
      <DialogHeader>
        <DialogTitle>연봉 변경</DialogTitle>
        <DialogDescription>새 연봉과 적용 시작일을 입력하세요.</DialogDescription>
      </DialogHeader>

      <Alert v-if="errorMessage" variant="destructive">
        <AlertTitle>연봉 변경에 실패했습니다.</AlertTitle>
        <AlertDescription>{{ errorMessage }}</AlertDescription>
      </Alert>

      <div class="grid gap-4 py-4">
        <div class="grid gap-2">
          <Label for="annualSalary">연봉</Label>
          <MoneyInput
            id="annualSalary"
            v-model="annualSalary"
            placeholder="연봉을 입력하세요"
            :disabled="isSubmitting"
          />
        </div>

        <div class="grid gap-2">
          <Label for="startDate">적용 시작일</Label>
          <DatePicker
            id="startDate"
            :model-value="toDateValue(startDate)"
            placeholder="적용 시작일을 선택하세요"
            :disabled="isSubmitting"
            @update:modelValue="(value) => { startDate = formatDate(value); }"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleOpenChange(false)">취소</Button>
        <Button :disabled="isSubmitDisabled" @click="handleSubmit">
          {{ isSubmitting ? '저장 중...' : '저장' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import { MoneyInput } from '@/components/business';
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
import { useChangeEmployeeSalaryMutation } from '@/features/employee/queries/useEmployeeQueries';

interface Props {
  open: boolean;
  employeeId: number;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'changed'): void;
}>();

const changeSalaryMutation = useChangeEmployeeSalaryMutation();
const annualSalary = ref(0);
const startDate = ref(new Date().toISOString().slice(0, 10));
const errorMessage = ref<string | null>(null);
const isSubmitting = computed(() => changeSalaryMutation.isPending.value);
const isSubmitDisabled = computed(
  () =>
    isSubmitting.value
    || props.employeeId <= 0
    || annualSalary.value <= 0
    || startDate.value.trim().length === 0,
);

function toDateValue(value: string): Date | null {
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

watch(
  () => props.open,
  (open) => {
    if (open) {
      annualSalary.value = 0;
      startDate.value = new Date().toISOString().slice(0, 10);
      errorMessage.value = null;
    }
  },
);

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

async function handleSubmit() {
  if (isSubmitDisabled.value) {
    return;
  }

  errorMessage.value = null;

  try {
    await changeSalaryMutation.mutateAsync({
      employeeId: props.employeeId,
      payload: {
        annualSalary: annualSalary.value,
        startDate: startDate.value,
      },
    });
    toast.success('연봉 정보를 변경했습니다.');
    emit('changed');
    handleOpenChange(false);
  } catch (error) {
    errorMessage.value =
      error instanceof HttpError ? error.message : '연봉 변경 중 오류가 발생했습니다.';
  }
}
</script>
