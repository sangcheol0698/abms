<template>
  <div class="flex items-start gap-3" :class="isUser ? 'flex-row-reverse text-right' : ''">
    <div
      class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border text-sm font-semibold"
      :class="
        isUser
          ? 'border-muted-foreground/40 text-muted-foreground'
          : 'border-primary/30 bg-primary/10 text-primary'
      "
    >
      {{ isUser ? '나' : 'AI' }}
    </div>

    <div
      class="flex max-w-[80%] flex-col gap-2 text-sm"
      :class="isUser ? 'items-end text-right' : 'items-start text-left'"
    >
      <div class="text-[11px] uppercase tracking-wide text-muted-foreground">
        {{ authorLabel }} · {{ formattedTimestamp }}
      </div>

      <div v-if="isUser" class="flex flex-col items-end gap-2">
        <div
          v-for="(block, index) in blocks"
          :key="index"
          :class="
            block.type === 'code'
              ? 'rounded-2xl border border-border/70 bg-muted/50 px-4 py-3 text-xs font-mono text-foreground'
              : 'rounded-3xl bg-primary text-primary-foreground px-4 py-3 text-sm leading-relaxed whitespace-pre-line shadow-sm'
          "
        >
          <template v-if="block.type === 'code'">
            <pre class="whitespace-pre-wrap text-xs leading-relaxed">{{ block.content }}</pre>
          </template>
          <template v-else>
            {{ block.content }}
          </template>
        </div>
      </div>

      <div v-else class="flex flex-col items-start gap-3">
        <div
          v-for="(block, index) in blocks"
          :key="index"
          :class="block.type === 'code' ? 'w-full overflow-hidden rounded-2xl border border-border/70 bg-muted/30 px-4 py-3 font-mono text-xs text-foreground' : ''"
        >
          <template v-if="block.type === 'code'">
            <div class="mb-2 flex items-center justify-between text-[11px] uppercase text-muted-foreground">
              <span>{{ block.language?.toUpperCase() ?? 'CODE' }}</span>
              <Button variant="ghost" size="icon" class="h-6 w-6" @click="copy(block.content)">
                <Copy class="h-3.5 w-3.5" />
                <span class="sr-only">코드 복사</span>
              </Button>
            </div>
            <pre class="whitespace-pre-wrap text-xs leading-relaxed">{{ block.content }}</pre>
          </template>
          <template v-else>
            <p class="whitespace-pre-line text-sm leading-relaxed text-foreground">{{ block.content }}</p>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Copy } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import type { ChatMessage } from '@/features/chat/entity/ChatMessage';

interface Block {
  type: 'text' | 'code';
  content: string;
  language?: string;
}

const props = defineProps<{ message: ChatMessage }>();

const isUser = computed(() => props.message.role === 'user');
const authorLabel = computed(() => (isUser.value ? 'You' : 'ABMS Copilot'));
const formattedTimestamp = computed(() =>
  props.message.createdAt.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
);

const blocks = computed<Block[]>(() => parseContent(props.message.content));

function parseContent(text: string): Block[] {
  const result: Block[] = [];
  const regex = /```(\w+)?\n([\s\S]*?)```/g;
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = regex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      result.push({ type: 'text', content: text.slice(lastIndex, match.index).trim() });
    }

    result.push({
      type: 'code',
      language: match[1] ?? 'text',
      content: match[2]?.trim() ?? '',
    });

    lastIndex = regex.lastIndex;
  }

  if (lastIndex < text.length) {
    result.push({ type: 'text', content: text.slice(lastIndex).trim() });
  }

  return result.filter((block) => block.content.length > 0);
}

async function copy(content: string) {
  try {
    await navigator.clipboard.writeText(content);
  } catch (error) {
    console.warn('코드 복사에 실패했습니다.', error);
  }
}
</script>
