package cz.majetek.card.mapper;


import cz.majetek.card.dto.CardChangeDto;
import cz.majetek.card.dto.CardDto;
import cz.majetek.entity.*;
import cz.majetek.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for card entity/DTO conversions with named reference resolvers.
 */
@Mapper(componentModel = "spring")
public interface ICardMapper {

    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "location.name", target = "locationName")
    @Mapping(source = "assetClass.id", target = "classId")
    @Mapping(source = "assetClass.name", target = "className")
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "group.name", target = "groupName")
    @Mapping(source = "purchaseMethod.id", target = "purchaseMethodId")
    @Mapping(source = "purchaseMethod.name", target = "purchaseMethodName")
    @Mapping(source = "writeOffMethod.id", target = "writeOffMethodId")
    @Mapping(source = "writeOffMethod.name", target = "writeOffMethodName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "createdBy.username", target = "createdByName")
    @Mapping(source = "responsiblePerson.id", target = "responsiblePersonId")
    @Mapping(source = "responsiblePerson.username", target = "responsiblePersonName")
    @Mapping(source = "serialNumber", target = "serialNumber")
    CardDto toDto(Card card);

    List<CardDto> toDto(List<Card> cards);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(source = "locationId", target = "location", qualifiedByName = "toLocation")
    @Mapping(source = "classId", target = "assetClass", qualifiedByName = "toClass")
    @Mapping(source = "groupId", target = "group", qualifiedByName = "toGroup")
    @Mapping(source = "purchaseMethodId", target = "purchaseMethod", qualifiedByName = "toPurchaseMethod")
    @Mapping(source = "writeOffMethodId", target = "writeOffMethod", qualifiedByName = "toWriteOffMethod")
    @Mapping(source = "createdById", target = "createdBy", qualifiedByName = "toUser")
    @Mapping(source = "responsiblePersonId", target = "responsiblePerson", qualifiedByName = "toUser")
    Card toEntity(CardChangeDto dto);

    @Named("toLocation")
    default Location toLocation(String id) {
        if (id == null) return null;
        Location l = new Location();
        l.setId(id);
        return l;
    }

    @Named("toClass")
    default ClassEntity toClass(String id) {
        if (id == null) return null;
        ClassEntity c = new ClassEntity();
        c.setId(id);
        return c;
    }

    @Named("toGroup")
    default AssetGroup toGroup(String id) {
        if (id == null) return null;
        AssetGroup g = new AssetGroup();
        g.setId(id);
        return g;
    }

    @Named("toPurchaseMethod")
    default PurchaseMethod toPurchaseMethod(String id) {
        if (id == null) return null;
        PurchaseMethod p = new PurchaseMethod();
        p.setId(id);
        return p;
    }

    @Named("toWriteOffMethod")
    default WriteOffMethod toWriteOffMethod(String id) {
        if (id == null) return null;
        WriteOffMethod w = new WriteOffMethod();
        w.setId(id);
        return w;
    }

    @Named("toUser")
    default User toUser(String id) {
        if (id == null) return null;
        User u = new User();
        u.setId(id);
        return u;
    }
}