<template>
  <section class="flex h-full min-h-0 flex-1 flex-col gap-6 overflow-hidden">
    <FeatureSplitLayout
      :sidebar-default-size="20"
      :sidebar-min-size="14"
      :sidebar-max-size="32"
      :content-min-size="52"
    >
      <template #sidebar="{ pane }">
        <div class="flex h-full min-h-0 flex-col border-r border-border/60 bg-background">
          <div class="shrink-0 border-b border-border/60 px-4 py-4">
            <Button class="w-full gap-2 text-sm" @click="openCreateDialog">
              커스텀 그룹 생성
            </Button>
          </div>

          <div class="shrink-0 space-y-3 px-4 pt-4">
            <div class="space-y-1">
              <p class="text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
                권한 그룹
              </p>
            </div>

            <div class="relative">
              <Search class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                v-model="filters.name"
                placeholder="그룹 이름 검색"
                class="pl-9 text-xs"
              />
            </div>
          </div>

          <div class="min-h-0 flex-1">
            <ScrollArea class="h-full">
              <div
                v-if="isGroupsLoading"
                class="flex min-h-full min-h-[220px] items-center justify-center px-6 text-sm text-muted-foreground"
              >
                권한 그룹 목록을 불러오는 중입니다...
              </div>

              <div v-else-if="groups.length === 0" class="p-6 text-sm text-muted-foreground">
                조건에 맞는 권한 그룹이 없습니다.
              </div>

              <div v-else class="space-y-5 px-4 pb-5 pt-5 text-sm">
                <div v-for="section in groupSections" :key="section.key">
                  <div class="flex items-center justify-between px-1 pb-1 text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
                    <span>{{ section.title }}</span>
                  </div>

                  <ul class="space-y-1">
                    <li v-for="group in section.groups" :key="group.id">
                      <button
                        type="button"
                        class="flex w-full min-w-0 flex-col rounded-xl border border-transparent px-3 py-3 text-left text-xs text-foreground transition-colors"
                        :class="
                          selectedGroupId === group.id
                            ? 'border-border bg-muted/40'
                            : 'hover:bg-muted/60'
                        "
                        @click="handleGroupSelect(group.id, pane)"
                      >
                        <div class="flex items-start justify-between gap-3">
                          <div class="min-w-0 flex-1">
                            <p class="truncate font-medium">{{ group.name }}</p>
                          </div>
                          <Badge
                            :variant="group.groupType === 'SYSTEM' ? 'secondary' : 'outline'"
                            class="shrink-0"
                          >
                            {{ group.groupType === 'SYSTEM' ? '시스템' : '커스텀' }}
                          </Badge>
                        </div>

                      </button>
                    </li>
                  </ul>
                </div>
              </div>
            </ScrollArea>
          </div>
        </div>
      </template>

      <template #default="{ pane }">
        <div class="flex h-full min-h-0 flex-col overflow-hidden bg-background">
          <header class="flex flex-col gap-4 border-b border-border/60 px-4 pt-4 pb-2">
            <div class="flex items-center gap-3">
              <Button
                variant="ghost"
                size="icon"
                class="-ml-1 h-8 w-8 text-muted-foreground transition hover:text-foreground"
                aria-label="권한 그룹 사이드바 토글"
                @click="pane.toggleSidebar()"
              >
                <Menu class="h-4 w-4 transition" :class="pane.isSidebarCollapsed.value ? 'rotate-180' : ''" />
              </Button>

              <div class="min-w-0 flex-1">
                <p class="text-xs font-medium tracking-[0.18em] text-muted-foreground uppercase">Permission Groups</p>
                <h2 class="truncate text-2xl font-semibold tracking-tight text-foreground">
                  {{ selectedGroupDetail?.name ?? '권한 그룹 선택' }}
                </h2>
              </div>
            </div>

            <div v-if="selectedGroupDetail" class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div class="space-y-3">
                <div class="flex flex-wrap items-center gap-2">
                  <Badge :variant="selectedGroupDetail.groupType === 'SYSTEM' ? 'secondary' : 'outline'">
                    {{ selectedGroupDetail.groupType === 'SYSTEM' ? '시스템 그룹' : '커스텀 그룹' }}
                  </Badge>
                  <Badge variant="secondary">권한 {{ selectedGroupDetail.grants.length }}</Badge>
                  <Badge variant="secondary">사용자 {{ selectedGroupDetail.accounts.length }}</Badge>
                </div>

                <p class="max-w-3xl text-sm leading-6 text-muted-foreground">
                  {{ selectedGroupDetail.description }}
                </p>

                <p
                  v-if="selectedGroupDetail.groupType === 'SYSTEM'"
                  class="text-xs font-medium text-amber-700"
                >
                  시스템 그룹은 권한 구성과 기본 정보를 수정할 수 없고, 사용자 할당만 관리할 수 있습니다.
                </p>
              </div>

              <div class="flex flex-wrap gap-2">
                <Button
                  variant="outline"
                  :disabled="selectedGroupDetail.groupType === 'SYSTEM'"
                  @click="openEditDialog"
                >
                  수정
                </Button>
                <Button
                  variant="destructive"
                  :disabled="selectedGroupDetail.groupType === 'SYSTEM'"
                  @click="isDeleteDialogOpen = true"
                >
                  삭제
                </Button>
              </div>
            </div>
          </header>

          <div class="flex-1 min-h-0 overflow-y-auto">
            <div v-if="!selectedGroupId" class="flex h-full items-center justify-center px-4 py-8 text-sm text-muted-foreground">
              권한 그룹을 선택하면 상세 정보를 볼 수 있습니다.
            </div>

            <div
              v-else-if="isDetailLoading"
              class="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 px-4 py-4"
            >
              <span class="text-sm text-muted-foreground">권한 그룹 상세를 불러오는 중입니다...</span>
            </div>

            <div v-else-if="selectedGroupDetail" class="flex min-h-full flex-col px-4 pb-4 pt-4">
              <Tabs
                :model-value="selectedTab"
                @update:model-value="handleTabChange"
                class="flex flex-col gap-4"
              >
                <TabsList class="w-fit">
                  <TabsTrigger value="grants">권한</TabsTrigger>
                  <TabsTrigger value="accounts">사용자</TabsTrigger>
                </TabsList>

                <TabsContent value="grants" class="m-0 space-y-0">
                  <div class="flex flex-col">
                    <div class="border-b border-border px-4 py-3">
                      <h3 class="text-sm font-semibold text-foreground">권한 구성</h3>
                      <p class="text-xs text-muted-foreground">
                        선택한 그룹에 포함된 권한 코드와 허용 scope입니다.
                      </p>
                    </div>

                    <div class="px-4 pt-4">
                      <DataTableToolbar
                        :table="grantTable"
                        searchPlaceholder="권한 코드 또는 이름 검색"
                        searchColumnId="permissionName"
                        :getColumnLabel="getGrantColumnLabel"
                        :applySearchOnEnter="true"
                        :showSelectedInfo="false"
                      />
                    </div>

                    <div class="px-4 pb-4 pt-4">
                      <DataTable
                        :columns="grantColumns"
                        :data="grantRows"
                        :tableInstance="grantTable"
                        emptyMessage="구성된 권한이 없습니다."
                        emptyDescription="그룹을 수정해 권한 코드와 scope를 추가해 보세요."
                        :pageSize="Math.max(grantRows.length, 1)"
                      />
                    </div>
                  </div>
                </TabsContent>

                <TabsContent value="accounts" class="m-0 space-y-0">
                  <div class="flex flex-col">
                    <div class="flex items-center justify-between gap-3 border-b border-border px-4 py-3">
                      <div>
                        <h3 class="text-sm font-semibold text-foreground">소속 계정</h3>
                        <p class="text-xs text-muted-foreground">
                          현재 선택한 권한 그룹에 속한 계정을 관리합니다.
                        </p>
                      </div>
                      <Button variant="outline" @click="openAssignDialog">계정 추가</Button>
                    </div>

                    <div class="px-4 pt-4">
                      <DataTableToolbar
                        :table="accountTable"
                        searchPlaceholder="이름 또는 이메일 검색"
                        searchColumnId="employeeName"
                        :getColumnLabel="getAccountColumnLabel"
                        :applySearchOnEnter="true"
                        :showSelectedInfo="false"
                      />
                    </div>

                    <div class="px-4 pb-4 pt-4">
                      <DataTable
                        :columns="accountColumns"
                        :data="accountRows"
                        :tableInstance="accountTable"
                        emptyMessage="아직 할당된 계정이 없습니다."
                        emptyDescription="우측 상단의 계정 추가 버튼으로 사용자를 할당해 보세요."
                        :pageSize="Math.max(accountRows.length, 1)"
                      />
                    </div>
                  </div>
                </TabsContent>
              </Tabs>
            </div>
          </div>
        </div>
      </template>
    </FeatureSplitLayout>
  </section>

  <PermissionGroupEditorDialog
    :open="isEditorDialogOpen"
    :mode="editorMode"
    :catalog="catalog"
    :initial-group="editorMode === 'edit' ? selectedGroupDetail : null"
    :loading="createMutation.isPending.value || updateMutation.isPending.value"
    @update:open="isEditorDialogOpen = $event"
    @submit="handleEditorSubmit"
  />

  <PermissionGroupAccountAssignDialog
    :open="isAssignDialogOpen"
    :keyword="assignSearchKeyword"
    :loading="isAssignableAccountsLoading"
    :accounts="assignableAccounts"
    @update:open="handleAssignDialogOpenChange"
    @update:keyword="assignSearchKeyword = $event"
    @assign="handleAssign"
  />

  <AlertDialog :open="isDeleteDialogOpen" @update:open="isDeleteDialogOpen = $event">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>권한 그룹을 삭제할까요?</AlertDialogTitle>
        <AlertDialogDescription>
          삭제하면 현재 그룹에 속한 활성 사용자 할당도 함께 해제됩니다.
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel :disabled="deleteMutation.isPending.value">취소</AlertDialogCancel>
        <AlertDialogAction
          :disabled="deleteMutation.isPending.value"
          @pointerdown.prevent
          @click="handleDelete"
        >
          삭제
        </AlertDialogAction>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>

