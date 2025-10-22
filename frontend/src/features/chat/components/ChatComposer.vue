<template>
  <form class="flex flex-col gap-2" @submit.prevent="submit">
    <div v-if="suggestions.length" class="flex flex-wrap gap-1.5">
      <Button
        v-for="suggestion in suggestions"
        :key="suggestion"
        type="button"
        variant="outline"
        class="text-[11px]"
        @click="emit('suggestion', suggestion)"
      >
        {{ suggestion }}
      </Button>
    </div>

    <InputGroup class="rounded-2xl border border-border/60 bg-card/95">
      <InputGroupTextarea
        ref="textareaRef"
        v-model="draft"
        :rows="3"
        :disabled="disabled"
        placeholder="메시지를 입력하세요..."
        class="min-h-[84px] px-4 text-sm leading-relaxed"
        @keydown="handleKeydown"
        @compositionstart="isComposing = true"
        @compositionend="handleCompositionEnd"
      />
      <InputGroupAddon align="block-end" class="gap-1.5 text-xs text-muted-foreground">
        <InputGroupButton
          type="button"
          variant="outline"
          size="icon-xs"
          class="rounded-full"
          aria-label="파일 첨부"
        >
          <Plus class="h-3.5 w-3.5" />
        </InputGroupButton>
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <InputGroupButton variant="ghost" class="gap-1 px-2 text-xs">
              {{ mode }}
              <ChevronDown class="h-3 w-3" />
            </InputGroupButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent side="top" align="start" class="[--radius:0.95rem]">
            <DropdownMenuItem v-for="option in modes" :key="option" @click="setMode(option)">
              {{ option }}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
        <InputGroupText class="ml-auto text-[11px]">
          {{ usageText }}
        </InputGroupText>
        <Separator orientation="vertical" class="!h-4" />
        <InputGroupButton
          type="submit"
          variant="default"
          size="icon-xs"
          class="rounded-full"
          :disabled="disabled || draft.trim().length === 0"
        >
          <ArrowUp class="h-3.5 w-3.5" />
          <span class="sr-only">전송</span>
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>

    <div class="flex items-center justify-between text-[11px] text-muted-foreground">
      <span>{{ infoText }}</span>
      <span>Shift + Enter 줄바꿈</span>
    </div>
  </form>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { ArrowUp, ChevronDown, Plus } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupTextarea,
  InputGroupText,
} from '@/components/ui/input-group';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
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
const mode = ref<'Auto' | 'Agent' | 'Manual'>('Auto');
const modes: Array<'Auto' | 'Agent' | 'Manual'> = ['Auto', 'Agent', 'Manual'];
const usageText = computed(() => '52% used');

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

function setMode(value: 'Auto' | 'Agent' | 'Manual') {
  mode.value = value;
}
</script>
