<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="w-full sm:max-w-2xl max-h-[85vh] overflow-y-auto p-0">
      <div class="flex flex-col">
        <DialogHeader class="border-b border-border/60 px-6 py-4">
          <DialogTitle>{{ dialogTitle }}</DialogTitle>
          <DialogDescription>{{ dialogDescription }}</DialogDescription>
        </DialogHeader>

        <Form
          :key="formKey"
          :initial-values="formInitialValues"
          :validation-schema="formSchema"
          v-slot="{ handleSubmit, isSubmitting, errors }"
        >
          <form class="flex flex-col" @submit.prevent="handleSubmit(onSubmit)">
            <div class="space-y-8 px-6 py-4">
              <Alert v-if="errorMessage" variant="destructive" class="mb-4">
                <AlertTitle>{{
                  isEditMode ? '수정에 실패했습니다' : '등록에 실패했습니다'
                }}</AlertTitle>
                <AlertDescription>{{ errorMessage }}</AlertDescription>
              </Alert>

              <FormField name="avatar" v-slot="{ field, handleChange }">
                <FormItem>
                  <FormLabel class="text-sm font-semibold text-muted-foreground">
                    아바타
                    <span class="ml-0.5 text-destructive">*</span>
                  </FormLabel>
                  <FormControl>
                    <div
                      class="flex flex-col gap-4 rounded-2xl border border-border/70 bg-muted/10 p-4 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div class="flex items-center gap-4">
                        <Avatar class="h-20 w-20 border border-border/60 bg-background">
                          <AvatarImage
                            :src="getAvatarOptionForDisplay(field.value)?.imageUrl ?? ''"
                            :alt="getAvatarOptionForDisplay(field.value)?.label ?? '선택된 아바타'"
                          />
                          <AvatarFallback class="text-base font-semibold">
                            {{ getAvatarFallbackLabel(field.value) }}
                          </AvatarFallback>
                        </Avatar>
                        <div class="space-y-1">
                          <p class="text-base font-semibold text-foreground">
                            {{ getAvatarOptionForDisplay(field.value)?.label ?? '아바타 미선택' }}
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
                        :disabled="isSubmitting || isLoadingAvatars"
                        @click="
                          openAvatarSelect(field.value, (value) => {
                            handleChange(value);
                          })
                        "
                      >
                        아바타 선택
                      </Button>
                    </div>
                  </FormControl>
                  <FormMessage class="sr-only" />
                </FormItem>
              </FormField>

              <section class="mt-4 space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">기본 정보</h3>
                  <p class="text-sm text-muted-foreground">
                    직원의 기본 인적 사항과 소속을 입력하세요.
                  </p>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <FormField name="name" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        이름
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          :model-value="field.value"
                          placeholder="홍길동"
                          :disabled="isSubmitting"
                          @update:model-value="handleChange"
                          @blur="handleBlur"
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="departmentId" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        소속 부서
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <div class="flex items-center gap-2">
                          <Select
                            :model-value="field.value"
                            class="flex-1"
                            :disabled="departmentOptions.length === 0 || isSubmitting"
                            @update:model-value="(value) => handleChange(value ?? 0)"
                            @blur="handleBlur"
                          >
                            <SelectTrigger class="w-full">
                              <SelectValue placeholder="부서를 선택하세요" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem
                                v-for="option in departmentOptions"
                                :key="option.value"
                                :value="option.value"
                              >
                                {{ option.label }}
                              </SelectItem>
                            </SelectContent>
                          </Select>
                          <Button
                            type="button"
                            variant="outline"
                            class="h-9 gap-2 whitespace-nowrap px-3"
                            :disabled="isSubmitting"
                            @click="
                              handleDepartmentSelectButton(field.value, handleChange, handleBlur)
                            "
                          >
                            <Building2 class="h-4 w-4" />
                            <span>부서</span>
                          </Button>
                        </div>
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="email" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem class="md:col-span-2">
                      <FormLabel>
                        이메일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          :model-value="field.value"
                          placeholder="user@example.com"
                          type="email"
                          autocomplete="email"
                          :disabled="isSubmitting"
                          @update:model-value="handleChange"
                          @blur="handleBlur"
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>
                </div>
              </section>

              <Separator />

              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">직무 정보</h3>
                  <p class="text-sm text-muted-foreground">역할과 등급, 근무 유형을 선택하세요.</p>
                </div>
                <div class="grid gap-4 md:grid-cols-3">
                  <FormField name="position" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        직책
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Select
                          :model-value="field.value"
                          :disabled="isSubmitting"
                          @update:model-value="(value) => handleChange(value ?? '')"
                          @blur="handleBlur"
                        >
                          <SelectTrigger class="w-full">
                            <SelectValue placeholder="선택" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem
                              v-for="option in positionOptions"
                              :key="option.value"
                              :value="option.value"
                            >
                              {{ option.label }}
                            </SelectItem>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="grade" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        등급
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Select
                          :model-value="field.value"
                          :disabled="isSubmitting"
                          @update:model-value="(value) => handleChange(value ?? '')"
                          @blur="handleBlur"
                        >
                          <SelectTrigger class="w-full">
                            <SelectValue placeholder="선택" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem
                              v-for="option in gradeOptions"
                              :key="option.value"
                              :value="option.value"
                            >
                              {{ option.label }}
                            </SelectItem>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="type" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        근무 유형
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Select
                          :model-value="field.value"
                          :disabled="isSubmitting"
                          @update:model-value="(value) => handleChange(value ?? '')"
                          @blur="handleBlur"
                        >
                          <SelectTrigger class="w-full">
                            <SelectValue placeholder="선택" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem
                              v-for="option in typeOptions"
                              :key="option.value"
                              :value="option.value"
                            >
                              {{ option.label }}
                            </SelectItem>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>
                </div>
              </section>

              <Separator />

              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">날짜 정보</h3>
                  <p class="text-sm text-muted-foreground">
                    입사일과 생년월일을 정확히 입력하세요.
                  </p>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <FormField name="birthDate" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        생년월일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <DatePicker
                          :model-value="toDateValue(field.value)"
                          placeholder="생년월일을 선택하세요"
                          :disabled="isSubmitting"
                          @update:modelValue="
                            (value) => {
                              handleChange(value ?? null);
                              handleBlur();
                            }
                          "
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="joinDate" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        입사일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <DatePicker
                          :model-value="toDateValue(field.value)"
                          placeholder="입사일을 선택하세요"
                          :disabled="isSubmitting"
                          @update:modelValue="
                            (value) => {
                              handleChange(value ?? null);
                              handleBlur();
                            }
                          "
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>
                </div>
              </section>

              <Separator />

              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">추가 메모</h3>
                  <p class="text-sm text-muted-foreground">
                    업무 배치나 특이사항 등 추가로 남기고 싶은 내용을 입력하세요.
                  </p>
                </div>
                <FormField name="memo" v-slot="{ field, handleChange, handleBlur }">
                  <FormItem>
                    <FormLabel>메모</FormLabel>
                    <FormControl>
                      <Textarea
                        :model-value="field.value"
                        rows="4"
                        placeholder="업무 배치나 특이사항을 입력하세요"
                        :disabled="isSubmitting"
                        @update:model-value="handleChange"
                        @blur="handleBlur"
                      />
                    </FormControl>
                    <FormDescription>최대 500자까지 입력할 수 있습니다.</FormDescription>
                    <FormMessage class="sr-only" />
                  </FormItem>
                </FormField>
              </section>

              <div
                v-if="
                  Object.values(errors).some(
                    (message) => typeof message === 'string' && message.length > 0,
                  )
                "
                class="border-t border-destructive/20 bg-destructive/10 px-6 py-3 text-xs text-destructive"
              >
                <p class="mb-1 font-semibold">입력값을 확인해 주세요:</p>
                <ul class="space-y-1 pl-4">
                  <template v-for="(message, key) in errors" :key="key">
                    <li
                      v-if="typeof message === 'string' && message.length > 0"
                      class="list-disc leading-tight"
                    >
                      {{ message }}
                    </li>
                  </template>
                </ul>
              </div>

              <DialogFooter class="border-t border-border/60 py-4">
                <div class="ml-auto flex items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    :disabled="isSubmitting"
                    @click="handleCancel"
                  >
                    취소
                  </Button>
                  <Button type="submit" :disabled="isSubmitting" class="gap-2">
                    <span>{{ isEditMode ? '저장' : '등록' }}</span>
                  </Button>
                </div>
              </DialogFooter>
            </div>
          </form>
        </Form>
      </div>
    </DialogContent>
  </Dialog>
  <EmployeeAvatarSelectDialog
    :open="isAvatarSelectOpen"
    :options="avatarOptions"
    :selected-avatar-code="selectedAvatarCodeForDialog"
    @update:open="handleAvatarDialogOpenChange"
    @select="handleAvatarSelected"
  />
  <DepartmentSelectDialog
    :open="isDepartmentSelectOpen"
    :selected-department-id="selectedDepartmentIdForDialog"
    @update:open="isDepartmentSelectOpen = $event"
    @select="handleDepartmentSelected"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { z } from 'zod';