<script setup lang="ts">
import { computed, h, reactive, ref, watch } from 'vue';
import {
  type ColumnDef,
  type ColumnFiltersState,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  getSortedRowModel,
  type RowSelectionState,
  type SortingState,
  useVueTable,
} from '@tanstack/vue-table';
import { Menu, Search } from 'lucide-vue-next';
import { toast } from 'vue-sonner';
import FeatureSplitLayout from '@/core/layouts/FeatureSplitLayout.vue';
import { useQuerySync } from '@/core/composables/useQuerySync';
import { DataTable, DataTableColumnHeader, DataTableToolbar } from '@/components/business';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
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
import PermissionGroupAccountAssignDialog from '@/features/admin/components/PermissionGroupAccountAssignDialog.vue';
import PermissionGroupAccountRowActions from '@/features/admin/components/PermissionGroupAccountRowActions.vue';
import PermissionGroupEditorDialog from '@/features/admin/components/PermissionGroupEditorDialog.vue';
import type {
  PermissionGroupCatalog,
  PermissionGroupType,
  PermissionGroupUpsertPayload,
} from '@/features/admin/models/permissionGroup';
import {
  useAssignableAccountsQuery,
  useAssignPermissionGroupAccountMutation,
  useCreatePermissionGroupMutation,
  useDeletePermissionGroupMutation,
  usePermissionGroupCatalogQuery,
  usePermissionGroupDetailQuery,
  usePermissionGroupListQuery,
  useUnassignPermissionGroupAccountMutation,
  useUpdatePermissionGroupMutation,
} from '@/features/admin/queries/usePermissionGroupQueries';
import { valueUpdater } from '@/components/ui/table/utils';

