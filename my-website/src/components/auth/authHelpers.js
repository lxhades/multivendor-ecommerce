export function authErrorMessage(error, fallback = "Không thể hoàn tất yêu cầu. Vui lòng thử lại.") {
  if (error.response?.status >= 500) return "Máy chủ đang gặp sự cố. Vui lòng thử lại sau.";
  const details = error.response?.data?.details;
  return (Array.isArray(details) && details.filter((item) => typeof item === "string").join(" ")) ||
    error.response?.data?.message || (error.response ? fallback : error.name === "AxiosError" ? "Không kết nối được máy chủ. Vui lòng kiểm tra kết nối và thử lại." : error.message) || fallback;
}

export function validateProfile(profile, session) {
  if (profile?.userId !== session.userId || profile.status !== "ACTIVE" || !["BUYER", "SELLER", "ADMIN"].includes(profile.role)) {
    throw new Error("Tài khoản không hoạt động hoặc thông tin đăng nhập không hợp lệ.");
  }
  return profile;
}

export function validateTokens(tokens) {
  if (!["userId", "accessToken", "refreshToken"].every((key) => typeof tokens?.[key] === "string" && tokens[key].trim())) {
    throw new Error("Phản hồi đăng nhập không hợp lệ. Vui lòng thử lại.");
  }
  return tokens;
}

export function readAuthInput(form, register = false) {
  const email = String(form.get("email") ?? "").trim();
  const password = String(form.get("password") ?? "");
  if (!email || email.length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) throw new Error("Vui lòng nhập email hợp lệ.");
  if (!password.trim()) throw new Error("Vui lòng nhập mật khẩu.");
  if (!register) return { email, password };
  const phone = String(form.get("phone") ?? "").trim();
  if (!/^0[0-9]{9}$/.test(phone)) throw new Error("Số điện thoại gồm 10 chữ số, bắt đầu bằng 0.");
  if (password.length < 8 || password.length > 20) throw new Error("Mật khẩu phải có từ 8 đến 20 ký tự.");
  if (password !== form.get("passwordConfirm")) throw new Error("Mật khẩu xác nhận không khớp.");
  return { email, phone, password };
}
