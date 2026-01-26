<template>
  <div
    v-if="!party"
    class="flex h-full items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-6 text-sm text-muted-foreground"
  >
    협력사 정보를 불러오지 못했습니다.
  </div>
  <div v-else class="grid gap-6 lg:grid-cols-2">
    <section class="rounded-lg border border-border/60 bg-background p-5">
      <h2 class="mb-4 text-sm font-semibold text-muted-foreground">기본 정보</h2>
      <dl class="grid gap-3 text-sm">
        <div class="grid grid-cols-[100px_1fr] gap-2">
          <dt class="text-muted-foreground">협력사명</dt>
          <dd class="font-medium text-foreground">{{ party.name }}</dd>
        </div>
        <div class="grid grid-cols-[100px_1fr] gap-2">
          <dt class="text-muted-foreground">대표자</dt>
          <dd class="text-foreground">{{ party.ceo || '—' }}</dd>
        </div>
      </dl>
    </section>

    <section class="rounded-lg border border-border/60 bg-background p-5">
      <h2 class="mb-4 text-sm font-semibold text-muted-foreground">담당자 정보</h2>
      <dl class="grid gap-3 text-sm">
        <div class="grid grid-cols-[100px_1fr] gap-2">
          <dt class="text-muted-foreground">담당자</dt>
          <dd class="text-foreground">{{ party.manager || '—' }}</dd>
        </div>
        <div class="grid grid-cols-[100px_1fr] gap-2">
          <dt class="text-muted-foreground">연락처</dt>
          <dd class="text-foreground">{{ party.contact || '—' }}</dd>
        </div>
        <div class="grid grid-cols-[100px_1fr] gap-2">
          <dt class="text-muted-foreground">이메일</dt>
          <dd class="text-foreground">{{ party.email || '—' }}</dd>
        </div>
      </dl>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { PartyDetail } from '@/features/party/models/partyDetail';

interface Props {
  party: PartyDetail | null;
}

defineProps<Props>();
</script>
