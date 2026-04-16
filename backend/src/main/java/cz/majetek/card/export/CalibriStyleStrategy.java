package cz.majetek.card.export;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;

/**
 * Sets Calibri font for both header (bold, no wrap) and content cells.
 */
public class CalibriStyleStrategy extends HorizontalCellStyleStrategy {

    public CalibriStyleStrategy() {
        super(headStyle(), contentStyle());
    }

    private static WriteCellStyle headStyle() {
        WriteCellStyle style = new WriteCellStyle();
        WriteFont font = new WriteFont();
        font.setFontName("Calibri");
        font.setBold(true);
        style.setWriteFont(font);
        style.setWrapped(false);
        return style;
    }

    private static WriteCellStyle contentStyle() {
        WriteCellStyle style = new WriteCellStyle();
        WriteFont font = new WriteFont();
        font.setFontName("Calibri");
        style.setWriteFont(font);
        return style;
    }
}
