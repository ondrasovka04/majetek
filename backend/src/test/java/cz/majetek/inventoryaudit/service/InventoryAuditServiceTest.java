package cz.majetek.inventoryaudit.service;

import cz.majetek.card.repository.ICardRepository;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.writeoffmethod.repository.IWriteOffMethodRepository;
import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import cz.majetek.entity.WriteOffMethod;
import cz.majetek.entity.inventoryaudit.InventoryAudit;
import cz.majetek.entity.inventoryaudit.InventoryAuditAction;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.LocationException;
import cz.majetek.exception.dictionary.WriteOffMethodException;
import cz.majetek.exception.inventoryaudit.InventoryAuditException;
import cz.majetek.inventoryaudit.dto.InventoryAuditDiffDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditInsertDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditPatchDto;
import cz.majetek.inventoryaudit.mapper.IInventoryAuditMapper;
import cz.majetek.inventoryaudit.repository.IInventoryAuditRepository;
import cz.majetek.inventoryaudit.service.implementation.InventoryAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAuditServiceTest {

    @Mock
    private IInventoryAuditRepository inventoryAuditRepository;

    @Mock
    private ILocationRepository locationRepository;

    @Mock
    private ICardRepository cardRepository;

    @Mock
    private IInventoryAuditMapper mapper;

    @Mock
    private IWriteOffMethodRepository writeOffMethodRepository;

    @InjectMocks
    private InventoryAuditService inventoryAuditService;

    private Card testCard;
    private Location realLocation;
    private InventoryAudit testAudit;
    private InventoryAuditDto testAuditDto;

    @BeforeEach
    void setUp() {
        Location testLocation = new Location("LOC1", "Expected Location");
        realLocation = new Location("LOC2", "Real Location");

        testCard = new Card();
        testCard.setId(1);
        testCard.setName("Test Card");
        testCard.setLocation(testLocation);

        testAudit = new InventoryAudit();
        testAudit.setCardId(1);
        testAudit.setCard(testCard);
        testAudit.setRealLocation(realLocation);
        testAudit.setTimestamp(LocalDateTime.now());

        testCard.setInventoryAudit(testAudit);

        testAuditDto = new InventoryAuditDto();
        testAuditDto.setCardId(1);
        testAuditDto.setCardName("Test Card");
        testAuditDto.setRealLocationId("LOC2");
        testAuditDto.setRealLocationName("Real Location");
    }

    @Test
    void getAll_ShouldReturnAllAudits() {
        List<InventoryAudit> audits = List.of(testAudit);
        when(inventoryAuditRepository.findAllByRealLocationIsNotNull()).thenReturn(audits);
        when(mapper.toDto(testAudit)).thenReturn(testAuditDto);

        List<InventoryAuditDto> result = inventoryAuditService.getAll();

        assertEquals(1, result.size());
        assertEquals(testAuditDto, result.getFirst());
        verify(inventoryAuditRepository).findAllByRealLocationIsNotNull();
    }

    @Test
    void getAll_WithNoAudits_ShouldReturnEmptyList() {
        when(inventoryAuditRepository.findAllByRealLocationIsNotNull()).thenReturn(Collections.emptyList());

        List<InventoryAuditDto> result = inventoryAuditService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getDiffs_WithNoAuditRecords_ShouldThrowException() {
        when(inventoryAuditRepository.findAllByRealLocationIsNotNull()).thenReturn(Collections.emptyList());

        assertThrows(InventoryAuditException.class, () -> inventoryAuditService.getDiffs());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getDiffs_WithAuditRecords_ShouldReturnDiffs() {
        when(inventoryAuditRepository.findAllByRealLocationIsNotNull()).thenReturn(List.of(testAudit));
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));

        var diffDto = new InventoryAuditDiffDto();
        diffDto.setCardId(1);
        diffDto.setCardName("Test Card");
        when(mapper.toDiffDto(testCard)).thenReturn(diffDto);

        var result = inventoryAuditService.getDiffs();

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getCardId());
        verify(cardRepository).findAll(any(Specification.class));
    }

    @Test
    void insertMany_WithValidData_ShouldInsertSuccessfully() {
        InventoryAuditInsertDto insertDto = new InventoryAuditInsertDto();
        insertDto.setCardId(1);
        insertDto.setRealLocationId("LOC2");
        insertDto.setTimestamp(LocalDateTime.now());

        when(locationRepository.findById("LOC2")).thenReturn(Optional.of(realLocation));
        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.empty());
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);

        InventoryAuditInsertDto resultDto = new InventoryAuditInsertDto();
        resultDto.setCardId(1);
        resultDto.setRealLocationId("LOC2");
        when(mapper.toInsertDto(testAudit)).thenReturn(resultDto);

        List<InventoryAuditInsertDto> result = inventoryAuditService.insertMany(List.of(insertDto));

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getCardId());
        verify(inventoryAuditRepository).save(any(InventoryAudit.class));
    }

    @Test
    void insertMany_WithNullTimestamp_ShouldUseCurrentTime() {
        InventoryAuditInsertDto insertDto = new InventoryAuditInsertDto();
        insertDto.setCardId(1);
        insertDto.setRealLocationId("LOC2");
        insertDto.setTimestamp(null);

        when(locationRepository.findById("LOC2")).thenReturn(Optional.of(realLocation));
        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.empty());
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);
        when(mapper.toInsertDto(testAudit)).thenReturn(insertDto);

        List<InventoryAuditInsertDto> result = inventoryAuditService.insertMany(List.of(insertDto));

        assertEquals(1, result.size());
        verify(inventoryAuditRepository).save(any(InventoryAudit.class));
    }

    @Test
    void insertMany_WithExistingAudit_ShouldUpdateExisting() {
        InventoryAuditInsertDto insertDto = new InventoryAuditInsertDto();
        insertDto.setCardId(1);
        insertDto.setRealLocationId("LOC2");
        insertDto.setTimestamp(LocalDateTime.now());

        when(locationRepository.findById("LOC2")).thenReturn(Optional.of(realLocation));
        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.of(testAudit));
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);
        when(mapper.toInsertDto(testAudit)).thenReturn(insertDto);

        List<InventoryAuditInsertDto> result = inventoryAuditService.insertMany(List.of(insertDto));

        assertEquals(1, result.size());
        verify(inventoryAuditRepository).save(any(InventoryAudit.class));
    }

    @Test
    void insertMany_WithInvalidLocation_ShouldThrowException() {
        InventoryAuditInsertDto insertDto = new InventoryAuditInsertDto();
        insertDto.setCardId(1);
        insertDto.setRealLocationId("INVALID");

        when(locationRepository.findById("INVALID")).thenReturn(Optional.empty());

        List<InventoryAuditInsertDto> insertDtoList = List.of(insertDto);
        assertThrows(LocationException.class, () -> inventoryAuditService.insertMany(insertDtoList));
        verify(inventoryAuditRepository, never()).save(any(InventoryAudit.class));
    }

    @Test
    void insertMany_WithInvalidCard_ShouldThrowException() {
        InventoryAuditInsertDto insertDto = new InventoryAuditInsertDto();
        insertDto.setCardId(999);
        insertDto.setRealLocationId("LOC2");

        when(locationRepository.findById("LOC2")).thenReturn(Optional.of(realLocation));
        when(cardRepository.findById(999)).thenReturn(Optional.empty());

        List<InventoryAuditInsertDto> insertDtoList = List.of(insertDto);
        assertThrows(CardException.class, () -> inventoryAuditService.insertMany(insertDtoList));
        verify(inventoryAuditRepository, never()).save(any(InventoryAudit.class));
    }

    @Test
    void patch_WithValidKeepAction_ShouldPatchSuccessfully() {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.KEEP);

        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.of(testAudit));
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);

        assertDoesNotThrow(() -> inventoryAuditService.patch(1, patchDto));
        verify(inventoryAuditRepository).save(any(InventoryAudit.class));
    }

    @Test
    void patch_WithWriteOffAction_ShouldSetWriteOffFields() {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.WRITE_OFF);
        patchDto.setWriteOffDate(LocalDate.now());
        patchDto.setWriteOffMethodId("WO1");

        WriteOffMethod method = new WriteOffMethod("WO1", "Test Method");

        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.of(testAudit));
        when(writeOffMethodRepository.findById("WO1")).thenReturn(Optional.of(method));
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);

        assertDoesNotThrow(() -> inventoryAuditService.patch(1, patchDto));
        verify(writeOffMethodRepository).findById("WO1");
        verify(inventoryAuditRepository).save(any(InventoryAudit.class));
    }

    @Test
    void patch_WithNullWriteOffMethodId_ShouldClearWriteOffMethod() {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.KEEP);
        patchDto.setWriteOffMethodId(null);

        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.of(testAudit));
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);

        assertDoesNotThrow(() -> inventoryAuditService.patch(1, patchDto));
        verify(writeOffMethodRepository, never()).findById(any(String.class));
    }

    @Test
    void patch_WithInvalidWriteOffMethodId_ShouldThrowException() {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.WRITE_OFF);
        patchDto.setWriteOffMethodId("INVALID");

        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.of(testAudit));
        when(writeOffMethodRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(WriteOffMethodException.class, () -> inventoryAuditService.patch(1, patchDto));
        verify(inventoryAuditRepository, never()).save(any(InventoryAudit.class));
    }

    @Test
    void patch_WithNonExistingCard_ShouldThrowException() {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.KEEP);

        when(cardRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> inventoryAuditService.patch(999, patchDto));
        verify(inventoryAuditRepository, never()).save(any(InventoryAudit.class));
    }

    @Test
    void patch_WithNoExistingAudit_ShouldCreateNew() {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.SU);

        when(cardRepository.findById(1)).thenReturn(Optional.of(testCard));
        when(inventoryAuditRepository.findById(1)).thenReturn(Optional.empty());
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(testAudit);

        assertDoesNotThrow(() -> inventoryAuditService.patch(1, patchDto));
        verify(inventoryAuditRepository).save(any(InventoryAudit.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void project_WithSUAction_ShouldUpdateLocation() {
        testAudit.setAction(InventoryAuditAction.SU);
        testAudit.setRealLocation(realLocation);
        testCard.setInventoryAudit(testAudit);

        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        doNothing().when(inventoryAuditRepository).deleteAllInBatch();

        inventoryAuditService.project();

        assertEquals(realLocation, testCard.getLocation());
        verify(cardRepository).save(testCard);
    }

    @Test
    @SuppressWarnings("unchecked")
    void project_WithKeepAction_ShouldNotModifyCard() {
        testAudit.setAction(InventoryAuditAction.KEEP);
        testCard.setInventoryAudit(testAudit);

        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        doNothing().when(inventoryAuditRepository).deleteAllInBatch();

        assertDoesNotThrow(() -> inventoryAuditService.project());
        verify(cardRepository).save(testCard);
        verify(inventoryAuditRepository).deleteAllInBatch();
    }

    @Test
    @SuppressWarnings("unchecked")
    void project_WithWriteOffAction_ShouldSetWriteOffFields() {
        WriteOffMethod method = new WriteOffMethod("WO1", "Test Method");
        testAudit.setAction(InventoryAuditAction.WRITE_OFF);
        testAudit.setWriteOffDate(LocalDate.of(2026, 1, 1));
        testAudit.setWriteOffMethod(method);
        testCard.setInventoryAudit(testAudit);

        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        doNothing().when(inventoryAuditRepository).deleteAllInBatch();

        inventoryAuditService.project();

        assertEquals(LocalDate.of(2026, 1, 1), testCard.getWriteOffDate());
        assertEquals(method, testCard.getWriteOffMethod());
    }

    @Test
    @SuppressWarnings("unchecked")
    void project_WithCardMissingAudit_ShouldThrowException() {
        testCard.setInventoryAudit(null);
        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));

        assertThrows(InventoryAuditException.class, () -> inventoryAuditService.project());
    }

    @Test
    @SuppressWarnings("unchecked")
    void project_WithOUAction_ShouldNotModifyCard() {
        testAudit.setAction(InventoryAuditAction.OU);
        testCard.setInventoryAudit(testAudit);

        when(cardRepository.findAll(any(Specification.class))).thenReturn(List.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        doNothing().when(inventoryAuditRepository).deleteAllInBatch();

        Location originalLocation = testCard.getLocation();
        inventoryAuditService.project();

        assertEquals(originalLocation, testCard.getLocation());
        assertNull(testCard.getWriteOffDate());
    }
}
