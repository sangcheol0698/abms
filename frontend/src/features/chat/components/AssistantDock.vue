<template>
  <template v-if="!isAssistantRoute">
    <aside
      class="relative hidden h-full min-h-0 shrink-0 overflow-hidden overscroll-contain border-l border-border/60 bg-background xl:flex"
      :class="isResizing ? 'transition-none' : 'transition-[width] duration-200'"
      :style="{ width: isDockOpen ? `${desktopWidth}px` : `${DOCK_COLLAPSED_WIDTH}px` }"
    >
      <button
        v-if="isDockOpen"
        type="button"
        class="group absolute inset-y-0 left-0 z-20 flex w-3 cursor-col-resize items-center justify-center"
        aria-label="사이드바 너비 조절"
        @pointerdown.prevent="startResize"
      >
        <span class="h-12 w-px rounded-full bg-border/60 transition-colors group-hover:bg-border" />
      </button>

      <div v-if="isDockOpen" class="flex h-full min-h-0 w-full flex-col">
        <header class="flex items-center justify-between border-b border-border/40 px-3 py-2">
          <div class="min-w-0">
            <p class="truncate text-sm font-semibold text-foreground">{{ dockSessionTitle }}</p>
          </div>
          <div class="flex items-center gap-1">
            <Tooltip>
              <TooltipTrigger as-child>
                <Button variant="ghost" size="icon" class="h-7 w-7" :disabled="isResponding" @click="handleNewChat">
                  <SquarePen class="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">새 채팅</TooltipContent>
            </Tooltip>
            <Tooltip>
              <TooltipTrigger as-child>
                <Button variant="ghost" size="icon" class="h-7 w-7" :disabled="isResponding" @click="openFullView">
                  <Expand class="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">전체 보기</TooltipContent>
            </Tooltip>
            <Tooltip>
              <TooltipTrigger as-child>
                <Button variant="ghost" size="icon" class="h-7 w-7" @click="isDockOpen = false">
                  <PanelRightClose class="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent side="bottom">채팅 닫기</TooltipContent>
            </Tooltip>
          </div>
        </header>
        <div class="min-h-0 flex-1">
          <ChatWidget
            class="h-full"
            v-model="draftValue"
            :messages="messages"
            :is-responding="isResponding"
            :suggestions="[]"
            :info-text="infoText"
            @submit="handleSubmit"
            @suggestion="handleSuggestion"
            @stop="handleStopResponse"
          />
        </div>
      </div>

      <div v-else class="flex h-full w-full items-start justify-center pt-3">
        <Button variant="ghost" size="icon" class="h-9 w-9" @click="isDockOpen = true">
          <Bot class="h-5 w-5" />
        </Button>
      </div>
    </aside>

    <div class="fixed bottom-5 right-5 z-40 xl:hidden">
      <Button size="icon" class="h-12 w-12 rounded-full shadow-lg" @click="isMobileOpen = true">
        <Bot class="h-5 w-5" />
      </Button>
    </div>

    <Sheet :open="isMobileOpen" @update:open="isMobileOpen = $event">
      <SheetContent side="right" class="p-0 w-[92vw] sm:w-[420px]">
        <SheetTitle class="sr-only">Assistant Panel</SheetTitle>
        <SheetDescription class="sr-only">Assistant chat panel</SheetDescription>
        <div class="flex h-full min-h-0 flex-col bg-background">
          <header class="flex items-center justify-between border-b border-border/40 px-3 py-2">
            <p class="truncate text-sm font-semibold text-foreground">{{ dockSessionTitle }}</p>
            <div class="flex items-center gap-1">
              <Tooltip>
                <TooltipTrigger as-child>
                  <Button variant="ghost" size="icon" class="h-7 w-7" :disabled="isResponding" @click="handleNewChat">
                    <SquarePen class="h-4 w-4" />
                  </Button>
                </TooltipTrigger>
                <TooltipContent side="bottom">새 채팅</TooltipContent>
              </Tooltip>
              <Tooltip>
                <TooltipTrigger as-child>
                  <Button variant="ghost" size="icon" class="h-7 w-7" :disabled="isResponding" @click="openFullView">
                    <Expand class="h-4 w-4" />
                  </Button>
                </TooltipTrigger>
                <TooltipContent side="bottom">전체 보기</TooltipContent>
              </Tooltip>
            </div>
          </header>
          <div class="min-h-0 flex-1">
            <ChatWidget
              class="h-full"
              v-model="draftValue"
              :messages="messages"
              :is-responding="isResponding"
              :suggestions="[]"
              :info-text="infoText"
              @submit="handleSubmit"
              @suggestion="handleSuggestion"
              @stop="handleStopResponse"
            />
          </div>
        </div>
      </SheetContent>
    </Sheet>
  </template>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { Bot, Expand, PanelRightClose, SquarePen } from 'lucide-vue-next';
