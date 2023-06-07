package com.zerobase.cms.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CartServiceTest {
    @Mock
    private RedisClient redisClient;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        cartService = new CartService(redisClient);
    }

    @Test
    @DisplayName("getCard-성공")
    public void testGetCart_Success() {
        // given
        Long customerId = 1L;
        Cart cart = new Cart();

        when(redisClient.get(customerId, Cart.class)).thenReturn(cart);
        // when
        Cart result = cartService.getCart(customerId);
        // then
        assertEquals(cart, result);
    }

    @Test
    @DisplayName("addCart-getCart가 null인 경우")
    public void testAddCart_NewCart() {
        // given
        Long customerId = 1L;
        AddProductCartForm form = AddProductCartForm.builder()
            .id(1L)
            .sellerId(1L)
            .name("Product 1")
            .description("상품 설명")
            .items(new ArrayList<>())
            .build();

        when(redisClient.get(customerId, Cart.class)).thenReturn(null);
        // when
        Cart result = cartService.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getProducts().size());
        assertEquals("Product 1", result.getProducts().get(0).getName());
    }

    @Test
    @DisplayName("addCart - Existing Product, Different Name")
    public void testAddCart_ExistingProduct_DifferentName() {
        // given
        Long customerId = 1L;
        String cartProductName = "Product 1";
        String newProductName = "Product 2";

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(100L);
        cartProduct.setName(cartProductName);
        cart.getProducts().add(cartProduct);

        AddProductCartForm form = AddProductCartForm.builder()
            .id(100L)
            .name(newProductName)
            .items(new ArrayList<>())
            .build();

        when(redisClient.get(customerId, Cart.class)).thenReturn(cart);

        // when
        Cart result = cartService.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getProducts().size());
        assertNotEquals(newProductName, result.getProducts().get(0).getName());
        assertTrue(result.getMessages().contains(cartProductName + "의 정보가 변동되었습니다. 확인 부탁드립니다."));
    }

    @Test
    @DisplayName("addCart - Existing Product, Different ItemPrice")
    public void testAddCart_ExistingProduct_DifferentPrice() {
        // given
        Long customerId = 1L;
        String productName = "Product 1";
        Long productId = 100L;
        Long itemId = 200L;
        String itemName = "Item 1";
        Integer itemPrice = 10000;
        Integer newPrice = 20000;
        int itemCount = 5;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(productId);
        cartProduct.setName(productName);
        cart.getProducts().add(cartProduct);

        Cart.ProductItem cartItem = new Cart.ProductItem();
        cartItem.setId(itemId);
        cartItem.setName(itemName);
        cartItem.setPrice(itemPrice);
        cartItem.setCount(itemCount);
        cartProduct.getItems().add(cartItem);

        AddProductCartForm form = AddProductCartForm.builder()
            .id(productId)
            .name(productName)
            .items(Collections.singletonList(
                AddProductCartForm.ProductItem.builder()
                    .id(itemId)
                    .name(itemName)
                    .price(newPrice)
                    .count(itemCount)
                    .build()))
            .build();

        when(redisClient.get(customerId, Cart.class)).thenReturn(cart);

        // when
        Cart result = cartService.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getProducts().size());
        assertEquals(productName, result.getProducts().get(0).getName());
        assertNotEquals(form.getItems().get(0).getPrice(), result.getProducts().get(0).getItems().get(0).getPrice());
        assertTrue(result.getMessages().contains(cartProduct.getName() + cartItem.getName() + "의 가격이 변동되었습니다."));
    }

    @Test
    @DisplayName("addCart - Existing Product, New Item")
    public void testAddCart_NewItemForExistingProduct() {
        // given
        Long customerId = 1L;
        String productName = "Product 1";
        Long productId = 100L;
        Long itemId = 2L;
        String itemName = "Item 1";
        Integer itemPrice = 10000;

        Long newItemId = 5L;
        String newItemName = "Item 3";
        Integer newItemPrice = 50000;

        int itemCount = 5;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(productId);
        cartProduct.setName(productName);
        cart.getProducts().add(cartProduct);

        Cart.ProductItem cartItem = new Cart.ProductItem();
        cartItem.setId(itemId);
        cartItem.setName(itemName);
        cartItem.setPrice(itemPrice);
        cartItem.setCount(itemCount);
        cartProduct.getItems().add(cartItem);

        AddProductCartForm form = AddProductCartForm.builder()
            .id(productId)
            .name(productName)
            .items(Collections.singletonList(
                AddProductCartForm.ProductItem.builder()
                    .id(newItemId)
                    .name(newItemName)
                    .price(newItemPrice)
                    .count(itemCount)
                    .build()))
            .build();

        when(redisClient.get(customerId, Cart.class)).thenReturn(cart);

        // when
        Cart result = cartService.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(cart.getCustomerId(), result.getCustomerId());
        assertEquals(1, result.getProducts().size());
        assertEquals(cart.getProducts().get(0).getName(), result.getProducts().get(0).getName());
        assertEquals(2, result.getProducts().get(0).getItems().size());
        assertTrue(result.getProducts().get(0).getItems().stream().anyMatch(item -> item.getId().equals(newItemId)));
    }

    @Test
    @DisplayName("addCart - Existing Product, Same Item")
    public void testAddCart_ExistingProduct_SameItem() {
        // given
        Long customerId = 1L;
        String productName = "Product 1";
        Long productId = 100L;
        Long itemId = 2L;
        String itemName = "Item 1";
        Integer itemPrice = 10000;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(productId);
        cartProduct.setName(productName);
        cart.getProducts().add(cartProduct);

        Cart.ProductItem cartItem = new Cart.ProductItem();
        cartItem.setId(itemId);
        cartItem.setName(itemName);
        cartItem.setPrice(itemPrice);
        cartItem.setCount(3);
        cartProduct.getItems().add(cartItem);

        AddProductCartForm form = AddProductCartForm.builder()
            .id(productId)
            .name(productName)
            .items(Collections.singletonList(
                AddProductCartForm.ProductItem.builder()
                    .id(itemId)
                    .name(itemName)
                    .price(itemPrice)
                    .count(2)
                    .build()))
            .build();

        when(redisClient.get(customerId, Cart.class)).thenReturn(cart);

        // when
        Cart result = cartService.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(cart.getCustomerId(), result.getCustomerId());
        assertEquals(1, result.getProducts().size());
        assertEquals(cart.getProducts().get(0).getName(), result.getProducts().get(0).getName());
        assertEquals(5, result.getProducts().get(0).getItems().get(0).getCount());
    }

    @Test
    @DisplayName("addCart - Existing Product, Same Item")
    public void testAddCart_Different_Product() {
        // given
        Long customerId = 1L;
        String productName = "Product 1";
        Long productId = 1L;
        Long itemId = 1L;
        String itemName = "Item 1";
        Integer itemPrice = 10000;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(productId);
        cartProduct.setName(productName);
        cart.getProducts().add(cartProduct);

        Cart.ProductItem cartItem = new Cart.ProductItem();
        cartItem.setId(itemId);
        cartItem.setName(itemName);
        cartItem.setPrice(itemPrice);
        cartItem.setCount(3);
        cartProduct.getItems().add(cartItem);

        AddProductCartForm form = AddProductCartForm.builder()
            .id(5L)
            .name("Product 5")
            .items(Collections.singletonList(
                AddProductCartForm.ProductItem.builder()
                    .id(5L)
                    .name("item 5")
                    .price(50000)
                    .count(5)
                    .build()))
            .build();

        when(redisClient.get(customerId, Cart.class)).thenReturn(cart);

        // when
        Cart result = cartService.addCart(customerId, form);

        // then
        assertNotNull(result);
        assertEquals(cart.getCustomerId(), result.getCustomerId());
        assertEquals(2, result.getProducts().size());
    }
}