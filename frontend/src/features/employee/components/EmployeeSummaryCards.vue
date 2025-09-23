<template>
  <div class="space-y-4">
    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <Card v-for="card in cards" :key="card.id" class="shadow-sm">
        <CardHeader class="space-y-1">
          <CardTitle class="text-sm font-semibold text-muted-foreground">{{ card.title }}</CardTitle>
          <div class="flex items-baseline gap-2">
            <span class="text-2xl font-semibold text-foreground">{{ card.value }}</span>
            <Badge
              v-if="card.trend?.direction"
              :variant="card.trend.direction === 'down' ? 'destructive' : card.trend.direction === 'up' ? 'secondary' : 'outline'"
            >
              {{ card.trend.label }}
            </Badge>
          </div>
        </CardHeader>
        <CardContent v-if="card.description" class="text-xs text-muted-foreground">
          {{ card.description }}
        </CardContent>
      </Card>
    </div>

    <Card v-if="insights.length" class="shadow-sm">
      <CardHeader>
        <CardTitle class="text-base">인사이트</CardTitle>
        <CardDescription>조직 운영에 도움이 될 만한 간단한 알림입니다.</CardDescription>
      </CardHeader>
      <CardContent class="grid gap-3 sm:grid-cols-2">
        <div
          v-for="insight in insights"
          :key="insight.headline"
          class="rounded-lg border border-border/60 bg-muted/30 p-3 text-sm"
        >
          <p class="font-semibold text-foreground">{{ insight.headline }}</p>
          <p v-if="insight.subline" class="text-xs text-muted-foreground">{{ insight.subline }}</p>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { EmployeeSummaryCard, EmployeeSummaryInsight } from '@/features/employee/composables/useEmployeeSummary';

withDefaults(
  defineProps<{
    cards: EmployeeSummaryCard[];
    insights: EmployeeSummaryInsight[];
  }>(),
  {
    cards: () => [],
    insights: () => [],
  },
);
</script>
