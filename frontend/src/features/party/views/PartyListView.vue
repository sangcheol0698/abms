<template>
  <section class="flex h-full flex-col gap-6">
    <PartySummaryCards :cards="partySummaryCards" />

    <div class="space-y-4">
      <DataTableToolbar
        :table="table"
        searchPlaceholder="협력사명을 입력하세요"
        searchColumnId="name"
        :getColumnLabel="getColumnLabel"
        :applySearchOnEnter="true"
      >
        <template #actions>
          <Button size="sm" class="h-8 gap-1 px-2 sm:px-3" @click="handleCreateParty">
            <Plus class="h-4 w-4" />
            협력사 추가
          </Button>
        </template>
      </DataTableToolbar>

      <DataTable
        :columns="columns"
        :data="parties"
        :tableInstance="table"
        :loading="isLoading"
        emptyMessage="협력사가 없습니다"
        emptyDescription="새로운 협력사를 등록해보세요"
        :pageSize="pageSize"
      />

      <DataTablePagination
        :page="page"
        :pageSize="pageSize"
        :totalPages="totalPages"
        :totalElements="totalElements"
        :selectedRowCount="selectedRowCount"
        :loading="isLoading"
        @pageChange="handlePageChange"
        @pageSizeChange="handlePageSizeChange"
      />
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
  </section>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type {
  ColumnDef,
  ColumnFiltersState,
  RowSelectionState,
  SortingState,
  VisibilityState,
} from '@tanstack/vue-table';
import {
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  useVueTable,
} from '@tanstack/vue-table';
import { MoreHorizontal, Plus } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
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
import {
  DataTable,
  DataTableColumnHeader,
  DataTablePagination,
  DataTableToolbar,
} from '@/components/business';
import { valueUpdater } from '@/components/ui/table/utils';
import { appContainer } from '@/core/di/container';
import PartyFormDialog from '@/features/party/components/PartyFormDialog.vue';
import PartySummaryCards from '@/features/party/components/PartySummaryCards.vue';
import { partySummaryCards } from '@/features/party/data';
import type { PartyDetail } from '@/features/party/models/partyDetail';
import type { PartyListItem } from '@/features/party/models/partyListItem';
import PartyRepository from '@/features/party/repository/PartyRepository';
import { usePartyQuerySync } from '@/features/party/composables/usePartyQuerySync';
import { toast } from 'vue-sonner';

defineOptions({ name: 'PartyListView' });

const route = useRoute();
const router = useRouter();
const repository = appContainer.resolve(PartyRepository);

const parties = ref<PartyListItem[]>([]);
const isLoading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const totalElements = ref(0);

const sorting = ref<SortingState>([]);
const columnFilters = ref<ColumnFiltersState>([]);
const columnVisibility = ref<VisibilityState>({});
const rowSelection = ref<RowSelectionState>({});

const selectedRowCount = computed(() => Object.keys(rowSelection.value).length);

