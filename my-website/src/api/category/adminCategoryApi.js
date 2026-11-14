import axiosClient from "../axiosClient.js";

export async function createCategory(input, options) {
  const { data } = await axiosClient.post("/api/categories/admin", input, options);
  return data.data;
}

export async function updateCategory(id, input, options) {
  const { data } = await axiosClient.put(`/api/categories/admin/${encodeURIComponent(id)}`, input, options);
  return data.data;
}

export async function updateCategoryStatus(id, status, options) {
  const { data } = await axiosClient.patch(`/api/categories/admin/${encodeURIComponent(id)}/status`, { status }, options);
  return data.data;
}

export async function deleteCategory(id, options) {
  await axiosClient.delete(`/api/categories/admin/${encodeURIComponent(id)}`, options);
}