import { toTypedSchema } from '@vee-validate/zod';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Building2 } from 'lucide-vue-next';
import { DatePicker } from '@/components/ui/date-picker';
import { Separator } from '@/components/ui/separator';
import { appContainer } from '@/core/di/container';
import { EmployeeRepository } from '@/features/employee/repository/EmployeeRepository';
import HttpError from '@/core/http/HttpError';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import {
  getEmployeeGradeOptions,
  getEmployeePositionOptions,
  getEmployeeTypeOptions,
} from '@/features/employee/models/employeeFilters';
import { toast } from 'vue-sonner';
import DepartmentSelectDialog from '@/features/department/components/DepartmentSelectDialog.vue';
import EmployeeAvatarSelectDialog from '@/features/employee/components/EmployeeAvatarSelectDialog.vue';
import type {
  EmployeeAvatarOption,
  EmployeeAvatarCode,
} from '@/features/employee/constants/avatars';
import {
  defaultEmployeeAvatar,
  getEmployeeAvatarOptions,
} from '@/features/employee/constants/avatars';

interface DepartmentOption {
  label: string;
  value: number;
}

const props = withDefaults(
  defineProps<{
    open: boolean;
    departmentOptions: DepartmentOption[];
    mode?: 'create' | 'edit';
    employee?: EmployeeSummary | null;
    gradeOptions?: EmployeeFilterOption[];
    positionOptions?: EmployeeFilterOption[];
    typeOptions?: EmployeeFilterOption[];
  }>(),
  {
    mode: 'create',
    employee: null,
    gradeOptions: undefined,
    positionOptions: undefined,
    typeOptions: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'created'): void;
  (event: 'updated'): void;
}>();

