package cz.majetek.dictionary.location.service.implementation;

import cz.majetek.card.service.implementation.LabelPrinter;
import cz.majetek.dictionary.location.dto.UpdateLocationRequest;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.location.service.ILocationService;
import cz.majetek.entity.Location;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.LocationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/**
 * Implementation of location service interface.
 */
@Service
@RequiredArgsConstructor
public class LocationService implements ILocationService {
    private final ILocationRepository locationRepository;
    private final LabelPrinter labelPrinter;

    @Value("${export.path}")
    public String exportPath;

    @Override
    @Cacheable("locations")
    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    @Override
    @Cacheable(value = "locations", key = "#id")
    public Location getById(String id) {
        return locationRepository.findById(id).orElseThrow(
                () -> LocationException.notFound(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public Location create(Location location) {
        if (locationRepository.existsById(location.getId())) {
            throw new LocationException("Location with id " + location.getId() + " already exists");
        }
        return locationRepository.save(location);
    }

    @Override
    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public Location update(String id, UpdateLocationRequest request) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> LocationException.notFound(id));

        existing.setName(request.getName());
        return locationRepository.save(existing);
    }

    @Override
    @Transactional
    @CacheEvict(value = "locations", allEntries = true)
    public void delete(String id) {
        try {
            locationRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new LocationException("Location cannot be deleted because it is being used.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportLocation(String id) {
        Location existing = locationRepository.findById(id)
                .orElseThrow(() -> LocationException.notFound(id));

        String zplContent = labelPrinter.generateZplFromLocation(existing);
        Path path = Path.of(exportPath, "location-" + Instant.now().toEpochMilli() + ".zpl");
        try {
            Files.writeString(path, zplContent);

            labelPrinter.sendToZebraCloud(path.toFile());

            return new InputStreamResource(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            throw new CardException("Cannot write ZPL file");
        }
    }
}
