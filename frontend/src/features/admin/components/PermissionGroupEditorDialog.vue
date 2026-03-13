<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="overflow-hidden p-0 sm:max-w-4xl">
      <div class="flex max-h-[85vh] flex-col">
      <DialogHeader class="border-b border-border px-6 pt-6 pb-4">
        <DialogTitle>{{ mode === 'create' ? '커스텀 권한 그룹 생성' : '권한 그룹 수정' }}</DialogTitle>
        <DialogDescription>
          권한 코드별로 scope를 선택해 그룹을 구성합니다. 시스템 그룹은 수정할 수 없습니다.
        </DialogDescription>
      </DialogHeader>

      <div class="grid flex-1 gap-4 overflow-y-auto px-6 py-4">
        <div class="grid gap-2">
          <Label for="permission-group-name">이름</Label>
          <Input id="permission-group-name" v-model="name" maxlength="50" />
        </div>

        <div class="grid gap-2">
          <Label for="permission-group-description">설명</Label>
          <Textarea
            id="permission-group-description"
            v-model="description"
            class="min-h-24"
            maxlength="255"
          />
        </div>

        <div class="grid gap-3">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-sm font-semibold text-foreground">권한 매트릭스</h3>
              <p class="text-xs text-muted-foreground">
                체크된 scope가 하나 이상인 권한만 저장됩니다.
              </p>
            </div>
          </div>

          <ScrollArea class="max-h-[40vh] rounded-xl border border-border">
            <div class="divide-y divide-border">
              <div
                v-for="permission in catalog.permissions"
                :key="permission.code"
                class="grid gap-3 p-4"
              >
                <div class="space-y-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="text-sm font-medium text-foreground">{{ permission.name }}</span>
                    <Badge variant="secondary">{{ permission.code }}</Badge>
                  </div>
                  <p class="text-xs text-muted-foreground">{{ permission.description }}</p>
                </div>

                <div class="flex flex-wrap gap-3">
                  <label
                    v-for="scope in permission.availableScopes"
                    :key="`${permission.code}-${scope.code}`"
                    class="flex items-center gap-2 rounded-lg border border-border px-3 py-2 text-sm"
                  >
                    <Checkbox
                      :model-value="isScopeChecked(permission.code, scope.code)"
                      @update:model-value="
                        (checked) => toggleScope(permission.code, scope.code, Boolean(checked))
                      "
                    />
                    <span>{{ scope.description }}</span>
                  </label>
                </div>
              </div>
            </div>
          </ScrollArea>
        </div>
      </div>

      <DialogFooter class="border-t border-border px-6 py-4">
        <Button type="button" variant="outline" :disabled="loading" @click="handleOpenChange(false)">취소</Button>
        <Button type="button" :disabled="loading || !canSubmit" @click="submit">
          {{ loading ? '저장 중...' : mode === 'create' ? '생성' : '저장' }}
        </Button>
      </DialogFooter>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
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
import { Textarea } from '@/components/ui/textarea';
import type {
  PermissionGroupCatalog,
  PermissionGroupDetailItem,
  PermissionGroupUpsertPayload,
} from '@/features/admin/models/permissionGroup';

const props = withDefaults(
  defineProps<{
    open: boolean;
    mode: 'create' | 'edit';
    loading?: boolean;
    catalog: PermissionGroupCatalog;
    initialGroup?: PermissionGroupDetailItem | null;
  }>(),
  {
    loading: false,
    initialGroup: null,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'submit', payload: PermissionGroupUpsertPayload): void;
}>();

const name = ref('');
const description = ref('');
const selectedScopesByPermission = ref<Record<string, string[]>>({});

const canSubmit = computed(() => name.value.trim().length > 0 && description.value.trim().length > 0);

watch(
  () => [props.open, props.initialGroup, props.mode] as const,
  ([open]) => {
    if (open) {
      resetForm();
    }
  },
  { immediate: true },
);

function resetForm() {
  if (props.mode === 'edit' && props.initialGroup) {
    name.value = props.initialGroup.name;
    description.value = props.initialGroup.description;
    selectedScopesByPermission.value = Object.fromEntries(
      props.initialGroup.grants.map((grant) => [grant.permissionCode, [...grant.scopes]]),
    );
    return;
  }

  name.value = '';
  description.value = '';
  selectedScopesByPermission.value = {};
}

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

function isScopeChecked(permissionCode: string, scopeCode: string): boolean {
  return selectedScopesByPermission.value[permissionCode]?.includes(scopeCode) ?? false;
}

function toggleScope(permissionCode: string, scopeCode: string, checked: boolean) {
  const next = new Set(selectedScopesByPermission.value[permissionCode] ?? []);

  if (checked) {
    next.add(scopeCode);
  } else {
    next.delete(scopeCode);
  }

  if (next.size === 0) {
    delete selectedScopesByPermission.value[permissionCode];
    selectedScopesByPermission.value = {
      ...selectedScopesByPermission.value,
    };
    return;
  }

  selectedScopesByPermission.value = {
    ...selectedScopesByPermission.value,
    [permissionCode]: Array.from(next),
  };
}

function submit() {
  const grants = Object.entries(selectedScopesByPermission.value)
    .map(([permissionCode, scopes]) => ({
      permissionCode,
      scopes: [...scopes],
    }))
    .filter((grant) => grant.scopes.length > 0)
    .sort((a, b) => a.permissionCode.localeCompare(b.permissionCode));

  emit('submit', {
    name: name.value.trim(),
    description: description.value.trim(),
    grants,
  });
}
</script>
