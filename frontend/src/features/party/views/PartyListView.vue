<template>
  <div class="flex h-full flex-col gap-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight">협력사 관리</h1>
        <p class="text-sm text-muted-foreground">거래처 및 협력사 정보를 관리합니다.</p>
      </div>
      <Button size="sm" class="gap-1" @click="handleCreateParty">
        <Plus class="h-4 w-4" />
        협력사 추가
      </Button>
    </div>

    <div class="flex flex-col gap-4">
      <div class="flex items-center gap-2">
        <div class="relative flex-1 max-w-sm">
          <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            v-model="searchQuery"
            placeholder="협력사명 검색"
            class="pl-9"
            @keydown.enter="handleSearch"
          />
        </div>
        <Button variant="outline" size="sm" @click="handleSearch">검색</Button>
      </div>

      <div v-if="isLoading" class="flex items-center justify-center py-12">
        <p class="text-muted-foreground">로딩 중...</p>
      </div>

      <div v-else-if="parties.length === 0" class="flex items-center justify-center py-12">
        <p class="text-muted-foreground">협력사가 없습니다.</p>
      </div>

      <div v-else class="rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="w-[200px]">협력사명</TableHead>
              <TableHead>대표자</TableHead>
              <TableHead>담당자</TableHead>
              <TableHead>연락처</TableHead>
              <TableHead>이메일</TableHead>
              <TableHead class="w-[50px]"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="party in parties" :key="party.partyId">
              <TableCell>
                <button
                  type="button"
                  class="font-medium text-primary underline underline-offset-4 hover:text-primary/80"
                  @click="handleViewParty(party)"
                >
                  {{ party.name }}
                </button>
              </TableCell>
              <TableCell>{{ party.ceo || '—' }}</TableCell>
              <TableCell>{{ party.manager || '—' }}</TableCell>
              <TableCell>{{ party.contact || '—' }}</TableCell>
              <TableCell>{{ party.email || '—' }}</TableCell>
              <TableCell>
                <DropdownMenu>
                  <DropdownMenuTrigger as-child>
                    <Button variant="ghost" size="icon" class="h-8 w-8">
                      <MoreHorizontal class="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem @click="handleViewParty(party)">
                      상세 보기
                    </DropdownMenuItem>
                    <DropdownMenuItem @click="handleEditParty(party)">
                      수정
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem class="text-destructive" @click="handleDeleteParty(party)">
                      삭제
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>

      <div v-if="totalPages > 1" class="flex items-center justify-center gap-2">
        <Button
          variant="outline"
          size="sm"
          :disabled="page === 1"
          @click="page--"
        >
          이전
        </Button>
        <span class="text-sm text-muted-foreground">{{ page }} / {{ totalPages }}</span>
        <Button
          variant="outline"
          size="sm"
          :disabled="page === totalPages"
          @click="page++"
        >
          다음
        </Button>
      </div>
    </div>

    <PartyFormDialog
      :open="isFormDialogOpen"
      :mode="formDialogMode"
      :party="editingParty"
      @update:open="isFormDialogOpen = $event"
      @created="handlePartyCreated"
      @updated="handlePartyUpdated"
    />

    <AlertDialog :open="isDeleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>협력사를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {{ deletingParty?.name }} 협력사 정보가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="isDeleteDialogOpen = false">취소</AlertDialogCancel>
          <AlertDialogAction
            class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            :disabled="isDeleting"
            @click="confirmDelete"
          >
            {{ isDeleting ? '삭제 중...' : '삭제' }}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { MoreHorizontal, Plus, Search } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
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
import { appContainer } from '@/core/di/container';
import PartyRepository from '@/features/party/repository/PartyRepository';
import type { PartyListItem } from '@/features/party/models/partyListItem';
import type { PartyDetail } from '@/features/party/models/partyDetail';
import PartyFormDialog from '@/features/party/components/PartyFormDialog.vue';
import { toast } from 'vue-sonner';

defineOptions({ name: 'PartyListView' });

const router = useRouter();
const repository = appContainer.resolve(PartyRepository);

const parties = ref<PartyListItem[]>([]);
const isLoading = ref(false);
const page = ref(1);
const pageSize = ref(20);
const totalPages = ref(1);
const searchQuery = ref('');

const isFormDialogOpen = ref(false);
const formDialogMode = ref<'create' | 'edit'>('create');
const editingParty = ref<PartyDetail | null>(null);

const isDeleteDialogOpen = ref(false);
const deletingParty = ref<PartyListItem | null>(null);
const isDeleting = ref(false);

watch(page, () => loadParties());

async function loadParties() {
  isLoading.value = true;
  try {
    const response = await repository.list({
      page: page.value,
      size: pageSize.value,
      name: searchQuery.value || undefined,
    });
    parties.value = response.content;
    totalPages.value = response.totalPages;
  } catch (error) {
    console.error('협력사 목록을 불러오지 못했습니다.', error);
    parties.value = [];
  } finally {
    isLoading.value = false;
  }
}

function handleSearch() {
  page.value = 1;
  loadParties();
}

function handleViewParty(party: PartyListItem) {
  router.push({ name: 'party-detail', params: { partyId: party.partyId } });
}

function handleCreateParty() {
  formDialogMode.value = 'create';
  editingParty.value = null;
  isFormDialogOpen.value = true;
}

async function handleEditParty(party: PartyListItem) {
  try {
    const detail = await repository.find(party.partyId);
    formDialogMode.value = 'edit';
    editingParty.value = detail;
    isFormDialogOpen.value = true;
  } catch {
    toast.error('협력사 정보를 불러오지 못했습니다.');
  }
}

function handlePartyCreated() {
  isFormDialogOpen.value = false;
  toast.success('협력사가 등록되었습니다.');
  loadParties();
}

function handlePartyUpdated() {
  isFormDialogOpen.value = false;
  toast.success('협력사 정보가 수정되었습니다.');
  loadParties();
}

function handleDeleteParty(party: PartyListItem) {
  deletingParty.value = party;
  isDeleteDialogOpen.value = true;
}

async function confirmDelete() {
  if (!deletingParty.value) return;

  isDeleting.value = true;
  try {
    await repository.delete(deletingParty.value.partyId);
    toast.success('협력사가 삭제되었습니다.');
    isDeleteDialogOpen.value = false;
    deletingParty.value = null;
    loadParties();
  } catch {
    toast.error('협력사 삭제에 실패했습니다.');
  } finally {
    isDeleting.value = false;
  }
}

onMounted(() => {
  loadParties();
});
</script>
