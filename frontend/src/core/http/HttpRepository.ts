import { inject, singleton } from 'tsyringe';
import AxiosHttpClient, {
  type HttpRequestConfig,
  type HttpRequestMethod,
} from '@/core/http/AxiosHttpClient';

function withMethod(config: HttpRequestConfig, method: HttpRequestMethod): HttpRequestConfig {
  return { ...config, method };
}

@singleton()
export default class HttpRepository {
  constructor(@inject(AxiosHttpClient) private readonly httpClient: AxiosHttpClient) {}

  get<T = unknown>(config: HttpRequestConfig) {
    return this.httpClient.request<T>(withMethod(config, 'GET'));
  }

  post<T = unknown>(config: HttpRequestConfig) {
    return this.httpClient.request<T>(withMethod(config, 'POST'));
  }

  put<T = unknown>(config: HttpRequestConfig) {
    return this.httpClient.request<T>(withMethod(config, 'PUT'));
  }

  patch<T = unknown>(config: HttpRequestConfig) {
    return this.httpClient.request<T>(withMethod(config, 'PATCH'));
  }

  delete<T = unknown>(config: HttpRequestConfig) {
    return this.httpClient.request<T>(withMethod(config, 'DELETE'));
  }

  download(config: HttpRequestConfig) {
    return this.httpClient.download(withMethod(config, 'GET'));
  }

  upload(config: HttpRequestConfig & { onProgress?: (progress: number) => void }) {
    return this.httpClient.upload(withMethod(config, 'POST'));
  }
}
