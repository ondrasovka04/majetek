package cz.majetek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAudit {

    @Id
    @Column(name = "card_id", nullable = false)
    private Integer cardId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "real_location_id")
    private Location realLocation;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
