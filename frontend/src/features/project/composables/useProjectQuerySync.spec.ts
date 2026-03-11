import { flushPromises } from '@vue/test-utils';
import { ref } from 'vue';
import { beforeEach, describe, expect, it } from 'vitest';
import { mountComposableWithProviders } from '@/test-utils';
import { useProjectQuerySync } from '@/features/project/composables/useProjectQuerySync';

describe('useProjectQuerySync', () => {
  beforeEach(() => {
    window.history.replaceState({}, '', '/');
  });

  it('초기 route query를 상태와 검색 파라미터로 복원한다', async () => {
    const page = ref(1);
    const pageSize = ref(10);
    const sorting = ref([]);
    const columnFilters = ref([]);
    const dateRange = ref(null);

    const mounted = await mountComposableWithProviders(
      () => ({
        page,
        pageSize,
        sorting,
        columnFilters,
        dateRange,
        sync: useProjectQuerySync({ page, pageSize, sorting, columnFilters, dateRange }),
      }),
      {
        route:
          '/projects?page=3&size=25&name=ABMS&statuses=IN_PROGRESS,COMPLETED&partyIds=1,2&periodStart=2024-01-01&periodEnd=2024-12-31&sort=periodStart,desc',
        routes: [{ path: '/projects', component: { template: '<div />' } }],
      },
    );

    const state = mounted.exposed();

    expect(state.page.value).toBe(3);
    expect(state.pageSize.value).toBe(25);
    expect(state.sorting.value).toEqual([{ id: 'period', desc: true }]);
    expect(state.columnFilters.value).toEqual([
      { id: 'name', value: 'ABMS' },
      { id: 'status', value: ['IN_PROGRESS', 'COMPLETED'] },
      { id: 'partyId', value: ['1', '2'] },
    ]);
    expect(state.dateRange.value).toEqual({
      start: '2024-01-01',
      end: '2024-12-31',
    });
    expect(state.sync.buildSearchParams()).toEqual({
      page: 3,
      size: 25,
      name: 'ABMS',
      statuses: ['IN_PROGRESS', 'COMPLETED'],
      partyIds: [1, 2],
      periodStart: '2024-01-01',
      periodEnd: '2024-12-31',
      sort: 'periodStart,desc',
    });
  });

  it('상태 변경 시 route query를 갱신하고 필터 변경 시 page를 1로 되돌린다', async () => {
    const page = ref(4);
    const pageSize = ref(10);
    const sorting = ref([]);
    const columnFilters = ref([]);
    const dateRange = ref(null);

    const mounted = await mountComposableWithProviders(
      () => ({
        page,
        pageSize,
        sorting,
        columnFilters,
        dateRange,
        sync: useProjectQuerySync({ page, pageSize, sorting, columnFilters, dateRange }),
      }),
      {
        route: '/projects',
        routes: [{ path: '/projects', component: { template: '<div />' } }],
      },
    );

    const state = mounted.exposed();
    state.columnFilters.value = [
      { id: 'name', value: '  리뉴얼  ' },
      { id: 'status', value: ['IN_PROGRESS'] },
      { id: 'partyId', value: ['7'] },
    ];
    state.sorting.value = [{ id: 'period', desc: false }];
    state.dateRange.value = {
      start: new Date('2024-04-01T00:00:00Z'),
      end: { year: 2024, month: 5, day: 31 },
    };

    await flushPromises();

    expect(state.page.value).toBe(1);
    expect(mounted.router.currentRoute.value.query).toMatchObject({
      page: '1',
      size: '10',
      name: '리뉴얼',
      statuses: 'IN_PROGRESS',
      partyIds: '7',
      periodStart: '2024-04-01',
      periodEnd: '2024-05-31',
      sort: 'periodStart,asc',
    });

    state.pageSize.value = 50;
    await flushPromises();

    expect(state.page.value).toBe(1);
    expect(mounted.router.currentRoute.value.query.size).toBe('50');
  });
});
