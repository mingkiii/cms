package com.zerobase.cms.order.service;

import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_PRODUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductSearchService productSearchService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        productSearchService = new ProductSearchService(productRepository);
    }

    @Test
    @DisplayName("상품 검색-성공")
    public void success_searchByName() {
        // Given
        String name = "나이키";
        Product p1 = Product.builder()
            .name("나이키 에어조던")
            .build();
        Product p2 = Product.builder()
            .name("나이키 에어맥스")
            .build();
        List<Product> products = new ArrayList<>();
        products.add(p1);
        products.add(p2);
        when(productRepository.searchByName(name))
            .thenReturn(products);

        // When
        List<Product> result = productSearchService.searchByName(name);

        // Then
        assertEquals(products, result);
        verify(productRepository, times(1)).searchByName(name);
        for (Product product : result) {
            assertTrue(product.getName().contains(name));
        }
    }

    @Test
    @DisplayName("상품 상세 정보 검색-성공")
    public void success_getByProductId() {
        // Given
        Long productId = 1L;
        Product product = Product.builder()
            .id(1L)
            .name("나이키 운동화")
            .description("축구화")
            .build();
        when(productRepository.findWithProductItemsById(productId))
            .thenReturn(Optional.of(product));

        // When
        Product result = productSearchService.getByProductId(productId);

        // Then
        assertEquals(product, result);
        verify(productRepository, times(1)).findWithProductItemsById(productId);
    }

    @Test
    @DisplayName("상품 상세 검색-실패")
    public void fail_getByProductId() {
        // Given
        Long searchProductId = 3L;
        when(productRepository.findWithProductItemsById(searchProductId))
            .thenReturn(Optional.empty());

        // When/Then
        CustomException exception = assertThrows(
            CustomException.class, () ->
                productSearchService.getByProductId(searchProductId));

        assertEquals(NOT_FOUND_PRODUCT, exception.getErrorCode());
        verify(productRepository, times(1)).findWithProductItemsById(searchProductId);
    }
}