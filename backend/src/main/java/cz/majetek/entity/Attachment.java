package cz.majetek.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * File attachment linked to a card via composite key (id + cardId).
 */
@Entity
@Table(name = "attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"card"})
public class Attachment {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private AttachmentId id;

    @Column(length = 50)
    private String name;

    private String filePath;

    @MapsId("cardId")
    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentId implements Serializable {

        private Integer id;

        @Column(name = "card_id")
        private Integer cardId;
    }
}
