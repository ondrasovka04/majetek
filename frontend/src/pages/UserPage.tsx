/** User settings page — password change form and admin user creation dialog. */
import { useState } from "react";
import { Typography, TextField, Button } from "@mui/material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuthStore } from "../store/authStore";
import { translateError } from "../utils/errorTranslations";
import { extractErrorMessage } from "../utils/extractErrorMessage";
import MenuBar from "../components/MenuBar";
import AdminUserDialog from "../components/AdminUserDialog";
import {
  changePasswordSchema,
  ChangePasswordFormValues,
} from "../schemas/userSchema";
import { useNotification } from "../hooks/useNotification";
import { useChangePassword } from "../hooks/queries/useAuthQueries";

const UserPage = () => {
  const user = useAuthStore((state) => state.user);
  const [adminDialogOpen, setAdminDialogOpen] = useState(false);
  const { notify, notifyError, NotificationBar } = useNotification();
  const changePasswordMutation = useChangePassword();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: "", newPassword: "" },
  });

  const onSubmit = (data: ChangePasswordFormValues) => {
    changePasswordMutation.mutate(data, {
      onSuccess: () => {
        notify("Heslo bylo úspěšně změněno.", "success");
        reset();
      },
      onError: (err: unknown) => {
        notifyError(translateError(extractErrorMessage(err)));
      },
    });
  };

  const onInvalid = () => {
    const firstError = Object.values(errors)[0];
    if (firstError?.message) {
      notifyError(String(firstError.message));
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
            {...register("currentPassword")}
            error={!!errors.currentPassword}
            helperText={errors.currentPassword?.message}
          />
          <TextField
            label="Nové heslo"
            type="password"
            fullWidth
            sx={{ mb: 2 }}
            {...register("newPassword")}
            error={!!errors.newPassword}
            helperText={errors.newPassword?.message}
          />
          <Button
            variant="contained"
            onClick={handleSubmit(onSubmit, onInvalid)}
          >
            Změnit heslo
          </Button>
        </div>

        {NotificationBar}

        <AdminUserDialog
          open={adminDialogOpen}
          onClose={() => setAdminDialogOpen(false)}
          onSuccess={(msg) => {
            notify(msg, "success");
          }}
        />


      </div>
    </div>
  );
};

export default UserPage;
