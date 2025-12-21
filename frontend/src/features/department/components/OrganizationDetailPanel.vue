<template>
  <div class="min-h-full">
    <div v-if="department" class="flex flex-col gap-4 p-4">
      <div class="flex flex-col gap-3">
        <div class="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <Badge variant="outline" class="font-medium">
            {{ department.departmentCode }}
          </Badge>
          <Badge variant="secondary" class="capitalize">
            {{ department.departmentType }}
          </Badge>
          <span v-if="department.childDepartmentCount" class="inline-flex items-center gap-1">
            하위 부서 {{ department.childDepartmentCount }}개
          </span>
        </div>
      </div>

      <div class="grid gap-2 sm:grid-cols-3">
        <article
          class="flex items-center gap-3 rounded-lg border border-border/60 bg-background/80 p-3 relative group"
        >
          <div
            class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary"
          >
            <UserRound class="h-4 w-4" />
          </div>
          <div class="flex flex-col text-sm">
            <span class="text-xs font-medium text-muted-foreground">부서 리더</span>
            <span class="font-semibold text-foreground">
              {{ department.departmentLeader?.employeeName ?? '미지정' }}
            </span>
          </div>
          <Button
            variant="ghost"
            size="icon"
            class="absolute right-2 top-1/2 -translate-y-1/2 h-7 w-7 opacity-0 transition-opacity group-hover:opacity-100"
            @click="isAssignDialogOpen = true"
          >
            <Pencil class="h-3.5 w-3.5 text-muted-foreground" />
          </Button>
        </article>
        <article
          class="flex items-center gap-3 rounded-lg border border-border/60 bg-background/80 p-3"
        >
          <div
            class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary"
          >
            <Users class="h-4 w-4" />
          </div>
          <div class="flex flex-col text-sm">
            <span class="text-xs font-medium text-muted-foreground">직원 수</span>
            <span class="font-semibold text-foreground">{{ department.employeeCount }}명</span>
          </div>
        </article>
        <article
          class="flex items-center gap-3 rounded-lg border border-border/60 bg-background/80 p-3"
        >
          <div
            class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary"
          >
            <GitBranch class="h-4 w-4" />
          </div>
          <div class="flex flex-col text-sm">
            <span class="text-xs font-medium text-muted-foreground">하위 부서</span>
            <span class="font-semibold text-foreground">
              {{ department.childDepartmentCount ?? 0 }}개
            </span>
          </div>
        </article>
      </div>

      <Separator />

      <div class="flex flex-col">
        <Tabs
          :model-value="selectedTab"
          @update:model-value="handleTabChange"
          class="flex flex-col"
        >
          <TabsList class="rounded-lg bg-muted/30 p-1">
            <TabsTrigger value="info" class="text-sm">팀 기본정보</TabsTrigger>
            <TabsTrigger value="employees" class="text-sm">직원</TabsTrigger>
            <TabsTrigger value="revenue" class="text-sm">매출</TabsTrigger>
          </TabsList>

          <div class="pt-3">
            <TabsContent value="info" class="flex flex-col gap-4">
              <div class="grid gap-3 rounded-lg border border-border/60 bg-muted/20 p-4 text-sm">
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    부서 코드
                  </dt>
                  <dd class="text-foreground">{{ department.departmentCode }}</dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    부서 유형
                  </dt>
                  <dd class="text-foreground">{{ department.departmentType }}</dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    리더
                  </dt>
                  <dd class="flex items-center justify-between">
                    <div v-if="department.departmentLeader" class="flex items-center gap-3">
                      <div class="flex items-center gap-2 text-sm">
                        <button
                          type="button"
                          class="text-left font-semibold text-primary underline underline-offset-4 hover:underline focus:outline-none focus:underline"
                          @click="navigateToEmployee(department.departmentLeader?.employeeId)"
                        >
                          {{ department.departmentLeader?.employeeName }}
                        </button>
                        <Badge variant="secondary" class="h-5 px-1.5 text-[10px] font-normal">
                          {{ department.departmentLeader?.position }}
                        </Badge>
                      </div>
                    </div>
                    <p v-else class="text-sm text-muted-foreground">
                      리더가 아직 지정되지 않은 부서입니다.
                    </p>
                    <Button
                      variant="outline"
                      size="sm"
                      class="h-7 text-xs"
                      @click="isAssignDialogOpen = true"
                    >
                      변경
                    </Button>
                  </dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    직원 수
                  </dt>
                  <dd class="text-foreground">{{ department.employeeCount }}명</dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    메모
                  </dt>
                  <dd class="text-sm text-muted-foreground">
                    팀별 KPI, 진행 중인 프로젝트 등 메모가 필요하다면 이 영역을 확장하세요.
                  </dd>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="employees" class="flex flex-col">
              <DepartmentEmployeeList
                v-if="department.departmentId"
                :department-id="department.departmentId"
              />
              <div
                v-else
                class="flex flex-col items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-4 text-sm text-muted-foreground"
              >
                부서 정보가 올바르지 않습니다.
              </div>
            </TabsContent>

            <TabsContent value="revenue" class="flex flex-col">
              <div
                class="flex flex-col gap-3 rounded-lg border border-border/60 bg-muted/20 p-4 text-sm"
              >
                <p class="text-sm font-semibold text-foreground">매출 지표</p>
                <p class="text-xs text-muted-foreground">
                  매출 데이터 연동을 준비 중입니다. 연결된 ERP 또는 회계 시스템 API가 마련되면 이
                  탭에서 월별 매출 및 목표 대비 실적을 시각화할 예정입니다.
                </p>
              </div>
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>

    <div
      v-else
      class="flex h-full flex-col items-center justify-center rounded-lg bg-muted/10 p-6 text-center"
    >
      <h3 class="text-base font-semibold text-foreground">부서를 선택해 주세요</h3>
      <p class="mt-2 text-sm text-muted-foreground">
        왼쪽 부서 목록에서 팀을 선택하면 이 영역에 상세 정보가 표시됩니다.
      </p>
    </div>

    <DepartmentLeaderAssignDialog
      v-if="department"
      v-model:open="isAssignDialogOpen"
      :department-id="department.departmentId"
      :department-name="department.departmentName"
      @assigned="$emit('refresh')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useQuerySync } from '@/core/composables/useQuerySync';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { GitBranch, UserRound, Users, Pencil } from 'lucide-vue-next';
