<template>
  <div class="grid gap-4 sm:grid-cols-3">
    <button
      v-for="option in options"
      :key="option.code"
      type="button"
      class="group flex flex-col items-center gap-3 rounded-2xl border bg-card p-4 transition focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
      :class="[
        option.code === modelValue
          ? 'border-primary shadow-md shadow-primary/10'
          : 'border-border/70 hover:border-primary/60 hover:shadow-sm',
        disabled ? 'pointer-events-none opacity-60' : '',
      ]"
      role="radio"
      :aria-checked="option.code === modelValue"
      @click="() => handleSelect(option.code)"
    >
      <div class="relative">
        <div
          class="flex h-20 w-20 items-center justify-center rounded-full border transition"
          :class="option.code === modelValue ? 'border-primary ring-4 ring-primary/20' : 'border-border/60'"
        >
          <img
            :src="option.imageUrl"
            :alt="option.label"
            class="h-16 w-16 rounded-full object-cover"
            loading="lazy"
          />
        </div>
        <span
          v-if="option.code === modelValue"
          class="absolute -right-2 -bottom-1 flex h-6 w-6 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground shadow-sm"
        >
          ✓
        </span>
      </div>
      <span class="text-sm font-semibold text-foreground">{{ option.label }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import type { EmployeeAvatarOption } from '@/features/employee/constants/avatars';

const props = withDefaults(
  defineProps<{
    modelValue: string;
    options: EmployeeAvatarOption[];
    disabled?: boolean;
  }>(),
  {
    modelValue: '',
    options: () => [],
    disabled: false,
  },
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void;
}>();

function handleSelect(code: string) {
  if (props.disabled) {
    return;
  }
  emit('update:modelValue', code);
}
</script>
