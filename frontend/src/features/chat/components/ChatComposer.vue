<template>
  <form
    class="flex flex-col gap-2"
    @submit.prevent="submit"
    @dragover.prevent="handleDragOver"
    @drop.prevent="handleDrop"
  >
    <!-- Image Previews -->
    <div v-if="attachments.length > 0" class="flex gap-2 overflow-x-auto pb-2">
      <div v-for="(attachment, index) in attachments" :key="index" class="relative shrink-0">
        <div
          class="relative h-20 w-20 overflow-hidden rounded-lg border border-border/50 shadow-sm"
        >
          <img
            :src="attachment.previewUrl"
            alt="Selected image"
            class="h-full w-full object-cover transition-opacity duration-200"
            :class="{ 'opacity-50': attachment.uploading }"
          />

          <!-- Loading Spinner -->
          <div
            v-if="attachment.uploading"
            class="absolute inset-0 flex items-center justify-center bg-black/10 backdrop-blur-[1px]"
          >
            <Loader2 class="h-5 w-5 animate-spin text-white" />
          </div>
        </div>

        <button
          type="button"
          class="absolute right-1 top-1 flex h-5 w-5 items-center justify-center rounded-full bg-black/50 text-white backdrop-blur-[2px] transition-colors hover:bg-black/70"
          @click="removeFile(index)"
        >
          <X class="h-3 w-3" />
        </button>
      </div>
    </div>

    <!-- Hidden File Input -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      multiple
      class="hidden"
      @change="handleFileSelect"
    />

    <InputGroup class="rounded-2xl border border-border/60 bg-background shadow-sm">
      <InputGroupTextarea
        ref="textareaRef"
        v-model="draft"
        :rows="1"
        :disabled="disabled"
        placeholder="메시지를 입력하세요..."
        class="min-h-[48px] max-h-[200px] px-4 py-3 text-sm leading-relaxed resize-none whitespace-pre-wrap"
        @keydown="handleKeydown"
        @compositionstart="isComposing = true"
        @compositionend="handleCompositionEnd"
      />
      <InputGroupAddon align="block-end" class="gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <InputGroupButton
              type="button"
              variant="ghost"
              size="icon-xs"
              class="rounded-full"
              aria-label="더 보기"
            >
              <Plus class="h-4 w-4" />
            </InputGroupButton>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" class="w-56" :side-offset="8">
            <DropdownMenuItem @click="triggerFileInput">
              <Paperclip class="mr-2 h-4 w-4" />
              <span>사진 및 파일 추가</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem disabled>
              <Image class="mr-2 h-4 w-4" />
              <span>이미지 만들기</span>
            </DropdownMenuItem>
            <DropdownMenuItem disabled>
              <Lightbulb class="mr-2 h-4 w-4" />
              <span>잘 생각하기</span>
            </DropdownMenuItem>
            <DropdownMenuItem disabled>
              <Telescope class="mr-2 h-4 w-4" />
              <span>심층 리서치</span>
            </DropdownMenuItem>
            <DropdownMenuItem disabled>
              <ShoppingBag class="mr-2 h-4 w-4" />
              <span>쇼핑 어시스턴트</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem disabled>
              <MoreHorizontal class="mr-2 h-4 w-4" />
              <span>더 보기</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>

        <InputGroupText class="ml-auto text-[11px] text-muted-foreground">
          {{ infoText }}
        </InputGroupText>
        <Separator orientation="vertical" class="!h-4" />
        <InputGroupButton
          v-if="props.isResponding"
          type="button"
          variant="secondary"
          size="icon-xs"
          class="rounded-full"
          title="응답 중지"
          @click="emit('stop')"
        >
          <Square class="h-3.5 w-3.5" />
          <span class="sr-only">응답 중지</span>
        </InputGroupButton>
        <InputGroupButton
          v-else
          type="submit"
          variant="default"
          size="icon-xs"
          class="rounded-full"
          :disabled="
            disabled || (draft.trim().length === 0 && attachments.length === 0) || isUploading
          "
        >
          <ArrowUp class="h-4 w-4" />
          <span class="sr-only">전송</span>
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  </form>
</template>

