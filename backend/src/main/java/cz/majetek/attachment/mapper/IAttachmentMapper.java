package cz.majetek.attachment.mapper;

import cz.majetek.attachment.dto.AttachmentDto;
import cz.majetek.entity.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IAttachmentMapper {

    @Mapping(source = "id.id", target = "id")
    @Mapping(source = "id.cardId", target = "cardId")
    @Mapping(target = "name", source = "name")
    AttachmentDto toDto(Attachment attachment);

    List<AttachmentDto> toDto(List<Attachment> attachments);
}
