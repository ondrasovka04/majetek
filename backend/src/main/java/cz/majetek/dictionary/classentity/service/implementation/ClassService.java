package cz.majetek.dictionary.classentity.service.implementation;

import cz.majetek.dictionary.classentity.dto.UpdateClassRequest;
import cz.majetek.dictionary.classentity.repository.IClassRepository;
import cz.majetek.dictionary.classentity.service.IClassService;
import cz.majetek.entity.ClassEntity;
import cz.majetek.exception.dictionary.ClassEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of class service interface.
 */
@Service
@RequiredArgsConstructor
public class ClassService implements IClassService {
    private final IClassRepository classEntityRepository;

    @Override
    @Cacheable("classes")
    public List<ClassEntity> getAll() {
        return classEntityRepository.findAll();
    }

    @Override
    @Cacheable(value = "classes", key = "#id")
    public ClassEntity getById(String id) {
        return classEntityRepository.findById(id).orElseThrow(
                () -> ClassEntityException.notFound(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "classes", allEntries = true)
    public ClassEntity create(ClassEntity classEntity) {
        if (classEntityRepository.existsById(classEntity.getId())) {
            throw new ClassEntityException("Class with id " + classEntity.getId() + " already exists");
        }
        return classEntityRepository.save(classEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "classes", allEntries = true)
    public ClassEntity update(String id, UpdateClassRequest request) {
        ClassEntity existing = classEntityRepository.findById(id)
                .orElseThrow(() -> ClassEntityException.notFound(id));

        existing.setName(request.getName());
        return classEntityRepository.save(existing);
    }

    @Override
    @Transactional
    @CacheEvict(value = "classes", allEntries = true)
    public void delete(String id) {
        try {
            classEntityRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ClassEntityException("Class cannot be deleted because it is being used.");
        }
    }
}
