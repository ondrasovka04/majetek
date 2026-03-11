package cz.majetek.card.export;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColumnWidthStrategyTest {

    private ColumnWidthStrategy strategy;

    @Mock
    private WriteSheetHolder writeSheetHolder;
    @Mock
    private Sheet sheet;
    @Mock
    private Cell cell;
    @Mock
    private Head head;

    @BeforeEach
    void setUp() {
        strategy = new ColumnWidthStrategy();
        lenient().when(writeSheetHolder.getSheet()).thenReturn(sheet);
        lenient().when(cell.getCellType()).thenReturn(CellType.STRING);
    }

    @Test
    void fixedWidthColumn_setsExactWidth() {
        when(cell.getColumnIndex()).thenReturn(0);

        invokeSetColumnWidth(true);

        verify(sheet).setColumnWidth(0, 10 * 256);
    }

    @Test
    void headerColumn_addsSpaceForBoldAndFilter() {
        when(cell.getColumnIndex()).thenReturn(1);
        when(cell.getStringCellValue()).thenReturn("Název");

        invokeSetColumnWidth(true);

        // expected: ceil(5 * 1.15) + 5 = 6 + 5 = 11, +1 padding = 12
        verify(sheet).setColumnWidth(1, 12 * 256);
    }

    @Test
    void contentWiderThanHeader_usesContentWidth() {
        when(cell.getColumnIndex()).thenReturn(2);

        // first call: header "AB" → ceil(2*1.15)+5 = 3+5 = 8
        when(cell.getStringCellValue()).thenReturn("AB");
        invokeSetColumnWidth(true);

        // second call: content with 20 chars → 20 > 8 → uses 20+1 = 21
        when(cell.getStringCellValue()).thenReturn("12345678901234567890");
        invokeSetColumnWidth(false);

        verify(sheet).setColumnWidth(2, 21 * 256);
    }

    @Test
    void contentNarrowerThanHeader_usesHeaderMinWidth() {
        when(cell.getColumnIndex()).thenReturn(3);

        // header "Zodpovědná osoba" (16 chars) → ceil(16*1.15)+5 = 19+5 = 24
        when(cell.getStringCellValue()).thenReturn("Zodpovědná osoba");
        invokeSetColumnWidth(true);

        // content "Jan" (3 chars) → 3 < 24 → keeps header width 24+1 = 25
        when(cell.getStringCellValue()).thenReturn("Jan");
        invokeSetColumnWidth(false);

        verify(sheet, never()).setColumnWidth(3, 4 * 256);
        // header width should remain
        verify(sheet).setColumnWidth(3, 25 * 256);
    }

    @Test
    void narrowerContent_doesNotShrinkColumn() {
        when(cell.getColumnIndex()).thenReturn(4);

        // first: long content
        when(cell.getStringCellValue()).thenReturn("Borovičková Lucie");
        invokeSetColumnWidth(false);

        // second: shorter content — should NOT update
        when(cell.getStringCellValue()).thenReturn("Jan");
        invokeSetColumnWidth(false);

        // setColumnWidth should be called only once (for the longer content)
        verify(sheet, times(1)).setColumnWidth(eq(4), anyInt());
    }

    @Test
    void numericCell_calculatesLengthFromValue() {
        when(cell.getColumnIndex()).thenReturn(5);
        when(cell.getCellType()).thenReturn(CellType.NUMERIC);
        when(cell.getNumericCellValue()).thenReturn(12345.67);

        invokeSetColumnWidth(false);

        int expectedLen = String.valueOf(12345.67).length(); // 8
        verify(sheet).setColumnWidth(5, (expectedLen + 1) * 256);
    }

    @Test
    void veryLongContent_isCappedAtMaxWidth() {
        when(cell.getColumnIndex()).thenReturn(6);
        when(cell.getStringCellValue()).thenReturn("A".repeat(100));

        invokeSetColumnWidth(false);

        // capped at MAX_WIDTH (60) + 1 padding = 61
        verify(sheet).setColumnWidth(6, 61 * 256);
    }

    private void invokeSetColumnWidth(boolean isHead) {
        strategy.setColumnWidth(writeSheetHolder, List.of(), cell, head, 0, isHead);
    }
}
