/** Reusable notification hook. Returns `notify` (success), `notifyError` (error), and a `NotificationBar` component to render. */
import { useState, useCallback } from "react";
import { Snackbar, Alert } from "@mui/material";

type Severity = "success" | "error";

interface NotificationState {
  open: boolean;
  message: string;
  severity: Severity;
}

export function useNotification() {
  const [state, setState] = useState<NotificationState>({
    open: false,
    message: "",
    severity: "success",
  });

  const notify = useCallback((message: string, severity: Severity = "success") => {
    setState({ open: true, message, severity });
  }, []);

  const notifyError = useCallback((message: string) => {
    setState({ open: true, message, severity: "error" });
  }, []);

  const close = useCallback(() => {
    setState((prev) => ({ ...prev, open: false }));
  }, []);

  const NotificationBar = (
    <Snackbar
      open={state.open}
      autoHideDuration={6000}
      onClose={close}
      anchorOrigin={{ vertical: "top", horizontal: "center" }}>
      <Alert onClose={close} severity={state.severity} variant="filled" sx={{ width: "100%" }}>
        {state.message}
      </Alert>
    </Snackbar>
  );

  return { notify, notifyError, NotificationBar };
}
