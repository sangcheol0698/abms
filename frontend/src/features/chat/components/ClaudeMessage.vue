<template>
  <div class="flex items-start gap-4" :class="isUser ? 'flex-row-reverse' : ''">
    <div
      class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full border text-xs font-semibold shadow-sm"
      :class="
        isUser
          ? 'border-muted-foreground/20 bg-muted/50 text-muted-foreground'
          : 'border-primary/10 bg-primary/5 text-primary'
      "
    >
      <template v-if="isUser">나</template>
      <template v-else>AI</template>
    </div>

    <div
      class="flex max-w-[85%] flex-col gap-1 text-sm"
      :class="isUser ? 'items-end' : 'items-start'"
    >
      <div class="text-[11px] font-medium text-muted-foreground/70">
        {{ authorLabel }}
      </div>

      <div v-if="isUser" class="flex flex-col items-end gap-2">
        <div
          v-for="(block, index) in blocks"
          :key="index"
          :class="
            block.type === 'code'
              ? 'rounded-2xl border border-border/70 bg-muted/50 px-4 py-3 text-xs font-mono text-foreground'
              : 'rounded-[20px] bg-secondary/80 px-5 py-3 text-sm leading-relaxed text-secondary-foreground shadow-sm'
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

      <div v-else class="flex flex-col items-start gap-3 pt-1">
        <div
          v-for="(block, index) in blocks"
          :key="index"
          class="w-full"
          :class="block.type === 'code' ? 'overflow-hidden rounded-lg border border-border/50 bg-muted/30' : ''"
        >
          <template v-if="block.type === 'code'">
            <div class="flex items-center justify-between border-b border-border/50 bg-muted/30 px-4 py-2 text-[11px] text-muted-foreground">
              <span class="font-mono">{{ block.language?.toUpperCase() ?? 'CODE' }}</span>
              <Button variant="ghost" size="icon" class="h-6 w-6 hover:bg-background/50" @click="copy(block.content)">
                <Copy class="h-3.5 w-3.5" />
                <span class="sr-only">코드 복사</span>
              </Button>
            </div>
            <div class="overflow-x-auto p-4">
              <pre class="font-mono text-xs leading-relaxed">{{ block.content }}</pre>
            </div>
          </template>
          <template v-else>
            <p class="whitespace-pre-line text-[15px] leading-7 text-foreground/90">{{ block.content }}</p>
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
