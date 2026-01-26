<template>
  <section class="flex h-full flex-col gap-6">
    <div
      v-if="isLoading"
      class="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10"
    >
      <span class="text-sm text-muted-foreground">프로젝트 정보를 불러오는 중입니다...</span>
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>프로젝트 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <template v-else>
      <ProjectDetailHeader
        :project="project"
        @party-click="goToParty"
      >
        <template #actions>
          <AlertDialog>
            <AlertDialogTrigger as-child>
              <Button variant="outline" size="sm" class="text-destructive hover:text-destructive">
                <Trash2 class="mr-2 h-4 w-4" />
                삭제
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>프로젝트를 삭제하시겠습니까?</AlertDialogTitle>
                <AlertDialogDescription>
                  {{ project?.name }} 프로젝트가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>취소</AlertDialogCancel>
                <AlertDialogAction
                  class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  :disabled="isDeleting"
                  @click="handleDelete"
                >
                  {{ isDeleting ? '삭제 중...' : '삭제' }}
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
          <Button variant="outline" size="sm" @click="openEditDialog">
            <Pencil class="mr-2 h-4 w-4" />
            프로젝트 편집
          </Button>
        </template>
      </ProjectDetailHeader>

      <Tabs default-value="overview" class="flex min-h-[320px] flex-1 flex-col gap-4">
        <TabsList class="flex-wrap">
          <TabsTrigger value="overview">개요</TabsTrigger>
          <TabsTrigger value="assignments">투입인력</TabsTrigger>
        </TabsList>

        <div class="flex flex-1 flex-col gap-4">
          <TabsContent value="overview" class="flex-1">
            <ProjectOverviewPanel
              :project="project"
              :format-date="formatDate"
              @party-click="goToParty"
            />
          </TabsContent>
          <TabsContent value="assignments" class="flex-1">
            <ProjectAssignmentPanel :project-id="project?.projectId ?? 0" />
          </TabsContent>
        </div>
      </Tabs>
    </template>

    <ProjectUpdateDialog
      :open="isProjectUpdateDialogOpen"
      :project="project ?? undefined"
      @update:open="isProjectUpdateDialogOpen = $event"
      @updated="handleProjectUpdated"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { Pencil, Trash2 } from 'lucide-vue-next';
import { appContainer } from '@/core/di/container';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import type { ProjectDetail } from '@/features/project/models/projectDetail';
import HttpError from '@/core/http/HttpError';
import ProjectDetailHeader from '@/features/project/components/ProjectDetailHeader.vue';
import ProjectOverviewPanel from '@/features/project/components/ProjectOverviewPanel.vue';
import ProjectAssignmentPanel from '@/features/project/components/ProjectAssignmentPanel.vue';
import ProjectUpdateDialog from '@/features/project/components/ProjectUpdateDialog.vue';

defineOptions({ name: 'ProjectDetailView' });

const route = useRoute();
const router = useRouter();

const repository = appContainer.resolve(ProjectRepository);

const project = ref<ProjectDetail | null>(null);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const isProjectUpdateDialogOpen = ref(false);
const isDeleting = ref(false);

watch(
  () => route.params.projectId,
  (next) => {
    if (typeof next === 'string' && next.trim().length > 0) {
      const projectId = Number(next);
      if (!isNaN(projectId)) {
        fetchProject(projectId);
      }
    }
  },
  { immediate: true },
);

function resolveErrorMessage(error: unknown, fallback: string) {
  return error instanceof HttpError ? error.message : fallback;
}

async function fetchProject(projectId: number, options: { showLoading?: boolean } = {}) {
  const { showLoading = true } = options;

  if (showLoading) {
    isLoading.value = true;
    errorMessage.value = null;
    project.value = null;
  }

  try {
    const result = await repository.find(projectId);
    project.value = result;
    return result;
  } catch (error) {
    const message = resolveErrorMessage(error, '프로젝트 정보를 불러오는 중 오류가 발생했습니다.');
    if (showLoading) {
      errorMessage.value = message;
      project.value = null;
      return null;
    }
    throw error instanceof Error ? error : new Error(message);
  } finally {
    if (showLoading) {
      isLoading.value = false;
    }
  }
}

function goToParty() {
  const id = project.value?.partyId;
  if (!id) return;
  router.push({ name: 'party-detail', params: { partyId: id } });
}

function formatDate(value?: string | null) {
  if (!value) {
    return '—';
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleDateString();
}

function openEditDialog() {
  isProjectUpdateDialogOpen.value = true;
}

function handleProjectUpdated() {
  isProjectUpdateDialogOpen.value = false;
  if (project.value?.projectId) {
    fetchProject(project.value.projectId, { showLoading: false });
  }
}

async function handleDelete() {
  if (!project.value?.projectId) return;
  
  isDeleting.value = true;
  try {
    await repository.delete(project.value.projectId);
    router.push('/projects');
  } catch (error) {
    const message = resolveErrorMessage(error, '프로젝트 삭제 중 오류가 발생했습니다.');
    errorMessage.value = message;
  } finally {
    isDeleting.value = false;
  }
}
</script>
