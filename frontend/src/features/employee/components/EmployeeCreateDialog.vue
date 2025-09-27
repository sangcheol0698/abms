<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="w-full sm:max-w-2xl max-h-[85vh] overflow-y-auto p-0">
      <div class="flex flex-col">
        <DialogHeader class="border-b border-border/60 px-6 py-4">
          <DialogTitle>구성원 추가</DialogTitle>
          <DialogDescription> 필수 정보를 입력해 신규 구성원을 등록하세요. </DialogDescription>
        </DialogHeader>

        <Form
          :key="formKey"
          :initial-values="initialValues"
          :validation-schema="formSchema"
          v-slot="{ handleSubmit, isSubmitting, errors }"
        >
          <form class="flex flex-col" @submit.prevent="handleSubmit(onSubmit)">
            <div class="space-y-8 px-6 py-4">
              <Alert v-if="errorMessage" variant="destructive" class="mb-4">
                <AlertTitle>등록에 실패했습니다</AlertTitle>
                <AlertDescription>{{ errorMessage }}</AlertDescription>
              </Alert>

              <section class="space-y-4">
                <div class="space-y-1">
                  <h3 class="text-lg font-semibold text-foreground">기본 정보</h3>
                  <p class="text-sm text-muted-foreground">
                    구성원의 기본 인적 사항과 소속을 입력하세요.
                  </p>
                </div>
                <div class="grid gap-4 md:grid-cols-2">
                  <FormField name="departmentId" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        소속 부서
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <div class="flex items-center gap-2">
                          <Select
                            v-bind="componentField"
                            class="flex-1"
                            :disabled="departmentOptions.length === 0 || isSubmitting"
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
                              handleDepartmentSelectButton(
                                componentField.value,
                                componentField.onChange,
                                componentField.onBlur,
                              )
                            "
                          >
                            <Building2 class="h-4 w-4" />
                            <span>조직도</span>
                          </Button>
                        </div>
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="name" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        이름
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          v-bind="componentField"
                          placeholder="홍길동"
                          :disabled="isSubmitting"
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="email" v-slot="{ componentField }">
                    <FormItem class="md:col-span-2">
                      <FormLabel>
                        이메일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          v-bind="componentField"
                          placeholder="user@example.com"
                          type="email"
                          autocomplete="email"
                          :disabled="isSubmitting"
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
                  <FormField name="position" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        직책
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Select v-bind="componentField" :disabled="isSubmitting">
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

                  <FormField name="grade" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        등급
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Select v-bind="componentField" :disabled="isSubmitting">
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

                  <FormField name="type" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        근무 유형
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Select v-bind="componentField" :disabled="isSubmitting">
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
                  <FormField name="birthDate" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        생년월일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <DatePicker
                          :model-value="componentField.value ?? null"
                          placeholder="생년월일을 선택하세요"
                          :disabled="isSubmitting"
                          @update:modelValue="
                            (value) => {
                              componentField.onChange?.(value);
                              componentField.onBlur?.();
                            }
                          "
                        />
                      </FormControl>
                      <FormMessage class="sr-only" />
                    </FormItem>
                  </FormField>

                  <FormField name="joinDate" v-slot="{ componentField }">
                    <FormItem>
                      <FormLabel>
                        입사일
                        <span class="ml-0.5 text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <DatePicker
                          :model-value="componentField.value ?? null"
                          placeholder="입사일을 선택하세요"
                          :disabled="isSubmitting"
                          @update:modelValue="
                            (value) => {
                              componentField.onChange?.(value);
                              componentField.onBlur?.();
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
                    업무 배치나 전달 사항이 있다면 기록해 주세요.
                  </p>
                </div>
                <FormField name="memo" v-slot="{ componentField }">
                  <FormItem>
                    <FormLabel>메모</FormLabel>
                    <FormControl>
                      <Textarea
                        v-bind="componentField"
                        rows="4"
                        placeholder="업무 배치나 특이사항을 입력하세요"
                        :disabled="isSubmitting"
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
                    <span>등록</span>
                  </Button>
                </div>
              </DialogFooter>
            </div>
          </form>
        </Form>
      </div>
    </DialogContent>
  </Dialog>
  <OrganizationSelectDialog
    :open="isDepartmentSelectOpen"
    :selected-department-id="selectedDepartmentIdForDialog"
    @update:open="isDepartmentSelectOpen = $event"
    @select="handleDepartmentSelected"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
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
import { Building2 } from 'lucide-vue-next';
import { DatePicker } from '@/components/ui/date-picker';
import { Separator } from '@/components/ui/separator';
import { appContainer } from '@/core/di/container';
import EmployeeRepository from '@/features/employee/repository/EmployeeRepository';
import HttpError from '@/core/http/HttpError';
import {
  EMPLOYEE_GRADE_OPTIONS,
  EMPLOYEE_POSITION_OPTIONS,
  EMPLOYEE_TYPE_OPTIONS,
} from '@/features/employee/models/employeeFilters';
import { toast } from 'vue-sonner';
import OrganizationSelectDialog from '@/features/organization/components/OrganizationSelectDialog.vue';

interface DepartmentOption {
  label: string;
  value: string;
}

const props = defineProps<{
  open: boolean;
  departmentOptions: DepartmentOption[];
}>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'created'): void;
}>();

