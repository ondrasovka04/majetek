package cz.majetek.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @EmbeddedId
    private AttachmentId id;

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
