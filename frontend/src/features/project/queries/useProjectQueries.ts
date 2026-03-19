import { computed, toValue, type MaybeRefOrGetter } from 'vue';
import { keepPreviousData, useMutation, useQuery } from '@tanstack/vue-query';
import { appContainer } from '@/core/di/container';
import { dashboardKeys, partyKeys, projectKeys, queryClient } from '@/core/query';
import ProjectRepository from '@/features/project/repository/ProjectRepository';
import type { ProjectSearchParams } from '@/features/project/models/projectListItem';
import type { ProjectCreateData, ProjectUpdateData } from '@/features/project/models/projectDetail';
import type { ProjectOverviewSummaryParams } from '@/features/project/repository/ProjectRepository';
import ProjectRevenueRepository, {
  type ProjectRevenuePlanCreateRequest,
  type ProjectRevenuePlanUpdatePayload,
} from '@/features/project/repository/ProjectRevenueRepository';
import ProjectAssignmentRepository, {
} from '@/features/project/repository/ProjectAssignmentRepository';
import type {
  ProjectAssignmentCreatePayload,
  ProjectAssignmentEndPayload,
  ProjectAssignmentSearchParams,
  ProjectAssignmentUpdatePayload,
} from '@/features/project/models/projectAssignment';

async function invalidateProjectSideEffects(projectId?: number) {
  const tasks: Promise<unknown>[] = [
    queryClient.invalidateQueries({ queryKey: projectKeys.all }),
    queryClient.invalidateQueries({ queryKey: partyKeys.all }),
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
    placeholderData: keepPreviousData,
  });
}

export function useProjectOverviewSummaryQuery(
  paramsRef: MaybeRefOrGetter<ProjectOverviewSummaryParams>,
) {
  const repository = appContainer.resolve(ProjectRepository);
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() => projectKeys.summary(params.value ?? {})),
    queryFn: () => repository.fetchOverviewSummary(params.value),
    placeholderData: keepPreviousData,
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
  paramsRef: MaybeRefOrGetter<ProjectAssignmentSearchParams>,
) {
  const repository = appContainer.resolve(ProjectAssignmentRepository);
  const projectId = computed(() => Number(toValue(projectIdRef) ?? 0));
  const params = computed(() => toValue(paramsRef));

  return useQuery({
    queryKey: computed(() =>
      projectKeys.assignments(projectId.value, (params.value ?? {}) as unknown as Record<string, unknown>),
    ),
    queryFn: () => repository.findByProjectId(projectId.value, params.value),
    enabled: computed(() => projectId.value > 0),
    placeholderData: keepPreviousData,
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
    onSuccess: async (_updated, variables) => {
      await invalidateProjectSideEffects(variables.projectId);
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
    onSuccess: async (_data, projectId) => {
      await invalidateProjectSideEffects(projectId);
    },
  });
}

export function useCancelProjectMutation() {
  const repository = appContainer.resolve(ProjectRepository);

  return useMutation({
    mutationFn: (projectId: number) => repository.cancel(projectId),
    onSuccess: async (_data, projectId) => {
      await invalidateProjectSideEffects(projectId);
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

export function useUpdateProjectRevenuePlanMutation() {
  const repository = appContainer.resolve(ProjectRevenueRepository);

  return useMutation({
    mutationFn: (variables: {
      projectId: number;
      sequence: number;
      payload: ProjectRevenuePlanUpdatePayload;
    }) => repository.update(variables.projectId, variables.sequence, variables.payload),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.revenuePlans(variables.projectId),
      });
    },
  });
}

export function useIssueProjectRevenuePlanMutation() {
  const repository = appContainer.resolve(ProjectRevenueRepository);

  return useMutation({
    mutationFn: (variables: { projectId: number; sequence: number }) =>
      repository.issue(variables.projectId, variables.sequence),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.revenuePlans(variables.projectId),
      });
    },
  });
}

export function useCancelProjectRevenuePlanMutation() {
  const repository = appContainer.resolve(ProjectRevenueRepository);

  return useMutation({
    mutationFn: (variables: { projectId: number; sequence: number }) =>
      repository.cancel(variables.projectId, variables.sequence),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.revenuePlans(variables.projectId),
      });
    },
  });
}

export function useCreateProjectAssignmentMutation() {
  const repository = appContainer.resolve(ProjectAssignmentRepository);

  return useMutation({
    mutationFn: (request: ProjectAssignmentCreatePayload) => repository.create(request),
    onSuccess: async (_data, request) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.assignmentsRoot(request.projectId),
      });
    },
  });
}

export function useUpdateProjectAssignmentMutation() {
  const repository = appContainer.resolve(ProjectAssignmentRepository);

  return useMutation({
    mutationFn: (variables: { assignmentId: number; payload: ProjectAssignmentUpdatePayload; projectId: number }) =>
      repository.update(variables.assignmentId, variables.payload),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.assignmentsRoot(variables.projectId),
      });
    },
  });
}

export function useEndProjectAssignmentMutation() {
  const repository = appContainer.resolve(ProjectAssignmentRepository);

  return useMutation({
    mutationFn: (variables: { assignmentId: number; payload: ProjectAssignmentEndPayload; projectId: number }) =>
      repository.end(variables.assignmentId, variables.payload),
    onSuccess: async (_data, variables) => {
      await queryClient.invalidateQueries({
        queryKey: projectKeys.assignmentsRoot(variables.projectId),
      });
    },
  });
}
