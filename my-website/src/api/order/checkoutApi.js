import axiosClient from "../axiosClient.js";

export async function createCheckout(input, idempotencyKey, options) {
  const { data } = await axiosClient.post("/api/checkouts", input, { ...options, headers: { ...options?.headers, "Idempotency-Key": idempotencyKey } });
  return data.data;
}

export async function getCheckout(id, options) {
  const { data } = await axiosClient.get(`/api/checkouts/${encodeURIComponent(id)}`, options);
  return data.data;
}
