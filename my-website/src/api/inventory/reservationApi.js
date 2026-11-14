import axiosClient from "../axiosClient.js";

export async function getReservationsByOrder(orderId, options) {
  const { data } = await axiosClient.get("/api/reservations/admin", { ...options, params: { orderId } });
  return data.data;
}

export async function getReservation(id, options) {
  const { data } = await axiosClient.get(`/api/reservations/admin/${encodeURIComponent(id)}`, options);
  return data.data;
}

export async function reserveStock(input, options) {
  const { data } = await axiosClient.post("/api/reservations/admin", input, options);
  return data.data;
}

export async function confirmReservation(id, options) {
  const { data } = await axiosClient.post(`/api/reservations/admin/${encodeURIComponent(id)}/confirm`, undefined, options);
  return data.data;
}

export async function releaseReservation(id, options) {
  const { data } = await axiosClient.post(`/api/reservations/admin/${encodeURIComponent(id)}/release`, undefined, options);
  return data.data;
}
