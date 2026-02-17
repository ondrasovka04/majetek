/** Modal for generating a PDF report filtered by location and optional date. */
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  TextField,
  MenuItem,
  Box,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { Location } from "../../types/location";

interface LocationReportModalProps {
  open: boolean;
  onClose: () => void;
  onGenerate: () => void;
  selectedLocation: string | null;
  onLocationChange: (value: string | null) => void;
  locations: Location[];
  loading: boolean;
  selectedDate: Date | null;
  onDateChange: (date: Date | null) => void;
}

const LocationReportModal = ({
  open,
  onClose,
  onGenerate,
  selectedLocation,
  onLocationChange,
  locations,
  loading,
  selectedDate,
  onDateChange,
}: LocationReportModalProps) => {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Vyberte místnost</DialogTitle>
      <DialogContent>
        <FormControl fullWidth>
          <TextField
            sx={{ my: 2 }}
            label="Místnost"
            select
            fullWidth
            value={selectedLocation}
            onChange={(e) => {
              onLocationChange(e.target.value);
            }}
            slotProps={{
              select: {
                renderValue: (value: unknown) => {
                  const selected = value as string;
                  return selected || "(nevybráno)";
                },
              },
            }}
          >
            {locations.map((location) => (
              <MenuItem key={location.id} value={location.id}>
                <Box display="flex" gap={2} width="100%">
                  <span style={{ minWidth: 60 }}>{location.id}</span>
                  <span>{location.name}</span>
                </Box>
              </MenuItem>
            ))}
          </TextField>
          <DatePicker
            label="Stav k"
            value={selectedDate}
            onChange={onDateChange}
          />
        </FormControl>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Zrušit</Button>
        <Button
          onClick={onGenerate}
          variant="contained"
          disabled={selectedLocation === null || loading}
        >
          Zobrazit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default LocationReportModal;
