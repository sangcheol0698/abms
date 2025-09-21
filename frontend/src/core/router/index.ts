import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    component: () => import('@/core/layouts/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'organization-chart',
        component: () => import('@/features/organization/views/OrganizationView.vue'),
        meta: {
          title: '조직도',
          navGroup: 'main',
        },
      },
      {
        path: 'employees',
        name: 'employees',
        component: () => import('@/features/employee/views/EmployeeListView.vue'),
        meta: {
          title: '구성원',
          navGroup: 'main',
        },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title as string} | ABMS`;
  }
});

export default router;
