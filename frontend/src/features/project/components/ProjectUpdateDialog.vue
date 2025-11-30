<template>
  <ProjectFormDialog
    mode="edit"
    :open="open"
    :project="project"
    @update:open="(value) => emit('update:open', value)"
    @updated="() => emit('updated')"
  />
</template>

<script setup lang="ts">
import ProjectFormDialog from './ProjectFormDialog.vue';
import type { ProjectDetail } from '@/features/project/models/projectDetail';

withDefaults(
  defineProps<{
    open: boolean;
    project?: ProjectDetail | null;
  }>(),
  {
    open: false,
    project: null,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'updated'): void;
}>();
</script>
