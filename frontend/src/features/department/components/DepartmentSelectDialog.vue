<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="w-full max-w-xl overflow-hidden">
      <DialogHeader>
        <DialogTitle>부서 선택</DialogTitle>
        <DialogDescription
          >부서 목록에서 원하는 부서를 검색하거나 직접 선택하세요.</DialogDescription
        >
      </DialogHeader>

      <div class="max-h-[55vh] overflow-y-auto rounded-md border px-2 py-2">
        <DepartmentTree
          :key="treeRenderKey"
          :nodes="nodes"
          :selected-node-id="selectedNodeId"
          default-expand-all
          @update:selectedNodeId="handleTreeSelect"
        />
      </div>

      <div class="mt-4 flex items-center justify-between rounded-md bg-muted/40 px-3 py-2 text-sm">
        <div class="flex flex-col">
          <span class="font-medium text-foreground">선택된 부서</span>
          <span class="text-muted-foreground">
            {{ selectedNodeSummary ?? '부서를 선택하세요.' }}
          </span>
        </div>
        <Button variant="ghost" size="sm" :disabled="!selectedNodeId" @click="clearSelection">
          선택 해제
        </Button>
      </div>

      <DialogFooter class="justify-between">
        <Button variant="outline" @click="$emit('update:open', false)">취소</Button>
        <Button :disabled="!selectedNodeId" @click="confirmSelection">선택</Button>
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
import DepartmentTree from '@/features/department/components/DepartmentTree.vue';
import type { DepartmentChartNode } from '@/features/department/models/department';
import { toast } from 'vue-sonner';
import { useDepartmentOrganizationChartQuery } from '@/features/department/queries/useDepartmentQueries';

interface Props {
  open: boolean;
  selectedDepartmentId?: number;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'select', payload: { departmentId: number; departmentName: string }): void;
}>();

const organizationChartQuery = useDepartmentOrganizationChartQuery();
const nodes = computed(() => organizationChartQuery.data.value ?? []);
const selectedNodeId = ref<number | undefined>();
const treeRenderKey = ref(0);

const selectedNodeSummary = computed(() => {
  if (!selectedNodeId.value) {
    return null;
  }
  const node = findNode(nodes.value, selectedNodeId.value);
  if (!node) {
    return null;
  }
  return node.departmentName;
});

watch(
  () => organizationChartQuery.error.value,
  (error) => {
    if (!error) {
      return;
    }
    toast.error('부서 목록을 불러오지 못했습니다.', {
      description: '네트워크 상태를 확인한 뒤 다시 시도해 주세요.',
    });
  },
);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      selectedNodeId.value = props.selectedDepartmentId;
      treeRenderKey.value += 1;
    } else {
      selectedNodeId.value = undefined;
    }
  },
  { immediate: true },
);

watch(
  () => props.selectedDepartmentId,
  (next) => {
    if (props.open) {
      selectedNodeId.value = next;
    }
  },
);

function handleTreeSelect(departmentId: number) {
  if (!departmentId) {
    return;
  }
  selectedNodeId.value = departmentId;
}

function clearSelection() {
  selectedNodeId.value = undefined;
}

function confirmSelection() {
  if (!selectedNodeId.value) {
    toast.info('부서를 선택해 주세요.');
    return;
  }
  const node = findNode(nodes.value, selectedNodeId.value);
  if (!node) {
    toast.error('선택한 부서를 찾을 수 없습니다. 다시 시도해 주세요.');
    return;
  }
  emit('select', { departmentId: node.departmentId, departmentName: node.departmentName });
  emit('update:open', false);
}

function findNode(
  target: DepartmentChartNode[],
  departmentId: number,
): DepartmentChartNode | null {
  for (const node of target) {
    if (node.departmentId === departmentId) {
      return node;
    }
    const child = findNode(node.children ?? [], departmentId);
    if (child) {
      return child;
    }
  }
  return null;
}
</script>
