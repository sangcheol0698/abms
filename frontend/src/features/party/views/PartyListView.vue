<template>
  <section class="flex h-full flex-col gap-6">
    <PartySummaryCards :cards="partySummary.cards" />

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
import { computed, h, ref } from 'vue';
import { useRouter } from 'vue-router';
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
import PartyFormDialog from '@/features/party/components/PartyFormDialog.vue';
import PartySummaryCards from '@/features/party/components/PartySummaryCards.vue';
import { usePartySummary } from '@/features/party/composables/usePartySummary';
import type { PartyDetail } from '@/features/party/models/partyDetail';
import type { PartyListItem } from '@/features/party/models/partyListItem';
import { usePartyQuerySync } from '@/features/party/composables/usePartyQuerySync';
import { toast } from 'vue-sonner';
import {
  useDeletePartyMutation,
  usePartyDetailQuery,
  usePartyListQuery,
  usePartyOverviewSummaryQuery,
} from '@/features/party/queries/usePartyQueries';
import { partyKeys, queryClient } from '@/core/query';

defineOptions({ name: 'PartyListView' });

const router = useRouter();

const page = ref(1);
const pageSize = ref(10);

const sorting = ref<SortingState>([]);
const columnFilters = ref<ColumnFiltersState>([]);
const columnVisibility = ref<VisibilityState>({});
const rowSelection = ref<RowSelectionState>({});
const editingPartyId = ref<number | null>(null);

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

const { buildSearchParams } = usePartyQuerySync({
  page,
  pageSize,
  sorting,
  columnFilters,
});

const searchParams = computed(() => buildSearchParams());
const partiesQuery = usePartyListQuery(searchParams);
const partyOverviewSummaryQuery = usePartyOverviewSummaryQuery(
  computed(() => ({ name: searchParams.value.name })),
);
const partyDetailQuery = usePartyDetailQuery(editingPartyId);
const deletePartyMutation = useDeletePartyMutation();
const partySummary = usePartySummary({
  summary: computed(() => partyOverviewSummaryQuery.data.value),
});

const parties = computed(() => partiesQuery.data.value?.content ?? []);
const totalPages = computed(() => Math.max(partiesQuery.data.value?.totalPages ?? 1, 1));
const totalElements = computed(() => partiesQuery.data.value?.totalElements ?? 0);
const isLoading = computed(() => partiesQuery.isLoading.value || partiesQuery.isFetching.value);

const table = useVueTable({
  data: parties,
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
const isDeleting = computed(() => deletePartyMutation.isPending.value);

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
    editingPartyId.value = party.partyId;
    const result = await partyDetailQuery.refetch();
    const detail = result.data;
    if (!detail) {
      throw new Error('협력사 정보를 불러오지 못했습니다.');
    }
    formDialogMode.value = 'edit';
    editingParty.value = detail;
    isFormDialogOpen.value = true;
  } catch {
    toast.error('협력사 정보를 불러오지 못했습니다.');
  }
}

async function handlePartyCreated() {
  isFormDialogOpen.value = false;
  toast.success('협력사가 등록되었습니다.');
  if (page.value !== 1) {
    page.value = 1;
  }
  await queryClient.invalidateQueries({ queryKey: partyKeys.all });
}

async function handlePartyUpdated() {
  isFormDialogOpen.value = false;
  toast.success('협력사 정보가 수정되었습니다.');
  await queryClient.invalidateQueries({ queryKey: partyKeys.all });
}

function handleDeleteParty(party: PartyListItem) {
  deletingParty.value = party;
  isDeleteDialogOpen.value = true;
}

async function confirmDelete() {
  if (!deletingParty.value) return;

  try {
    await deletePartyMutation.mutateAsync(deletingParty.value.partyId);
    toast.success('협력사가 삭제되었습니다.');
    isDeleteDialogOpen.value = false;
    deletingParty.value = null;
  } catch {
    toast.error('협력사 삭제에 실패했습니다.');
  }
}

</script>
