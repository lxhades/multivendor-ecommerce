import axiosClient from "../axiosClient.js";

export async function getPendingProducts(options) {
  const { data } = await axiosClient.get("/api/products/admin/pending", options);
  return data.data;
}

export async function getAdminProduct(id, options) {
  const { data } = await axiosClient.get(`/api/products/admin/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function approveProduct(id, input, options) {
  const { data } = await axiosClient.post(`/api/products/admin/${encodeURIComponent(id)}/approve`, input, options);
  return data.data;
}

export async function rejectProduct(id, input, options) {
  const { data } = await axiosClient.post(`/api/products/admin/${encodeURIComponent(id)}/reject`, input, options);
  return data.data;
}
