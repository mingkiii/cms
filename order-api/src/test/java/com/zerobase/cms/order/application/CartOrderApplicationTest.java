package com.zerobase.cms.order.application;

import static com.zerobase.cms.order.exception.ErrorCode.ORDER_FAIL_CHECK_CART;
import static com.zerobase.cms.order.exception.ErrorCode.ORDER_FAIL_NOT_ENOUGH_MONEY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerobase.cms.order.client.UserClient;
import com.zerobase.cms.order.client.user.CustomerDto;
import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.service.ProductItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class CartOrderApplicationTest {

    @Mock
    private CartApplication cartApplication;
    @Mock
    private UserClient userClient;
    @Mock
    private ProductItemService productItemService;

    @InjectMocks
    private CartOrderApplication cartOrderApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cartOrderApplication = new CartOrderApplication(cartApplication, userClient, productItemService);
    }

    @Test
    @DisplayName("order-success")
    void order_SuccessfulOrder() {
        // when
        String token = "test-token";
        Long sellerId = 2L;

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Product 1");
        product1.setSellerId(sellerId);
        product1.setDescription("Happy Shopping!");
        ProductItem item1 = new ProductItem();
        item1.setId(10L);
        item1.setName("Option 1");
        item1.setPrice(1000);
        item1.setCount(10);
        product1.getProductItems().add(item1);
        int orderBeforeCount = item1.getCount();

        Long customerId = 1L;
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(10L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(1000);
        cartProductItem.setCount(6);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);
        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(1L);
        customerDto.setBalance(10000);

        when(cartApplication.refreshCart(cart)).thenReturn(cart);
        when(userClient.getCustomerInfo(token)).thenReturn(ResponseEntity.ok(customerDto));
        when(productItemService.getProductItem(cartProductItem.getId())).thenReturn(item1);

        // when
        cartOrderApplication.order(token, cart);
        int totalPrice = calculateTotalPrice(cart);
        int orderAfterCount = item1.getCount();

        // then
        verify(userClient).changeBalance(eq(token), argThat(form ->
            form.getFrom().equals("USER") &&
                form.getMessage().equals("Order") &&
                form.getMoney() == -totalPrice // totalPrice: (6 * 1000) = 6000
        ));

        assertDoesNotThrow(() -> cartOrderApplication.order(token, cart));
        assertEquals(orderAfterCount, orderBeforeCount - cartProductItem.getCount());
        assertEquals(6000, totalPrice);

    }

    @Test
    @DisplayName("order-fail_주문 상품 정보 변경됨")
    void order_error_refreshCart() {
        //given
        String token = "test-token";
        Long customerId = 1L;
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(10L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(1000);
        cartProductItem.setCount(6);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);

        Cart orderCart = new Cart();
        orderCart.setCustomerId(customerId);
        orderCart.getMessages().add("가격과 수량이 변동되었습니다.");
        Cart.Product cartProduct2 = new Cart.Product();
        cartProduct2.setId(1L);
        cartProduct2.setName("Product 1");
        Cart.ProductItem cartProductItem2 = new Cart.ProductItem();
        cartProductItem2.setId(10L);
        cartProductItem2.setName("Option 1");
        cartProductItem2.setPrice(5000);
        cartProductItem2.setCount(3);
        cartProduct2.getItems().add(cartProductItem2);
        orderCart.getProducts().add(cartProduct2);

        when(cartApplication.refreshCart(cart)).thenReturn(orderCart);
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> cartOrderApplication.order(token, cart));

        //then
        assertEquals(ORDER_FAIL_CHECK_CART, exception.getErrorCode());
        assertNotEquals(cart, orderCart);
    }

    @Test
    @DisplayName("order-fail_결제 금액 부족")
    void order_Not_enough_balance() {
        //given
        String token = "test-token";
        Long customerId = 1L;
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        Cart.Product cartProduct = new Cart.Product();
        cartProduct.setId(1L);
        cartProduct.setName("Product 1");
        Cart.ProductItem cartProductItem = new Cart.ProductItem();
        cartProductItem.setId(10L);
        cartProductItem.setName("Option 1");
        cartProductItem.setPrice(1000);
        cartProductItem.setCount(6);
        cartProduct.getItems().add(cartProductItem);
        cart.getProducts().add(cartProduct);

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(1L);
        customerDto.setBalance(3000);

        when(cartApplication.refreshCart(cart)).thenReturn(cart);
        when(userClient.getCustomerInfo(token)).thenReturn(ResponseEntity.ok(customerDto));

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> cartOrderApplication.order(token, cart));
        int totalPrice = calculateTotalPrice(cart);
        //then
        assertTrue(totalPrice > customerDto.getBalance());
        assertEquals(ORDER_FAIL_NOT_ENOUGH_MONEY, exception.getErrorCode());
    }

    private int calculateTotalPrice(Cart cart) {
        int totalPrice = 0;
        for (Cart.Product product : cart.getProducts()) {
            for (Cart.ProductItem cartItem : product.getItems()) {
                totalPrice += cartItem.getPrice() * cartItem.getCount();
            }
        }
        return totalPrice;
    }
}