const columns: ColumnDef<PartyListItem>[] = [
  {
    id: 'select',
    header: ({ table }) =>
      h(Checkbox, {
        modelValue:
          table.getIsAllPageRowsSelected() ||
          (table.getIsSomePageRowsSelected() && 'indeterminate'),
        'onUpdate:modelValue': (value: boolean | 'indeterminate') =>
          table.toggleAllPageRowsSelected(value === 'indeterminate' ? true : Boolean(value)),
        ariaLabel: '모두 선택',
      }),
    cell: ({ row }) =>
      h(Checkbox, {
        modelValue: row.getIsSelected(),
        'onUpdate:modelValue': (value: boolean | 'indeterminate') =>
          row.toggleSelected(value === 'indeterminate' ? true : Boolean(value)),
        ariaLabel: '행 선택',
      }),
    enableSorting: false,
    enableHiding: false,
    size: 48,
  },
  {
    accessorKey: 'name',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '협력사명', align: 'left' }),
    cell: ({ row }) =>
      h(
        'button',
        {
          type: 'button',
          class:
            'cursor-pointer text-left font-medium text-primary underline underline-offset-4 hover:underline focus:outline-none',
          onClick: () => handleViewParty(row.original),
        },
        row.original.name,
      ),
    enableSorting: true,
    size: 220,
  },
  {
    accessorKey: 'ceo',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '대표자', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'text-sm' }, row.original.ceo || '—'),
    enableSorting: false,
    size: 140,
  },
  {
    accessorKey: 'manager',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '담당자', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'text-sm' }, row.original.manager || '—'),
    enableSorting: false,
    size: 140,
  },
  {
    accessorKey: 'contact',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '연락처', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'text-sm' }, row.original.contact || '—'),
    enableSorting: false,
    size: 160,
  },
  {
    accessorKey: 'email',
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '이메일', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'text-sm' }, row.original.email || '—'),
    enableSorting: false,
    size: 200,
  },
  {
    id: 'actions',
    header: () => h('span', { class: 'sr-only' }, '작업'),
    cell: ({ row }) =>
      h(
        DropdownMenu,
        {},
        {
          default: () =>
            h(
              DropdownMenuTrigger,
              { asChild: true },
              {
                default: () =>
                  h(
                    Button,
                    { variant: 'ghost', size: 'icon', class: 'h-8 w-8' },
                    { default: () => h(MoreHorizontal, { class: 'h-4 w-4' }) },
                  ),
              },
            ),
          content: () =>
            h(
              DropdownMenuContent,
              { align: 'end' },
              {
                default: () => [
                  h(
                    DropdownMenuItem,
                    { onClick: () => handleViewParty(row.original) },
                    { default: () => '상세 보기' },
                  ),
                  h(
                    DropdownMenuItem,
                    { onClick: () => handleEditParty(row.original) },
                    { default: () => '수정' },
                  ),
                  h(DropdownMenuSeparator),
                  h(
                    DropdownMenuItem,
                    {
                      class: 'text-destructive',
                      onClick: () => handleDeleteParty(row.original),
                    },
                    { default: () => '삭제' },
                  ),
                ],
              },
            ),
        },
      ),
    enableSorting: false,
    enableHiding: false,
    size: 60,
  },
];

const table = useVueTable({
  data: computed(() => parties.value),
  columns,
  manualPagination: true,
  manualFiltering: true,
  manualSorting: true,
  state: {
    get sorting() {
      return sorting.value;
    },
    get columnFilters() {
      return columnFilters.value;
    },
    get columnVisibility() {
      return columnVisibility.value;
    },
    get rowSelection() {
      return rowSelection.value;
    },
  },
  onSortingChange: (updater) => valueUpdater(updater, sorting),
  onColumnFiltersChange: (updater) => valueUpdater(updater, columnFilters),
  onColumnVisibilityChange: (updater) => valueUpdater(updater, columnVisibility),
  onRowSelectionChange: (updater) => valueUpdater(updater, rowSelection),
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
});

const { buildSearchParams, applyRouteQuery } = usePartyQuerySync({
  page,
  pageSize,
  sorting,
  columnFilters,
  onLoadParties: loadParties,
});

async function loadParties() {
  isLoading.value = true;
  try {
    const response = await repository.list(buildSearchParams());
    parties.value = response.content;
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
  } catch (error) {
    console.error('협력사 목록을 불러오지 못했습니다.', error);
    parties.value = [];
    totalPages.value = 1;
    totalElements.value = 0;
  } finally {
    isLoading.value = false;
  }
}

function handlePageChange(nextPage: number) {
  if (nextPage < 1) {
    return;
  }
  if (nextPage === page.value) {
    return;
  }
  page.value = nextPage;
}

function handlePageSizeChange(nextSize: number) {
  if (nextSize === pageSize.value) {
    return;
  }
  pageSize.value = nextSize;
}

function getColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    name: '협력사명',
    ceo: '대표자',
    manager: '담당자',
    contact: '연락처',
    email: '이메일',
  };
  return labels[columnId] ?? columnId;
}

const isFormDialogOpen = ref(false);
const formDialogMode = ref<'create' | 'edit'>('create');
const editingParty = ref<PartyDetail | null>(null);

const isDeleteDialogOpen = ref(false);
const deletingParty = ref<PartyListItem | null>(null);
const isDeleting = ref(false);

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
  if (Object.keys(route.query).length > 0) {
    applyRouteQuery({ ...route.query });
  } else {
    loadParties();
  }
});
</script>
