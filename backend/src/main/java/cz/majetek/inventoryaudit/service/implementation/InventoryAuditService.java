package cz.majetek.inventoryaudit.service.implementation;

import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.entity.InventoryAudit;
import cz.majetek.entity.Location;
import cz.majetek.exception.dictionary.LocationException;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
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

    private final IInventoryAuditRepository repository;
    private final ILocationRepository locationRepository;

    @Override
    public List<InventoryAuditDto> getAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public List<InventoryAuditDto> insertMany(List<InventoryAuditDto> items) {
        List<InventoryAudit> saved = items.stream().map(dto -> {
            Location location = locationRepository.findById(dto.getRealLocationId())
                    .orElseThrow(() -> LocationException.notFound(dto.getRealLocationId()));

            InventoryAudit entity = repository.findById(dto.getCardId()).orElse(new InventoryAudit());
            entity.setCardId(dto.getCardId());
            entity.setRealLocation(location);
            entity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
            return repository.save(entity);
        }).toList();

        return saved.stream().map(this::toDto).toList();
    }

    private InventoryAuditDto toDto(InventoryAudit entity) {
        InventoryAuditDto dto = new InventoryAuditDto();
        dto.setCardId(entity.getCardId());
        dto.setRealLocationId(entity.getRealLocation().getId());
        dto.setTimestamp(entity.getTimestamp());
        return dto;
    }
}
