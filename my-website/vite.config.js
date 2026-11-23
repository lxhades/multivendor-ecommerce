
import { fileURLToPath, URL } from "node:url";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
    resolve: {
        alias: {
            "@": fileURLToPath(new URL("./src", import.meta.url)),
        },
    },

    plugins: [
        react(),
        tailwindcss(),
    ],

    server: {
        port: 3000,
        open: true,

        // Chuyển tiếp đường dẫn media đến Kong
        proxy: {
            "/media": {
                target: "http://localhost:8000",
                changeOrigin: true,
            },
        },
    },
});
