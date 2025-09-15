package cz.majetek.card.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardExportDto {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("Název")
    private String name;

    @ExcelProperty("Cena")
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
    private LocalDate purchaseDate;

    @ExcelProperty("Datum vyřazení")
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
    private LocalDate created;

    @ExcelProperty("Vytvořil")
    private String createdBy;
}
