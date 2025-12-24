<template>
  <FeatureSplitLayout :sidebar-default-size="18" :sidebar-min-size="10" :sidebar-max-size="26" :content-min-size="60">
    <template #sidebar="{ pane }">
      <div class="flex h-full min-h-0 flex-col border-r border-border/60 bg-background">
        <div class="flex items-center justify-between px-3 py-3"></div>

        <div class="px-4 pb-3">
          <Button class="w-full gap-2 text-sm" @click="handleCreateNewChat(pane)">
            <SquarePen class="h-4 w-4" /> 새 채팅
          </Button>
        </div>

        <div class="px-4">
          <div class="relative">
            <Search
              class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input v-model="searchQuery" placeholder="채팅 검색" class="pl-9 text-xs" />
          </div>
        </div>

        <nav class="mt-5 flex-1 space-y-5 overflow-y-auto px-4 pb-5 text-sm">
          <div v-if="filteredFavorites.length > 0">
            <div
              class="flex items-center justify-between px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
              <span>즐겨찾기</span>
              <Sparkles class="h-3.5 w-3.5" />
            </div>
            <ul class="space-y-1">
              <li v-for="item in filteredFavorites" :key="item.sessionId">
                <button type="button"
                  class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-xs transition-colors"
                  :class="currentSessionId === item.sessionId
                    ? 'bg-primary/10 text-primary'
                    : 'hover:bg-muted/60 text-foreground'
                    " @click="handleSessionSelect(item.sessionId, pane)">
                  <span class="truncate">{{ item.title }}</span>
                  <Star class="h-3.5 w-3.5 fill-current" />
                </button>
              </li>
            </ul>
          </div>

          <div v-if="filteredRecent.length > 0">
            <div class="px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
              최근 항목
            </div>
            <ul class="space-y-1 text-xs">
              <li v-for="item in filteredRecent" :key="item.sessionId">
                <button type="button"
                  class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left transition-colors"
                  :class="currentSessionId === item.sessionId
                    ? 'bg-muted text-foreground'
                    : 'hover:bg-muted/50 text-foreground'
                    " @click="handleSessionSelect(item.sessionId, pane)">
                  <div class="min-w-0">
                    <p class="truncate font-medium">{{ item.title }}</p>
                    <p class="truncate text-[11px] text-muted-foreground">
                      {{ formatRelativeTime(item.updatedAt) }}
                    </p>
                  </div>
                </button>
              </li>
            </ul>
          </div>

          <div v-if="isLoading" class="flex items-center justify-center py-4">
            <Loader2 class="h-5 w-5 animate-spin text-muted-foreground" />
          </div>

          <div v-if="!isLoading && filteredFavorites.length === 0 && filteredRecent.length === 0" 
               class="text-center py-4 text-xs text-muted-foreground">
            채팅 기록이 없습니다
          </div>
        </nav>

        <div class="border-t border-border/60 p-3 text-[11px] text-muted-foreground">
          <div class="flex items-center justify-between">
            <div>
              <p class="font-medium text-foreground">ABMS Assistant</p>
              <p>AI 어시스턴트</p>
            </div>
            <Button variant="ghost" size="icon" class="h-7 w-7">
              <MoreHorizontal class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>
    </template>

    <template #default="{ pane }">
      <div
        class="relative flex h-full min-h-0 flex-1 flex-col bg-background"
        @dragenter.prevent="handleDragEnter"
        @dragleave.prevent="handleDragLeave"
        @dragover.prevent
        @drop.prevent="handleDrop"
      >
        <!-- Drag & Drop Overlay -->
        <div
          v-if="isDragging"
          class="absolute inset-0 z-50 flex flex-col items-center justify-center bg-background/80 backdrop-blur-sm transition-all animate-in fade-in duration-200"
        >
          <div class="flex flex-col items-center gap-4 p-8">
            <div class="flex h-20 w-20 items-center justify-center rounded-3xl bg-primary/10 shadow-lg ring-1 ring-primary/20">
              <FileUp class="h-10 w-10 text-primary animate-bounce" />
            </div>
            <div class="text-center space-y-1">
              <h3 class="text-xl font-semibold tracking-tight">무엇이든 추가하세요</h3>
              <p class="text-sm text-muted-foreground">대화에 추가하려면 여기에 파일을 드롭하세요</p>
            </div>
          </div>
        </div>

        <header class="flex items-center justify-between border-b border-border/40 px-4 py-3">
          <div class="flex items-center gap-3">
            <Button variant="ghost" size="icon"
              class="-ml-1 h-8 w-8 text-muted-foreground transition hover:text-foreground" aria-label="채팅 사이드바 토글"
              @click="pane.toggleSidebar()">
              <Menu class="h-4 w-4 transition" :class="pane.isSidebarCollapsed.value ? 'rotate-180' : ''" />
            </Button>
            <div class="flex flex-col">
              <h2 class="text-base font-semibold text-foreground">{{ currentSessionTitle }}</h2>
              <Badge v-if="currentSession" variant="outline" class="mt-1 w-fit gap-1 text-[11px]">
                <History class="h-3 w-3" /> {{ formatRelativeTime(currentSession.updatedAt) }}
              </Badge>
            </div>
          </div>
          <div class="flex items-center gap-1 text-xs text-muted-foreground">
            <Button 
              v-if="currentSessionId" 
              variant="ghost" 
              size="sm" 
              class="gap-1"
              @click="handleToggleFavorite"
            >
              <Star class="h-3.5 w-3.5" :class="currentSession?.favorite ? 'fill-current text-yellow-500' : ''" /> 
              즐겨찾기
            </Button>
            <Button variant="ghost" size="sm" class="gap-1">
              <Share class="h-3.5 w-3.5" /> 공유
            </Button>
            <Button variant="ghost" size="icon" class="hidden h-8 w-8 md:inline-flex">
              <MoreHorizontal class="h-4 w-4" />
            </Button>
          </div>
        </header>


        <div class="flex h-full min-h-0 flex-col items-center bg-background"
          :class="pane.isLargeScreen.value ? 'px-0 py-0' : 'px-0 py-0'">
          <ChatWidget
            ref="chatWidgetRef"
            class="flex w-full flex-1 min-h-0"
            v-model="draft"
            :messages="messages"
            :is-responding="isResponding"
            :suggestions="[]"
            :info-text="infoText"
            @submit="handleSubmit"
            @suggestion="handleSuggestion"
          />
        </div>
      </div>
    </template>
  </FeatureSplitLayout>
