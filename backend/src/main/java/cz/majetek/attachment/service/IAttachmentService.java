package cz.majetek.attachment.service;

import cz.majetek.attachment.dto.AttachmentDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for file attachment management.
 * Handles upload, download, listing, and deletion of attachments linked to asset cards.
 */
public interface IAttachmentService {

    /**
     * Uploads a file attachment to the given card.
     *
     * @param cardId card ID
     * @param file   uploaded file
     * @return metadata of the stored attachment
     */
    AttachmentDto upload(Integer cardId, MultipartFile file);

    /**
     * Returns all attachments for the given card.
     *
     * @param cardId card ID
     * @return list of attachment metadata
     */
    List<AttachmentDto> getAttachments(Integer cardId);

    /**
     * Downloads an attachment file.
     *
     * @param cardId card ID
     * @param id     attachment ID
     * @return file resource for streaming
     */
    Resource download(Integer cardId, Integer id);

    /**
     * Deletes an attachment.
     *
     * @param cardId card ID
     * @param id     attachment ID
     */
    void delete(Integer cardId, Integer id);
}
