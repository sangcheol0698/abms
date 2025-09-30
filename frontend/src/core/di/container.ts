import 'reflect-metadata';
import { container } from 'tsyringe';
import AxiosHttpClient from '@/core/http/AxiosHttpClient';
import HttpRepository from '@/core/http/HttpRepository';

/**
 * 전역 DI 컨테이너. 프런트엔드 전역에서 동일 컨테이너를 사용하도록 export 합니다.
 */
export const appContainer = container;

/**
 * DI 컨테이너에 공용 의존성을 등록합니다.
 * 실제 구현은 HTTP 클라이언트, Repository, Store 등에 맞춰 단계적으로 확장합니다.
 */
export function configureContainer() {
  if (!appContainer.isRegistered(AxiosHttpClient)) {
    appContainer.registerSingleton(AxiosHttpClient, AxiosHttpClient);
  }

  if (!appContainer.isRegistered(HttpRepository)) {
    appContainer.registerSingleton(HttpRepository, HttpRepository);
  }
}
