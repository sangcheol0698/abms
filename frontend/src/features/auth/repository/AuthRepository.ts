import { inject, singleton } from 'tsyringe';
import HttpRepository from '@/core/http/HttpRepository';

interface LoginPayload {
  username: string;
  password: string;
}

interface RegistrationRequestPayload {
  email: string;
}

interface RegistrationConfirmPayload {
  token: string;
  password: string;
}

export interface AuthMeResponse {
  name: string;
  email: string;
}

@singleton()
export default class AuthRepository {
  constructor(@inject(HttpRepository) private readonly httpRepository: HttpRepository) {}

  async login(payload: LoginPayload): Promise<void> {
    await this.httpRepository.post<void>({
      path: '/api/auth/login',
      data: payload,
    });
  }

  async requestRegistration(payload: RegistrationRequestPayload): Promise<void> {
    await this.httpRepository.post<void>({
      path: '/api/auth/registration-requests',
      data: payload,
    });
  }

  async confirmRegistration(payload: RegistrationConfirmPayload): Promise<void> {
    await this.httpRepository.post<void>({
      path: '/api/auth/registration-confirmations',
      data: payload,
    });
  }

  async logout(): Promise<void> {
    await this.httpRepository.post<void>({
      path: '/api/auth/logout',
    });
  }

  async fetchMe(): Promise<AuthMeResponse> {
    return this.httpRepository.get<AuthMeResponse>({
      path: '/api/auth/me',
    });
  }
}
