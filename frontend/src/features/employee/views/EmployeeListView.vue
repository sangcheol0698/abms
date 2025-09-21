<template>
  <section class="flex h-full flex-col gap-4">
    <header class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight">구성원</h1>
        <p class="text-sm text-muted-foreground">직원 목록과 상세 정보를 관리합니다.</p>
      </div>
      <button
        type="button"
        class="inline-flex items-center gap-2 rounded-md border border-border bg-primary px-4 py-2 text-sm font-medium text-primary-foreground shadow-sm transition hover:bg-primary/90">
        새 직원 등록
      </button>
    </header>
    <div class="flex h-full items-center justify-center rounded-lg border border-dashed border-muted p-6 text-sm text-muted-foreground">
      직원 API 연동 준비 중입니다.
    </div>
  </section>
</template>

<script setup lang="ts">
// TODO: EmployeeFinder 연동 후 직원 목록 표 추가 예정
</script>
