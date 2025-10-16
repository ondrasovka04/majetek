package cz.majetek.inventoryaudit.service.implementation;

import cz.majetek.card.repository.ICardRepository;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.entity.Card;
import cz.majetek.entity.InventoryAudit;
import cz.majetek.entity.Location;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.LocationException;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.mapper.IInventoryAuditMapper;
import cz.majetek.inventoryaudit.repository.IInventoryAuditRepository;
import cz.majetek.inventoryaudit.service.IInventoryAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryAuditService implements IInventoryAuditService {

    private final IInventoryAuditRepository inventoryAuditRepository;
    private final ILocationRepository locationRepository;
    private final ICardRepository cardRepository;
    private final IInventoryAuditMapper mapper;

    @Override
    public List<InventoryAuditDto> getAll() {
        return inventoryAuditRepository.findAll().stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public List<InventoryAuditDto> insertMany(List<InventoryAuditDto> items) {
        List<InventoryAudit> saved = items.stream().map(dto -> {
            Location location = locationRepository.findById(dto.getRealLocationId())
                    .orElseThrow(() -> LocationException.notFound(dto.getRealLocationId()));

            Card card = cardRepository.findById(dto.getCardId()).orElseThrow(() -> CardException.notFound(dto.getCardId()));

            InventoryAudit entity = inventoryAuditRepository.findById(dto.getCardId()).orElse(new InventoryAudit());
            entity.setCard(card);
            entity.setRealLocation(location);
            entity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
            return inventoryAuditRepository.save(entity);
        }).toList();

        return saved.stream().map(mapper::toDto).toList();
    }
}
