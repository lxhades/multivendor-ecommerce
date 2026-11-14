import axiosClient from "../axiosClient.js";

export async function getSellerDemandReport(shopId, params, options) {
  const { data } = await axiosClient.get(`/api/orders/seller/shops/${encodeURIComponent(shopId)}/reports/demand`, { ...options, params });
  return data.data;
}
export async function getSellerSalesReport(shopId, params, options) {
  const { data } = await axiosClient.get(`/api/orders/seller/shops/${encodeURIComponent(shopId)}/reports/sales`, { ...options, params });
  return data.data;
}
