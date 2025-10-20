package cz.majetek.inventoryaudit.controller;

import cz.majetek.inventoryaudit.dto.InventoryAuditDiffDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditInsertDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditPatchDto;
import cz.majetek.inventoryaudit.service.IInventoryAuditService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/inventory-audit")
@RequiredArgsConstructor
@Tag(name = "Inventory Audit Controller", description = "Handles inventory audit insertions and reads")
public class InventoryAuditController {

    private final IInventoryAuditService auditService;

    @GetMapping
    @Operation(summary = "Get all inventory audit records", description = "Returns a list of all inventory audit records")
    @ApiResponse(responseCode = "200", description = "List of inventory audit records",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InventoryAuditDto.class)))
    public ResponseEntity<List<InventoryAuditDto>> getAll() {
        return ResponseEntity.ok(auditService.getAll());
    }

    @GetMapping("/differences")
    @Operation(summary = "Get all inventory audit differences", description = "Returns a list of all inventory audit differences")
    @ApiResponse(responseCode = "200", description = "List of inventory audit differences",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InventoryAuditDiffDto.class)))
    public ResponseEntity<List<InventoryAuditDiffDto>> getDiffs() {
        return ResponseEntity.ok(auditService.getDiffs());
    }

    @PostMapping("/insert-many")
    @Operation(summary = "Insert multiple inventory audit records", description = "Creates multiple inventory audit entries in a single request")
    @ApiResponse(responseCode = "201", description = "Inventory audit records created",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InventoryAuditInsertDto.class)))
    public ResponseEntity<List<InventoryAuditInsertDto>> insertMany(
            @Valid @RequestBody List<InventoryAuditInsertDto> request
    ) {
        List<InventoryAuditInsertDto> created = auditService.insertMany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{cardId}")
    @Operation(summary = "Update inventory audit record", description = "Updates action, write-off date and method for given card ID")
    @ApiResponse(responseCode = "200", description = "Inventory audit record updated")
    public ResponseEntity<Void> patch(
            @PathVariable Integer cardId,
            @Valid @RequestBody InventoryAuditPatchDto request
    ) {
        auditService.patch(cardId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/project")
    @Operation(summary = "Project inventory differences", description = "Projects inventory differences based on current inventory audit data")
    @ApiResponse(responseCode = "200", description = "Inventory differences projected",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InventoryAuditInsertDto.class)))
    public ResponseEntity<Void> project() {
        auditService.project();
        return ResponseEntity.ok().build();
    }
}
