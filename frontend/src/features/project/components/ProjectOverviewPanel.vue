<template>
  <div
    v-if="!project"
    class="flex h-full items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-6 text-sm text-muted-foreground"
  >
    프로젝트 정보를 불러오지 못했습니다.
  </div>
  <div v-else class="grid gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
    <div class="space-y-6">
      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="mb-4 text-sm font-semibold text-muted-foreground">기본 정보</h2>
        <dl class="grid gap-3 text-sm">
          <div class="grid grid-cols-[100px_1fr] gap-2">
            <dt class="text-muted-foreground">프로젝트 코드</dt>
            <dd class="font-medium text-foreground">{{ project.code }}</dd>
          </div>
          <div class="grid grid-cols-[100px_1fr] gap-2">
            <dt class="text-muted-foreground">상태</dt>
            <dd>
              <Badge :variant="statusVariant">{{ project.statusLabel }}</Badge>
            </dd>
          </div>
          <div class="grid grid-cols-[100px_1fr] gap-2">
            <dt class="text-muted-foreground">시작일</dt>
            <dd class="font-medium text-foreground">{{ formatDate(project.startDate) }}</dd>
          </div>
          <div class="grid grid-cols-[100px_1fr] gap-2">
            <dt class="text-muted-foreground">종료일</dt>
            <dd class="font-medium text-foreground">{{ formatDate(project.endDate) }}</dd>
          </div>
        </dl>
      </section>

      <section class="rounded-lg border border-border/60 bg-background p-5">
        <h2 class="mb-4 text-sm font-semibold text-muted-foreground">계약 정보</h2>
        <dl class="grid gap-3 text-sm">
          <div class="grid grid-cols-[100px_1fr] gap-2">
            <dt class="text-muted-foreground">거래처</dt>
            <dd>
              <button
                type="button"
                class="font-medium text-primary underline underline-offset-4 transition hover:text-primary/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:text-muted-foreground"
                :disabled="!project.partyId"
                @click="handlePartyClick"
              >
                {{ project.partyName || '—' }}
              </button>
            </dd>
          </div>
          <div class="grid grid-cols-[100px_1fr] gap-2">
            <dt class="text-muted-foreground">계약금액</dt>
            <dd class="font-medium text-foreground">{{ formatCurrency(project.contractAmount) }}</dd>
          </div>
        </dl>
      </section>
    </div>

    <section class="rounded-lg border border-border/60 bg-background p-5">
      <h2 class="mb-4 text-sm font-semibold text-muted-foreground">프로젝트 설명</h2>
      <p class="text-sm leading-relaxed text-muted-foreground">
        {{ project.description || '등록된 설명이 없습니다.' }}
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Badge } from '@/components/ui/badge';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import { formatCurrency } from '@/features/project/models/projectListItem';

interface Props {
  project: ProjectDetail | null;
  formatDate: (value?: string | null) => string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  'party-click': [];
}>();

const statusVariant = computed(() => {
  const status = props.project?.status;
  switch (status) {
    case 'IN_PROGRESS':
      return 'default';
    case 'COMPLETED':
      return 'secondary';
    case 'CANCELLED':
      return 'destructive';
    default:
      return 'outline';
  }
});

function handlePartyClick() {
  if (!props.project?.partyId) {
    return;
  }
  emit('party-click');
}
</script>