</template>

<script setup lang="ts">
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import {
  History,
  Loader2,
  Menu,
  MoreHorizontal,
  SquarePen,
  Search,
  Share,
  Sparkles,
  Star,
  FileUp,
} from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import ChatWidget from '@/features/chat/components/ChatWidget.vue';
import {
  createChatMessage,
  type ChatMessage,
  type ChatSession,
} from '@/features/chat/entity/ChatMessage';
import { useChatRepository } from '@/features/chat/repository/useChatRepository';
import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import type { FeatureSplitPaneContext } from '@/core/composables/useFeatureSplitPane';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const repository: ChatRepository = useChatRepository();
const route = useRoute();
const router = useRouter();

// State
const favorites = ref<ChatSession[]>([]);
const recentSessions = ref<ChatSession[]>([]);
const currentSessionId = ref<string | null>(null);
const currentSession = ref<ChatSession | null>(null);
const searchQuery = ref('');
const messages = ref<ChatMessage[]>([]);
const draft = ref('');
const isResponding = ref(false);
const isLoading = ref(false);
const isDragging = ref(false);
const chatWidgetRef = ref<any>(null);
let dragCounter = 0;

// Computed
const infoText = computed(() =>
  isResponding.value ? '응답을 생성 중입니다...' : 'Enter: 전송 · Shift + Enter: 줄바꿈',
);

const filteredFavorites = computed(() =>
  favorites.value.filter((item) => item.title.toLowerCase().includes(searchQuery.value.toLowerCase())),
);

const filteredRecent = computed(() =>
  recentSessions.value
    .filter((item) => !item.favorite)
    .filter((item) => item.title.toLowerCase().includes(searchQuery.value.toLowerCase())),
);

const currentSessionTitle = computed(() => {
  if (!currentSessionId.value) return '새 채팅';
  return currentSession.value?.title ?? '채팅';
});

// Helpers
function formatRelativeTime(date: Date): string {
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (days < 7) return `${days}일 전`;
  return date.toLocaleDateString('ko-KR');
}

const TOOL_CONFIG: Record<string, { emoji: string; description: string }> = {
  getEmployeeInfo: { emoji: '👤', description: '직원 정보 조회 중...' },
  getDepartmentInfo: { emoji: '🏢', description: '부서 정보 조회 중...' },
  getSubDepartments: { emoji: '📊', description: '하위 부서 조회 중...' },
};

function getToolEmoji(toolName: string): string {
  return TOOL_CONFIG[toolName]?.emoji ?? '🔍';
}

function getToolDescription(toolName: string): string {
  return TOOL_CONFIG[toolName]?.description ?? `${toolName} 실행 중...`;
}

// Data loading
async function loadSessions() {
  isLoading.value = true;
  try {
    const [favs, recent] = await Promise.all([
      repository.getFavoriteSessions(),
      repository.getRecentSessions(20),
    ]);
    favorites.value = favs;
    recentSessions.value = recent;
  } catch (error) {
    console.error('Failed to load sessions:', error);
  } finally {
    isLoading.value = false;
  }
}

