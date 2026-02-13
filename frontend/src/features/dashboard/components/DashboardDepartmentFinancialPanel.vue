<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>부서별 재무 현황</CardTitle>
      <CardDescription>이번 달 기준 부서별 실적입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="space-y-6">
        <div
          v-for="(dept, index) in departments"
          :key="index"
          class="flex items-center justify-between"
        >
          <div class="space-y-1">
            <p class="text-sm font-medium leading-none">{{ dept.name }}</p>
            <p class="text-xs text-muted-foreground">
              이익률 {{ ((dept.profit / dept.revenue) * 100).toFixed(1) }}%
            </p>
          </div>
          <div class="text-right">
            <div class="font-medium text-sm">{{ formatAmount(dept.revenue) }}</div>
            <div class="text-xs text-muted-foreground">이익 {{ formatAmount(dept.profit) }}</div>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const departments = [
  { name: '솔루션사업본부', revenue: 450000000, cost: 300000000, profit: 150000000 },
  { name: '플랫폼개발팀', revenue: 320000000, cost: 240000000, profit: 80000000 },
  { name: 'DX사업팀', revenue: 210000000, cost: 150000000, profit: 60000000 },
  { name: '전략기획팀', revenue: 120000000, cost: 90000000, profit: 30000000 },
];

function formatAmount(value: number) {
  if (value >= 100000000) return `${(value / 100000000).toFixed(1)}억`;
  if (value >= 1000000) return `${(value / 1000000).toFixed(0)}백만`;
  return value.toLocaleString();
}
</script>
