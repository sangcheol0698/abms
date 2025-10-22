<template>
  <section
    class="flex min-h-0 flex-1 flex-col overflow-hidden"
    :class="{ 'h-[calc(100dvh-4rem)]': !isLargeScreen }"
  >
    <template v-if="isLargeScreen">
      <ResizablePanelGroup direction="horizontal" class="flex flex-1 min-h-0 overflow-hidden">
        <template v-if="!isSidebarCollapsed">
          <ResizablePanel :default-size="18" :min-size="10" :max-size="26">
            <aside
              class="flex min-h-0 flex-col border-r border-border/60 bg-background"
              :class="isLargeScreen ? 'h-[calc(100dvh-4rem)]' : 'h-full'"
            >
              <div class="flex items-center justify-between px-3 py-3">
                <span class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  채팅
                </span>
                <Button
                  variant="ghost"
                  size="icon"
                  class="h-7 w-7"
                  aria-label="사이드바 접기"
                  @click="closeSidebar"
                >
                  <ChevronsLeftRight class="h-4 w-4" />
                </Button>
              </div>

                <div class="px-4 pb-3">
                  <Button class="w-full gap-2 text-sm" @click="createNewChat">
                    <Plus class="h-4 w-4" /> 새 채팅
                  </Button>
                </div>

                <div class="px-4">
                  <div class="relative">
                    <Search
                      class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                    />
                    <Input v-model="searchQuery" placeholder="채팅 검색" class="pl-9 text-xs" />
                  </div>
                </div>

                <nav class="mt-5 flex-1 space-y-5 overflow-y-auto px-4 pb-5 text-sm">
                  <div>
                    <div
                      class="flex items-center justify-between px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground"
                    >
                      <span>즐겨찾기</span>
                      <Sparkles class="h-3.5 w-3.5" />
                    </div>
                    <ul class="space-y-1">
                      <li v-for="item in filteredFavorites" :key="item.id">
                        <button
                          type="button"
                          class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-xs transition-colors"
                          :class="
                            currentSessionId === item.id
                              ? 'bg-primary/10 text-primary'
                              : 'hover:bg-muted/60 text-foreground'
                          "
                          @click="selectSession(item.id)"
                        >
                          <span class="truncate">{{ item.title }}</span>
                          <Star class="h-3.5 w-3.5" />
                        </button>
                      </li>
                    </ul>
                  </div>

                  <div>
                    <div
                      class="px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground"
                    >
                      최근 항목
                    </div>
                    <ul class="space-y-1 text-xs">
                      <li v-for="item in filteredRecent" :key="item.id">
                        <button
                          type="button"
                          class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left transition-colors"
                          :class="
                            currentSessionId === item.id
                              ? 'bg-muted text-foreground'
                              : 'hover:bg-muted/50 text-foreground'
                          "
                          @click="selectSession(item.id)"
                        >
                          <div class="min-w-0">
                            <p class="truncate font-medium">{{ item.title }}</p>
                            <p class="truncate text-[11px] text-muted-foreground">
                              {{ item.description }}
                            </p>
                          </div>
                          <span class="text-[10px] text-muted-foreground">{{ item.updated }}</span>
                        </button>
                      </li>
                    </ul>
                  </div>
                </nav>

                <div class="border-t border-border/60 p-3 text-[11px] text-muted-foreground">
                  <div class="flex items-center justify-between">
                    <div>
                      <p class="font-medium text-foreground">박상철</p>
                      <p>무료 플랜</p>
                    </div>
                    <Button variant="ghost" size="icon" class="h-7 w-7">
                      <MoreHorizontal class="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </aside>
          </ResizablePanel>

          <ResizableHandle with-handle class="bg-border/70" />
        </template>

        <ResizablePanel :default-size="isSidebarCollapsed ? 100 : 82" :min-size="60">
          <div
            class="flex min-h-0 w-full"
            :class="isLargeScreen ? 'h-[calc(100dvh-4rem)]' : 'h-full'"
          >
            <div class="flex h-full min-h-0 flex-1 flex-col bg-background">
              <header
                class="flex items-center justify-between border-b border-border/60 px-8 py-5 text-sm"
              >
                <div class="flex items-center gap-3">
                <Button
                  v-if="isSidebarCollapsed"
                  variant="ghost"
                  size="icon"
                  class="h-8 w-8"
                  aria-label="사이드바 열기"
                  @click="openSidebar"
                >
                  <ChevronRight class="h-4 w-4" />
                </Button>
                  <h2 class="text-base font-semibold text-foreground">{{ currentSessionTitle }}</h2>
                  <Badge variant="outline" class="gap-1 text-[11px]">
                    <History class="h-3 w-3" /> {{ sessionUpdatedAt }}
                  </Badge>
                </div>
                <div class="flex items-center gap-1 text-xs text-muted-foreground">
                  <Button variant="ghost" size="sm" class="gap-1">
                    <Star class="h-3.5 w-3.5" /> 즐겨찾기
                  </Button>
                  <Button variant="ghost" size="sm" class="gap-1">
                    <Share class="h-3.5 w-3.5" /> 공유
                  </Button>
                  <Button variant="ghost" size="icon" class="h-8 w-8">
                    <MoreHorizontal class="h-4 w-4" />
                  </Button>
                </div>
              </header>
              <div class="flex h-full min-h-0 flex-col px-8 py-6">
                <ChatWidget
                  class="flex flex-1 min-h-0"
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
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </template>

    <template v-else>
      <div class="flex h-[calc(100dvh-4rem)] min-h-0 flex-1 flex-col bg-background">
        <header class="flex items-center justify-between border-b border-border/60 px-4 py-4">
          <div class="flex items-center gap-2">
            <Button
              variant="outline"
              size="icon"
              class="h-9 w-9"
              aria-label="채팅 목록 열기"
              @click="isMobileSidebarOpen = true"
            >
              <Menu class="h-4 w-4" />
            </Button>
            <div class="flex flex-col">
              <h2 class="text-base font-semibold text-foreground">{{ currentSessionTitle }}</h2>
              <Badge variant="outline" class="mt-1 w-fit gap-1 text-[11px]">
                <History class="h-3 w-3" /> {{ sessionUpdatedAt }}
              </Badge>
            </div>
          </div>
          <div class="flex items-center gap-1 text-xs text-muted-foreground">
            <Button variant="ghost" size="sm" class="gap-1">
              <Star class="h-3.5 w-3.5" /> 즐겨찾기
            </Button>
            <Button variant="ghost" size="sm" class="gap-1">
              <Share class="h-3.5 w-3.5" /> 공유
            </Button>
          </div>
        </header>

        <div class="relative flex flex-1 min-h-0 flex-col">
          <ChatWidget
            v-model="draft"
            :messages="messages"
            :is-responding="isResponding"
            :suggestions="[]"
            :info-text="infoText"
            class="flex flex-1 min-h-0"
            @submit="handleSubmit"
            @suggestion="handleSuggestion"
          />

          <Transition name="fade">
            <div
              v-if="isMobileSidebarOpen"
              class="absolute inset-0 z-30 flex bg-background/80 backdrop-blur-sm"
            >
              <div class="w-10 flex-shrink-0" @click="isMobileSidebarOpen = false"></div>
              <aside class="flex h-full w-full max-w-xs flex-col border-l border-border/60 bg-background">
                <div class="flex items-center justify-between border-b border-border/60 px-4 py-3">
                  <span class="text-xs font-semibold uppercase tracking-wide text-muted-foreground"
                    >채팅</span
                  >
                  <Button
                    variant="ghost"
                    size="icon"
                    class="h-8 w-8"
                    aria-label="목록 닫기"
                    @click="isMobileSidebarOpen = false"
                  >
                    <X class="h-4 w-4" />
                  </Button>
                </div>
                <div class="flex-1 overflow-y-auto">
                  <div class="px-4 pb-3 pt-4">
                    <Button class="w-full gap-2 text-sm" @click="createNewChat">
                      <Plus class="h-4 w-4" /> 새 채팅
                    </Button>
                  </div>
                  <div class="px-4">
                    <div class="relative">
                      <Search
                        class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                      />
                      <Input v-model="searchQuery" placeholder="채팅 검색" class="pl-9 text-xs" />
                    </div>
                  </div>
                  <div class="mt-5 space-y-5 px-4 pb-6 text-sm">
                    <div>
                      <div
                        class="flex items-center justify-between px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground"
                      >
                        <span>즐겨찾기</span>
                        <Sparkles class="h-3.5 w-3.5" />
                      </div>
                      <ul class="space-y-1">
                        <li v-for="item in filteredFavorites" :key="item.id">
                          <button
                            type="button"
                            class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-xs transition-colors"
                            :class="
                              currentSessionId === item.id
                                ? 'bg-primary/10 text-primary'
                                : 'hover:bg-muted/60 text-foreground'
                            "
                            @click="selectSession(item.id)"
                          >
                            <span class="truncate">{{ item.title }}</span>
                            <Star class="h-3.5 w-3.5" />
                          </button>
                        </li>
                      </ul>
                    </div>

                    <div>
                      <div
                        class="px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground"
                      >
                        최근 항목
                      </div>
                      <ul class="space-y-1 text-xs">
                        <li v-for="item in filteredRecent" :key="item.id">
                          <button
                            type="button"
                            class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left transition-colors"
                            :class="
                              currentSessionId === item.id
                                ? 'bg-muted text-foreground'
                                : 'hover:bg-muted/50 text-foreground'
                            "
                            @click="selectSession(item.id)"
                          >
                            <div class="min-w-0">
                              <p class="truncate font-medium">{{ item.title }}</p>
                              <p class="truncate text-[11px] text-muted-foreground">
                                {{ item.description }}
                              </p>
                            </div>
                            <span class="text-[10px] text-muted-foreground">{{ item.updated }}</span>
                          </button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </aside>
            </div>
          </Transition>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue';
