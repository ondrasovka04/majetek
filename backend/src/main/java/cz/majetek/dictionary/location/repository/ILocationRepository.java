package cz.majetek.dictionary.location.repository;

import cz.majetek.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Location dictionary entity.
 */
public interface ILocationRepository extends JpaRepository<Location, String> {
}
