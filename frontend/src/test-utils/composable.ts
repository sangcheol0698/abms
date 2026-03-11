import { defineComponent, type Component } from 'vue';
import type { RouteRecordRaw } from 'vue-router';
import { renderWithProviders } from './render';

interface MountComposableOptions {
  route?: string;
  routes?: RouteRecordRaw[];
}

export async function mountComposableWithProviders<T>(
  setup: () => T,
  options: MountComposableOptions = {},
) {
  let exposed!: T;

  const Harness = defineComponent({
    setup() {
      exposed = setup();
      return () => null;
    },
  }) as Component;

  const rendered = await renderWithProviders(Harness, options);

  return {
    ...rendered,
    exposed: () => exposed,
  };
}
