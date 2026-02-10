<template>
  <div class="flex flex-col gap-6">
    <!-- 요약 정보 -->
    <div class="grid gap-4 md:grid-cols-3">
      <div class="rounded-lg border border-border/60 bg-background p-4">
        <div class="text-sm font-medium text-muted-foreground">총 매출 금액</div>
        <div class="mt-2 text-2xl font-bold">{{ formatCurrency(totalAmount) }}</div>
      </div>
      <div class="rounded-lg border border-border/60 bg-background p-4">
        <div class="text-sm font-medium text-muted-foreground">예정 금액</div>
        <div class="mt-2 text-2xl font-bold text-muted-foreground">
          {{ formatCurrency(plannedAmount) }}
        </div>
      </div>
      <div class="rounded-lg border border-border/60 bg-background p-4">
        <div class="text-sm font-medium text-muted-foreground">발행된 금액</div>
        <div class="mt-2 text-2xl font-bold text-primary">
          {{ formatCurrency(invoicedAmount) }}
        </div>
      </div>
    </div>

    <!-- 목록 및 액션 -->
    <div class="flex flex-col gap-4">
      <div class="flex items-center justify-between">
        <div class="space-y-1">
          <h3 class="text-lg font-medium leading-none">매출 일정</h3>
          <p class="text-sm text-muted-foreground">프로젝트의 매출 일정을 관리합니다.</p>
        </div>
        <Button size="sm" @click="handleCreate">
          <Plus class="mr-2 h-4 w-4" />
          일정 추가
        </Button>
      </div>

      <div class="rounded-md border border-border/60 bg-background">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="w-[80px]">회차</TableHead>
              <TableHead>구분</TableHead>
              <TableHead>상태</TableHead>
              <TableHead>예정일</TableHead>
              <TableHead class="text-right">금액</TableHead>
              <TableHead>비고</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-if="isLoading">
              <TableCell colspan="6" class="h-32 text-center">
                <div class="flex flex-col items-center justify-center gap-2 text-muted-foreground">
                  <span class="text-sm">정보를 불러오는 중입니다...</span>
                </div>
              </TableCell>
            </TableRow>
            <TableRow v-else-if="items.length === 0">
              <TableCell colspan="6" class="h-32 text-center text-muted-foreground">
                등록된 매출 일정이 없습니다.
              </TableCell>
            </TableRow>
            <template v-else>
              <TableRow v-for="(item, index) in items" :key="index">
                <TableCell class="font-medium">{{ item.sequence }}회차</TableCell>
                <TableCell>
                  <Badge variant="outline">{{ item.typeLabel }}</Badge>
                </TableCell>
                <TableCell>
                  <Badge :variant="item.status === 'INVOICED' ? 'default' : 'secondary'">
                    {{ item.statusLabel }}
                  </Badge>
                </TableCell>
                <TableCell>{{ formatDate(item.revenueDate) }}</TableCell>
                <TableCell class="text-right font-medium">
                  {{ formatCurrency(item.amount) }}
                </TableCell>
                <TableCell class="max-w-[200px] truncate text-muted-foreground">
                  {{ item.memo || '-' }}
                </TableCell>
              </TableRow>
            </template>
          </TableBody>
        </Table>
      </div>
    </div>

    <ProjectRevenuePlanDialog
      :open="isDialogOpen"
      :project-id="projectId"
      @update:open="isDialogOpen = $event"
      @saved="handleSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Plus } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { formatCurrency } from '@/features/project/models/projectListItem';
import { appContainer } from '@/core/di/container';
import ProjectRevenueRepository, {
  type ProjectRevenuePlanResponse,
} from '@/features/project/repository/ProjectRevenueRepository';
import ProjectRevenuePlanDialog from './ProjectRevenuePlanDialog.vue';
import ProjectRepository from '@/features/project/repository/ProjectRepository';

interface Props {
  projectId: number;
}

const props = defineProps<Props>();

const revenueRepository = appContainer.resolve(ProjectRevenueRepository);
const projectRepository = appContainer.resolve(ProjectRepository);

const isLoading = ref(false);
const items = ref<(ProjectRevenuePlanResponse & { typeLabel: string; statusLabel: string })[]>([]);
const isDialogOpen = ref(false);
const contractAmount = ref(0);

const totalAmount = computed(() => contractAmount.value);
const plannedAmount = computed(() =>
  items.value
    .filter((item) => item.status !== 'INVOICED')
    .reduce((sum, item) => sum + item.amount, 0),
);
const invoicedAmount = computed(() =>
  items.value
    .filter((item) => item.status === 'INVOICED')
    .reduce((sum, item) => sum + item.amount, 0),
);

const typeLabelMap: Record<string, string> = {
  DOWN_PAYMENT: '착수금',
  INTERMEDIATE_PAYMENT: '중도금',
  BALANCE_PAYMENT: '잔금',
  MAINTENANCE: '유지보수',
  ETC: '기타',
};

async function fetchRevenuePlans(projectId: number) {
  if (!projectId) return;

  isLoading.value = true;
  try {
    const [revenueResponse, projectResponse] = await Promise.all([
      revenueRepository.findByProjectId(projectId),
      projectRepository.find(projectId),
    ]);

    contractAmount.value = projectResponse.contractAmount;

    items.value = revenueResponse
      .map((item) => ({
        ...item,
        typeLabel: typeLabelMap[item.type] || item.type,
        statusLabel: item.status === 'INVOICED' ? '발행' : '미발행',
      }))
      .sort((a, b) => a.sequence - b.sequence);
  } catch (error) {
    console.error('Failed to fetch revenue plans:', error);
    items.value = [];
  } finally {
    isLoading.value = false;
  }
}

function formatDate(value: string) {
  if (!value) return '-';
  return new Date(value).toLocaleDateString();
}

function handleCreate() {
  isDialogOpen.value = true;
}

function handleSaved() {
  fetchRevenuePlans(props.projectId);
}

watch(
  () => props.projectId,
  (newId) => {
    fetchRevenuePlans(newId);
  },
  { immediate: true },
);
</script>