import { useRoute, useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetDescription, SheetTitle } from '@/components/ui/sheet';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import ChatWidget from '@/features/chat/components/ChatWidget.vue';
import { createChatMessage, type ChatMessage } from '@/features/chat/entity/ChatMessage';
import { useChatSessionDetailQuery } from '@/features/chat/queries/useChatQueries';
import { useChatRepository } from '@/features/chat/repository/useChatRepository';
import { sanitizeAssistantLinks } from '@/features/chat/utils/linkSanitizer';
import { useAssistantDockStore } from '@/features/chat/stores/assistantDock.store';
import { toast } from 'vue-sonner';

const repository = useChatRepository();
const dockStore = useAssistantDockStore();
const { activeSessionId, isDockOpen, isMobileOpen } = storeToRefs(dockStore);
const sessionDetailQuery = useChatSessionDetailQuery(activeSessionId);

const route = useRoute();
const router = useRouter();

const DOCK_MIN_WIDTH = 320;
const DOCK_MAX_WIDTH = 560;
const DOCK_DEFAULT_WIDTH = 380;
const DOCK_COLLAPSED_WIDTH = 56;
const DESKTOP_WIDTH_STORAGE_KEY = 'assistant_dock_desktop_width';

const messages = ref<ChatMessage[]>([]);
const draft = ref('');
const isResponding = ref(false);
const streamAbortController = ref<AbortController | null>(null);
const isStopRequested = ref(false);
const titleRefreshTimeout = ref<ReturnType<typeof setTimeout> | null>(null);
const sessionTitle = ref('새 채팅');
const desktopWidth = ref(DOCK_DEFAULT_WIDTH);
const isResizing = ref(false);
let resizeStartX = 0;
let resizeStartWidth = DOCK_DEFAULT_WIDTH;

const infoText = computed(() =>
  isResponding.value ? '응답 생성 중... 중지 버튼으로 멈출 수 있습니다.' : 'Enter: 전송 · Shift + Enter: 줄바꿈',
);
const dockSessionTitle = computed(() => sessionTitle.value || '새 채팅');

const draftValue = computed({
  get: () => draft.value,
  set: (value: string) => {
    draft.value = value;
  },
});

const isAssistantRoute = computed(() => {
  const name = String(route.name ?? '');
  return name === 'assistant' || name === 'assistant-session';
});

const TOOL_CONFIG: Record<string, { emoji: string; description: string }> = {
  getEmployeeInfo: { emoji: '👤', description: '직원 정보 조회 중...' },
  getEmployeeInfoByDepartment: { emoji: '🪪', description: '부서 기준 직원 정보 조회 중...' },
  getEmployeeInfoById: { emoji: '🆔', description: '직원 ID 기준 상세 조회 중...' },
  getDepartmentInfo: { emoji: '🏢', description: '부서 정보 조회 중...' },
  getSubDepartments: { emoji: '📊', description: '하위 부서 조회 중...' },
  getDepartmentMembers: { emoji: '👥', description: '부서 구성원 조회 중...' },
  getAllDepartments: { emoji: '🗂️', description: '전체 부서 목록 조회 중...' },
  getOrganizationStats: { emoji: '📈', description: '조직 통계 조회 중...' },
  searchEmployees: { emoji: '🔎', description: '직원 목록 검색 중...' },
  searchProjects: { emoji: '📁', description: '프로젝트 검색 중...' },
  getProjectDetail: { emoji: '📌', description: '프로젝트 상세 조회 중...' },
  getDashboardSummary: { emoji: '📊', description: '대시보드 지표 조회 중...' },
  searchParties: { emoji: '🏷️', description: '협력사 검색 중...' },
  getPartyProjects: { emoji: '🧾', description: '협력사 프로젝트 조회 중...' },
  getMonthlyRevenueSummary: { emoji: '💹', description: '월별 매출 집계 조회 중...' },
  getDepartmentEmployees: { emoji: '👨‍👩‍👧‍👦', description: '부서 직원 목록 조회 중...' },
  getProjectAssignments: { emoji: '🧑‍💻', description: '프로젝트 투입 인력 조회 중...' },
  getEmployeePositionHistory: { emoji: '🪜', description: '직급 이력 조회 중...' },
};

function getToolEmoji(toolName: string): string {
  return TOOL_CONFIG[toolName]?.emoji ?? '🔍';
}

function getToolDescription(toolName: string): string {
  return TOOL_CONFIG[toolName]?.description ?? `${toolName} 실행 중...`;
}

