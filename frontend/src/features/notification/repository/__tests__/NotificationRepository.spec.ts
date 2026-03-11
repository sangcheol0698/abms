import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type HttpRepository from '@/core/http/HttpRepository';
import NotificationRepository from '@/features/notification/repository/NotificationRepository';

describe('NotificationRepository', () => {
  let httpGet: ReturnType<typeof vi.fn>;
  let httpPost: ReturnType<typeof vi.fn>;
  let httpPatch: ReturnType<typeof vi.fn>;
  let httpDelete: ReturnType<typeof vi.fn>;
  let repository: NotificationRepository;

  beforeEach(() => {
    httpGet = vi.fn();
    httpPost = vi.fn();
    httpPatch = vi.fn();
    httpDelete = vi.fn();
    repository = new NotificationRepository({
      get: httpGet,
      post: httpPost,
      patch: httpPatch,
      delete: httpDelete,
    } as unknown as HttpRepository);
  });

  it('목록 응답이 배열이 아니면 빈 배열을 반환한다', async () => {
    httpGet.mockResolvedValueOnce({ items: [] });

    await expect(repository.fetchAll()).resolves.toEqual([]);
  });

  it('알림 목록 응답을 모델로 매핑한다', async () => {
    httpGet.mockResolvedValueOnce([
      {
        id: 1,
        title: '알림',
        description: null,
        type: 'SUCCESS',
        createdAt: '2024-01-01T00:00:00Z',
        read: false,
        link: null,
      },
    ]);

    await expect(repository.fetchAll()).resolves.toEqual([
      {
        id: 1,
        title: '알림',
        description: undefined,
        type: 'success',
        createdAt: '2024-01-01T00:00:00Z',
        read: false,
        link: undefined,
      },
    ]);
  });

  it('생성 시 API 타입을 대문자로 변환한다', async () => {
    httpPost.mockResolvedValueOnce({
      id: 2,
      title: '생성',
      description: '설명',
      type: 'ERROR',
      createdAt: '2024-01-02T00:00:00Z',
      read: false,
      link: '/projects/1',
    });

    const result = await repository.create({
      title: '생성',
      description: '설명',
      type: 'error',
      link: '/projects/1',
    });

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/notifications',
      data: {
        title: '생성',
        description: '설명',
        type: 'ERROR',
        link: '/projects/1',
      },
    });
    expect(result.type).toBe('error');
  });

  it('markAsRead, markAllAsRead, clearAll을 각각 호출한다', async () => {
    await repository.markAsRead(1);
    await repository.markAllAsRead();
    await repository.clearAll();

    expect(httpPatch).toHaveBeenNthCalledWith(1, {
      path: '/api/notifications/1/read',
    });
    expect(httpPatch).toHaveBeenNthCalledWith(2, {
      path: '/api/notifications/read-all',
    });
    expect(httpDelete).toHaveBeenCalledWith({
      path: '/api/notifications',
    });
  });
});
