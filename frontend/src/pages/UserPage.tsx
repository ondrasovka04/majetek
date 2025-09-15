import { useState } from "react";
import { Typography, TextField, Button, Alert, Snackbar } from "@mui/material";
import { useAuthStore } from "../store/authStore";
import { changePassword } from "../services/authService";
import { translateError } from "../utils/errorTranslations";
import MenuBar from "../components/MenuBar";
import AdminUserDialog from "../components/AdminUserDialog";

const UserPage = () => {
  const user = useAuthStore((state) => state.user);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [adminDialogOpen, setAdminDialogOpen] = useState(false);

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const handleChangePassword = async () => {
    if (!currentPassword.trim() || !newPassword.trim()) {
      setError("Obě pole musí být vyplněna.");
      setSnackbarOpen(true);
      return;
    }
    try {
      await changePassword({ currentPassword, newPassword });
      setSuccess("Heslo bylo úspěšně změněno.");
      setError("");
      setSnackbarOpen(true);
      setCurrentPassword("");
      setNewPassword("");
    } catch (err: any) {
      const backendMessage = err.response?.data?.message;
      setError(translateError(backendMessage));
      setSuccess("");
      setSnackbarOpen(true);
    }
  };

  return (
    <div>
      <MenuBar />
      <div className="p-6">
        <div className="flex justify-between items-center mb-4">
          <Typography variant="h4" className="mb-6">
            {user?.name}
          </Typography>
          {user?.role === "ADMIN" && (
            <>
              <Button
                variant="contained"
                onClick={() => setAdminDialogOpen(true)}
              >
                Vytvořit nového uživatele
              </Button>
            </>
          )}
        </div>
        <hr className="my-4 border-gray-300" />
        <div className="max-w-xl mx-auto">
          <Typography variant="h6" sx={{ mb: 3 }}>
            Změna hesla
          </Typography>
          <TextField
            label="Aktuální heslo"
            type="password"
            fullWidth
            sx={{ mb: 2 }}
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
          />
          <TextField
            label="Nové heslo"
            type="password"
            fullWidth
            sx={{ mb: 2 }}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
          <Button
            variant="contained"
            onClick={handleChangePassword}
          >
            Změnit heslo
          </Button>
        </div>

        <AdminUserDialog
          open={adminDialogOpen}
          onClose={() => setAdminDialogOpen(false)}
          onSuccess={(msg) => {
            setSuccess(msg);
            setError("");
            setSnackbarOpen(true);
          }}
        />

        <Snackbar
          open={snackbarOpen}
          autoHideDuration={6000}
          onClose={() => setSnackbarOpen(false)}
          anchorOrigin={{ vertical: "top", horizontal: "center" }}
        >
          <Alert
            onClose={() => setSnackbarOpen(false)}
            severity={error ? "error" : "success"}
            variant="filled"
            sx={{ width: "100%" }}
          >
            {error ? error : success}
          </Alert>
        </Snackbar>
      </div>
    </div>
  );
};

export default UserPage;
