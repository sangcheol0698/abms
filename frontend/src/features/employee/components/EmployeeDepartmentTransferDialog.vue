<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-[425px]">
      <DialogHeader>
        <DialogTitle>직원 부서 이동</DialogTitle>
        <DialogDescription>
          {{ employee?.name }} 님을 이동할 부서를 선택해 주세요.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-6 py-4">
        <div class="space-y-2 rounded-lg border bg-muted/50 p-3">
          <span class="text-xs text-muted-foreground">현재 부서</span>
          <p class="text-sm font-medium">{{ employee?.departmentName || '-' }}</p>
        </div>

        <div class="space-y-2">
          <Label>이동할 부서</Label>
          <Select v-model="departmentIdValue">
            <SelectTrigger>
              <SelectValue placeholder="부서를 선택하세요" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="option in departmentOptions"
                :key="option.value"
                :value="String(option.value)"
              >
                {{ option.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="$emit('update:open', false)">취소</Button>
        <Button :disabled="isSubmitting" @click="handleSubmit">
          {{ isSubmitting ? '처리 중...' : '부서 이동' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import { useTransferDepartmentEmployeeMutation } from '@/features/employee/queries/useEmployeeQueries';

const props = defineProps<{
  open: boolean;
  employee?: EmployeeSummary | null;
  departmentOptions: { label: string; value: number }[];
}>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'transferred'): void;
}>();

const transferDepartmentMutation = useTransferDepartmentEmployeeMutation();
const isSubmitting = ref(false);
const departmentIdValue = ref('');

const availableDepartmentOptions = computed(() =>
  props.departmentOptions.filter((option) => option.value !== props.employee?.departmentId),
);

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) {
      departmentIdValue.value = '';
      return;
    }

    const firstOption = availableDepartmentOptions.value[0];
    departmentIdValue.value = firstOption ? String(firstOption.value) : '';
  },
);

async function handleSubmit() {
  if (!props.employee?.employeeId) {
    return;
  }
  const nextDepartmentId = Number(departmentIdValue.value);
  if (!Number.isFinite(nextDepartmentId) || nextDepartmentId <= 0) {
    toast.error('이동할 부서를 선택해주세요.');
    return;
  }

  isSubmitting.value = true;
  try {
    await transferDepartmentMutation.mutateAsync({
      employeeId: props.employee.employeeId,
      departmentId: nextDepartmentId,
    });
    toast.success('부서 이동이 완료되었습니다.');
    emit('transferred');
    emit('update:open', false);
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '부서 이동 중 오류가 발생했습니다.';
    toast.error('부서 이동에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
