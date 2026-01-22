import { request } from '@/utils/request';

export interface NotificationPayload {
  id?: number;
  title: string;
  summary?: string;
  content: string;
  priority: string;
  status?: string;
  type?: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  summary?: string;
  content: string;
  priority: string;
  status: string;
  type?: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  publishAt?: string;
  createdAt?: string;
  updatedAt?: string;
  createdByName?: string;
}

export interface NotificationSummary {
  id: number;
  title: string;
  summary?: string;
  priority: string;
  type?: string;
  status: string;
  coverUrl?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  publishAt?: string;
  updatedAt?: string;
}

export function fetchNotifications(params: {
  page?: number;
  size?: number;
  keyword?: string;
  status?: string;
  priority?: string;
}) {
  return request.get<{ list: NotificationItem[]; total: number }>({
    url: '/notification',
    params,
  });
}

export function fetchNotificationDetail(id: number) {
  return request.get<NotificationItem>({ url: `/notification/${id}` });
}

export function fetchLatestNotifications(size = 8) {
  return request.get<NotificationSummary[]>({ url: '/notification/latest', params: { size } });
}

export function createNotification(payload: NotificationPayload) {
  return request.post<NotificationItem>({ url: '/notification', data: payload });
}

export function updateNotification(id: number, payload: NotificationPayload) {
  return request.put<NotificationItem>({ url: `/notification/${id}`, data: payload });
}

export function publishNotification(id: number, publish = true) {
  const url = `/notification/${id}/publish?publish=${encodeURIComponent(String(publish))}`;
  return request.post<NotificationItem>({ url });
}

export function deleteNotification(id: number) {
  return request.delete({ url: `/notification/${id}` });
}

export function broadcastNotification(payload: NotificationPayload) {
  return request.post<NotificationItem>({ url: '/notification/broadcast', data: payload });
}
