package cz.majetek.dictionary.assetgroup.service;

import cz.majetek.dictionary.assetgroup.dto.UpdateAssetGroupRequest;
import cz.majetek.dictionary.assetgroup.repository.IAssetGroupRepository;
import cz.majetek.dictionary.assetgroup.service.implementation.AssetGroupService;
import cz.majetek.entity.AssetGroup;
import cz.majetek.exception.dictionary.AssetGroupException;
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
class AssetGroupServiceTest {

    @Mock
    private IAssetGroupRepository assetGroupRepository;

    @InjectMocks
    private AssetGroupService assetGroupService;

    private AssetGroup testAssetGroup;

    @BeforeEach
    void setUp() {
        testAssetGroup = new AssetGroup();
        testAssetGroup.setId("1");
        testAssetGroup.setName("Test Asset Group");
    }

    @Test
    void getAll_ShouldReturnAllAssetGroups() {
        List<AssetGroup> assetGroups = List.of(testAssetGroup);
        when(assetGroupRepository.findAll()).thenReturn(assetGroups);

        List<AssetGroup> result = assetGroupService.getAll();

        assertEquals(1, result.size());
        assertEquals(testAssetGroup, result.getFirst());
        verify(assetGroupRepository).findAll();
    }

    @Test
    void getById_WithExistingId_ShouldReturnAssetGroup() {
        when(assetGroupRepository.findById("1")).thenReturn(Optional.of(testAssetGroup));

        AssetGroup result = assetGroupService.getById("1");

        assertEquals(testAssetGroup, result);
        verify(assetGroupRepository).findById("1");
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowException() {
        when(assetGroupRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(AssetGroupException.class, () -> assetGroupService.getById("999"));
        verify(assetGroupRepository).findById("999");
    }

    @Test
    void create_WithNewAssetGroup_ShouldCreateSuccessfully() {
        AssetGroup newAssetGroup = new AssetGroup();
        newAssetGroup.setId("2");
        newAssetGroup.setName("New Asset Group");

        when(assetGroupRepository.existsById("2")).thenReturn(false);
        when(assetGroupRepository.save(newAssetGroup)).thenReturn(newAssetGroup);

        AssetGroup result = assetGroupService.create(newAssetGroup);

        assertEquals(newAssetGroup, result);
        verify(assetGroupRepository).existsById("2");
        verify(assetGroupRepository).save(newAssetGroup);
    }

    @Test
    void create_WithExistingId_ShouldThrowException() {
        AssetGroup existingAssetGroup = new AssetGroup();
        existingAssetGroup.setId("1");
        existingAssetGroup.setName("Existing");

        when(assetGroupRepository.existsById("1")).thenReturn(true);

        assertThrows(AssetGroupException.class, () -> assetGroupService.create(existingAssetGroup));
        verify(assetGroupRepository).existsById("1");
        verify(assetGroupRepository, never()).save(any(AssetGroup.class));
    }

    @Test
    void update_WithExistingAssetGroup_ShouldUpdateSuccessfully() {
        UpdateAssetGroupRequest request = new UpdateAssetGroupRequest();
        request.setName("Updated Name");
        AssetGroup updatedGroup = new AssetGroup();
        updatedGroup.setId("1");
        updatedGroup.setName("Updated Name");

        when(assetGroupRepository.findById("1")).thenReturn(Optional.of(testAssetGroup));
        when(assetGroupRepository.save(any(AssetGroup.class))).thenReturn(updatedGroup);

        AssetGroup result = assetGroupService.update("1", request);

        assertEquals("Updated Name", result.getName());
        verify(assetGroupRepository).findById("1");
        verify(assetGroupRepository).save(any(AssetGroup.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        UpdateAssetGroupRequest request = new UpdateAssetGroupRequest();
        request.setName("Updated Name");
        when(assetGroupRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(AssetGroupException.class, () -> assetGroupService.update("999", request));
        verify(assetGroupRepository).findById("999");
        verify(assetGroupRepository, never()).save(any(AssetGroup.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSuccessfully() {
        doNothing().when(assetGroupRepository).deleteById("1");

        assertDoesNotThrow(() -> assetGroupService.delete("1"));
        verify(assetGroupRepository).deleteById("1");
    }

    @Test
    void delete_WithAssetGroupInUse_ShouldThrowException() {
        doThrow(new DataIntegrityViolationException("FK constraint"))
                .when(assetGroupRepository).deleteById("1");

        AssetGroupException exception = assertThrows(AssetGroupException.class,
                () -> assetGroupService.delete("1"));

        assertTrue(exception.getMessage().contains("being used"));
        verify(assetGroupRepository).deleteById("1");
    }
}


