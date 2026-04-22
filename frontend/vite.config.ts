import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "node:path";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  server: {
    port: 3000,
    host: !!process.env.VITE_HOST,
    allowedHosts: process.env.VITE_ALLOWED_HOSTS?.split(",").map((h) => h.trim()).filter(Boolean) ?? [],
  },
  build: {
    outDir: "build",
    sourcemap: false,
    chunkSizeWarningLimit: 1000,
    rolldownOptions: {
      output: {
        manualChunks(id: string) {
          if (id.includes("react-dom") || id.includes("react-router") || id.includes("/react/")) return "vendor-react";
          if (id.includes("@mui/")) return "vendor-mui";
          if (id.includes("@react-pdf-viewer/") || id.includes("pdfjs-dist")) return "vendor-pdf";
          if (id.includes("@tanstack/react-query")) return "vendor-query";
        },
      },
    },
  },
});
