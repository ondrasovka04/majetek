/** Modal for selecting an asset card via Autocomplete search and generating its PDF report. */
import { useState } from "react";
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
import { useQuery } from "@tanstack/react-query";
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

const CardReportModal = ({ open, onClose, onGenerate, loading }: CardReportModalProps) => {
  const [selectedCard, setSelectedCard] = useState<CardOption | null>(null);
  const [inputValue, setInputValue] = useState("");

  const { data: options = [], isLoading: searchLoading } = useQuery({
    queryKey: ["cardSearch", inputValue],
    queryFn: async () => {
      const results = await filterCards({ fulltextSearch: inputValue });
      results.sort((a, b) => parseInt(a.id) - parseInt(b.id));
      return results.map((card) => ({
        id: parseInt(card.id),
        label: `${String(card.id).padStart(4, "0")} \u2003${card.name}`,
      }));
    },
    enabled: open && inputValue.length > 0,
    placeholderData: (prev) => prev,
  });

  const displayOptions = inputValue ? options : selectedCard ? [selectedCard] : [];

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
          options={displayOptions}
          getOptionLabel={(option) => option.label}
          filterOptions={(x) => x}
          autoComplete
          includeInputInList
          filterSelectedOptions
          value={selectedCard}
          noOptionsText="Žádné výsledky"
          loading={searchLoading}
          loadingText="Načítání..."
          onChange={(_: React.SyntheticEvent, newValue: CardOption | null) => {
            setSelectedCard(newValue);
          }}
          onInputChange={(_, newInputValue) => {
            setInputValue(newInputValue);
          }}
          renderInput={(params) => {
            params.InputProps.endAdornment = (
              <>
                {searchLoading ? <CircularProgress color="inherit" size={20} /> : null}
                {params.InputProps.endAdornment}
              </>
            );
            return <TextField {...params} label="Hledat podle inv. čísla nebo názvu" />;
          }}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Zrušit</Button>
        <Button onClick={handleGenerate} variant="contained" disabled={!selectedCard || loading}>
          Zobrazit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default CardReportModal;
