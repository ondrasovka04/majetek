import { TextField } from "@mui/material";
import { useState, useCallback } from "react";

interface PriceInputProps {
  value: number | null;
  onChange: (value: number | null) => void;
  label: string;
}

const formatCurrency = (value: number) =>
  value.toLocaleString("cs-CZ", {
    style: "currency",
    currency: "CZK",
    minimumFractionDigits: 2,
  });

/**
 * Currency input that shows formatted value when blurred, raw number when focused.
 * Derives display value from props instead of syncing via useEffect.
 */
export const PriceInput = ({ value, label, onChange }: PriceInputProps) => {
  const [focused, setFocused] = useState(false);
  const [editingValue, setEditingValue] = useState("");

  // Derive display value: when focused, show raw editing text; when blurred, format from prop
  const displayValue = focused ? editingValue : value != null ? formatCurrency(value) : "";

  const handleFocus = useCallback(() => {
    setFocused(true);
    setEditingValue(value != null ? value.toString().replace(".", ",") : "");
  }, [value]);

  const handleBlur = useCallback(() => {
    setFocused(false);
    const numeric = parseFloat(editingValue.replace(",", "."));
    onChange(!isNaN(numeric) ? numeric : null);
  }, [editingValue, onChange]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (/^[0-9]*[.,]?[0-9]*$/.test(val) || val === "") {
      setEditingValue(val);
    }
  }, []);

  return (
    <TextField
      label={label}
      fullWidth
      value={displayValue}
      onFocus={handleFocus}
      onBlur={handleBlur}
      onChange={handleChange}
      type="text"
      slotProps={{
        htmlInput: { inputMode: "decimal", pattern: "[0-9]*[.,]?[0-9]*" },
      }}
    />
  );
};