const filters = reactive<{
  name: string;
  groupType: PermissionGroupType | 'ALL';
}>({
  name: '',
  groupType: 'ALL',
});

const selectedGroupId = ref<number | null>(null);
const VALID_TABS = ['grants', 'accounts'] as const;
type TabValue = (typeof VALID_TABS)[number];
const selectedTab = ref<TabValue>('grants');
const isEditorDialogOpen = ref(false);
const editorMode = ref<'create' | 'edit'>('create');
const isAssignDialogOpen = ref(false);
const assignSearchKeyword = ref('');
const isDeleteDialogOpen = ref(false);
const grantSorting = ref<SortingState>([]);
const accountSorting = ref<SortingState>([]);
const grantColumnFilters = ref<ColumnFiltersState>([]);
const accountColumnFilters = ref<ColumnFiltersState>([]);
const grantRowSelection = ref<RowSelectionState>({});
const accountRowSelection = ref<RowSelectionState>({});

const groupsQuery = usePermissionGroupListQuery(filters);
const detailQuery = usePermissionGroupDetailQuery(selectedGroupId);
const catalogQuery = usePermissionGroupCatalogQuery();
const assignableAccountsQuery = useAssignableAccountsQuery(selectedGroupId, assignSearchKeyword);

const createMutation = useCreatePermissionGroupMutation();
const updateMutation = useUpdatePermissionGroupMutation();
const deleteMutation = useDeletePermissionGroupMutation();
const assignMutation = useAssignPermissionGroupAccountMutation();
const unassignMutation = useUnassignPermissionGroupAccountMutation();

