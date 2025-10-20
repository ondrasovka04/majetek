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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryAuditService implements IInventoryAuditService {

    private final IInventoryAuditRepository inventoryAuditRepository;
    private final ILocationRepository locationRepository;
    private final ICardRepository cardRepository;
    private final IInventoryAuditMapper mapper;
    private final IWriteOffMethodRepository writeOffMethodRepository;

    @Override
    public List<InventoryAuditDto> getAll() {
        return inventoryAuditRepository.findAllByRealLocationIsNotNull().stream().map(mapper::toDto).toList();
    }

    @Override
    public List<InventoryAuditDiffDto> getDiffs() {
        if (inventoryAuditRepository.findAllByRealLocationIsNotNull().isEmpty()) {
            throw new InventoryAuditException("No inventory audit records found.");
        }

        CardFilterQuery cardFilterQuery = new CardFilterQuery();
        cardFilterQuery.setIncludeWriteOff(false);

        List<Card> cards = cardRepository.findAll(CardSpecification.filter(cardFilterQuery));

        return cards.stream()
                .map(mapper::toDiffDto)
                .sorted(Comparator.comparing(InventoryAuditDiffDto::getCardId))
                .toList();
    }

    @Override
    @Transactional
    public List<InventoryAuditInsertDto> insertMany(List<InventoryAuditInsertDto> items) {
        List<InventoryAudit> saved = items.stream().map(dto -> {
            Location location = locationRepository.findById(dto.getRealLocationId())
                    .orElseThrow(() -> LocationException.notFound(dto.getRealLocationId()));

            Card card = cardRepository.findById(dto.getCardId()).orElseThrow(() -> CardException.notFound(dto.getCardId()));

            InventoryAudit entity = inventoryAuditRepository.findById(dto.getCardId()).orElse(new InventoryAudit());
            entity.setCard(card);
            entity.setRealLocation(location);
            entity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
            entity.setAction(null);
            entity.setWriteOffDate(null);
            entity.setWriteOffMethod(null);
            return inventoryAuditRepository.save(entity);
        }).toList();

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

    @Override
    @Transactional
    public void project() {
        CardFilterQuery query = new CardFilterQuery();
        query.setIncludeWriteOff(false);

        cardRepository.findAll(CardSpecification.filter(query))
                .stream()
                .peek(card -> {
                    if (card.getInventoryAudit() == null) {
                        throw InventoryAuditException.notFound(card.getId());
                    }
                })
                .forEach(card -> {
                    InventoryAudit audit = card.getInventoryAudit();
                    switch (audit.getAction()) {
                        case KEEP, OU -> { /* no-op */ }
                        case SU -> card.setLocation(audit.getRealLocation());
                        case WRITE_OFF -> {
                            card.setWriteOffDate(audit.getWriteOffDate());
                            card.setWriteOffMethod(audit.getWriteOffMethod());
                        }
                    }
                    cardRepository.save(card);
                });
        inventoryAuditRepository.deleteAllInBatch();
    }
}
