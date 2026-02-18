<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="flex-1 min-h-0">
      <ScrollArea ref="scrollAreaRef" class="h-full">
        <div class="flex justify-center px-4 py-6">
          <div class="w-full max-w-3xl">
            <template v-if="messages.length === 0">
              <div class="mt-12 flex flex-col items-center justify-center gap-6 text-center">
                <div class="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/5 shadow-sm">
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
                  <button v-for="suggestion in defaultSuggestions" :key="suggestion.label" type="button"
                    class="flex flex-col items-start gap-1 rounded-xl border border-border/40 bg-card p-4 text-left transition-all hover:bg-muted/50 hover:shadow-sm"
                    @click="$emit('submit', suggestion.query)">
                    <span class="text-sm font-medium text-foreground">{{ suggestion.label }}</span>
                    <span class="text-xs text-muted-foreground">{{ suggestion.description }}</span>
                  </button>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="flex flex-col gap-6">
                <ChatMessage v-for="message in messages" :key="message.id" :message="message" />
                <ChatLoadingBubble v-if="shouldShowLoadingBubble" />
              </div>
            </template>
          </div>
        </div>
      </ScrollArea>
    </div>

    <div class="bg-background px-4 pb-6 pt-2">
      <div class="mx-auto w-full max-w-3xl">
        <ChatComposer ref="composerRef" v-model="draftValue" :disabled="isResponding" :info-text="infoText"
          :is-responding="isResponding" @submit="$emit('submit', $event)" @suggestion="$emit('suggestion', $event)"
          @stop="$emit('stop')" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ComponentPublicInstance } from 'vue';
import { computed, nextTick, ref, watch } from 'vue';
import { Bot } from 'lucide-vue-next';
import ChatComposer from '@/features/chat/components/ChatComposer.vue';
import ChatLoadingBubble from '@/features/chat/components/ChatLoadingBubble.vue';
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
  stop: [];
}>();

const scrollAreaRef = ref<ComponentPublicInstance | null>(null);
const draftValue = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
});

const shouldShowLoadingBubble = computed(() => {
  if (!props.isResponding) return false;
  const lastMessage = props.messages[props.messages.length - 1];

  // If no message or last message is user, we are waiting for assistant message creation
  if (!lastMessage || lastMessage.role === 'user') return true;

  // If assistant message exists, show bubble only if it's completely empty (no content AND no tool status)
  // Tool status acts as its own indicator ("Scanning..."), and content acts as streaming indicator.
  return !lastMessage.content && !lastMessage.toolStatus;
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
    label: '조직 핵심 지표',
    description: '직원/부서/휴직 등 조직 현황을 한 번에 확인',
    query: '현재 조직 핵심 지표를 요약해줘 (총 직원 수, 부서 수, 휴직 인원 포함)',
  },
  {
    label: '부서별 인원 현황',
    description: '전체 부서를 인원 기준으로 빠르게 비교',
    query: '전체 부서 목록과 각 부서 인원 수를 정리해줘',
  },
  {
    label: '프로젝트 투입 현황',
    description: '프로젝트별 투입 인력 현황을 요약',
    query: '진행 중인 프로젝트별 투입 인력 현황을 요약해줘',
  },
  {
    label: '협력사 프로젝트 현황',
    description: '협력사별 진행 프로젝트를 빠르게 조회',
    query: '협력사 목록과 각 협력사의 진행 프로젝트 수를 정리해줘',
  },
];
const composerRef = ref<any>(null);

function addFiles(files: File[]) {
  composerRef.value?.addFiles(files);
}

defineExpose({
  addFiles,
});
</script>
