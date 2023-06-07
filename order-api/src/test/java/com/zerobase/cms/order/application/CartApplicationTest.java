package com.zerobase.cms.order.application;

import static com.zerobase.cms.order.exception.ErrorCode.ITEM_COUNT_NOT_ENOUGH;
import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_PRODUCT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CartApplicationTest {

    @Mock
    private ProductSearchService productSearchService;
    private CartService cartService;

    @Mock
    RedisClient redisClient;

    @InjectMocks
    private CartApplication cartApplication;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        cartService = new CartService(redisClient);
        cartApplication = new CartApplication(productSearchService,
            cartService);
    }

    @Test
    @DisplayName("addCart - Success")
    void testAddCart_Success() {
        // given
        Long customerId = 1L;
        Long productId = 100L;
        String productName = "Product 1";
        Integer cartCount = 5;

        AddProductCartForm form = AddProductCartForm.builder()
            .id(productId)
            .name(productName)
            .items(Collections.singletonList(
                AddProductCartForm.ProductItem.builder()
                    .id(1L)
                    .name("Item 1")
                    .price(10000)
                    .count(1)
                    .build()
            ))
            .build();

        Product sellerProduct = Product.builder()
            .id(productId)
            .name(productName)
            .build();
        ProductItem productItem = new ProductItem();
        productItem.setId(1L);
        productItem.setName("Item 1");
        productItem.setPrice(10000);
        productItem.setCount(10);
        sellerProduct.setProductItems(Collections.singletonList(productItem));

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(productId);
        cartProduct.setName(productName);
        cart.getProducts().add(cartProduct);

        Cart.ProductItem cartItem = new Cart.ProductItem();
        cartItem.setId(1L);
        cartItem.setName("Item 1");
        cartItem.setPrice(10000);
        cartItem.setCount(cartCount);
        cartProduct.getItems().add(cartItem);

        when(productSearchService.getByProductId(form.getId()))
            .thenReturn(sellerProduct);
        when(cartService.getCart(customerId))
            .thenReturn(cart);

        // when
        Cart result = cartApplication.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getProducts().size());
        assertEquals(productId, result.getProducts().get(0).getId());
        assertEquals(1, result.getProducts().get(0).getItems().size());
        assertEquals(form.getItems().get(0).getCount() + cartCount,
            result.getProducts().get(0).getItems().get(0).getCount());
        assertTrue(sellerProduct.getProductItems().get(0).getCount() >
            result.getProducts().get(0).getItems().get(0).getCount());
    }

    @Test
    @DisplayName("addCart - Product Not Found")
    void testAddCart_ProductNotFound() {
        // given
        Long customerId = 1L;
        Long productId = 1L;

        AddProductCartForm form = AddProductCartForm.builder()
            .id(productId)
            .build();

        when(productSearchService.getByProductId(productId))
            .thenReturn(null);
        // when/then
        CustomException exception = assertThrows(CustomException.class,
            () -> cartApplication.addCart(customerId, form));
        assertEquals(NOT_FOUND_PRODUCT, exception.getErrorCode());
    }

    @Test
    @DisplayName("addCart - Item Count Not Enough")
    void testAddCart_ItemCountNotEnough() {
        // given
        Long customerId = 1L;
        Long productId = 100L;
        String productName = "Product 1";
        Integer cartCount = 5;

        AddProductCartForm form = AddProductCartForm.builder()
            .id(productId)
            .name(productName)
            .items(Collections.singletonList(
                AddProductCartForm.ProductItem.builder()
                    .id(1L)
                    .name("Item 1")
                    .price(10000)
                    .count(6)
                    .build()
            ))
            .build();

        Product sellerProduct = Product.builder()
            .id(productId)
            .name(productName)
            .build();
        ProductItem productItem = new ProductItem();
        productItem.setId(1L);
        productItem.setName("Item 1");
        productItem.setPrice(10000);
        productItem.setCount(10);
        sellerProduct.setProductItems(Collections.singletonList(productItem));

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(productId);
        cartProduct.setName(productName);
        cart.getProducts().add(cartProduct);

        Cart.ProductItem cartItem = new Cart.ProductItem();
        cartItem.setId(1L);
        cartItem.setName("Item 1");
        cartItem.setPrice(10000);
        cartItem.setCount(cartCount);
        cartProduct.getItems().add(cartItem);

        when(productSearchService.getByProductId(productId)).thenReturn(
            sellerProduct);
        when(cartService.getCart(customerId)).thenReturn(cart);

        // when/then
        CustomException exception = assertThrows(CustomException.class,
            () -> cartApplication.addCart(customerId, form));
        assertEquals(ITEM_COUNT_NOT_ENOUGH, exception.getErrorCode());
        assertFalse(sellerProduct.getProductItems().get(0).getCount() >
            form.getItems().get(0).getCount() + cartCount);
    }
}