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
            <div class="space-y-6 px-6 py-4">
              <Alert v-if="errorMessage" variant="destructive" class="mb-4">
                <AlertTitle>{{
                  isEditMode ? '수정에 실패했습니다' : '등록에 실패했습니다'
                }}</AlertTitle>
                <AlertDescription>{{ errorMessage }}</AlertDescription>
              </Alert>

              <!-- 기본 정보 -->
              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">기본 정보</h3>
                  <p class="text-sm text-muted-foreground">프로젝트의 기본 정보를 입력하세요.</p>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <FormField name="code" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        프로젝트 코드
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          :model-value="field.value"
                          placeholder="PROJ-2024-001"
                          :disabled="isSubmitting"
                          @update:model-value="handleChange"
                          @blur="handleBlur"
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="name" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        프로젝트명
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          :model-value="field.value"
                          placeholder="ERP 시스템 구축"
                          :disabled="isSubmitting"
                          @update:model-value="handleChange"
                          @blur="handleBlur"
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="partyId" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem class="md:col-span-2">
                      <FormLabel>
                        협력사
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <div class="flex items-center gap-2">
                          <Select
                            :model-value="field.value"
                            class="flex-1"
                            :disabled="partyOptions.length === 0 || isSubmitting"
                            @update:model-value="(value) => handleChange(value ?? 0)"
                            @blur="handleBlur"
                          >
                            <SelectTrigger class="w-full">
                              <SelectValue placeholder="협력사를 선택하세요" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem
                                v-for="option in partyOptions"
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
                            @click="handlePartySelectButton(field.value, handleChange, handleBlur)"
                          >
                            <Building class="h-4 w-4" />
                            <span>협력사</span>
                          </Button>
                        </div>
                      </FormControl>
                      <FormDescription>협력사 목록에서 선택하세요.</FormDescription>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="description" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem class="md:col-span-2">
                      <FormLabel>설명</FormLabel>
                      <FormControl>
                        <Textarea
                          :model-value="field.value"
                          rows="3"
                          placeholder="프로젝트에 대한 설명을 입력하세요"
                          :disabled="isSubmitting"
                          @update:model-value="handleChange"
                          @blur="handleBlur"
                        />
                      </FormControl>
                      <FormDescription>최대 500자까지 입력할 수 있습니다.</FormDescription>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>
                </div>
              </section>

              <Separator />

              <!-- 계약 정보 -->
              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">계약 정보</h3>
                  <p class="text-sm text-muted-foreground">계약 금액과 상태를 입력하세요.</p>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <FormField name="status" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        상태
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
                            <SelectValue placeholder="상태를 선택하세요" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem
                              v-for="option in statusOptions"
                              :key="option.value"
                              :value="option.value"
                            >
                              {{ option.label }}
                            </SelectItem>
                          </SelectContent>
                        </Select>
                      </FormControl>
                      <FormDescription>현재 프로젝트의 진행 상태를 선택하세요.</FormDescription>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="contractAmount" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        계약금액
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <MoneyInput
                          :model-value="toNumber(field.value)"
                          placeholder="580,000,000"
                          :disabled="isSubmitting"
                          @update:model-value="handleChange"
                          @blur="handleBlur"
                        />
                      </FormControl>
                      <FormDescription>원 단위로 입력하세요.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  </FormField>
                </div>
              </section>

              <Separator />

              <!-- 기간 정보 -->
              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">프로젝트 기간</h3>
                  <p class="text-sm text-muted-foreground">시작일과 종료일을 정확히 입력하세요.</p>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <FormField name="startDate" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        시작일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <DatePicker
                          :model-value="toDateValue(field.value)"
                          placeholder="시작일을 선택하세요"
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

                  <FormField name="endDate" v-slot="{ field, handleChange, handleBlur }">
                    <FormItem>
                      <FormLabel>
                        종료일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <DatePicker
                          :model-value="toDateValue(field.value)"
                          placeholder="종료일을 선택하세요"
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
  <PartySelectDialog
    :open="isPartySelectOpen"
    :selected-party-id="selectedPartyIdForDialog"
    @update:open="isPartySelectOpen = $event"
    @select="handlePartySelected"
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
import { DatePicker } from '@/components/ui/date-picker';
import { Separator } from '@/components/ui/separator';
import { appContainer } from '@/core/di/container';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import PartyRepository from '@/features/party/repository/PartyRepository';
import { MoneyInput } from '@/components/business';
import PartySelectDialog from '@/features/party/components/PartySelectDialog.vue';
import { Building } from 'lucide-vue-next';
import HttpError from '@/core/http/HttpError';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import { toast } from 'vue-sonner';

const props = withDefaults(
  defineProps<{
    open: boolean;
    mode?: 'create' | 'edit';
    project?: ProjectDetail | null;
  }>(),
  {
    mode: 'create',
    project: null,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'created'): void;
  (event: 'updated'): void;
}>();

const repository = appContainer.resolve(ProjectRepository);
const partyRepository = appContainer.resolve(PartyRepository);

const schema = z.object({
  partyId: z.number({ required_error: '협력사를 선택하세요.' }).min(1, '협력사를 선택하세요.'),
  code: z
    .string({ required_error: '프로젝트 코드를 입력하세요.' })
    .min(1, '프로젝트 코드를 입력하세요.')
    .max(50, '프로젝트 코드는 50자 이내로 입력하세요.'),
  name: z
    .string({ required_error: '프로젝트명을 입력하세요.' })
    .min(1, '프로젝트명을 입력하세요.')
    .max(100, '프로젝트명은 100자 이내로 입력하세요.'),
  description: z.string().max(500, '설명은 500자 이내로 입력하세요.').optional(),
  status: z.string({ required_error: '상태를 선택하세요.' }).min(1, '상태를 선택하세요.'),
  contractAmount: z
    .number({ required_error: '계약금액을 입력하세요.' })
    .min(0, '계약금액은 0 이상이어야 합니다.'),
  startDate: z
    .date({ required_error: '시작일을 선택하세요.' })
    .nullable()
    .refine((value): value is Date => value instanceof Date, '시작일을 선택하세요.'),
  endDate: z
    .date({ required_error: '종료일을 선택하세요.' })
    .nullable()
    .refine((value): value is Date => value instanceof Date, '종료일을 선택하세요.'),
});

