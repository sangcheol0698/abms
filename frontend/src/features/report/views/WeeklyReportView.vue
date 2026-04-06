<template>
  <FeatureSplitLayout
    :sidebar-default-size="24"
    :sidebar-min-size="16"
    :sidebar-max-size="32"
    :content-min-size="56"
    :use-viewport-height="false"
  >
    <template #sidebar="{ pane }">
      <div class="flex h-full min-h-0 flex-col border-r border-border/60 bg-background">
        <div class="px-4 py-4">
          <Button class="w-full gap-2" :disabled="isGenerating" @click="handleGenerate(pane)">
            <Sparkles class="h-4 w-4" />
            {{ isGenerating ? '요청 중...' : '초안 생성' }}
          </Button>
        </div>

        <div class="border-y border-border/60 px-4 py-4">
          <div class="grid gap-3">
            <div class="grid gap-1.5">
              <Label for="weekly-report-anchor">주차 선택</Label>
              <DatePicker
                id="weekly-report-anchor"
                :model-value="weekAnchorDate"
                placeholder="주차를 선택하세요"
                @update:modelValue="handleWeekSelect"
              />
            </div>
            <div class="rounded-lg border border-border/60 bg-muted/30 px-3 py-2 text-sm">
              <p class="font-medium text-foreground">선택한 주차</p>
              <p class="mt-1 text-muted-foreground">
                {{ selectedWeekLabel }}
              </p>
            </div>
          </div>
        </div>

        <nav class="flex-1 overflow-y-auto px-4 pb-5 text-sm">
          <div class="flex items-center justify-between py-4">
            <h3 class="text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
              최근 초안
            </h3>
            <Loader2 v-if="isListPending" class="h-4 w-4 animate-spin text-muted-foreground" />
          </div>

          <div v-if="drafts.length === 0 && !isListPending" class="rounded-xl border border-dashed p-4 text-sm text-muted-foreground">
            생성된 초안이 없습니다.
          </div>

          <div v-else class="space-y-3">
            <button
              v-for="draft in drafts"
              :key="draft.id"
              type="button"
              class="w-full rounded-xl border px-3 py-2.5 text-left transition-colors"
              :class="selectedDraftId === draft.id ? 'border-primary bg-primary/5' : 'border-border hover:bg-muted/40'"
              @click="handleDraftSelect(draft.id, pane)"
            >
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0 space-y-1">
                  <p class="truncate text-sm font-medium text-foreground">{{ draft.title }}</p>
                  <p class="truncate text-[11px] text-muted-foreground">
                    {{ draft.weekStart }} ~ {{ draft.weekEnd }}
                  </p>
                </div>
                <Badge variant="outline" class="shrink-0 text-[11px]">{{ draft.statusDescription }}</Badge>
              </div>
              <div class="mt-2 flex gap-3 text-[11px] text-muted-foreground">
                <span>직원 {{ draft.snapshotSummary.totalEmployees }}</span>
                <span>프로젝트 {{ draft.snapshotSummary.inProgressProjects }}</span>
              </div>
            </button>
          </div>

          <p v-if="errorMessage" class="mt-4 text-sm text-destructive">{{ errorMessage }}</p>
        </nav>
      </div>
    </template>

    <template #default="{ pane }">
      <div class="flex h-full min-h-0 flex-col bg-background">
        <div class="flex items-center justify-between border-b border-border/60 px-5 py-4">
          <div class="flex items-center gap-3">
            <Button
              variant="ghost"
              size="icon"
              class="-ml-1 h-8 w-8 text-muted-foreground transition hover:text-foreground"
              aria-label="주간 보고서 사이드바 토글"
              @click="pane.toggleSidebar()"
            >
              <Menu
                class="h-4 w-4 transition"
                :class="pane.isSidebarCollapsed.value ? 'rotate-180' : ''"
              />
            </Button>
            <div>
              <h1 class="text-lg font-semibold text-foreground">{{ selectedDraft?.title ?? '주간 운영 보고서' }}</h1>
              <p class="text-sm text-muted-foreground">
                {{ selectedDraft ? `${selectedDraft.weekStart} ~ ${selectedDraft.weekEnd}` : '초안을 선택하거나 새로 생성하세요.' }}
              </p>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <Button
              variant="outline"
              class="gap-2"
              :disabled="!selectedDraft || isRegenerating || isSelectedDraftRunning"
              @click="handleRegenerate"
            >
              <RefreshCcw class="h-4 w-4" />
              {{ isRegenerating ? '요청 중...' : '다시 생성' }}
            </Button>
            <Button
              v-if="selectedDraftDetail && canDeleteSelectedDraft"
              variant="destructive"
              class="gap-2"
              @click="isDeleteDialogOpen = true"
            >
              <Trash2 class="h-4 w-4" />
              삭제
            </Button>
            <Button
              v-if="selectedDraftDetail && isSelectedDraftRunning"
              variant="destructive"
              class="gap-2"
              :disabled="isCancelling"
              @click="handleCancel"
            >
              <Loader2 v-if="isCancelling" class="h-4 w-4 animate-spin" />
              <span>{{ isCancelling ? '중지 요청 중...' : '중지' }}</span>
            </Button>
          </div>
        </div>

        <div v-if="isDetailPending" class="flex flex-1 items-center justify-center">
          <Loader2 class="h-5 w-5 animate-spin text-muted-foreground" />
        </div>

        <div
          v-else-if="selectedDraftDetail && isSelectedDraftRunning"
          class="flex flex-1 items-center justify-center px-6"
        >
          <Card class="w-full max-w-2xl">
            <CardHeader class="space-y-3">
              <div class="flex items-center gap-2">
                <Loader2 class="h-4 w-4 animate-spin text-primary" />
                <CardTitle>{{ selectedDraftDetail.statusDescription }}</CardTitle>
              </div>
              <CardDescription>
                백그라운드에서 주간 운영 보고서 초안을 생성하고 있습니다. 화면은 자동으로 갱신됩니다.
              </CardDescription>
              <Progress :model-value="progressValue" class="h-2" />
            </CardHeader>
            <CardContent class="space-y-2 text-sm text-muted-foreground">
              <p>{{ progressMessage }}</p>
              <p>이 화면을 벗어나도 생성은 계속 진행됩니다.</p>
            </CardContent>
          </Card>
        </div>

        <div
          v-else-if="selectedDraftDetail && selectedDraftDetail.status === 'CANCELLED'"
          class="flex flex-1 items-center justify-center px-6"
        >
          <Card class="w-full max-w-2xl">
            <CardHeader>
              <CardTitle>보고서 생성이 중지되었습니다</CardTitle>
              <CardDescription>필요하면 다시 생성 버튼으로 동일 범위를 재시도할 수 있습니다.</CardDescription>
            </CardHeader>
          </Card>
        </div>

        <div
          v-else-if="selectedDraftDetail && selectedDraftDetail.status === 'FAILED'"
          class="flex flex-1 items-center justify-center px-6"
        >
          <Card class="w-full max-w-2xl">
            <CardHeader>
              <CardTitle class="text-destructive">보고서 생성 실패</CardTitle>
              <CardDescription>
                {{ selectedDraftDetail.failureReason || '백그라운드 생성 중 오류가 발생했습니다.' }}
              </CardDescription>
            </CardHeader>
            <CardContent class="space-y-3">
              <p class="text-sm text-muted-foreground">
                다시 생성 버튼을 눌러 동일 범위로 재시도할 수 있습니다.
              </p>
            </CardContent>
          </Card>
        </div>

        <div v-else-if="selectedDraftDetail" class="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto px-5 py-5">
          <div class="grid gap-4 md:grid-cols-5">
            <Card>
              <CardHeader class="pb-2">
                <CardDescription>총 직원</CardDescription>
                <CardTitle class="text-2xl">{{ selectedDraftDetail.snapshotSummary.totalEmployees }}</CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader class="pb-2">
                <CardDescription>휴직 인원</CardDescription>
                <CardTitle class="text-2xl">{{ selectedDraftDetail.snapshotSummary.onLeaveEmployees }}</CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader class="pb-2">
                <CardDescription>진행 프로젝트</CardDescription>
                <CardTitle class="text-2xl">{{ selectedDraftDetail.snapshotSummary.inProgressProjects }}</CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader class="pb-2">
                <CardDescription>신규 시작</CardDescription>
                <CardTitle class="text-2xl">{{ selectedDraftDetail.snapshotSummary.startedProjects }}</CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader class="pb-2">
                <CardDescription>월 매출 집계</CardDescription>
                <CardTitle class="text-base">
                  {{ selectedDraftDetail.snapshotSummary.monthlyRevenueAvailable ? '사용 가능' : '미계산' }}
                </CardTitle>
              </CardHeader>
            </Card>
          </div>

          <Card class="min-h-[320px] overflow-hidden">
            <CardHeader class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
              <div class="space-y-1">
                <CardTitle>AI 초안</CardTitle>
                <CardDescription>Markdown 기반 읽기 전용 초안입니다.</CardDescription>
              </div>
              <div
                v-if="selectedDraftDetail && canEditSelectedDraft"
                class="flex flex-wrap items-center gap-2"
              >
                <Button variant="outline" size="sm" @click="openEditDialog">
                  <Pencil class="mr-1 h-4 w-4" />
                  수정
                </Button>
                <Button variant="outline" size="sm" @click="handleCopy">
                  <Clipboard class="mr-1 h-4 w-4" />
                  복사
                </Button>
              </div>
            </CardHeader>
            <CardContent class="min-h-0 min-w-0 overflow-hidden">
              <div class="max-h-[50vh] min-w-0 max-w-full overflow-auto pr-1">
                <MarkdownRenderer :content="selectedDraftDetail.reportMarkdown" />
                <div class="h-8" aria-hidden="true" />
              </div>
            </CardContent>
          </Card>

        </div>

        <div v-else class="flex flex-1 items-center justify-center px-6 text-sm text-muted-foreground">
          초안을 선택하면 상세 내용을 확인할 수 있습니다.
        </div>
      </div>
    </template>
  </FeatureSplitLayout>

  <Dialog :open="isEditDialogOpen" @update:open="isEditDialogOpen = $event">
    <DialogContent class="max-h-[85vh] sm:max-w-3xl">
      <DialogHeader>
        <DialogTitle>주간 보고서 수정</DialogTitle>
        <DialogDescription>제목과 Markdown 초안을 직접 수정할 수 있습니다.</DialogDescription>
      </DialogHeader>

      <div class="grid max-h-[calc(85vh-11rem)] gap-4 overflow-hidden">
        <div class="grid gap-1.5">
          <Label for="weekly-report-edit-title">제목</Label>
          <Input id="weekly-report-edit-title" v-model="editTitle" />
        </div>
        <div class="grid min-h-0 gap-1.5 overflow-hidden">
          <Label for="weekly-report-edit-markdown">Markdown 본문</Label>
          <Textarea
            id="weekly-report-edit-markdown"
            v-model="editMarkdown"
            class="min-h-[360px] max-h-full overflow-y-auto font-mono text-sm"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="isEditDialogOpen = false">취소</Button>
        <Button :disabled="isUpdating" @click="handleUpdate">
          {{ isUpdating ? '저장 중...' : '저장' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <AlertDialog :open="isDeleteDialogOpen" @update:open="isDeleteDialogOpen = $event">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>보고서를 삭제할까요?</AlertDialogTitle>
        <AlertDialogDescription>
          삭제한 보고서는 목록에서 사라집니다.
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel>취소</AlertDialogCancel>
        <AlertDialogAction
          class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          :disabled="isDeleting"
          @click="handleDelete"
        >
          {{ isDeleting ? '삭제 중...' : '삭제' }}
        </AlertDialogAction>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Clipboard, Loader2, Menu, Pencil, RefreshCcw, Sparkles, Trash2 } from 'lucide-vue-next';
import { useRoute, useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { DatePicker } from '@/components/ui/date-picker';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import MarkdownRenderer from '@/features/chat/components/MarkdownRenderer.vue';
import { isWeeklyReportTerminalStatus } from '@/features/report/models/weeklyReport';
import { copyTextToClipboard } from '@/core/utils/clipboard';
import {
  useCreateWeeklyReportDraftMutation,
  useCancelWeeklyReportDraftMutation,
  useDeleteWeeklyReportDraftMutation,
  useRegenerateWeeklyReportDraftMutation,
  useUpdateWeeklyReportDraftMutation,
  useWeeklyReportDraftDetailQuery,
  useWeeklyReportDraftListQuery,
} from '@/features/report/queries/useWeeklyReportQueries';
import { toast } from 'vue-sonner';
import type { FeatureSplitPaneContext } from '@/core/composables/useFeatureSplitPane';

const route = useRoute();
const router = useRouter();
const draftListQuery = useWeeklyReportDraftListQuery();
const createDraftMutation = useCreateWeeklyReportDraftMutation();
const regenerateDraftMutation = useRegenerateWeeklyReportDraftMutation();
const cancelDraftMutation = useCancelWeeklyReportDraftMutation();
const updateDraftMutation = useUpdateWeeklyReportDraftMutation();
const deleteDraftMutation = useDeleteWeeklyReportDraftMutation();

const weekAnchorDate = ref<Date | null>(null);
const selectedDraftId = ref<number | null>(null);
const errorMessage = ref('');
const isEditDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const editTitle = ref('');
const editMarkdown = ref('');

const detailQuery = useWeeklyReportDraftDetailQuery(selectedDraftId);

const drafts = computed(() => draftListQuery.data.value ?? []);
const selectedDraft = computed(() => drafts.value.find((draft) => draft.id === selectedDraftId.value) ?? null);
const selectedDraftDetail = computed(() => detailQuery.data.value ?? null);
const isListPending = computed(() => draftListQuery.isLoading.value && drafts.value.length === 0);
const isDetailPending = computed(() => detailQuery.isLoading.value && selectedDraftDetail.value == null);
const isGenerating = computed(() => createDraftMutation.isPending.value);
const isRegenerating = computed(() => regenerateDraftMutation.isPending.value);
const isCancelling = computed(() => cancelDraftMutation.isPending.value);
const isUpdating = computed(() => updateDraftMutation.isPending.value);
const isDeleting = computed(() => deleteDraftMutation.isPending.value);
const isSelectedDraftRunning = computed(() =>
  selectedDraftDetail.value != null && !isWeeklyReportTerminalStatus(selectedDraftDetail.value.status),
);
const canEditSelectedDraft = computed(() =>
  selectedDraftDetail.value != null && selectedDraftDetail.value.status === 'DRAFT',
);
const canDeleteSelectedDraft = computed(() =>
  selectedDraftDetail.value != null && isWeeklyReportTerminalStatus(selectedDraftDetail.value.status),
);
const progressValue = computed(() => {
  switch (selectedDraftDetail.value?.status) {
    case 'PENDING':
      return 15;
    case 'COLLECTING':
      return 45;
    case 'GENERATING':
      return 80;
    case 'DRAFT':
      return 100;
    case 'CANCELLED':
      return 0;
    default:
      return 0;
  }
});
const progressMessage = computed(() => {
  switch (selectedDraftDetail.value?.status) {
    case 'PENDING':
      return '생성 요청을 접수했습니다.';
    case 'COLLECTING':
      return '보고서에 필요한 조직, 프로젝트, 매출 데이터를 집계하고 있습니다.';
    case 'GENERATING':
      return 'AI가 Markdown 초안을 작성하고 있습니다.';
    case 'CANCELLED':
      return '생성 작업을 중지했습니다.';
    default:
      return '';
  }
});

watch(
  drafts,
  (items) => {
    if (items.length === 0) {
      selectedDraftId.value = null;
      if (route.name === 'weekly-report-detail') {
        void router.replace({ name: 'weekly-report' });
      }
      return;
    }

    const routeDraftId = Number(route.params.draftId ?? 0);
    if (routeDraftId > 0 && items.some((item) => item.id === routeDraftId)) {
      selectedDraftId.value = routeDraftId;
      return;
    }

    const firstDraft = items[0];
    if (!firstDraft) {
      selectedDraftId.value = null;
      return;
    }

    if (selectedDraftId.value == null || !items.some((item) => item.id === selectedDraftId.value)) {
      selectedDraftId.value = firstDraft.id;
      void router.replace({ name: 'weekly-report-detail', params: { draftId: firstDraft.id } });
    }
  },
  { immediate: true },
);

watch(
  () => route.params.draftId,
  (draftId) => {
    if (typeof draftId !== 'string') {
      if (route.name === 'weekly-report') {
        selectedDraftId.value = null;
      }
      return;
    }

    const parsedDraftId = Number(draftId);
    selectedDraftId.value = Number.isNaN(parsedDraftId) ? null : parsedDraftId;
  },
  { immediate: true },
);

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function startOfWeek(date: Date): Date {
  const base = new Date(date);
  const day = base.getDay();
  const mondayOffset = day === 0 ? -6 : 1 - day;
  base.setDate(base.getDate() + mondayOffset);
  base.setHours(0, 0, 0, 0);
  return base;
}

function endOfWeek(date: Date): Date {
  const monday = startOfWeek(date);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  sunday.setHours(0, 0, 0, 0);
  return sunday;
}

function formatDisplayDate(date: Date): string {
  return toIsoDate(date);
}

function initializeDefaultWeek() {
  const today = new Date();
  const lastWeekReference = new Date(today);
  lastWeekReference.setDate(today.getDate() - 7);
  weekAnchorDate.value = startOfWeek(lastWeekReference);
}

const selectedWeekStart = computed(() => (weekAnchorDate.value ? startOfWeek(weekAnchorDate.value) : null));
const selectedWeekEnd = computed(() => (weekAnchorDate.value ? endOfWeek(weekAnchorDate.value) : null));
const selectedWeekLabel = computed(() => {
  if (!selectedWeekStart.value || !selectedWeekEnd.value) {
    return '주차를 선택하면 월요일부터 일요일까지 자동 계산됩니다.';
  }
  return `${formatDisplayDate(selectedWeekStart.value)} ~ ${formatDisplayDate(selectedWeekEnd.value)}`;
});

initializeDefaultWeek();

function handleWeekSelect(value: Date | null) {
  weekAnchorDate.value = value;
}

function openEditDialog() {
  if (!selectedDraftDetail.value) {
    return;
  }
  editTitle.value = selectedDraftDetail.value.title;
  editMarkdown.value = selectedDraftDetail.value.reportMarkdown;
  isEditDialogOpen.value = true;
}

function handleDraftSelect(draftId: number, pane?: FeatureSplitPaneContext) {
  if (selectedDraftId.value === draftId) {
    if (pane && !pane.isLargeScreen.value) {
      pane.closeSidebar();
    }
    return;
  }

  selectedDraftId.value = draftId;
  void router.push({ name: 'weekly-report-detail', params: { draftId } });

  if (pane && !pane.isLargeScreen.value) {
    pane.closeSidebar();
  }
}

async function handleGenerate(pane?: FeatureSplitPaneContext) {
  errorMessage.value = '';
  try {
    const detail = await createDraftMutation.mutateAsync({
      weekStart: selectedWeekStart.value ? toIsoDate(selectedWeekStart.value) : undefined,
      weekEnd: selectedWeekEnd.value ? toIsoDate(selectedWeekEnd.value) : undefined,
    });
    selectedDraftId.value = detail.id;
    await router.replace({ name: 'weekly-report-detail', params: { draftId: detail.id } });
    await draftListQuery.refetch();
    if (pane && !pane.isLargeScreen.value) {
      pane.closeSidebar();
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '주간 보고서 초안 생성에 실패했습니다.';
  }
}

async function handleRegenerate() {
  if (!selectedDraftId.value) {
    return;
  }

  errorMessage.value = '';
  try {
    const detail = await regenerateDraftMutation.mutateAsync(selectedDraftId.value);
    selectedDraftId.value = detail.id;
    await draftListQuery.refetch();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '주간 보고서 재생성에 실패했습니다.';
  }
}

async function handleCancel() {
  if (!selectedDraftId.value) {
    return;
  }

  errorMessage.value = '';
  try {
    const detail = await cancelDraftMutation.mutateAsync(selectedDraftId.value);
    selectedDraftId.value = detail.id;
    await draftListQuery.refetch();
    await router.replace({ name: 'weekly-report-detail', params: { draftId: detail.id } });
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '주간 보고서 생성 중지에 실패했습니다.';
  }
}

async function handleUpdate() {
  if (!selectedDraftId.value) {
    return;
  }

  errorMessage.value = '';
  try {
    const detail = await updateDraftMutation.mutateAsync({
      draftId: selectedDraftId.value,
      title: editTitle.value,
      reportMarkdown: editMarkdown.value,
    });
    selectedDraftId.value = detail.id;
    isEditDialogOpen.value = false;
    await draftListQuery.refetch();
    await router.replace({ name: 'weekly-report-detail', params: { draftId: detail.id } });
    toast.success('주간 보고서를 저장했습니다.');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '주간 보고서 수정에 실패했습니다.';
  }
}

async function handleDelete() {
  if (!selectedDraftId.value) {
    return;
  }

  const targetDraftId = selectedDraftId.value;
  errorMessage.value = '';
  try {
    await deleteDraftMutation.mutateAsync(targetDraftId);
    isDeleteDialogOpen.value = false;
    selectedDraftId.value = null;
    await router.push({ name: 'weekly-report' });
    await draftListQuery.refetch();
    toast.success('주간 보고서를 삭제했습니다.');
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '주간 보고서 삭제에 실패했습니다.';
  }
}

async function handleCopy() {
  if (!selectedDraftDetail.value) {
    return;
  }

  try {
    const result = await copyTextToClipboard(selectedDraftDetail.value.reportMarkdown);
    if (result === 'copied') {
      toast.success('보고서 본문을 복사했습니다.');
      return;
    }
    toast.info('자동 복사가 지원되지 않아 복사 창을 열었습니다.');
  } catch (error) {
    console.error('Failed to copy weekly report:', error);
    toast.error('보고서 복사에 실패했습니다.');
  }
}
</script>
