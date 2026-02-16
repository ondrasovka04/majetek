package cz.majetek.card.repository;

import cz.majetek.entity.AssetGroup;
import cz.majetek.entity.Card;
import cz.majetek.entity.ClassEntity;
import cz.majetek.entity.Location;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class CardRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

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
    void findMaxId_WithNoCards_ShouldReturnZero() {
        Integer maxId = cardRepository.findMaxId();

        assertEquals(0, maxId);
    }

    @Test
    void findMaxId_WithCards_ShouldReturnMaximumId() {
        Card card1 = createCard(1, "Card 1");
        Card card2 = createCard(5, "Card 2");
        Card card3 = createCard(3, "Card 3");

        entityManager.persist(card1);
        entityManager.persist(card2);
        entityManager.persist(card3);
        entityManager.flush();

        Integer maxId = cardRepository.findMaxId();

        assertEquals(5, maxId);
    }

    @Test
    void save_ShouldPersistCard() {
        Card card = createCard(10, "New Card");

        Card saved = cardRepository.save(card);

        assertNotNull(saved);
        assertEquals(10, saved.getId());
        assertEquals("New Card", saved.getName());

        Card found = entityManager.find(Card.class, 10);
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

    private Card createCard(Integer id, String name) {
        Card card = new Card();
        card.setId(id);
        card.setName(name);
        card.setPrice(BigDecimal.valueOf(1000));
        card.setQuantity(BigDecimal.ONE);
        card.setLocation(testLocation);
        card.setAssetClass(testClass);
        card.setGroup(testGroup);
        card.setPurchaseDate(LocalDate.now());
        card.setCreatedDate(LocalDateTime.now());
        card.setCreatedBy(testUser);
        return card;
    }
}

