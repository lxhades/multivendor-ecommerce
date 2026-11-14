import axiosClient from "../axiosClient.js";

export async function registerBuyer(input, options) {
  const { data } = await axiosClient.post("/auth/register", input, { ...options, skipAuth: true });
  return data.data;
}

export async function login(input, options) {
  const { data } = await axiosClient.post("/auth/login", input, { ...options, skipAuth: true });
  return data.data;
}

export async function getCurrentUser(options) {
  const { data } = await axiosClient.get("/auth/me", options);
  return data.data;
}

export async function forgotPassword(email, options) {
  await axiosClient.put("/auth/forgotpassword", { email }, { ...options, skipAuth: true });
}

export async function changePassword(input, options) {
  await axiosClient.put("/auth/changepassword", input, options);
}

export async function refreshToken(refreshToken, options) {
  const { data } = await axiosClient.post("/auth/refresh", { refreshToken }, { ...options, skipAuth: true });
  return data.data;
}

export async function logout(refreshToken, options) {
  await axiosClient.post("/auth/logout", { refreshToken }, { ...options, skipAuth: true });
}

export async function registerSeller(options) {
  await axiosClient.post("/auth/me/register-seller", undefined, options);
}
