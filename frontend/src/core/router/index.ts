import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    component: () => import('@/core/layouts/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/features/dashboard/views/DashboardView.vue'),
        meta: {
          title: '대시보드',
          breadcrumbs: [
            {
              title: '대시보드',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'organization',
        name: 'organization',
        component: () => import('@/features/organization/views/OrganizationView.vue'),
        meta: {
          title: '조직도',
          padding: 'flush',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '조직도',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'employees',
        name: 'employees',
        component: () => import('@/features/employee/views/EmployeeListView.vue'),
        meta: {
          title: '구성원',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '구성원',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'employees/:employeeId',
        name: 'employee-detail',
        component: () => import('@/features/employee/views/EmployeeDetailView.vue'),
        meta: {
          title: '구성원 상세',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '구성원',
              to: '/employees',
            },
            {
              title: '상세',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'projects',
        name: 'projects',
        component: () => import('@/features/project/views/ProjectListView.vue'),
        meta: {
          title: '프로젝트',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '프로젝트',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'projects/:projectId',
        name: 'project-detail',
        component: () => import('@/features/project/views/ProjectDetailView.vue'),
        meta: {
          title: '프로젝트 상세',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '프로젝트',
              to: '/projects',
            },
            {
              title: '프로젝트 상세',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'assistant',
        name: 'assistant',
        component: () => import('@/features/chat/views/ChatView.vue'),
        meta: {
          title: 'AI Copilot',
          padding: 'flush',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: 'AI Copilot',
              disabled: true,
            },
          ],
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
