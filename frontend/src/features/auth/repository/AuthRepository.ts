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

interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

interface PasswordResetRequestPayload {
  email: string;
}

interface PasswordResetConfirmPayload {
  token: string;
  password: string;
}

export interface AuthMeResponse {
  name: string;
  email: string;
  employeeId: number | null;
  departmentId: number | null;
  permissions: AuthPermissionResponse[];
}

export interface AuthPermissionResponse {
  code: string;
  scopes: string[];
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

  async changePassword(payload: ChangePasswordPayload): Promise<void> {
    await this.httpRepository.patch<void>({
      path: '/api/auth/password',
      data: payload,
    });
  }

  async requestPasswordReset(payload: PasswordResetRequestPayload): Promise<void> {
    await this.httpRepository.post<void>({
      path: '/api/auth/password-reset-requests',
      data: payload,
    });
  }

  async confirmPasswordReset(payload: PasswordResetConfirmPayload): Promise<void> {
    await this.httpRepository.post<void>({
      path: '/api/auth/password-reset-confirmations',
      data: payload,
    });
  }

  async fetchMe(): Promise<AuthMeResponse> {
    return this.httpRepository.get<AuthMeResponse>({
      path: '/api/auth/me',
    });
  }
}
