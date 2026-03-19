import { computed, ref } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { partyKeys, projectKeys, queryClient } from '@/core/query';
import {
  useCreatePartyMutation,
  useDeletePartyMutation,
  usePartyDetailQuery,
  usePartyListQuery,
  usePartyOverviewSummaryQuery,
  usePartyProjectsQuery,
  useUpdatePartyMutation,
} from '@/features/party/queries/usePartyQueries';

const vueQueryMocks = vi.hoisted(() => ({
  useQueryMock: vi.fn((options) => options),
  useMutationMock: vi.fn((options) => options),
}));

const partyRepositoryMock = {
  list: vi.fn(),
  fetchOverviewSummary: vi.fn(),
  find: vi.fn(),
  fetchProjects: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  delete: vi.fn(),
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
    resolve: () => partyRepositoryMock,
  },
}));

describe('usePartyQueries', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(queryClient, 'invalidateQueries').mockResolvedValue(undefined as never);
  });

  it('목록/요약 query를 저장소와 연결한다', async () => {
    partyRepositoryMock.list.mockResolvedValueOnce('list');
    partyRepositoryMock.fetchOverviewSummary.mockResolvedValueOnce('summary');

    const listQuery = usePartyListQuery(ref({ page: 1, size: 5, name: '협력사' }));
    const summaryQuery = usePartyOverviewSummaryQuery(computed(() => ({ name: '협력사' })));

    expect(listQuery.queryKey.value).toEqual(partyKeys.list({ page: 1, size: 5, name: '협력사' }));
    expect(summaryQuery.queryKey.value).toEqual(partyKeys.summary({ name: '협력사' }));
    await expect(listQuery.queryFn()).resolves.toBe('list');
    await expect(summaryQuery.queryFn()).resolves.toBe('summary');
  });

  it('상세/프로젝트 query는 partyId가 양수일 때만 활성화한다', async () => {
    const disabledDetail = usePartyDetailQuery(ref(0));
    const enabledDetail = usePartyDetailQuery(ref(9));
    const enabledProjects = usePartyProjectsQuery(ref(9), computed(() => ({ page: 1, size: 10 })));

    expect(disabledDetail.enabled.value).toBe(false);
    expect(enabledDetail.enabled.value).toBe(true);
    expect(enabledProjects.enabled.value).toBe(true);
    expect(enabledProjects.queryKey.value).toEqual(partyKeys.projects(9, { page: 1, size: 10 }));
  });

  it('생성/수정/삭제 mutation 성공 시 관련 party/project query를 invalidate한다', async () => {
    const createMutation = useCreatePartyMutation();
    const updateMutation = useUpdatePartyMutation();
    const deleteMutation = useDeletePartyMutation();

    await createMutation.onSuccess?.({ partyId: 4 });
    await updateMutation.onSuccess?.({ partyId: 5 });
    await deleteMutation.onSuccess?.(undefined, 6);

    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({ queryKey: partyKeys.all });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({ queryKey: projectKeys.all });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: partyKeys.detail(4),
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: partyKeys.projectsRoot(4),
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: partyKeys.detail(5),
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({
      queryKey: partyKeys.detail(6),
    });
  });

  it('mutationFn은 각 저장소 메서드를 호출한다', async () => {
    const createMutation = useCreatePartyMutation();
    const updateMutation = useUpdatePartyMutation();
    const deleteMutation = useDeletePartyMutation();

    await createMutation.mutationFn({ name: '신규' } as never);
    await updateMutation.mutationFn({ partyId: 3, data: { name: '수정' } as never });
    await deleteMutation.mutationFn(3);

    expect(partyRepositoryMock.create).toHaveBeenCalledWith({ name: '신규' });
    expect(partyRepositoryMock.update).toHaveBeenCalledWith(3, { name: '수정' });
    expect(partyRepositoryMock.delete).toHaveBeenCalledWith(3);
  });
});
