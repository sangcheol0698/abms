<template>
  <div
    v-if="!employee"
    class="rounded-lg border border-dashed border-border/60 bg-muted/10 p-6 text-sm text-muted-foreground"
  >
    구성원 정보를 찾을 수 없습니다.
  </div>
  <div v-else class="flex flex-col gap-4 rounded-lg md:flex-row md:items-center md:gap-6">
    <Avatar class="h-20 w-20 rounded-2xl border border-border/60 bg-background">
      <AvatarImage :src="employee.avatarImageUrl" :alt="employee.name ?? 'Employee avatar'" />
      <AvatarFallback class="rounded-2xl text-lg font-semibold">
        {{ employeeInitials }}
      </AvatarFallback>
    </Avatar>
    <div class="flex flex-1 flex-col gap-1">
      <h1 class="text-2xl font-semibold tracking-tight text-foreground">{{ employee.name }}</h1>
      <p class="text-sm text-muted-foreground">{{ employee.email }}</p>
      <div class="flex flex-wrap items-center gap-2 text-sm text-muted-foreground/80">
        <Badge variant="outline">{{ employee.status || '—' }}</Badge>
        <span class="text-border/60">|</span>
        <button
          type="button"
          class="font-medium text-primary underline underline-offset-4 transition hover:text-primary/80 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:text-muted-foreground"
          :disabled="!employee.departmentId"
          @click="emit('department-click')"
        >
          {{ employee.departmentName || '—' }}
        </button>
        <span class="text-border/60">|</span>
        <span>{{ employee.position || '—' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import type { EmployeeSummary } from '@/features/employee/models/employee';

interface Props {
  employee: EmployeeSummary | null;
  employeeInitials: string;
}

import { toRefs } from 'vue';

const props = defineProps<Props>();
const emit = defineEmits<{
  'department-click': [];
}>();

const { employee, employeeInitials } = toRefs(props);
</script>
