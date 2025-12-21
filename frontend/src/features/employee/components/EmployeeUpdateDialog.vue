<template>
  <EmployeeFormDialog
    mode="edit"
    :open="open"
    :employee="employee"
    :department-options="departmentOptions"
    :grade-options="gradeOptions"
    :position-options="positionOptions"
    :type-options="typeOptions"
    @update:open="(value) => emit('update:open', value)"
    @updated="() => emit('updated')"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import EmployeeFormDialog from './EmployeeFormDialog.vue';
import type { EmployeeSummary } from '@/features/employee/models/employee';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';

const props = withDefaults(
  defineProps<{
    open: boolean;
    employee?: EmployeeSummary | null;
    departmentOptions: { label: string; value: number }[];
    gradeOptions?: EmployeeFilterOption[];
    positionOptions?: EmployeeFilterOption[];
    typeOptions?: EmployeeFilterOption[];
  }>(),
  {
    employee: null,
    gradeOptions: undefined,
    positionOptions: undefined,
    typeOptions: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'updated'): void;
}>();

const departmentOptions = computed(() => props.departmentOptions);
const gradeOptions = computed(() => props.gradeOptions);
const positionOptions = computed(() => props.positionOptions);
const typeOptions = computed(() => props.typeOptions);
const employee = computed(() => props.employee ?? null);
</script>
