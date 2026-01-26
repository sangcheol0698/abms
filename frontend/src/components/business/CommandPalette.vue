<script setup lang="ts">
import { useRouter } from 'vue-router';
import { Bot, Briefcase, FileText, Network, PieChart, UserCircle } from 'lucide-vue-next';
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

const navigationCommands: NavigationCommand[] = [
  { label: '대시보드', to: '/', shortcut: '', icon: PieChart },
  { label: '부서', to: '/departments', shortcut: '', icon: Network },
  { label: '직원', to: '/employees', shortcut: '', icon: UserCircle },
  { label: '프로젝트', to: '/projects', shortcut: '', icon: Briefcase },
  { label: 'AI Assistant', to: '/assistant', shortcut: '', icon: Bot },
];

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
