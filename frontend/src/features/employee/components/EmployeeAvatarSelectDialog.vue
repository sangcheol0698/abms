<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="w-full max-w-3xl">
      <DialogHeader>
        <DialogTitle>아바타 선택</DialogTitle>
        <DialogDescription>구성원의 프로필에 사용할 아바타를 선택하세요.</DialogDescription>
      </DialogHeader>
      <div class="max-h-[60vh] overflow-y-auto pr-1">
        <EmployeeAvatarPicker
          :model-value="selectedAvatarCode"
          :options="options"
          :disabled="disabled"
          @update:modelValue="handleSelect"
        />
      </div>
      <DialogFooter>
        <Button type="button" variant="outline" @click="emit('update:open', false)">취소</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import EmployeeAvatarPicker from '@/features/employee/components/EmployeeAvatarPicker.vue';
import type { EmployeeAvatarOption } from '@/features/employee/constants/avatars';

const props = withDefaults(
  defineProps<{
    open: boolean;
    options: EmployeeAvatarOption[];
    selectedAvatarCode: string;
    disabled?: boolean;
  }>(),
  {
    open: false,
    options: () => [],
    selectedAvatarCode: '',
    disabled: false,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'select', value: string): void;
}>();

function handleSelect(code: string) {
  if (props.disabled) {
    return;
  }
  emit('select', code);
}
</script>
