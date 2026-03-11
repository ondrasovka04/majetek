package cz.majetek.dictionary.location.service;

import cz.majetek.card.service.implementation.LabelPrinter;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.location.service.implementation.LocationService;
import cz.majetek.entity.Location;
import cz.majetek.exception.dictionary.LocationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceExportTest {

    @Mock
    private ILocationRepository locationRepository;

    @Mock
    private LabelPrinter labelPrinter;

    @InjectMocks
    private LocationService locationService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(locationService, "exportPath", tempDir.toString());
    }

    @Test
    void exportLocation_WithValidId_ShouldReturnResource() throws IOException {
        Location loc = new Location();
        loc.setId("LOC1");
        loc.setName("Test Location");
        when(locationRepository.findById("LOC1")).thenReturn(Optional.of(loc));
        when(labelPrinter.generateZplFromLocation(loc)).thenReturn("^XA^FO50,50^A0N,50,50^FDLOC1^FS^XZ");

        InputStreamResource result = locationService.exportLocation("LOC1");

        assertNotNull(result);
        result.getInputStream().close();
        verify(labelPrinter).generateZplFromLocation(loc);
        verify(labelPrinter).sendToZebraCloud(any(File.class));
    }

    @Test
    void exportLocation_WithInvalidId_ShouldThrow() {
        when(locationRepository.findById("INVALID")).thenReturn(Optional.empty());
        assertThrows(LocationException.class, () -> locationService.exportLocation("INVALID"));
    }
}
