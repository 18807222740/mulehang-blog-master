import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type { Notification } from "@/types/api";

type ConnectionStatus = "disconnected" | "connecting" | "connected" | "error";

type NotificationItem = Notification & {
  localId: string;
  read: boolean;
};

/**
 * WebSocket 通知状态管理。
 */
export const useNotificationStore = defineStore("notifications", () => {
  const notifications = ref<NotificationItem[]>([]);
  const connectionStatus = ref<ConnectionStatus>("disconnected");
  const lastError = ref("");
  const socket = ref<WebSocket | null>(null);
  const reconnectTimer = ref<number | null>(null);
  const reconnectAttempts = ref(0);
  const shouldReconnect = ref(false);

  const unreadCount = computed(
    () => notifications.value.filter((item) => !item.read).length,
  );

  /**
   * 构建 WebSocket 连接地址。
   */
  function resolveWebSocketUrl(): string {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || window.location.origin;
    const url = new URL(baseUrl, window.location.origin);
    url.protocol = url.protocol === "https:" ? "wss:" : "ws:";
    url.pathname = "/ws/notifications";
    return url.toString();
  }

  /**
   * 解析 WebSocket 消息体。
   */
  function parseMessage(payload: string): Notification {
    try {
      return JSON.parse(payload) as Notification;
    } catch (error) {
      return {
        type: "SYSTEM",
        title: "系统通知",
        content: payload,
        timestamp: new Date().toISOString(),
      };
    }
  }

  /**
   * 追加通知到列表顶部。
   */
  function addNotification(payload: Notification) {
    const localId = `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`;
    notifications.value.unshift({
      ...payload,
      localId,
      read: payload.read ?? false,
    });
  }

  /**
   * 计划重连（指数退避）。
   */
  function scheduleReconnect() {
    if (reconnectTimer.value) return;
    if (reconnectAttempts.value >= 5) {
      connectionStatus.value = "error";
      lastError.value = "重连次数过多，请刷新页面重试";
      return;
    }
    const delay = Math.min(3000, 800 + reconnectAttempts.value * 600);
    reconnectAttempts.value += 1;
    reconnectTimer.value = window.setTimeout(() => {
      reconnectTimer.value = null;
      connect();
    }, delay);
  }

  /**
   * 建立 WebSocket 连接。
   */
  function connect() {
    if (
      socket.value &&
      (connectionStatus.value === "connected" ||
        connectionStatus.value === "connecting")
    ) {
      return;
    }

    shouldReconnect.value = true;
    connectionStatus.value = "connecting";
    lastError.value = "";

    const ws = new WebSocket(resolveWebSocketUrl());
    socket.value = ws;

    ws.onopen = () => {
      connectionStatus.value = "connected";
      reconnectAttempts.value = 0;
      lastError.value = "";
    };

    ws.onmessage = (event) => {
      const { data } = event;
      if (typeof data === "string") {
        addNotification(parseMessage(data));
        return;
      }
      if (data instanceof Blob) {
        data
          .text()
          .then((text) => addNotification(parseMessage(text)))
          .catch(() => {});
        return;
      }
      if (data instanceof ArrayBuffer) {
        const text = new TextDecoder().decode(data);
        addNotification(parseMessage(text));
      }
    };

    ws.onerror = () => {
      connectionStatus.value = "error";
      lastError.value = "WebSocket 连接异常";
    };

    ws.onclose = () => {
      socket.value = null;
      if (connectionStatus.value !== "error") {
        connectionStatus.value = "disconnected";
      }
      if (shouldReconnect.value) {
        scheduleReconnect();
      }
    };
  }

  /**
   * 主动断开 WebSocket 连接。
   */
  function disconnect() {
    shouldReconnect.value = false;
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value);
      reconnectTimer.value = null;
    }
    if (socket.value) {
      socket.value.close(1000, "client disconnect");
      socket.value = null;
    }
    connectionStatus.value = "disconnected";
  }

  /**
   * 标记指定通知已读。
   */
  function markAsRead(localId: string) {
    const target = notifications.value.find((item) => item.localId === localId);
    if (target) {
      target.read = true;
    }
  }

  /**
   * 标记全部通知已读。
   */
  function markAllRead() {
    notifications.value.forEach((item) => {
      item.read = true;
    });
  }

  /**
   * 清空通知列表。
   */
  function clearAll() {
    notifications.value = [];
  }

  return {
    notifications,
    unreadCount,
    connectionStatus,
    lastError,
    connect,
    disconnect,
    markAsRead,
    markAllRead,
    clearAll,
  };
});
