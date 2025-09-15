package cz.majetek.dictionary.writeoffmethod.controller;

import cz.majetek.dictionary.writeoffmethod.dto.UpdateWriteOffMethodRequest;
import cz.majetek.dictionary.writeoffmethod.service.IWriteOffMethodService;
import cz.majetek.entity.WriteOffMethod;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/write-off-method")
@RequiredArgsConstructor
@Tag(name = "WriteOffMethod Controller", description = "Manages write-off method dictionary")
public class WriteOffMethodController {
    private final IWriteOffMethodService writeOffMethodService;

    /**
     * Get all write-off methods.
     */
    @GetMapping
    @Operation(summary = "Get all write-off methods", description = "Returns a list of all write-off methods")
    @ApiResponse(responseCode = "200", description = "List of write-off methods",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WriteOffMethod.class)))
    public ResponseEntity<List<WriteOffMethod>> getAll() {
        return ResponseEntity.ok(writeOffMethodService.getAll());
    }

    /**
     * Get write-off method by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Get write-off method by ID", description = "Returns a write-off method by its ID")
    @ApiResponse(responseCode = "200", description = "WriteOffMethod found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WriteOffMethod.class)))
    public ResponseEntity<WriteOffMethod> getById(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") String id) {
        return ResponseEntity.ok(writeOffMethodService.getById(id));
    }

    /**
     * Create a new write-off method.
     */
    @PostMapping
    @Operation(summary = "Create write-off method", description = "Creates a new write-off method")
    @ApiResponse(responseCode = "200", description = "WriteOffMethod created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WriteOffMethod.class)))
    public ResponseEntity<WriteOffMethod> create(
            @RequestBody @Valid @Parameter(description = "WriteOffMethod to create") WriteOffMethod purchaseMethod) {
        return ResponseEntity.ok(writeOffMethodService.create(purchaseMethod));
    }

    /**
     * Update existing write-off method.
     */
    @PatchMapping("{id}")
    @Operation(summary = "Update write-off method", description = "Updates the name of a write-off method")
    @ApiResponse(responseCode = "200", description = "Updated write-off method",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WriteOffMethod.class)))
    public ResponseEntity<WriteOffMethod> update(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable String id,
            @RequestBody @Valid @Parameter(description = "New write-off method name") UpdateWriteOffMethodRequest request) {

        return ResponseEntity.ok(writeOffMethodService.update(id, request));
    }

    /**
     * Delete write-off method by ID.
     */
    @DeleteMapping("{id}")
    @Operation(summary = "Delete write-off method ", description = "Deletes a write-off method by ID")
    @ApiResponse(responseCode = "204", description = "WriteOffMethod deleted")
    public ResponseEntity<Void> delete(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") String id) {
        writeOffMethodService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
