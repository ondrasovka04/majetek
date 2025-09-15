package cz.majetek.attachment.service;

import cz.majetek.attachment.dto.AttachmentDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IAttachmentService {
    AttachmentDto upload(Integer cardId, MultipartFile file);

    List<AttachmentDto> getAttachments(Integer cardId);

    Resource download(Integer cardId, Integer id);

    void delete(Integer cardId, Integer id);
}