const repository = appContainer.resolve(EmployeeRepository);

const schema = z.object({
  departmentId: z
    .number({ required_error: '부서를 선택하세요.' })
    .min(1, '부서를 선택하세요.'),
  name: z
    .string({ required_error: '이름을 입력하세요.' })
    .min(1, '이름을 입력하세요.')
    .max(10, '이름은 10자 이내로 입력하세요.'),
  email: z
    .string({ required_error: '이메일을 입력하세요.' })
    .min(1, '이메일을 입력하세요.')
    .email('올바른 이메일을 입력하세요.'),
  joinDate: z
    .date({ required_error: '입사일을 선택하세요.' })
    .nullable()
    .refine((value): value is Date => value instanceof Date, '입사일을 선택하세요.'),
  birthDate: z
    .date({ required_error: '생년월일을 선택하세요.' })
    .nullable()
    .refine((value): value is Date => value instanceof Date, '생년월일을 선택하세요.'),
  position: z.string({ required_error: '직책을 선택하세요.' }).min(1, '직책을 선택하세요.'),
  grade: z.string({ required_error: '등급을 선택하세요.' }).min(1, '등급을 선택하세요.'),
  type: z.string({ required_error: '근무 유형을 선택하세요.' }).min(1, '근무 유형을 선택하세요.'),
  avatar: z.string({ required_error: '아바타를 선택하세요.' }).min(1, '아바타를 선택하세요.'),
  memo: z.string().max(500, '메모는 500자 이내로 입력하세요.').optional(),
});

const formSchema = toTypedSchema(schema);
const initialValues = {
  departmentId: 0,
  name: '',
  email: '',
  joinDate: null as Date | null,
  birthDate: null as Date | null,
  position: '',
  grade: '',
  type: '',
  avatar: defaultEmployeeAvatar,
  memo: '',
};

