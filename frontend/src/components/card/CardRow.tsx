/** Single row in the card list table. Shows a red dot if the card has been written off. Memoized to avoid re-renders on parent state changes. */
import { memo } from "react";
import { TableRow, TableCell } from "@mui/material";
import { Card } from "../../types/card/card";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";

interface Props {
  card: Card;
  selected: boolean;
  onClick: (id: string) => void;
}

const CardRow = memo(({ card, selected, onClick }: Props) => {
  return (
    <TableRow
      hover
      selected={selected}
      onClick={() => onClick(card.id)}
      sx={{
        cursor: "pointer",
        "&:hover": { backgroundColor: "rgba(0, 0, 0, 0.04)" },
      }}>
      <TableCell sx={{ p: 0, width: "1px" }}>
        {card.writeOffDate && <FiberManualRecordIcon sx={{ fontSize: 8, mx: 0.5 }} color="error" />}
      </TableCell>
      <TableCell>{card.id}</TableCell>
      <TableCell>{card.name}</TableCell>
    </TableRow>
  );
});

CardRow.displayName = "CardRow";

export default CardRow;
