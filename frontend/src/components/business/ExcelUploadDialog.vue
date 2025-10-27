<template>
  <Dialog :open="open" @update:open="handleOpenChange">
    <DialogContent class="w-full max-w-lg">
      <DialogHeader>
        <DialogTitle>{{ title }}</DialogTitle>
        <DialogDescription>
          {{ description }}
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-2">
        <div class="rounded-xl border border-dashed border-border/60 bg-muted/30 p-2">
          <div class="text-center space-y-2">
            <FileSpreadsheet class="mx-auto h-6 w-6 text-muted-foreground" />
            <div class="space-y-1">
              <p class="text-xs font-semibold text-foreground">샘플 파일</p>
              <p class="text-xs text-muted-foreground">
                업로드 형식이 필요하다면 샘플 파일을 먼저 내려받으세요.
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              class="w-full sm:w-auto"
              :disabled="isDownloading"
              @click="downloadSample"
            >
              <Download class="mr-2 h-4 w-4" />
              {{ isDownloading ? '다운로드 중...' : '샘플 다운로드' }}
            </Button>
          </div>
        </div>

        <div
          class="rounded-xl border border-dashed p-10 sm:p-12 transition-colors min-h-56"
          :class="isDragging ? 'border-primary bg-primary/10' : 'border-border/60 bg-background'"
          @dragover.prevent="handleDragOver"
          @dragleave.prevent="handleDragLeave"
          @drop.prevent="handleDrop"
        >
          <div class="flex flex-col items-center gap-4 text-center">
            <Upload class="h-12 w-12 text-muted-foreground" />
            <div class="space-y-3">
              <input
                ref="inputRef"
                type="file"
                accept=".xlsx,.xls"
                class="hidden"
                @change="handleFileChange"
              />
              <Button variant="outline" size="lg" class="w-full sm:w-auto" :disabled="isUploading" @click="openFilePicker">
                <Upload class="mr-2 h-5 w-5" />
                파일 선택
              </Button>
              <p class="text-sm text-muted-foreground">
                또는 이 영역에 파일을 끌어다 놓으세요 (.xlsx, .xls / 최대 10MB)
              </p>
            </div>

            <div v-if="selectedFile" class="flex flex-col items-center gap-1 text-sm">
              <div class="flex items-center gap-2">
                <FileSpreadsheet class="h-4 w-4" />
                <span class="font-medium text-foreground">{{ selectedFile.name }}</span>
                <Button
                  variant="ghost"
                  size="icon"
                  class="h-6 w-6"
                  :disabled="isUploading"
                  @click="clearFile"
                >
                  <X class="h-3 w-3" />
                </Button>
              </div>
              <span class="text-xs text-muted-foreground">
                {{ formatFileSize(selectedFile.size) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="isUploading" class="space-y-2">
          <div class="flex items-center justify-between text-sm text-muted-foreground">
            <span>업로드 중...</span>
            <span>{{ uploadProgress }}%</span>
          </div>
          <div class="h-2 rounded-full bg-muted">
            <div
              class="h-2 rounded-full bg-primary transition-all duration-300"
              :style="{ width: `${uploadProgress}%` }"
            />
          </div>
        </div>

        <div v-if="errorMessage" class="rounded-md border border-destructive/20 bg-destructive/10 p-3">
          <div class="flex items-start gap-2 text-sm text-destructive">
            <AlertCircle class="mt-0.5 h-4 w-4" />
            <p class="whitespace-pre-line">{{ errorMessage }}</p>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" :disabled="isUploading" @click="handleCancel">
          취소
        </Button>
        <Button :disabled="!selectedFile || isUploading" @click="uploadFile">
          <template v-if="isUploading">
            <Loader2 class="mr-2 h-4 w-4 animate-spin" />
            업로드 중...
          </template>
          <template v-else>
            <Upload class="mr-2 h-4 w-4" />
            업로드
          </template>
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { AlertCircle, Download, FileSpreadsheet, Loader2, Upload, X } from 'lucide-vue-next';
import { formatFileSize, validateExcelFile } from '@/core/utils/excel';
import HttpError from '@/core/http/HttpError';

interface Props {
  open: boolean;
  title?: string;
  description?: string;
  onDownloadSample: () => Promise<void>;
  onUpload: (file: File, onProgress: (progress: number) => void) => Promise<void>;
}

const props = withDefaults(defineProps<Props>(), {
  title: '엑셀 파일 업로드',
  description: '업로드할 엑셀 파일을 선택하세요.',
});

const emit = defineEmits<{
  (event: 'update:open', value: boolean): void;
  (event: 'success'): void;
  (event: 'error', message: string): void;
  (event: 'sample'): void;
}>();

const selectedFile = ref<File | null>(null);
const isUploading = ref(false);
const isDownloading = ref(false);
const uploadProgress = ref(0);
const errorMessage = ref('');
const isDragging = ref(false);
const inputRef = ref<HTMLInputElement>();

const open = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
});

function handleOpenChange(value: boolean) {
  open.value = value;
  if (!value) {
    resetState();
  }
}

function handleCancel() {
  open.value = false;
}

function resetState() {
  selectedFile.value = null;
  isUploading.value = false;
  isDownloading.value = false;
  uploadProgress.value = 0;
  errorMessage.value = '';
  isDragging.value = false;
}

function openFilePicker() {
  inputRef.value?.click();
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file) {
    validateAndSetFile(file);
    target.value = '';
  }
}

function validateAndSetFile(file: File) {
  const { isValid, message } = validateExcelFile(file);
  if (!isValid) {
    errorMessage.value = message ?? '업로드 가능한 파일이 아닙니다.';
    return;
  }
  selectedFile.value = file;
  errorMessage.value = '';
}

function clearFile() {
  selectedFile.value = null;
  errorMessage.value = '';
}

function handleDragOver() {
  isDragging.value = true;
}

function handleDragLeave() {
  isDragging.value = false;
}

function handleDrop(event: DragEvent) {
  isDragging.value = false;
  const file = event.dataTransfer?.files?.[0];
  if (file) {
    validateAndSetFile(file);
  }
}

async function downloadSample() {
  if (isDownloading.value) {
    return;
  }
  isDownloading.value = true;
  errorMessage.value = '';
  try {
    await props.onDownloadSample();
    emit('sample');
  } catch (error) {
    const message = extractMessage(error, '샘플 파일 다운로드 중 오류가 발생했습니다.');
    errorMessage.value = message;
    emit('error', message);
  } finally {
    isDownloading.value = false;
  }
}

async function uploadFile() {
  if (!selectedFile.value || isUploading.value) {
    return;
  }

  isUploading.value = true;
  uploadProgress.value = 0;
  errorMessage.value = '';

  try {
    await props.onUpload(selectedFile.value, (progress) => {
      uploadProgress.value = progress;
    });
    emit('success');
    open.value = false;
  } catch (error) {
    const message = extractMessage(error, '엑셀 업로드 중 오류가 발생했습니다.');
    errorMessage.value = message;
    emit('error', message);
  } finally {
    isUploading.value = false;
  }
}

function extractMessage(error: unknown, fallbackMessage: string): string {
  if (error instanceof HttpError) {
    return error.message;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallbackMessage;
}
</script>
