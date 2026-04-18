import { api } from './client';
import type { ApiResponse, PageResponse, Notification, Transaction, Wallet, Rating, UserRatingSummary } from '../types';

export const notificationApi = {
  getNotifications: (unreadOnly = false, page = 0) =>
    api.get<ApiResponse<PageResponse<Notification>>>('/api/v1/notifications', {
      params: { unreadOnly, page, size: 20 },
    }),

  getUnreadCount: () =>
    api.get<ApiResponse<{ count: number }>>('/api/v1/notifications/unread-count'),

  markAsRead: (id: string) =>
    api.patch(`/api/v1/notifications/${id}/read`),

  markAllRead: () =>
    api.patch('/api/v1/notifications/read-all'),
};

export const paymentApi = {
  createOrder: (data: { taskId: string; finisherId: string; bidId: string; agreedAmount: number }) =>
    api.post<ApiResponse<Transaction>>('/api/v1/payments/orders', data),

  releasePayment: (taskId: string) =>
    api.post<ApiResponse<Transaction>>(`/api/v1/payments/tasks/${taskId}/release`),

  getWallet: () =>
    api.get<ApiResponse<Wallet>>('/api/v1/payments/wallet'),

  getHistory: (asPublisher = true) =>
    api.get<ApiResponse<Transaction[]>>('/api/v1/payments/history', { params: { asPublisher } }),
};

export const ratingApi = {
  submitRating: (data: { taskId: string; ratedUserId: string; stars: number; review?: string }) =>
    api.post<ApiResponse<Rating>>('/api/v1/ratings', data),

  getRatings: (userId: string, page = 0) =>
    api.get<ApiResponse<PageResponse<Rating>>>(`/api/v1/ratings/users/${userId}`, { params: { page } }),

  getSummary: (userId: string) =>
    api.get<ApiResponse<UserRatingSummary>>(`/api/v1/ratings/users/${userId}/summary`),
};