const formKey = ref(0);
const formInitialValues = ref({ ...initialValues });
const errorMessage = ref<string | null>(null);
const departmentOptions = computed(() => props.departmentOptions ?? []);
const isDepartmentSelectOpen = ref(false);
const selectedDepartmentIdForDialog = ref<number | undefined>();
const applyDepartmentSelection = ref<((value: number) => void) | null>(null);

const gradeOptions = computed(() => props.gradeOptions ?? getEmployeeGradeOptions());
const positionOptions = computed(() => props.positionOptions ?? getEmployeePositionOptions());
const typeOptions = computed(() => props.typeOptions ?? getEmployeeTypeOptions());
const avatarOptions = ref<EmployeeAvatarOption[]>(getEmployeeAvatarOptions());
const isLoadingAvatars = ref(false);
const isAvatarSelectOpen = ref(false);
const avatarSelectionResolver = ref<((value: string) => void) | null>(null);
const selectedAvatarCodeForDialog = ref(defaultEmployeeAvatar);
let hasLoadedAvatarOptions = false;

const isEditMode = computed(() => props.mode === 'edit');
const dialogTitle = computed(() => (isEditMode.value ? '직원 편집' : '직원 추가'));
const dialogDescription = computed(() =>
  isEditMode.value
    ? '필요한 정보를 수정한 뒤 저장하세요.'
    : '필수 정보를 입력해 신규 직원을 등록하세요.',
);

watch(
  [() => props.open, () => props.employee],
  ([open, employee]) => {
    if (!open) {
      return;
    }

    ensureAvatarOptionsLoaded();

    if (isEditMode.value && !employee) {
      return;
    }

    initializeFormValues();
  },
  { deep: true, immediate: true },
);

watch(
  () => props.open,
  (open, prev) => {
    if (!open && prev) {
      resetForm();
    }
  },
);

watch(isDepartmentSelectOpen, (next) => {
  if (!next) {
    applyDepartmentSelection.value = null;
    selectedDepartmentIdForDialog.value = undefined;
  }
});

watch(isAvatarSelectOpen, (next) => {
  if (!next) {
    avatarSelectionResolver.value = null;
  }
});

onMounted(() => {
  ensureAvatarOptionsLoaded();
});

async function ensureAvatarOptionsLoaded() {
  if (hasLoadedAvatarOptions) {
    return;
  }
  isLoadingAvatars.value = true;
  try {
    const options = await repository.fetchAvatars();
    if (Array.isArray(options) && options.length > 0) {
      avatarOptions.value = options;
    }
    selectedAvatarCodeForDialog.value = resolveAvatarCode(selectedAvatarCodeForDialog.value);
  } catch {
    // ignore and fallback to bundled avatars
  } finally {
    hasLoadedAvatarOptions = true;
    isLoadingAvatars.value = false;
  }
}

function resolveAvatarCode(candidate?: string | null): EmployeeAvatarCode {
  if (typeof candidate === 'string' && candidate.length > 0) {
    const exists = avatarOptions.value.some((option) => option.code === candidate);
    if (exists) {
      return candidate as EmployeeAvatarCode;
    }
  }
  return (
    avatarOptions.value.length > 0 && avatarOptions.value[0]
      ? avatarOptions.value[0].code
      : defaultEmployeeAvatar
  ) as EmployeeAvatarCode;
}

function getAvatarOptionForDisplay(value: unknown): EmployeeAvatarOption | null {
  const normalized = resolveAvatarCode(typeof value === 'string' ? value : null);
  return avatarOptions.value.find((option) => option.code === normalized) ?? null;
}

function getAvatarFallbackLabel(value: unknown): string {
  const option = getAvatarOptionForDisplay(value);
  if (!option) {
    return 'AV';
  }
  return option.label.slice(0, 2).toUpperCase();
}

function openAvatarSelect(currentValue: unknown, setter: (value: string) => void) {
  const normalized = resolveAvatarCode(typeof currentValue === 'string' ? currentValue : null);
  selectedAvatarCodeForDialog.value = normalized;
  avatarSelectionResolver.value = setter;
  isAvatarSelectOpen.value = true;
}

