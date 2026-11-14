import { fileURLToPath, URL } from "node:url";
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'
// https://vite.dev/config/
export default defineConfig({
  resolve: { alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) } },
  plugins: [react(),
        tailwindcss(),
  ],
   server: {
    port: 3000, // Đổi thành cổng bạn muốn (ví dụ 3000, 8080, 8000...)
    open: true, // (Tùy chọn) Tự động mở trình duyệt khi chạy
  }
})
