package cz.majetek.dictionary.classentity.controller;

import cz.majetek.dictionary.classentity.dto.UpdateClassRequest;
import cz.majetek.dictionary.classentity.service.IClassService;
import cz.majetek.entity.ClassEntity;
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

/**
 * REST controller for class dictionary CRUD operations.
 */
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
@Tag(name = "ClassEntity Controller", description = "Manages class dictionary")
public class ClassController {
    private final IClassService classService;

    /**
     * Get all classes.
     */
    @GetMapping
    @Operation(summary = "Get all classes", description = "Returns a list of all classes")
    @ApiResponse(responseCode = "200", description = "List of classes",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassEntity.class)))
    public ResponseEntity<List<ClassEntity>> getAll() {
        return ResponseEntity.ok(classService.getAll());
    }

    /**
     * Get class by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Get class by ID", description = "Returns a class by its ID")
    @ApiResponse(responseCode = "200", description = "Class found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassEntity.class)))
    public ResponseEntity<ClassEntity> getById(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") String id) {
        return ResponseEntity.ok(classService.getById(id));
    }

    /**
     * Create a new class.
     */
    @PostMapping
    @Operation(summary = "Create class", description = "Creates a new class")
    @ApiResponse(responseCode = "201", description = "Class created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClassEntity.class)))
    public ResponseEntity<ClassEntity> create(
            @RequestBody @Valid @Parameter(description = "Class to create") ClassEntity classEntity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.create(classEntity));
    }

    /**
     * Update existing class.
     */
    @PatchMapping("{id}")
    @Operation(summary = "Update class", description = "Updates the name of a class")
    @ApiResponse(responseCode = "200", description = "Updated class",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ClassEntity.class)))
    public ResponseEntity<ClassEntity> update(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable String id,
            @RequestBody @Valid @Parameter(description = "New class name") UpdateClassRequest request) {

        return ResponseEntity.ok(classService.update(id, request));
    }

    /**
     * Delete class by ID.
     */
    @DeleteMapping("{id}")
    @Operation(summary = "Delete class", description = "Deletes a class by ID")
    @ApiResponse(responseCode = "204", description = "Class deleted")
    public ResponseEntity<Void> delete(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") String id) {
        classService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
