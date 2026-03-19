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
        :can-manage="canManageParty"
        @edit="openEditDialog"
        @delete="openDeleteDialog"
      />

      <Tabs v-model="activeTab" class="flex min-h-[320px] flex-1 flex-col gap-4">
        <TabsList class="flex-wrap">
          <TabsTrigger value="overview">개요</TabsTrigger>
          <TabsTrigger v-if="canViewProjectsTab" value="projects">프로젝트</TabsTrigger>
        </TabsList>

        <div class="flex flex-1 flex-col gap-4">
          <TabsContent value="overview" class="flex-1">
            <PartyOverviewPanel :party="party" />
          </TabsContent>
          <TabsContent v-if="canViewProjectsTab" value="projects" class="flex-1">
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
import { computed, ref } from 'vue';
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
import PartyDetailHeader from '@/features/party/components/PartyDetailHeader.vue';
import PartyOverviewPanel from '@/features/party/components/PartyOverviewPanel.vue';
import PartyProjectsPanel from '@/features/party/components/PartyProjectsPanel.vue';
import PartyFormDialog from '@/features/party/components/PartyFormDialog.vue';
import { useDeletePartyMutation, usePartyDetailQuery } from '@/features/party/queries/usePartyQueries';
import { partyKeys, queryClient } from '@/core/query';
import { canManageParties, canReadParties } from '@/features/party/permissions';
import { canReadProjects } from '@/features/project/permissions';

defineOptions({ name: 'PartyDetailView' });

const route = useRoute();
const router = useRouter();
const PARTY_DETAIL_TABS = ['overview', 'projects'] as const;
type PartyDetailTab = (typeof PARTY_DETAIL_TABS)[number];

function isPartyDetailTab(value: unknown): value is PartyDetailTab {
  return typeof value === 'string' && PARTY_DETAIL_TABS.includes(value as PartyDetailTab);
}

const partyId = computed(() => {
  const raw = route.params.partyId;
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : 0;
});
const activeTab = computed<PartyDetailTab>({
  get() {
    const tab = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab;
    if (tab === 'projects' && !canViewProjectsTab.value) {
      return 'overview';
    }
    return isPartyDetailTab(tab) ? tab : 'overview';
  },
  async set(tab) {
    const nextQuery = { ...route.query };
    if (tab === 'overview') {
      delete nextQuery.tab;
    } else {
      nextQuery.tab = tab;
    }

    await router.replace({
      query: nextQuery,
    });
  },
});

const isFormDialogOpen = ref(false);
const isDeleteDialogOpen = ref(false);
const partyQuery = usePartyDetailQuery(partyId);
const deletePartyMutation = useDeletePartyMutation();

const party = computed(() => partyQuery.data.value ?? null);
const isLoading = computed(() => partyQuery.isLoading.value);
const errorMessage = computed(() => {
  const error = partyQuery.error.value;
  if (!error) {
    return null;
  }
  return error instanceof Error ? error.message : '협력사 정보를 불러오는 중 오류가 발생했습니다.';
});
const isDeleting = computed(() => deletePartyMutation.isPending.value);
const canManageParty = computed(() => canManageParties());
const canViewProjectsTab = computed(() => canReadParties() && canReadProjects());

function openEditDialog() {
  isFormDialogOpen.value = true;
}

function openDeleteDialog() {
  isDeleteDialogOpen.value = true;
}

async function handlePartyUpdated() {
  isFormDialogOpen.value = false;
  if (partyId.value > 0) {
    await queryClient.invalidateQueries({ queryKey: partyKeys.detail(partyId.value) });
  }
}

async function handleDelete() {
  if (!party.value?.partyId) return;

  try {
    await deletePartyMutation.mutateAsync(party.value.partyId);
    router.push('/parties');
  } catch {
    // Error state is surfaced by query/mutation error handling and toast in caller scope.
  }
}
</script>
