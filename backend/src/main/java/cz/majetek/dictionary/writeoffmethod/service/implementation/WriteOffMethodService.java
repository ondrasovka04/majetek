package cz.majetek.dictionary.writeoffmethod.service.implementation;

import cz.majetek.dictionary.writeoffmethod.dto.UpdateWriteOffMethodRequest;
import cz.majetek.dictionary.writeoffmethod.repository.IWriteOffMethodRepository;
import cz.majetek.dictionary.writeoffmethod.service.IWriteOffMethodService;
import cz.majetek.entity.WriteOffMethod;
import cz.majetek.exception.dictionary.WriteOffMethodException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of write-off method service interface.
 */
@Service
@RequiredArgsConstructor
public class WriteOffMethodService implements IWriteOffMethodService {
    private final IWriteOffMethodRepository writeOffMethodRepository;

    @Override
    @Cacheable("writeOffMethods")
    public List<WriteOffMethod> getAll() {
        return writeOffMethodRepository.findAll();
    }

    @Override
    @Cacheable(value = "writeOffMethods", key = "#id")
    public WriteOffMethod getById(String id) {
        return writeOffMethodRepository.findById(id).orElseThrow(
                () -> WriteOffMethodException.notFound(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "writeOffMethods", allEntries = true)
    public WriteOffMethod create(WriteOffMethod writeOffMethod) {
        if (writeOffMethodRepository.existsById(writeOffMethod.getId())) {
            throw new WriteOffMethodException("Write off method with id " + writeOffMethod.getId() + " already exists");
        }
        return writeOffMethodRepository.save(writeOffMethod);
    }

    @Override
    @Transactional
    @CacheEvict(value = "writeOffMethods", allEntries = true)
    public WriteOffMethod update(String id, UpdateWriteOffMethodRequest request) {
        WriteOffMethod existing = writeOffMethodRepository.findById(id)
                .orElseThrow(() -> WriteOffMethodException.notFound(id));

        existing.setName(request.getName());
        return writeOffMethodRepository.save(existing);
    }

    @Override
    @CacheEvict(value = "writeOffMethods", allEntries = true)
    public void delete(String id) {
        try {
            writeOffMethodRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new WriteOffMethodException("Write off method cannot be deleted because it is being used.");
        }
    }
}
