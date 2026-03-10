<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Bot, Briefcase, Handshake, Network, PieChart, ShieldCheck, UserCircle } from 'lucide-vue-next';
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
  CommandShortcut,
} from '@/components/ui/command';
import { hasStoredPermission } from '@/features/auth/session';
import { canReadEmployeeDetail } from '@/features/employee/permissions';

interface NavigationCommand {
  label: string;
  to: string;
  shortcut: string;
  icon: any;
}

withDefaults(
  defineProps<{
    open?: boolean;
  }>(),
  {
    open: false,
  },
);

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
}>();

const router = useRouter();
const canManagePermissionGroups = ref(hasStoredPermission('permission.group.manage'));
const canReadEmployees = ref(canReadEmployeeDetail());

const navigationCommands = computed<NavigationCommand[]>(() => [
  { label: '대시보드', to: '/', shortcut: '', icon: PieChart },
  { label: '부서', to: '/departments', shortcut: '', icon: Network },
  ...(canReadEmployees.value
    ? [{ label: '직원', to: '/employees', shortcut: '', icon: UserCircle }]
    : []),
  { label: '프로젝트', to: '/projects', shortcut: '', icon: Briefcase },
  { label: '협력사', to: '/parties', shortcut: '', icon: Handshake },
  { label: 'AI Assistant', to: '/assistant', shortcut: '', icon: Bot },
  ...(canManagePermissionGroups.value
    ? [{ label: '권한 그룹 관리', to: '/system/permission-groups', shortcut: '', icon: ShieldCheck }]
    : []),
]);

function refreshPermissions() {
  canManagePermissionGroups.value = hasStoredPermission('permission.group.manage');
  canReadEmployees.value = canReadEmployeeDetail();
}

function onStorage(event: StorageEvent) {
  if (event.key === 'user') {
    refreshPermissions();
  }
}

onMounted(() => {
  refreshPermissions();
  window.addEventListener('storage', onStorage);
});

onBeforeUnmount(() => {
  window.removeEventListener('storage', onStorage);
});

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

async function navigate(path: string) {
  emit('update:open', false);
  await router.push(path);
}
</script>

<template>
  <CommandDialog :open="open" @update:open="handleOpenChange">
    <CommandInput placeholder="명령을 검색하세요..." />
    <CommandList>
      <CommandEmpty>일치하는 명령이 없습니다.</CommandEmpty>
      <CommandGroup heading="화면 이동">
        <CommandItem
          v-for="item in navigationCommands"
          :key="item.to"
          :value="item.label"
          @select="navigate(item.to)"
        >
          <component :is="item.icon" class="mr-2 h-4 w-4" />
          <span>{{ item.label }}</span>
          <CommandShortcut>{{ item.shortcut }}</CommandShortcut>
        </CommandItem>
      </CommandGroup>
      <CommandSeparator />
    </CommandList>
  </CommandDialog>
</template>