import type { OrganizationDepartmentSummary } from '@/features/department/models/organization';
import DepartmentEmployeeList from '@/features/department/components/DepartmentEmployeeList.vue';
import DepartmentLeaderAssignDialog from '@/features/department/components/DepartmentLeaderAssignDialog.vue';

defineOptions({ name: 'OrganizationDetailPanel' });

withDefaults(
  defineProps<{
    department: OrganizationDepartmentSummary | null;
    isLoading?: boolean;
  }>(),
  {
    department: null,
    isLoading: false,
  },
);

defineEmits<{
  (event: 'refresh'): void;
}>();

const router = useRouter();

const isAssignDialogOpen = ref(false);

// 탭 상태 관리
const VALID_TABS = ['info', 'employees', 'revenue'] as const;
type TabValue = (typeof VALID_TABS)[number];

const selectedTab = ref<TabValue>('info');

// URL 쿼리와 탭 동기화
useQuerySync({
  state: selectedTab,
  queryKey: 'tab',
  serialize: (value) => {
    // 기본 탭(info)인 경우 URL에서 제거
    return value === 'info' ? undefined : value;
  },
  deserialize: (value) => {
    if (Array.isArray(value)) {
      const first = value.find((v) => typeof v === 'string' && VALID_TABS.includes(v as TabValue));
      return (first as TabValue) ?? 'info';
    }
    if (typeof value === 'string' && VALID_TABS.includes(value as TabValue)) {
      return value as TabValue;
    }
    return 'info';
  },
  defaultValue: 'info',
});

// 탭 변경 핸들러
function handleTabChange(newTab: string | number) {
  if (typeof newTab !== 'string') {
    return;
  }

  if (!VALID_TABS.includes(newTab as TabValue)) {
    return;
  }

  selectedTab.value = newTab as TabValue;
}

// 직원 상세 페이지로 이동
function navigateToEmployee(employeeId?: number) {
  if (!employeeId) {
    return;
  }
  router.push({ name: 'employee-detail', params: { employeeId: String(employeeId) } });
}
</script>
