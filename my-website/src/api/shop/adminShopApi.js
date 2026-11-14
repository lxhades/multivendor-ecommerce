import { normalizeShopPage } from "./shopPage.js";
import axiosClient from "../axiosClient.js";

export async function getAdminShops(params, options) {
  const { data } = await axiosClient.get("/api/shops/admin", { ...options, params });
  return normalizeShopPage(data.data);
}

export async function getAdminShop(shopId, options) {
  const { data } = await axiosClient.get(`/api/shops/admin/${encodeURIComponent(shopId)}`, options);
  return data.data;
}

export async function updateAdminShopStatus(shopId, shopStatus, options) {
  const { data } = await axiosClient.patch(`/api/shops/admin/${encodeURIComponent(shopId)}/status`, undefined, { ...options, params: { shopStatus } });
  return data.data;
}

export async function closeAdminShop(shopId, options) {
  await axiosClient.delete(`/api/shops/admin/${encodeURIComponent(shopId)}`, options);
}