import { useBreakpoints } from '@vueuse/core';
import {
  ChevronRight,
  ChevronsLeftRight,
  Clock,
  History,
  MoreHorizontal,
  Search,
  Share,
  Sparkles,
  Star,
  PlusSquareIcon as Plus,
  Menu,
  X,
} from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '@/components/ui/resizable';
import ChatWidget from '@/features/chat/components/ChatWidget.vue';
import { createChatMessage, type ChatMessage } from '@/features/chat/entity/ChatMessage';
import type { ChatRepository } from '@/features/chat/repository/ChatRepository';
import { MockChatRepository } from '@/features/chat/repository/ChatRepository.mock';

const repository: ChatRepository = new MockChatRepository();
const breakpoints = useBreakpoints({ lg: 1024 });
const isLargeScreen = breakpoints.greater('lg');
const isMobileSidebarOpen = ref(false);
const isSidebarCollapsed = ref(false);

interface SessionItem {
  id: string;
  title: string;
  description?: string;
  updated: string;
  favorite?: boolean;
}

const favorites = reactive<SessionItem[]>([
  { id: 'design-review', title: 'ERD 설계 리뷰', updated: '방금 전', favorite: true },
  { id: 'spec-update', title: 'Claude Sonnet 문서화', updated: '2시간 전', favorite: true },
]);

