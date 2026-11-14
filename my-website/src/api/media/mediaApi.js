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
    // Let the browser generate the multipart boundary instead of using JSON.
    headers: { "Content-Type": undefined },
  });
  if (typeof data?.url !== "string" || !/^https?:\/\//i.test(data.url)) {
    throw new Error("Máy chủ chưa trả về đường dẫn ảnh/video hợp lệ.");
  }
  return data.url;
}

// Preserve existing callers that upload images only.
export const uploadImage = uploadMedia;
