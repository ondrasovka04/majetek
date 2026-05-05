package cz.majetek.dictionary.assetgroup.controller;

import cz.majetek.dictionary.assetgroup.dto.UpdateAssetGroupRequest;
import cz.majetek.dictionary.assetgroup.service.IAssetGroupService;
import cz.majetek.entity.AssetGroup;
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
 * REST controller for asset group dictionary CRUD operations.
 */
@RestController
@RequestMapping("/api/asset-group")
@RequiredArgsConstructor
@Tag(name = "AssetGroup Controller", description = "Manages asset group dictionary")
public class AssetGroupController {

    private final IAssetGroupService assetGroupService;

    /**
     * Get all asset groups.
     */
    @GetMapping
    @Operation(summary = "Get all asset groups", description = "Returns a list of all asset groups")
    @ApiResponse(responseCode = "200", description = "List of asset groups",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetGroup.class)))
    public ResponseEntity<List<AssetGroup>> getAll() {
        return ResponseEntity.ok(assetGroupService.getAll());
    }

    /**
     * Get an asset group by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Get asset group by ID", description = "Returns an asset group by its ID")
    @ApiResponse(responseCode = "200", description = "Asset group found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetGroup.class)))
    public ResponseEntity<AssetGroup> getById(
            @PathVariable @Parameter(name = "id", required = true, in = ParameterIn.PATH) String id) {
        return ResponseEntity.ok(assetGroupService.getById(id));
    }

    /**
     * Create a new asset group.
     */
    @PostMapping
    @Operation(summary = "Create asset group", description = "Creates a new asset group")
    @ApiResponse(responseCode = "201", description = "Asset group created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssetGroup.class)))
    public ResponseEntity<AssetGroup> create(
            @RequestBody @Valid @Parameter(description = "Asset group to create") AssetGroup assetGroup) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetGroupService.create(assetGroup));
    }

    /**
     * Update an existing asset group.
     */
    @PatchMapping("{id}")
    @Operation(summary = "Update asset group", description = "Updates the name of an asset group")
    @ApiResponse(responseCode = "200", description = "Updated asset group",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AssetGroup.class)))
    public ResponseEntity<AssetGroup> update(
            @Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable String id,
            @RequestBody @Valid @Parameter(description = "New asset group name") UpdateAssetGroupRequest request) {

        return ResponseEntity.ok(assetGroupService.update(id, request));
    }

    /**
     * Delete an asset group by ID.
     */
    @DeleteMapping("{id}")
    @Operation(summary = "Delete asset group", description = "Deletes an asset group by ID")
    @ApiResponse(responseCode = "204", description = "Asset group deleted")
    public ResponseEntity<Void> delete(
            @PathVariable @Parameter(name = "id", required = true, in = ParameterIn.PATH) String id) {
        assetGroupService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
