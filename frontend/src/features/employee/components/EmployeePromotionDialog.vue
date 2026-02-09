<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-[425px]">
      <DialogHeader>
        <DialogTitle>직원 승진 발령</DialogTitle>
        <DialogDescription>
          {{ employee?.name }} 님의 새로운 직급과 직책을 선택해 주세요.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-6 py-4">
        <!-- 현재 정보 요약 -->
        <div class="flex items-center justify-between rounded-lg border p-3 bg-muted/50">
          <div class="space-y-0.5">
            <span class="text-xs text-muted-foreground">현재 직급</span>
            <p class="text-sm font-medium">{{ employee?.gradeLabel || '-' }}</p>
          </div>
          <div class="h-8 w-px bg-border"></div>
          <div class="space-y-0.5 text-right">
            <span class="text-xs text-muted-foreground">현재 직책</span>
            <p class="text-sm font-medium">{{ employee?.positionLabel || '-' }}</p>
          </div>
        </div>

        <div class="space-y-4">
          <div class="space-y-2">
            <Label>변경할 직급</Label>
            <Select v-model="form.gradeCode">
              <SelectTrigger>
                <SelectValue placeholder="직급 선택" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in gradeOptions"
                  :key="option.value"
                  :value="String(option.value)"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div class="space-y-2">
            <Label>변경할 직책</Label>
            <Select v-model="form.positionCode">
              <SelectTrigger>
                <SelectValue placeholder="직책 선택" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in positionOptions"
                  :key="option.value"
                  :value="String(option.value)"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="$emit('update:open', false)">취소</Button>
        <Button @click="handleSubmit" :disabled="isSubmitting">
          {{ isSubmitting ? '처리 중...' : '승진 처리' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue';
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
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';

const props = defineProps<{
  open: boolean;
  employee?: EmployeeSummary | null;
  gradeOptions: EmployeeFilterOption[];
  positionOptions: EmployeeFilterOption[];
}>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'promoted'): void;
}>();

const repository = appContainer.resolve(EmployeeRepository);
const isSubmitting = ref(false);

const form = reactive({
  gradeCode: '',
  positionCode: '',
});

// 다이얼로그가 열릴 때 현재 값으로 초기화
watch(
  () => props.open,
  (isOpen) => {
    if (isOpen && props.employee) {
      form.gradeCode = props.employee.gradeCode || '';
      form.positionCode = props.employee.positionCode || '';
    }
  },
);

async function handleSubmit() {
  if (!props.employee?.employeeId) return;
  if (!form.gradeCode && !form.positionCode) {
    toast.error('변경할 직급이나 직책을 선택해주세요.');
    return;
  }

  isSubmitting.value = true;
  try {
    await repository.update(props.employee.employeeId, {
      gradeCode: form.gradeCode,
      positionCode: form.positionCode,
      // 기존 데이터 유지 (API 스펙에 따라 필요할 수 있음)
      name: props.employee.name,
      email: props.employee.email,
      departmentId: props.employee.departmentId,
      typeCode: props.employee.typeCode,
      statusCode: props.employee.statusCode,
      joinDate: props.employee.joinDate,
    });

    toast.success('승진 처리가 완료되었습니다.');
    emit('promoted');
    emit('update:open', false);
  } catch (error) {
    console.error(error);
    toast.error('승진 처리 중 오류가 발생했습니다.');
  } finally {
    isSubmitting.value = false;
  }
}
</script>