const groups = computed(() => groupsQuery.data.value ?? []);
const selectedGroupDetail = computed(() => detailQuery.data.value ?? null);
const catalog = computed<PermissionGroupCatalog>(() => {
  return (
    catalogQuery.data.value ?? {
      permissions: [],
      scopes: [],
    }
  );
});
const scopeLabelMap = computed<Record<string, string>>(() =>
  Object.fromEntries(catalog.value.scopes.map((scope) => [scope.code, scope.description])),
);
const assignableAccounts = computed(() => assignableAccountsQuery.data.value ?? []);
const isGroupsLoading = computed(() => groupsQuery.isLoading.value || groupsQuery.isFetching.value);
const isDetailLoading = computed(() => detailQuery.isLoading.value || detailQuery.isFetching.value);
const isAssignableAccountsLoading = computed(
  () => assignableAccountsQuery.isLoading.value || assignableAccountsQuery.isFetching.value,
);
const groupSections = computed(() => {
  if (filters.groupType === 'SYSTEM') {
    return [
      {
        key: 'system',
        title: '시스템 그룹',
        groups: groups.value,
      },
    ];
  }

  if (filters.groupType === 'CUSTOM') {
    return [
      {
        key: 'custom',
        title: '커스텀 그룹',
        groups: groups.value,
      },
    ];
  }

  const systemGroups = groups.value.filter((group) => group.groupType === 'SYSTEM');
  const customGroups = groups.value.filter((group) => group.groupType === 'CUSTOM');

  return [
    {
      key: 'system',
      title: '시스템 그룹',
      groups: systemGroups,
    },
    {
      key: 'custom',
      title: '커스텀 그룹',
      groups: customGroups,
    },
  ].filter((section) => section.groups.length > 0);
});
const grantRows = computed(() => selectedGroupDetail.value?.grants ?? []);
const accountRows = computed(() => selectedGroupDetail.value?.accounts ?? []);
const grantColumns: ColumnDef<(typeof grantRows.value)[number]>[] = [
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
    accessorKey: 'permissionName',
    size: 360,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '권한명 / 설명', align: 'left' }),
    cell: ({ row }) =>
      h('div', { class: 'space-y-1' }, [
        h('p', { class: 'text-sm font-medium text-foreground' }, row.original.permissionName),
        h('p', { class: 'text-xs leading-5 text-muted-foreground whitespace-normal' }, row.original.permissionDescription),
      ]),
  },
  {
    accessorKey: 'permissionCode',
    size: 180,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '권한 코드', align: 'left' }),
    cell: ({ row }) =>
      h(Badge, { variant: 'outline', class: 'font-mono text-[11px]' }, () => row.original.permissionCode),
  },
  {
    id: 'scopes',
    accessorFn: (row) => row.scopes.join(','),
    size: 260,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: 'Scope', align: 'left' }),
    cell: ({ row }) =>
      h(
        'div',
        { class: 'flex flex-wrap gap-2' },
        row.original.scopes.map((scope) =>
          h(Badge, { key: scope, variant: 'secondary' }, () => scopeLabelMap.value[scope] ?? scope)),
      ),
  },
];
const accountColumns: ColumnDef<(typeof accountRows.value)[number]>[] = [
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
    accessorKey: 'employeeName',
    size: 160,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '이름', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'font-medium' }, row.original.employeeName),
  },
  {
    accessorKey: 'email',
    size: 220,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '이메일', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'text-muted-foreground' }, row.original.email),
  },
  {
    accessorKey: 'departmentName',
    size: 180,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '부서', align: 'left' }),
    cell: ({ row }) => h('span', { class: 'line-clamp-1' }, row.original.departmentName || '-'),
  },
  {
    id: 'position',
    accessorFn: (row) => row.position.description,
    size: 140,
    header: ({ column }) => h(DataTableColumnHeader, { column, title: '직책', align: 'left' }),
    cell: ({ row }) => h(Badge, { variant: 'secondary' }, () => row.original.position.description),
  },
  {
    id: 'actions',
    enableSorting: false,
    size: 120,
    header: () => null,
    cell: ({ row }) =>
      h(PermissionGroupAccountRowActions, {
        row: row.original,
        onCopyEmail: () => handleCopyAccountEmail(row.original.email),
        onRemove: () => handleUnassign(row.original.accountId),
      }),
  },
];
const grantTable = useVueTable({
  get data() {
    return grantRows.value;
  },
  get columns() {
    return grantColumns;
  },
  enableRowSelection: true,
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
  getSortedRowModel: getSortedRowModel(),
  state: {
    get sorting() {
      return grantSorting.value;
    },
    get columnFilters() {
      return grantColumnFilters.value;
    },
    get rowSelection() {
      return grantRowSelection.value;
    },
  },
  onSortingChange: (updater) => {
    grantSorting.value =
      typeof updater === 'function' ? updater(grantSorting.value) : updater;
  },
  onColumnFiltersChange: (updater) => {
    grantColumnFilters.value =
      typeof updater === 'function' ? updater(grantColumnFilters.value) : updater;
  },
  onRowSelectionChange: (updater) => valueUpdater(updater, grantRowSelection),
});
const accountTable = useVueTable({
  get data() {
    return accountRows.value;
  },
  get columns() {
    return accountColumns;
  },
  enableRowSelection: true,
  getCoreRowModel: getCoreRowModel(),
  getFilteredRowModel: getFilteredRowModel(),
  getFacetedRowModel: getFacetedRowModel(),
  getFacetedUniqueValues: getFacetedUniqueValues(),
  getSortedRowModel: getSortedRowModel(),
  state: {
    get sorting() {
      return accountSorting.value;
    },
    get columnFilters() {
      return accountColumnFilters.value;
    },
    get rowSelection() {
      return accountRowSelection.value;
    },
  },
  onSortingChange: (updater) => {
    accountSorting.value =
      typeof updater === 'function' ? updater(accountSorting.value) : updater;
  },
  onColumnFiltersChange: (updater) => {
    accountColumnFilters.value =
      typeof updater === 'function' ? updater(accountColumnFilters.value) : updater;
  },
  onRowSelectionChange: (updater) => valueUpdater(updater, accountRowSelection),
});

function getGrantColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    permissionCode: '권한 코드',
    permissionName: '권한명 / 설명',
    scopes: 'Scope',
  };
  return labels[columnId] ?? columnId;
}

function getAccountColumnLabel(columnId: string): string {
  const labels: Record<string, string> = {
    employeeName: '이름',
    email: '이메일',
    departmentName: '부서',
    position: '직책',
    actions: '액션',
  };
  return labels[columnId] ?? columnId;
}

watch(
  groups,
  (nextGroups) => {
    if (nextGroups.length === 0) {
      selectedGroupId.value = null;
      return;
    }

    const exists = nextGroups.some((group) => group.id === selectedGroupId.value);
    if (!exists) {
      selectedGroupId.value = nextGroups[0]?.id ?? null;
    }
  },
  { immediate: true },
);

watch(
  () => groupsQuery.error.value,
  (error) => {
    if (error) {
      toast.error('권한 그룹 목록을 불러오지 못했습니다.');
    }
  },
);

watch(
  () => detailQuery.error.value,
  (error) => {
    if (error) {
      toast.error('권한 그룹 상세를 불러오지 못했습니다.');
    }
  },
);

useQuerySync({
  state: selectedTab,
  queryKey: 'tab',
  serialize: (value) => (value === 'grants' ? undefined : value),
  deserialize: (value) => {
    if (Array.isArray(value)) {
      const first = value.find((item) => typeof item === 'string' && VALID_TABS.includes(item as TabValue));
      return (first as TabValue) ?? 'grants';
    }

    if (typeof value === 'string' && VALID_TABS.includes(value as TabValue)) {
      return value as TabValue;
    }

    return 'grants';
  },
  defaultValue: 'grants',
});

