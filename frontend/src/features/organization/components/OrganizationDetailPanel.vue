<template>
  <div class="space-y-4">
    <Card v-if="department">
      <CardHeader class="gap-3">
        <div class="flex flex-col gap-3">
          <div class="flex flex-col">
            <CardTitle class="text-xl font-semibold text-foreground">
              {{ department.departmentName }}
            </CardTitle>
            <CardDescription class="flex flex-wrap items-center gap-2 text-xs">
              <Badge variant="outline" class="font-medium">{{ department.departmentCode }}</Badge>
              <Badge variant="secondary" class="capitalize">{{ department.departmentType }}</Badge>
              <Badge variant="ghost" class="text-xs font-semibold">
                구성원 {{ department.employeeCount }}명
              </Badge>
              <Badge v-if="department.childDepartmentCount" variant="ghost" class="text-xs">
                하위 부서 {{ department.childDepartmentCount }}개
              </Badge>
            </CardDescription>
          </div>

          <Separator />

          <div class="flex flex-col gap-3">
            <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              부서 리더
            </p>
            <div
              v-if="department.departmentLeader"
              class="flex items-center gap-3 rounded-lg border border-border/60 bg-muted/20 p-3"
            >
              <Avatar class="size-11 border border-border/70">
                <AvatarFallback class="text-sm font-semibold">
                  {{ getInitials(department.departmentLeader.employeeName) }}
                </AvatarFallback>
              </Avatar>
              <div class="flex flex-col">
                <span class="text-sm font-semibold text-foreground">
                  {{ department.departmentLeader.employeeName }}
                </span>
                <span class="text-xs text-muted-foreground">
                  {{ department.departmentLeader.position }}
                </span>
              </div>
            </div>
            <div v-else class="rounded-lg border border-dashed border-border/60 bg-muted/10 p-3 text-sm text-muted-foreground">
              리더가 아직 지정되지 않은 조직입니다.
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent class="space-y-6">
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <p class="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              구성원
            </p>
            <Badge variant="outline" class="text-xs font-semibold">
              총 {{ department.employeeCount }}명
            </Badge>
          </div>
          <div
            v-if="isLoading"
            class="rounded-lg border border-dashed border-border/60 bg-muted/10 p-3 text-sm text-muted-foreground"
          >
            구성원 정보를 불러오는 중입니다...
          </div>
          <div v-else-if="department.employees.length" class="flex max-h-72 flex-col gap-2 overflow-y-auto pr-1">
            <div
              v-for="employee in department.employees"
              :key="employee.employeeId"
              class="flex items-center justify-between rounded-lg border border-border/60 bg-background/80 px-3 py-2 text-sm"
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
            </div>
          </div>
          <p v-else class="rounded-lg border border-dashed border-border/60 bg-muted/10 p-3 text-sm text-muted-foreground">
            아직 등록된 구성원이 없습니다.
          </p>
        </div>

        <div class="space-y-2 text-xs text-muted-foreground">
          <p class="font-semibold uppercase tracking-wide">조직 메모</p>
          <p>
            이 패널에서는 선택한 조직의 세부 정보를 빠르게 확인할 수 있습니다. 추가 KPI나 메모 필드가 필요하다면 이
            영역을 확장하세요.
          </p>
        </div>
      </CardContent>
    </Card>

    <Card v-else class="rounded-xl border-dashed border-border/60 bg-muted/20">
      <CardContent class="flex h-60 flex-col items-center justify-center gap-3 text-center">
        <h3 class="text-base font-semibold text-foreground">조직 노드를 선택해 주세요</h3>
        <p class="text-sm text-muted-foreground">
          왼쪽 그래프에서 조직을 클릭하면 이곳에 상세 정보가 표시됩니다.
        </p>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
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
