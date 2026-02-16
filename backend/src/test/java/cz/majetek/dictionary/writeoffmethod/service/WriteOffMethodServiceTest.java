package cz.majetek.dictionary.writeoffmethod.service;

import cz.majetek.dictionary.writeoffmethod.dto.UpdateWriteOffMethodRequest;
import cz.majetek.dictionary.writeoffmethod.repository.IWriteOffMethodRepository;
import cz.majetek.dictionary.writeoffmethod.service.implementation.WriteOffMethodService;
import cz.majetek.entity.WriteOffMethod;
import cz.majetek.exception.dictionary.WriteOffMethodException;
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
class WriteOffMethodServiceTest {

    @Mock
    private IWriteOffMethodRepository writeOffMethodRepository;

    @InjectMocks
    private WriteOffMethodService writeOffMethodService;

    private WriteOffMethod testMethod;

    @BeforeEach
    void setUp() {
        testMethod = new WriteOffMethod("WO1", "Test Write Off Method");
    }

    @Test
    void getAll_ShouldReturnAllMethods() {
        List<WriteOffMethod> methods = List.of(testMethod);
        when(writeOffMethodRepository.findAll()).thenReturn(methods);

        List<WriteOffMethod> result = writeOffMethodService.getAll();

        assertEquals(1, result.size());
        assertEquals(testMethod, result.getFirst());
        verify(writeOffMethodRepository).findAll();
    }

    @Test
    void getById_WithExistingId_ShouldReturnMethod() {
        when(writeOffMethodRepository.findById("WO1")).thenReturn(Optional.of(testMethod));

        WriteOffMethod result = writeOffMethodService.getById("WO1");

        assertEquals(testMethod, result);
        verify(writeOffMethodRepository).findById("WO1");
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowException() {
        when(writeOffMethodRepository.findById("WO999")).thenReturn(Optional.empty());

        assertThrows(WriteOffMethodException.class, () -> writeOffMethodService.getById("WO999"));
        verify(writeOffMethodRepository).findById("WO999");
    }

    @Test
    void create_WithNewMethod_ShouldCreateSuccessfully() {
        WriteOffMethod newMethod = new WriteOffMethod("WO2", "New Method");
        when(writeOffMethodRepository.existsById("WO2")).thenReturn(false);
        when(writeOffMethodRepository.save(newMethod)).thenReturn(newMethod);

        WriteOffMethod result = writeOffMethodService.create(newMethod);

        assertEquals(newMethod, result);
        verify(writeOffMethodRepository).existsById("WO2");
        verify(writeOffMethodRepository).save(newMethod);
    }

    @Test
    void create_WithExistingId_ShouldThrowException() {
        WriteOffMethod existing = new WriteOffMethod("WO1", "Existing");
        when(writeOffMethodRepository.existsById("WO1")).thenReturn(true);

        assertThrows(WriteOffMethodException.class, () -> writeOffMethodService.create(existing));
        verify(writeOffMethodRepository).existsById("WO1");
        verify(writeOffMethodRepository, never()).save(any(WriteOffMethod.class));
    }

    @Test
    void update_WithExistingMethod_ShouldUpdateSuccessfully() {
        UpdateWriteOffMethodRequest request = new UpdateWriteOffMethodRequest();
        request.setName("Updated Name");
        WriteOffMethod updated = new WriteOffMethod("WO1", "Updated Name");

        when(writeOffMethodRepository.findById("WO1")).thenReturn(Optional.of(testMethod));
        when(writeOffMethodRepository.save(any(WriteOffMethod.class))).thenReturn(updated);

        WriteOffMethod result = writeOffMethodService.update("WO1", request);

        assertEquals("Updated Name", result.getName());
        verify(writeOffMethodRepository).findById("WO1");
        verify(writeOffMethodRepository).save(any(WriteOffMethod.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        UpdateWriteOffMethodRequest request = new UpdateWriteOffMethodRequest();
        request.setName("Updated Name");
        when(writeOffMethodRepository.findById("WO999")).thenReturn(Optional.empty());

        assertThrows(WriteOffMethodException.class, () -> writeOffMethodService.update("WO999", request));
        verify(writeOffMethodRepository).findById("WO999");
        verify(writeOffMethodRepository, never()).save(any(WriteOffMethod.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSuccessfully() {
        doNothing().when(writeOffMethodRepository).deleteById("WO1");

        assertDoesNotThrow(() -> writeOffMethodService.delete("WO1"));
        verify(writeOffMethodRepository).deleteById("WO1");
    }

    @Test
    void delete_WithMethodInUse_ShouldThrowException() {
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(writeOffMethodRepository).deleteById("WO1");

        WriteOffMethodException exception = assertThrows(WriteOffMethodException.class,
                () -> writeOffMethodService.delete("WO1"));

        assertTrue(exception.getMessage().contains("being used"));
        verify(writeOffMethodRepository).deleteById("WO1");
    }
}