function isSessionNotFoundError(error: unknown): boolean {
  return error instanceof Error && /\b404\b/.test(error.message);
}

function resetDockToNewChat() {
  dockStore.setActiveSessionId(null);
  clearScheduledTitleRefresh();
  sessionTitle.value = '새 채팅';
  messages.value = [];
  draft.value = '';
}

async function loadSessionDetail(sessionId: string) {
  try {
    if (activeSessionId.value !== sessionId) {
      return;
    }
    const result = await sessionDetailQuery.refetch();
    const detail = result.data;
    if (!detail) {
      sessionTitle.value = '새 채팅';
      messages.value = [];
      return;
    }
    const nextTitle = detail.title?.trim() || '새 채팅';
    if (sessionTitle.value !== nextTitle) {
      sessionTitle.value = nextTitle;
    }

    const nextMessages = detail.messages.map((message) =>
      message.role === 'assistant'
        ? {
            ...message,
            content: sanitizeAssistantLinks(message.content),
          }
        : message
    );
    if (!areMessagesEquivalent(messages.value, nextMessages)) {
      messages.value = nextMessages;
    }
  } catch (error) {
    if (isSessionNotFoundError(error) && activeSessionId.value === sessionId) {
      resetDockToNewChat();
      toast.error('접근할 수 없는 채팅 세션입니다. 새 채팅으로 전환했어요.');
      return;
    }
    sessionTitle.value = '새 채팅';
    messages.value = [];
  }
}

async function refreshSessionTitle(sessionId: string) {
  try {
    if (activeSessionId.value !== sessionId) {
      return;
    }
    const result = await sessionDetailQuery.refetch();
    const detail = result.data;
    if (!detail) {
      return;
    }
    sessionTitle.value = detail.title?.trim() || '새 채팅';
  } catch {
    // Ignore title refresh failures
  }
}

function clearScheduledTitleRefresh() {
  if (titleRefreshTimeout.value == null) {
    return;
  }
  clearTimeout(titleRefreshTimeout.value);
  titleRefreshTimeout.value = null;
}

function scheduleSessionTitleRefresh(sessionId: string) {
  void refreshSessionTitle(sessionId);
  clearScheduledTitleRefresh();
  titleRefreshTimeout.value = setTimeout(() => {
    void refreshSessionTitle(sessionId);
    titleRefreshTimeout.value = null;
  }, 1500);
}

function markAssistantMessageStopped(messageIndex: number) {
  const message = messages.value[messageIndex];
  if (!message || message.role !== 'assistant') {
    return;
  }
  message.toolStatus = undefined;
  if (message.content.includes('(중지됨)')) {
    return;
  }
  const trimmed = message.content.trimEnd();
  message.content = trimmed.length > 0 ? `${trimmed}\n\n(중지됨)` : '(중지됨)';
}

function areMessagesEquivalent(current: ChatMessage[], next: ChatMessage[]): boolean {
  if (current.length !== next.length) {
    return false;
  }
  return current.every((message, index) => {
    const nextMessage = next[index];
    if (!nextMessage) {
      return false;
    }
    return message.role === nextMessage.role && message.content === nextMessage.content;
  });
}

function handleStopResponse() {
  if (!isResponding.value || !streamAbortController.value) {
    return;
  }
  isStopRequested.value = true;
  streamAbortController.value.abort();
}

