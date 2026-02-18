package cz.majetek.dictionary.purchasemethod.service;

import cz.majetek.dictionary.purchasemethod.dto.UpdatePurchaseMethodRequest;
import cz.majetek.dictionary.purchasemethod.repository.IPurchaseMethodRepository;
import cz.majetek.dictionary.purchasemethod.service.implementation.PurchaseMethodService;
import cz.majetek.entity.PurchaseMethod;
import cz.majetek.exception.dictionary.PurchaseMethodException;
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
class PurchaseMethodServiceTest {

    @Mock
    private IPurchaseMethodRepository purchaseMethodRepository;

    @InjectMocks
    private PurchaseMethodService purchaseMethodService;

    private PurchaseMethod testMethod;

    @BeforeEach
    void setUp() {
        testMethod = new PurchaseMethod("PM1", "Test Purchase Method");
    }

    @Test
    void getAll_ShouldReturnAllMethods() {
        List<PurchaseMethod> methods = List.of(testMethod);
        when(purchaseMethodRepository.findAll()).thenReturn(methods);

        List<PurchaseMethod> result = purchaseMethodService.getAll();

        assertEquals(1, result.size());
        assertEquals(testMethod, result.getFirst());
        verify(purchaseMethodRepository).findAll();
    }

    @Test
    void getById_WithExistingId_ShouldReturnMethod() {
        when(purchaseMethodRepository.findById("PM1")).thenReturn(Optional.of(testMethod));

        PurchaseMethod result = purchaseMethodService.getById("PM1");

        assertEquals(testMethod, result);
        verify(purchaseMethodRepository).findById("PM1");
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowException() {
        when(purchaseMethodRepository.findById("PM999")).thenReturn(Optional.empty());

        assertThrows(PurchaseMethodException.class, () -> purchaseMethodService.getById("PM999"));
        verify(purchaseMethodRepository).findById("PM999");
    }

    @Test
    void create_WithNewMethod_ShouldCreateSuccessfully() {
        PurchaseMethod newMethod = new PurchaseMethod("PM2", "New Method");
        when(purchaseMethodRepository.existsById("PM2")).thenReturn(false);
        when(purchaseMethodRepository.save(newMethod)).thenReturn(newMethod);

        PurchaseMethod result = purchaseMethodService.create(newMethod);

        assertEquals(newMethod, result);
        verify(purchaseMethodRepository).existsById("PM2");
        verify(purchaseMethodRepository).save(newMethod);
    }

    @Test
    void create_WithExistingId_ShouldThrowException() {
        PurchaseMethod existing = new PurchaseMethod("PM1", "Existing");
        when(purchaseMethodRepository.existsById("PM1")).thenReturn(true);

        assertThrows(PurchaseMethodException.class, () -> purchaseMethodService.create(existing));
        verify(purchaseMethodRepository).existsById("PM1");
        verify(purchaseMethodRepository, never()).save(any(PurchaseMethod.class));
    }

    @Test
    void update_WithExistingMethod_ShouldUpdateSuccessfully() {
        UpdatePurchaseMethodRequest request = new UpdatePurchaseMethodRequest();
        request.setName("Updated Name");
        PurchaseMethod updated = new PurchaseMethod("PM1", "Updated Name");

        when(purchaseMethodRepository.findById("PM1")).thenReturn(Optional.of(testMethod));
        when(purchaseMethodRepository.save(any(PurchaseMethod.class))).thenReturn(updated);

        PurchaseMethod result = purchaseMethodService.update("PM1", request);

        assertEquals("Updated Name", result.getName());
        verify(purchaseMethodRepository).findById("PM1");
        verify(purchaseMethodRepository).save(any(PurchaseMethod.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        UpdatePurchaseMethodRequest request = new UpdatePurchaseMethodRequest();
        request.setName("Updated Name");
        when(purchaseMethodRepository.findById("PM999")).thenReturn(Optional.empty());

        assertThrows(PurchaseMethodException.class, () -> purchaseMethodService.update("PM999", request));
        verify(purchaseMethodRepository).findById("PM999");
        verify(purchaseMethodRepository, never()).save(any(PurchaseMethod.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSuccessfully() {
        doNothing().when(purchaseMethodRepository).deleteById("PM1");

        assertDoesNotThrow(() -> purchaseMethodService.delete("PM1"));
        verify(purchaseMethodRepository).deleteById("PM1");
    }

    @Test
    void delete_WithMethodInUse_ShouldThrowException() {
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(purchaseMethodRepository).deleteById("PM1");

        PurchaseMethodException exception = assertThrows(PurchaseMethodException.class,
                () -> purchaseMethodService.delete("PM1"));

        assertTrue(exception.getMessage().contains("being used"));
        verify(purchaseMethodRepository).deleteById("PM1");
    }
}
