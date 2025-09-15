import { useState, useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Autocomplete,
  CircularProgress,
} from "@mui/material";
import { filterCards } from "../../services/cardService";

type CardOption = {
  id: number;
  label: string;
};

interface CardReportModalProps {
  open: boolean;
  onClose: () => void;
  onGenerate: (cardId: number) => void;
  loading: boolean;
}

const CardReportModal = ({
  open,
  onClose,
  onGenerate,
  loading,
}: CardReportModalProps) => {
  const [selectedCard, setSelectedCard] = useState<CardOption | null>(null);
  const [inputValue, setInputValue] = useState("");
  const [options, setOptions] = useState<readonly CardOption[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);

  useEffect(() => {
    if (!open) {
      setOptions([]);
      setSelectedCard(null);
      return;
    }

    let active = true;

    if (inputValue === "") {
      setOptions(selectedCard ? [selectedCard] : []);
      return;
    }

    setSearchLoading(true);
    (async () => {
      const results = await filterCards({ fulltextSearch: inputValue });
      if (active) {
        setOptions(
          results.map((card) => ({
            id: parseInt(card.id),
            label: `${String(card.id).padStart(4, "0")} \u2003${card.name}`,
          }))
        );
        setSearchLoading(false);
      }
    })();

    return () => {
      active = false;
    };
  }, [inputValue, open, selectedCard]);

  const handleGenerate = () => {
    if (selectedCard) {
      onGenerate(selectedCard.id);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Vyberte kartu majetku</DialogTitle>
      <DialogContent>
        <Autocomplete
          id="card-search-autocomplete"
          sx={{ mt: 2 }}
          options={options}
          getOptionLabel={(option) => option.label}
          filterOptions={(x) => x}
          autoComplete
          includeInputInList
          filterSelectedOptions
          value={selectedCard}
          noOptionsText="Žádné výsledky"
          loading={searchLoading}
          loadingText="Načítání..."
          onChange={(_: any, newValue: CardOption | null) => {
            setOptions(newValue ? [newValue, ...options] : options);
            setSelectedCard(newValue);
          }}
          onInputChange={(_, newInputValue) => {
            setInputValue(newInputValue);
          }}
          renderInput={(params) => {
            params.InputProps.endAdornment = (
              <>
                {searchLoading ? (
                  <CircularProgress color="inherit" size={20} />
                ) : null}
                {params.InputProps.endAdornment}
              </>
            );
            return (
              <TextField
                {...params}
                label="Hledat podle inv. čísla nebo názvu"
              />
            );
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Zrušit</Button>
        <Button
          onClick={handleGenerate}
          variant="contained"
          disabled={!selectedCard || loading}
        >
          Zobrazit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CardReportModal;
