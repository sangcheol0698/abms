<template>
  <div class="space-y-4">
    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <Card v-for="card in cards" :key="card.id" class="shadow-sm">
        <CardHeader class="space-y-1">
          <CardTitle class="text-sm font-semibold text-muted-foreground">{{
            card.title
          }}</CardTitle>
          <div class="flex items-baseline gap-2">
            <span class="text-2xl font-semibold text-foreground">{{ card.value }}</span>
            <Badge
              v-if="card.trend?.direction"
              :variant="
                card.trend.direction === 'down'
                  ? 'destructive'
                  : card.trend.direction === 'up'
                    ? 'secondary'
                    : 'outline'
              "
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
  </div>
</template>

<script setup lang="ts">
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { EmployeeSummaryCard } from '@/features/employee/composables/useEmployeeSummary';

withDefaults(
  defineProps<{
    cards: EmployeeSummaryCard[];
  }>(),
  {
    cards: () => [],
  },
);
</script>
