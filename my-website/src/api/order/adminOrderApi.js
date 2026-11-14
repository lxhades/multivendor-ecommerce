import axiosClient from "../axiosClient.js";

export async function getAdminOrder(orderId, options) {
  const { data } = await axiosClient.get(`/api/orders/admin/${encodeURIComponent(orderId)}`, options);
  return data.data;
}

export async function getAdminOrdersByCheckout(checkoutId, options) {
  const { data } = await axiosClient.get("/api/orders/admin", { ...options, params: { checkoutId } });
  return data.data;
}

export async function getAdminOrdersByShop(shopId, options) {
  const { data } = await axiosClient.get("/api/orders/admin", { ...options, params: { shopId } });
  return data.data;
}

export async function getAdminOrdersByBuyer(buyerId, options) {
  const { data } = await axiosClient.get("/api/orders/admin", { ...options, params: { buyerId } });
  return data.data;
}

export async function cancelAdminOrder(orderId, options) {
  const { data } = await axiosClient.post(`/api/orders/admin/${encodeURIComponent(orderId)}/cancel`, undefined, options);
  return data.data;
}

export async function shipAdminOrder(orderId, options) {
  const { data } = await axiosClient.post(`/api/orders/admin/${encodeURIComponent(orderId)}/ship`, undefined, options);
  return data.data;
}

export async function getAdminOrderPage(params, options) {
  const { data } = await axiosClient.get("/api/orders/admin/page", { ...options, params });
  return data.data;
}
export async function getAdminRevenueReport(params, options) {
  const { data } = await axiosClient.get("/api/orders/admin/reports/revenue", { ...options, params });
  return data.data;
}
