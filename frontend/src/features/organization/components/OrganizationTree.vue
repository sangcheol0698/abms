<template>
  <ul class="space-y-4">
    <li v-for="node in nodes" :key="node.departmentId" class="space-y-3">
      <Card class="gap-0 border-border/70 shadow-none">
        <CardHeader class="gap-1 sm:flex sm:flex-row sm:items-start sm:justify-between">
          <div class="space-y-1">
            <CardTitle class="text-base font-semibold">{{ node.departmentName }}</CardTitle>
            <CardDescription class="flex flex-wrap items-center gap-2 text-xs">
              <Badge variant="outline" class="font-medium">{{ node.departmentCode }}</Badge>
              <Badge variant="secondary" class="font-medium capitalize">{{
                node.departmentType
              }}</Badge>
            </CardDescription>
          </div>
          <div
            v-if="node.departmentLeader"
            class="flex items-center gap-2 text-xs text-muted-foreground"
          >
            <Badge variant="outline" class="border-dashed">리더</Badge>
            <span class="font-medium text-foreground">{{
              node.departmentLeader.employeeName
            }}</span>
            <span>({{ node.departmentLeader.position }})</span>
          </div>
        </CardHeader>

        <Separator v-if="node.employees.length" class="mb-4 mt-1" />

        <CardContent v-if="node.employees.length" class="pt-0">
          <p class="mb-3 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
            구성원
          </p>
          <ul class="grid gap-2 sm:grid-cols-2">
            <li
              v-for="employee in node.employees"
              :key="employee.employeeId"
              class="flex items-center justify-between rounded-md border border-dashed border-border/70 px-3 py-2 text-sm"
            >
              <span class="font-medium text-foreground">{{ employee.employeeName }}</span>
              <Badge variant="outline" class="text-[11px] uppercase tracking-tight">{{
                employee.position
              }}</Badge>
            </li>
          </ul>
        </CardContent>
      </Card>

      <div v-if="node.children.length" class="border-l border-dashed border-border/60 pl-4">
        <OrganizationTree :nodes="node.children" />
      </div>
    </li>
  </ul>
</template>

<script setup lang="ts">
import type { OrganizationChartWithEmployeesNode } from '@/features/organization/models/organization';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';

defineOptions({ name: 'OrganizationTree' });

defineProps<{ nodes: OrganizationChartWithEmployeesNode[] }>();
</script>
