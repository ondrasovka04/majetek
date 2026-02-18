package cz.majetek.dictionary.purchasemethod.controller;

import cz.majetek.dictionary.purchasemethod.dto.UpdatePurchaseMethodRequest;
import cz.majetek.dictionary.purchasemethod.service.IPurchaseMethodService;
import cz.majetek.entity.PurchaseMethod;
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
@RequestMapping("/api/purchase-method")
@RequiredArgsConstructor
@Tag(name = "PurchaseMethod Controller", description = "Manages purchase method dictionary")
public class PurchaseMethodController {
    private final IPurchaseMethodService purchaseMethodService;

    /**
     * Get all purchase methods.
     */
    @GetMapping
    @Operation(summary = "Get all purchase methods", description = "Returns a list of all purchase methods")
    @ApiResponse(responseCode = "200", description = "List of purchase methods",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseMethod.class)))
    public ResponseEntity<List<PurchaseMethod>> getAll() {
        return ResponseEntity.ok(purchaseMethodService.getAll());
    }

    /**
     * Get purchase method by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Get purchase method by ID", description = "Returns a purchase method by its ID")
    @ApiResponse(responseCode = "200", description = "PurchaseMethod found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseMethod.class)))
    public ResponseEntity<PurchaseMethod> getById(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") String id) {
        return ResponseEntity.ok(purchaseMethodService.getById(id));
    }

    /**
     * Create a new purchase method.
     */
    @PostMapping
    @Operation(summary = "Create purchase method", description = "Creates a new purchase method")
    @ApiResponse(responseCode = "201", description = "PurchaseMethod created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PurchaseMethod.class)))
    public ResponseEntity<PurchaseMethod> create(
            @RequestBody @Valid @Parameter(description = "PurchaseMethod to create") PurchaseMethod purchaseMethod) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseMethodService.create(purchaseMethod));
    }

    /**
     * Update existing purchase method.
     */
    @PatchMapping("{id}")
    @Operation(summary = "Update purchase method", description = "Updates the name of a purchase method")
    @ApiResponse(responseCode = "200", description = "Updated purchase method",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PurchaseMethod.class)))
    public ResponseEntity<PurchaseMethod> update(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable String id,
            @RequestBody @Valid @Parameter(description = "New purchase method name") UpdatePurchaseMethodRequest request) {

        return ResponseEntity.ok(purchaseMethodService.update(id, request));
    }

    /**
     * Delete purchase method by ID.
     */
    @DeleteMapping("{id}")
    @Operation(summary = "Delete purchase method ", description = "Deletes a purchase method by ID")
    @ApiResponse(responseCode = "204", description = "PurchaseMethod deleted")
    public ResponseEntity<Void> delete(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") String id) {
        purchaseMethodService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
