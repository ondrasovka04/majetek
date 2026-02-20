import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, ThemeProvider } from "@mui/material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMemo } from "react";
import { DictionaryEntity } from "../../types/dictionary";
import { createDictionarySchema, DictionaryFormValues, DictionaryFormOutput } from "../../schemas/dictionarySchema";
import { defaultTheme } from "../../theme";

interface Props<T extends DictionaryEntity> {
  readonly open: boolean;
  readonly onClose: () => void;
  readonly onCreate: (item: T) => Promise<void>;
  readonly title: string;
  /** Maximum length of the ID field (default: 20) */
  readonly maxIdLength?: number;
}

export default function AddDictionaryDialog<T extends DictionaryEntity>({
  open,
  onClose,
  onCreate,
  title,
  maxIdLength = 20,
}: Props<T>) {
  const schema = useMemo(() => createDictionarySchema(maxIdLength), [maxIdLength]);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<DictionaryFormValues, unknown, DictionaryFormOutput>({
    resolver: zodResolver(schema),
    defaultValues: { id: "", name: "" },
  });

  const onSubmit = async (data: DictionaryFormOutput) => {
    try {
      await onCreate({ id: data.id, name: data.name } as T);
      reset();
      onClose();
    } catch {
      // Error is handled by the caller — keep dialog open so user can fix input
    }
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <ThemeProvider theme={defaultTheme}>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="ID"
            fullWidth
            {...register("id")}
            error={!!errors.id}
            helperText={errors.id?.message}
          />
          <TextField
            margin="dense"
            label="Název"
            fullWidth
            {...register("name")}
            error={!!errors.name}
            helperText={errors.name?.message}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Zrušit</Button>
          <Button onClick={handleSubmit(onSubmit)} variant="contained">
            Vytvořit
          </Button>
        </DialogActions>
      </Dialog>
    </ThemeProvider>
  );
}
