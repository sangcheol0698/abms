<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-[560px]">
      <DialogHeader>
        <DialogTitle>{{ isEdit ? '투입 인력 수정' : '투입 인력 추가' }}</DialogTitle>
        <DialogDescription>직원, 역할, 투입 기간을 입력하세요.</DialogDescription>
      </DialogHeader>

      <Alert v-if="errorMessage" variant="destructive">
        <AlertTitle>{{ isEdit ? '투입 인력 수정' : '투입 인력 추가' }}에 실패했습니다.</AlertTitle>
        <AlertDescription>{{ errorMessage }}</AlertDescription>
      </Alert>

      <div class="grid gap-4 py-4">
        <div class="grid gap-2">
          <Label>직원</Label>
          <div class="flex items-center gap-2">
            <Button variant="outline" class="justify-between" :disabled="isSubmitting" @click="isEmployeeSelectOpen = true">
              <span>{{ selectedEmployeeLabel }}</span>
            </Button>
            <Button
              v-if="selectedEmployeeId"
              variant="ghost"
              size="sm"
              :disabled="isSubmitting"
              @click="clearEmployeeSelection"
            >
              선택 해제
            </Button>
          </div>
        </div>

        <div class="grid gap-2">
          <Label for="assignmentRole">역할</Label>
          <Select :model-value="role ?? undefined" :disabled="isSubmitting" @update:model-value="updateRole">
            <SelectTrigger id="assignmentRole">
              <SelectValue placeholder="역할을 선택하세요" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                v-for="option in projectAssignmentRoleOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <div class="grid gap-2 sm:grid-cols-2">
          <div class="grid gap-2">
            <Label for="startDate">투입 시작일</Label>
            <DatePicker
              id="startDate"
              :model-value="startDateValue"
              :disabled="isSubmitting"
              placeholder="시작일을 선택하세요"
              @update:modelValue="handleStartDateChange"
            />
          </div>

          <div class="grid gap-2">
            <Label for="endDate">투입 종료일</Label>
            <DatePicker
              id="endDate"
              :model-value="endDateValue"
              :disabled="isSubmitting"
              placeholder="종료일을 선택하세요"
              @update:modelValue="handleEndDateChange"
            />
          </div>
        </div>

        <p class="text-xs text-muted-foreground">
          프로젝트 기간: {{ formatProjectDate(projectStartDate) }} ~ {{ projectEndDate ? formatProjectDate(projectEndDate) : '미정' }}
        </p>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleOpenChange(false)">취소</Button>
        <Button :disabled="isSubmitDisabled" @click="handleSubmit">
          {{ isSubmitting ? '저장 중...' : '저장' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <ProjectAssignmentEmployeeSelectDialog
    :open="isEmployeeSelectOpen"
    :selected-employee-id="selectedEmployeeId ?? undefined"
    @update:open="isEmployeeSelectOpen = $event"
    @select="handleEmployeeSelect"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { toast } from 'vue-sonner';
import HttpError from '@/core/http/HttpError';
import { formatProjectDate } from '@/features/project/models/projectListItem';
import type { ProjectAssignmentItem, ProjectAssignmentRole } from '@/features/project/models/projectAssignment';
import { projectAssignmentRoleOptions } from '@/features/project/models/projectAssignment';
import {
  useCreateProjectAssignmentMutation,
  useUpdateProjectAssignmentMutation,
} from '@/features/project/queries/useProjectQueries';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import ProjectAssignmentEmployeeSelectDialog from '@/features/project/components/ProjectAssignmentEmployeeSelectDialog.vue';

interface Props {
  open: boolean;
  projectId: number;
  projectStartDate: string;
  projectEndDate: string | null;
  assignment?: ProjectAssignmentItem | null;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'saved'): void;
}>();

const createMutation = useCreateProjectAssignmentMutation();
const updateMutation = useUpdateProjectAssignmentMutation();
const selectedEmployeeId = ref<number | null>(null);
const selectedEmployeeName = ref('');
const selectedDepartmentId = ref<number | null>(null);
const selectedDepartmentName = ref<string | null>(null);
const role = ref<ProjectAssignmentRole | null>(null);
const startDate = ref('');
const endDate = ref('');
const startDateValue = ref<Date | null>(null);
const endDateValue = ref<Date | null>(null);
const errorMessage = ref<string | null>(null);
const isEmployeeSelectOpen = ref(false);
const isEdit = computed(() => Boolean(props.assignment?.id));
const isSubmitting = computed(() => createMutation.isPending.value || updateMutation.isPending.value);
const selectedEmployeeLabel = computed(() => {
  if (!selectedEmployeeId.value) {
    return '직원을 선택하세요';
  }
  if (selectedDepartmentName.value) {
    return `${selectedEmployeeName.value} · ${selectedDepartmentName.value}`;
  }
  return selectedEmployeeName.value;
});
const isSubmitDisabled = computed(() => {
  if (isSubmitting.value || props.projectId <= 0 || !selectedEmployeeId.value || !role.value || !startDate.value) {
    return true;
  }
  if (props.projectEndDate && !endDate.value) {
    return true;
  }
  return false;
});

watch(
  () => props.open,
  (open) => {
    if (!open) {
      errorMessage.value = null;
      return;
    }

    errorMessage.value = null;
    selectedEmployeeId.value = props.assignment?.employeeId ?? null;
    selectedEmployeeName.value = props.assignment?.employeeName ?? '';
    selectedDepartmentId.value = props.assignment?.departmentId ?? null;
    selectedDepartmentName.value = props.assignment?.departmentName ?? null;
    role.value = props.assignment?.role ?? null;
    startDate.value = props.assignment?.startDate ?? props.projectStartDate;
    endDate.value = props.assignment?.endDate ?? props.projectEndDate ?? '';
    startDateValue.value = toDateValue(startDate.value);
    endDateValue.value = toDateValue(endDate.value);
  },
  { immediate: true },
);

function toDateValue(value: string | null): Date | null {
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

function handleStartDateChange(value: Date | null) {
  startDateValue.value = value;
  startDate.value = formatDate(value);
}

function handleEndDateChange(value: Date | null) {
  endDateValue.value = value;
  endDate.value = formatDate(value);
}

function updateRole(value: string) {
  role.value = value as ProjectAssignmentRole;
}

function clearEmployeeSelection() {
  selectedEmployeeId.value = null;
  selectedEmployeeName.value = '';
  selectedDepartmentId.value = null;
  selectedDepartmentName.value = null;
}

function handleEmployeeSelect(payload: {
  employeeId: number;
  employeeName: string;
  departmentId: number | null;
  departmentName: string | null;
}) {
  selectedEmployeeId.value = payload.employeeId;
  selectedEmployeeName.value = payload.employeeName;
  selectedDepartmentId.value = payload.departmentId;
  selectedDepartmentName.value = payload.departmentName;
}

function validateDates(): string | null {
  if (startDate.value < props.projectStartDate) {
    return '투입 시작일은 프로젝트 시작일보다 빠를 수 없습니다.';
  }
  if (props.projectEndDate && endDate.value > props.projectEndDate) {
    return '투입 종료일은 프로젝트 종료일보다 늦을 수 없습니다.';
  }
  if (endDate.value && endDate.value < startDate.value) {
    return '투입 종료일은 투입 시작일보다 빠를 수 없습니다.';
  }
  if (!props.projectEndDate && !endDate.value) {
    return null;
  }
  return null;
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

async function handleSubmit() {
  if (isSubmitDisabled.value || !selectedEmployeeId.value || !role.value) {
    return;
  }

  const validationMessage = validateDates();
  if (validationMessage) {
    errorMessage.value = validationMessage;
    return;
  }

  errorMessage.value = null;

  try {
    if (props.assignment?.id) {
      await updateMutation.mutateAsync({
        assignmentId: props.assignment.id,
        projectId: props.projectId,
        payload: {
          employeeId: selectedEmployeeId.value,
          role: role.value,
          startDate: startDate.value,
          endDate: endDate.value || null,
        },
      });
      toast.success('투입 인력 정보를 수정했습니다.');
    } else {
      await createMutation.mutateAsync({
        projectId: props.projectId,
        employeeId: selectedEmployeeId.value,
        role: role.value,
        startDate: startDate.value,
        endDate: endDate.value || null,
      });
      toast.success('투입 인력을 추가했습니다.');
    }

    emit('saved');
    handleOpenChange(false);
  } catch (error) {
    errorMessage.value = error instanceof HttpError ? error.message : '투입 인력 저장 중 오류가 발생했습니다.';
  }
}
</script>
