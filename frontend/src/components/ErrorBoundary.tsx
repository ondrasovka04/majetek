/** React class-based error boundary. Catches unhandled render errors and shows a recovery UI. */
import { Component, ErrorInfo, ReactNode } from "react";
import { Typography, Button, Box } from "@mui/material";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, error: null };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("ErrorBoundary caught:", error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Box
          display="flex"
          flexDirection="column"
          alignItems="center"
          justifyContent="center"
          minHeight="100vh"
          gap={2}
          p={4}
        >
          <Typography variant="h5" color="error">
            Něco se pokazilo
          </Typography>
          <Typography variant="body1" color="text.secondary" textAlign="center">
            {this.state.error?.message ?? "Neočekávaná chyba."}
          </Typography>
          <Button
            variant="contained"
            onClick={() => {
              this.setState({ hasError: false, error: null });
              window.location.reload();
            }}
          >
            Obnovit stránku
          </Button>
        </Box>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
