import { computed, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { dashboardKeys, partyKeys, projectKeys, queryClient } from '@/core/query';
import {
  useCancelProjectMutation,
  useCompleteProjectMutation,
  useCreateProjectAssignmentMutation,
  useCreateProjectMutation,
  useCreateProjectRevenuePlanMutation,
  useDeleteProjectMutation,
  useProjectAssignmentsQuery,
  useProjectDetailQuery,
  useProjectListQuery,
  useProjectOverviewSummaryQuery,
  useProjectRevenuePlansQuery,
  useProjectStatusesQuery,
  useUpdateProjectMutation,
} from '@/features/project/queries/useProjectQueries';

const vueQueryMocks = vi.hoisted(() => ({
  useQueryMock: vi.fn((options) => options),
  useMutationMock: vi.fn((options) => options),
}));

const projectRepositoryMock = {
  search: vi.fn(),
  fetchOverviewSummary: vi.fn(),
  find: vi.fn(),
  fetchStatuses: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  delete: vi.fn(),
  complete: vi.fn(),
  cancel: vi.fn(),
};

const revenueRepositoryMock = {
  findByProjectId: vi.fn(),
  create: vi.fn(),
};

const assignmentRepositoryMock = {
  findByProjectId: vi.fn(),
  create: vi.fn(),
};

vi.mock('@tanstack/vue-query', async () => {
  const actual = await vi.importActual<typeof import('@tanstack/vue-query')>('@tanstack/vue-query');
  return {
    ...actual,
    keepPreviousData: Symbol('keepPreviousData'),
    useQuery: vueQueryMocks.useQueryMock,
    useMutation: vueQueryMocks.useMutationMock,
  };
});

vi.mock('@/core/di/container', () => ({
  appContainer: {
    resolve: (token: { name?: string }) => {
      switch (token?.name) {
        case 'ProjectRevenueRepository':
          return revenueRepositoryMock;
        case 'ProjectAssignmentRepository':
          return assignmentRepositoryMock;
        default:
          return projectRepositoryMock;
      }
    },
  },
}));

describe('useProjectQueries', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined as never);
  });

  it('목록 query key와 queryFn을 프로젝트 검색 파라미터에 연결한다', async () => {
    projectRepositoryMock.search.mockResolvedValueOnce('result');
    const params = ref({ page: 2, size: 20, name: 'ABMS' });

    const query = useProjectListQuery(params);

    expect(query.queryKey.value).toEqual(projectKeys.list({ page: 2, size: 20, name: 'ABMS' }));
    await expect(query.queryFn()).resolves.toBe('result');
    expect(projectRepositoryMock.search).toHaveBeenCalledWith({
      page: 2,
      size: 20,
      name: 'ABMS',
    });
  });

  it('요약 query를 요약 저장소와 연결한다', async () => {
    projectRepositoryMock.fetchOverviewSummary.mockResolvedValueOnce('summary');

    const query = useProjectOverviewSummaryQuery(computed(() => ({ name: 'A', statuses: ['IN_PROGRESS'] })));

    expect(query.queryKey.value).toEqual(
      projectKeys.summary({ name: 'A', statuses: ['IN_PROGRESS'] }),
    );
    await expect(query.queryFn()).resolves.toBe('summary');
  });

  it('상세 query는 projectId가 0 이하일 때 비활성화한다', () => {
    const disabledQuery = useProjectDetailQuery(ref(0));
    const enabledQuery = useProjectDetailQuery(ref(3));

    expect(disabledQuery.enabled.value).toBe(false);
    expect(enabledQuery.enabled.value).toBe(true);
    expect(enabledQuery.queryKey.value).toEqual(projectKeys.detail(3));
  });

  it('상태, 매출 계획, 배정 query를 각각 저장소에 연결한다', async () => {
    projectRepositoryMock.fetchStatuses.mockResolvedValueOnce([{ value: 'A', label: 'A' }]);
    revenueRepositoryMock.findByProjectId.mockResolvedValueOnce([]);
    assignmentRepositoryMock.findByProjectId.mockResolvedValueOnce([]);

    const statusesQuery = useProjectStatusesQuery();
    const revenuePlansQuery = useProjectRevenuePlansQuery(ref(5));
    const assignmentsQuery = useProjectAssignmentsQuery(ref(5));

    expect(statusesQuery.queryKey).toEqual(projectKeys.statuses());
    expect(revenuePlansQuery.queryKey.value).toEqual(projectKeys.revenuePlans(5));
    expect(assignmentsQuery.queryKey.value).toEqual(projectKeys.assignments(5));
    expect(revenuePlansQuery.enabled.value).toBe(true);
    expect(assignmentsQuery.enabled.value).toBe(true);

    await statusesQuery.queryFn();
    await revenuePlansQuery.queryFn();
    await assignmentsQuery.queryFn();

    expect(projectRepositoryMock.fetchStatuses).toHaveBeenCalled();
    expect(revenueRepositoryMock.findByProjectId).toHaveBeenCalledWith(5);
    expect(assignmentRepositoryMock.findByProjectId).toHaveBeenCalledWith(5);
  });

  it('생성 mutation 성공 시 project, party, dashboard와 상세 query를 invalidate한다', async () => {
    const mutation = useCreateProjectMutation();

    await mutation.onSuccess?.({ projectId: 7 });

    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({ queryKey: projectKeys.all });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({ queryKey: partyKeys.all });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: dashboardKeys.summary(),
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: projectKeys.detail(7),
    });
  });

  it('수정, 삭제, 완료, 취소 mutation은 해당 저장소와 invalidate를 연결한다', async () => {
    const updateMutation = useUpdateProjectMutation();
    const deleteMutation = useDeleteProjectMutation();
    const completeMutation = useCompleteProjectMutation();
    const cancelMutation = useCancelProjectMutation();

    await updateMutation.mutationFn({
      projectId: 10,
      data: {
        partyId: 1,
        leadDepartmentId: null,
        name: '수정',
        description: '',
        status: 'IN_PROGRESS',
        contractAmount: 10,
        startDate: '2024-01-01',
        endDate: null,
      },
    });
    await deleteMutation.mutationFn(10);
    await completeMutation.mutationFn(10);
    await cancelMutation.mutationFn(10);
    await updateMutation.onSuccess?.({ projectId: 10 }, { projectId: 10, data: {} as never });
    await deleteMutation.onSuccess?.(undefined, 10);
    await completeMutation.onSuccess?.(undefined, 10);
    await cancelMutation.onSuccess?.(undefined, 10);

    expect(projectRepositoryMock.update).toHaveBeenCalledWith(10, expect.any(Object));
    expect(projectRepositoryMock.delete).toHaveBeenCalledWith(10);
    expect(projectRepositoryMock.complete).toHaveBeenCalledWith(10);
    expect(projectRepositoryMock.cancel).toHaveBeenCalledWith(10);
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: projectKeys.detail(10),
    });
  });

  it('매출 계획/배정 생성 mutation 성공 시 각 상세 query만 invalidate한다', async () => {
    const createRevenuePlanMutation = useCreateProjectRevenuePlanMutation();
    const createAssignmentMutation = useCreateProjectAssignmentMutation();

    await createRevenuePlanMutation.mutationFn({ projectId: 3 } as never);
    await createAssignmentMutation.mutationFn({ projectId: 4 } as never);
    await createRevenuePlanMutation.onSuccess?.(undefined, { projectId: 3 } as never);
    await createAssignmentMutation.onSuccess?.(undefined, { projectId: 4 } as never);

    expect(revenueRepositoryMock.create).toHaveBeenCalledWith({ projectId: 3 });
    expect(assignmentRepositoryMock.create).toHaveBeenCalledWith({ projectId: 4 });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: projectKeys.revenuePlans(3),
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: projectKeys.assignments(4),
    });
  });
});
