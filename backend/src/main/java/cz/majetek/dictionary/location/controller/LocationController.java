package cz.majetek.dictionary.location.controller;

import cz.majetek.card.dto.PrintResponse;
import cz.majetek.dictionary.location.dto.PrintRequest;
import cz.majetek.dictionary.location.dto.UpdateLocationRequest;
import cz.majetek.dictionary.location.service.ILocationService;
import cz.majetek.entity.Location;
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
 * REST controller for location dictionary CRUD and label printing.
 */
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Tag(name = "Location Controller", description = "Manages location dictionary")
public class LocationController {
    private final ILocationService locationService;

    /**
     * Get all locations.
     */
    @GetMapping
    @Operation(summary = "Get all locations", description = "Returns a list of all locations")
    @ApiResponse(responseCode = "200", description = "List of locations", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Location.class)))
    public ResponseEntity<List<Location>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    /**
     * Get location by ID.
     */
    @GetMapping("{id}")
    @Operation(summary = "Get location by ID", description = "Returns a location by its ID")
    @ApiResponse(responseCode = "200", description = "Location found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Location.class)))
    public ResponseEntity<Location> getById(@PathVariable @Parameter(name = "id", required = true, in = ParameterIn.PATH) String id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    /**
     * Create a new location.
     */
    @PostMapping
    @Operation(summary = "Create location", description = "Creates a new location")
    @ApiResponse(responseCode = "201", description = "Location created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Location.class)))
    public ResponseEntity<Location> create(@RequestBody @Valid @Parameter(description = "Location to create") Location location) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(location));
    }

    /**
     * Update existing location.
     */
    @PatchMapping("{id}")
    @Operation(summary = "Update location", description = "Updates the name of a location")
    @ApiResponse(responseCode = "200", description = "Updated location", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Location.class)))
    public ResponseEntity<Location> update(@Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable String id, @RequestBody @Valid @Parameter(description = "New location name") UpdateLocationRequest request) {

        return ResponseEntity.ok(locationService.update(id, request));
    }

    /**
     * Delete location by ID.
     */
    @DeleteMapping("{id}")
    @Operation(summary = "Delete location", description = "Deletes a location by ID")
    @ApiResponse(responseCode = "204", description = "Location deleted")
    public ResponseEntity<Void> delete(@PathVariable @Parameter(name = "id", required = true, in = ParameterIn.PATH) String id) {
        locationService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Print location by ID.
     */
    @PostMapping("print")
    @Operation(summary = "Print location", description = "Prints location to cloud")
    @ApiResponse(responseCode = "200", description = "Location printed successfully", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PrintResponse.class))})
    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    public ResponseEntity<PrintResponse> exportLabel(@RequestBody @Parameter(description = "Id of location") PrintRequest request) {
        locationService.exportLocation(request.getId());
        return ResponseEntity.ok(new PrintResponse("Label printed successfully to cloud."));
    }
}
