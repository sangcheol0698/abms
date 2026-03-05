import type { Component } from 'vue';
import { mount, type MountingOptions, type VueWrapper } from '@vue/test-utils';
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query';
import { createMemoryHistory, createRouter, type RouteRecordRaw } from 'vue-router';

export interface RenderOptions {
  route?: string;
  routes?: RouteRecordRaw[];
  queryClient?: QueryClient;
  global?: MountingOptions<any>['global'];
}

interface RenderResult {
  wrapper: VueWrapper<any>;
  router: ReturnType<typeof createRouter>;
  queryClient: QueryClient;
}

const DEFAULT_ROUTES: RouteRecordRaw[] = [
  {
    path: '/:pathMatch(.*)*',
    name: 'catch-all',
    component: { template: '<div />' },
  },
];

export async function renderWithProviders<T extends Component>(
  component: T,
  options: RenderOptions = {},
): Promise<RenderResult> {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: options.routes ?? DEFAULT_ROUTES,
  });

  const queryClient =
    options.queryClient ??
    new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });

  if (options.route) {
    await router.push(options.route);
  } else {
    await router.push('/');
  }
  await router.isReady();

  const wrapper = mount(component as any, {
    global: {
      plugins: [router, [VueQueryPlugin, { queryClient }]],
      ...(options.global ?? {}),
    },
  });

  return {
    wrapper: wrapper as VueWrapper<any>,
    router,
    queryClient,
  };
}
