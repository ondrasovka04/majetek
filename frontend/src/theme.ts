/**
 * MUI theme configuration.
 * - `dictionaryTheme`: compact table styling for dictionary pages.
 * - `defaultTheme`: standard theme with autocomplete off by default.
 */
import { createTheme } from "@mui/material/styles";

export const dictionaryTheme = createTheme({
  components: {
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: "4px 8px",
          fontSize: "0.85rem",
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          marginTop: "2px",
          marginBottom: "2px",
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        input: {
          fontSize: "0.875rem",
          padding: "4px 6px",
        },
      },
    },
  },
});

export const defaultTheme = createTheme({
  components: {
    MuiTextField: {
      defaultProps: {
        autoComplete: "off",
      },
    },
    MuiInputBase: {
      defaultProps: {
        autoComplete: "off",
      },
    },
  },
});

