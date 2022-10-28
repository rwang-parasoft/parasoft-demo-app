package com.parasoft.demoapp.service;

import com.parasoft.demoapp.dto.InventoryInfoDTO;
import com.parasoft.demoapp.dto.InventoryOperationRequestMessageDTO;
import com.parasoft.demoapp.dto.InventoryOperationResultMessageDTO;
import com.parasoft.demoapp.model.industry.ItemInventoryEntity;
import com.parasoft.demoapp.repository.industry.ItemInventoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static com.parasoft.demoapp.dto.InventoryOperation.DECREASE;
import static com.parasoft.demoapp.dto.InventoryOperationStatus.FAIL;
import static com.parasoft.demoapp.dto.InventoryOperationStatus.SUCCESS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemInventoryServiceTest {

    @InjectMocks
    private ItemInventoryService itemInventoryService;

    @Mock
    private ItemInventoryRepository itemInventoryRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDecrease_normal() {
        // Given
        InventoryOperationRequestMessageDTO requestMessage = createRequestMessageForDecrease();

        when(itemInventoryRepository.findByItemId(1L)).thenReturn(new ItemInventoryEntity(1L, 1));
        when(itemInventoryRepository.findByItemId(2L)).thenReturn(new ItemInventoryEntity(2L, 3));

        // When
        InventoryOperationResultMessageDTO resultMessage = itemInventoryService.receiveFromRequestQueue(requestMessage);

        // Then
        InventoryOperationResultMessageDTO expectedResultMessage =
                new InventoryOperationResultMessageDTO(DECREASE, requestMessage.getOrderNumber(), SUCCESS, null);
        assertEquals(expectedResultMessage, resultMessage);
    }

    @Test
    public void testDecrease_itemNotExist() {
        // Given
        InventoryOperationRequestMessageDTO requestMessage = createRequestMessageForDecrease();

        when(itemInventoryRepository.findByItemId(1L)).thenReturn(new ItemInventoryEntity(1L, 1));
        when(itemInventoryRepository.findByItemId(2L)).thenReturn(null);

        // When
        InventoryOperationResultMessageDTO resultMessage = itemInventoryService.receiveFromRequestQueue(requestMessage);

        // Then
        InventoryOperationResultMessageDTO expectedResultMessage =
                new InventoryOperationResultMessageDTO(DECREASE, requestMessage.getOrderNumber(), FAIL,
                        "Inventory item with id 2 doesn't exist.");
        assertEquals(expectedResultMessage, resultMessage);
    }

    @Test
    public void testDecrease_itemOutOfStock() {
        // Given
        InventoryOperationRequestMessageDTO requestMessage = createRequestMessageForDecrease();

        when(itemInventoryRepository.findByItemId(1L)).thenReturn(new ItemInventoryEntity(1L, 1));
        when(itemInventoryRepository.findByItemId(2L)).thenReturn(new ItemInventoryEntity(2L, 1));

        // When
        InventoryOperationResultMessageDTO resultMessage = itemInventoryService.receiveFromRequestQueue(requestMessage);

        // Then
        InventoryOperationResultMessageDTO expectedResultMessage =
                new InventoryOperationResultMessageDTO(DECREASE, requestMessage.getOrderNumber(), FAIL,
                        "Inventory item with id 2 is out of stock.");
        assertEquals(expectedResultMessage, resultMessage);
    }

    @Test
    public void testDecrease_noRequestItem() {
        // Given
        InventoryOperationRequestMessageDTO requestMessage =
                new InventoryOperationRequestMessageDTO(DECREASE, "23-456-001",
                        new ArrayList<>(), null);

        // When
        InventoryOperationResultMessageDTO resultMessage = itemInventoryService.receiveFromRequestQueue(requestMessage);

        // Then
        assertNull(resultMessage);
    }

    private static InventoryOperationRequestMessageDTO createRequestMessageForDecrease() {
        return new InventoryOperationRequestMessageDTO(DECREASE, "23-456-001",
                Arrays.asList(
                        new InventoryInfoDTO(1L, 1),
                        new InventoryInfoDTO(2L, 2)), null);
    }

    @Test
    public void testSaveItemInStock() {
        // Given
        Long itemId = 1L;
        Integer inStock = 10;

        when(itemInventoryRepository.save(new ItemInventoryEntity(itemId, inStock))).thenReturn(new ItemInventoryEntity(itemId, inStock));

        // When
        ItemInventoryEntity res = itemInventoryService.saveItemInStock(itemId, inStock);

        // Then
        assertEquals(itemId, res.getItemId());
        assertEquals(inStock, res.getInStock());
    }

    @Test
    public void testGetInStockByItemId() {
        // Given
        Long itemId = 1L;
        Integer inStock = 10;

        when(itemInventoryRepository.findInStockByItemId(itemId)).thenReturn(inStock);

        // When
        Integer res = itemInventoryService.getInStockByItemId(itemId);

        // Then
        assertEquals(inStock, res);
    }

    @Test
    public void testGetInStockByItemId_ItemIdNotExist() {
        // Given
        Long itemId = 1L;

        when(itemInventoryRepository.findInStockByItemId(itemId)).thenReturn(null);

        // When
        Integer res = itemInventoryService.getInStockByItemId(itemId);

        // Then
        assertNull(res);
    }

    @Test
    public void testRemoveItemInventoryByItemId_normal() {
        // Given
        Long itemId = 1L;

        when(itemInventoryService.itemInventoryExistById(itemId)).thenReturn(true);

        // When
        itemInventoryService.removeItemInventoryByItemId(itemId);

        // Then
        verify(itemInventoryRepository, times(1)).deleteById(itemId);
    }

    @Test
    public void testRemoveItemInventoryByItemId_itemInventoryNotExist() {
        // Given
        Long itemId = 1L;

        when(itemInventoryService.itemInventoryExistById(itemId)).thenReturn(false);

        // When
        itemInventoryService.removeItemInventoryByItemId(itemId);

        // Then
        verify(itemInventoryRepository, times(0)).deleteById(itemId);
    }

    @Test
    public void testItemInventoryExistById_exist() {
        // Given
        Long itemId = 1L;

        when(itemInventoryRepository.existsById(itemId)).thenReturn(true);

        // When
        Boolean res = itemInventoryService.itemInventoryExistById(itemId);

        // Then
        assertTrue(res);
    }

    @Test
    public void testItemInventoryExistById_notExist() {
        // Given
        Long itemId = 1L;

        when(itemInventoryRepository.existsById(itemId)).thenReturn(false);

        // When
        Boolean res = itemInventoryService.itemInventoryExistById(itemId);

        // Then
        assertFalse(res);
    }
}