<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="sm:max-w-3xl">
      <DialogHeader>
        <DialogTitle>계정 추가</DialogTitle>
        <DialogDescription>
          검색 결과에서 계정을 선택해 현재 권한 그룹에 할당합니다.
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-4 py-4">
        <div class="grid gap-2">
          <Label for="assignable-account-search">이름 또는 이메일 검색</Label>
          <Input
            id="assignable-account-search"
            :model-value="keyword"
            placeholder="예: 홍길동, user@abacus.co.kr"
            @update:model-value="(value) => emit('update:keyword', String(value ?? ''))"
          />
        </div>

        <ScrollArea class="max-h-[45vh] rounded-xl border border-border">
          <div v-if="loading" class="p-6 text-sm text-muted-foreground">계정 목록을 불러오는 중...</div>
          <div
            v-else-if="accounts.length === 0"
            class="p-6 text-sm text-muted-foreground"
          >
            추가 가능한 계정이 없습니다.
          </div>
          <div v-else class="divide-y divide-border">
            <button
              v-for="account in accounts"
              :key="account.accountId"
              type="button"
              class="flex w-full items-start justify-between gap-4 px-4 py-3 text-left transition-colors hover:bg-muted/60"
              @click="emit('assign', account.accountId)"
            >
              <div class="space-y-1">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-medium text-foreground">{{ account.employeeName }}</span>
                  <Badge variant="secondary">{{ account.position.description }}</Badge>
                </div>
                <p class="text-xs text-muted-foreground">{{ account.email }}</p>
                <p class="text-xs text-muted-foreground">{{ account.departmentName || '부서 미지정' }}</p>
              </div>
              <Button variant="outline" size="sm">추가</Button>
            </button>
          </div>
        </ScrollArea>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleOpenChange(false)">닫기</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ScrollArea } from '@/components/ui/scroll-area';
import type { AssignableAccountSummary } from '@/features/admin/models/permissionGroup';

defineProps<{
  open: boolean;
  keyword: string;
  loading: boolean;
  accounts: AssignableAccountSummary[];
}>();

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'update:keyword', value: string): void;
  (event: 'assign', accountId: number): void;
}>();

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}
</script>
