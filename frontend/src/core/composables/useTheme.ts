import { computed, ref, watch, type WatchStopHandle } from 'vue';

type ThemePreference = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'abms-theme-preference';
const theme = ref<ThemePreference>('system');
let initialized = false;
let mediaQuery: MediaQueryList | null = null;
let stopWatch: WatchStopHandle | null = null;
let mediaQueryHandler: ((event: MediaQueryListEvent) => void) | null = null;

function isBrowser(): boolean {
  return typeof window !== 'undefined' && typeof document !== 'undefined';
}

function readStoredPreference(): ThemePreference {
  if (!isBrowser()) {
    return 'system';
  }

  const stored = window.localStorage.getItem(STORAGE_KEY);
  if (stored === 'light' || stored === 'dark' || stored === 'system') {
    return stored;
  }

  return 'system';
}

function persistPreference(value: ThemePreference) {
  if (!isBrowser()) {
    return;
  }

  if (value === 'system') {
    window.localStorage.removeItem(STORAGE_KEY);
    return;
  }

  window.localStorage.setItem(STORAGE_KEY, value);
}

function resolveTheme(value: ThemePreference): 'light' | 'dark' {
  if (!isBrowser()) {
    return 'light';
  }

  if (value !== 'system') {
    return value;
  }

  mediaQuery ??= window.matchMedia('(prefers-color-scheme: dark)');
  return mediaQuery.matches ? 'dark' : 'light';
}

function applyTheme(value: ThemePreference) {
  if (!isBrowser()) {
    return;
  }

  const effectiveTheme = resolveTheme(value);
  const root = document.documentElement;

  root.classList.toggle('dark', effectiveTheme === 'dark');
  root.dataset.theme = value;
}

function watchTheme() {
  if (stopWatch) {
    return;
  }

  stopWatch = watch(theme, (next) => {
    persistPreference(next);
    applyTheme(next);
  });
}

function watchSystemPreference() {
  if (!isBrowser()) {
    return;
  }

  mediaQuery ??= window.matchMedia('(prefers-color-scheme: dark)');

  if (!mediaQuery) {
    return;
  }

  if (mediaQueryHandler) {
    return;
  }

  mediaQueryHandler = (event: MediaQueryListEvent) => {
    if (theme.value === 'system') {
      applyTheme(event.matches ? 'dark' : 'light');
    }
  };

  if (typeof mediaQuery.addEventListener === 'function') {
    mediaQuery.addEventListener('change', mediaQueryHandler);
    return;
  }
}

export function initializeTheme() {
  if (!isBrowser() || initialized) {
    applyTheme(theme.value);
    return;
  }

  initialized = true;
  theme.value = readStoredPreference();
  applyTheme(theme.value);
  watchTheme();
  watchSystemPreference();
}

export function useTheme() {
  initializeTheme();

  const resolvedTheme = computed(() => resolveTheme(theme.value));
  const isDark = computed(() => resolvedTheme.value === 'dark');

  function setTheme(next: ThemePreference) {
    theme.value = next;
  }

  function toggleTheme() {
    setTheme(resolvedTheme.value === 'dark' ? 'light' : 'dark');
  }

  return {
    theme,
    resolvedTheme,
    isDark,
    setTheme,
    toggleTheme,
    initializeTheme,
  };
}
