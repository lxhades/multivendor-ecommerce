import axiosClient from "../axiosClient.js";

export async function getSellerOrderPage(shopId, params, options) {
  const { data } = await axiosClient.get(`/api/orders/seller/shops/${encodeURIComponent(shopId)}`, { ...options, params });
  return data.data;
}

export async function getSellerOrdersByCheckout(checkoutId, options) {
  const { data } = await axiosClient.get("/api/orders/seller", { ...options, params: { checkoutId } });
  return data.data;
}

export async function getSellerOrdersByShop(shopId, options) {
  const { data } = await axiosClient.get("/api/orders/seller", { ...options, params: { shopId } });
  return data.data;
}

export async function getSellerOrder(id, options) {
  const { data } = await axiosClient.get(`/api/orders/seller/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function cancelSellerOrder(id, options) {
  const { data } = await axiosClient.post(`/api/orders/seller/${encodeURIComponent(id)}/cancel`, undefined, options);
  return data.data;
}

export async function shipSellerOrder(id, options) {
  const { data } = await axiosClient.post(`/api/orders/seller/${encodeURIComponent(id)}/ship`, undefined, options);
  return data.data;
}
