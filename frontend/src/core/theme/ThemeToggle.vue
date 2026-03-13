<template>
  <DropdownMenu>
    <DropdownMenuTrigger as-child>
      <Button
        :variant="variant"
        :size="size"
        :class="cn('rounded-lg text-muted-foreground hover:text-foreground', props.class)"
        :aria-label="`${currentOption.label} 테마 선택`"
      >
        <Sun v-if="resolvedTheme === 'light'" class="h-4 w-4" />
        <Moon v-else class="h-4 w-4" />
        <span class="sr-only">{{ currentOption.label }} 테마 선택</span>
      </Button>
    </DropdownMenuTrigger>
    <DropdownMenuContent align="end" class="w-40">
      <DropdownMenuLabel class="space-y-1">
        <p>현재 선택: {{ currentOption.label }}</p>
        <p>적용 중: {{ resolvedOption.label }}</p>
      </DropdownMenuLabel>
      <DropdownMenuSeparator />
      <DropdownMenuRadioGroup :model-value="theme">
        <DropdownMenuRadioItem
          v-for="option in themeOptions"
          :key="option.value"
          :value="option.value"
          class="items-center"
          @click="setTheme(option.value)"
        >
          <span>{{ option.label }}</span>
        </DropdownMenuRadioItem>
      </DropdownMenuRadioGroup>
    </DropdownMenuContent>
  </DropdownMenu>
</template>

<script setup lang="ts">
import type { HTMLAttributes } from 'vue';
import { computed } from 'vue';
import { Moon, Sun } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import type { ButtonVariants } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useTheme } from '@/core/composables';
import { themeOptions } from '@/core/theme/theme';
import { cn } from '@/lib/utils';

const props = withDefaults(
  defineProps<{
    variant?: ButtonVariants['variant'];
    size?: ButtonVariants['size'];
    class?: HTMLAttributes['class'];
  }>(),
  {
    variant: 'ghost',
    size: 'icon',
  },
);

const { theme, resolvedTheme, setTheme } = useTheme();

const currentOption = computed(
  () => themeOptions.find((option) => option.value === theme.value) ?? themeOptions[0],
);

const resolvedOption = computed(
  () => themeOptions.find((option) => option.value === resolvedTheme.value) ?? themeOptions[0],
);
</script>
