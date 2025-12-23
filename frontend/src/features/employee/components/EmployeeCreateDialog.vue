<template>
  <EmployeeFormDialog
    mode="create"
    :open="open"
    :department-options="departmentOptions"
    :grade-options="gradeOptions"
    :position-options="positionOptions"
    :type-options="typeOptions"
    @update:open="(value) => emit('update:open', value)"
    @created="() => emit('created')"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import EmployeeFormDialog from './EmployeeFormDialog.vue';
import type { EmployeeFilterOption } from '@/features/employee/models/employeeFilters';

const props = withDefaults(
  defineProps<{
    open: boolean;
    departmentOptions: { label: string; value: number }[];
    gradeOptions?: EmployeeFilterOption[];
    positionOptions?: EmployeeFilterOption[];
    typeOptions?: EmployeeFilterOption[];
  }>(),
  {
    gradeOptions: undefined,
    positionOptions: undefined,
    typeOptions: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'created'): void;
}>();

const departmentOptions = computed(() => props.departmentOptions);
const gradeOptions = computed(() => props.gradeOptions);
const positionOptions = computed(() => props.positionOptions);
const typeOptions = computed(() => props.typeOptions);
</script>
