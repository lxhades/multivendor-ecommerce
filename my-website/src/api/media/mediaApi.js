
import axiosClient from "../axiosClient.js";

export async function uploadMedia(file, { domain, entityId, variantId, signal }) {
  const body = new FormData();

  body.append("domain", domain);
  body.append("entityId", entityId);

  if (variantId) body.append("variantId", variantId);

  body.append("file", file);

  const { data } = await axiosClient.post("/api/media/upload", body, {
    signal,
    timeout: 120000,
    // Để trình duyệt tự tạo multipart boundary.
    headers: { "Content-Type": undefined },
  });

  // Backend phải trả về URL tương đối, ví dụ /media/products/...
  if (
      typeof data?.url !== "string" ||
      !data.url.startsWith("/media/") ||
      data.url.includes("\\")
  ) {
    throw new Error("Máy chủ chưa trả về đường dẫn media hợp lệ.");
  }

  return data.url;
}

// Giữ tương thích với các nơi đang gọi uploadImage.
export const uploadImage = uploadMedia;
