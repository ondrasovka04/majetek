package cz.majetek.attachment.service;

import cz.majetek.attachment.dto.AttachmentDto;
import cz.majetek.attachment.mapper.IAttachmentMapper;
import cz.majetek.attachment.repository.IAttachmentRepository;
import cz.majetek.attachment.service.implementation.AttachmentService;
import cz.majetek.card.repository.ICardRepository;
import cz.majetek.entity.Attachment;
import cz.majetek.entity.Card;
import cz.majetek.exception.attachment.AttachmentException;
import cz.majetek.exception.card.CardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private IAttachmentRepository attachmentRepository;

    @Mock
    private ICardRepository cardRepository;

    @Mock
    private IAttachmentMapper attachmentMapper;

    @InjectMocks
    private AttachmentService attachmentService;

    @TempDir
    Path tempDir;

    private Card testCard;
    private Attachment testAttachment;
    private AttachmentDto testAttachmentDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "uploadRoot", tempDir.toString());

        testCard = new Card();
        testCard.setId(1);
        testCard.setName("Test Card");

        Attachment.AttachmentId attachmentId = new Attachment.AttachmentId(1, 1);
        testAttachment = new Attachment();
        testAttachment.setId(attachmentId);
        testAttachment.setName("test.pdf");
        testAttachment.setFilePath(tempDir.resolve("1/uuid-test.pdf").toString());
        testAttachment.setCard(testCard);

        testAttachmentDto = new AttachmentDto();
        testAttachmentDto.setId(1);
        testAttachmentDto.setCardId(1);
        testAttachmentDto.setName("test.pdf");
    }

    @Test
    void upload_WithValidFile_ShouldUploadSuccessfully() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(cardRepository.findByIdForUpdate(1)).thenReturn(Optional.of(testCard));
        when(attachmentRepository.findNextIdForCard(1)).thenReturn(1);
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(testAttachment);
        when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(testAttachmentDto);

        AttachmentDto result = attachmentService.upload(1, file);

        assertNotNull(result);
        assertEquals("test.pdf", result.getName());
        verify(cardRepository).findByIdForUpdate(1);
        verify(attachmentRepository).save(any(Attachment.class));

        // Verify file was created
        assertTrue(Files.exists(tempDir.resolve("1")));
    }

    @Test
    void upload_WithNonExistingCard_ShouldThrowException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(cardRepository.findByIdForUpdate(999)).thenReturn(Optional.empty());

        assertThrows(CardException.class, () -> attachmentService.upload(999, file));
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void upload_WithNullFilename_ShouldThrowException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                null,
                "application/pdf",
                "test content".getBytes()
        );

        when(cardRepository.findByIdForUpdate(1)).thenReturn(Optional.of(testCard));

        assertThrows(AttachmentException.class, () -> attachmentService.upload(1, file));
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void upload_WithBlankFilename_ShouldThrowException() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "   ",
                "application/pdf",
                "test content".getBytes()
        );

        when(cardRepository.findByIdForUpdate(1)).thenReturn(Optional.of(testCard));

        assertThrows(AttachmentException.class, () -> attachmentService.upload(1, file));
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void upload_WithIOException_ShouldThrowAttachmentException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        doThrow(new IOException("disk full")).when(file).transferTo(any(Path.class));

        when(cardRepository.findByIdForUpdate(1)).thenReturn(Optional.of(testCard));

        assertThrows(AttachmentException.class, () -> attachmentService.upload(1, file));
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void getAttachments_ShouldReturnAllAttachmentsForCard() {
        List<Attachment> attachments = List.of(testAttachment);
        List<AttachmentDto> attachmentDtos = List.of(testAttachmentDto);

        when(attachmentRepository.findAllByIdCardId(1)).thenReturn(attachments);
        when(attachmentMapper.toDto(attachments)).thenReturn(attachmentDtos);

        List<AttachmentDto> result = attachmentService.getAttachments(1);

        assertEquals(1, result.size());
        assertEquals(testAttachmentDto, result.getFirst());
        verify(attachmentRepository).findAllByIdCardId(1);
    }

    @Test
    void download_WithExistingFile_ShouldReturnResource() throws IOException {
        // Create actual file for testing
        Path cardFolder = tempDir.resolve("1");
        Files.createDirectories(cardFolder);
        Path filePath = cardFolder.resolve("uuid-test.pdf");
        Files.write(filePath, "test content".getBytes());

        testAttachment.setFilePath(filePath.toString());
        Attachment.AttachmentId id = new Attachment.AttachmentId(1, 1);

        when(attachmentRepository.findById(id)).thenReturn(Optional.of(testAttachment));

        Resource resource = attachmentService.download(1, 1);

        assertNotNull(resource);
        assertTrue(resource.exists());
        verify(attachmentRepository).findById(id);
    }

    @Test
    void download_WithNonExistingAttachment_ShouldThrowException() {
        Attachment.AttachmentId id = new Attachment.AttachmentId(1, 999);
        when(attachmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AttachmentException.class, () -> attachmentService.download(999, 1));
    }

    @Test
    void download_WithNonExistingFile_ShouldThrowException() {
        Attachment.AttachmentId id = new Attachment.AttachmentId(1, 1);
        testAttachment.setFilePath("/nonexistent/file.pdf");

        when(attachmentRepository.findById(id)).thenReturn(Optional.of(testAttachment));

        assertThrows(AttachmentException.class, () -> attachmentService.download(1, 1));
    }

    @Test
    void delete_WithExistingAttachment_ShouldDeleteSuccessfully() throws IOException {
        // Create actual file for testing
        Path cardFolder = tempDir.resolve("1");
        Files.createDirectories(cardFolder);
        Path filePath = cardFolder.resolve("uuid-test.pdf");
        Files.write(filePath, "test content".getBytes());

        testAttachment.setFilePath(filePath.toString());
        Attachment.AttachmentId id = new Attachment.AttachmentId(1, 1);

        when(attachmentRepository.findById(id)).thenReturn(Optional.of(testAttachment));
        doNothing().when(attachmentRepository).delete(testAttachment);

        assertDoesNotThrow(() -> attachmentService.delete(1, 1));

        assertFalse(Files.exists(filePath));
        verify(attachmentRepository).delete(testAttachment);
    }

    @Test
    void delete_WithNonExistingAttachment_ShouldThrowException() {
        Attachment.AttachmentId id = new Attachment.AttachmentId(1, 999);
        when(attachmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AttachmentException.class, () -> attachmentService.delete(999, 1));
        verify(attachmentRepository, never()).delete(any(Attachment.class));
    }
}


