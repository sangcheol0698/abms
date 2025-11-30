<template>
  <div class="flex h-full flex-col gap-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-semibold tracking-tight">프로젝트 상세</h1>
      </div>
    </div>

    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <p class="text-muted-foreground">로딩 중...</p>
    </div>

    <div v-else-if="project" class="flex flex-col gap-6">
      <div class="rounded-lg border p-6">
        <div class="grid gap-4">
          <div class="grid grid-cols-[120px_1fr] gap-2">
            <dt class="text-sm font-medium text-muted-foreground">프로젝트 코드</dt>
            <dd class="text-sm">{{ project.code }}</dd>
          </div>
          <div class="grid grid-cols-[120px_1fr] gap-2">
            <dt class="text-sm font-medium text-muted-foreground">프로젝트명</dt>
            <dd class="text-sm font-semibold">{{ project.name }}</dd>
          </div>
          <div class="grid grid-cols-[120px_1fr] gap-2">
            <dt class="text-sm font-medium text-muted-foreground">상태</dt>
            <dd class="text-sm">{{ project.statusLabel }}</dd>
          </div>
          <div class="grid grid-cols-[120px_1fr] gap-2">
            <dt class="text-sm font-medium text-muted-foreground">계약금액</dt>
            <dd class="text-sm">{{ formatCurrency(project.contractAmount) }}</dd>
          </div>
          <div class="grid grid-cols-[120px_1fr] gap-2">
            <dt class="text-sm font-medium text-muted-foreground">프로젝트 기간</dt>
            <dd class="text-sm">{{ formatProjectPeriod(project.startDate, project.endDate) }}</dd>
          </div>
          <div v-if="project.description" class="grid grid-cols-[120px_1fr] gap-2">
            <dt class="text-sm font-medium text-muted-foreground">설명</dt>
            <dd class="text-sm">{{ project.description }}</dd>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="flex items-center justify-center py-12">
      <p class="text-muted-foreground">프로젝트를 찾을 수 없습니다.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { appContainer } from '@/core/di/container';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import { formatCurrency, formatProjectPeriod } from '@/features/project/models/projectListItem';

defineOptions({ name: 'ProjectDetailView' });

const route = useRoute();
const repository = appContainer.resolve(ProjectRepository);

const project = ref<ProjectDetail | null>(null);
const isLoading = ref(false);

async function loadProject() {
  const projectId = route.params.projectId as string;
  if (!projectId) return;

  isLoading.value = true;
  try {
    project.value = await repository.find(projectId);
  } catch (error) {
    console.error('프로젝트를 불러오지 못했습니다.', error);
    project.value = null;
  } finally {
    isLoading.value = false;
  }
}

onMounted(() => {
  loadProject();
});
</script>
