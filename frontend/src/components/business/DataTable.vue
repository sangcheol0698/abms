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
          <TableRow>
            <TableCell :colspan="columns.length" class="h-24">
              <div class="flex items-center justify-center gap-2 text-sm text-muted-foreground">
                <Loader2 class="h-4 w-4 animate-spin" aria-hidden="true" />
                <span>데이터를 불러오는 중입니다...</span>
              </div>
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
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Loader2 } from 'lucide-vue-next';

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

function getColumnSizePx(col: any): number {
  try {
    if (col?.id === 'select') return 44;
    const size = typeof col?.getSize === 'function' ? col.getSize() : (col?.columnDef?.size ?? 120);
    return Math.max(48, Math.min(400, Number(size) || 120));
  } catch {
    return 120;
  }
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

</script>
