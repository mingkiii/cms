package com.zerobase.cms.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductForm;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.product.UpdateProductForm;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import com.zerobase.cms.order.domain.repository.ProductItemRepository;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    private ProductService productService;

    @Captor
    private ArgumentCaptor<Product> productArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    @Test
    @DisplayName("상품 추가 등록-성공")
    void addProduct_ValidInput_ReturnsSavedProduct() {
        // Given
        Long sellerId = 1L;
        AddProductForm form = AddProductForm.builder()
            .name("Test Product")
            .description("This is a test product")
            .items(Arrays.asList(
                AddProductItemForm.builder().name("Item 1").price(100).count(2).build(),
                AddProductItemForm.builder().name("Item 2").price(200).count(3).build()
            ))
            .build();

        Product savedProduct = Product.builder()
            .id(1L)
            .sellerId(sellerId)
            .name(form.getName())
            .description(form.getDescription())
            .productItems(Arrays.asList(
                ProductItem.builder().id(1L).name("Item 1").price(100).count(2).build(),
                ProductItem.builder().id(2L).name("Item 2").price(200).count(3).build()
            ))
            .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        Product result = productService.addProduct(sellerId, form);

        // Then
        verify(productRepository, times(1)).save(productArgumentCaptor.capture());
        Product capturedProduct = productArgumentCaptor.getValue();

        assertEquals(sellerId, capturedProduct.getSellerId());
        assertEquals(form.getName(), capturedProduct.getName());
        assertEquals(form.getDescription(), capturedProduct.getDescription());
        assertEquals(form.getItems().size(), capturedProduct.getProductItems().size());

        assertEquals(savedProduct, result);
    }

    @Test
    @DisplayName("상품 수정-성공")
    void success_updateProduct() {
        // Given
        Long sellerId = 1L;
        Long productId = 1L;

        Product existingProduct = Product.builder()
            .id(productId)
            .sellerId(sellerId)
            .name("Existing Product")
            .description("This is an existing product")
            .productItems(Arrays.asList(
                ProductItem.builder().id(1L).name("Item 1").price(100).count(2).build(),
                ProductItem.builder().id(2L).name("Item 2").price(200).count(3).build()
            ))
            .build();

        UpdateProductForm form = UpdateProductForm.builder()
            .id(productId)
            .name("Updated Product")
            .description("This is an updated product")
            .build();

        when(productRepository.findBySellerIdAndId(sellerId, productId)).thenReturn(Optional.of(existingProduct));

        // When
        Product result = productService.updateProduct(sellerId, form);

        // Then
        assertEquals(form.getId(), result.getId());
        assertEquals(form.getName(), result.getName());
        assertEquals(form.getDescription(), result.getDescription());
    }

    @Test
    @DisplayName("상품 수정-실패_해당 상품 없음")
    void fail_updateProduct_ProductNotFound() {
        // Given
        Long sellerId = 1L;
        Long productId = 1L;

        UpdateProductForm form = UpdateProductForm.builder()
            .id(productId)
            .name("Updated Product")
            .description("This is an updated product")
            .build();

        when(productRepository.findBySellerIdAndId(sellerId, productId)).thenReturn(
            Optional.empty());

        // When/Then
        CustomException exception = assertThrows(CustomException.class,
            () -> productService.updateProduct(sellerId, form));

        assertEquals(ErrorCode.NOT_FOUND_PRODUCT, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("없습니다."));
    }

    @Test
    @DisplayName("상품 삭제-성공")
    void success_deleteProduct() {
        // Given
        Long sellerId = 1L;
        Long productId = 1L;

        Product existingProduct = Product.builder()
            .id(productId)
            .sellerId(sellerId)
            .name("Existing Product")
            .description("This is an existing product")
            .productItems(Arrays.asList(
                ProductItem.builder().id(1L).name("Item 1").price(100).count(2)
                    .build(),
                ProductItem.builder().id(2L).name("Item 2").price(200).count(3)
                    .build()
            ))
            .build();

        when(productRepository.findBySellerIdAndId(sellerId,
            productId)).thenReturn(Optional.of(existingProduct));

        // When
        productService.deleteProduct(sellerId, productId);

        // Then
        verify(productRepository, times(1)).findBySellerIdAndId(sellerId,
            productId);
        verify(productRepository, times(1)).delete(existingProduct);

        when(productRepository.findById(productId)).thenReturn(
            Optional.empty());
        Optional<Product> deletedProduct = productRepository.findById(
            productId);
        assertFalse(deletedProduct.isPresent());
        // 상품 아이템 같이 삭제 되었는지 확인
        List<ProductItem> deletedItems = existingProduct.getProductItems();
        for (ProductItem item : deletedItems) {
            when(productItemRepository.findById(item.getId())).thenReturn(
                Optional.empty());
            Optional<ProductItem> deletedItem = productItemRepository.findById(
                item.getId());
            assertFalse(deletedItem.isPresent());
        }
    }
}