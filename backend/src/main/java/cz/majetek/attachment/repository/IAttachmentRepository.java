package cz.majetek.attachment.repository;

import cz.majetek.entity.Attachment;
import cz.majetek.entity.Attachment.AttachmentId;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IAttachmentRepository extends JpaRepository<Attachment, AttachmentId> {
    List<Attachment> findAllByIdCardId(Integer cardId);

    @NonNull
    Optional<Attachment> findById(@NonNull Attachment.AttachmentId id);

    @Query("SELECT COALESCE(MAX(a.id.id), 0) + 1 FROM Attachment a WHERE a.id.cardId = :cardId")
    int findNextIdForCard(@Param("cardId") Integer cardId);
}