function openCreateDialog() {
  editorMode.value = 'create';
  isEditorDialogOpen.value = true;
}

function handleTabChange(nextTab: string | number) {
  if (typeof nextTab !== 'string') {
    return;
  }

  if (!VALID_TABS.includes(nextTab as TabValue)) {
    return;
  }

  selectedTab.value = nextTab as TabValue;
}

function openEditDialog() {
  if (!selectedGroupDetail.value || selectedGroupDetail.value.groupType === 'SYSTEM') {
    return;
  }

  editorMode.value = 'edit';
  isEditorDialogOpen.value = true;
}

function openAssignDialog() {
  assignSearchKeyword.value = '';
  isAssignDialogOpen.value = true;
}

function handleAssignDialogOpenChange(value: boolean) {
  isAssignDialogOpen.value = value;
  if (!value) {
    assignSearchKeyword.value = '';
  }
}

function handleGroupSelect(
  groupId: number,
  pane?: { isLargeScreen: { value: boolean }; closeSidebar: () => void },
) {
  selectedGroupId.value = groupId;
  if (pane && !pane.isLargeScreen.value) {
    pane.closeSidebar();
  }
}

async function handleEditorSubmit(payload: PermissionGroupUpsertPayload) {
  try {
    if (editorMode.value === 'create') {
      const createdId = await createMutation.mutateAsync(payload);
      selectedGroupId.value = createdId;
      toast.success('커스텀 권한 그룹을 생성했습니다.');
    } else if (selectedGroupId.value) {
      await updateMutation.mutateAsync({ id: selectedGroupId.value, payload });
      toast.success('권한 그룹을 수정했습니다.');
    }
    isEditorDialogOpen.value = false;
  } catch (error: any) {
    toast.error('권한 그룹 저장에 실패했습니다.', {
      description: error?.message ?? '입력값을 확인한 뒤 다시 시도해 주세요.',
    });
  }
}

async function handleDelete() {
  if (!selectedGroupId.value) {
    return;
  }

  try {
    const deletingId = selectedGroupId.value;
    await deleteMutation.mutateAsync(deletingId);
    toast.success('권한 그룹을 삭제했습니다.');
    isDeleteDialogOpen.value = false;
    selectedGroupId.value = groups.value.find((group) => group.id !== deletingId)?.id ?? null;
  } catch (error: any) {
    toast.error('권한 그룹 삭제에 실패했습니다.', {
      description: error?.message ?? '다시 시도해 주세요.',
    });
  }
}

async function handleAssign(accountId: number) {
  if (!selectedGroupId.value) {
    return;
  }

  try {
    await assignMutation.mutateAsync({
      permissionGroupId: selectedGroupId.value,
      accountId,
    });
    toast.success('계정을 권한 그룹에 추가했습니다.');
    isAssignDialogOpen.value = false;
    assignSearchKeyword.value = '';
  } catch (error: any) {
    toast.error('계정 추가에 실패했습니다.', {
      description: error?.message ?? '다시 시도해 주세요.',
    });
  }
}

async function handleUnassign(accountId: number) {
  if (!selectedGroupId.value) {
    return;
  }

  try {
    await unassignMutation.mutateAsync({
      permissionGroupId: selectedGroupId.value,
      accountId,
    });
    toast.success('계정을 권한 그룹에서 해제했습니다.');
  } catch (error: any) {
    toast.error('계정 해제에 실패했습니다.', {
      description: error?.message ?? '다시 시도해 주세요.',
    });
  }
}

async function handleCopyAccountEmail(email: string) {
  try {
    await navigator.clipboard.writeText(email);
    toast.success('이메일을 복사했습니다.');
  } catch {
    toast.error('이메일 복사에 실패했습니다.');
  }
}
</script>
