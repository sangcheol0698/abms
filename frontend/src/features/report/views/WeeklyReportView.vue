<template>
  <FeatureSplitLayout
    :sidebar-default-size="24"
    :sidebar-min-size="16"
    :sidebar-max-size="32"
    :content-min-size="56"
    :use-viewport-height="false"
  >
    <template #sidebar>
      <div class="flex h-full min-h-0 flex-col border-r border-border/60 bg-background">
        <div class="border-b border-border/60 px-4 py-4">
          <div class="space-y-1">
            <h2 class="text-base font-semibold text-foreground">주간 운영 보고서</h2>
            <p class="text-sm text-muted-foreground">지난주 월-일 기준 초안을 생성합니다.</p>
          </div>
        </div>

        <div class="space-y-4 px-4 py-4">
          <div class="grid gap-3">
            <div class="grid gap-1.5">
              <Label for="weekly-report-start">시작일</Label>
              <Input id="weekly-report-start" v-model="weekStart" type="date" />
            </div>
            <div class="grid gap-1.5">
              <Label for="weekly-report-end">종료일</Label>
              <Input id="weekly-report-end" v-model="weekEnd" type="date" />
            </div>
          </div>

          <Button class="w-full gap-2" :disabled="isGenerating" @click="handleGenerate">
            <Sparkles class="h-4 w-4" />
            {{ isGenerating ? '요청 중...' : '초안 생성' }}
          </Button>

          <p v-if="errorMessage" class="text-sm text-destructive">{{ errorMessage }}</p>
        </div>

        <div class="flex-1 overflow-y-auto border-t border-border/60 px-4 py-4">
          <div class="flex items-center justify-between pb-2">
            <h3 class="text-sm font-medium text-foreground">최근 초안</h3>
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
              class="w-full rounded-xl border p-3 text-left transition-colors"
              :class="selectedDraftId === draft.id ? 'border-primary bg-primary/5' : 'border-border hover:bg-muted/40'"
              @click="selectedDraftId = draft.id"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <p class="truncate font-medium text-foreground">{{ draft.title }}</p>
                  <p class="text-xs text-muted-foreground">{{ draft.weekStart }} ~ {{ draft.weekEnd }}</p>
                </div>
                <Badge variant="outline" class="shrink-0 text-[11px]">{{ draft.statusDescription }}</Badge>
              </div>
              <div class="mt-3 grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                <div>직원 {{ draft.snapshotSummary.totalEmployees }}명</div>
                <div>진행 프로젝트 {{ draft.snapshotSummary.inProgressProjects }}건</div>
                <div>신규 시작 {{ draft.snapshotSummary.startedProjects }}건</div>
                <div>종료 {{ draft.snapshotSummary.endedProjects }}건</div>
              </div>
            </button>
          </div>
        </div>
      </div>
    </template>

    <template #default>
      <div class="flex h-full min-h-0 flex-col bg-background">
        <div class="flex items-center justify-between border-b border-border/60 px-5 py-4">
          <div>
            <h1 class="text-lg font-semibold text-foreground">{{ selectedDraft?.title ?? '주간 운영 보고서' }}</h1>
            <p class="text-sm text-muted-foreground">
              {{ selectedDraft ? `${selectedDraft.weekStart} ~ ${selectedDraft.weekEnd}` : '초안을 선택하거나 새로 생성하세요.' }}
            </p>
          </div>

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
            v-if="selectedDraftDetail && isSelectedDraftRunning"
            variant="destructive"
            class="ml-2 gap-2"
            :disabled="isCancelling"
            @click="handleCancel"
          >
            <Loader2 v-if="isCancelling" class="h-4 w-4 animate-spin" />
            <span>{{ isCancelling ? '중지 요청 중...' : '중지' }}</span>
          </Button>
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

          <Card class="min-h-[320px]">
            <CardHeader>
              <CardTitle>AI 초안</CardTitle>
              <CardDescription>Markdown 기반 읽기 전용 초안입니다.</CardDescription>
            </CardHeader>
            <CardContent>
              <MarkdownRenderer :content="selectedDraftDetail.reportMarkdown" />
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>스냅샷 JSON</CardTitle>
              <CardDescription>AI 생성에 사용된 구조화 원본 데이터입니다.</CardDescription>
            </CardHeader>
            <CardContent>
              <pre class="overflow-x-auto rounded-lg bg-muted/50 p-4 text-xs leading-5 text-foreground">{{ selectedDraftDetail.snapshotJson }}</pre>
            </CardContent>
          </Card>
        </div>

        <div v-else class="flex flex-1 items-center justify-center px-6 text-sm text-muted-foreground">
          초안을 선택하면 상세 내용을 확인할 수 있습니다.
        </div>
      </div>
    </template>
  </FeatureSplitLayout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Loader2, RefreshCcw, Sparkles } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import MarkdownRenderer from '@/features/chat/components/MarkdownRenderer.vue';
