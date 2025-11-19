<template>
  <form class="flex flex-col gap-2" @submit.prevent="submit">

    <InputGroup class="rounded-[24px] border border-border/60 bg-background shadow-sm transition-shadow focus-within:shadow-md">
      <InputGroupTextarea
        ref="textareaRef"
        v-model="draft"
        :rows="1"
        :disabled="disabled"
        placeholder="메시지를 입력하세요..."
        class="min-h-[48px] max-h-[200px] px-4 py-3 text-sm leading-relaxed resize-none"
        @keydown="handleKeydown"
        @compositionstart="isComposing = true"
        @compositionend="handleCompositionEnd"
      />
      <InputGroupAddon align="block-end" class="gap-2">
        <InputGroupButton
          type="button"
          variant="ghost"
          size="icon-xs"
          class="rounded-full"
          aria-label="파일 첨부"
        >
          <Plus class="h-4 w-4" />
        </InputGroupButton>
        <InputGroupText class="ml-auto text-[11px] text-muted-foreground">
          {{ infoText }}
        </InputGroupText>
        <Separator orientation="vertical" class="!h-4" />
        <InputGroupButton
          type="submit"
          variant="default"
          size="icon-xs"
          class="rounded-full"
          :disabled="disabled || draft.trim().length === 0"
        >
          <ArrowUp class="h-4 w-4" />
          <span class="sr-only">전송</span>
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  </form>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue';
import { ArrowUp, Plus } from 'lucide-vue-next';
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupText,
  InputGroupTextarea,
} from '@/components/ui/input-group';
import { Separator } from '@/components/ui/separator';

const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    suggestions?: string[];
    infoText?: string;
  }>(),
  {
    disabled: false,
    suggestions: () => [],
    infoText: 'Enter: 전송 · Shift + Enter: 줄바꿈',
  },
);

const emit = defineEmits<{
  submit: [value: string];
  suggestion: [value: string];
}>();

const draft = defineModel<string>({ default: '' });
const textareaRef = ref<any>(null);
const isComposing = ref(false);

async function focusTextarea() {
  await nextTick();
  const textarea = textareaRef.value?.$el as HTMLTextAreaElement | undefined;
  if (!textarea) return;
  textarea.focus();
  textarea.setSelectionRange(textarea.value.length, textarea.value.length);
}

async function submit() {
  const content = draft.value.trim();
  if (!content || props.disabled) {
    return;
  }
  emit('submit', content);
  draft.value = '';
  await focusTextarea();
}

watch(
  () => props.disabled,
  (value) => {
    if (!value) {
      void focusTextarea();
    }
  },
  { immediate: true },
);

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter' || event.shiftKey) {
    return;
  }
  if (isComposing.value) {
    return;
  }
  event.preventDefault();
  void submit();
}

function handleCompositionEnd() {
  isComposing.value = false;
}
</script>
