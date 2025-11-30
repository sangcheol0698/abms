<template>
  <ProjectFormDialog
    mode="create"
    :open="open"
    @update:open="(value) => emit('update:open', value)"
    @created="() => emit('created')"
  />
</template>

<script setup lang="ts">
import ProjectFormDialog from './ProjectFormDialog.vue';

withDefaults(
  defineProps<{
    open: boolean;
  }>(),
  {
    open: false,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'created'): void;
}>();
</script>
