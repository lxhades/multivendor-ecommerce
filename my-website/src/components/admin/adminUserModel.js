export const userRoles = { BUYER: "Người mua", SELLER: "Người bán", ADMIN: "Quản trị viên" };
export const userStatuses = { ACTIVE: "Hoạt động", LOCKED: "Đã cấm" };
export function validateAdminUser(user, id = user?.userId) {
  if (!user?.userId || user.userId !== id || typeof user.email !== "string" || typeof user.phone !== "string" || !Object.hasOwn(userRoles, user.role) || !Object.hasOwn(userStatuses, user.status)) throw new Error("Dữ liệu người dùng không hợp lệ.");
  return user;
}
export function validateAdminUsers(data, page, size) {
  if (!Array.isArray(data?.users) || data.page !== page || data.size !== size || !Number.isSafeInteger(data.totalElements) || data.totalElements < 0 || !Number.isInteger(data.totalPages) || data.totalPages < 0) throw new Error("Danh sách người dùng không hợp lệ.");
  data.users.forEach((user) => validateAdminUser(user));
  return data;
}
export function adminUserContact(form) {
  const email = String(form.get("email") ?? "").trim();
  const phone = String(form.get("phone") ?? "").trim();
  if (email.length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) || !/^0[0-9]{9}$/.test(phone)) throw new Error("Nhập email hợp lệ và số điện thoại 10 số bắt đầu bằng 0.");
  return { email, phone };
}
