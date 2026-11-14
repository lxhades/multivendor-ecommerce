import { useEffect, useState } from "react";
import { getProduct } from "../../api/product/productApi.js";
import { cartError, loadCartItem } from "./cartRequests.js";

export function useCartAvailability(items) {
  const signature = JSON.stringify(items.map(({ productId, variantId }) => ({ productId, variantId })));
  const [attempt, setAttempt] = useState(0);
  const [result, setResult] = useState(null);
  useEffect(() => {
    const pairs = JSON.parse(signature);
    if (!pairs.length) return;
    const controller = new AbortController();
    // Share one product read when multiple cart variants belong to the same product.
    const products = new Map();
    const checks = pairs.map(async ({ productId, variantId }) => {
      if (!products.has(productId)) products.set(productId, getProduct(productId, { signal: controller.signal, skipAuth: true }));
      try {
        const checked = await loadCartItem(productId, variantId, { signal: controller.signal, productRequest: products.get(productId) });
        return [variantId, checked];
      } catch (error) { return [variantId, { error: cartError(error) }]; }
    });
    Promise.all(checks).then((entries) => {
      if (!controller.signal.aborted) setResult({ signature, attempt, checks: Object.fromEntries(entries) });
    });
    return () => controller.abort();
  }, [signature, attempt]);
  const current = result?.signature === signature && result.attempt === attempt ? result : null;
  function updateCheck(variantId, check) {
    setResult((value) => value?.signature === signature && value.attempt === attempt
      ? { ...value, checks: { ...value.checks, [variantId]: check } } : value);
  }
  return { updateCheck, checks: current?.checks ?? {}, loading: items.length > 0 && !current, refresh: () => setAttempt((value) => value + 1) };
}
