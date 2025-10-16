package cz.majetek.inventoryaudit.mapper;

import cz.majetek.entity.InventoryAudit;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IInventoryAuditMapper {

    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "realLocation.id", target = "realLocationId")
    InventoryAuditDto toDto(InventoryAudit entity);

    List<InventoryAuditDto> toDto(List<InventoryAudit> entities);
}
