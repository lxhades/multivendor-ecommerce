import test from "node:test";
import assert from "node:assert/strict";
import axiosClient from "../src/api/axiosClient.js";
import { getProductReviews } from "../src/api/order/reviewApi.js";
import { saveSession } from "../src/api/auth/authSession.js";

const review = { id: "r1", productId: "p1", buyerName: "Nguyễn An", rating: 5, comment: "Sản phẩm tốt", mediaUrls: [], createdAt: "2026-11-12T01:00:00Z" };
const page = { items: [review], page: 2, size: 5, totalElements: 23, totalPages: 5, averageRating: 4.3 };

test("review read sends public zero-based paging, forwards cancellation and keeps server-wide average", async () => {
  const original = axiosClient.defaults.adapter;
  const controller = new AbortController();
  saveSession({ userId: "buyer", accessToken: "private-token", refreshToken: "refresh" });
  axiosClient.defaults.adapter = async (config) => {
    assert.equal(config.url, "/api/reviews/products/p1");
    assert.deepEqual(config.params, { page: 2, size: 5 });
    assert.equal(config.signal, controller.signal);
    assert.equal(config.headers.get("Authorization"), undefined);
    return { data: { data: page }, status: 200, headers: {}, config };
  };
  try {
    const result = await getProductReviews("p1", { page: 2, size: 5 }, { signal: controller.signal });
    assert.equal(result.averageRating, 4.3);
    assert.equal(result.items[0].rating, 5);
    assert.equal(result.totalElements, 23);
  } finally { axiosClient.defaults.adapter = original; saveSession(null); }
});

test("a product without reviews returns the empty page and default request paging", async () => {
  const original = axiosClient.defaults.adapter;
  axiosClient.defaults.adapter = async (config) => {
    assert.deepEqual(config.params, { page: 0, size: 5 });
    return { data: { data: { items: [], page: 0, size: 5, totalElements: 0, totalPages: 0, averageRating: 0 } }, status: 200, headers: {}, config };
  };
  try { assert.deepEqual((await getProductReviews("p1")).items, []); }
  finally { axiosClient.defaults.adapter = original; }
});

test("invalid review ratings, wrong product IDs and invalid averages are rejected before rendering", async () => {
  const original = axiosClient.defaults.adapter;
  try {
    for (const invalid of [
      { ...page, items: [{ ...review, rating: 6 }] },
      { ...page, items: [{ ...review, productId: "other-product" }] },
      { ...page, averageRating: "4.3" },
      { ...page, items: null },
    ]) {
      axiosClient.defaults.adapter = async (config) => ({ data: { data: invalid }, status: 200, headers: {}, config });
      await assert.rejects(getProductReviews("p1"), /không hợp lệ/);
    }
  } finally { axiosClient.defaults.adapter = original; }
});
