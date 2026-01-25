<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="w-full max-w-2xl overflow-hidden">
      <DialogHeader>
        <DialogTitle>협력사 선택</DialogTitle>
        <DialogDescription>목록에서 협력사를 선택하세요.</DialogDescription>
      </DialogHeader>

      <!-- 검색 입력 -->
      <div class="px-1">
        <Input
          v-model="searchQuery"
          placeholder="협력사명으로 검색..."
          class="h-9"
          @keydown.enter.prevent="handleSearch"
        />
      </div>

      <div v-if="loading" class="flex items-center justify-center py-8">
        <p class="text-sm text-muted-foreground">로딩 중...</p>
      </div>

      <div v-else class="max-h-[55vh] overflow-y-auto rounded-md border">
        <table class="w-full">
          <thead class="sticky top-0 border-b bg-muted/50">
            <tr>
              <th class="px-4 py-2 text-left text-sm font-medium">협력사명</th>
              <th class="px-4 py-2 text-left text-sm font-medium">대표</th>
              <th class="px-4 py-2 text-left text-sm font-medium">담당자</th>
              <th class="px-4 py-2 text-left text-sm font-medium">연락처</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="party in parties"
              :key="party.partyId"
              :class="[
                'cursor-pointer hover:bg-muted/50 transition-colors',
                selectedPartyId === party.partyId && 'bg-primary/10',
              ]"
              @click="handleSelect(party)"
            >
              <td class="px-4 py-2 text-sm font-medium">{{ party.name }}</td>
              <td class="px-4 py-2 text-sm">{{ party.ceo }}</td>
              <td class="px-4 py-2 text-sm">{{ party.manager }}</td>
              <td class="px-4 py-2 text-sm">{{ party.contact }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 페이지네이션 -->
      <div class="flex items-center justify-between px-2 py-2">
        <p class="text-sm text-muted-foreground">
          전체 {{ totalElements }}개 중 {{ (page - 1) * pageSize + 1 }}-{{ Math.min(page * pageSize, totalElements) }}개 표시
        </p>
        <div class="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            :disabled="page === 1"
            @click="handlePreviousPage"
          >
            이전
          </Button>
          <span class="text-sm">
            {{ page }} / {{ totalPages }}
          </span>
          <Button
            variant="outline"
            size="sm"
            :disabled="page >= totalPages"
            @click="handleNextPage"
          >
            다음
          </Button>
        </div>
      </div>

      <div class="mt-4 flex items-center justify-between rounded-md bg-muted/40 px-3 py-2 text-sm">
        <div class="flex flex-col">
          <span class="font-medium text-foreground">선택된 협력사</span>
          <span class="text-muted-foreground">
            {{ selectedPartySummary ?? '협력사를 선택하세요.' }}
          </span>
        </div>
        <Button variant="ghost" size="sm" :disabled="!selectedPartyId" @click="clearSelection">
          선택 해제
        </Button>
      </div>

      <DialogFooter class="justify-between">
        <Button variant="outline" @click="$emit('update:open', false)">취소</Button>
        <Button :disabled="!selectedPartyId" @click="confirmSelection">선택</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { appContainer } from '@/core/di/container';
import PartyRepository from '@/features/party/repository/PartyRepository';
import type { PartyListItem } from '@/features/party/models/partyListItem';
import { toast } from 'vue-sonner';

interface Props {
  open: boolean;
  selectedPartyId?: number;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'select', payload: { partyId: number; partyName: string }): void;
}>();

const repository = appContainer.resolve(PartyRepository);

const loading = ref(false);
const parties = ref<PartyListItem[]>([]);
const selectedPartyId = ref<number | undefined>();
const searchQuery = ref('');
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

const selectedPartySummary = computed(() => {
  if (!selectedPartyId.value) {
    return null;
  }
  const party = parties.value.find((p) => p.partyId === selectedPartyId.value);
  return party ? `${party.name} (${party.ceo})` : null;
});

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      selectedPartyId.value = props.selectedPartyId;
      searchQuery.value = '';
      page.value = 1;
      loadParties();
    } else {
      selectedPartyId.value = undefined;
      searchQuery.value = '';
      page.value = 1;
    }
  },
);

watch(
  () => props.selectedPartyId,
  (next) => {
    if (props.open) {
      selectedPartyId.value = next;
    }
  },
);

async function loadParties() {
  loading.value = true;
  try {
    const response = await repository.list({ 
      page: page.value, 
      size: pageSize.value,
      name: searchQuery.value || undefined,
    });
    parties.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
  } catch (error) {
    console.warn('협력사 목록 불러오기 중 오류가 발생했습니다.', error);
    toast.error('협력사 목록을 불러오지 못했습니다.', {
      description: '네트워크 상태를 확인한 뒤 다시 시도해 주세요.',
    });
  } finally {
    loading.value = false;
  }
}

function handleSelect(party: PartyListItem) {
  selectedPartyId.value = party.partyId;
}

function clearSelection() {
  selectedPartyId.value = undefined;
}

function confirmSelection() {
  if (!selectedPartyId.value) {
    toast.info('협력사를 선택해 주세요.');
    return;
  }
  const party = parties.value.find((p) => p.partyId === selectedPartyId.value);
  if (!party) {
    toast.error('선택한 협력사를 찾을 수 없습니다. 다시 시도해 주세요.');
    return;
  }
  emit('select', { partyId: party.partyId, partyName: party.name });
  emit('update:open', false);
}

function handleSearch() {
  page.value = 1;
  loadParties();
}

function handlePreviousPage() {
  if (page.value > 1) {
    page.value--;
    loadParties();
  }
}

function handleNextPage() {
  if (page.value < totalPages.value) {
    page.value++;
    loadParties();
  }
}
</script>
