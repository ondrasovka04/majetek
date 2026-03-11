package cz.majetek.card.export;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto-fits column widths by character count, with extra space for bold headers and autofilter dropdowns.
 */
public class ColumnWidthStrategy extends AbstractColumnWidthStyleStrategy {

    private static final int MAX_WIDTH = 60;
    private static final int FILTER_DROPDOWN_CHARS = 5;
    private static final double BOLD_WIDTH_FACTOR = 1.15;

    private static final Map<Integer, Integer> FIXED_WIDTHS = Map.of(
            0, 10 // ID
    );

    private final Map<Integer, Integer> columnMaxWidth = new HashMap<>();
    private final Map<Integer, Integer> headerMinWidth = new HashMap<>();

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList,
                                  Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        int col = cell.getColumnIndex();

        Integer fixed = FIXED_WIDTHS.get(col);
        if (fixed != null) {
            writeSheetHolder.getSheet().setColumnWidth(col, fixed * 256);
            return;
        }

        int length = cellStringLength(cell);
        if (Boolean.TRUE.equals(isHead)) {
            int headLen = (int) Math.ceil(length * BOLD_WIDTH_FACTOR) + FILTER_DROPDOWN_CHARS;
            headerMinWidth.put(col, headLen);
            length = headLen;
        }

        length = Math.min(length, MAX_WIDTH);
        Integer current = columnMaxWidth.get(col);

        if (current == null || length > current) {
            columnMaxWidth.put(col, length);
            int finalWidth = Math.max(length, headerMinWidth.getOrDefault(col, 0)) + 1;
            writeSheetHolder.getSheet().setColumnWidth(col, finalWidth * 256);
        }
    }

    private int cellStringLength(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().length();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).length();
            case BOOLEAN -> cell.getBooleanCellValue() ? 4 : 5;
            default -> 0;
        };
    }
}
