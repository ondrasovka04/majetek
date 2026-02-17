/**
 * Root application component.
 * Sets up providers (QueryClient, MUI theme, date localization),
 * lazy-loads all pages behind authenticated routes, and wraps
 * everything in an ErrorBoundary.
 */
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import PrivateRoute from "./components/PrivateRoute";
import { lazy, Suspense, useEffect } from "react";
import { useAuthStore } from "./store/authStore";
import { Navigate } from "react-router-dom";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { cs } from "date-fns/locale";
import { ThemeProvider, CssBaseline, CircularProgress } from "@mui/material";
import { defaultTheme } from "./theme";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import ErrorBoundary from "./components/ErrorBoundary";

// Lazy-loaded pages — only the login page is loaded eagerly
import LoginPage from "./pages/LoginPage";

const CardPage = lazy(() => import("./pages/CardPage"));
const ReportPage = lazy(() => import("./pages/ReportPage"));
const InventoryOverviewPage = lazy(() => import("./pages/inventoryAudit/InventoryOverviewPage"));
const InventoryDifferencesPage = lazy(() => import("./pages/inventoryAudit/InventoryDifferencesPage"));
const LocationPage = lazy(() => import("./pages/LocationPage"));
const AssetGroupPage = lazy(() => import("./pages/AssetGroupPage"));
const ClassPage = lazy(() => import("./pages/ClassPage"));
const PurchaseMethodPage = lazy(() => import("./pages/PurchaseMethodPage"));
const WriteOffMethodPage = lazy(() => import("./pages/WriteOffMethodPage"));
const UserPage = lazy(() => import("./pages/UserPage"));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 2 * 60 * 1000, // 2 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  const attemptRehydrate = useAuthStore((s) => s.attemptRehydrate);
  const hydrated = useAuthStore((s) => s.hydrated);

  useEffect(() => {
    void attemptRehydrate();
  }, [attemptRehydrate]);

  return (
    <QueryClientProvider client={queryClient}>
      <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={cs}>
        <ThemeProvider theme={defaultTheme}>
          <CssBaseline />
          <Router>
            <ErrorBoundary>
              <Suspense
                fallback={
                  <div className="flex justify-center items-center h-screen">
                    <CircularProgress />
                  </div>
                }>
                {hydrated && (
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
                )}
              </Suspense>
            </ErrorBoundary>
          </Router>
        </ThemeProvider>
      </LocalizationProvider>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}

export default App;
