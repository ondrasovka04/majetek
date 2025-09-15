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

type Group = {
  id: string;
  name: string;
};

interface GroupReportModalProps {
  open: boolean;
  onClose: () => void;
  onGenerate: () => void;
  selectedGroup: string | null;
  onGroupChange: (value: string) => void;
  groups: Group[];
  loading: boolean;
  selectedDate: Date | null;
  onDateChange: (date: Date | null) => void;
}

const GroupReportModal = ({
  open,
  onClose,
  onGenerate,
  selectedGroup,
  onGroupChange,
  groups,
  loading,
  selectedDate,
  onDateChange,
}: GroupReportModalProps) => {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Vyberte skupinu</DialogTitle>
      <DialogContent>
        <FormControl fullWidth>
          <TextField
            sx={{ my: 2 }}
            label="Skupina"
            select
            fullWidth
            value={selectedGroup}
            onChange={(e) => {
              onGroupChange(e.target.value);
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
            <MenuItem value="">Všechny skupiny</MenuItem>
            {groups.map((group) => (
              <MenuItem key={group.id} value={group.id}>
                <Box display="flex" gap={2} width="100%">
                  <span style={{ minWidth: 60 }}>{group.id}</span>
                  <span>{group.name}</span>
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
          disabled={selectedGroup === null || loading}
        >
          Zobrazit
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default GroupReportModal;
