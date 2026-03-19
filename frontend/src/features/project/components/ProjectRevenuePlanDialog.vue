<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-[500px]">
      <DialogHeader>
        <DialogTitle>{{ isEdit ? '매출 일정 수정' : '매출 일정 추가' }}</DialogTitle>
        <DialogDescription> 프로젝트 매출 일정 정보를 입력해 주세요. </DialogDescription>
      </DialogHeader>

      <Alert v-if="errorMessage" variant="destructive">
        <AlertTitle>{{ isEdit ? '매출 일정 수정' : '매출 일정 추가' }}에 실패했습니다.</AlertTitle>
        <AlertDescription>{{ errorMessage }}</AlertDescription>
      </Alert>

      <div class="grid gap-4 py-4">
        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="sequence" class="text-right">회차</Label>
          <Input
            id="sequence"
            v-model.number="form.sequence"
            type="number"
            class="col-span-3"
            placeholder="예: 1"
            min="1"
          />
        </div>

        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="type" class="text-right">구분</Label>
          <Select v-model="form.type">
            <SelectTrigger class="col-span-3">
              <SelectValue placeholder="구분 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="DOWN_PAYMENT">착수금</SelectItem>
              <SelectItem value="INTERMEDIATE_PAYMENT">중도금</SelectItem>
              <SelectItem value="BALANCE_PAYMENT">잔금</SelectItem>
              <SelectItem value="MAINTENANCE">유지보수</SelectItem>
              <SelectItem value="ETC">기타</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="plannedDate" class="text-right">예정일</Label>
          <DatePicker
            id="plannedDate"
            :model-value="plannedDateValue"
            class-name="col-span-3"
            :disabled="isSubmitting"
            placeholder="예정일을 선택하세요"
            @update:modelValue="handlePlannedDateChange"
          />
        </div>

        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="amount" class="text-right">금액</Label>
          <MoneyInput
            id="amount"
            v-model="form.amount"
            class="col-span-3"
            placeholder="금액을 입력하세요"
          />
        </div>

        <div class="grid grid-cols-4 items-start gap-4">
          <Label for="description" class="text-right pt-2">비고</Label>
          <Textarea
            id="description"
            v-model="form.description"
            class="col-span-3"
            placeholder="비고 사항을 입력하세요"
            rows="3"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleOpenChange(false)">취소</Button>
        <Button type="submit" @click="handleSubmit" :disabled="isSubmitting">
          {{ isSubmitting ? '저장 중...' : '저장' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { toast } from 'vue-sonner';
import { MoneyInput } from '@/components/business';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { DatePicker } from '@/components/ui/date-picker';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import HttpError from '@/core/http/HttpError';
import {
  useCreateProjectRevenuePlanMutation,
  useUpdateProjectRevenuePlanMutation,
} from '@/features/project/queries/useProjectQueries';
import type { ProjectRevenuePlanResponse } from '@/features/project/repository/ProjectRevenueRepository';

interface RevenuePlanForm {
  projectId: number;
  sequence: number;
  type: string;
  plannedDate: string;
  amount: number;
  description: string;
}

interface Props {
  open: boolean;
  plan?: ProjectRevenuePlanResponse | null;
  projectId: number;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'saved'): void;
}>();

const createRevenuePlanMutation = useCreateProjectRevenuePlanMutation();
const updateRevenuePlanMutation = useUpdateProjectRevenuePlanMutation();
const plannedDateValue = ref<Date | null>(null);
const errorMessage = ref<string | null>(null);

const defaultForm: RevenuePlanForm = {
  projectId: props.projectId,
  sequence: 1,
  type: 'DOWN_PAYMENT',
  plannedDate: new Date().toISOString().slice(0, 10),
  amount: 0,
  description: '',
};

const form = ref<RevenuePlanForm>({ ...defaultForm });
const isEdit = computed(() => !!props.plan);
const isSubmitting = computed(
  () => createRevenuePlanMutation.isPending.value || updateRevenuePlanMutation.isPending.value,
);

watch(
  () => props.open,
  (isOpen) => {
    if (!isOpen) {
      errorMessage.value = null;
      return;
    }

    errorMessage.value = null;

    if (props.plan) {
      form.value = {
        projectId: props.plan.projectId,
        sequence: props.plan.sequence,
        type: props.plan.type,
        plannedDate: props.plan.revenueDate,
        amount: props.plan.amount,
        description: props.plan.memo ?? '',
      };
    } else {
      form.value = {
        ...defaultForm,
        projectId: props.projectId,
      };
    }

    plannedDateValue.value = toDateValue(form.value.plannedDate);
  },
  { immediate: true },
);

async function handleSubmit() {
  errorMessage.value = null;

  try {
    if (isEdit.value) {
      await updateRevenuePlanMutation.mutateAsync({
        projectId: props.projectId,
        sequence: props.plan?.sequence ?? form.value.sequence,
        payload: {
          sequence: form.value.sequence,
          revenueDate: form.value.plannedDate,
          type: form.value.type,
          amount: form.value.amount,
          memo: form.value.description || undefined,
        },
      });
      toast.success('매출 일정을 수정했습니다.');
    } else {
      await createRevenuePlanMutation.mutateAsync({
        projectId: form.value.projectId,
        sequence: form.value.sequence,
        revenueDate: form.value.plannedDate,
        type: form.value.type,
        amount: form.value.amount,
        memo: form.value.description || undefined,
      });
      toast.success('매출 일정을 추가했습니다.');
    }

    emit('saved');
    handleOpenChange(false);
  } catch (error) {
    errorMessage.value = error instanceof HttpError ? error.message : '매출 일정 저장 중 오류가 발생했습니다.';
  }
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

function toDateValue(value: string | null | undefined): Date | null {
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

function handlePlannedDateChange(value: Date | null) {
  plannedDateValue.value = value;
  form.value.plannedDate = formatDate(value);
}
</script>
