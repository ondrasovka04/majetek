package cz.majetek.attachment.repository;

import cz.majetek.entity.Attachment;
import cz.majetek.entity.Attachment.AttachmentId;
import cz.majetek.entity.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AttachmentRepositoryTest {

    @Autowired
    private IAttachmentRepository attachmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Card card1;

    @BeforeEach
    void setUp() {
        card1 = new Card();
        card1.setId(1);
        card1.setName("Test Card 1");
        entityManager.persist(card1);

        Card card2 = new Card();
        card2.setId(2);
        card2.setName("Test Card 2");
        entityManager.persist(card2);

        Attachment att1 = new Attachment(new AttachmentId(1, 1), "file1.pdf", "/path/file1.pdf", card1);
        Attachment att2 = new Attachment(new AttachmentId(2, 1), "file2.txt", "/path/file2.txt", card1);
        Attachment att3 = new Attachment(new AttachmentId(1, 2), "file3.png", "/path/file3.png", card2);

        entityManager.persist(att1);
        entityManager.persist(att2);
        entityManager.persist(att3);
        entityManager.flush();
    }

    @Test
    void findAllByIdCardId_ShouldReturnAttachmentsForCard() {
        List<Attachment> result = attachmentRepository.findAllByIdCardId(1);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Attachment::getName)
                .containsExactlyInAnyOrder("file1.pdf", "file2.txt");
    }

    @Test
    void findAllByIdCardId_ShouldReturnEmptyForCardWithNoAttachments() {
        Card card3 = new Card();
        card3.setId(3);
        card3.setName("Empty Card");
        entityManager.persistAndFlush(card3);

        List<Attachment> result = attachmentRepository.findAllByIdCardId(3);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_ShouldReturnAttachmentWhenExists() {
        Optional<Attachment> result = attachmentRepository.findById(new AttachmentId(1, 1));

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("file1.pdf");
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<Attachment> result = attachmentRepository.findById(new AttachmentId(99, 1));

        assertThat(result).isEmpty();
    }

    @Test
    void findNextIdForCard_ShouldReturnNextId() {
        int nextId = attachmentRepository.findNextIdForCard(1);

        // Card 1 has attachments with id 1 and 2, so next should be 3
        assertThat(nextId).isEqualTo(3);
    }

    @Test
    void findNextIdForCard_ShouldReturn1ForCardWithNoAttachments() {
        Card card3 = new Card();
        card3.setId(3);
        card3.setName("Empty Card");
        entityManager.persistAndFlush(card3);

        int nextId = attachmentRepository.findNextIdForCard(3);

        // COALESCE(MAX, 0) + 1 = 1
        assertThat(nextId).isEqualTo(1);
    }

    @Test
    void save_ShouldPersistNewAttachment() {
        Attachment newAtt = new Attachment(new AttachmentId(3, 1), "new.doc", "/path/new.doc", card1);
        attachmentRepository.save(newAtt);
        entityManager.flush();

        Optional<Attachment> found = attachmentRepository.findById(new AttachmentId(3, 1));
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("new.doc");
    }

    @Test
    void delete_ShouldRemoveAttachment() {
        Attachment att = attachmentRepository.findById(new AttachmentId(1, 1)).orElseThrow();
        attachmentRepository.delete(att);
        entityManager.flush();

        Optional<Attachment> result = attachmentRepository.findById(new AttachmentId(1, 1));
        assertThat(result).isEmpty();

        // Other attachments should remain
        assertThat(attachmentRepository.findAllByIdCardId(1)).hasSize(1);
    }
}
