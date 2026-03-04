<template>
  <div class="flex items-start gap-3 group">
    <div class="flex flex-col gap-1 text-sm min-w-0 items-start w-full">
      <div class="flex items-center gap-2 mb-1 animate-pulse select-none">
        <span class="text-sm font-medium text-muted-foreground">생각 중...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ChatLoadingBubble' });
</script>
