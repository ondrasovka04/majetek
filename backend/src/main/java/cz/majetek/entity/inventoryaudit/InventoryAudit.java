package cz.majetek.entity.inventoryaudit;

import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import cz.majetek.entity.WriteOffMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"card"})
public class InventoryAudit {

    @Id
    @Column(name = "card_id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer cardId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "card_id")
    private Card card;

    @ManyToOne
    @JoinColumn(name = "real_location_id")
    private Location realLocation;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private InventoryAuditAction action;

    private LocalDate writeOffDate;

    @ManyToOne
    @JoinColumn(name = "write_off_method_id")
    private WriteOffMethod writeOffMethod;
}
