<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>프로젝트 상태 현황</CardTitle>
      <CardDescription>전체 프로젝트의 진행 상태 분포입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="space-y-5">
        <div v-for="status in statuses" :key="status.label" class="space-y-1.5">
          <div class="flex items-center justify-between text-sm">
            <span class="font-medium">{{ status.label }}</span>
            <span class="text-muted-foreground"
              >{{ status.count }}건 ({{ status.percentage }}%)</span
            >
          </div>
          <div class="h-2 w-full rounded-full bg-muted/50 overflow-hidden">
            <div
              class="h-full transition-all"
              :style="{ width: `${status.percentage}%`, backgroundColor: status.colorCode }"
            />
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const statuses = [
  { label: '진행 중 (In Progress)', count: 12, percentage: 45, colorCode: '#f97316' },
  { label: '준비 중 (Preparing)', count: 4, percentage: 15, colorCode: '#eab308' },
  { label: '완료 (Completed)', count: 8, percentage: 30, colorCode: '#84cc16' },
  { label: '보류 (On Hold)', count: 3, percentage: 10, colorCode: '#cbd5e1' },
];
</script>
