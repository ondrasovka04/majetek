/** Admin dialog for registering a new user account. Uses Zod validation and useRegisterUser mutation. */
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button } from "@mui/material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { registerUserSchema, RegisterUserFormValues, RegisterUserFormOutput } from "@/schemas/userSchema";
import { translateError } from "@/utils/errorTranslations";
import { extractErrorMessage } from "@/utils/extractErrorMessage";
import { useRegisterUser } from "@/hooks/queries/useAuthQueries";

interface Props {
  open: boolean;
  onClose: () => void;
  onSuccess: (message: string) => void;
}

const AdminUserDialog = ({ open, onClose, onSuccess }: Props) => {
  const registerMutation = useRegisterUser();
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm<RegisterUserFormValues, unknown, RegisterUserFormOutput>({
    resolver: zodResolver(registerUserSchema),
    defaultValues: { id: "", name: "", username: "", password: "" },
  });

  const onSubmit = (data: RegisterUserFormOutput) => {
    registerMutation.mutate(data, {
      onSuccess: () => {
        onSuccess(`Uživatel ${data.username} byl úspěšně vytvořen.`);
        reset();
        onClose();
      },
      onError: (err: unknown) => {
        setError("root", {
          message: translateError(extractErrorMessage(err)),
        });
      },
    });
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose}>
      <DialogTitle>Vytvořit nového uživatele</DialogTitle>
      <DialogContent>
        <TextField
          label="ID"
          fullWidth
          sx={{ mb: 2, mt: 1 }}
          {...register("id")}
          error={!!errors.id}
          helperText={errors.id?.message}
        />
        <TextField
          label="Uživatelské jméno"
          fullWidth
          sx={{ mb: 2 }}
          {...register("username")}
          error={!!errors.username}
          helperText={errors.username?.message}
        />
        <TextField label="Jméno" fullWidth sx={{ mb: 2 }} {...register("name")} />
        <TextField
          label="Heslo"
          type="password"
          fullWidth
          sx={{ mb: 2 }}
          {...register("password")}
          error={!!errors.password}
          helperText={errors.password?.message}
        />
        {errors.root && <span style={{ color: "#d32f2f", fontSize: "0.875rem" }}>{errors.root.message}</span>}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Zrušit</Button>
        <Button onClick={handleSubmit(onSubmit)} variant="contained">
          Vytvořit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default AdminUserDialog;
