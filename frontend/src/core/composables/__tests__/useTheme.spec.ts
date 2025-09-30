import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { nextTick } from 'vue';

const STORAGE_KEY = 'abms-theme-preference';

describe('useTheme', () => {
  let systemPrefersDark = false;
  let storage: Map<string, string>;
  let localStorageMock: Storage;

  beforeEach(() => {
    vi.resetModules();
    vi.restoreAllMocks();

    systemPrefersDark = false;
    storage = new Map<string, string>();

    localStorageMock = {
      get length() {
        return storage.size;
      },
      clear: vi.fn(() => storage.clear()),
      getItem: vi.fn((key: string) => (storage.has(key) ? storage.get(key)! : null)),
      key: vi.fn((index: number) => Array.from(storage.keys())[index] ?? null),
      removeItem: vi.fn((key: string) => {
        storage.delete(key);
      }),
      setItem: vi.fn((key: string, value: string) => {
        storage.set(key, value);
      }),
    } as Storage;

    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      value: localStorageMock,
    });

    vi.stubGlobal(
      'matchMedia',
      vi.fn((query: string) => ({
        matches: systemPrefersDark,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    );

    document.documentElement.className = '';
    document.documentElement.removeAttribute('data-theme');
  });

  afterEach(() => {
    vi.unstubAllGlobals?.();
  });

  it('사용자 설정이 없으면 시스템 선호도를 따른다', async () => {
    systemPrefersDark = true;

    const { initializeTheme } = await import('../useTheme');

    initializeTheme();
    await nextTick();

    expect(document.documentElement.classList.contains('dark')).toBe(true);
    expect(document.documentElement.dataset.theme).toBe('system');
    expect(localStorageMock.getItem).toHaveBeenCalledWith(STORAGE_KEY);
  });

  it('저장된 테마가 있으면 해당 값을 적용한다', async () => {
    storage.set(STORAGE_KEY, 'light');

    const { initializeTheme } = await import('../useTheme');

    initializeTheme();
    await nextTick();

    expect(document.documentElement.classList.contains('dark')).toBe(false);
    expect(document.documentElement.dataset.theme).toBe('light');
    expect(localStorageMock.getItem).toHaveBeenCalledWith(STORAGE_KEY);
    expect(localStorageMock.setItem).not.toHaveBeenCalledWith(STORAGE_KEY, 'system');
  });

  it('토글을 호출하면 다크/라이트 테마가 전환된다', async () => {
    storage.set(STORAGE_KEY, 'dark');
    systemPrefersDark = true;

    const { useTheme } = await import('../useTheme');

    const { isDark, toggleTheme } = useTheme();
    await nextTick();

    expect(isDark.value).toBe(true);

    toggleTheme();
    await nextTick();

    expect(isDark.value).toBe(false);
    expect(localStorageMock.setItem).toHaveBeenCalledWith(STORAGE_KEY, 'light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });
});
