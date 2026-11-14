import axiosClient from "../axiosClient.js";

export async function getProductReviews(productId, params = {}, options = {}) {
  const { data } = await axiosClient.get(`/api/reviews/products/${encodeURIComponent(productId)}`, {
    ...options, params: { page: 0, size: 5, ...params }, skipAuth: true,
  });
  const result = data?.data;
  if (!Array.isArray(result?.items) || !Number.isInteger(result.page) || result.page < 0 ||
      !Number.isInteger(result.totalPages) || result.totalPages < 0 ||
      !Number.isSafeInteger(result.totalElements) || result.totalElements < 0 ||
      typeof result.averageRating !== "number" || !Number.isFinite(result.averageRating) || result.averageRating < 0 || result.averageRating > 5 ||
      result.items.some((review) => review.productId !== productId || !review.id || !Number.isInteger(review.rating) || review.rating < 1 || review.rating > 5)) {
    throw new Error("Phản hồi đánh giá sản phẩm không hợp lệ.");
  }
  return result;
}


export async function createReview(input, options) {
  const { data } = await axiosClient.post("/api/reviews", input, options);
  const review = data?.data;
  if (!review?.id || review.productId !== input.productId || review.rating !== input.rating || typeof review.comment !== "string") {
    throw new Error("Chưa xác minh được kết quả gửi đánh giá. Vui lòng kiểm tra trước khi gửi lại.");
  }
  return review;
}
