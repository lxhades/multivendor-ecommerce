import { normalizeShopPage } from "./shopPage.js";
import axiosClient from "../axiosClient.js";

export async function getShops(params, options) {
  const { data } = await axiosClient.get("/shops", { ...options, params });
  return normalizeShopPage(data.data);
}

export async function getShop(shopId, options) {
  const { data } = await axiosClient.get(`/shops/${encodeURIComponent(shopId)}`, options);
  return data.data;
}