<script setup lang="ts">
import { nextTick, ref, watch, onUnmounted, computed } from 'vue';
import {
  ArrowUp,
  Square,
  Plus,
  X,
  Paperclip,
  Image,
  Lightbulb,
  Telescope,
  ShoppingBag,
  MoreHorizontal,
  Loader2,
} from 'lucide-vue-next';
import {
  InputGroup,
  InputGroupAddon,
  InputGroupButton,
  InputGroupText,
  InputGroupTextarea,
} from '@/components/ui/input-group';
import { Separator } from '@/components/ui/separator';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    isResponding?: boolean;
    suggestions?: string[];
    infoText?: string;
  }>(),
  {
    disabled: false,
    isResponding: false,
    suggestions: () => [],
    infoText: 'Enter: 전송 · Shift + Enter: 줄바꿈',
  },
);

const emit = defineEmits<{
  submit: [value: string];
  suggestion: [value: string];
  stop: [];
}>();

interface FileAttachment {
  file: File;
  previewUrl: string;
  uploading: boolean;
}

const draft = defineModel<string>({ default: '' });
const textareaRef = ref<any>(null);
const isComposing = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);
const attachments = ref<FileAttachment[]>([]);

const isUploading = computed(() => attachments.value.some((a) => a.uploading));

async function focusTextarea() {
  await nextTick();
  const textarea = textareaRef.value?.$el as HTMLTextAreaElement | undefined;
  if (!textarea) return;
  textarea.focus();
  textarea.setSelectionRange(textarea.value.length, textarea.value.length);
}

function triggerFileInput() {
  fileInputRef.value?.click();
}

function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement;
  if (input.files) {
    addFiles(Array.from(input.files));
  }
  // Reset input so the same file can be selected again if needed
  input.value = '';
}

function handleDragOver(event: DragEvent) {
  // Prevent default behavior (Prevent file from being opened)
  event.preventDefault();
}

function handleDrop(event: DragEvent) {
  // Prevent default behavior (Prevent file from being opened)
  event.preventDefault();

  if (event.dataTransfer?.files) {
    addFiles(Array.from(event.dataTransfer.files));
  }
}

function addFiles(files: File[]) {
  const imageFiles = files.filter((file) => file.type.startsWith('image/'));
  if (imageFiles.length === 0) return;

  const newAttachments = imageFiles.map((file) => ({
    file,
    previewUrl: URL.createObjectURL(file),
    uploading: true,
  }));

  const startIndex = attachments.value.length;
  attachments.value = [...attachments.value, ...newAttachments];

  // Simulate upload for each file
  newAttachments.forEach((_, i) => {
    const index = startIndex + i;
    // Random delay between 1s and 2s
    const delay = 1000 + Math.random() * 1000;
    setTimeout(() => {
      if (attachments.value[index]) {
        attachments.value[index].uploading = false;
      }
    }, delay);
  });
}

function removeFile(index: number) {
  const attachment = attachments.value[index];
  if (attachment) {
    URL.revokeObjectURL(attachment.previewUrl);
  }
  attachments.value.splice(index, 1);
}

function clearFiles() {
  attachments.value.forEach((a) => URL.revokeObjectURL(a.previewUrl));
  attachments.value = [];
}

onUnmounted(() => {
  clearFiles();
});

async function submit() {
  const content = draft.value.trim();
  if ((!content && attachments.value.length === 0) || props.disabled || isUploading.value) {
    return;
  }

  emit('submit', content);

  draft.value = '';
  clearFiles();
  await focusTextarea();
}

watch(
  () => props.disabled,
  (value) => {
    if (!value) {
      void focusTextarea();
    }
  },
  { immediate: true },
);

function handleKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter' || event.shiftKey) {
    return;
  }
  if (isComposing.value) {
    return;
  }
  event.preventDefault();
  void submit();
}

function handleCompositionEnd() {
  isComposing.value = false;
}

defineExpose({
  addFiles,
});
</script>
