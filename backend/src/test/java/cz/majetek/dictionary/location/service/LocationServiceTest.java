package cz.majetek.dictionary.location.service;

import cz.majetek.dictionary.location.dto.UpdateLocationRequest;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.location.service.implementation.LocationService;
import cz.majetek.entity.Location;
import cz.majetek.exception.dictionary.LocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private ILocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new Location();
        testLocation.setId("LOC1");
        testLocation.setName("Test Location");
        ReflectionTestUtils.setField(locationService, "exportPath", "/tmp/export");
    }

    @Test
    void getAll_ShouldReturnAllLocations() {
        List<Location> locations = List.of(testLocation);
        when(locationRepository.findAll()).thenReturn(locations);

        List<Location> result = locationService.getAll();

        assertEquals(1, result.size());
        assertEquals(testLocation, result.getFirst());
        verify(locationRepository).findAll();
    }

    @Test
    void getById_WithExistingId_ShouldReturnLocation() {
        when(locationRepository.findById("LOC1")).thenReturn(Optional.of(testLocation));

        Location result = locationService.getById("LOC1");

        assertEquals(testLocation, result);
        verify(locationRepository).findById("LOC1");
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowException() {
        when(locationRepository.findById("LOC999")).thenReturn(Optional.empty());

        assertThrows(LocationException.class, () -> locationService.getById("LOC999"));
        verify(locationRepository).findById("LOC999");
    }

    @Test
    void create_WithNewLocation_ShouldCreateSuccessfully() {
        Location newLocation = new Location();
        newLocation.setId("LOC2");
        newLocation.setName("New Location");

        when(locationRepository.existsById("LOC2")).thenReturn(false);
        when(locationRepository.save(newLocation)).thenReturn(newLocation);

        Location result = locationService.create(newLocation);

        assertEquals(newLocation, result);
        verify(locationRepository).existsById("LOC2");
        verify(locationRepository).save(newLocation);
    }

    @Test
    void create_WithExistingId_ShouldThrowException() {
        Location existingLocation = new Location();
        existingLocation.setId("LOC1");
        existingLocation.setName("Existing");

        when(locationRepository.existsById("LOC1")).thenReturn(true);

        assertThrows(LocationException.class, () -> locationService.create(existingLocation));
        verify(locationRepository).existsById("LOC1");
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void update_WithExistingLocation_ShouldUpdateSuccessfully() {
        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setName("Updated Location");
        Location updatedLocation = new Location();
        updatedLocation.setId("LOC1");
        updatedLocation.setName("Updated Location");

        when(locationRepository.findById("LOC1")).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(updatedLocation);

        Location result = locationService.update("LOC1", request);

        assertEquals("Updated Location", result.getName());
        verify(locationRepository).findById("LOC1");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        UpdateLocationRequest request = new UpdateLocationRequest();
        request.setName("Updated Location");
        when(locationRepository.findById("LOC999")).thenReturn(Optional.empty());

        assertThrows(LocationException.class, () -> locationService.update("LOC999", request));
        verify(locationRepository).findById("LOC999");
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSuccessfully() {
        doNothing().when(locationRepository).deleteById("LOC1");

        assertDoesNotThrow(() -> locationService.delete("LOC1"));
        verify(locationRepository).deleteById("LOC1");
    }

    @Test
    void delete_WithLocationInUse_ShouldThrowException() {
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(locationRepository).deleteById("LOC1");

        LocationException exception = assertThrows(LocationException.class,
                () -> locationService.delete("LOC1"));

        assertTrue(exception.getMessage().contains("being used"));
        verify(locationRepository).deleteById("LOC1");
    }
}

