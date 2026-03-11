package cz.majetek.card.export;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import cz.majetek.card.dto.CardExportDto;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Arrays;

/**
 * Freezes the first row and enables auto-filter on all columns.
 * Column count is derived from @ExcelProperty fields in CardExportDto.
 */
public class FreezeFirstRowHandler implements SheetWriteHandler {

    private static final int COLUMN_COUNT = (int) Arrays.stream(CardExportDto.class.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(ExcelProperty.class))
            .count();

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        var sheet = writeSheetHolder.getSheet();
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, COLUMN_COUNT - 1));
    }
}
