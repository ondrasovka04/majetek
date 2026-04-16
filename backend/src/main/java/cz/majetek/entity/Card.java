package cz.majetek.entity;

import cz.majetek.entity.inventoryaudit.InventoryAudit;
import cz.majetek.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Main asset card entity with relations to location, group, attachments and audits.
 */
@Entity
@Table(name = "card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"attachments", "inventoryAudit"})
public class Card {

    @Id
    @EqualsAndHashCode.Include
    private Integer id;

    private String name;

    private BigDecimal price;

    private BigDecimal quantity;

    private String serialNumber;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private ClassEntity assetClass;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private AssetGroup group;

    private LocalDate purchaseDate;

    @ManyToOne
    @JoinColumn(name = "purchase_method_id")
    private PurchaseMethod purchaseMethod;

    private String purchaseInvoice;

    private LocalDate writeOffDate;

    @ManyToOne
    @JoinColumn(name = "write_off_method_id")
    private WriteOffMethod writeOffMethod;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "responsible_person")
    private User responsiblePerson;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    private List<Attachment> attachments = new ArrayList<>();

    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL)
    private InventoryAudit inventoryAudit;
}
