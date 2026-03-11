package cz.majetek.attachment.controller;

import cz.majetek.attachment.dto.AttachmentDto;
import cz.majetek.attachment.service.IAttachmentService;
import cz.majetek.entity.Attachment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * REST controller for card attachment upload, download and deletion.
 */
@RestController
@RequestMapping("/api/card/{cardId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment Controller", description = "Handles upload, retrieval and deletion of attachments")
public class AttachmentController {

    private final IAttachmentService attachmentService;

    /**
     * Upload attachment for a card.
     */
    @PostMapping
    @Operation(summary = "Upload attachment", description = "Upload attachment for given card")
    @ApiResponse(responseCode = "201", description = "Attachment uploaded successfully")
    public ResponseEntity<AttachmentDto> uploadAttachment(
            @PathVariable Integer cardId,
            @RequestParam MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attachmentService.upload(cardId, file));
    }

    /**
     * List all attachments for a card.
     */
    @GetMapping
    @Operation(summary = "List attachments", description = "List all attachments for the given card")
    @ApiResponse(responseCode = "200", description = "List of attachments",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Attachment.class)))
    public ResponseEntity<List<AttachmentDto>> listAttachments(
            @PathVariable Integer cardId) {
        return ResponseEntity.ok(attachmentService.getAttachments(cardId));
    }

    /**
     * Download attachment by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Download attachment", description = "Download a specific attachment by its ID")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Integer cardId,
            @PathVariable Integer id) {

        Resource resource = attachmentService.download(cardId, id);

        String mimeType;
        try {
            mimeType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            mimeType = null;
        }

        MediaType mediaType = mimeType != null
                ? MediaType.parseMediaType(mimeType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * Delete attachment by ID.
     */
    @DeleteMapping("{id}")
    @Operation(summary = "Delete attachment", description = "Delete a specific attachment by its ID")
    @ApiResponse(responseCode = "204", description = "Attachment deleted")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Integer cardId,
            @PathVariable Integer id) {
        attachmentService.delete(cardId, id);
        return ResponseEntity.noContent().build();
    }
}
