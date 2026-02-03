<template>
  <div class="flex items-start gap-3 group" :class="message.role === 'user' ? 'flex-row-reverse text-right' : ''">
    <div class="flex flex-col gap-1 text-sm min-w-0"
      :class="[message.role === 'user' ? 'items-end max-w-[85%]' : 'items-start w-full']">

      <!-- Tool Status Indicator (Simple Skeleton Style) -->
      <div v-if="message.toolStatus && !message.content" class="flex items-center gap-2 mb-1 animate-pulse select-none">
        <span class="text-sm font-medium text-muted-foreground">{{ message.toolStatus.description }}</span>
      </div>

      <div v-if="message.role === 'user'"
        class="whitespace-pre-wrap rounded-2xl border px-3 py-2 shadow-sm text-sm text-left"
        :class="'border-primary/40 bg-primary/10 text-foreground'">
        {{ message.content }}
      </div>
      <div v-else-if="message.content" class="text-foreground">
        <MarkdownRenderer :content="message.content" />
      </div>

      <!-- Message Actions (Common for User and AI) -->
      <div class="flex items-center gap-2 mt-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
        <span class="text-xs text-muted-foreground tabular-nums">{{ formattedTimeOnly }}</span>
        <button class="p-1 rounded-md hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
          :title="isCopied ? '복사됨' : '복사'" @click="copyToClipboard(message.content)">
          <Check v-if="isCopied" class="w-3.5 h-3.5" />
          <Copy v-else class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { ChatMessage } from '@/features/chat/entity/ChatMessage';
import MarkdownRenderer from '@/features/chat/components/MarkdownRenderer.vue';
import { Copy, Check } from 'lucide-vue-next';
import { toast } from 'vue-sonner';

const props = defineProps<{ message: ChatMessage }>();

// Format structured like "2025년 11월 1일" + Icon Buttons
const formattedTimeOnly = computed(() => {
  const date = props.message.createdAt;
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
});


const isCopied = ref(false);

const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text);
    isCopied.value = true;
    setTimeout(() => {
      isCopied.value = false;
    }, 2000);
  } catch (err) {
    console.error('Failed to copy text: ', err);
    toast.error('복사에 실패했습니다.');
  }
};
</script>
