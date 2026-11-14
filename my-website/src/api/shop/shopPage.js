// product_service uses Spring Data VIA_DTO: { content, page: { ... } }.
// Keep the flat pagination shape expected by the shop screens and accept
// legacy PageImpl responses as well.
export function normalizeShopPage(data) {
  const page = data?.page;
  if (!page || typeof page !== "object" || Array.isArray(page)) return data;
  return {
    ...data,
    number: page.number,
    size: page.size,
    totalElements: page.totalElements,
    totalPages: page.totalPages,
  };
}
