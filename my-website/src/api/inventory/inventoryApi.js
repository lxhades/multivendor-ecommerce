import axiosClient from "../axiosClient.js";

export async function getAvailableStock(variantId, options) {
  const { data } = await axiosClient.get(`/api/inventories/variant/${encodeURIComponent(variantId)}`, options);
  return data.data;
}

export async function getSellerInventory(shopId, options) {
  const { data } = await axiosClient.get("/api/inventories/seller", { ...options, params: { shopId } });
  return data.data;
}

export async function getSellerInventoryRecord(id, options) {
  const { data } = await axiosClient.get(`/api/inventories/seller/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function getSellerInventoryByVariant(variantId, options) {
  const { data } = await axiosClient.get(`/api/inventories/seller/variants/${encodeURIComponent(variantId)}`, options);
  return data.data;
}

export async function adjustSellerStock(variantId, newQuantity, options) {
  const { data } = await axiosClient.patch(`/api/inventories/seller/variants/${encodeURIComponent(variantId)}/stock`, { newQuantity }, options);
  return data.data;
}

export async function getAdminInventory(shopId, options) {
  const { data } = await axiosClient.get("/api/inventories/admin", { ...options, params: { shopId } });
  return data.data;
}

export async function getAdminInventoryRecord(id, options) {
  const { data } = await axiosClient.get(`/api/inventories/admin/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function getAdminInventoryByVariant(variantId, options) {
  const { data } = await axiosClient.get(`/api/inventories/admin/variants/${encodeURIComponent(variantId)}`, options);
  return data.data;
}

export async function initializeInventory(input, options) {
  const { data } = await axiosClient.post("/api/inventories/admin", input, options);
  return data.data;
}

export async function increaseStock(input, options) {
  const { data } = await axiosClient.patch("/api/inventories/admin/increase-stock", input, options);
  return data.data;
}
