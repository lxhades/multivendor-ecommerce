import axiosClient from "../axiosClient.js";

export async function createShipment(input, options) {
  const { data } = await axiosClient.post("/api/shipments/admin", input, options);
  return data;
}

export async function getShipmentByTrackingCode(trackingCode, options) {
  const { data } = await axiosClient.get(`/api/shipments/admin/${encodeURIComponent(trackingCode)}`, options);
  return data;
}

export async function getShipmentByOrder(orderId, options) {
  const { data } = await axiosClient.get("/api/shipments/admin", { ...options, params: { orderId } });
  return data;
}
