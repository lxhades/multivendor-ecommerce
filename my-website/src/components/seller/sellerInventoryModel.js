export function validateVariantStock(stock, shopId, productId, variantId = stock?.variantId) {
  if (!stock?.id || stock.shopId !== shopId || stock.productId !== productId || stock.variantId !== variantId ||
      ![stock.onHandQuantity, stock.reservedQuantity, stock.availableQuantity].every((quantity) => Number.isSafeInteger(quantity) && quantity >= 0) ||
      stock.reservedQuantity > stock.onHandQuantity || stock.availableQuantity !== stock.onHandQuantity - stock.reservedQuantity) {
    throw new Error("Dữ liệu tồn kho không hợp lệ hoặc không khớp biến thể.");
  }
  return stock;
}
