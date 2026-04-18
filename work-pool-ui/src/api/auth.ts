import { api } from './client';
import type { ApiResponse, UserProfile } from '../types';

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phoneNumber?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserProfile;
}

export const authApi = {
  register: (data: RegisterRequest) =>
    api.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', data),

  login: (data: LoginRequest) =>
    api.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', data),
};

export const userApi = {
  getMe: (token?: string) =>
    api.get<ApiResponse<UserProfile>>('/api/v1/users/me', token
      ? { headers: { Authorization: `Bearer ${token}` } }
      : undefined),

  getProfile: (userId: string) =>
    api.get<ApiResponse<UserProfile>>(`/api/v1/users/${userId}`),

  updateProfile: (data: Partial<UserProfile>) =>
    api.put<ApiResponse<UserProfile>>('/api/v1/users/me', data),
};
