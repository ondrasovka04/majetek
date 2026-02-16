package cz.majetek.card.specification;

import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.card.repository.ICardRepository;
import cz.majetek.entity.*;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class CardSpecificationIntegrationTest {

    @Autowired
    private ICardRepository cardRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Create reference entities
        Location locationA = new Location();
        locationA.setId("LOC_A");
        locationA.setName("Location A");
        entityManager.persist(locationA);

        Location locationB = new Location();
        locationB.setId("LOC_B");
        locationB.setName("Location B");
        entityManager.persist(locationB);

        ClassEntity classA = new ClassEntity();
        classA.setId("CLS_A");
        classA.setName("Class A");
        entityManager.persist(classA);

        ClassEntity classB = new ClassEntity();
        classB.setId("CLS_B");
        classB.setName("Class B");
        entityManager.persist(classB);

        AssetGroup groupA = new AssetGroup();
        groupA.setId("GRP_A");
        groupA.setName("Group A");
        entityManager.persist(groupA);

        AssetGroup groupB = new AssetGroup();
        groupB.setId("GRP_B");
        groupB.setName("Group B");
        entityManager.persist(groupB);

        PurchaseMethod purchaseMethodA = new PurchaseMethod();
        purchaseMethodA.setId("PM_A");
        purchaseMethodA.setName("Purchase Method A");
        entityManager.persist(purchaseMethodA);

        User userA = new User("U1", "user_a", "pass", "User A", UserRole.USER);
        entityManager.persist(userA);

        User userB = new User("U2", "user_b", "pass", "User B", UserRole.ADMIN);
        entityManager.persist(userB);

        // Create test cards
        Card card1 = new Card();
        card1.setId(1);
        card1.setName("Notebook Dell");
        card1.setPrice(new BigDecimal("25000"));
        card1.setQuantity(new BigDecimal("1"));
        card1.setLocation(locationA);
        card1.setAssetClass(classA);
        card1.setGroup(groupA);
        card1.setPurchaseDate(LocalDate.of(2024, 1, 15));
        card1.setPurchaseMethod(purchaseMethodA);
        card1.setPurchaseInvoice("INV-001");
        card1.setCreatedBy(userA);
        card1.setCreatedDate(LocalDateTime.of(2024, 1, 15, 10, 0));
        card1.setResponsiblePerson(userA);
        card1.setNote("First notebook for development");
        entityManager.persist(card1);

        Card card2 = new Card();
        card2.setId(2);
        card2.setName("Monitor LG");
        card2.setPrice(new BigDecimal("8000"));
        card2.setQuantity(new BigDecimal("2"));
        card2.setLocation(locationA);
        card2.setAssetClass(classB);
        card2.setGroup(groupA);
        card2.setPurchaseDate(LocalDate.of(2024, 3, 20));
        card2.setPurchaseInvoice("INV-002");
        card2.setCreatedBy(userA);
        card2.setCreatedDate(LocalDateTime.of(2024, 3, 20, 14, 30));
        card2.setResponsiblePerson(userB);
        card2.setNote("Two monitors for office");
        entityManager.persist(card2);

        Card card3 = new Card();
        card3.setId(3);
        card3.setName("Keyboard Logitech");
        card3.setPrice(new BigDecimal("1500"));
        card3.setQuantity(new BigDecimal("5"));
        card3.setLocation(locationB);
        card3.setAssetClass(classA);
        card3.setGroup(groupB);
        card3.setPurchaseDate(LocalDate.of(2024, 6, 10));
        card3.setCreatedBy(userB);
        card3.setCreatedDate(LocalDateTime.of(2024, 6, 10, 9, 0));
        card3.setNote("Keyboards for new employees");
        entityManager.persist(card3);

        Card cardWrittenOff = new Card();
        cardWrittenOff.setId(4);
        cardWrittenOff.setName("Old Printer");
        cardWrittenOff.setPrice(new BigDecimal("5000"));
        cardWrittenOff.setQuantity(new BigDecimal("1"));
        cardWrittenOff.setLocation(locationB);
        cardWrittenOff.setAssetClass(classB);
        cardWrittenOff.setGroup(groupB);
        cardWrittenOff.setPurchaseDate(LocalDate.of(2020, 1, 1));
        cardWrittenOff.setWriteOffDate(LocalDate.of(2024, 6, 1));
        cardWrittenOff.setCreatedBy(userA);
        cardWrittenOff.setCreatedDate(LocalDateTime.of(2020, 1, 1, 8, 0));
        entityManager.persist(cardWrittenOff);

        entityManager.flush();
    }

    // --- ID filters ---

    @Test
    void filter_ById_ShouldReturnExactMatch() {
        CardFilterQuery q = new CardFilterQuery();
        q.setId(1);

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Notebook Dell", result.getFirst().getName());
    }

    @Test
    void filter_ByIdRange_ShouldReturnCardsInRange() {
        CardFilterQuery q = new CardFilterQuery();
        q.setMinId(2);
        q.setMaxId(3);

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getId() == 2));
        assertTrue(result.stream().anyMatch(c -> c.getId() == 3));
    }

    // --- Name filter ---

    @Test
    void filter_ByName_ShouldReturnMatchingCards() {
        CardFilterQuery q = new CardFilterQuery();
        q.setName("notebook");  // case insensitive

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Notebook Dell", result.getFirst().getName());
    }

    @Test
    void filter_ByNamePartial_ShouldReturnMatchingCards() {
        CardFilterQuery q = new CardFilterQuery();
        q.setName("Log");  // matches "Logitech"

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Keyboard Logitech", result.getFirst().getName());
    }

    // --- Price filters ---

    @Test
    void filter_ByMinPrice_ShouldReturnCardsAbovePrice() {
        CardFilterQuery q = new CardFilterQuery();
        q.setMinPrice(new BigDecimal("5000"));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(3, result.size());  // 25000, 8000, 5000
        assertTrue(result.stream().allMatch(c -> c.getPrice().compareTo(new BigDecimal("5000")) >= 0));
    }

    @Test
    void filter_ByMaxPrice_ShouldReturnCardsBelowPrice() {
        CardFilterQuery q = new CardFilterQuery();
        q.setMaxPrice(new BigDecimal("5000"));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // 1500, 5000
        assertTrue(result.stream().allMatch(c -> c.getPrice().compareTo(new BigDecimal("5000")) <= 0));
    }

    @Test
    void filter_ByPriceRange_ShouldReturnCardsInRange() {
        CardFilterQuery q = new CardFilterQuery();
        q.setMinPrice(new BigDecimal("2000"));
        q.setMaxPrice(new BigDecimal("10000"));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // 8000, 5000
    }

    // --- Quantity filters ---

    @Test
    void filter_ByMinQuantity_ShouldReturnCardsAboveQuantity() {
        CardFilterQuery q = new CardFilterQuery();
        q.setMinQuantity(new BigDecimal("2"));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // quantity 2 and 5
    }

    @Test
    void filter_ByQuantityRange_ShouldReturnCardsInRange() {
        CardFilterQuery q = new CardFilterQuery();
        q.setMinQuantity(new BigDecimal("1"));
        q.setMaxQuantity(new BigDecimal("2"));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(3, result.size());  // 1, 1, 2
    }

    // --- Location filter ---

    @Test
    void filter_ByLocation_ShouldReturnCardsInLocation() {
        CardFilterQuery q = new CardFilterQuery();
        q.setLocationId("LOC_A");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> "LOC_A".equals(c.getLocation().getId())));
    }

    // --- Class filter ---

    @Test
    void filter_ByClass_ShouldReturnCardsInClass() {
        CardFilterQuery q = new CardFilterQuery();
        q.setClassId("CLS_A");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 and card3
    }

    // --- Group filter ---

    @Test
    void filter_ByGroup_ShouldReturnCardsInGroup() {
        CardFilterQuery q = new CardFilterQuery();
        q.setGroupId("GRP_B");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card3 and cardWrittenOff
    }

    // --- Purchase date filters ---

    @Test
    void filter_ByPurchasedAfter_ShouldReturnCardsAfterDate() {
        CardFilterQuery q = new CardFilterQuery();
        q.setPurchasedAfter(LocalDate.of(2024, 3, 1));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card2 (March) and card3 (June)
    }

    @Test
    void filter_ByPurchasedBefore_ShouldReturnCardsBeforeDate() {
        CardFilterQuery q = new CardFilterQuery();
        q.setPurchasedBefore(LocalDate.of(2024, 2, 1));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 (Jan 2024) and cardWrittenOff (Jan 2020)
    }

    @Test
    void filter_ByPurchaseDateRange_ShouldReturnCardsInRange() {
        CardFilterQuery q = new CardFilterQuery();
        q.setPurchasedAfter(LocalDate.of(2024, 1, 1));
        q.setPurchasedBefore(LocalDate.of(2024, 4, 1));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 and card2
    }

    // --- Purchase method filter ---

    @Test
    void filter_ByPurchaseMethod_ShouldReturnCardsWithMethod() {
        CardFilterQuery q = new CardFilterQuery();
        q.setPurchaseMethodId("PM_A");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Notebook Dell", result.getFirst().getName());
    }

    // --- Purchase invoice filter ---

    @Test
    void filter_ByPurchaseInvoice_ShouldReturnMatchingCards() {
        CardFilterQuery q = new CardFilterQuery();
        q.setPurchaseInvoice("INV-001");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Notebook Dell", result.getFirst().getName());
    }

    // --- Created by filter ---

    @Test
    void filter_ByCreatedBy_ShouldReturnCardsCreatedByUser() {
        CardFilterQuery q = new CardFilterQuery();
        q.setCreatedBy("U1");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(3, result.size());  // card1, card2, cardWrittenOff
    }

    // --- Created date filters ---

    @Test
    void filter_ByCreatedAfter_ShouldReturnCardsCreatedAfterDate() {
        CardFilterQuery q = new CardFilterQuery();
        q.setCreatedAfter(LocalDate.of(2024, 3, 1));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card2 and card3
    }

    @Test
    void filter_ByCreatedDateRange_ShouldReturnCardsInRange() {
        CardFilterQuery q = new CardFilterQuery();
        q.setCreatedAfter(LocalDate.of(2024, 1, 1));
        q.setCreatedBefore(LocalDate.of(2024, 4, 1));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 and card2
    }

    // --- Responsible person filter ---

    @Test
    void filter_ByResponsiblePerson_ShouldReturnCardsWithPerson() {
        CardFilterQuery q = new CardFilterQuery();
        q.setResponsePerson("U2");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Monitor LG", result.getFirst().getName());
    }

    // --- Note filter ---

    @Test
    void filter_ByNote_ShouldReturnCardsWithMatchingNote() {
        CardFilterQuery q = new CardFilterQuery();
        q.setNote("office");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Monitor LG", result.getFirst().getName());
    }

    // --- Write-off filters ---

    @Test
    void filter_IncludeWriteOffFalse_ShouldExcludeWrittenOffCards() {
        CardFilterQuery q = new CardFilterQuery();
        q.setIncludeWriteOff(false);

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(3, result.size());
        assertTrue(result.stream().noneMatch(c -> c.getWriteOffDate() != null));
    }

    @Test
    void filter_IncludeWriteOffTrue_ShouldIncludeAllCards() {
        CardFilterQuery q = new CardFilterQuery();
        q.setIncludeWriteOff(true);

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(4, result.size());
    }

    @Test
    void filter_ByWriteOffDateRange_ShouldReturnCardsInRange() {
        CardFilterQuery q = new CardFilterQuery();
        q.setWriteOffAfter(LocalDate.of(2024, 1, 1));
        q.setWriteOffBefore(LocalDate.of(2024, 12, 31));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Old Printer", result.getFirst().getName());
    }

    // --- Fulltext search ---
    // Note: Fulltext search uses PostgreSQL's unaccent() function which is not available in H2.
    // These tests are skipped in unit tests but work in production with PostgreSQL.

    // --- Combined filters ---

    @Test
    void filter_CombinedLocationAndClass_ShouldReturnIntersection() {
        CardFilterQuery q = new CardFilterQuery();
        q.setLocationId("LOC_A");
        q.setClassId("CLS_A");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(1, result.size());
        assertEquals("Notebook Dell", result.getFirst().getName());
    }

    @Test
    void filter_CombinedGroupAndPriceRange_ShouldReturnMatching() {
        CardFilterQuery q = new CardFilterQuery();
        q.setGroupId("GRP_A");
        q.setMinPrice(new BigDecimal("5000"));
        q.setMaxPrice(new BigDecimal("30000"));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 (25000) and card2 (8000)
    }

    @Test
    void filter_CombinedMultipleFilters_ShouldNarrowResults() {
        CardFilterQuery q = new CardFilterQuery();
        q.setLocationId("LOC_A");
        q.setCreatedBy("U1");
        q.setPurchasedAfter(LocalDate.of(2024, 1, 1));
        q.setIncludeWriteOff(false);

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 and card2
    }

    @Test
    void filter_NoMatchingResults_ShouldReturnEmptyList() {
        CardFilterQuery q = new CardFilterQuery();
        q.setLocationId("NONEXISTENT");

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertTrue(result.isEmpty());
    }

    // --- Empty query ---

    @Test
    void filter_EmptyQuery_ShouldReturnAllCards() {
        CardFilterQuery q = new CardFilterQuery();

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(4, result.size());
    }

    // --- asOfDate filter ---

    @Test
    void filter_ByAsOfDate_ShouldReturnCardsCreatedBeforeDate() {
        CardFilterQuery q = new CardFilterQuery();
        q.setAsOfDate(LocalDate.of(2024, 2, 1));

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        assertEquals(2, result.size());  // card1 (Jan 2024) and cardWrittenOff (Jan 2020)
    }

    @Test
    void filter_ByAsOfDate_ExcludesWrittenOffBeforeDate() {
        CardFilterQuery q = new CardFilterQuery();
        q.setAsOfDate(LocalDate.of(2024, 7, 1));
        q.setIncludeWriteOff(false);

        List<Card> result = cardRepository.findAll(CardSpecification.filter(q));

        // cardWrittenOff was written off on 2024-06-01, before asOfDate, so excluded
        assertEquals(3, result.size());
    }
}
