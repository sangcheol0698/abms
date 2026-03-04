<template>
  <ChatMessage :message="loadingMessage" />
</template>

<script setup lang="ts">
import ChatMessage from '@/features/chat/components/ChatMessage.vue';
import type { ChatMessage as ChatMessageModel } from '@/features/chat/entity/ChatMessage';

defineOptions({ name: 'ChatLoadingBubble' });

const loadingMessage: ChatMessageModel = {
  id: 'assistant-loading-indicator',
  role: 'assistant',
  content: '',
  toolStatus: {
    name: 'thinking',
    emoji: '',
    description: '생각 중...',
  },
  createdAt: new Date(),
};
</script>
