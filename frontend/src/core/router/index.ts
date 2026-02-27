import { createRouter, createWebHistory } from 'vue-router';
import type { RouteLocationRaw } from 'vue-router';
import AuthLayout from '@/core/layouts/AuthLayout.vue';
import SidebarLayout from '@/core/layouts/SidebarLayout.vue';
import { ensureServerSessionValid, hasStoredUser } from '@/features/auth/session';

const routes = [
  {
    path: '/auths',
    children: [
      {
        path: 'login',
        name: 'auth-login',
        component: () => import('@/features/auth/views/AuthLoginView.vue'),
        meta: {
          title: '로그인',
          layout: AuthLayout,
        },
      },
      {
        path: 'register',
        name: 'auth-register',
        component: () => import('@/features/auth/views/AuthRegisterView.vue'),
        meta: {
          title: '회원가입 요청',
          layout: AuthLayout,
        },
      },
      {
        path: 'registration-confirm',
        name: 'auth-registration-confirm',
        component: () => import('@/features/auth/views/AuthRegistrationConfirmView.vue'),
        meta: {
          title: '회원가입 확정',
          layout: AuthLayout,
        },
      },
      {
        path: 'session-expired',
        name: 'auth-session-expired',
        component: () => import('@/features/auth/views/AuthSessionExpiredView.vue'),
        meta: {
          title: '세션 만료',
          layout: AuthLayout,
        },
      },
      {
        path: 'forbidden',
        name: 'auth-forbidden',
        component: () => import('@/features/auth/views/AuthForbiddenView.vue'),
        meta: {
          title: '접근 권한 없음',
          layout: AuthLayout,
        },
      },
    ],
  },
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
          layout: SidebarLayout,
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
              title: '직원 상세',
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
          layout: SidebarLayout,
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
        path: 'parties',
        name: 'parties',
        component: () => import('@/features/party/views/PartyListView.vue'),
        meta: {
          title: '협력사',
          layout: SidebarLayout,
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '협력사',
              disabled: true,
            },
          ],
        },
      },
      {
        path: 'parties/:partyId',
        name: 'party-detail',
        component: () => import('@/features/party/views/PartyDetailView.vue'),
        meta: {
          title: '협력사 상세',
          layout: SidebarLayout,
          breadcrumbs: [
            {
              title: '대시보드',
              to: '/',
            },
            {
              title: '협력사',
              to: '/parties',
            },
            {
              title: '협력사 상세',
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

function isAuthRoute(path: string): boolean {
  return path.startsWith('/auths');
}

function resolveLoginRedirect(fullPath: string): RouteLocationRaw {
  return {
    name: 'auth-login',
    query: { redirect: fullPath },
  };
}

const AUTH_ROUTE_WHITELIST = new Set(['auth-registration-confirm', 'auth-session-expired', 'auth-forbidden']);

router.beforeEach(async (to) => {
  const isLoggedIn = hasStoredUser();
  const authPage = isAuthRoute(to.path);
  const isWhitelistedAuthPage =
    typeof to.name === 'string' && AUTH_ROUTE_WHITELIST.has(to.name);

  if (authPage) {
    if (!isLoggedIn || isWhitelistedAuthPage) {
      return true;
    }
    const sessionValid = await ensureServerSessionValid();
    if (!sessionValid) {
      return true;
    }
    return { path: '/' };
  }

  if (!isLoggedIn) {
    return resolveLoginRedirect(to.fullPath);
  }

  const sessionValid = await ensureServerSessionValid();
  if (!sessionValid) {
    return resolveLoginRedirect(to.fullPath);
  }

  return true;
});

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title as string} | ABMS`;
  }
});

export default router;
