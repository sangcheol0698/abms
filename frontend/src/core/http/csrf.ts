import AxiosHttpClient from '@/core/http/AxiosHttpClient';

const csrfHttpClient = new AxiosHttpClient();

let isCsrfInitialized = false;
let csrfInitializationPromise: Promise<void> | null = null;

export async function ensureCsrfInitialized(): Promise<void> {
  if (isCsrfInitialized) {
    return;
  }

  if (!csrfInitializationPromise) {
    csrfInitializationPromise = csrfHttpClient
      .request<void>({
        method: 'GET',
        path: '/api/csrf',
      })
      .then(() => {
        isCsrfInitialized = true;
      });
  }

  try {
    await csrfInitializationPromise;
  } catch (error) {
    csrfInitializationPromise = null;
    throw error;
  }

  csrfInitializationPromise = null;
}

export function resetCsrfInitialization(): void {
  isCsrfInitialized = false;
  csrfInitializationPromise = null;
}
