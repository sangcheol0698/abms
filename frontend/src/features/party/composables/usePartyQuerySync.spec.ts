import { flushPromises } from '@vue/test-utils';
import { ref } from 'vue';
import { beforeEach, describe, expect, it } from 'vitest';
import { mountComposableWithProviders } from '@/test-utils';
import { usePartyQuerySync } from '@/features/party/composables/usePartyQuerySync';

describe('usePartyQuerySync', () => {
  beforeEach(() => {
    window.history.replaceState({}, '', '/');
  });

  it('초기 route query를 상태로 복원한다', async () => {
    const page = ref(1);
    const pageSize = ref(10);
    const sorting = ref([]);
    const columnFilters = ref([]);

    const mounted = await mountComposableWithProviders(
      () => ({
        page,
        pageSize,
        sorting,
        columnFilters,
        sync: usePartyQuerySync({ page, pageSize, sorting, columnFilters }),
      }),
      {
        route: '/parties?page=2&size=15&name=협력사&sort=name,desc',
        routes: [{ path: '/parties', component: { template: '<div />' } }],
      },
    );

    const state = mounted.exposed();

    expect(state.page.value).toBe(2);
    expect(state.pageSize.value).toBe(15);
    expect(state.sorting.value).toEqual([{ id: 'name', desc: true }]);
    expect(state.columnFilters.value).toEqual([{ id: 'name', value: '협력사' }]);
    expect(state.sync.buildSearchParams()).toEqual({
      page: 2,
      size: 15,
      name: '협력사',
      sort: 'name,desc',
    });
  });

  it('검색/정렬/페이지 크기 변경 시 route query를 갱신한다', async () => {
    const page = ref(3);
    const pageSize = ref(10);
    const sorting = ref([]);
    const columnFilters = ref([]);

    const mounted = await mountComposableWithProviders(
      () => ({
        page,
        pageSize,
        sorting,
        columnFilters,
        sync: usePartyQuerySync({ page, pageSize, sorting, columnFilters }),
      }),
      {
        route: '/parties',
        routes: [{ path: '/parties', component: { template: '<div />' } }],
      },
    );

    const state = mounted.exposed();
    state.columnFilters.value = [{ id: 'name', value: '  ABMS  ' }];
    state.sorting.value = [{ id: 'name', desc: false }];

    await flushPromises();

    expect(state.page.value).toBe(1);
    expect(mounted.router.currentRoute.value.query).toMatchObject({
      page: '1',
      size: '10',
      name: 'ABMS',
      sort: 'name,asc',
    });

    state.pageSize.value = 20;
    await flushPromises();

    expect(mounted.router.currentRoute.value.query.size).toBe('20');
  });
});