import { isWeeklyReportTerminalStatus } from '@/features/report/models/weeklyReport';
import {
  useCreateWeeklyReportDraftMutation,
  useCancelWeeklyReportDraftMutation,
  useRegenerateWeeklyReportDraftMutation,
  useWeeklyReportDraftDetailQuery,
  useWeeklyReportDraftListQuery,
} from '@/features/report/queries/useWeeklyReportQueries';

const draftListQuery = useWeeklyReportDraftListQuery();
const createDraftMutation = useCreateWeeklyReportDraftMutation();
const regenerateDraftMutation = useRegenerateWeeklyReportDraftMutation();
const cancelDraftMutation = useCancelWeeklyReportDraftMutation();

const weekStart = ref('');
const weekEnd = ref('');
const selectedDraftId = ref<number | null>(null);
const errorMessage = ref('');

const detailQuery = useWeeklyReportDraftDetailQuery(selectedDraftId);

const drafts = computed(() => draftListQuery.data.value ?? []);
const selectedDraft = computed(() => drafts.value.find((draft) => draft.id === selectedDraftId.value) ?? null);
const selectedDraftDetail = computed(() => detailQuery.data.value ?? null);
const isListPending = computed(() => draftListQuery.isLoading.value && drafts.value.length === 0);
const isDetailPending = computed(() => detailQuery.isLoading.value && selectedDraftDetail.value == null);
const isGenerating = computed(() => createDraftMutation.isPending.value);
const isRegenerating = computed(() => regenerateDraftMutation.isPending.value);
const isCancelling = computed(() => cancelDraftMutation.isPending.value);
const isSelectedDraftRunning = computed(() =>
  selectedDraftDetail.value != null && !isWeeklyReportTerminalStatus(selectedDraftDetail.value.status),
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
      return;
    }

    const firstDraft = items[0];
    if (!firstDraft) {
      selectedDraftId.value = null;
      return;
    }

    if (selectedDraftId.value == null || !items.some((item) => item.id === selectedDraftId.value)) {
      selectedDraftId.value = firstDraft.id;
    }
  },
  { immediate: true },
);

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function initializeDefaultWeekRange() {
  const today = new Date();
  const lastWeekReference = new Date(today);
  lastWeekReference.setDate(today.getDate() - 7);

  const day = lastWeekReference.getDay();
  const mondayOffset = day === 0 ? -6 : 1 - day;
  const monday = new Date(lastWeekReference);
  monday.setDate(lastWeekReference.getDate() + mondayOffset);

  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);

  weekStart.value = toIsoDate(monday);
  weekEnd.value = toIsoDate(sunday);
}

initializeDefaultWeekRange();

async function handleGenerate() {
  errorMessage.value = '';
  try {
    const detail = await createDraftMutation.mutateAsync({
      weekStart: weekStart.value || undefined,
      weekEnd: weekEnd.value || undefined,
    });
    selectedDraftId.value = detail.id;
    await draftListQuery.refetch();
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
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '주간 보고서 생성 중지에 실패했습니다.';
  }
}
</script>
