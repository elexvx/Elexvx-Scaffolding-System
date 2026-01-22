import { defineStore } from 'pinia';

import { fetchMessages as fetchMessageList, markAllMessagesRead, markMessageRead } from '@/api/message';
import type { NotificationItem } from '@/types/interface';

type MsgDataType = NotificationItem[];

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    msgData: [] as NotificationItem[],
  }),
  getters: {
    unreadMsg: (state) => state.msgData.filter((item: NotificationItem) => item.status),
    readMsg: (state) => state.msgData.filter((item: NotificationItem) => !item.status),
  },
  actions: {
    async fetchMessages() {
      try {
        const list = await fetchMessageList();
        if (Array.isArray(list)) {
          // 去重处理，防止后端或持久化导致重复
          const uniqueMap = new Map();
          list.forEach((item) => {
            if (!uniqueMap.has(item.id)) {
              uniqueMap.set(item.id, item);
            }
          });
          this.msgData = Array.from(uniqueMap.values());
        } else {
          this.msgData = [];
        }
      } catch {
        this.msgData = [];
      }
    },
    async markRead(id: string, read = true) {
      await markMessageRead(id, read);
      this.msgData = this.msgData.map((item) => (item.id === id ? { ...item, status: !read } : item));
    },
    async markAllRead() {
      await markAllMessagesRead();
      this.msgData = this.msgData.map((item) => ({ ...item, status: false }));
    },
    setMsgData(data: MsgDataType) {
      this.msgData = data;
    },
  },
  persist: true,
});
