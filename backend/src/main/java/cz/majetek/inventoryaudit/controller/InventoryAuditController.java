package cz.majetek.inventoryaudit.controller;

import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
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

    @PostMapping("/insert-many")
    @Operation(summary = "Insert multiple inventory audit records", description = "Creates multiple inventory audit entries in a single request")
    @ApiResponse(responseCode = "201", description = "Inventory audit records created",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = InventoryAuditDto.class)))
    public ResponseEntity<List<InventoryAuditDto>> insertMany(
            @Valid @RequestBody List<InventoryAuditDto> request
    ) {
        List<InventoryAuditDto> created = auditService.insertMany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
