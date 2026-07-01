package cz.majetek.attachment.repository;

import cz.majetek.entity.Attachment;
import cz.majetek.entity.Attachment.AttachmentId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Attachment entity with composite key lookups.
 */
public interface IAttachmentRepository extends JpaRepository<Attachment, AttachmentId> {
    List<Attachment> findAllByIdCardId(Integer cardId);

    @NullMarked
    Optional<Attachment> findById(Attachment.AttachmentId id);

    @Query("SELECT COALESCE(MAX(a.id.id), 0) + 1 FROM Attachment a WHERE a.id.cardId = :cardId")
    int findNextIdForCard(@Param("cardId") Integer cardId);
}
