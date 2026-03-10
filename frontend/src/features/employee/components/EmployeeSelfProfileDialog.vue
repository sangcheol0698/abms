<template>
  <Dialog :open="open" @update:open="(value) => emit('update:open', value)">
    <DialogContent class="sm:max-w-xl">
      <DialogHeader>
        <DialogTitle>내 정보 수정</DialogTitle>
        <DialogDescription>
          생년월일과 아바타만 수정할 수 있습니다.
        </DialogDescription>
      </DialogHeader>

      <div v-if="!employee" class="py-6 text-sm text-muted-foreground">
        수정할 직원 정보를 불러오지 못했습니다.
      </div>

      <form v-else class="space-y-5" @submit.prevent="handleSubmit">
        <Alert v-if="errorMessage" variant="destructive">
          <AlertTitle>내 정보 수정 실패</AlertTitle>
          <AlertDescription>{{ errorMessage }}</AlertDescription>
        </Alert>

        <div class="grid gap-4">
          <div class="space-y-2">
            <Label class="text-sm font-semibold text-muted-foreground">
              아바타
              <span class="ml-0.5 text-destructive">*</span>
            </Label>
            <div
              class="flex flex-col gap-4 rounded-2xl border border-border/70 bg-muted/10 p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div class="flex items-center gap-4">
                <Avatar class="h-20 w-20 border border-border/60 bg-background">
                  <AvatarImage
                    :src="selectedAvatar.imageUrl"
                    :alt="selectedAvatar.label || '선택된 아바타'"
                  />
                  <AvatarFallback class="text-base font-semibold">
                    {{ getAvatarFallbackLabel() }}
                  </AvatarFallback>
                </Avatar>
                <div class="space-y-1">
                  <p class="text-base font-semibold text-foreground">
                    {{ selectedAvatar.label || '아바타 미선택' }}
                  </p>
                  <p class="text-sm text-muted-foreground">
                    이 아바타는 직원 목록과 상세 화면에 표시됩니다.
                  </p>
                </div>
              </div>
              <Button
                type="button"
                variant="outline"
                class="self-start sm:self-auto"
                :disabled="isSubmitting || avatarOptions.length === 0"
                @click="openAvatarSelect"
              >
                아바타 선택
              </Button>
            </div>
          </div>

          <div class="space-y-2">
            <Label for="self-profile-birthDate">생년월일</Label>
            <DatePicker
              id="self-profile-birthDate"
              :model-value="toDateValue(birthDate)"
              placeholder="생년월일을 선택하세요"
              :disabled="isSubmitting"
              @update:modelValue="(value) => { birthDate = formatDate(value); }"
            />
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" :disabled="isSubmitting" @click="emit('update:open', false)">
            취소
          </Button>
          <Button type="submit" :disabled="isSubmitting || !canSubmit">
            {{ isSubmitting ? '저장 중...' : '저장' }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>

  <EmployeeAvatarSelectDialog
    :open="isAvatarDialogOpen"
    :options="avatarOptions"
    :selected-avatar-code="avatarCode"
    @update:open="handleAvatarDialogOpenChange"
    @select="handleAvatarSelected"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
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
import type { EmployeeSummary } from '@/features/employee/models/employee';
import type { EmployeeAvatarOption } from '@/features/employee/constants/avatars';
import EmployeeAvatarSelectDialog from '@/features/employee/components/EmployeeAvatarSelectDialog.vue';

const props = withDefaults(
  defineProps<{
    open: boolean;
    employee?: EmployeeSummary | null;
    avatarOptions?: EmployeeAvatarOption[];
    isSubmitting?: boolean;
    errorMessage?: string | null;
  }>(),
  {
    employee: null,
    avatarOptions: () => [],
    isSubmitting: false,
    errorMessage: null,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'submit', payload: { birthDate: string; avatar: string }): void;
}>();

const birthDate = ref('');
const avatarCode = ref('');
const isAvatarDialogOpen = ref(false);

watch(
  () => [props.open, props.employee] as const,
  ([open]) => {
    if (!open || !props.employee) {
      return;
    }

    birthDate.value = props.employee.birthDate;
    avatarCode.value = props.employee.avatarCode;
  },
  { immediate: true },
);

const selectedAvatar = computed(() => {
  return (
    props.avatarOptions.find((option) => option.code === avatarCode.value) ?? props.avatarOptions[0] ?? {
      code: '',
      label: '아바타 없음',
      imageUrl: '',
    }
  );
});

const canSubmit = computed(() => {
  return birthDate.value.trim().length > 0 && avatarCode.value.length > 0;
});

function handleSubmit() {
  emit('submit', {
    birthDate: birthDate.value,
    avatar: avatarCode.value,
  });
}

function getAvatarFallbackLabel(): string {
  return selectedAvatar.value.label?.slice(0, 2).toUpperCase() || 'AV';
}

function openAvatarSelect() {
  isAvatarDialogOpen.value = true;
}

function handleAvatarDialogOpenChange(value: boolean) {
  isAvatarDialogOpen.value = value;
}

function handleAvatarSelected(code: string) {
  avatarCode.value = code;
  isAvatarDialogOpen.value = false;
}

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
</script>
