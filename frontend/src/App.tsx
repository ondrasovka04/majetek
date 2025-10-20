import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import CardPage from "./pages/CardPage";
import PrivateRoute from "./components/PrivateRoute";
import { useEffect } from "react";
import { useAuthStore } from "./store/authStore";
import { User } from "./types/auth";
import { Navigate } from "react-router-dom";
import LocationPage from "./pages/LocationPage";
import UserPage from "./pages/UserPage";
import AssetGroupPage from "./pages/AssetGroupPage";
import ClassPage from "./pages/ClassPage";
import PurchaseMethodPage from "./pages/PurchaseMethodPage";
import WriteOffMethodPage from "./pages/WriteOffMethodPage";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { cs } from "date-fns/locale";
import { ThemeProvider, CssBaseline } from "@mui/material";
import { defaultTheme } from "./theme";
import ReportPage from "./pages/ReportPage";
import InventoryOverviewPage from "./pages/inventoryAudit/InventoryOverviewPage";
import InventoryDifferencesPage from "./pages/inventoryAudit/InventoryDifferencesPage";

function App() {
  const setAuth = useAuthStore((state) => state.setAuth);
  const logout = useAuthStore((state) => state.logout);

  useEffect(() => {
    const tokenFromStorage = localStorage.getItem("token");
    const userJson = localStorage.getItem("user");
    const expiresInStorage = localStorage.getItem("expiresIn");

    if (tokenFromStorage && userJson && expiresInStorage) {
      const expiryDate = new Date(expiresInStorage);
      if (expiryDate <= new Date()) {
        logout();
      } else {
        const user: User = JSON.parse(userJson);
        setAuth(user, tokenFromStorage, expiresInStorage);
      }
    }
  }, [setAuth, logout]);

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={cs}>
      <ThemeProvider theme={defaultTheme}>
        <CssBaseline />
        <Router>
          <Routes>
            <Route path="/" element={<LoginPage />} />
            <Route
              path="/card"
              element={
                <PrivateRoute>
                  <CardPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/report"
              element={
                <PrivateRoute>
                  <ReportPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/inventory-audit/overview"
              element={
                <PrivateRoute>
                  <InventoryOverviewPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/inventory-audit/differences"
              element={
                <PrivateRoute>
                  <InventoryDifferencesPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/location"
              element={
                <PrivateRoute>
                  <LocationPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/asset-group"
              element={
                <PrivateRoute>
                  <AssetGroupPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/class"
              element={
                <PrivateRoute>
                  <ClassPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/purchase-method"
              element={
                <PrivateRoute>
                  <PurchaseMethodPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/write-off-method"
              element={
                <PrivateRoute>
                  <WriteOffMethodPage />
                </PrivateRoute>
              }
            />
            <Route
              path="/user"
              element={
                <PrivateRoute>
                  <UserPage />
                </PrivateRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </ThemeProvider>
    </LocalizationProvider>
  );
}

export default App;
