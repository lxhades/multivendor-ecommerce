import axiosClient from "../axiosClient.js";

export async function getCategories(options) {
  const { data } = await axiosClient.get("/categories", options);
  return data.data;
}

export async function getCategory(id, options) {
  const { data } = await axiosClient.get(`/categories/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function getChildCategories(parentId, options) {
  const { data } = await axiosClient.get(`/categories/${encodeURIComponent(parentId)}/children`, options);
  return data.data;
}
