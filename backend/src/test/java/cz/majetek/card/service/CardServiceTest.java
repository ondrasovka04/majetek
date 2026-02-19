package cz.majetek.card.service;

import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.auth.security.UserPrincipal;
import cz.majetek.card.dto.CardChangeDto;
import cz.majetek.card.dto.CardDto;
import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.card.mapper.ICardMapper;
import cz.majetek.card.repository.ICardRepository;
import cz.majetek.card.service.implementation.CardService;
import cz.majetek.card.service.implementation.LabelPrinter;
import cz.majetek.dictionary.assetgroup.repository.IAssetGroupRepository;
import cz.majetek.dictionary.classentity.repository.IClassRepository;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.purchasemethod.repository.IPurchaseMethodRepository;
import cz.majetek.dictionary.writeoffmethod.repository.IWriteOffMethodRepository;
import cz.majetek.entity.AssetGroup;
import cz.majetek.entity.Card;
import cz.majetek.entity.ClassEntity;
import cz.majetek.entity.Location;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import cz.majetek.exception.auth.UserException;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private ICardRepository cardRepository;
    @Mock
    private ILocationRepository locationRepository;
    @Mock
    private IClassRepository classRepository;
    @Mock
    private IAssetGroupRepository assetGroupRepository;
    @Mock
    private IPurchaseMethodRepository purchaseMethodRepository;
    @Mock
    private IUserRepository userRepository;
    @Mock
    private IWriteOffMethodRepository writeOffMethodRepository;
    @Mock
    private LabelPrinter labelPrinter;
    @Mock
    private ICardMapper cardMapper;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CardService cardService;

    @TempDir
    Path tempDir;

    private Card testCard;
    private CardDto testCardDto;
    private User testUser;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        ReflectionTestUtils.setField(cardService, "uploadRoot", tempDir.toString());
        ReflectionTestUtils.setField(cardService, "exportPath", tempDir.toString());
        ReflectionTestUtils.setField(cardService, "maxLabelPerPrint", 100L);

        testUser = new User("1", "testuser", "password", "Test User", UserRole.USER);

        Location location = new Location();
        location.setId("LOC1");
        location.setName("Test Location");

        ClassEntity classEntity = new ClassEntity();
        classEntity.setId("CLS1");
        classEntity.setName("Test Class");

        AssetGroup group = new AssetGroup();
        group.setId("GRP1");
        group.setName("Test Group");

        testCard = new Card();
        testCard.setId(1);
        testCard.setName("Test Card");
        testCard.setPrice(BigDecimal.valueOf(1000));
        testCard.setLocation(location);
        testCard.setAssetClass(classEntity);
        testCard.setGroup(group);
        testCard.setCreatedBy(testUser);
        testCard.setCreatedDate(LocalDateTime.now());
        testCard.setPurchaseDate(LocalDate.now());
        testCard.setQuantity(BigDecimal.ONE);
        testCard.setResponsiblePerson(testUser);

        testCardDto = new CardDto();
        testCardDto.setId(1);
        testCardDto.setName("Test Card");
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        List<Card> cards = List.of(testCard);
        List<CardDto> cardDtos = List.of(testCardDto);
        when(cardRepository.findAll()).thenReturn(cards);
        when(cardMapper.toDto(cards)).thenReturn(cardDtos);

        List<CardDto> result = cardService.getAllCards();

        assertEquals(1, result.size());
        verify(cardRepository).findAll();
        verify(cardMapper).toDto(cards);
    }

    @Test
    void getCardById_WithExistingCard_ShouldReturnCard() {
        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);

        CardDto result = cardService.getCardById(1);

        assertEquals(testCardDto, result);
        verify(cardRepository).findById(1);
        verify(cardMapper).toDto(testCard);
    }

    @Test
    void getCardById_WithNonExistingCard_ShouldThrowException() {
        when(cardRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> cardService.getCardById(999));
        verify(cardRepository).findById(999);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getFilteredCards_ShouldReturnFilteredCards() {
        CardFilterQuery query = new CardFilterQuery();
        query.setLocationId("LOC1");
        List<Card> cards = List.of(testCard);
        List<CardDto> cardDtos = List.of(testCardDto);

        when(cardRepository.findAll(any(Specification.class))).thenReturn(cards);
        when(cardMapper.toDto(cards)).thenReturn(cardDtos);

        List<CardDto> result = cardService.getFilteredCards(query);

        assertEquals(1, result.size());
        verify(cardRepository).findAll(any(Specification.class));
    }

    @Test
    void createCard_WithValidData_ShouldCreateCard() {
        CardChangeDto changeDto = new CardChangeDto();
        changeDto.setName("New Card");
        changeDto.setLocationId("LOC1");
        changeDto.setClassId("CLS1");
        changeDto.setGroupId("GRP1");
        changeDto.setCreatedById("1");

        UserPrincipal principal = UserPrincipal.create(testUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(classRepository.existsById("CLS1")).thenReturn(true);
        when(assetGroupRepository.existsById("GRP1")).thenReturn(true);
        when(userRepository.existsById("1")).thenReturn(true);
        when(cardMapper.toEntity(changeDto)).thenReturn(testCard);
        when(cardRepository.findMaxId()).thenReturn(0);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);

        CardDto result = cardService.createCard(changeDto);

        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_WithInvalidLocation_ShouldThrowException() {
        CardChangeDto changeDto = new CardChangeDto();
        changeDto.setLocationId("INVALID");

        when(locationRepository.existsById("INVALID")).thenReturn(false);

        assertThrows(LocationException.class, () -> cardService.createCard(changeDto));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void updateCard_WithExistingCard_ShouldUpdateSuccessfully() {
        CardChangeDto changeDto = new CardChangeDto();
        changeDto.setName("Updated Card");
        changeDto.setLocationId("LOC1");
        changeDto.setClassId("CLS1");
        changeDto.setGroupId("GRP1");
        changeDto.setCreatedById("1");

        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(classRepository.existsById("CLS1")).thenReturn(true);
        when(assetGroupRepository.existsById("GRP1")).thenReturn(true);
        when(userRepository.existsById("1")).thenReturn(true);
        when(cardMapper.toEntity(changeDto)).thenReturn(testCard);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(testCardDto);

        CardDto result = cardService.updateCard(1, changeDto);

        assertNotNull(result);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCard_WithNonExistingCard_ShouldThrowException() {
        CardChangeDto changeDto = new CardChangeDto();
        when(cardRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> cardService.updateCard(999, changeDto));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_WithExistingCard_ShouldDeleteSuccessfully() {
        when(cardRepository.existsById(1)).thenReturn(true);
        doNothing().when(cardRepository).deleteById(1);

        assertDoesNotThrow(() -> cardService.deleteCard(1));
        verify(cardRepository).deleteById(1);
    }

    @Test
    void deleteCard_WithNonExistingCard_ShouldThrowException() {
        when(cardRepository.existsById(999)).thenReturn(false);

        assertThrows(CardException.class, () -> cardService.deleteCard(999));
        verify(cardRepository, never()).deleteById(any(Integer.class));
    }

    // --- exportLabel ---

    @Test
    @SuppressWarnings("unchecked")
    void exportLabel_NoCards_ShouldThrow() {
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of());
        CardFilterQuery q = new CardFilterQuery();
        assertThrows(CardException.class, () -> cardService.exportLabel(q, false));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportLabel_TooManyForCloud_ShouldThrow() {
        ReflectionTestUtils.setField(cardService, "maxLabelPerPrint", 1L);
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard, testCard));
        CardFilterQuery q = new CardFilterQuery();
        assertThrows(CardException.class, () -> cardService.exportLabel(q, true));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportLabel_LocalExport_ShouldReturnResource() {
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));
        when(labelPrinter.generateZplFromCards(anyList())).thenReturn("^XA^XZ");

        CardFilterQuery q = new CardFilterQuery();
        InputStreamResource result = cardService.exportLabel(q, false);

        assertNotNull(result);
        verify(labelPrinter, never()).sendToZebraCloud(any(File.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportLabel_CloudPrint_ShouldSendToCloud() {
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));
        when(labelPrinter.generateZplFromCards(anyList())).thenReturn("^XA^XZ");

        CardFilterQuery q = new CardFilterQuery();
        InputStreamResource result = cardService.exportLabel(q, true);

        assertNotNull(result);
        verify(labelPrinter).sendToZebraCloud(any(File.class));
    }

    // --- exportExcel ---

    @Test
    @SuppressWarnings("unchecked")
    void exportExcel_NoCards_ShouldThrow() {
        when(cardRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of());
        CardFilterQuery q = new CardFilterQuery();
        assertThrows(CardException.class, () -> cardService.exportExcel(q));
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportExcel_WithCards_ShouldReturnResource() {
        when(cardRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testCard));

        CardFilterQuery q = new CardFilterQuery();
        InputStreamResource result = cardService.exportExcel(q);
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportExcel_WithNullableFields_ShouldHandle() {
        Card cardWithNulls = new Card();
        cardWithNulls.setId(2);
        cardWithNulls.setName("Null Card");
        cardWithNulls.setPrice(null);
        cardWithNulls.setLocation(testCard.getLocation());
        cardWithNulls.setGroup(testCard.getGroup());
        cardWithNulls.setPurchaseMethod(null);
        cardWithNulls.setWriteOffMethod(null);
        cardWithNulls.setResponsiblePerson(null);
        cardWithNulls.setCreatedBy(testUser);
        cardWithNulls.setCreatedDate(LocalDateTime.now());
        cardWithNulls.setPurchaseDate(LocalDate.now());

        when(cardRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(cardWithNulls));

        InputStreamResource result = cardService.exportExcel(new CardFilterQuery());
        assertNotNull(result);
    }

    // --- exportGroupReport ---

    @Test
    @SuppressWarnings("unchecked")
    void exportGroupReport_ShouldReturnResource() {
        AssetGroup grp = new AssetGroup();
        grp.setId("GRP1");
        grp.setName("Group 1");
        when(assetGroupRepository.findById("GRP1")).thenReturn(Optional.of(grp));
        when(cardRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testCard));

        InputStreamResource result = cardService.exportGroupReport("GRP1", LocalDate.now());
        assertNotNull(result);
    }

    @Test
    void exportGroupReport_GroupNotFound_ShouldThrow() {
        when(assetGroupRepository.findById("INVALID")).thenReturn(Optional.empty());
        LocalDate now = LocalDate.now();
        assertThrows(AssetGroupException.class, () -> cardService.exportGroupReport("INVALID", now));
    }

    // --- exportAllGroupsReport ---

    @Test
    @SuppressWarnings("unchecked")
    void exportAllGroupsReport_ShouldReturnResource() {
        AssetGroup grp = new AssetGroup();
        grp.setId("GRP1");
        grp.setName("Group 1");
        when(assetGroupRepository.findAll(any(Sort.class))).thenReturn(List.of(grp));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));

        InputStreamResource result = cardService.exportAllGroupsReport(LocalDate.now());
        assertNotNull(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void exportAllGroupsReport_NullPriceAndQuantity_ShouldHandleDefaults() {
        Card cardNull = new Card();
        cardNull.setId(3);
        cardNull.setPrice(null);
        cardNull.setQuantity(null);

        AssetGroup grp = new AssetGroup();
        grp.setId("GRP1");
        grp.setName("Group 1");
        when(assetGroupRepository.findAll(any(Sort.class))).thenReturn(List.of(grp));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(cardNull));

        InputStreamResource result = cardService.exportAllGroupsReport(LocalDate.now());
        assertNotNull(result);
    }

    // --- exportLocationReport ---

    @Test
    @SuppressWarnings("unchecked")
    void exportLocationReport_ShouldReturnResource() {
        Location loc = new Location();
        loc.setId("LOC1");
        loc.setName("Loc");
        when(locationRepository.findById("LOC1")).thenReturn(Optional.of(loc));
        when(cardRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(testCard));

        InputStreamResource result = cardService.exportLocationReport("LOC1", LocalDate.now());
        assertNotNull(result);
    }

    @Test
    void exportLocationReport_NotFound_ShouldThrow() {
        when(locationRepository.findById("INVALID")).thenReturn(Optional.empty());
        LocalDate now = LocalDate.now();
        assertThrows(LocationException.class, () -> cardService.exportLocationReport("INVALID", now));
    }

    // --- exportCardReport ---

    @Test
    void exportCardReport_ShouldReturnResource() {
        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));

        InputStreamResource result = cardService.exportCardReport(1);
        assertNotNull(result);
    }

    @Test
    void exportCardReport_NotFound_ShouldThrow() {
        when(cardRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(CardException.class, () -> cardService.exportCardReport(999));
    }

    // --- deleteCard with folder ---

    @Test
    void deleteCard_WithAttachmentFolder_ShouldDeleteFolder() throws IOException {
        Path cardFolder = tempDir.resolve("42");
        Files.createDirectories(cardFolder);
        Files.writeString(cardFolder.resolve("test.txt"), "content");

        when(cardRepository.existsById(42)).thenReturn(true);

        cardService.deleteCard(42);

        assertFalse(Files.exists(cardFolder));
        verify(cardRepository).deleteById(42);
    }

    // --- validateReferencesExist branches ---

    @Test
    void createCard_WithInvalidClass_ShouldThrow() {
        CardChangeDto dto = new CardChangeDto();
        dto.setLocationId("LOC1");
        dto.setClassId("INVALID");
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(classRepository.existsById("INVALID")).thenReturn(false);
        assertThrows(ClassEntityException.class, () -> cardService.createCard(dto));
    }

    @Test
    void createCard_WithInvalidGroup_ShouldThrow() {
        CardChangeDto dto = new CardChangeDto();
        dto.setLocationId("LOC1");
        dto.setGroupId("INVALID");
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(assetGroupRepository.existsById("INVALID")).thenReturn(false);
        assertThrows(AssetGroupException.class, () -> cardService.createCard(dto));
    }

    @Test
    void createCard_WithInvalidPurchaseMethod_ShouldThrow() {
        CardChangeDto dto = new CardChangeDto();
        dto.setLocationId("LOC1");
        dto.setPurchaseMethodId("INVALID");
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(purchaseMethodRepository.existsById("INVALID")).thenReturn(false);
        assertThrows(PurchaseMethodException.class, () -> cardService.createCard(dto));
    }

    @Test
    void createCard_WithInvalidWriteOffMethod_ShouldThrow() {
        CardChangeDto dto = new CardChangeDto();
        dto.setLocationId("LOC1");
        dto.setWriteOffMethodId("INVALID");
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(writeOffMethodRepository.existsById("INVALID")).thenReturn(false);
        assertThrows(WriteOffMethodException.class, () -> cardService.createCard(dto));
    }

    @Test
    void createCard_WithInvalidCreatedBy_ShouldThrow() {
        CardChangeDto dto = new CardChangeDto();
        dto.setLocationId("LOC1");
        dto.setCreatedById("INVALID");
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(userRepository.existsById("INVALID")).thenReturn(false);
        assertThrows(UserException.class, () -> cardService.createCard(dto));
    }

    @Test
    void createCard_WithInvalidResponsiblePerson_ShouldThrow() {
        CardChangeDto dto = new CardChangeDto();
        dto.setLocationId("LOC1");
        dto.setResponsiblePersonId("INVALID");
        when(locationRepository.existsById("LOC1")).thenReturn(true);
        when(userRepository.existsById("INVALID")).thenReturn(false);
        assertThrows(UserException.class, () -> cardService.createCard(dto));
    }
}
