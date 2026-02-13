<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-[500px]">
      <DialogHeader>
        <DialogTitle>{{ isEdit ? '매출 일정 수정' : '매출 일정 추가' }}</DialogTitle>
        <DialogDescription> 프로젝트 매출 일정 정보를 입력해 주세요. </DialogDescription>
      </DialogHeader>

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
          <Input id="plannedDate" v-model="form.plannedDate" type="date" class="col-span-3" />
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
        <Button variant="outline" @click="$emit('update:open', false)">취소</Button>
        <Button type="submit" @click="handleSubmit" :disabled="isSubmitting">
          {{ isSubmitting ? '저장 중...' : '저장' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { MoneyInput } from '@/components/business';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { appContainer } from '@/core/di/container';
import ProjectRevenueRepository from '@/features/project/repository/ProjectRevenueRepository';

interface RevenuePlanForm {
  id?: number;
  projectId: number;
  sequence: number;
  type: string;
  plannedDate: string;
  amount: number;
  description: string;
}

interface Props {
  open: boolean;
  plan?: any;
  projectId: number;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:open', 'saved']);

const repository = appContainer.resolve(ProjectRevenueRepository);

const isSubmitting = ref(false);

const defaultForm: RevenuePlanForm = {
  projectId: props.projectId,
  sequence: 1,
  type: 'DOWN_PAYMENT',
  plannedDate: new Date().toISOString().split('T')[0],
  amount: 0,
  description: '',
};

const form = ref<RevenuePlanForm>({ ...defaultForm });

const isEdit = computed(() => !!props.plan);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      if (props.plan) {
        form.value = {
          ...props.plan,
          plannedDate: props.plan.plannedDate ? props.plan.plannedDate.split('T')[0] : '',
        };
      } else {
        form.value = {
          ...defaultForm,
          projectId: props.projectId,
        };
      }
    }
  },
);

async function handleSubmit() {
  isSubmitting.value = true;
  try {
    if (isEdit.value) {
      // TODO: 수정 API 연동
      await new Promise((resolve) => setTimeout(resolve, 500));
      emit('saved', { ...form.value });
    } else {
      await repository.create({
        projectId: form.value.projectId,
        sequence: form.value.sequence,
        revenueDate: form.value.plannedDate,
        type: form.value.type,
        amount: form.value.amount,
        memo: form.value.description || undefined,
      });
      emit('saved', null); // 생성 후 목록 갱신을 위해 null 전달
    }

    emit('update:open', false);
  } catch (error) {
    console.error('Failed to save revenue plan', error);
  } finally {
    isSubmitting.value = false;
  }
}
</script>