async function handleSubmit(content: string) {
  messages.value.push(createChatMessage('user', content));
  let assistantMessageIndex = -1;
  let abortController: AbortController | null = null;
  const startedWithoutSession = !activeSessionId.value;

  try {
    isResponding.value = true;
    isStopRequested.value = false;
    abortController = new AbortController();
    streamAbortController.value = abortController;
    const assistantMessage = createChatMessage('assistant', '');
    messages.value.push(assistantMessage);
    assistantMessageIndex = messages.value.length - 1;
    let toolIndicator: string | null = null;

    const streamedSessionId = await repository.streamMessage(
      {
        sessionId: activeSessionId.value ?? undefined,
        content,
      },
      (chunk: string) => {
        const message = messages.value[assistantMessageIndex];
        if (message) {
          if (toolIndicator) {
            message.toolStatus = undefined;
            toolIndicator = null;
          }
          message.content += chunk;
          message.content = sanitizeAssistantLinks(message.content);
        }
      },
      (error: Error) => {
        if (isStopRequested.value) {
          return;
        }
        console.error('Dock streaming error:', error);
        const message = messages.value[assistantMessageIndex];
        if (message && !message.content) {
          message.content = '응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.';
        }
      },
      (toolName: string) => {
        const message = messages.value[assistantMessageIndex];
        if (!message) return;
        toolIndicator = toolName;
        message.toolStatus = {
          name: toolName,
          emoji: getToolEmoji(toolName),
          description: getToolDescription(toolName),
        };
      },
      { signal: abortController.signal },
    );

    const effectiveSessionId = streamedSessionId ?? activeSessionId.value;

    if (isStopRequested.value) {
      markAssistantMessageStopped(assistantMessageIndex);
    }

    if (streamedSessionId && streamedSessionId !== activeSessionId.value) {
      dockStore.setActiveSessionId(streamedSessionId);
    }

    if (effectiveSessionId && (startedWithoutSession || sessionTitle.value === '새 채팅')) {
      scheduleSessionTitleRefresh(effectiveSessionId);
    }
  } catch (error) {
    if (isStopRequested.value) {
      markAssistantMessageStopped(assistantMessageIndex);
      return;
    }

    if (isSessionNotFoundError(error)) {
      resetDockToNewChat();
      toast.error('해당 채팅 세션에 접근할 수 없어 새 채팅으로 전환했습니다.');
      return;
    }

    const fallback =
      error instanceof Error
        ? error.message
        : '응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.';
    const lastMessage = messages.value[messages.value.length - 1];
    if (lastMessage && lastMessage.role === 'assistant' && !lastMessage.content) {
      lastMessage.content = fallback;
    } else {
      messages.value.push(createChatMessage('assistant', fallback));
    }
  } finally {
    if (streamAbortController.value === abortController) {
      streamAbortController.value = null;
    }
    isStopRequested.value = false;
    isResponding.value = false;
  }
}

function handleSuggestion(value: string) {
  draft.value = value;
}

function handleNewChat() {
  if (isResponding.value) return;
  resetDockToNewChat();
}

function openFullView() {
  if (isResponding.value) {
    return;
  }
  isMobileOpen.value = false;
  if (activeSessionId.value) {
    router.push({ name: 'assistant-session', params: { sessionId: activeSessionId.value } });
    return;
  }
  router.push({ name: 'assistant' });
}

function clampDockWidth(width: number): number {
  return Math.min(Math.max(width, DOCK_MIN_WIDTH), DOCK_MAX_WIDTH);
}

function persistDesktopWidth() {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(DESKTOP_WIDTH_STORAGE_KEY, String(desktopWidth.value));
}

function applyResizeBodyState(active: boolean) {
  if (typeof document === 'undefined') return;
  document.body.style.cursor = active ? 'col-resize' : '';
  document.body.style.userSelect = active ? 'none' : '';
}

function handleResizeMove(event: PointerEvent) {
  if (!isResizing.value) return;
  const delta = resizeStartX - event.clientX;
  desktopWidth.value = clampDockWidth(resizeStartWidth + delta);
}

function stopResize() {
  if (!isResizing.value) return;
  isResizing.value = false;
  applyResizeBodyState(false);
  window.removeEventListener('pointermove', handleResizeMove);
  window.removeEventListener('pointerup', stopResize);
  window.removeEventListener('pointercancel', stopResize);
  persistDesktopWidth();
}

function startResize(event: PointerEvent) {
  if (!isDockOpen.value) return;
  resizeStartX = event.clientX;
  resizeStartWidth = desktopWidth.value;
  isResizing.value = true;
  applyResizeBodyState(true);
  window.addEventListener('pointermove', handleResizeMove);
  window.addEventListener('pointerup', stopResize);
  window.addEventListener('pointercancel', stopResize);
}

watch(
  () => route.params.sessionId,
  (sessionId) => {
    if (typeof sessionId === 'string') {
      dockStore.setActiveSessionId(sessionId);
    }
  },
  { immediate: true },
);

watch(
  () => [activeSessionId.value, isAssistantRoute.value] as const,
  async ([sessionId, assistantRoute]) => {
    if (assistantRoute) return;
    if (!sessionId) {
      sessionTitle.value = '새 채팅';
      messages.value = [];
      return;
    }
    if (isResponding.value) return;
    await loadSessionDetail(sessionId);
  },
  { immediate: true },
);

onMounted(async () => {
  if (typeof window !== 'undefined') {
    const savedWidth = Number(window.localStorage.getItem(DESKTOP_WIDTH_STORAGE_KEY));
    if (Number.isFinite(savedWidth)) {
      desktopWidth.value = clampDockWidth(savedWidth);
    }
  }

  if (!isAssistantRoute.value && activeSessionId.value) {
    await loadSessionDetail(activeSessionId.value);
  }
});

onBeforeUnmount(() => {
  streamAbortController.value?.abort();
  clearScheduledTitleRefresh();
  stopResize();
});
</script>
