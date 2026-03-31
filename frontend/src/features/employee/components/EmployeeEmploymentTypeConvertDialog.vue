<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-[425px]">
      <DialogHeader>
        <DialogTitle>고용유형 변경</DialogTitle>
        <DialogDescription>
          {{ employee?.name }} 님의 새로운 고용유형을 선택해 주세요.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-6 py-4">
        <div class="space-y-2 rounded-lg border bg-muted/50 p-3">
          <span class="text-xs text-muted-foreground">현재 고용유형</span>
          <p class="text-sm font-medium">{{ employee?.type || '-' }}</p>
        </div>

        <div class="space-y-2">
          <Label>변경할 고용유형</Label>
          <Select v-model="typeValue">
            <SelectTrigger>
              <SelectValue placeholder="고용유형을 선택하세요" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="option in availableTypeOptions"
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
          {{ isSubmitting ? '처리 중...' : '고용유형 변경' }}
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
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import { useConvertEmploymentTypeEmployeeMutation } from '@/features/employee/queries/useEmployeeQueries';

const props = defineProps<{
  open: boolean;
  employee?: EmployeeSummary | null;
  typeOptions: EmployeeFilterOption[];
}>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'converted'): void;
}>();

const convertEmploymentTypeMutation = useConvertEmploymentTypeEmployeeMutation();
const isSubmitting = ref(false);
const typeValue = ref('');

const availableTypeOptions = computed(() =>
  props.typeOptions.filter((option) => String(option.value) !== props.employee?.typeCode),
);

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) {
      typeValue.value = '';
      return;
    }

    const firstOption = availableTypeOptions.value[0];
    typeValue.value = firstOption ? String(firstOption.value) : '';
  },
);

async function handleSubmit() {
  if (!props.employee?.employeeId) {
    return;
  }
  if (!typeValue.value) {
    toast.error('변경할 고용유형을 선택해주세요.');
    return;
  }

  isSubmitting.value = true;
  try {
    await convertEmploymentTypeMutation.mutateAsync({
      employeeId: props.employee.employeeId,
      type: typeValue.value,
    });
    toast.success('고용유형 변경이 완료되었습니다.');
    emit('converted');
    emit('update:open', false);
  } catch (error) {
    const message =
      error instanceof HttpError ? error.message : '고용유형 변경 중 오류가 발생했습니다.';
    toast.error('고용유형 변경에 실패했습니다.', { description: message });
  } finally {
    isSubmitting.value = false;
  }
}
</script>
