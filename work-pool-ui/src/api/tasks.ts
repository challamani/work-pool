import { api } from './client';
import type { ApiResponse, PageResponse, Task, Bid } from '../types';

export interface CreateTaskRequest {
  title: string;
  description: string;
  category: string;
  requiredSkills: string[];
  city: string;
  district: string;
  state: string;
  pincode?: string;
  latitude?: number;
  longitude?: number;
  budgetMin: number;
  budgetMax: number;
  scheduledStart?: string;
  scheduledEnd?: string;
  tags?: string[];
}

export const taskApi = {
  getOpenTasks: (state?: string, page = 0, size = 20) =>
    api.get<ApiResponse<PageResponse<Task>>>('/api/v1/tasks', {
      params: { state, page, size },
    }),

  getTask: (id: string) =>
    api.get<ApiResponse<Task>>(`/api/v1/tasks/${id}`),

  createTask: (data: CreateTaskRequest) =>
    api.post<ApiResponse<Task>>('/api/v1/tasks', data),

  getMyPublished: (page = 0) =>
    api.get<ApiResponse<PageResponse<Task>>>('/api/v1/tasks/my/published', { params: { page } }),

  getMyAssigned: (page = 0) =>
    api.get<ApiResponse<PageResponse<Task>>>('/api/v1/tasks/my/assigned', { params: { page } }),

  placeBid: (taskId: string, data: { proposedAmount: number; coverNote?: string; estimatedDurationHours: number }) =>
    api.post<ApiResponse<Bid>>(`/api/v1/tasks/${taskId}/bids`, data),

  getBids: (taskId: string) =>
    api.get<ApiResponse<Bid[]>>(`/api/v1/tasks/${taskId}/bids`),

  acceptBid: (taskId: string, bidId: string) =>
    api.post<ApiResponse<Bid>>(`/api/v1/tasks/${taskId}/bids/${bidId}/accept`),

  markComplete: (taskId: string, proofUrl?: string) =>
    api.post<ApiResponse<Task>>(`/api/v1/tasks/${taskId}/complete`, null, { params: { proofUrl } }),

  confirmCompletion: (taskId: string) =>
    api.post<ApiResponse<Task>>(`/api/v1/tasks/${taskId}/confirm`),

  sendMessage: (taskId: string, recipientUserId: string, message: string) =>
    api.post<ApiResponse<null>>(`/api/v1/tasks/${taskId}/messages`, { recipientUserId, message }),
};
