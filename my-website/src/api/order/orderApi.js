import axiosClient from "../axiosClient.js";

export async function getBuyerOrders(options) {
  const { data } = await axiosClient.get("/api/orders", options);
  return data.data;
}

export async function getBuyerOrdersByCheckout(checkoutId, options) {
  const { data } = await axiosClient.get("/api/orders", { ...options, params: { checkoutId } });
  return data.data;
}

export async function getBuyerOrder(id, options) {
  const { data } = await axiosClient.get(`/api/orders/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function cancelBuyerOrder(id, options) {
  const { data } = await axiosClient.post(`/api/orders/${encodeURIComponent(id)}/cancel`, undefined, options);
  return data.data;
}
