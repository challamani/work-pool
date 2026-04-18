import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '../api/other';
import { useNotificationStore } from '../store/notificationStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Bell, CheckCheck } from 'lucide-react';

const typeColors: Record<string, string> = {
  TASK_MATCHED: 'bg-blue-500',
  BID_RECEIVED: 'bg-green-500',
  BID_ACCEPTED: 'bg-purple-500',
  BID_REJECTED: 'bg-red-500',
  PAYMENT_HELD: 'bg-yellow-500',
  PAYMENT_RELEASED: 'bg-green-600',
  TASK_COMPLETED: 'bg-blue-600',
  RATING_RECEIVED: 'bg-yellow-400',
  SYSTEM_ALERT: 'bg-gray-500',
};

const NotificationsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { clearUnread } = useNotificationStore();

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationApi.getNotifications(false, 0),
  });

  const markAllMutation = useMutation({
    mutationFn: notificationApi.markAllRead,
    onSuccess: () => {
      clearUnread();
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const markOneMutation = useMutation({
    mutationFn: (id: string) => notificationApi.markAsRead(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] }),
  });

  const notifications = data?.data?.data?.content ?? [];

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
        {notifications.some((n) => !n.read) && (
          <button
            onClick={() => markAllMutation.mutate()}
            className="btn-secondary text-sm py-1.5 px-3 flex items-center gap-1"
          >
            <CheckCheck className="w-4 h-4" /> Mark all read
          </button>
        )}
      </div>

      {isLoading ? (
        <LoadingSpinner className="py-12" />
      ) : notifications.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          <Bell className="w-12 h-12 mx-auto mb-3 text-gray-300" />
          <p>No notifications yet</p>
        </div>
      ) : (
        <div className="space-y-2">
          {notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.read && markOneMutation.mutate(n.id)}
              className={`card p-4 flex items-start gap-3 cursor-pointer hover:shadow-md transition-shadow ${!n.read ? 'border-blue-200 bg-blue-50/30' : ''}`}
            >
              <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${typeColors[n.type] || 'bg-gray-400'}`} />
              <div className="flex-1 min-w-0">
                <p className="font-medium text-gray-900 text-sm">{n.title}</p>
                <p className="text-sm text-gray-600 mt-0.5">{n.message}</p>
                <p className="text-xs text-gray-400 mt-1">
                  {new Date(n.createdAt).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' })}
                </p>
              </div>
              {!n.read && <span className="w-2 h-2 rounded-full bg-blue-500 flex-shrink-0 mt-2" />}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
