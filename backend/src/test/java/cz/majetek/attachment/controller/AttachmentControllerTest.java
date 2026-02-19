package cz.majetek.attachment.controller;

import cz.majetek.attachment.dto.AttachmentDto;
import cz.majetek.attachment.service.IAttachmentService;
import cz.majetek.exception.ErrorResponseUtils;
import cz.majetek.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAttachmentService attachmentService;

    @InjectMocks
    private AttachmentController attachmentController;

    @TempDir
    Path tempDir;

    private AttachmentDto testAttachmentDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attachmentController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();

        testAttachmentDto = new AttachmentDto();
        testAttachmentDto.setId(1);
        testAttachmentDto.setName("test.pdf");
    }

    @Test
    void uploadAttachment_ShouldReturnCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test content".getBytes());
        when(attachmentService.upload(eq(1), any(MultipartFile.class))).thenReturn(testAttachmentDto);

        mockMvc.perform(multipart("/api/card/1/attachments").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test.pdf"));
    }

    @Test
    void listAttachments_ShouldReturnAttachmentList() throws Exception {
        when(attachmentService.getAttachments(1)).thenReturn(List.of(testAttachmentDto));

        mockMvc.perform(get("/api/card/1/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("test.pdf"));
    }

    @Test
    void downloadAttachment_ShouldReturnResource() throws Exception {
        Path pdfFile = tempDir.resolve("test.pdf");
        Files.write(pdfFile, "test content".getBytes());
        Resource resource = new FileSystemResource(pdfFile.toFile());

        when(attachmentService.download(1, 1)).thenReturn(resource);

        mockMvc.perform(get("/api/card/1/attachments/1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void downloadAttachment_KnownMimeType_ShouldReturnCorrectType() throws Exception {
        Path txtFile = tempDir.resolve("test.txt");
        Files.writeString(txtFile, "hello world");
        Resource resource = new FileSystemResource(txtFile.toFile());

        when(attachmentService.download(1, 5)).thenReturn(resource);

        mockMvc.perform(get("/api/card/1/attachments/5"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void downloadAttachment_UnknownMimeType_ShouldReturnOctetStream() throws Exception {
        Path unknownFile = tempDir.resolve("data.xyz123");
        Files.writeString(unknownFile, "binary-ish");
        Resource resource = new FileSystemResource(unknownFile.toFile());

        when(attachmentService.download(1, 6)).thenReturn(resource);

        mockMvc.perform(get("/api/card/1/attachments/6"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void deleteAttachment_ShouldReturn204() throws Exception {
        doNothing().when(attachmentService).delete(1, 1);

        mockMvc.perform(delete("/api/card/1/attachments/1"))
                .andExpect(status().isNoContent());

        verify(attachmentService).delete(1, 1);
    }
}
