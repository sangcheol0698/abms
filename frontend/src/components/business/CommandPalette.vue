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
          {{ item.label }}
          <CommandShortcut>{{ item.shortcut }}</CommandShortcut>
        </CommandItem>
      </CommandGroup>
      <CommandSeparator />
      <CommandGroup heading="도움말">
        <CommandItem value="docs" @select="openDocs">문서 열기</CommandItem>
      </CommandGroup>
    </CommandList>
  </CommandDialog>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
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
  { label: '대시보드', to: '/', shortcut: '' },
  { label: '조직도', to: '/organization', shortcut: '' },
  { label: '구성원', to: '/employees', shortcut: '' },
  { label: '프로젝트', to: '/projects', shortcut: '' },
  { label: 'AI Copilot', to: '/assistant', shortcut: '' },
];

function handleOpenChange(value: boolean) {
  emit('update:open', value);
}

async function navigate(path: string) {
  emit('update:open', false);
  await router.push(path);
}

function openDocs() {
  emit('update:open', false);
  window.open('/docs/도메인모델.md', '_blank');
}
</script>
