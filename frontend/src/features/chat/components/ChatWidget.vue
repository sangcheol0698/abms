<template>
  <div class="flex h-full min-h-0 flex-col overflow-hidden">
    <div class="flex-1 min-h-0 px-2">
      <ScrollArea ref="scrollAreaRef" class="h-full">
        <div class="flex flex-col gap-3 px-6 py-4">
          <template v-if="messages.length === 0">
            <div class="flex min-h-[220px] flex-col items-center justify-center gap-3 text-center text-muted-foreground">
              <Bot class="h-10 w-10 opacity-60" />
              <p class="max-w-[280px] text-xs leading-relaxed">
                안녕하세요! 좌측 메뉴에서 확인한 조직·구성원 데이터를 기반으로 궁금한 내용을 질문해
                보세요.
              </p>
            </div>
          </template>
          <template v-else>
            <ClaudeMessage v-for="message in messages" :key="message.id" :message="message" />
            <div
              v-if="isResponding"
              class="self-start rounded-2xl border border-border/40 bg-muted px-3 py-2 text-xs text-muted-foreground"
            >
              응답을 생성하는 중입니다...
            </div>
          </template>
        </div>
      </ScrollArea>
    </div>

    <footer class="border-t border-border/60 px-6 py-4">
      <ChatComposer
        v-model="draftValue"
        :disabled="isResponding"
        :info-text="infoText"
        @submit="$emit('submit', $event)"
        @suggestion="$emit('suggestion', $event)"
      />
    </footer>
  </div>
</template>

<script setup lang="ts">
import type { ComponentPublicInstance } from 'vue';
import { computed, nextTick, ref, watch } from 'vue';
import { Bot } from 'lucide-vue-next';
import ChatComposer from '@/features/chat/components/ChatComposer.vue';
import ClaudeMessage from '@/features/chat/components/ClaudeMessage.vue';
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
</script>
