import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { dashboardKeys, projectKeys, queryClient } from '@/core/query';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import type { ProjectSearchParams } from '@/features/project/models/projectListItem';
import type { ProjectCreateData, ProjectUpdateData } from '@/features/project/models/projectDetail';
import ProjectRevenueRepository, {
  type ProjectRevenuePlanCreateRequest,
} from '@/features/project/repository/ProjectRevenueRepository';
import ProjectAssignmentRepository, {
  type ProjectAssignmentCreateRequest,
} from '@/features/project/repository/ProjectAssignmentRepository';

async function invalidateProjectSideEffects(projectId?: number) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: projectKeys.all }),
    queryClient.invalidateQueries({ queryKey: dashboardKeys.summary() }),
  ];

  if (projectId && projectId > 0) {
    tasks.push(queryClient.invalidateQueries({ queryKey: projectKeys.detail(projectId) }));
  }

  await Promise.all(tasks);
}

export function useProjectListQuery(paramsRef: MaybeRefOrGetter<ProjectSearchParams>) {
  const repository = appContainer.resolve(ProjectRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() =>
      projectKeys.list((params.value ?? {}) as unknown as Record<string, unknown>),
    ),
    queryFn: () => repository.search(params.value),
  });
}

export function useProjectDetailQuery(projectIdRef: MaybeRefOrGetter<number | null | undefined>) {
  const repository = appContainer.resolve(ProjectRepository);
  const projectId = computed(() => Number(toValue(projectIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => projectKeys.detail(projectId.value)),
    queryFn: () => repository.find(projectId.value),
    enabled: computed(() => projectId.value > 0),
  });
}

export function useProjectStatusesQuery() {
  const repository = appContainer.resolve(ProjectRepository);

  return useQuery({
    queryKey: projectKeys.statuses(),
    queryFn: () => repository.fetchStatuses(),
  });
}

export function useProjectRevenuePlansQuery(
  projectIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(ProjectRevenueRepository);
  const projectId = computed(() => Number(toValue(projectIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => projectKeys.revenuePlans(projectId.value)),
    queryFn: () => repository.findByProjectId(projectId.value),
    enabled: computed(() => projectId.value > 0),
  });
}

export function useProjectAssignmentsQuery(
  projectIdRef: MaybeRefOrGetter<number | null | undefined>,
) {
  const repository = appContainer.resolve(ProjectAssignmentRepository);
  const projectId = computed(() => Number(toValue(projectIdRef) ?? 0));

  return useQuery({
    queryKey: computed(() => projectKeys.assignments(projectId.value)),
    queryFn: () => repository.findByProjectId(projectId.value),
    enabled: computed(() => projectId.value > 0),
  });
}

export function useCreateProjectMutation() {
  const repository = appContainer.resolve(ProjectRepository);

  return useMutation({
    mutationFn: (data: ProjectCreateData) => repository.create(data),
    onSuccess: async (created) => {
      await invalidateProjectSideEffects(created.projectId);
    },
  });
}

export function useUpdateProjectMutation() {
  const repository = appContainer.resolve(ProjectRepository);

  return useMutation({
    mutationFn: (variables: { projectId: number; data: ProjectUpdateData }) =>
      repository.update(variables.projectId, variables.data),
    onSuccess: async (updated) => {
      await invalidateProjectSideEffects(updated.projectId);
    },
  });
}

export function useDeleteProjectMutation() {
  const repository = appContainer.resolve(ProjectRepository);

  return useMutation({
    mutationFn: (projectId: number) => repository.delete(projectId),
    onSuccess: async (_data, projectId) => {
      await invalidateProjectSideEffects(projectId);
    },
  });
}

export function useCompleteProjectMutation() {
  const repository = appContainer.resolve(ProjectRepository);

  return useMutation({
    mutationFn: (projectId: number) => repository.complete(projectId),
    onSuccess: async (updated) => {
      await invalidateProjectSideEffects(updated.projectId);
    },
  });
}

export function useCancelProjectMutation() {
  const repository = appContainer.resolve(ProjectRepository);

  return useMutation({
    mutationFn: (projectId: number) => repository.cancel(projectId),
    onSuccess: async (updated) => {
      await invalidateProjectSideEffects(updated.projectId);
    },
  });
}

export function useCreateProjectRevenuePlanMutation() {
  const repository = appContainer.resolve(ProjectRevenueRepository);

  return useMutation({
    mutationFn: (request: ProjectRevenuePlanCreateRequest) => repository.create(request),
    onSuccess: async (_data, request) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.revenuePlans(request.projectId),
      });
    },
  });
}

export function useCreateProjectAssignmentMutation() {
  const repository = appContainer.resolve(ProjectAssignmentRepository);

  return useMutation({
    mutationFn: (request: ProjectAssignmentCreateRequest) => repository.create(request),
    onSuccess: async (_data, request) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.assignments(request.projectId),
      });
    },
  });
}
