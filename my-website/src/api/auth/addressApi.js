import axiosClient from "../axiosClient.js";

export async function getAddresses(userId, options) {
  const { data } = await axiosClient.get(`/auth/users/${encodeURIComponent(userId)}/addresses`, options);
  return data.data;
}

export async function createAddress(userId, input, options) {
  const { data } = await axiosClient.post(`/auth/users/${encodeURIComponent(userId)}/addresses`, input, options);
  return data.data;
}

export async function updateAddress(userId, addressId, input, options) {
  const { data } = await axiosClient.put(`/auth/users/${encodeURIComponent(userId)}/addresses/${encodeURIComponent(addressId)}`, input, options);
  return data.data;
}

export async function deleteAddress(userId, addressId, options) {
  await axiosClient.delete(`/auth/users/${encodeURIComponent(userId)}/addresses/${encodeURIComponent(addressId)}`, options);
}
