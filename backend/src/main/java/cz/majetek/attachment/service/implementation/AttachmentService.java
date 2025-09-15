package cz.majetek.attachment.service.implementation;

import cz.majetek.attachment.dto.AttachmentDto;
import cz.majetek.attachment.mapper.IAttachmentMapper;
import cz.majetek.attachment.repository.IAttachmentRepository;
import cz.majetek.attachment.service.IAttachmentService;
import cz.majetek.card.repository.ICardRepository;
import cz.majetek.entity.Attachment;
import cz.majetek.entity.Card;
import cz.majetek.exception.attachment.AttachmentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService implements IAttachmentService {

    private final IAttachmentRepository attachmentRepository;
    private final ICardRepository cardRepository;
    private final IAttachmentMapper attachmentMapper;

    @Value("${attachments.folder}")
    private String uploadRoot;

    @Override
    public AttachmentDto upload(Integer cardId, MultipartFile file) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AttachmentException("Card not found"));

        String uuidName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path cardFolder = Paths.get(uploadRoot, String.valueOf(cardId));
        Path filePath = cardFolder.resolve(uuidName);

        try {
            Files.createDirectories(cardFolder);
            file.transferTo(filePath);
        } catch (IOException e) {
            throw new AttachmentException("File upload failed");
        }

        Attachment.AttachmentId id = new Attachment.AttachmentId(attachmentRepository.findNextIdForCard(cardId), cardId);
        return attachmentMapper.toDto(attachmentRepository.save(new Attachment(id, file.getOriginalFilename(), filePath.toString(), card)));
    }

    @Override
    public List<AttachmentDto> getAttachments(Integer cardId) {
        return attachmentMapper.toDto(attachmentRepository.findAllByIdCardId(cardId));
    }

    @Override
    public Resource download(Integer cardId, Integer id) {
        Attachment attachment = attachmentRepository.findById(new Attachment.AttachmentId(id, cardId))
                .orElseThrow(() -> new AttachmentException("Attachment not found"));

        Path filePath = Path.of(attachment.getFilePath());
        if (!Files.exists(filePath)) {
            throw new AttachmentException("File not found on disk");
        }

        return new FileSystemResource(filePath);
    }

    @Override
    public void delete(Integer cardId, Integer id) {
        Attachment attachment = attachmentRepository.findById(new Attachment.AttachmentId(id, cardId))
                .orElseThrow(() -> new AttachmentException("Attachment not found"));

        try {
            Files.deleteIfExists(Path.of(attachment.getFilePath()));
        } catch (IOException e) {
            throw new AttachmentException("File deletion failed");
        }

        attachmentRepository.delete(attachment);
    }
}

