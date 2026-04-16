package cz.majetek.attachment.mapper;

import cz.majetek.attachment.dto.AttachmentDto;
import cz.majetek.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for attachment entity/DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface IAttachmentMapper {

    @Mapping(source = "id.id", target = "id")
    @Mapping(source = "id.cardId", target = "cardId")
    AttachmentDto toDto(Attachment attachment);

    List<AttachmentDto> toDto(List<Attachment> attachments);
}
