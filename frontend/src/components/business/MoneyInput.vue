<template>
  <Input
    v-model="textValue"
    :class="props.class"
    inputmode="numeric"
    v-bind="$attrs"
    @update:model-value="handleInput"
    @focus="isFocused = true"
    @blur="handleBlur"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { HTMLAttributes } from 'vue';
import { Input } from '@/components/ui/input';

const props = withDefaults(
  defineProps<{
    modelValue?: number;
    class?: HTMLAttributes['class'];
  }>(),
  {
    modelValue: 0,
    class: undefined,
  },
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: number): void;
  (event: 'blur'): void;
}>();

const isFocused = ref(false);
const textValue = ref(formatAmount(props.modelValue));

watch(
  () => props.modelValue,
  (value) => {
    if (isFocused.value) {
      return;
    }
    textValue.value = formatAmount(value);
  },
);

function handleInput(value: string | number) {
  const normalized = normalizeDigits(value);
  if (!normalized) {
    textValue.value = '';
    emit('update:modelValue', 0);
    return;
  }

  const parsed = Number(normalized);
  if (!Number.isFinite(parsed)) {
    return;
  }

  textValue.value = formatAmount(parsed);
  emit('update:modelValue', parsed);
}

function handleBlur() {
  isFocused.value = false;
  textValue.value = formatAmount(props.modelValue);
  emit('blur');
}

function normalizeDigits(value: string | number): string {
  return String(value ?? '').replace(/\D/g, '');
}

function formatAmount(value?: number): string {
  if (typeof value !== 'number' || !Number.isFinite(value)) {
    return '';
  }
  return new Intl.NumberFormat('ko-KR').format(Math.max(0, Math.trunc(value)));
}
</script>