const recentSessions = reactive<SessionItem[]>([
  {
    id: 'erp-diagram',
    title: 'ERP 시스템 ERD 점검',
    description: '테이블 구조 확인',
    updated: '5분 전',
  },
  { id: 'shadcn', title: 'shadcn-vue 설치', description: 'UI 컴포넌트 세팅', updated: '12분 전' },
  {
    id: 'rest-api',
    title: 'RESTful OpenAPI 개선',
    description: 'Swagger 문서 보완',
    updated: '어제',
  },
  {
    id: 'newsletter',
    title: '뉴스 스프린트 정리',
    description: '주간 하이라이트',
    updated: '3일 전',
  },
]);

const currentSessionId = ref<string>('design-review');
const searchQuery = ref('');
const sessionId = ref<string | null>(null);

const greetingText =
  '안녕하세요! 저는 ABMS Copilot입니다.\n\n다음과 같은 질문을 할 수 있어요:\n• "이번 달 신규 입사자 목록 알려줘"\n• "휴직 중인 구성원은 누구야?"\n• "조직도에서 인사팀 위치 보여줘"';

const messages = ref<ChatMessage[]>([createChatMessage('assistant', greetingText)]);
const draft = ref('');
const isResponding = ref(false);

const infoText = computed(() =>
  isResponding.value ? '응답을 생성 중입니다...' : 'Enter: 전송 · Shift + Enter: 줄바꿈',
);

watch(isLargeScreen, (value) => {
  if (value) {
    isMobileSidebarOpen.value = false;
  }
});

const filteredFavorites = computed(() =>
  favorites.filter((item) => item.title.toLowerCase().includes(searchQuery.value.toLowerCase())),
);

const filteredRecent = computed(() =>
  recentSessions.filter(
    (item) =>
      item.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.description?.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
);

const currentSessionTitle = computed(() => {
  const target = [...favorites, ...recentSessions].find(
    (item) => item.id === currentSessionId.value,
  );
  return target?.title ?? '새 채팅';
});

const sessionUpdatedAt = computed(() => {
  const target = [...favorites, ...recentSessions].find(
    (item) => item.id === currentSessionId.value,
  );
  return target?.updated ?? '방금 전';
});

function createNewChat() {
  const id = crypto.randomUUID();
  recentSessions.unshift({ id, title: '새로운 대화', description: '초안', updated: '방금 전' });
  selectSession(id);
}

function selectSession(id: string) {
  currentSessionId.value = id;
  messages.value = [createChatMessage('assistant', greetingText)];
  draft.value = '';
  sessionId.value = null;
  if (!isLargeScreen.value) {
    isMobileSidebarOpen.value = false;
  }
}

function closeSidebar() {
  isSidebarCollapsed.value = true;
}

function openSidebar() {
  isSidebarCollapsed.value = false;
}

function handleSuggestion(value: string) {
  draft.value = value;
}

async function handleSubmit(content: string) {
  messages.value.push(createChatMessage('user', content));

  try {
    isResponding.value = true;
    const response = await repository.sendMessage({
      sessionId: sessionId.value ?? undefined,
      content,
    });
    sessionId.value = response.sessionId;
    response.messages.forEach((message) => {
      messages.value.push({
        ...message,
        createdAt: new Date(message.createdAt ?? new Date()),
      });
    });
  } catch (error) {
    const fallback =
      error instanceof Error
        ? error.message
        : '응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.';
    messages.value.push(createChatMessage('assistant', fallback));
  } finally {
    isResponding.value = false;
  }
}

watch(
  () => messages.value.length,
  async () => {
    await nextTick();
  },
);
</script>
