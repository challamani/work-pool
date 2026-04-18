import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '../store/authStore';
import { useNotificationStore } from '../store/notificationStore';
import type { Notification } from '../types';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8083';

export function useWebSocket(onNotification?: (n: Notification) => void) {
  const { token, user } = useAuthStore();
  const { incrementUnread } = useNotificationStore();
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!token || !user) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_URL}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/user/${user.id}/queue/notifications`, (message) => {
          const notification: Notification = JSON.parse(message.body);
          incrementUnread();
          onNotification?.(notification);
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [incrementUnread, onNotification, token, user]);

  return clientRef;
}
