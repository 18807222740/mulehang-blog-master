import type { ApiResponse, UploadFile } from "@/types/api";
import instance from "./request";

/**
 * 文件上传 API
 */
export const fileApi = {
  /**
   * 上传文件
   *
   * @param file 图片文件
   * @param onProgress 上传进度回调（0-100）
   */
  upload(file: File, onProgress?: (progress: number) => void): Promise<UploadFile> {
    const form = new FormData();
    form.append("file", file);
    return instance
      .post<ApiResponse<UploadFile>>("/api/v1/files/upload", form, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
        onUploadProgress: (event) => {
          if (!onProgress || !event.total) return;
          const percent = Math.round((event.loaded / event.total) * 100);
          onProgress(percent);
        },
      })
      .then((res) => res.data.data);
  },
};
