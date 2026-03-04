import { describe, expect, it } from 'vitest';
import HttpError from '@/core/http/HttpError';
import { queryClient } from '@/core/query/queryClient';

function createHttpError(status: number): HttpError {
  return new HttpError({
    response: {
      status,
      data: {
        message: `status ${status}`,
      },
    },
  } as any);
}

describe('queryClient retry policy', () => {
  const retry = queryClient.getDefaultOptions().queries?.retry as (
    failureCount: number,
    error: unknown,
  ) => boolean;

  it('retries once for 5xx errors', () => {
    expect(retry(0, createHttpError(500))).toBe(true);
    expect(retry(1, createHttpError(500))).toBe(false);
  });

  it('does not retry for 4xx errors', () => {
    expect(retry(0, createHttpError(400))).toBe(false);
    expect(retry(0, createHttpError(401))).toBe(false);
    expect(retry(0, createHttpError(403))).toBe(false);
  });

  it('does not retry for non-http errors', () => {
    expect(retry(0, new Error('unknown'))).toBe(false);
  });
});