function handleAvatarSelected(code: string) {
  const normalized = resolveAvatarCode(code);
  avatarSelectionResolver.value?.(normalized);
  avatarSelectionResolver.value = null;
  selectedAvatarCodeForDialog.value = normalized;
  isAvatarSelectOpen.value = false;
}

function handleAvatarDialogOpenChange(open: boolean) {
  isAvatarSelectOpen.value = open;
  if (!open) {
    avatarSelectionResolver.value = null;
  }
}

function resetForm() {
  errorMessage.value = null;
  setFormInitialValues(initialValues);
  isDepartmentSelectOpen.value = false;
  selectedDepartmentIdForDialog.value = undefined;
  applyDepartmentSelection.value = null;
  isAvatarSelectOpen.value = false;
  avatarSelectionResolver.value = null;
  selectedAvatarCodeForDialog.value = defaultEmployeeAvatar;
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

function handleCancel() {
  emit('update:open', false);
}

function handleDepartmentSelectButton(
  currentValue: unknown,
  onChange?: (value: unknown) => void,
  onBlur?: () => void,
) {
  const currentId =
    typeof currentValue === 'number' ? currentValue : Number(currentValue) || 0;
  openDepartmentSelect(currentId, (value: number) => {
    onChange?.(value);
    onBlur?.();
  });
}

function openDepartmentSelect(currentDepartmentId: number, setter: (value: number) => void) {
  selectedDepartmentIdForDialog.value = currentDepartmentId || undefined;
  applyDepartmentSelection.value = setter;
  isDepartmentSelectOpen.value = true;
}

function handleDepartmentSelected({ departmentId }: { departmentId: number }) {
  applyDepartmentSelection.value?.(departmentId);
  applyDepartmentSelection.value = null;
  isDepartmentSelectOpen.value = false;
}

async function onSubmit(rawValues: Record<string, unknown>) {
  errorMessage.value = null;

  const values = rawValues as typeof initialValues;

  const payload = {
    departmentId: values.departmentId,
    name: values.name.trim(),
    email: values.email.trim(),
    joinDate: formatDate(values.joinDate),
    birthDate: formatDate(values.birthDate),
    position: values.position,
    grade: values.grade,
    type: values.type,
    avatar: resolveAvatarCode(values.avatar),
    memo: values.memo?.trim() ?? '',
  } as const;

  try {
    if (isEditMode.value && props.employee?.employeeId) {
      await repository.update(props.employee.employeeId, payload);
      toast.success('직원 정보를 업데이트했습니다.');
      emit('updated');
    } else {
      await repository.create(payload);
      toast.success('직원을 성공적으로 추가했습니다.');
      emit('created');
    }
    emit('update:open', false);
  } catch (error) {
    const message =
      error instanceof HttpError
        ? error.message
        : isEditMode.value
          ? '직원 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.'
          : '직원 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.';
    errorMessage.value = message;
    toast.error(isEditMode.value ? '직원 수정에 실패했습니다.' : '직원 등록에 실패했습니다.', {
      description: message,
    });
  }
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

function initializeFormValues() {
  errorMessage.value = null;
  if (isEditMode.value && props.employee) {
    setFormInitialValues(mapEmployeeToFormValues(props.employee));
  } else {
    setFormInitialValues(initialValues);
  }
}

function setFormInitialValues(values: typeof initialValues) {
  formInitialValues.value = {
    ...values,
    avatar: resolveAvatarCode(values.avatar),
  };
  selectedAvatarCodeForDialog.value = resolveAvatarCode(values.avatar);
  formKey.value += 1;
}

function mapEmployeeToFormValues(employee: EmployeeSummary): typeof initialValues {
  return {
    departmentId: employee.departmentId ?? 0,
    name: employee.name ?? '',
    email: employee.email ?? '',
    joinDate: parseDate(employee.joinDate),
    birthDate: parseDate(employee.birthDate),
    position: employee.positionCode ?? '',
    grade: employee.gradeCode ?? '',
    type: employee.typeCode ?? '',
    avatar: resolveAvatarCode(employee.avatarCode),
    memo: employee.memo ?? '',
  };
}

function toDateValue(value: unknown): Date | null {
  if (typeof value === 'string' && value.length > 0) {
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return value;
  }

  return null;
}

function parseDate(value?: string | null): Date | null {
  if (!value) {
    return null;
  }
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}
</script>
