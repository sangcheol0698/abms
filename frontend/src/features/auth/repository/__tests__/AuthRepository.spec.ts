import 'reflect-metadata';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type HttpRepository from '@/core/http/HttpRepository';
import AuthRepository from '@/features/auth/repository/AuthRepository';

describe('AuthRepository', () => {
  let httpPost: ReturnType<typeof vi.fn>;
  let httpGet: ReturnType<typeof vi.fn>;
  let repository: AuthRepository;

  beforeEach(() => {
    httpPost = vi.fn().mockResolvedValue(undefined);
    httpGet = vi.fn().mockResolvedValue(undefined);
    repository = new AuthRepository({
      post: httpPost,
      get: httpGet,
    } as unknown as HttpRepository);
  });

  it('로그인 API를 호출한다', async () => {
    await repository.login({
      username: 'user@iabacus.co.kr',
      password: 'Password123!',
    });

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/auth/login',
      data: {
        username: 'user@iabacus.co.kr',
        password: 'Password123!',
      },
    });
  });

  it('회원가입 요청 API를 호출한다', async () => {
    await repository.requestRegistration({
      email: 'user@iabacus.co.kr',
    });

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/auth/registration-requests',
      data: {
        email: 'user@iabacus.co.kr',
      },
    });
  });

  it('회원가입 확정 API를 호출한다', async () => {
    await repository.confirmRegistration({
      token: 'token-value',
      password: 'Password123!',
    });

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/auth/registration-confirmations',
      data: {
        token: 'token-value',
        password: 'Password123!',
      },
    });
  });

  it('로그아웃 API를 호출한다', async () => {
    await repository.logout();

    expect(httpPost).toHaveBeenCalledWith({
      path: '/api/auth/logout',
    });
  });

  it('현재 사용자 정보를 조회한다', async () => {
    httpGet.mockResolvedValueOnce({
      name: '인증사용자',
      email: 'auth-user@abacus.co.kr',
    });

    const result = await repository.fetchMe();

    expect(httpGet).toHaveBeenCalledWith({
      path: '/api/auth/me',
    });
    expect(result).toEqual({
      name: '인증사용자',
      email: 'auth-user@abacus.co.kr',
    });
  });
});