async function loadSessionDetail(sessionId: string) {
  try {
    const detail = await repository.getSessionDetail(sessionId);
    currentSession.value = detail;
    messages.value = detail.messages;
  } catch {
    // Session not found in backend - this is a new session, start fresh
    currentSession.value = null;
    messages.value = [];
  }
}

// Actions
function handleCreateNewChat(pane?: FeatureSplitPaneContext) {
  // Generate new sessionId and navigate to it
  const newSessionId = crypto.randomUUID();
  router.push({ name: 'assistant-session', params: { sessionId: newSessionId } });
  currentSessionId.value = newSessionId;
  currentSession.value = null;
  messages.value = [];
  draft.value = '';
  
  if (!pane) return;
  if (pane.isLargeScreen.value) {
    pane.expandSidebar();
  } else {
    pane.openSidebar();
  }
}

function handleSessionSelect(sessionId: string, pane?: FeatureSplitPaneContext) {
  router.push({ name: 'assistant-session', params: { sessionId } });
  
  if (pane && !pane.isLargeScreen.value) {
    pane.closeSidebar();
  }
}

async function handleToggleFavorite() {
  if (!currentSessionId.value) return;
  
  try {
    await repository.toggleFavorite(currentSessionId.value);
    // Reload sessions to reflect the change
    await loadSessions();
    // Update current session
    if (currentSession.value) {
      currentSession.value = {
        ...currentSession.value,
        favorite: !currentSession.value.favorite,
      };
    }
  } catch (error) {
    console.error('Failed to toggle favorite:', error);
  }
}

function handleSuggestion(value: string) {
  draft.value = value;
}

function handleDragEnter(event: DragEvent) {
  dragCounter++;
  if (event.dataTransfer?.types.includes('Files')) {
    isDragging.value = true;
  }
}

function handleDragLeave(_event: DragEvent) {
  dragCounter--;
  if (dragCounter === 0) {
    isDragging.value = false;
  }
}

function handleDrop(event: DragEvent) {
  isDragging.value = false;
  dragCounter = 0;
  
  if (event.dataTransfer?.files) {
    const files = Array.from(event.dataTransfer.files);
    if (files.length > 0) {
      chatWidgetRef.value?.addFiles(files);
    }
  }
}

async function handleSubmit(content: string) {
  messages.value.push(createChatMessage('user', content));

  try {
    isResponding.value = true;

    // Create a placeholder message for the assistant
    const assistantMessage = createChatMessage('assistant', '');
    messages.value.push(assistantMessage);

    // Get the index of the assistant message we just added
    const assistantMessageIndex = messages.value.length - 1;

    // Track tool call indicator separately
    let toolIndicator: string | null = null;

    await repository.streamMessage(
      {
        sessionId: currentSessionId.value ?? undefined,
        content,
      },
      (chunk: string) => {
        // If there was a tool indicator, clear it when real content arrives
        const message = messages.value[assistantMessageIndex];
        if (message) {
          if (toolIndicator && message.content === toolIndicator) {
            message.content = '';
          }
          toolIndicator = null;
          message.content += chunk;
        }
      },
      (error: Error) => {
        console.error('Streaming error:', error);
        const message = messages.value[assistantMessageIndex];
        if (message && !message.content) {
          message.content = '응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.';
        }
      },
      (toolName: string) => {
        // Show tool call notification temporarily
        const message = messages.value[assistantMessageIndex];
        if (message && !message.content) {
          const toolEmoji = getToolEmoji(toolName);
          toolIndicator = `${toolEmoji} ${getToolDescription(toolName)}`;
          message.content = toolIndicator;
        }
      }
    );

    // Reload sessions to get the new/updated session
    await loadSessions();
    
    // Reload again after delay to get AI-generated title
    setTimeout(() => loadSessions(), 2000);
  } catch (error) {
    const fallback =
      error instanceof Error
        ? error.message
        : '응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.';
    
    // If the last message is empty assistant message, update it
    const lastMessage = messages.value[messages.value.length - 1];
    if (lastMessage && lastMessage.role === 'assistant' && !lastMessage.content) {
      lastMessage.content = fallback;
    } else {
      messages.value.push(createChatMessage('assistant', fallback));
    }
  } finally {
    isResponding.value = false;
  }
}

// Watch route changes
watch(
  () => route.params.sessionId,
  async (newSessionId) => {
    if (newSessionId && typeof newSessionId === 'string') {
      currentSessionId.value = newSessionId;
      await loadSessionDetail(newSessionId);
    } else {
      // No sessionId in URL - generate new one and redirect
      const newId = crypto.randomUUID();
      router.replace({ name: 'assistant-session', params: { sessionId: newId } });
      currentSessionId.value = newId;
      currentSession.value = null;
      messages.value = [];
    }
  },
  { immediate: true }
);

// Initial load
onMounted(async () => {
  await loadSessions();
});
</script>
