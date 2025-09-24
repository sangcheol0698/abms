<template>
  <div class="h-full">
    <div
      v-if="department"
      class="flex h-full flex-col rounded-xl border border-border/60 bg-card p-4 shadow-sm"
    >
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
          <span class="inline-flex items-center gap-1">
            ID {{ department.departmentId }}
          </span>
        </div>
      </div>

      <div class="mt-4 flex flex-1 flex-col overflow-hidden">
        <Tabs defaultValue="info" class="flex h-full flex-col">
          <TabsList class="inline-flex h-9 w-fit gap-2 bg-transparent p-0">
            <TabsTrigger value="info" class="text-sm">팀 기본정보</TabsTrigger>
            <TabsTrigger value="members" class="text-sm">구성원</TabsTrigger>
            <TabsTrigger value="revenue" class="text-sm">매출</TabsTrigger>
          </TabsList>

          <div class="flex-1 overflow-hidden pt-3">
            <TabsContent value="info" class="flex h-full flex-col gap-4">
              <div class="grid gap-3 rounded-lg border border-border/60 bg-muted/20 p-4 text-sm">
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">부서 코드</dt>
                  <dd class="text-foreground">{{ department.departmentCode }}</dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">부서 유형</dt>
                  <dd class="text-foreground">{{ department.departmentType }}</dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">리더</dt>
                  <dd>
                    <div v-if="department.departmentLeader" class="flex items-center gap-3">
                      <Avatar class="size-10 border border-border/60">
                        <AvatarFallback class="text-xs font-semibold">
                          {{ getInitials(department.departmentLeader.employeeName) }}
                        </AvatarFallback>
                      </Avatar>
                      <div class="flex flex-col text-sm">
                        <span class="font-semibold text-foreground">
                          {{ department.departmentLeader.employeeName }}
                        </span>
                        <span class="text-xs text-muted-foreground">
                          {{ department.departmentLeader.position }}
                        </span>
                      </div>
                    </div>
                    <p v-else class="text-sm text-muted-foreground">
                      리더가 아직 지정되지 않은 조직입니다.
                    </p>
                  </dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">구성원 수</dt>
                  <dd class="text-foreground">{{ department.employeeCount }}명</dd>
                </div>
                <div class="grid grid-cols-[120px_1fr] gap-2">
                  <dt class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">메모</dt>
                  <dd class="text-sm text-muted-foreground">
                    팀별 KPI, 진행 중인 프로젝트 등 메모가 필요하다면 이 영역을 확장하세요.
                  </dd>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="members" class="flex h-full flex-col">
              <div
                v-if="isLoading"
                class="flex flex-1 items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-4 text-sm text-muted-foreground"
              >
                구성원 정보를 불러오는 중입니다...
              </div>
              <div
                v-else-if="department.employees.length"
                class="flex-1 space-y-2 overflow-y-auto rounded-lg border border-border/60 bg-background/60 p-3"
              >
                <div
                  v-for="employee in department.employees"
                  :key="employee.employeeId"
                  class="flex items-center justify-between rounded-md border border-border/50 bg-card/80 px-3 py-2 text-sm"
                >
                  <div class="flex items-center gap-3">
                    <Avatar class="size-9 border border-border/50 bg-muted/40">
                      <AvatarFallback class="text-xs font-semibold">
                        {{ getInitials(employee.employeeName) }}
                      </AvatarFallback>
                    </Avatar>
                    <div class="flex flex-col">
                      <span class="font-semibold text-foreground">{{ employee.employeeName }}</span>
                      <span class="text-xs text-muted-foreground">{{ employee.position }}</span>
                    </div>
                  </div>
                  <Badge variant="secondary" class="text-[11px] font-medium">
                    {{ employee.employeeId }}
                  </Badge>
                </div>
              </div>
              <div
                v-else
                class="flex flex-1 items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10 p-4 text-sm text-muted-foreground"
              >
                아직 등록된 구성원이 없습니다.
              </div>
            </TabsContent>

            <TabsContent value="revenue" class="flex h-full flex-col">
              <div class="flex flex-1 flex-col gap-3 rounded-lg border border-border/60 bg-muted/20 p-4 text-sm">
                <p class="text-sm font-semibold text-foreground">매출 지표</p>
                <p class="text-xs text-muted-foreground">
                  매출 데이터 연동을 준비 중입니다. 연결된 ERP 또는 회계 시스템 API가 마련되면 이 탭에서 월별 매출 및
                  목표 대비 실적을 시각화할 예정입니다.
                </p>
              </div>
            </TabsContent>
          </div>
        </Tabs>
      </div>
    </div>

    <div v-else class="flex h-full flex-col items-center justify-center rounded-xl border border-dashed border-border/60 bg-muted/20 p-6 text-center">
      <h3 class="text-base font-semibold text-foreground">조직 노드를 선택해 주세요</h3>
      <p class="mt-2 text-sm text-muted-foreground">
        왼쪽 조직도에서 팀을 선택하면 이 영역에 상세 정보가 표시됩니다.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import type { OrganizationDepartmentSummary } from '@/features/organization/models/organization';

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

function getInitials(name?: string) {
  if (!name) {
    return '–';
  }
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }
  return parts
    .map((part) => part.charAt(0))
    .slice(0, 2)
    .join('')
    .toUpperCase();
}
</script>