const formSchema = toTypedSchema(schema);
const initialValues = {
  partyId: 0,
  code: '',
  name: '',
  description: '',
  status: '',
  contractAmount: 0,
  startDate: null as Date | null,
  endDate: null as Date | null,
};

const formKey = ref(0);
const formInitialValues = ref({ ...initialValues });
const errorMessage = ref<string | null>(null);
const isPartySelectOpen = ref(false);
const selectedPartyIdForDialog = ref<number | undefined>();
const applyPartySelection = ref<((value: number) => void) | null>(null);
const statusOptions = ref<{ value: string; label: string }[]>([]);
const partyOptions = ref<{ value: number; label: string }[]>([]);

const isEditMode = computed(() => props.mode === 'edit');
const dialogTitle = computed(() => (isEditMode.value ? '프로젝트 편집' : '프로젝트 추가'));
const dialogDescription = computed(() =>
  isEditMode.value
    ? '필요한 정보를 수정한 뒤 저장하세요.'
    : '필수 정보를 입력해 신규 프로젝트를 등록하세요.',
);

watch(
  [() => props.open, () => props.project],
  ([open, project]) => {
    if (!open) {
      return;
    }

    if (isEditMode.value && !project) {
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

watch(isPartySelectOpen, (next) => {
  if (!next) {
    applyPartySelection.value = null;
    selectedPartyIdForDialog.value = undefined;
  }
});

onMounted(async () => {
  try {
    const [statuses, parties] = await Promise.all([
      repository.fetchStatuses(),
      partyRepository.fetchAll(),
    ]);
    statusOptions.value = statuses;
    partyOptions.value = parties;
  } catch (error) {
    console.error('Failed to fetch options:', error);
    // Fallback to empty array
    statusOptions.value = [];
    partyOptions.value = [];
  }
});

function resetForm() {
  errorMessage.value = null;
  setFormInitialValues(initialValues);
  isPartySelectOpen.value = false;
  selectedPartyIdForDialog.value = undefined;
  applyPartySelection.value = null;
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

function handleCancel() {
  emit('update:open', false);
}

async function onSubmit(rawValues: Record<string, unknown>) {
  errorMessage.value = null;

  const values = rawValues as typeof initialValues;

  const payload = {
    partyId: values.partyId,
    code: values.code.trim(),
    name: values.name.trim(),
    description: values.description?.trim() || '',
    status: values.status,
    contractAmount: values.contractAmount,
    startDate: formatDate(values.startDate),
    endDate: formatDate(values.endDate),
  } as const;

  try {
    if (isEditMode.value && props.project?.projectId) {
      await repository.update(props.project.projectId, payload);
      toast.success('프로젝트 정보를 업데이트했습니다.');
      emit('updated');
    } else {
      await repository.create(payload);
      toast.success('프로젝트를 성공적으로 추가했습니다.');
      emit('created');
    }
    emit('update:open', false);
  } catch (error) {
    const message =
      error instanceof HttpError
        ? error.message
        : isEditMode.value
          ? '프로젝트 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.'
          : '프로젝트 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.';
    errorMessage.value = message;
    toast.error(
      isEditMode.value ? '프로젝트 수정에 실패했습니다.' : '프로젝트 등록에 실패했습니다.',
      {
        description: message,
      },
    );
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

function toDateValue(value: unknown): Date | undefined {
  if (value instanceof Date) {
    return value;
  }
  if (typeof value === 'string' && value.length >= 10) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? undefined : date;
  }
  return undefined;
}

function toNumber(value: unknown): number {
  const parsed = typeof value === "number" ? value : Number(value ?? 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function initializeFormValues() {
  errorMessage.value = null;
  if (isEditMode.value && props.project) {
    setFormInitialValues(mapProjectToFormValues(props.project));
  } else {
    setFormInitialValues(initialValues);
  }
}

function setFormInitialValues(values: typeof initialValues) {
  formInitialValues.value = { ...values };
  formKey.value += 1;
}

function mapProjectToFormValues(project: ProjectDetail): typeof initialValues {
  return {
    partyId: project.partyId,
    code: project.code,
    name: project.name,
    description: project.description || '',
    status: project.status,
    contractAmount: project.contractAmount,
    startDate: toDateValue(project.startDate) || null,
    endDate: toDateValue(project.endDate) || null,
  };
}

function handlePartySelectButton(
  currentValue: unknown,
  onChange?: (value: unknown) => void,
  onBlur?: () => void,
) {
  const currentId = typeof currentValue === 'number' ? currentValue : Number(currentValue) || 0;
  openPartySelect(currentId, (value: number) => {
    onChange?.(value);
    onBlur?.();
  });
}

function openPartySelect(currentPartyId: number, setter: (value: number) => void) {
  selectedPartyIdForDialog.value = currentPartyId || undefined;
  applyPartySelection.value = setter;
  isPartySelectOpen.value = true;
}

function handlePartySelected({ partyId }: { partyId: number }) {
  applyPartySelection.value?.(partyId);
  applyPartySelection.value = null;
  isPartySelectOpen.value = false;
}
</script>
