package cz.majetek.inventoryaudit.service.implementation;

import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.card.repository.ICardRepository;
import cz.majetek.card.specification.CardSpecification;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.writeoffmethod.repository.IWriteOffMethodRepository;
import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import cz.majetek.entity.WriteOffMethod;
import cz.majetek.entity.inventoryaudit.InventoryAudit;
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
import cz.majetek.inventoryaudit.service.IInventoryAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service for inventory audit operations – scans, diffs and card state projections.
 */
@Service
@RequiredArgsConstructor
public class InventoryAuditService implements IInventoryAuditService {

    private static final ZoneId ZONE_PRAGUE = ZoneId.of("Europe/Prague");

    private final IInventoryAuditRepository inventoryAuditRepository;
    private final ILocationRepository locationRepository;
    private final ICardRepository cardRepository;
    private final IInventoryAuditMapper mapper;
    private final IWriteOffMethodRepository writeOffMethodRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryAuditDto> getAll() {
        return inventoryAuditRepository.findAllByRealLocationIsNotNull().stream().map(mapper::toDto).toList();
    }

    /**
     * Compute diffs between expected and scanned card locations.
     */
    @Override
    @Transactional(readOnly = true)
    public List<InventoryAuditDiffDto> getDiffs() {
        // No scans uploaded yet – the audit has not started, which is a valid state
        if (!inventoryAuditRepository.existsByRealLocationIsNotNull()) {
            return List.of();
        }

        CardFilterQuery cardFilterQuery = new CardFilterQuery();
        cardFilterQuery.setIncludeWriteOff(false);

        List<Card> cards = cardRepository.findAll(CardSpecification.filter(cardFilterQuery));

        return cards.stream()
                .map(mapper::toDiffDto)
                .sorted(Comparator.comparing(InventoryAuditDiffDto::getCardId))
                .toList();
    }

    /**
     * Upsert audit records from mobile scans.
     */
    @Override
    @Transactional
    public List<InventoryAuditInsertDto> insertMany(List<InventoryAuditInsertDto> items) {
        List<InventoryAudit> entities = items.stream().map(dto -> {
            Location location = locationRepository.findById(dto.getRealLocationId())
                    .orElseThrow(() -> LocationException.notFound(dto.getRealLocationId()));

            Card card = cardRepository.findById(dto.getCardId()).orElseThrow(() -> CardException.notFound(dto.getCardId()));

            InventoryAudit entity = inventoryAuditRepository.findById(dto.getCardId()).orElse(new InventoryAudit());
            entity.setCard(card);
            entity.setRealLocation(location);
            entity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now(ZONE_PRAGUE));
            entity.setAction(null);
            entity.setWriteOffDate(null);
            entity.setWriteOffMethod(null);
            return entity;
        }).toList();

        List<InventoryAudit> saved = inventoryAuditRepository.saveAll(entities);
        return saved.stream().map(mapper::toInsertDto).toList();
    }

    @Override
    @Transactional
    public void patch(Integer cardId, InventoryAuditPatchDto dto) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> CardException.notFound(cardId));

        InventoryAudit entity = inventoryAuditRepository.findById(cardId)
                .orElseGet(() -> {
                    InventoryAudit newAudit = new InventoryAudit();
                    newAudit.setCard(card);
                    newAudit.setTimestamp(LocalDateTime.now(ZONE_PRAGUE));
                    return newAudit;
                });

        entity.setAction(dto.getAction());
        entity.setWriteOffDate(dto.getWriteOffDate());

        if (dto.getWriteOffMethodId() != null) {
            WriteOffMethod method = writeOffMethodRepository.findById(dto.getWriteOffMethodId())
                    .orElseThrow(() -> WriteOffMethodException.notFound(dto.getWriteOffMethodId()));
            entity.setWriteOffMethod(method);
        } else {
            entity.setWriteOffMethod(null);
        }

        inventoryAuditRepository.save(entity);
    }

    /**
     * Apply audit actions (relocate, write-off) to cards and clear audit data.
     */
    @Override
    @Transactional
    public void project() {
        CardFilterQuery query = new CardFilterQuery();
        query.setIncludeWriteOff(false);

        List<Card> cards = cardRepository.findAll(CardSpecification.filter(query));

        cards.forEach(card -> {
            if (card.getInventoryAudit() == null) {
                throw InventoryAuditException.notFound(card.getId());
            }
        });

        List<Card> modifiedCards = new ArrayList<>();
        cards.forEach(card -> {
            InventoryAudit audit = card.getInventoryAudit();
            if (audit.getAction() == null) return;
            switch (audit.getAction()) {
                case KEEP, EL -> { /* no-op */ }
                case RL -> card.setLocation(audit.getRealLocation());
                case WRITE_OFF -> {
                    card.setWriteOffDate(audit.getWriteOffDate());
                    card.setWriteOffMethod(audit.getWriteOffMethod());
                }
            }
            modifiedCards.add(card);
        });
        cardRepository.saveAll(modifiedCards);
        inventoryAuditRepository.deleteAllInBatch();
    }
}
