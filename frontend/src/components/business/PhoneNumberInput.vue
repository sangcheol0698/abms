<template>
  <Input
    v-model="textValue"
    :class="props.class"
    inputmode="tel"
    maxlength="13"
    autocomplete="tel-national"
    v-bind="$attrs"
    @update:model-value="handleInput"
    @blur="emit('blur')"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { HTMLAttributes } from 'vue';
import { Input } from '@/components/ui/input';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    class?: HTMLAttributes['class'];
  }>(),
  {
    modelValue: '',
    class: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: string): void;
  (event: 'blur'): void;
}>();

const textValue = ref(formatPhoneNumber(props.modelValue));

watch(
  () => props.modelValue,
  (value) => {
    textValue.value = formatPhoneNumber(value);
  },
);

function handleInput(value: string | number) {
  const formatted = formatPhoneNumber(String(value ?? ''));
  textValue.value = formatted;
  emit('update:modelValue', formatted);
}

function formatPhoneNumber(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, 11);
  if (!digits) {
    return '';
  }

  if (digits.startsWith('02')) {
    if (digits.length <= 2) {
      return digits;
    }
    if (digits.length <= 5) {
      return `${digits.slice(0, 2)}-${digits.slice(2)}`;
    }
    if (digits.length <= 9) {
      return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`;
    }
    return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6)}`;
  }

  if (digits.length <= 3) {
    return digits;
  }
  if (digits.length <= 7) {
    return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  }
  if (digits.length <= 10) {
    return `${digits.slice(0, 3)}-${digits.slice(3, 6)}-${digits.slice(6)}`;
  }
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
}
</script>
