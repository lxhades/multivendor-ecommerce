import axios from 'axios';
import { getSession, waitForSessionRefresh } from "./auth/authSession.js";

const axiosClient = axios.create({
  baseURL: 'http://localhost:8000', 
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

axiosClient.interceptors.request.use(async (config) => {
  const url = config.url ?? "";
  const gatewayRequest = config.baseURL === axiosClient.defaults.baseURL &&
    url.startsWith("/") && !url.startsWith("//") && !url.includes("\\");
  if (gatewayRequest && !config.skipAuth && !config.headers.has("Authorization")) {
    const waitingSession = getSession();
    await waitForSessionRefresh();
    if (waitingSession && getSession()?.userId !== waitingSession.userId) {
      throw new axios.CanceledError("Phiên tài khoản đã thay đổi.");
    }
  }
  const token = getSession()?.accessToken;
  if (token && gatewayRequest && !config.skipAuth && !config.headers.has("Authorization")) {
    config.headers.set("Authorization", `Bearer ${token}`);
  }
  return config;
});

export default axiosClient;