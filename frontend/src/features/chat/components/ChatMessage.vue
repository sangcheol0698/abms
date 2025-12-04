<template>
  <div
    class="flex items-start gap-3"
    :class="message.role === 'user' ? 'flex-row-reverse text-right' : ''"
  >
    <div
      class="flex h-9 w-9 items-center justify-center rounded-full border text-sm font-semibold"
      :class="
        message.role === 'user'
          ? 'border-muted-foreground/40 text-muted-foreground'
          : 'border-primary/40 bg-primary/10 text-primary'
      "
    >
      {{ message.role === 'user' ? '나' : 'AI' }}
    </div>
    <div class="max-w-[85%] space-y-1 text-sm">
      <p class="text-xs uppercase tracking-wide text-muted-foreground">
        {{ authorLabel }} · {{ formattedTimestamp }}
      </p>
      <div
        v-if="message.role === 'user'"
        class="whitespace-pre-line rounded-2xl border px-4 py-3 shadow-sm"
        :class="'border-primary/40 bg-primary/10 text-foreground'"
      >
        {{ message.content }}
      </div>
      <div v-else class="text-foreground">
        <MarkdownRenderer :content="message.content" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ChatMessage } from '@/features/chat/entity/ChatMessage';
import MarkdownRenderer from '@/features/chat/components/MarkdownRenderer.vue';

const props = defineProps<{ message: ChatMessage }>();

const formattedTimestamp = computed(() =>
  props.message.createdAt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
);

const authorLabel = computed(() => (props.message.role === 'user' ? 'You' : 'AI Assistant'));
</script>
