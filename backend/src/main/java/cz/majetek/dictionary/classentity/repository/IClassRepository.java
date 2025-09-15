package cz.majetek.dictionary.classentity.repository;

import cz.majetek.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClassRepository extends JpaRepository<ClassEntity, String> {
}
