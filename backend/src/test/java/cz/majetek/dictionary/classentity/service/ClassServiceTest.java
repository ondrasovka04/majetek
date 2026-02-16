package cz.majetek.dictionary.classentity.service;

import cz.majetek.dictionary.classentity.dto.UpdateClassRequest;
import cz.majetek.dictionary.classentity.repository.IClassRepository;
import cz.majetek.dictionary.classentity.service.implementation.ClassService;
import cz.majetek.entity.ClassEntity;
import cz.majetek.exception.dictionary.ClassEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock
    private IClassRepository classRepository;

    @InjectMocks
    private ClassService classService;

    private ClassEntity testClass;

    @BeforeEach
    void setUp() {
        testClass = new ClassEntity("CLS1", "Test Class");
    }

    @Test
    void getAll_ShouldReturnAllClasses() {
        List<ClassEntity> classes = List.of(testClass);
        when(classRepository.findAll()).thenReturn(classes);

        List<ClassEntity> result = classService.getAll();

        assertEquals(1, result.size());
        assertEquals(testClass, result.get(0));
        verify(classRepository).findAll();
    }

    @Test
    void getById_WithExistingId_ShouldReturnClass() {
        when(classRepository.findById("CLS1")).thenReturn(Optional.of(testClass));

        ClassEntity result = classService.getById("CLS1");

        assertEquals(testClass, result);
        verify(classRepository).findById("CLS1");
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowException() {
        when(classRepository.findById("CLS999")).thenReturn(Optional.empty());

        assertThrows(ClassEntityException.class, () -> classService.getById("CLS999"));
        verify(classRepository).findById("CLS999");
    }

    @Test
    void create_WithNewClass_ShouldCreateSuccessfully() {
        ClassEntity newClass = new ClassEntity("CLS2", "New Class");
        when(classRepository.existsById("CLS2")).thenReturn(false);
        when(classRepository.save(newClass)).thenReturn(newClass);

        ClassEntity result = classService.create(newClass);

        assertEquals(newClass, result);
        verify(classRepository).existsById("CLS2");
        verify(classRepository).save(newClass);
    }

    @Test
    void create_WithExistingId_ShouldThrowException() {
        ClassEntity existing = new ClassEntity("CLS1", "Existing");
        when(classRepository.existsById("CLS1")).thenReturn(true);

        assertThrows(ClassEntityException.class, () -> classService.create(existing));
        verify(classRepository).existsById("CLS1");
        verify(classRepository, never()).save(any(ClassEntity.class));
    }

    @Test
    void update_WithExistingClass_ShouldUpdateSuccessfully() {
        UpdateClassRequest request = new UpdateClassRequest();
        request.setName("Updated Name");
        ClassEntity updatedClass = new ClassEntity("CLS1", "Updated Name");

        when(classRepository.findById("CLS1")).thenReturn(Optional.of(testClass));
        when(classRepository.save(any(ClassEntity.class))).thenReturn(updatedClass);

        ClassEntity result = classService.update("CLS1", request);

        assertEquals("Updated Name", result.getName());
        verify(classRepository).findById("CLS1");
        verify(classRepository).save(any(ClassEntity.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        UpdateClassRequest request = new UpdateClassRequest();
        request.setName("Updated Name");
        when(classRepository.findById("CLS999")).thenReturn(Optional.empty());

        assertThrows(ClassEntityException.class, () -> classService.update("CLS999", request));
        verify(classRepository).findById("CLS999");
        verify(classRepository, never()).save(any(ClassEntity.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSuccessfully() {
        doNothing().when(classRepository).deleteById("CLS1");

        assertDoesNotThrow(() -> classService.delete("CLS1"));
        verify(classRepository).deleteById("CLS1");
    }

    @Test
    void delete_WithClassInUse_ShouldThrowException() {
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(classRepository).deleteById("CLS1");

        ClassEntityException exception = assertThrows(ClassEntityException.class,
                () -> classService.delete("CLS1"));

        assertTrue(exception.getMessage().contains("being used"));
        verify(classRepository).deleteById("CLS1");
    }
}
