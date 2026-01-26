<template>
  <div
    v-if="!party"
    class="rounded-lg border border-dashed border-border/60 bg-muted/10 p-6 text-sm text-muted-foreground"
  >
    협력사 정보를 찾을 수 없습니다.
  </div>
  <div v-else class="flex flex-col gap-4 rounded-lg md:flex-row md:items-center md:gap-6">
    <div class="flex h-20 w-20 items-center justify-center rounded-2xl border border-border/60 bg-primary/10">
      <Building class="h-10 w-10 text-primary" />
    </div>
    <div class="flex flex-1 flex-col gap-1">
      <h1 class="text-2xl font-semibold tracking-tight text-foreground">{{ party.name }}</h1>
      <div class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground/80">
        <span v-if="party.ceo">대표: {{ party.ceo }}</span>
        <span v-if="party.ceo && party.manager" class="text-border/60">|</span>
        <span v-if="party.manager">담당자: {{ party.manager }}</span>
      </div>
    </div>
    <div class="flex items-center gap-2">
      <Button variant="outline" size="sm" class="text-destructive hover:text-destructive" @click="emit('delete')">
        <Trash2 class="mr-2 h-4 w-4" />
        삭제
      </Button>
      <Button variant="outline" size="sm" @click="emit('edit')">
        <Pencil class="mr-2 h-4 w-4" />
        협력사 편집
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Building, Pencil, Trash2 } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import type { PartyDetail } from '@/features/party/models/partyDetail';

interface Props {
  party: PartyDetail | null;
}

defineProps<Props>();
const emit = defineEmits<{
  edit: [];
  delete: [];
}>();
</script>
