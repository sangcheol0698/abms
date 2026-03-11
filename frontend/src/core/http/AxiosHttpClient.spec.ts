import type { AxiosError } from 'axios';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import HttpError from '@/core/http/HttpError';
import AxiosHttpClient from '@/core/http/AxiosHttpClient';

const axiosMocks = vi.hoisted(() => ({
  emitAuthHttpErrorMock: vi.fn(),
  axiosRequestMock: vi.fn(),
  interceptorUseMock: vi.fn(),
}));

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      defaults: {
        baseURL: 'http://localhost:8080',
      },
      interceptors: {
        response: {
          use: axiosMocks.interceptorUseMock,
        },
      },
      request: axiosMocks.axiosRequestMock,
    })),
  },
}));

vi.mock('@/features/auth/http-auth-error', () => ({
  emitAuthHttpError: axiosMocks.emitAuthHttpErrorMock,
}));

describe('AxiosHttpClient', () => {
  let client: AxiosHttpClient;

  beforeEach(() => {
    vi.clearAllMocks();
    client = new AxiosHttpClient();
  });

  it('request는 기본 withCredentials를 true로 보낸다', async () => {
    axiosMocks.axiosRequestMock.mockResolvedValueOnce({ data: { ok: true } });

    await expect(client.request({ method: 'GET', path: '/api/test' })).resolves.toEqual({
      ok: true,
    });

    expect(axiosMocks.axiosRequestMock).toHaveBeenCalledWith({
      method: 'GET',
      url: '/api/test',
      params: undefined,
      data: undefined,
      withCredentials: true,
    });
  });

  it('download는 Response 형태로 변환한다', async () => {
    const blob = new Blob(['file']);
    axiosMocks.axiosRequestMock.mockResolvedValueOnce({
      data: blob,
      headers: { 'content-disposition': 'attachment; filename=test.xlsx' },
      status: 200,
      statusText: 'OK',
    });

    const response = await client.download({ path: '/api/download' });

    expect(response.ok).toBe(true);
    await expect(response.blob()).resolves.toBe(blob);
    expect(response.headers.get('content-disposition')).toBe('attachment; filename=test.xlsx');
  });

  it('upload는 multipart/form-data와 progress 콜백을 사용한다', async () => {
    axiosMocks.axiosRequestMock.mockImplementationOnce(async (config) => {
      config.onUploadProgress?.({ loaded: 25, total: 100 });
      return { data: { ok: true } };
    });

    const onProgress = vi.fn();
    await client.upload({
      path: '/api/upload',
      data: new FormData(),
      onProgress,
    });

    expect(onProgress).toHaveBeenCalledWith(25);
    expect(axiosMocks.axiosRequestMock).toHaveBeenCalledWith(
      expect.objectContaining({
        method: 'POST',
        url: '/api/upload',
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }),
    );
  });

  it('401/403 public auth 요청은 인증 이벤트를 발생시키지 않는다', async () => {
    const handler = axiosMocks.interceptorUseMock.mock.calls[0][1] as (error: AxiosError) => Promise<never>;
    const error = {
      response: { status: 401 },
      config: { url: '/api/auth/login' },
      message: 'unauthorized',
    } as AxiosError;

    await expect(handler(error)).rejects.toBeInstanceOf(HttpError);
    expect(axiosMocks.emitAuthHttpErrorMock).not.toHaveBeenCalled();
  });

  it('401/403 보호된 요청은 인증 이벤트를 발생시킨다', async () => {
    const handler = axiosMocks.interceptorUseMock.mock.calls[0][1] as (error: AxiosError) => Promise<never>;
    const error = {
      response: { status: 403 },
      config: { url: '/api/projects' },
      message: 'forbidden',
    } as AxiosError;

    await expect(handler(error)).rejects.toBeInstanceOf(HttpError);
    expect(axiosMocks.emitAuthHttpErrorMock).toHaveBeenCalledWith({ status: 403 });
  });
});
