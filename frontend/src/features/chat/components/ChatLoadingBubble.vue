<template>
  <div class="flex items-start gap-3">
    <div class="flex items-center space-x-1">
      <div class="h-1.5 w-1.5 animate-bounce rounded-full bg-primary/60 [animation-delay:-0.3s]"></div>
      <div class="h-1.5 w-1.5 animate-bounce rounded-full bg-primary/60 [animation-delay:-0.15s]"></div>
      <div class="h-1.5 w-1.5 animate-bounce rounded-full bg-primary/60"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'ChatLoadingBubble' });
</script>
