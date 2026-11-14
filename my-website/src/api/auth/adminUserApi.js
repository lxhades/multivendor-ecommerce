import axiosClient from "../axiosClient.js";

export async function getAdminUsers(params, options) {
  const { data } = await axiosClient.get("/api/users/admin", { ...options, params });
  return data.data;
}

export async function getAdminUser(userId, options) {
  const { data } = await axiosClient.get(`/api/users/admin/${encodeURIComponent(userId)}`, options);
  return data.data;
}

export async function deleteAdminUser(userId, options) {
  await axiosClient.delete(`/api/users/admin/${encodeURIComponent(userId)}`, options);
}


export async function updateAdminUser(userId, input, options) {
  const { data } = await axiosClient.put(`/api/users/admin/${encodeURIComponent(userId)}`, input, options);
  return data.data;
}
export async function setAdminUserStatus(userId, status, options) {
  const { data } = await axiosClient.patch(`/api/users/admin/${encodeURIComponent(userId)}/status`, { status }, options);
  return data.data;
}
