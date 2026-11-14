import axiosClient from "../axiosClient.js";

export async function searchProducts(params, signal) {
  const { data } = await axiosClient.get("/api/products/search", { params, signal, skipAuth: true });
  if (!Array.isArray(data?.data?.items)) throw new Error("Phản hồi danh sách sản phẩm không hợp lệ.");
  return data.data;
}

export async function getProducts(params, options) {
  const { data } = await axiosClient.get("/api/products", { ...options, params });
  return data.data;
}

export async function getProduct(id, options) {
  const { data } = await axiosClient.get(`/api/products/${encodeURIComponent(id)}`, options);
  return data.data;
}
