import 'reflect-metadata';
import { beforeEach } from 'vitest';

function createStorageMock() {
  const storage = new Map<string, string>();

  return {
    get length() {
      return storage.size;
    },
    clear() {
      storage.clear();
    },
    getItem(key: string) {
      return storage.get(key) ?? null;
    },
    key(index: number) {
      return Array.from(storage.keys())[index] ?? null;
    },
    removeItem(key: string) {
      storage.delete(key);
    },
    setItem(key: string, value: string) {
      storage.set(key, String(value));
    },
  } satisfies Storage;
}

function ensureStorage(name: 'localStorage' | 'sessionStorage') {
  const target = typeof window !== 'undefined' ? window : globalThis;
  const current = target[name];

  if (
    current &&
    typeof current.getItem === 'function' &&
    typeof current.setItem === 'function' &&
    typeof current.removeItem === 'function' &&
    typeof current.clear === 'function'
  ) {
    return;
  }

  const storageMock = createStorageMock();
  Object.defineProperty(globalThis, name, {
    configurable: true,
    value: storageMock,
  });
  if (typeof window !== 'undefined') {
    Object.defineProperty(window, name, {
      configurable: true,
      value: storageMock,
    });
  }
}

ensureStorage('localStorage');
ensureStorage('sessionStorage');

beforeEach(() => {
  ensureStorage('localStorage');
  ensureStorage('sessionStorage');
});
