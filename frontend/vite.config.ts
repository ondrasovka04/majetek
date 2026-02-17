import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    open: false,
  },
  build: {
    outDir: "build",
    sourcemap: false,
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks: {
          "vendor-react": ["react", "react-dom", "react-router-dom"],
          "vendor-mui": [
            "@mui/material",
            "@mui/icons-material",
            "@mui/x-date-pickers",
            "@mui/x-data-grid",
          ],
          "vendor-pdf": [
            "@react-pdf-viewer/core",
            "@react-pdf-viewer/default-layout",
          ],
          "vendor-query": ["@tanstack/react-query"],
        },
      },
    },
  },
});
