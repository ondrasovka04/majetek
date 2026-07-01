package cz.majetek.card.repository;

import cz.majetek.entity.AssetGroup;
import cz.majetek.entity.Card;
import cz.majetek.entity.ClassEntity;
import cz.majetek.entity.Location;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CardRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ICardRepository cardRepository;

    private Location testLocation;
    private ClassEntity testClass;
    private AssetGroup testGroup;
    private User testUser;

    @BeforeEach
    void setUp() {
        testLocation = new Location();
        testLocation.setId("LOC1");
        testLocation.setName("Test Location");
        entityManager.persist(testLocation);

        testClass = new ClassEntity();
        testClass.setId("CLS1");
        testClass.setName("Test Class");
        entityManager.persist(testClass);

        testGroup = new AssetGroup();
        testGroup.setId("GRP1");
        testGroup.setName("Test Group");
        entityManager.persist(testGroup);

        testUser = new User("USR1", "testuser", "pass", "Test User", UserRole.USER);
        entityManager.persist(testUser);

        entityManager.flush();
    }

    @Test
    void save_ShouldPersistCard() {
        Card card = createCard(1, "New Card");

        Card saved = cardRepository.save(card);

        assertNotNull(saved);
        assertEquals(1, saved.getId());
        assertEquals("New Card", saved.getName());

        Card found = entityManager.find(Card.class, 1);
        assertNotNull(found);
        assertEquals("New Card", found.getName());
    }

    @Test
    void findById_WithExistingCard_ShouldReturnCard() {
        Card card = createCard(1, "Test Card");
        entityManager.persist(card);
        entityManager.flush();

        Optional<Card> found = cardRepository.findById(1);

        assertTrue(found.isPresent());
        assertEquals("Test Card", found.get().getName());
    }

    @Test
    void findById_WithNonExistingCard_ShouldReturnEmpty() {
        Optional<Card> found = cardRepository.findById(999);

        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_ShouldRemoveCard() {
        Card card = createCard(1, "Card to Delete");
        entityManager.persist(card);
        entityManager.flush();

        cardRepository.deleteById(1);

        Card found = entityManager.find(Card.class, 1);
        assertNull(found);
    }

    @Test
    void existsById_WithExistingCard_ShouldReturnTrue() {
        Card card = createCard(1, "Existing Card");
        entityManager.persist(card);
        entityManager.flush();

        boolean exists = cardRepository.existsById(1);

        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistingCard_ShouldReturnFalse() {
        boolean exists = cardRepository.existsById(999);

        assertFalse(exists);
    }

    @Test
    void findMaxId_WhenCardsExist_ShouldReturnMaxId() {
        entityManager.persist(createCard(5, "Card 5"));
        entityManager.persist(createCard(10, "Card 10"));
        entityManager.flush();

        assertEquals(10, cardRepository.findMaxId());
    }

    @Test
    void findMaxId_WhenNoCardsExist_ShouldReturnZero() {
        assertEquals(0, cardRepository.findMaxId());
    }

    private Card createCard(int id, String name) {
        Card card = new Card();
        card.setId(id);
        card.setName(name);
        card.setPrice(BigDecimal.valueOf(1000));
        card.setQuantity(BigDecimal.ONE);
        card.setLocation(testLocation);
        card.setAssetClass(testClass);
        card.setGroup(testGroup);
        card.setPurchaseDate(LocalDate.of(2025, Month.JUNE, 15));
        card.setCreatedDate(LocalDateTime.of(2025, Month.JUNE, 15, 10, 0));
        card.setCreatedBy(testUser);
        return card;
    }
}
