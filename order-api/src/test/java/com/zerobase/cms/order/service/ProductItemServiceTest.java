package com.zerobase.cms.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import com.zerobase.cms.order.domain.repository.ProductItemRepository;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.h2.command.dml.MergeUsing.When;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductItemServiceTest {

    private ProductItemService productItemService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        productItemService = new ProductItemService(productRepository,
            productItemRepository);
    }

    @Test
    @DisplayName("아이템 추가 등록-성공")
    void success_addProductItem() {
        // Given
        Long sellerId = 1L;
        Long productId = 1L;
        String itemName = "Item 1";
        AddProductItemForm form = AddProductItemForm.builder()
            .productId(productId)
            .name(itemName)
            .price(100)
            .count(2)
            .build();

        Product product = new Product();
        product.setProductItems(new ArrayList<>());

        when(productRepository.findBySellerIdAndId(sellerId, productId))
            .thenReturn(Optional.of(product));

        // When
        Product result = productItemService.addProductItem(sellerId, form);

        // Then
        assertEquals(1, result.getProductItems().size());
        ProductItem addedItem = result.getProductItems().get(0);
        assertEquals(itemName, addedItem.getName());
        assertEquals(100, addedItem.getPrice());
        assertEquals(2, addedItem.getCount());
    }

    @Test
    @DisplayName("아이템 추가 등록-실패_아이템명 중복")
    void fail_addProductItem_DuplicateItemName() {
        // Given
        Long sellerId = 1L;
        Long productId = 1L;
        String itemName = "Item 1";
        AddProductItemForm form = AddProductItemForm.builder()
            .productId(productId)
            .name(itemName)
            .price(100)
            .count(2)
            .build();

        Product product = new Product();
        ProductItem existingItem = new ProductItem();
        existingItem.setName(itemName);
        product.setProductItems(List.of(existingItem));

        // Mock the repository method
        when(productRepository.findBySellerIdAndId(sellerId, productId))
            .thenReturn(Optional.of(product));

        // When/Then
        assertThrows(CustomException.class,
            () -> productItemService.addProductItem(sellerId, form));
    }

    @Test
    @DisplayName("아이템 추가 등록-실패_해당 상품 없음")
    void addProductItem_ProductNotFound() {
        // given
        Long sellerId = 1L;
        Long productId = 1L;

        AddProductItemForm form = AddProductItemForm.builder()
            .productId(1L)
            .name("상품 1")
            .price(100)
            .count(2)
            .build();

        when(productRepository.findBySellerIdAndId(sellerId, productId))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(CustomException.class, () -> productItemService.addProductItem(sellerId, form));
    }

    @Test
    @DisplayName("아이템 수정-성공")
    void success_updateProductItem() {
        // Given
        Long sellerId = 1L;

        UpdateProductItemForm form = UpdateProductItemForm.builder()
            .id(1L)
            .name("Updated Item")
            .price(200)
            .count(3)
            .build();

        ProductItem existingItem = ProductItem.builder()
            .id(1L)
            .sellerId(sellerId)
            .name("item")
            .price(100)
            .count(2)
            .build();

        when(productItemRepository.findById(form.getId()))
            .thenReturn(Optional.of(existingItem));

        // When
        ProductItem result = productItemService.updateProductItem(sellerId,
            form);

        // Then
        assertEquals(form.getId(), result.getId());
        assertEquals(form.getName(), result.getName());
        assertEquals(form.getPrice(), result.getPrice());
        assertEquals(form.getCount(), result.getCount());
    }

    @Test
    @DisplayName("아이템 수정-실패_해당 아이템 없음")
    void updateProductItem_NonExistingItem() {
        // Given
        Long sellerId = 1L;

        UpdateProductItemForm form = UpdateProductItemForm.builder()
            .id(1L)
            .name("Updated Item")
            .price(200)
            .count(3)
            .build();

        // Mock the repository method
        when(productItemRepository.findById(form.getId()))
            .thenReturn(Optional.empty());

        // When/Then
        assertThrows(CustomException.class, () -> productItemService.updateProductItem(sellerId, form));
    }

    @Test
    @DisplayName("아이템 삭제-성공")
    void success_deleteProductItem() {
        // Given
        Long sellerId = 1L;
        Long itemId = 1L;

        ProductItem existingProductItem = ProductItem.builder()
            .id(itemId)
            .sellerId(sellerId)
            .name("Existing Item")
            .price(100)
            .count(2)
            .build();

        when(productItemRepository.findById(itemId)).thenReturn(
            Optional.of(existingProductItem));

        //When
        productItemService.deleteProductItem(sellerId, itemId);

        // Then
        verify(productItemRepository, times(1)).findById(itemId);
        verify(productItemRepository, times(1)).delete(existingProductItem);
        when(productItemRepository.findById(itemId)).thenReturn(Optional.empty());
        Optional<ProductItem> deletedItem = productItemRepository.findById(itemId);
        assertFalse(deletedItem.isPresent());
    }
}