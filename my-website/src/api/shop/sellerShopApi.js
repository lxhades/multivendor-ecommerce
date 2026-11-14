import axiosClient from "../axiosClient.js";

export async function createSellerShop(input, options) {
  const { data } = await axiosClient.post("/api/shops/seller", input, options);
  return data.data;
}

export async function getMyShop(options) {
  const { data } = await axiosClient.get("/api/shops/seller/my-shop", options);
  return data.data;
}

export async function updateSellerShop(shopId, input, options) {
  const { data } = await axiosClient.put(`/api/shops/seller/${encodeURIComponent(shopId)}`, input, options);
  return data.data;
}

export async function closeSellerShop(shopId, options) {
  await axiosClient.delete(`/api/shops/seller/${encodeURIComponent(shopId)}`, options);
}

export async function reopenSellerShop(shopId, options) {
  await axiosClient.patch(`/api/shops/seller/${encodeURIComponent(shopId)}`, undefined, options);
}
