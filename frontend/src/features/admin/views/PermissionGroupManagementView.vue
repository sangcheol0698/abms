<template>
  <section class="space-y-6">
    <header class="space-y-2">
      <p class="text-sm font-medium tracking-[0.18em] text-muted-foreground uppercase">System</p>
      <div class="space-y-1">
        <h1 class="text-3xl font-semibold tracking-tight text-foreground">권한 그룹 관리</h1>
        <p class="max-w-2xl text-sm leading-6 text-muted-foreground">
          최고 관리자가 계정별 권한 그룹을 조회하고 할당할 수 있는 화면입니다. 이번 단계에서는 라우터와
          네비게이션 진입점을 먼저 구성했습니다.
        </p>
      </div>
    </header>

    <div class="grid gap-4 md:grid-cols-[1.4fr_1fr]">
      <section class="rounded-2xl border border-border bg-card p-6 shadow-sm">
        <div class="space-y-3">
          <h2 class="text-lg font-semibold text-foreground">예정 기능</h2>
          <ul class="space-y-2 text-sm text-muted-foreground">
            <li>계정별 현재 권한 그룹 조회</li>
            <li>권한 그룹 추가 및 제거</li>
            <li>권한 그룹별 권한 코드와 scope 확인</li>
          </ul>
        </div>
      </section>

      <aside class="rounded-2xl border border-dashed border-border bg-muted/30 p-6">
        <div class="space-y-2">
          <h2 class="text-sm font-semibold text-foreground">진행 상태</h2>
          <p class="text-sm leading-6 text-muted-foreground">
            메뉴와 라우터 등록이 완료된 상태입니다. 다음 단계에서 실제 데이터 조회와 할당 인터랙션을
            연결합니다.
          </p>
        </div>
      </aside>
    </div>
  </section>
</template>
