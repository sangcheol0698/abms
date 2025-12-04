<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="flex-1 min-h-0">
      <ScrollArea ref="scrollAreaRef" class="h-full">
        <div class="flex justify-center px-4 py-6">
          <div class="w-full max-w-3xl">
            <template v-if="messages.length === 0">
              <div class="mt-12 flex flex-col items-center justify-center gap-6 text-center">
                <div
                  class="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/5 shadow-sm"
                >
                  <Bot class="h-8 w-8 text-primary" />
                </div>
                <div class="space-y-2">
                  <h3 class="text-lg font-semibold text-foreground">AI Assistant</h3>
                  <p class="max-w-[320px] text-sm text-muted-foreground">
                    부서와 직원 데이터를 기반으로<br />
                    궁금한 점을 자유롭게 물어보세요.
                  </p>
                </div>

                <div class="mt-8 grid w-full max-w-2xl grid-cols-1 gap-3 sm:grid-cols-2">
                  <button
                    v-for="suggestion in defaultSuggestions"
                    :key="suggestion.label"
                    type="button"
                    class="flex flex-col items-start gap-1 rounded-xl border border-border/40 bg-card p-4 text-left transition-all hover:bg-muted/50 hover:shadow-sm"
                    @click="$emit('submit', suggestion.query)"
                  >
                    <span class="text-sm font-medium text-foreground">{{ suggestion.label }}</span>
                    <span class="text-xs text-muted-foreground">{{ suggestion.description }}</span>
                  </button>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="flex flex-col gap-6">
                <ChatMessage v-for="message in messages" :key="message.id" :message="message" />
                <div
                  v-if="isResponding"
                  class="flex items-center gap-2 self-start rounded-2xl px-3 py-2 text-sm text-muted-foreground"
                >
                  <div class="flex gap-1">
                    <span
                      class="h-1.5 w-1.5 animate-bounce rounded-full bg-muted-foreground/40"
                      style="animation-delay: 0ms"
                    ></span>
                    <span
                      class="h-1.5 w-1.5 animate-bounce rounded-full bg-muted-foreground/40"
                      style="animation-delay: 150ms"
                    ></span>
                    <span
                      class="h-1.5 w-1.5 animate-bounce rounded-full bg-muted-foreground/40"
                      style="animation-delay: 300ms"
                    ></span>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </ScrollArea>
    </div>

    <div class="bg-background px-4 pb-6 pt-2">
      <div class="mx-auto w-full max-w-3xl">
        <ChatComposer
          v-model="draftValue"
          :disabled="isResponding"
          :info-text="infoText"
          @submit="$emit('submit', $event)"
          @suggestion="$emit('suggestion', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ComponentPublicInstance } from 'vue';
import { computed, nextTick, ref, watch } from 'vue';
import { Bot } from 'lucide-vue-next';
import ChatComposer from '@/features/chat/components/ChatComposer.vue';
import ChatMessage from '@/features/chat/components/ChatMessage.vue';
import type { ChatMessage as ChatMessageModel } from '@/features/chat/entity/ChatMessage';
import { ScrollArea } from '@/components/ui/scroll-area';

const props = defineProps<{
  messages: ChatMessageModel[];
  isResponding: boolean;
  suggestions: string[];
  infoText: string;
  modelValue: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  submit: [value: string];
  suggestion: [value: string];
}>();

const scrollAreaRef = ref<ComponentPublicInstance | null>(null);
const draftValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
});

function getViewport(): HTMLElement | null {
  const root = scrollAreaRef.value?.$el as HTMLElement | undefined;
  if (!root) {
    return null;
  }
  return root.querySelector<HTMLElement>('[data-slot="scroll-area-viewport"]');
}

watch(
  () => props.messages.length,
  async () => {
    await nextTick();
    const viewport = getViewport();
    if (viewport) {
      viewport.scrollTop = viewport.scrollHeight;
    }
  },
  { immediate: true },
);

// Also watch for content changes in the last message (for streaming)
watch(
  () => props.messages[props.messages.length - 1]?.content,
  async () => {
    await nextTick();
    await nextTick(); // Double nextTick for markdown rendering
    const viewport = getViewport();
    if (viewport) {
      viewport.scrollTop = viewport.scrollHeight;
    }
  },
);

const defaultSuggestions = [
  {
    label: '신규 입사자 현황',
    description: '이번 달 새로 합류한 직원 목록을 보여줘',
    query: '이번 달 신규 입사자 목록 알려줘',
  },
  {
    label: '부서별 인원 통계',
    description: '각 부서의 현재 인원 현황을 알려줘',
    query: '부서별 인원 현황 알려줘',
  },
  {
    label: '휴직자 조회',
    description: '현재 휴직 중인 직원 목록 확인',
    query: '현재 휴직 중인 직원은 누구야?',
  },
  {
    label: '부서 위치 찾기',
    description: '특정 부서나 팀의 위치 확인',
    query: '인사팀은 부서 구조 어디에 있어?',
  },
];
</script>
