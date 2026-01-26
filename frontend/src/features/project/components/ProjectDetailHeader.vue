<template>
  <div
    v-if="!project"
    class="rounded-lg border border-dashed border-border/60 bg-muted/10 p-6 text-sm text-muted-foreground"
  >
    프로젝트 정보를 찾을 수 없습니다.
  </div>
  <div v-else class="flex flex-col gap-4 rounded-lg md:flex-row md:items-center md:gap-6">
    <div class="flex h-20 w-20 items-center justify-center rounded-2xl border border-border/60 bg-primary/10">
      <FolderKanban class="h-10 w-10 text-primary" />
    </div>
    <div class="flex flex-1 flex-col gap-1">
      <div class="flex items-center gap-2">
        <span class="text-sm font-medium text-muted-foreground">{{ project.code }}</span>
      </div>
      <h1 class="text-2xl font-semibold tracking-tight text-foreground">{{ project.name }}</h1>
      <div class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground/80">
        <Badge :variant="statusVariant">{{ project.statusLabel }}</Badge>
        <span class="text-border/60">|</span>
        <button
          type="button"
          class="font-medium text-primary underline underline-offset-4 transition hover:text-primary/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:text-muted-foreground"
          :disabled="!project.partyId"
          @click="emit('party-click')"
        >
          {{ project.partyName || '—' }}
        </button>
        <span class="text-border/60">|</span>
        <span class="font-medium">{{ formatCurrency(project.contractAmount) }}</span>
      </div>
    </div>
    <div class="flex items-center gap-2">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { FolderKanban } from 'lucide-vue-next';
import { Badge } from '@/components/ui/badge';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import { formatCurrency } from '@/features/project/models/projectListItem';

interface Props {
  project: ProjectDetail | null;
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
</script>
