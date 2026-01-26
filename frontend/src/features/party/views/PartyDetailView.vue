<template>
  <section class="flex h-full flex-col gap-6">
    <div
      v-if="isLoading"
      class="flex h-[240px] items-center justify-center rounded-lg border border-dashed border-border/60 bg-muted/10"
    >
      <span class="text-sm text-muted-foreground">협력사 정보를 불러오는 중입니다...</span>
    </div>

    <Alert v-else-if="errorMessage" variant="destructive">
      <AlertTitle>협력사 정보를 불러오지 못했습니다.</AlertTitle>
      <AlertDescription>{{ errorMessage }}</AlertDescription>
    </Alert>

    <template v-else>
      <PartyDetailHeader
        :party="party"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
      />

      <Tabs default-value="overview" class="flex min-h-[320px] flex-1 flex-col gap-4">
        <TabsList class="flex-wrap">
          <TabsTrigger value="overview">개요</TabsTrigger>
          <TabsTrigger value="projects">프로젝트</TabsTrigger>
        </TabsList>

        <div class="flex flex-1 flex-col gap-4">
          <TabsContent value="overview" class="flex-1">
            <PartyOverviewPanel :party="party" />
          </TabsContent>
          <TabsContent value="projects" class="flex-1">
            <PartyProjectsPanel :party-id="party?.partyId ?? 0" />
          </TabsContent>
        </div>
      </Tabs>
    </template>

    <PartyFormDialog
      :open="isFormDialogOpen"
      mode="edit"
      :party="party"
      @update:open="isFormDialogOpen = $event"
      @updated="handlePartyUpdated"
    />

    <AlertDialog :open="isDeleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>협력사를 삭제하시겠습니까?</AlertDialogTitle>
          <AlertDialogDescription>
            {{ party?.name }} 협력사 정보가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="isDeleteDialogOpen = false">취소</AlertDialogCancel>
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
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
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
} from '@/components/ui/alert-dialog';
import { appContainer } from '@/core/di/container';
import PartyRepository from '@/features/party/repository/PartyRepository';
import type { PartyDetail } from '@/features/party/models/partyDetail';
import HttpError from '@/core/http/HttpError';
import PartyDetailHeader from '@/features/party/components/PartyDetailHeader.vue';
import PartyOverviewPanel from '@/features/party/components/PartyOverviewPanel.vue';
import PartyProjectsPanel from '@/features/party/components/PartyProjectsPanel.vue';
import PartyFormDialog from '@/features/party/components/PartyFormDialog.vue';

defineOptions({ name: 'PartyDetailView' });

const route = useRoute();
const router = useRouter();

const repository = appContainer.resolve(PartyRepository);

const party = ref<PartyDetail | null>(null);
const isLoading = ref(true);
const errorMessage = ref<string | null>(null);
const isFormDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const isDeleting = ref(false);

watch(
  () => route.params.partyId,
  (next) => {
    if (typeof next === 'string' && next.trim().length > 0) {
      const partyId = Number(next);
      if (!isNaN(partyId)) {
        fetchParty(partyId);
      }
    }
  },
  { immediate: true },
);

function resolveErrorMessage(error: unknown, fallback: string) {
  return error instanceof HttpError ? error.message : fallback;
}

async function fetchParty(partyId: number, options: { showLoading?: boolean } = {}) {
  const { showLoading = true } = options;

  if (showLoading) {
    isLoading.value = true;
    errorMessage.value = null;
    party.value = null;
  }

  try {
    const result = await repository.find(partyId);
    party.value = result;
    return result;
  } catch (error) {
    const message = resolveErrorMessage(error, '협력사 정보를 불러오는 중 오류가 발생했습니다.');
    if (showLoading) {
      errorMessage.value = message;
      party.value = null;
      return null;
    }
    throw error instanceof Error ? error : new Error(message);
  } finally {
    if (showLoading) {
      isLoading.value = false;
    }
  }
}

function openEditDialog() {
  isFormDialogOpen.value = true;
}

function openDeleteDialog() {
  isDeleteDialogOpen.value = true;
}

function handlePartyUpdated() {
  isFormDialogOpen.value = false;
  if (party.value?.partyId) {
    fetchParty(party.value.partyId, { showLoading: false });
  }
}

async function handleDelete() {
  if (!party.value?.partyId) return;

  isDeleting.value = true;
  try {
    await repository.delete(party.value.partyId);
    router.push('/parties');
  } catch (error) {
    const message = resolveErrorMessage(error, '협력사 삭제 중 오류가 발생했습니다.');
    errorMessage.value = message;
  } finally {
    isDeleting.value = false;
  }
}
</script>
