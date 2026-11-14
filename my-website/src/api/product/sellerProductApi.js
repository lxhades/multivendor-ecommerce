import axiosClient from "../axiosClient.js";

export async function getSellerProducts(shopId, options) {
  const { data } = await axiosClient.get(`/api/products/seller/shops/${encodeURIComponent(shopId)}`, options);
  return data.data;
}

export async function createSellerProduct(shopId, input, options) {
  const { data } = await axiosClient.post(`/api/products/seller/shops/${encodeURIComponent(shopId)}`, input, options);
  return data.data;
}

export async function getSellerProduct(shopId, id, options) {
  const { data } = await axiosClient.get(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function updateSellerProduct(shopId, id, input, options) {
  const { data } = await axiosClient.put(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}`, input, options);
  return data.data;
}

export async function deleteSellerProduct(shopId, id, options) {
  await axiosClient.delete(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}`, options);
}

export async function updateSellerProductStatus(shopId, id, status, options) {
  const { data } = await axiosClient.patch(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}/status`, { status }, options);
  return data.data;
}

export async function createSellerVariant(shopId, id, input, options) {
  const { data } = await axiosClient.post(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}/variants`, input, options);
  return data.data;
}

export async function updateSellerVariant(shopId, id, variantId, input, options) {
  const { data } = await axiosClient.put(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}/variants/${encodeURIComponent(variantId)}`, input, options);
  return data.data;
}

export async function deleteSellerVariant(shopId, id, variantId, options) {
  const { data } = await axiosClient.delete(`/api/products/seller/shops/${encodeURIComponent(shopId)}/${encodeURIComponent(id)}/variants/${encodeURIComponent(variantId)}`, options);
  return data.data;
}
