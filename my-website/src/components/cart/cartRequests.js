import { getProduct } from "../../api/product/productApi.js";
import { getAvailableStock } from "../../api/inventory/inventoryApi.js";
import { productCartSnapshot } from "./cartModel.js";

export async function loadCartItem(productId, variantId, { signal, productRequest } = {}) {
  const [product, stock] = await Promise.all([
    productRequest ?? getProduct(productId, { signal, skipAuth: true }),
    getAvailableStock(variantId, { signal, skipAuth: true }),
  ]);
  if (product?.id !== productId || stock?.variantId !== variantId || !Number.isSafeInteger(stock.availableQuantity) || stock.availableQuantity < 0) {
    throw new Error("Thông tin sản phẩm hoặc tồn kho không khớp.");
  }
  return { snapshot: productCartSnapshot(product, variantId), availableQuantity: stock.availableQuantity };
}

export function cartError(error) {
  if ([400, 404].includes(error.response?.status)) return "Sản phẩm hoặc phân loại này không còn bán. Bạn có thể xóa khỏi giỏ.";
  return error.response?.data?.message || (error.response || error.name === "AxiosError" ? "Không kiểm tra được sản phẩm/tồn kho. Vui lòng thử lại." : error.message) || "Không thể cập nhật giỏ hàng.";
}
