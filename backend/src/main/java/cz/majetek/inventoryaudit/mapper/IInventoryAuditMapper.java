package cz.majetek.inventoryaudit.mapper;

import cz.majetek.entity.Card;
import cz.majetek.entity.inventoryaudit.InventoryAudit;
import cz.majetek.inventoryaudit.dto.InventoryAuditDiffDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditInsertDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for inventory audit entity/DTO conversions.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface IInventoryAuditMapper {

    @Mapping(source = "realLocation.id", target = "realLocationId")
    InventoryAuditInsertDto toInsertDto(InventoryAudit entity);

    List<InventoryAuditInsertDto> toInsertDto(List<InventoryAudit> entities);

    @Mapping(source = "card.name", target = "cardName")
    @Mapping(source = "realLocation.id", target = "realLocationId")
    @Mapping(source = "realLocation.name", target = "realLocationName")
    InventoryAuditDto toDto(InventoryAudit entity);

    List<InventoryAuditDto> toDto(List<InventoryAudit> entities);

    @Mapping(source = "id", target = "cardId")
    @Mapping(source = "name", target = "cardName")
    @Mapping(source = "inventoryAudit.realLocation.id", target = "realLocationId")
    @Mapping(source = "inventoryAudit.realLocation.name", target = "realLocationName")
    @Mapping(source = "location.id", target = "expectedLocationId")
    @Mapping(source = "location.name", target = "expectedLocationName")
    @Mapping(target = "status",
            expression = "java(mapStatus(entity))")
    @Mapping(source = "inventoryAudit.action", target = "action")
    @Mapping(source = "inventoryAudit.writeOffDate", target = "writeOffDate")
    @Mapping(source = "inventoryAudit.writeOffMethod.id", target = "writeOffMethodId")
    InventoryAuditDiffDto toDiffDto(Card entity);

    default InventoryAuditStatus mapStatus(Card entity) {
        if (entity.getInventoryAudit() == null || entity.getInventoryAudit().getRealLocation() == null) {
            return InventoryAuditStatus.WRONG;
        }

        String realId = entity.getInventoryAudit().getRealLocation().getId();
        String expectedId = entity.getLocation().getId();

        if (!realId.equals(expectedId)) {
            return InventoryAuditStatus.WARN;
        }

        return InventoryAuditStatus.CORRECT;
    }
}
