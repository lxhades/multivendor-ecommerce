export const categoryStatuses = { ACTIVE: "Đang hiển thị", INACTIVE: "Đã ẩn" };
export function validateAdminCategory(category, id = category?.id) {
  if (!category?.id || category.id !== id || typeof category.name !== "string" || typeof category.slug !== "string" || !Object.hasOwn(categoryStatuses, category.status) || !Number.isInteger(category.displayOrder) || category.displayOrder < 0) throw new Error("Thông tin danh mục không hợp lệ.");
  return category;
}
export function unavailableCategoryParents(categories, categoryId) {
  const ids = new Set(categoryId ? [categoryId] : []);
  let changed = true;
  while (changed) {
    changed = false;
    for (const item of categories) {
      if (ids.has(item.parentId) && !ids.has(item.id)) { ids.add(item.id); changed = true; }
    }
  }
  return ids;
}
export function categoryFormInput(form) {
  const name = String(form.get("name") ?? "").trim();
  const slug = String(form.get("slug") ?? "").trim();
  const description = String(form.get("description") ?? "").trim();
  const parentId = String(form.get("parentId") ?? "").trim() || null;
  const order = String(form.get("displayOrder") ?? "").trim();
  const displayOrder = Number(order);
  if (!name || name.length > 100 || !/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(slug) || slug.length > 100 || description.length > 1000 || !/^\d+$/.test(order) || !Number.isInteger(displayOrder) || displayOrder < 0 || displayOrder > 2147483647) throw new Error("Kiểm tra tên/slug tối đa 100 ký tự, mô tả tối đa 1.000 ký tự và thứ tự hiển thị nguyên không âm.");
  return { name, slug, description, parentId, displayOrder };
}
