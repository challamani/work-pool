import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '../api/other';
import { useNotificationStore } from '../store/notificationStore';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { Bell, CheckCheck } from 'lucide-react';

const typeColors: Record<string, string> = {
  TASK_MATCHED:     'bg-gradient-to-br from-ocean-500 to-cyan-400',
  BID_RECEIVED:     'bg-gradient-to-br from-emerald-500 to-teal-400',
  BID_ACCEPTED:     'bg-gradient-to-br from-brand-500 to-indigo-400',
  BID_REJECTED:     'bg-gradient-to-br from-red-500 to-rose-400',
  PAYMENT_HELD:     'bg-gradient-to-br from-amber-500 to-yellow-400',
  PAYMENT_RELEASED: 'bg-gradient-to-br from-emerald-600 to-green-500',
  TASK_COMPLETED:   'bg-gradient-to-br from-indigo-500 to-blue-400',
  RATING_RECEIVED:  'bg-gradient-to-br from-amber-400 to-yellow-300',
  SYSTEM_ALERT:     'bg-gradient-to-br from-slate-400 to-slate-500',
};

const typeEmoji: Record<string, string> = {
  TASK_MATCHED: '🎯', BID_RECEIVED: '📬', BID_ACCEPTED: '✅', BID_REJECTED: '❌',
  PAYMENT_HELD: '🔒', PAYMENT_RELEASED: '💸', TASK_COMPLETED: '🏆',
  RATING_RECEIVED: '⭐', SYSTEM_ALERT: '🔔',
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
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="section-title">Notifications</h1>
          <p className="text-slate-500 text-sm mt-0.5">Stay up to date with your tasks</p>
        </div>
        {notifications.some((n) => !n.read) && (
          <button
            onClick={() => markAllMutation.mutate()}
            className="btn-secondary text-sm py-1.5 px-3 gap-1.5"
          >
            <CheckCheck className="w-4 h-4" /> Mark all read
          </button>
        )}
      </div>

      {isLoading ? (
        <LoadingSpinner className="py-20" size="lg" />
      ) : notifications.length === 0 ? (
        <div className="text-center py-20 space-y-3">
          <div className="w-16 h-16 mx-auto rounded-full bg-brand-50 flex items-center justify-center">
            <Bell className="w-8 h-8 text-brand-200" />
          </div>
          <p className="font-semibold text-slate-500">No notifications yet</p>
          <p className="text-sm text-slate-400">We'll notify you when something happens with your tasks.</p>
        </div>
      ) : (
        <div className="space-y-2">
          {notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.read && markOneMutation.mutate(n.id)}
              className={`card flex items-start gap-3 p-4 cursor-pointer hover:shadow-card-hover transition-all duration-200
                ${!n.read ? 'border-brand-200 bg-brand-50/40 ring-1 ring-brand-100' : ''}`}
            >
              {/* Icon with gradient */}
              <div className={`w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0 text-sm
                ${typeColors[n.type] ?? 'bg-gradient-to-br from-slate-300 to-slate-400'}`}>
                {typeEmoji[n.type] ?? '🔔'}
              </div>

              <div className="flex-1 min-w-0">
                <p className="font-bold text-slate-900 text-sm">{n.title}</p>
                <p className="text-sm text-slate-500 mt-0.5 leading-relaxed">{n.message}</p>
                <p className="text-xs text-slate-300 mt-1.5">
                  {new Date(n.createdAt).toLocaleString('en-IN', { dateStyle: 'short', timeStyle: 'short' })}
                </p>
              </div>

              {!n.read && (
                <span className="w-2.5 h-2.5 rounded-full bg-brand-500 flex-shrink-0 mt-1.5 animate-pulse-brand" />
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
