import { createRouter, createWebHistory } from 'vue-router';
import SidebarLayout from '@/core/layouts/SidebarLayout.vue';

const routes = [
  {
    path: '/',
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/features/dashboard/views/DashboardView.vue'),
        meta: {
          title: '대시보드',
          layout: SidebarLayout,
          breadcrumbs: [
            {
              title: '대시보드',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'departments',
        name: 'departments',
        component: () => import('@/features/department/views/DepartmentView.vue'),
        meta: {
          title: '부서',
          layout: SidebarLayout,
          padding: 'flush',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '부서',
              disabled: true,
            },
            {
              title: '상세',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'departments/:departmentId',
        name: 'department',
        component: () => import('@/features/department/views/DepartmentView.vue'),
        meta: {
          title: '부서',
          layout: SidebarLayout,
          padding: 'flush',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '부서',
              disabled: true,
            },
            {
              title: '상세',
            },
          ],
        },
      },
      {
        path: 'employees',
        name: 'employees',
        component: () => import('@/features/employee/views/EmployeeListView.vue'),
        meta: {
          title: '직원',
          layout: SidebarLayout,
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '직원',
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
          title: '직원 상세',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '직원',
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
          layout: SidebarLayout,
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
          title: 'AI Assistant',
          layout: SidebarLayout,
          padding: 'flush',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: 'AI Assistant',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'assistant/:sessionId',
        name: 'assistant-session',
        component: () => import('@/features/chat/views/ChatView.vue'),
        meta: {
          title: 'AI Assistant',
          layout: SidebarLayout,
          padding: 'flush',
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: 'AI Assistant',
              to: '/assistant',
            },
            {
              title: '채팅',
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
