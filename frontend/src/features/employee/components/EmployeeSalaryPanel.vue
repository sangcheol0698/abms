<template>
  <div class="flex h-full flex-col gap-4 rounded-lg border border-dashed border-border/60 bg-muted/10 p-6">
    <header class="flex items-start justify-between gap-4">
      <div>
        <h2 class="text-base font-semibold text-foreground">연봉 현황</h2>
        <p class="text-sm text-muted-foreground">
          연봉 이력과 변경 내역은 전담 API 연동 후 이 영역에서 확인할 수 있습니다.
        </p>
      </div>
      <Button variant="outline" disabled>연봉 변경</Button>
    </header>

    <Alert>
      <AlertTitle>연동 대기</AlertTitle>
      <AlertDescription>
        현재 서버에서 연봉 정보를 제공하지 않아 기본 데이터만 노출합니다. 연봉 이력 API가 준비되면
        자동으로 연결됩니다.
      </AlertDescription>
    </Alert>

    <div class="rounded-lg border border-border/60 bg-background p-4 text-sm text-muted-foreground">
      <p class="font-medium text-foreground">현재 연봉</p>
      <p class="mt-1 text-2xl font-semibold text-muted-foreground/70">추가 정보 대기</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import type { EmployeeSummary } from '@/features/employee/models/employee';

interface Props {
  employee: EmployeeSummary | null;
}

defineProps<Props>();
</script>
