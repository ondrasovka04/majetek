package cz.majetek.dictionary.writeoffmethod.repository;

import cz.majetek.entity.WriteOffMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWriteOffMethodRepository extends JpaRepository<WriteOffMethod, String> {
}
