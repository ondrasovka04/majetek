package cz.majetek.card.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT)
public class CardExportDto {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("Název")
    private String name;

    @ExcelProperty("Cena")
    @NumberFormat("#,##0.00")
    private BigDecimal price;

    @ExcelProperty("Seriové číslo")
    private String serialNumber;

    @ExcelProperty("Umístění")
    private String location;

    @ExcelProperty("Skupina")
    private String group;

    @ExcelProperty("Faktura")
    private String purchaseInvoice;

    @ExcelProperty("Datum pořízení")
    @DateTimeFormat("dd.MM.yyyy")
    private LocalDate purchaseDate;

    @ExcelProperty("Datum vyřazení")
    @DateTimeFormat("dd.MM.yyyy")
    private LocalDate writeOffDate;

    @ExcelProperty("Způsob pořízení")
    private String purchaseMethod;

    @ExcelProperty("Způsob vyřazení")
    private String writeOffMethod;

    @ExcelProperty("Zodpovědná osoba")
    private String responsiblePerson;

    @ExcelProperty("Poznámka")
    private String note;

    @ExcelProperty("Vytvořeno")
    @DateTimeFormat("dd.MM.yyyy")
    private LocalDate created;

    @ExcelProperty("Vytvořil")
    private String createdBy;
}
