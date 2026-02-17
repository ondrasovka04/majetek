/** Sortable table header cell with ascending/descending indicator. */
import { TableCell } from "@mui/material";
import { InventoryAuditEntry } from "../../types/inventoryAudit/inventoryAudit";

interface SortableHeaderCellProps {
  label: string;
  sortKey: keyof InventoryAuditEntry;
  activeSortKey: keyof InventoryAuditEntry;
  sortDirection: "asc" | "desc";
  onSort: (key: keyof InventoryAuditEntry) => void;
  style?: React.CSSProperties;
}

const SortableHeaderCell = ({
  label,
  sortKey,
  activeSortKey,
  sortDirection,
  onSort,
  style,
}: SortableHeaderCellProps) => (
  <TableCell
    style={{ ...style, cursor: "pointer", fontWeight: "bold" }}
    onClick={() => onSort(sortKey)}
  >
    {label} {activeSortKey === sortKey ? (sortDirection === "asc" ? "▲" : "▼") : ""}
  </TableCell>
);

export default SortableHeaderCell;
