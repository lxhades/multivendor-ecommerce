import { useEffect } from "react";
import { useLocation } from "react-router-dom";

export default function ScrollToTop() {
    // Lấy thông tin đường dẫn hiện tại (pathname)
    const { pathname } = useLocation();

    useEffect(() => {
        // Mỗi khi pathname thay đổi, cuộn lên góc trên cùng bên trái (0, 0)
        window.scrollTo({
            top: 0,
            left: 0,
            behavior: "instant" // Dùng "instant" để cuộn ngay lập tức, không bị giật
        });
    }, [pathname]);

    return null; // Component này không render ra giao diện gì cả
}