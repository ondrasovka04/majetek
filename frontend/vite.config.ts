import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/test/setup.ts",
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
    rollupOptions: {
      output: {
        manualChunks: {
          "vendor-react": ["react", "react-dom", "react-router-dom"],
          "vendor-mui": ["@mui/material", "@mui/icons-material", "@mui/x-date-pickers", "@mui/x-data-grid"],
          "vendor-pdf": ["@react-pdf-viewer/core", "@react-pdf-viewer/default-layout"],
          "vendor-query": ["@tanstack/react-query"],
        },
      },
    },
  },
});
