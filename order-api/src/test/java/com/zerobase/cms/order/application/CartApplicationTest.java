package com.zerobase.cms.order.application;

import static com.zerobase.cms.order.exception.ErrorCode.ITEM_COUNT_NOT_ENOUGH;
import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_PRODUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.cms.order.service.ProductSearchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        ProductItem productItem = ProductItem.builder()
            .id(1L)
            .name("Item 1")
            .price(10000)
            .count(10)
            .build();
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

    @Test
    @DisplayName("getCart - Product Removed")
    void testGetCart_ProductRemoved() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        cart.getProducts().add(cartProduct);

        when(cartService.getCart(customerId)).thenReturn(cart);
        // when
        Cart result = cartApplication.getCart(customerId);

        // then
        assertNotNull(result);
        assertTrue(result.getProducts().isEmpty());
        assertEquals(1, result.getMessages().size());
        assertEquals("Product 1 상품이 삭제되었습니다.", result.getMessages().get(0));
    }

    @Test
    @DisplayName("getCart - All Options Removed")
    void testGetCart_AllOptionsRemoved() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(1L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(10000);
        cartProductItem.setCount(3);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);

        when(cartService.getCart(customerId)).thenReturn(cart);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        product.setProductItems(new ArrayList<>());
        when(productSearchService.getByProductId(cartProduct.getId())).thenReturn(product);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(productSearchService.getListByProductIds(anyList())).thenReturn(productList);

        // when
        Cart result = cartApplication.getCart(customerId);
        System.out.println(result.getMessages());

        // then
        assertNotNull(result);
        assertTrue(result.getProducts().isEmpty());
        assertEquals(1, result.getMessages().size());
        assertEquals("Product 1 상품의 옵션이 모두 없어져 구매가 불가능합니다.", result.getMessages().get(0));
    }

    @Test
    @DisplayName("getCart - Option Removed")
    void testGetCart_OptionRemoved() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(1L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(10000);
        cartProductItem.setCount(3);
        cartProduct.getItems().add(cartProductItem);
        Cart.ProductItem cartProductItem2 = new Cart.ProductItem();
        cartProductItem2.setId(2L);
        cartProductItem2.setName("Option 2");
        cartProductItem2.setPrice(10000);
        cartProductItem2.setCount(3);
        cartProduct.getItems().add(cartProductItem2);
        cart.getProducts().add(cartProduct);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setName("Option 1");
        item.setPrice(10000);
        item.setCount(3);
        product.getProductItems().add(item);

        when(cartService.getCart(customerId)).thenReturn(cart);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(productSearchService.getListByProductIds(anyList())).thenReturn(productList);
        when(productSearchService.getByProductId(cartProduct.getId())).thenReturn(product);

        // when
        Cart result = cartApplication.getCart(customerId);

        // then
        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        assertEquals(1,result.getProducts().get(0).getItems().size());
        assertEquals(1, result.getMessages().size());
        assertTrue(result.getMessages().get(0).contains("Option 2 옵션이 삭제되었습니다."));
    }

    @Test
    @DisplayName("getCart - Price Changed")
    void testGetCart_PriceChanged() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(1L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(10000);
        cartProductItem.setCount(3);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setName("Option 1");
        item.setPrice(20000);
        item.setCount(3);
        product.getProductItems().add(item);

        when(cartService.getCart(customerId)).thenReturn(cart);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(productSearchService.getListByProductIds(anyList())).thenReturn(productList);
        when(productSearchService.getByProductId(cartProduct.getId())).thenReturn(product);

        // when
        Cart result = cartApplication.getCart(customerId);

        // then
        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        assertEquals(1,result.getProducts().get(0).getItems().size());
        assertEquals(20000,result.getProducts().get(0).getItems().get(0).getPrice());
        assertEquals(1, result.getMessages().size());
        assertTrue(result.getMessages().get(0).contains("Option 1 가격이 변동되었습니다."));
    }

    @Test
    @DisplayName("getCart - Count Not Enough")
    void testGetCart_CountNotEnough() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(1L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(10000);
        cartProductItem.setCount(6);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setName("Option 1");
        item.setPrice(10000);
        item.setCount(5);
        product.getProductItems().add(item);

        when(cartService.getCart(customerId)).thenReturn(cart);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(productSearchService.getListByProductIds(anyList())).thenReturn(productList);
        when(productSearchService.getByProductId(cartProduct.getId())).thenReturn(product);

        // when
        Cart result = cartApplication.getCart(customerId);

        // then
        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        assertEquals(1,result.getProducts().get(0).getItems().size());
        assertEquals(5,result.getProducts().get(0).getItems().get(0).getCount());
        assertEquals(1, result.getMessages().size());
        assertTrue(result.getMessages().get(0).contains("Option 1 수량이 부족하여 구매 가능한 최대치로 변동되었습니다."));
    }
    @Test
    @DisplayName("getCart - Price Changed And Count Not Enough")
    void testGetCart_PriceChangedAndCountNotEnough() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(1L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(10000);
        cartProductItem.setCount(6);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);
        Cart.Product cartProduct2 = new Cart.Product();
        cartProduct2.setId(2L);
        cartProduct2.setName("Product 2");
        cart.getProducts().add(cartProduct2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setName("Option 1");
        item.setPrice(20000);
        item.setCount(3);
        product.getProductItems().add(item);

        when(cartService.getCart(customerId)).thenReturn(cart);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(productSearchService.getListByProductIds(anyList())).thenReturn(productList);
        when(productSearchService.getByProductId(cartProduct.getId())).thenReturn(product);

        // when
        Cart result = cartApplication.getCart(customerId);

        System.out.println(result.getMessages());
        // then
        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        assertEquals(1,result.getProducts().get(0).getItems().size());
        assertEquals(3,result.getProducts().get(0).getItems().get(0).getCount());
        assertEquals(20000,result.getProducts().get(0).getItems().get(0).getPrice());
        assertEquals(2, result.getMessages().size());
        assertTrue(result.getMessages().get(0).contains("Option 1 가격 변동, 수량이 부족하여 구매 가능한 최대치로 변동되었습니다."));
        assertEquals(result.getMessages().get(1),"Product 2 상품이 삭제되었습니다.");
    }

    @Test
    @DisplayName("getCart - Not Changed")
    void testGetCart_NotChanged() {
        // given
        Long customerId = 1L;

        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(1L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(10000);
        cartProductItem.setCount(6);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);

        Product product = new Product();
        product.setId(1L);
        product.setName("Product 1");
        ProductItem item = new ProductItem();
        item.setId(1L);
        item.setName("Option 1");
        item.setPrice(10000);
        item.setCount(10);
        product.getProductItems().add(item);

        when(cartService.getCart(customerId)).thenReturn(cart);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        when(productSearchService.getListByProductIds(anyList())).thenReturn(productList);
        when(productSearchService.getByProductId(cartProduct.getId())).thenReturn(product);

        // when
        Cart result = cartApplication.getCart(customerId);

        System.out.println(result.getMessages());
        // then
        assertNotNull(result);
        assertEquals(result, cart);
        assertEquals(0, result.getMessages().size());
    }
}