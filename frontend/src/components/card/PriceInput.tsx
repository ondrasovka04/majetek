import { TextField } from "@mui/material";
import { useState, useEffect } from "react";

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

export const PriceInput = ({ value, label, onChange }: PriceInputProps) => {
  const [displayValue, setDisplayValue] = useState<string>("");

  useEffect(() => {
    setDisplayValue(value === undefined || value === null ? "" : formatCurrency(value));
  }, [value]);

  const handleFocus = () => {
    if (value !== null) {
      setDisplayValue(value.toString().replace(".", ","));
    }
  };

  const handleBlur = () => {
    const numeric = parseFloat(displayValue.replace(",", "."));
    if (!isNaN(numeric)) {
      onChange(numeric);
      setDisplayValue(formatCurrency(numeric));
    } else {
      setDisplayValue("");
      onChange(null);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (/^[0-9]*[.,]?[0-9]*$/.test(val) || val === "") {
      setDisplayValue(val);
    }
  };

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
