package cz.majetek.dictionary.classentity.service.implementation;

import cz.majetek.dictionary.classentity.dto.UpdateClassRequest;
import cz.majetek.dictionary.classentity.repository.IClassRepository;
import cz.majetek.dictionary.classentity.service.IClassService;
import cz.majetek.entity.ClassEntity;
import cz.majetek.exception.dictionary.ClassEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of class service interface.
 */
@Service
@RequiredArgsConstructor
public class ClassService implements IClassService {
    private final IClassRepository classEntityRepository;

    @Override
    public List<ClassEntity> getAll() {
        return classEntityRepository.findAll();
    }

    @Override
    public ClassEntity getById(String id) {
        return classEntityRepository.findById(id).orElseThrow(
                () -> ClassEntityException.notFound(id));
    }

    @Override
    public ClassEntity create(ClassEntity classEntity) {
        if (classEntityRepository.existsById(classEntity.getId())) {
            throw new ClassEntityException("Class with id " + classEntity.getId() + " already exists");
        }
        return classEntityRepository.save(classEntity);
    }

    @Override
    public ClassEntity update(String id, UpdateClassRequest request) {
        ClassEntity existing = classEntityRepository.findById(id)
                .orElseThrow(() -> ClassEntityException.notFound(id));

        existing.setName(request.getName());
        return classEntityRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        try {
            classEntityRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ClassEntityException("Class cannot be deleted because it is being used.");
        }
    }
}
