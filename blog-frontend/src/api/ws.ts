import { request } from "./request";

/**
 * WebSocket 测试接口 API
 */
export const wsApi = {
  /**
   * 发送测试通知给指定用户
   */
  sendToUser(params: { userId: number; message: string }): Promise<void> {
    return request.post("/api/v1/ws/send", null, { params });
  },

  /**
   * 广播测试通知
   */
  broadcast(message: string): Promise<void> {
    return request.post("/api/v1/ws/broadcast", null, {
      params: { message },
    });
  },

  /**
   * 获取在线用户数
   */
  getOnlineCount(): Promise<number> {
    return request.get("/api/v1/ws/online-count");
  },

  /**
   * 检查用户是否在线
   */
  isOnline(userId: number): Promise<boolean> {
    return request.get("/api/v1/ws/is-online", { params: { userId } });
  },
};
