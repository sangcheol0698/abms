import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import AxiosHttpClient from '@/core/http/AxiosHttpClient';
import HttpRepository from '@/core/http/HttpRepository';

describe('HttpRepository', () => {
  let request: ReturnType<typeof vi.fn>;
  let download: ReturnType<typeof vi.fn>;
  let upload: ReturnType<typeof vi.fn>;
  let repository: HttpRepository;

  beforeEach(() => {
    request = vi.fn();
    download = vi.fn();
    upload = vi.fn();
    repository = new HttpRepository({
      request,
      download,
      upload,
    } as unknown as AxiosHttpClient);
  });

  it('HTTP method별로 request에 method를 주입한다', () => {
    repository.get({ path: '/get' });
    repository.post({ path: '/post' });
    repository.put({ path: '/put' });
    repository.patch({ path: '/patch' });
    repository.delete({ path: '/delete' });

    expect(request).toHaveBeenNthCalledWith(1, { path: '/get', method: 'GET' });
    expect(request).toHaveBeenNthCalledWith(2, { path: '/post', method: 'POST' });
    expect(request).toHaveBeenNthCalledWith(3, { path: '/put', method: 'PUT' });
    expect(request).toHaveBeenNthCalledWith(4, { path: '/patch', method: 'PATCH' });
    expect(request).toHaveBeenNthCalledWith(5, { path: '/delete', method: 'DELETE' });
  });

  it('download와 upload에도 method를 주입한다', () => {
    const onProgress = vi.fn();
    repository.download({ path: '/download' });
    repository.upload({ path: '/upload', onProgress });

    expect(download).toHaveBeenCalledWith({ path: '/download', method: 'GET' });
    expect(upload).toHaveBeenCalledWith({
      path: '/upload',
      onProgress,
      method: 'POST',
    });
  });
});
