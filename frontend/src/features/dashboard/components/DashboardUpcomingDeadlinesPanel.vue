<template>
  <Card class="shadow-none">
    <CardHeader>
      <CardTitle>다가오는 마감일</CardTitle>
      <CardDescription>마감 기한이 임박한 프로젝트입니다.</CardDescription>
    </CardHeader>
    <CardContent>
      <div class="space-y-4">
        <div
          v-for="project in projects"
          :key="project.id"
          class="flex items-center justify-between border-b border-border/40 pb-3 last:border-0 last:pb-0"
        >
          <div class="space-y-1">
            <p class="text-sm font-medium leading-none">{{ project.name }}</p>
            <p class="text-xs text-muted-foreground">{{ project.client }}</p>
          </div>
          <div class="text-right">
            <div class="text-sm font-medium" :class="getDeadlineColor(project.daysLeft)">
              D-{{ project.daysLeft }}
            </div>
            <p class="text-xs text-muted-foreground">{{ project.date }}</p>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
</template>

<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const projects = [
  { id: 1, name: '차세대 뱅킹 시스템 구축', client: '우리은행', daysLeft: 3, date: '2024-03-15' },
  { id: 2, name: '모바일 앱 리뉴얼', client: '삼성카드', daysLeft: 7, date: '2024-03-19' },
  { id: 3, name: 'ERP 고도화 2차', client: 'LG전자', daysLeft: 14, date: '2024-03-26' },
];

function getDeadlineColor(days: number) {
  if (days <= 3) return 'text-red-500 font-bold';
  if (days <= 7) return 'text-orange-500 font-semibold';
  return 'text-foreground';
}
</script>
