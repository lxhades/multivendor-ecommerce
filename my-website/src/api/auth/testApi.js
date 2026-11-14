import axiosClient from "../axiosClient.js";

export async function getTest() {
  const res = await axiosClient.get("auth/hello");
  return res.data;
}