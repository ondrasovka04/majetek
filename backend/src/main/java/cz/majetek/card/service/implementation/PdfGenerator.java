package cz.majetek.card.service.implementation;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import cz.majetek.entity.AssetGroup;
import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import cz.majetek.exception.card.CardException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public final class PdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat NUMBER_FORMAT;
    private static final String FONT_PATH = "fonts/calibri.ttf";
    private static final float BORDER_WIDTH = 2;
    private static final String NAME = "Název";
    private static final String GROUP = "Skupina";
    private static final String LOCATION = "Umístění";
    private static final String CLASS = "Třída";

    static {

        NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.forLanguageTag("cs-CZ"));
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(2);
    }

    private PdfGenerator() {
    }

    public static byte[] generateGroupReport(AssetGroup group, List<Card> cards, LocalDate asOfDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = createStyledDocument(baos);
            addReportHeader(doc, GROUP + " " + group.getId() + " (" + group.getName() + ")", asOfDate);

            float[] columnWidths = new float[]{5, 10, 51, 8, 13, 13};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
            addAssetTableHeaders(table, LOCATION, NAME);

            AssetTotals totals = addAssetTableRows(table, cards,
                    new Column(c -> c.getLocation().getId(), TextAlignment.RIGHT, 10),
                    new Column(Card::getName, TextAlignment.LEFT, 0));
            doc.add(table);
            addAssetTableSummary(doc, totals, columnWidths);

            doc.close();
            return addPageNumbers(baos);
        } catch (IOException e) {
            throw new CardException("Error while generating group report PDF");
        }
    }

    public static byte[] generateAllGroupsReport(List<Pair<String, Double>> groupTotals, LocalDate asOfDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = createStyledDocument(baos);
            addReportHeader(doc, "Souhrnný přehled skupin", asOfDate);

            Table table = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            table.addHeaderCell(createHeaderCell(GROUP, TextAlignment.LEFT));
            table.addHeaderCell(createHeaderCell("Celk. cena (Kč)", TextAlignment.RIGHT));

            double grandTotal = 0.0;
            for (Pair<String, Double> pair : groupTotals) {
                table.addCell(createDataCell(pair.getLeft()));
                table.addCell(createDataCell(formatPrice(pair.getRight()), TextAlignment.RIGHT));
                grandTotal += pair.getRight();
            }

            doc.add(table);
            doc.add(new LineSeparator(new SolidLine()).setMarginTop(5).setMarginBottom(5));

            Table summary = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
            summary.addCell(createTotalCell());
            summary.addCell(createTotalCell(formatPrice(grandTotal), TextAlignment.RIGHT));
            doc.add(summary);

            doc.close();
            return addPageNumbers(baos);
        } catch (IOException e) {
            throw new CardException("Error while generating group report PDF");
        }
    }

    public static byte[] generateLocationReport(Location location, List<Card> cards, LocalDate asOfDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = createStyledDocument(baos);
            String subtitle = String.format("Místnost %s%s", location.getId(), (location.getName() != null ? " (" + location.getName() + ")" : ""));
            addReportHeader(doc, "Inventární soupis majetku", subtitle, asOfDate);

            float[] columnWidths = new float[]{6, 50, 10, 8, 13, 13};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
            addAssetTableHeaders(table, NAME, GROUP);

            AssetTotals totals = addAssetTableRows(table, cards,
                    new Column(Card::getName, TextAlignment.LEFT, 0),
                    new Column(c -> c.getGroup() != null ? c.getGroup().getId() : "", TextAlignment.LEFT, 0));
            doc.add(table);
            addAssetTableSummary(doc, totals, columnWidths);

            doc.close();
            return addPageNumbers(baos);
        } catch (IOException e) {
            throw new CardException("Error while generating location report PDF");
        }
    }

    public static byte[] generateCardReport(Card card) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = createStyledDocument(baos);

            Table topRow = new Table(1).useAllAvailableWidth();
            topRow.setBorder(new SolidBorder(BORDER_WIDTH));
            topRow.setBorderBottom(Border.NO_BORDER);

            Cell invNumberCell = new Cell().setBorder(Border.NO_BORDER);
            Table invNumberTable = new Table(2).setHorizontalAlignment(HorizontalAlignment.CENTER);
            invNumberTable.addCell(createLabelCell("Inventární číslo"));
            invNumberTable.addCell(createValueCell(String.format("%04d", card.getId())));
            invNumberCell.add(invNumberTable);
            topRow.addCell(invNumberCell);

            doc.add(topRow);

            Table details = new Table(UnitValue.createPercentArray(new float[]{12, 10, 78})).useAllAvailableWidth();
            details.setFixedLayout();
            details.setBorder(new SolidBorder(BORDER_WIDTH));
            details.setBorderBottom(Border.NO_BORDER);

            details.addCell(createLabelCell(NAME));
            details.addCell(createValueCell(card.getName(), 2));

            details.addCell(createLabelCell(LOCATION));
            details.addCell(createValueCell(card.getLocation().getId()));
            details.addCell(createValueCell(Objects.toString(card.getLocation().getName(), "")));

            details.addCell(createLabelCell(CLASS));
            details.addCell(createValueCell(card.getAssetClass().getId()));
            details.addCell(createValueCell(Objects.toString(card.getAssetClass().getName(), "")));

            details.addCell(createLabelCell(GROUP));
            details.addCell(createValueCell(card.getGroup() != null ? card.getGroup().getId() : ""));
            details.addCell(createValueCell(card.getGroup() != null ? Objects.toString(card.getGroup().getName(), "") : ""));

            doc.add(details);

            Table pricing = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 15, 15, 25})).useAllAvailableWidth();
            pricing.setBorder(new SolidBorder(BORDER_WIDTH));
            pricing.setBorderBottom(Border.NO_BORDER);
            pricing.setBorderTop(new SolidBorder(0.5f));

            pricing.addCell(createLabelCell("Cena ks"));
            pricing.addCell(createValueCell(formatPrice(card.getPrice())));
            pricing.addCell(createLabelCell("Počet kusů"));
            pricing.addCell(createValueCell(String.valueOf(card.getQuantity())));
            pricing.addCell(createLabelCell("Celková cena"));
            pricing.addCell(createValueCell(formatPrice(card.getPrice().multiply(card.getQuantity()))));


            pricing.addCell(createLabelCell("Datum pořízení"));
            pricing.addCell(createValueCell(formatDate(card.getPurchaseDate())));
            pricing.addCell(createLabelCell("Způsob pořízení"));
            pricing.addCell(createValueCell(card.getPurchaseMethod() == null ? "" : card.getPurchaseMethod().getName()));
            pricing.addCell(createLabelCell("Výr. číslo"));
            pricing.addCell(createValueCell(card.getSerialNumber()));

            pricing.addCell(createLabelCell("Datum vyřazení"));
            pricing.addCell(createValueCell(formatDate(card.getWriteOffDate())));
            pricing.addCell(createLabelCell("Způsob vyřazení"));
            pricing.addCell(createValueCell(card.getWriteOffMethod() == null ? "" : card.getWriteOffMethod().getName()));
            pricing.addCell(createLabelCell("Číslo FA"));
            pricing.addCell(createValueCell(card.getPurchaseInvoice()));

            doc.add(pricing);

            Table bottomSection = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            bottomSection.setBorder(new SolidBorder(BORDER_WIDTH));

            Table userSection = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            userSection.addCell(createLabelCell("Vyhotovil"));
            userSection.addCell(createValueCell(card.getCreatedBy().getName()));
            userSection.addCell(createLabelCell("Zodpovídá"));
            userSection.addCell(createValueCell(card.getResponsiblePerson() != null ? card.getResponsiblePerson().getName() : ""));

            bottomSection.addCell(userSection);

            Cell noteCell = new Cell().setBorder(Border.NO_BORDER).setBorderLeft(new SolidBorder(0.5f));
            noteCell.add(createLabelCell("Poznámka"));
            noteCell.add(createValueCell(card.getNote()));
            bottomSection.addCell(noteCell);

            doc.add(bottomSection);

            doc.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new CardException("Error while generating card report PDF");
        }
    }

    private static PdfFont loadFont() throws IOException {
        try (InputStream fontStream = PdfGenerator.class.getClassLoader().getResourceAsStream(FONT_PATH)) {
            if (fontStream == null) {
                throw new IOException("Zdroj s fontem nebyl nalezen: " + FONT_PATH);
            }
            return PdfFontFactory.createFont(fontStream.readAllBytes(), PdfEncodings.IDENTITY_H);
        }
    }

    private static Document createStyledDocument(ByteArrayOutputStream baos) throws IOException {
        Document document = new Document(new PdfDocument(new PdfWriter(baos)), PageSize.A4);
        document.setFont(loadFont());
        document.setMargins(20, 20, 30, 20);
        return document;
    }

    private static void addReportHeader(Document doc, String title, LocalDate asOfDate) {
        addReportHeader(doc, title, null, asOfDate);
    }

    private static void addReportHeader(Document doc, String title, String subtitle, LocalDate asOfDate) {
        doc.add(new Paragraph(title).setBold().setFontSize(16));
        if (subtitle != null && !subtitle.isEmpty()) {
            doc.add(new Paragraph(subtitle).setFontSize(14));
        }
        doc.add(new Paragraph("Stav k: " + formatDate(asOfDate))
                .setFontSize(11)
                .setPaddingTop(subtitle == null ? 10 : 2)
                .setPaddingBottom(8));
    }

    private static void addAssetTableHeaders(Table table, String secondHeader, String thirdHeader) {
        table.addCell(createHeaderCell("Inv.č.", TextAlignment.LEFT));
        table.addCell(createHeaderCell(secondHeader, TextAlignment.LEFT));
        table.addCell(createHeaderCell(thirdHeader, TextAlignment.LEFT));
        table.addCell(createHeaderCell("Počet", TextAlignment.RIGHT));
        table.addCell(createHeaderCell("Cena ks", TextAlignment.RIGHT));
        table.addCell(createHeaderCell("Celk.cena", TextAlignment.RIGHT));
    }

    private static AssetTotals addAssetTableRows(Table table, List<Card> cards, Column secondColumn, Column thirdColumn) {
        int totalQty = 0;
        double totalSum = 0.0;

        for (Card card : cards) {
            double price = card.getPrice() != null ? card.getPrice().doubleValue() : 0;
            int quantity = card.getQuantity() != null ? card.getQuantity().intValue() : 1;
            double total = price * quantity;

            table.addCell(createDataCell(String.format("%04d", card.getId())));
            table.addCell(createDataCell(secondColumn.extractor.apply(card), secondColumn.alignment, secondColumn.marginRight));
            table.addCell(createDataCell(thirdColumn.extractor.apply(card), thirdColumn.alignment, thirdColumn.marginRight));
            table.addCell(createDataCell(String.valueOf(quantity), TextAlignment.RIGHT));
            table.addCell(createDataCell(formatPrice(price), TextAlignment.RIGHT));
            table.addCell(createDataCell(formatPrice(total), TextAlignment.RIGHT));

            totalQty += quantity;
            totalSum += total;
        }
        return new AssetTotals(totalQty, totalSum);
    }

    private static void addAssetTableSummary(Document doc, AssetTotals totals, float[] columnWidths) {
        doc.add(new LineSeparator(new SolidLine()).setMarginTop(5).setMarginBottom(5));
        Table summaryTable = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
        summaryTable.addCell(createTotalCell());
        summaryTable.addCell(createEmptyCell());
        summaryTable.addCell(createEmptyCell());
        summaryTable.addCell(createTotalCell(String.valueOf(totals.quantity()), TextAlignment.RIGHT));
        summaryTable.addCell(createEmptyCell());
        summaryTable.addCell(createTotalCell(formatPrice(totals.sum()), TextAlignment.RIGHT));
        doc.add(summaryTable);
    }

    private static Cell createHeaderCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontSize(11))
                .setTextAlignment(alignment)
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(1));
    }

    private static Cell createDataCell(String text) {
        return createDataCell(text, TextAlignment.LEFT);
    }

    private static Cell createDataCell(String text, TextAlignment alignment) {
        return createDataCell(text, alignment, 0);
    }

    private static Cell createDataCell(String text, TextAlignment alignment, float marginRight) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFontSize(11)
                        .setMultipliedLeading(0.75f)
                        .setMarginRight(marginRight))
                .setTextAlignment(alignment)
                .setBorder(Border.NO_BORDER);
    }

    private static Cell createTotalCell() {
        return createTotalCell("Celkem:", TextAlignment.LEFT);
    }

    private static Cell createTotalCell(String text, TextAlignment alignment) {
        Paragraph p = new Paragraph(text).setBold().setFontSize(11).setMultipliedLeading(0.75f);
        return new Cell().add(p).setTextAlignment(alignment).setBorder(Border.NO_BORDER);
    }

    private static Cell createEmptyCell() {
        return new Cell().setBorder(Border.NO_BORDER);
    }

    private static Cell createLabelCell(String text) {
        return new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph(text + ":").setBold().setFontSize(10));
    }

    private static Cell createValueCell(String text) {
        return createValueCell(text, 1);
    }

    private static Cell createValueCell(String text, int colspan) {
        return new Cell(1, colspan).setBorder(Border.NO_BORDER)
                .add(new Paragraph(text != null ? text : "").setFontSize(10));
    }

    private static String formatDate(LocalDate date) {
        return date != null ? DATE_FORMATTER.format(date) : "";
    }

    private static String formatPrice(BigDecimal price) {
        return price != null ? formatPrice(price.doubleValue()) : "0,00";
    }

    private static String formatPrice(double price) {
        return NUMBER_FORMAT.format(price);
    }

    private static byte[] addPageNumbers(ByteArrayOutputStream baos) throws IOException {
        ByteArrayOutputStream finalOutput = new ByteArrayOutputStream();
        try (PdfDocument resultDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())), new PdfWriter(finalOutput))) {

            PdfFont footerFont = loadFont();

            int totalPages = resultDoc.getNumberOfPages();
            for (int i = 1; i <= totalPages; i++) {
                PdfPage page = resultDoc.getPage(i);
                Rectangle pageSize = page.getPageSize();
                float x = pageSize.getRight() - 20;
                float y = pageSize.getBottom() + 20;

                try (Canvas canvas = new Canvas(new PdfCanvas(page.newContentStreamAfter(), page.getResources(), resultDoc), pageSize)) {
                    canvas.setFont(footerFont)
                            .setFontSize(8)
                            .setItalic();

                    canvas.showTextAligned(i + "/" + totalPages, x, y, TextAlignment.RIGHT);
                    canvas.showTextAligned(formatDate(LocalDate.now()), 20, y, TextAlignment.LEFT);
                }
            }
        }
        return finalOutput.toByteArray();
    }

    private record AssetTotals(int quantity, double sum) {
    }

    private record Column(Function<Card, String> extractor, TextAlignment alignment, float marginRight) {
    }

}