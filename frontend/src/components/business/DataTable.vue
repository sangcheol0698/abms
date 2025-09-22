<template>
  <div class="rounded-md border">
    <Table>
      <TableHeader>
        <TableRow v-for="headerGroup in tableInstance.getHeaderGroups()" :key="headerGroup.id">
          <TableHead
            v-for="header in headerGroup.headers"
            :key="header.id"
            :class="header.column.id === 'select' ? 'px-2' : ''"
            :style="{ width: getHeaderSizePx(header) + 'px' }"
          >
            <FlexRender
              v-if="!header.isPlaceholder"
              :render="header.column.columnDef.header"
              :props="header.getContext()"
            />
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        <template v-if="loading">
          <TableRow v-for="i in skeletonRowCount" :key="`skeleton-row-${i}`">
            <TableCell
              v-for="col in visibleColumns"
              :key="`skeleton-cell-${i}-${col.id}`"
              :class="col.id === 'select' ? 'px-2' : ''"
              :style="{ width: getColumnSizePx(col) + 'px' }"
            >
              <template v-if="getColSkeletonVariantByColumn(col) === 'checkbox'">
                <div class="flex items-center">
                  <Skeleton class="h-4 w-4 rounded-sm" />
                </div>
              </template>
              <template v-else-if="getColSkeletonVariantByColumn(col) === 'ellipsis'">
                <div class="flex items-center">
                  <Skeleton class="h-6 rounded-md" />
                </div>
              </template>
              <template v-else-if="getColSkeletonVariantByColumn(col) === 'enum-badge'">
                <div class="flex items-center">
                  <Skeleton class="h-6 rounded-md" :style="{ width: enumBadgeWidthPx(col) + 'px' }" />
                </div>
              </template>
              <template v-else-if="getColSkeletonVariantByColumn(col) === 'text-short'">
                <div class="flex items-center">
                  <Skeleton class="h-4 rounded-md" :style="{ width: textShortWidthPx(col) + 'px' }" />
                </div>
              </template>
              <template v-else-if="getColSkeletonVariantByColumn(col) === 'title-subtitle'">
                <Skeleton class="h-4" :style="{ width: lineWidthPx(col, 0) + 'px' }" />
                <div class="mt-2">
                  <Skeleton class="h-3" :style="{ width: lineWidthPx(col, 1) + 'px' }" />
                </div>
              </template>
              <template v-else>
                <Skeleton class="h-4" :style="{ width: lineWidthPx(col, 0) + 'px' }" />
              </template>
            </TableCell>
          </TableRow>
        </template>

        <template v-else-if="tableInstance.getRowModel().rows?.length">
          <template v-for="row in tableInstance.getRowModel().rows" :key="row.id">
            <TableRow :data-state="row.getIsSelected() && 'selected'" class="hover:bg-muted/50">
              <TableCell
                v-for="(cell, idx) in row.getVisibleCells()"
                :key="cell.id"
                :class="[cell.column.id === 'select' ? 'px-2' : '', idx === 1 ? 'pl-2' : '']"
                :style="{ width: getCellSizePx(cell) + 'px' }"
              >
                <FlexRender :render="cell.column.columnDef.cell" :props="cell.getContext()" />
              </TableCell>
            </TableRow>

            <TableRow v-if="row.getIsExpanded() && $slots['expanded-row']" :key="`${row.id}-expanded`">
              <TableCell :colspan="row.getVisibleCells().length">
                <slot name="expanded-row" :row="row" />
              </TableCell>
            </TableRow>
          </template>
        </template>

        <TableRow v-else>
          <TableCell :colspan="columns.length" class="h-24 text-center">
            <div class="flex flex-col items-center justify-center space-y-2">
              <p class="text-muted-foreground">{{ emptyMessage }}</p>
              <p class="text-sm text-muted-foreground">{{ emptyDescription }}</p>
            </div>
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef, Table as TanstackTable } from '@tanstack/vue-table';
import { FlexRender } from '@tanstack/vue-table';
import { computed } from 'vue';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Skeleton } from '@/components/ui/skeleton';

interface DataTableProps {
  columns: ColumnDef<any>[];
  data: any[];
  loading?: boolean;
  emptyMessage?: string;
  emptyDescription?: string;
  tableInstance: TanstackTable<any>;
  pageSize?: number;
}

const props = defineProps<DataTableProps>();

const visibleColumns = computed(() => props.tableInstance.getVisibleLeafColumns?.() ?? []);

const skeletonRowCount = computed(() => (props.pageSize && props.pageSize > 0 ? props.pageSize : 8));

function getColSkeletonVariantByColumn(col: any):
  | 'single'
  | 'title-subtitle'
  | 'checkbox'
  | 'ellipsis'
  | 'enum-badge'
  | 'text-short' {
  if (col?.id === 'select') return 'checkbox';
  if (col?.id === 'actions') return 'ellipsis';
  const meta = (col?.columnDef as any)?.meta;
  if (meta?.skeleton === 'enum-badge') return 'enum-badge';
  if (meta?.skeleton === 'text-short') return 'text-short';
  return meta?.skeleton === 'title-subtitle' ? 'title-subtitle' : 'single';
}

function getColumnSizePx(col: any): number {
  try {
    if (col?.id === 'select') return 44;
    const size = typeof col?.getSize === 'function' ? col.getSize() : (col?.columnDef?.size ?? 120);
    return Math.max(48, Math.min(400, Number(size) || 120));
  } catch {
    return 120;
  }
}

function lineWidthPx(col: any, lineIdx = 0): number {
  const base = getColumnSizePx(col);
  const variant = getColSkeletonVariantByColumn(col);
  if (variant === 'checkbox') return 16;

  let primary = 0.72;
  let secondary = 0.45;

  if (base <= 80) {
    primary = 0.9;
    secondary = 0.6;
  } else if (base <= 160) {
    primary = 0.72;
    secondary = 0.45;
  } else if (base <= 260) {
    primary = 0.65;
    secondary = 0.4;
  } else if (base <= 400) {
    primary = 0.6;
    secondary = 0.35;
  } else {
    primary = 0.55;
    secondary = 0.3;
  }

  const ratio = lineIdx === 1 ? secondary : primary;
  const px = Math.round(base * ratio);
  return Math.max(32, Math.min(px, base - 12));
}

function getHeaderSizePx(header: any): number {
  try {
    const col = header?.column;
    if (col?.id === 'select') return 44;
    const size = typeof col?.getSize === 'function' ? col.getSize() : (col?.columnDef?.size ?? 120);
    return Math.max(48, Math.min(400, Number(size) || 120));
  } catch {
    return 120;
  }
}

function getCellSizePx(cell: any): number {
  try {
    const col = cell?.column;
    if (col?.id === 'select') return 44;
    const size = typeof col?.getSize === 'function' ? col.getSize() : (col?.columnDef?.size ?? 120);
    return Math.max(48, Math.min(400, Number(size) || 120));
  } catch {
    return 120;
  }
}

function enumBadgeWidthPx(col: any): number {
  return Math.max(48, Math.min(getColumnSizePx(col) * 0.45, 120));
}

function textShortWidthPx(col: any): number {
  return Math.max(48, Math.min(getColumnSizePx(col) * 0.6, 200));
}
</script>
