import axios, {
  type AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
} from 'axios';
import { singleton } from 'tsyringe';
import HttpError from '@/core/http/HttpError';

export type HttpRequestMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface HttpRequestConfig {
  method?: HttpRequestMethod;
  path: string;
  params?: AxiosRequestConfig['params'];
  data?: AxiosRequestConfig['data'];
}

@singleton()
export default class AxiosHttpClient {
  private readonly client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
      timeout: Number(import.meta.env.VITE_API_BASE_TIMEOUT ?? 10000),
      withCredentials: true,
    });

    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        return Promise.reject(new HttpError(error));
      },
    );
  }

  async request<T = unknown>(config: HttpRequestConfig): Promise<T> {
    try {
      const response: AxiosResponse<T> = await this.client.request({
        method: config.method,
        url: config.path,
        params: config.params,
        data: config.data,
      });
      return response.data;
    } catch (error) {
      if (error instanceof HttpError) {
        throw error;
      }
      throw new HttpError(error as AxiosError);
    }
  }

  async download(config: HttpRequestConfig): Promise<Response> {
    try {
      const response = await this.client.request({
        method: config.method ?? 'GET',
        url: config.path,
        params: config.params,
        responseType: 'blob',
      });

      return {
        blob: () => Promise.resolve(response.data),
        headers: {
          get: (name: string) => response.headers?.[name.toLowerCase()] ?? null,
        },
        ok: response.status >= 200 && response.status < 300,
        status: response.status,
        statusText: response.statusText,
      } as Response;
    } catch (error) {
      throw new HttpError(error as AxiosError);
    }
  }

  async upload(
    config: HttpRequestConfig & { onProgress?: (progress: number) => void },
  ): Promise<unknown> {
    try {
      const response = await this.client.request({
        method: config.method ?? 'POST',
        url: config.path,
        data: config.data,
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (event) => {
          if (config.onProgress && event.total) {
            const progress = Math.round((event.loaded / event.total) * 100);
            config.onProgress(progress);
          }
        },
      });

      return response.data;
    } catch (error) {
      throw new HttpError(error as AxiosError);
    }
  }
}
