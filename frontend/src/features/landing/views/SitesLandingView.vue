<template>
  <div class="overflow-hidden bg-background">
    <header
      class="fixed inset-x-0 top-0 z-30 border-b border-white/12 bg-[oklch(0.22_0.012_88/0.82)] text-white backdrop-blur-xl"
    >
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <RouterLink to="/sites" class="flex min-w-0 items-center gap-3" aria-label="ABMS 홈">
          <img :src="logoUrl" alt="" class="h-9 w-9 rounded-md bg-white/95 p-1 shadow-sm" />
          <span class="text-base font-semibold tracking-normal">ABMS</span>
        </RouterLink>

        <nav class="hidden items-center gap-6 text-sm font-medium text-white/78 md:flex">
          <a href="#overview" class="hover:text-white">개요</a>
          <a href="#workflows" class="hover:text-white">운영</a>
          <a href="#security" class="hover:text-white">보안</a>
        </nav>

        <div class="flex items-center gap-2">
          <Button
            as-child
            variant="ghost"
            class="hidden h-9 rounded-md px-3 text-white hover:bg-white/10 hover:text-white sm:inline-flex"
          >
            <RouterLink to="/auths/login">로그인</RouterLink>
          </Button>
          <Button as-child class="h-9 rounded-md bg-white px-4 text-stone-900 hover:bg-white/90">
            <RouterLink to="/auths/login">
              시작하기
              <ArrowRight class="h-4 w-4" />
            </RouterLink>
          </Button>
        </div>
      </div>
    </header>

    <section
      class="relative flex min-h-[calc(100svh-7rem)] items-end overflow-hidden pt-20 text-white"
    >
      <img
        :src="dashboardUrl"
        alt="ABMS 대시보드 화면"
        class="absolute inset-0 h-full w-full object-cover object-[58%_center]"
      />
      <div class="absolute inset-0 bg-[oklch(0.2_0.012_88/0.72)]" />
      <div
        class="absolute inset-0 bg-[linear-gradient(90deg,oklch(0.2_0.012_88/0.94)_0%,oklch(0.2_0.012_88/0.78)_42%,oklch(0.2_0.012_88/0.2)_100%)]"
      />

      <div class="relative mx-auto grid w-full max-w-7xl gap-8 px-4 pb-10 sm:px-6 lg:px-8">
        <div class="max-w-3xl">
          <p class="mb-5 inline-flex rounded-md border border-white/20 bg-white/10 px-3 py-1 text-sm font-medium text-white/88 backdrop-blur">
            프로젝트, 인력, 매출 운영 시스템
          </p>
          <h1 class="max-w-3xl text-4xl font-semibold leading-tight tracking-normal sm:text-5xl lg:text-6xl">
            ABMS
          </h1>
          <p class="mt-5 max-w-2xl text-lg leading-8 text-white/82 sm:text-xl">
            조직의 계약, 투입 인력, 월별 매출 흐름을 같은 기준으로 정리해 의사결정과
            보고 시간을 줄입니다.
          </p>
          <div class="mt-8 flex flex-col gap-3 sm:flex-row">
            <Button as-child size="lg" class="h-11 rounded-md bg-white px-5 text-stone-900 hover:bg-white/90">
              <RouterLink to="/auths/login">
                업무 화면으로 이동
                <ArrowRight class="h-4 w-4" />
              </RouterLink>
            </Button>
            <Button
              as-child
              size="lg"
              variant="ghost"
              class="h-11 rounded-md border border-white/24 bg-white/8 px-5 text-white hover:bg-white/14 hover:text-white"
            >
              <a href="#overview">제품 보기</a>
            </Button>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-3">
          <div
            v-for="metric in heroMetrics"
            :key="metric.label"
            class="rounded-md border border-white/16 bg-white/10 p-4 backdrop-blur-md"
          >
            <p class="text-2xl font-semibold tracking-normal">{{ metric.value }}</p>
            <p class="mt-1 text-sm leading-6 text-white/72">{{ metric.label }}</p>
          </div>
        </div>
      </div>
    </section>

    <section id="overview" class="border-b border-border bg-background py-16 sm:py-20">
      <div class="mx-auto grid max-w-7xl gap-10 px-4 sm:px-6 lg:grid-cols-[0.85fr_1.15fr] lg:px-8">
        <div>
          <p class="text-sm font-semibold text-primary">운영 개요</p>
          <h2 class="mt-3 text-3xl font-semibold tracking-normal text-foreground sm:text-4xl">
            흩어진 데이터를 운영 기준으로 정렬합니다
          </h2>
          <p class="mt-4 text-base leading-7 text-muted-foreground">
            부서, 직원, 프로젝트, 협력사 정보를 서로 연결해 현재 상태와 재무 흐름을
            빠르게 확인할 수 있습니다.
          </p>
        </div>

        <div class="grid gap-4 sm:grid-cols-2">
          <article
            v-for="item in overviewItems"
            :key="item.title"
            class="rounded-md border border-border bg-card p-5 shadow-sm"
          >
            <component :is="item.icon" class="h-5 w-5 text-primary" />
            <h3 class="mt-4 text-base font-semibold text-card-foreground">{{ item.title }}</h3>
            <p class="mt-2 text-sm leading-6 text-muted-foreground">{{ item.description }}</p>
          </article>
        </div>
      </div>
    </section>

    <section id="workflows" class="bg-secondary/40 py-16 sm:py-20">
      <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
          <div class="max-w-2xl">
            <p class="text-sm font-semibold text-primary">업무 흐름</p>
            <h2 class="mt-3 text-3xl font-semibold tracking-normal text-foreground sm:text-4xl">
              보고까지 이어지는 일상 업무를 한 곳에서 처리합니다
            </h2>
          </div>
          <Button as-child variant="field" class="h-10 rounded-md">
            <RouterLink to="/auths/login">
              데모 계정으로 보기
              <ExternalLink class="h-4 w-4" />
            </RouterLink>
          </Button>
        </div>

        <div class="mt-10 grid gap-4 lg:grid-cols-3">
          <article
            v-for="step in workflowItems"
            :key="step.title"
            class="rounded-md border border-border bg-background p-5 shadow-sm"
          >
            <div class="flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary">
              <component :is="step.icon" class="h-5 w-5" />
            </div>
            <h3 class="mt-5 text-lg font-semibold text-foreground">{{ step.title }}</h3>
            <p class="mt-2 text-sm leading-6 text-muted-foreground">{{ step.description }}</p>
          </article>
        </div>
      </div>
    </section>

    <section id="security" class="border-y border-border bg-background py-16 sm:py-20">
      <div class="mx-auto grid max-w-7xl gap-8 px-4 sm:px-6 lg:grid-cols-[1fr_1fr] lg:px-8">
        <div>
          <p class="text-sm font-semibold text-primary">보안과 권한</p>
          <h2 class="mt-3 text-3xl font-semibold tracking-normal text-foreground sm:text-4xl">
            역할에 맞는 화면과 작업만 열립니다
          </h2>
        </div>
        <div class="grid gap-3">
          <div
            v-for="policy in securityItems"
            :key="policy.title"
            class="flex gap-4 rounded-md border border-border bg-card p-4"
          >
            <CheckCircle2 class="mt-0.5 h-5 w-5 shrink-0 text-status-success" />
            <div>
              <h3 class="text-sm font-semibold text-card-foreground">{{ policy.title }}</h3>
              <p class="mt-1 text-sm leading-6 text-muted-foreground">{{ policy.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <footer class="bg-[oklch(0.22_0.012_88)] py-8 text-white">
      <div class="mx-auto flex max-w-7xl flex-col gap-4 px-4 sm:px-6 md:flex-row md:items-center md:justify-between lg:px-8">
        <div class="flex items-center gap-3">
          <img :src="logoUrl" alt="" class="h-8 w-8 rounded-md bg-white/95 p-1" />
          <span class="text-sm font-semibold">ABMS</span>
        </div>
        <p class="text-sm text-white/66">Project, people and revenue operations.</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
import {
  ArrowRight,
  BarChart3,
  BriefcaseBusiness,
  Building2,
  CheckCircle2,
  ExternalLink,
  FileText,
  Handshake,
  ShieldCheck,
  Users,
} from 'lucide-vue-next';
import { Button } from '@/components/ui/button';
import logoUrl from '@/assets/main_logo.png';
import dashboardUrl from '@/assets/landing/dashboard.png';

const heroMetrics = [
  {
    value: '360°',
    label: '부서와 프로젝트 운영 현황',
  },
  {
    value: '월별',
    label: '매출 계획과 실적 추적',
  },
  {
    value: '권한',
    label: '역할 기반 접근 제어',
  },
];

const overviewItems = [
  {
    title: '프로젝트 포트폴리오',
    description: '진행 상태, 기간, 고객사, 투입 인력을 같은 화면에서 확인합니다.',
    icon: BriefcaseBusiness,
  },
  {
    title: '직원 운영',
    description: '소속, 직책, 급여 이력, 프로젝트 배정 정보를 연결해 관리합니다.',
    icon: Users,
  },
  {
    title: '협력사 관리',
    description: '계약과 프로젝트의 외부 파트너 정보를 운영 데이터와 함께 봅니다.',
    icon: Handshake,
  },
  {
    title: '매출 대시보드',
    description: '월별 흐름과 부서별 성과를 기준으로 재무 현황을 점검합니다.',
    icon: BarChart3,
  },
];

const workflowItems = [
  {
    title: '조직 정보 정비',
    description: '부서 구조와 구성원 정보를 최신 상태로 유지해 운영 기준을 맞춥니다.',
    icon: Building2,
  },
  {
    title: '프로젝트 배정',
    description: '투입 인력과 계약 정보를 연결하고 변경 이력을 남깁니다.',
    icon: ShieldCheck,
  },
  {
    title: '주간 보고',
    description: '프로젝트와 매출 데이터를 바탕으로 보고서 작성 흐름을 정리합니다.',
    icon: FileText,
  },
];

const securityItems = [
  {
    title: '권한 그룹 기반 메뉴 제어',
    description: '사용자 역할에 따라 직원, 프로젝트, 보고서, 시스템 관리 화면 접근을 제한합니다.',
  },
  {
    title: '인증 세션 검증',
    description: '주요 화면 진입 시 서버 세션과 권한 상태를 확인합니다.',
  },
  {
    title: '운영 데이터 변경 추적',
    description: '인력 배정, 급여, 매출 계획처럼 민감한 변경 지점을 업무 단위로 분리합니다.',
  },
];
</script>
