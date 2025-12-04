<template>
  <div
    class="flex items-start gap-3"
    :class="message.role === 'user' ? 'flex-row-reverse text-right' : ''"
  >

    <div
      class="flex flex-col gap-1 text-sm min-w-0"
      :class="[
        message.role === 'user' ? 'items-end max-w-[85%]' : 'items-start w-full'
      ]"
    >
      <p v-if="message.role === 'user'" class="text-[10px] uppercase tracking-wide text-muted-foreground">
        {{ authorLabel }} · {{ formattedTimestamp }}
      </p>
      <div
        v-if="message.role === 'user'"
        class="whitespace-pre-line rounded-2xl border px-3 py-2 shadow-sm text-sm"
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
