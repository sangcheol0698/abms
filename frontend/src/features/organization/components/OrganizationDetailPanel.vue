<template>
  <div class="min-h-full">
    <div v-if="department" class="flex flex-col gap-4 p-4">
      <div class="flex flex-col gap-3">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <div class="flex flex-col">
            <h2 class="text-xl font-semibold text-foreground">
              {{ department.departmentName }}
            </h2>
            <p class="text-sm text-muted-foreground">
              팀 기본 현황과 구성원, 매출 정보를 확인할 수 있습니다.
            </p>
          </div>
          <Badge variant="outline" class="text-xs font-semibold">
            구성원 {{ department.employeeCount }}명
          </Badge>
        </div>

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
        <article class="flex items-center gap-3 rounded-lg border border-border/60 bg-background/80 p-3 shadow-sm">
          <div class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary">
            <UserRound class="h-4 w-4" />
          </div>
          <div class="flex flex-col text-sm">
            <span class="text-xs font-medium text-muted-foreground">부서 리더</span>
            <span class="font-semibold text-foreground">
              {{ department.departmentLeader?.employeeName ?? '미지정' }}
            </span>
          </div>
        </article>
        <article class="flex items-center gap-3 rounded-lg border border-border/60 bg-background/80 p-3 shadow-sm">
          <div class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary">
            <Users class="h-4 w-4" />
          </div>
          <div class="flex flex-col text-sm">
            <span class="text-xs font-medium text-muted-foreground">구성원 수</span>
            <span class="font-semibold text-foreground">{{ department.employeeCount }}명</span>
          </div>
        </article>
        <article class="flex items-center gap-3 rounded-lg border border-border/60 bg-background/80 p-3 shadow-sm">
          <div class="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-primary">
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
        <Tabs :model-value="selectedTab" @update:model-value="handleTabChange" class="flex flex-col">
          <TabsList class="rounded-lg bg-muted/30 p-1">
            <TabsTrigger value="info" class="text-sm">팀 기본정보</TabsTrigger>
            <TabsTrigger value="members" class="text-sm">구성원</TabsTrigger>
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
                  <dd>
                    <div v-if="department.departmentLeader" class="flex items-center gap-3">
                      <Avatar class="size-10 border border-border/60">
                        <AvatarFallback class="text-xs font-semibold">
                          {{ getInitials(department.departmentLeader?.employeeName) }}
                        </AvatarFallback>
                      </Avatar>
                      <div class="flex flex-col text-sm">
                        <span class="font-semibold text-foreground">
                          {{ department.departmentLeader?.employeeName }}
                        </span>
                        <span class="text-xs text-muted-foreground">
                          {{ department.departmentLeader?.position }}
                        </span>
                      </div>
                    </div>
                    <p v-else class="text-sm text-muted-foreground">
                      리더가 아직 지정되지 않은 조직입니다.
                    </p>
                  </dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    구성원 수
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

            <TabsContent value="members" class="flex flex-col">
              <DepartmentEmployeeList
                v-if="department.departmentId"
                :department-id="department.departmentId"
              />
              <div v-else class="flex flex-col items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-4 text-sm text-muted-foreground">
                부서 정보가 올바르지 않습니다.
              </div>
            </TabsContent>

            <TabsContent value="revenue" class="flex flex-col">
              <div class="flex flex-col gap-3 rounded-lg border border-border/60 bg-muted/20 p-4 text-sm">
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

    <div v-else class="flex h-full flex-col items-center justify-center rounded-lg bg-muted/10 p-6 text-center">
      <h3 class="text-base font-semibold text-foreground">조직 노드를 선택해 주세요</h3>
      <p class="mt-2 text-sm text-muted-foreground">
        왼쪽 조직도에서 팀을 선택하면 이 영역에 상세 정보가 표시됩니다.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { GitBranch, UserRound, Users } from 'lucide-vue-next';
import type { OrganizationDepartmentSummary } from '@/features/organization/models/organization';
import DepartmentEmployeeList from '@/features/organization/components/DepartmentEmployeeList.vue';

defineOptions({ name: 'OrganizationDetailPanel' });

const props = withDefaults(
  defineProps<{
    department: OrganizationDepartmentSummary | null;
    isLoading?: boolean;
  }>(),
  {
    department: null,
    isLoading: false,
  },
);

const route = useRoute();
const router = useRouter();

// 탭 상태 관리
const VALID_TABS = ['info', 'members', 'revenue'] as const;
type TabValue = typeof VALID_TABS[number];

const selectedTab = ref<TabValue>('info');
let isUpdatingRoute = false;
let isApplyingRoute = false;

// URL에서 초기 탭 설정
function extractTabFromQuery(value: unknown): TabValue {
  if (typeof value === 'string' && VALID_TABS.includes(value as TabValue)) {
    return value as TabValue;
  }
  if (Array.isArray(value)) {
    const first = value.find((v) => typeof v === 'string' && VALID_TABS.includes(v as TabValue));
    if (first) return first as TabValue;
  }
  return 'info';
}

// URL 쿼리에서 탭 값 읽어서 초기화
const initialTab = extractTabFromQuery(route.query.tab);
selectedTab.value = initialTab;

// 탭 변경 핸들러
function handleTabChange(newTab: string) {
  if (!VALID_TABS.includes(newTab as TabValue)) {
    return;
  }
  
  selectedTab.value = newTab as TabValue;
  
  if (isApplyingRoute) {
    return;
  }
  
  updateRouteQuery(newTab as TabValue);
}

// URL 쿼리 업데이트
function updateRouteQuery(tab: TabValue) {
  const currentTab = extractTabFromQuery(route.query.tab);
  
  if (tab === currentTab) {
    return;
  }
  
  isUpdatingRoute = true;
  
  const query = { ...route.query };
  
  // 기본 탭(info)인 경우 쿼리 파라미터 제거
  if (tab === 'info') {
    delete query.tab;
  } else {
    query.tab = tab;
  }
  
  router
    .replace({ query })
    .finally(() => {
      isUpdatingRoute = false;
    })
    .catch((error) => {
      console.warn('탭 정보를 URL에 반영하지 못했습니다.', error);
    });
}

// URL 변경 감지하여 탭 업데이트
watch(
  () => route.query.tab,
  (value) => {
    if (isUpdatingRoute) {
      return;
    }
    
    const newTab = extractTabFromQuery(value);
    
    if (newTab === selectedTab.value) {
      return;
    }
    
    isApplyingRoute = true;
    selectedTab.value = newTab;
    isApplyingRoute = false;
  },
);

function getInitials(name?: string) {
  if (!name) {
    return '–';
  }
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) {
    const [first = ''] = parts;
    return first.slice(0, 2).toUpperCase();
  }
  return parts
    .map((part) => part.charAt(0))
    .slice(0, 2)
    .join('')
    .toUpperCase();
}
</script>