const repository = appContainer.resolve(EmployeeRepository);

const schema = z.object({
  departmentId: z.string({ required_error: '부서를 선택하세요.' }).min(1, '부서를 선택하세요.'),
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
  memo: z.string().max(500, '메모는 500자 이내로 입력하세요.').optional(),
});

const formSchema = toTypedSchema(schema);
const initialValues = {
  departmentId: '',
  name: '',
  email: '',
  joinDate: null as Date | null,
  birthDate: null as Date | null,
  position: '',
  grade: '',
  type: '',
  memo: '',
};

const formKey = ref(0);
const errorMessage = ref<string | null>(null);
const departmentOptions = computed(() => props.departmentOptions ?? []);
const isDepartmentSelectOpen = ref(false);
const selectedDepartmentIdForDialog = ref('');
const applyDepartmentSelection = ref<((value: string) => void) | null>(null);

const gradeOptions = EMPLOYEE_GRADE_OPTIONS;
const positionOptions = EMPLOYEE_POSITION_OPTIONS;
const typeOptions = EMPLOYEE_TYPE_OPTIONS;

watch(
  () => props.open,
  (next, prev) => {
    if (prev && !next) {
      resetForm();
    }
  },
);

watch(isDepartmentSelectOpen, (next) => {
  if (!next) {
    applyDepartmentSelection.value = null;
    selectedDepartmentIdForDialog.value = '';
  }
});

function resetForm() {
  errorMessage.value = null;
  formKey.value += 1;
  isDepartmentSelectOpen.value = false;
  selectedDepartmentIdForDialog.value = '';
  applyDepartmentSelection.value = null;
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

function handleCancel() {
  emit('update:open', false);
}

function handleDepartmentSelectButton(
  currentValue: unknown,
  onChange?: (value: string) => void,
  onBlur?: () => void,
) {
  const currentId =
    typeof currentValue === 'string' ? currentValue : currentValue ? String(currentValue) : '';
  openDepartmentSelect(currentId, (value: string) => {
    onChange?.(value);
    onBlur?.();
  });
}

function openDepartmentSelect(currentDepartmentId: string, setter: (value: string) => void) {
  selectedDepartmentIdForDialog.value = currentDepartmentId ?? '';
  applyDepartmentSelection.value = setter;
  isDepartmentSelectOpen.value = true;
}

function handleDepartmentSelected({ departmentId }: { departmentId: string }) {
  applyDepartmentSelection.value?.(departmentId);
  applyDepartmentSelection.value = null;
  isDepartmentSelectOpen.value = false;
}

async function onSubmit(values: typeof initialValues) {
  errorMessage.value = null;

  try {
    await repository.create({
      departmentId: values.departmentId,
      name: values.name.trim(),
      email: values.email.trim(),
      joinDate: formatDate(values.joinDate),
      birthDate: formatDate(values.birthDate),
      position: values.position,
      grade: values.grade,
      type: values.type,
      memo: values.memo?.trim() ?? '',
    });
    toast.success('구성원을 성공적으로 추가했습니다.');
    emit('created');
    emit('update:open', false);
  } catch (error) {
    const message =
      error instanceof HttpError
        ? error.message
        : '구성원 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.';
    errorMessage.value = message;
    toast.error('구성원 등록에 실패했습니다.', {
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
</script>
