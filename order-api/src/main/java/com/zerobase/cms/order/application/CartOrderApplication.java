package com.zerobase.cms.order.application;

import static com.zerobase.cms.order.exception.ErrorCode.CART_IS_EMPTY;
import static com.zerobase.cms.order.exception.ErrorCode.ORDER_FAIL_CHECK_CART;
import static com.zerobase.cms.order.exception.ErrorCode.ORDER_FAIL_NOT_ENOUGH_MONEY;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.client.UserClient;
import com.zerobase.cms.order.client.mailgun.MailgunClient;
import com.zerobase.cms.order.client.mailgun.SendMailForm;
import com.zerobase.cms.order.client.user.ChangeBalanceForm;
import com.zerobase.cms.order.client.user.CustomerDto;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.domain.redis.Cart.Product;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.service.ProductItemService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartOrderApplication {

    private final CartApplication cartApplication;
    private final UserClient userClient;
    private final ProductItemService productItemService;
    private final MailgunClient mailgunClient;
    private final RedisClient redisClient;

    @Transactional
    public void order(String token, Cart cart) {
        Cart orderCart = cartApplication.refreshCart(cart);
        if (orderCart.getMessages().size() > 0) {
            throw new CustomException(ORDER_FAIL_CHECK_CART);
        }
        if (orderCart.getProducts().isEmpty()) {
            throw new CustomException(CART_IS_EMPTY);
        }
        CustomerDto customerDto = userClient.getCustomerInfo(token).getBody();

        int totalPrice = getTotalPrice(orderCart);
        if (customerDto.getBalance() < totalPrice) {
            throw new CustomException(ORDER_FAIL_NOT_ENOUGH_MONEY);
        }
        userClient.changeBalance(token,
            ChangeBalanceForm.builder()
                .from("USER")
                .message("Order")
                .money(-totalPrice)
                .build());

        for (Cart.Product product : orderCart.getProducts()) {
            for (Cart.ProductItem cartItem : product.getItems()) {
                ProductItem productItem =
                    productItemService.getProductItem(cartItem.getId());
                productItem.setCount(productItem.getCount() - cartItem.getCount());
            }
        }
        // 주문이 처리된 후에 주문된 상품을 카트에서 제거
        removeOrderedProductsFromCart(orderCart);
        redisClient.put(customerDto.getId(), orderCart);

        // 주문 내역 이메일 발송
        sendOrderResultEmail(orderCart, customerDto.getEmail());
    }

    private Integer getTotalPrice(Cart cart) {
        return cart.getProducts().stream()
            .flatMapToInt(product -> product.getItems().stream()
                .flatMapToInt(productItem -> IntStream.of(
                        productItem.getPrice() * productItem.getCount()
                    )
                )
            )
            .sum();
    }

    private void removeOrderedProductsFromCart(Cart cart) {
        List<Product> productsToRemove = new ArrayList<>();

        for (Cart.Product product : cart.getProducts()) {
            List<Cart.ProductItem> itemsToRemove = new ArrayList<>();

            for (Cart.ProductItem cartItem : product.getItems()) {
                itemsToRemove.add(cartItem);
            }
            product.getItems().removeAll(itemsToRemove);
            if (product.getItems().isEmpty()) {
                productsToRemove.add(product);
            }
        }
        cart.getProducts().removeAll(productsToRemove);
    }

    private void sendOrderResultEmail(Cart cart, String customerEmail) {

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Thank you for your order!\n\n");
        emailContent.append("Order Details:\n");

        for (Cart.Product product : cart.getProducts()) {
            emailContent.append("Product: ").append(product.getName()).append("\n");

            for (Cart.ProductItem item : product.getItems()) {
                emailContent.append(" - ProductItem: ").append(item.getName()).append("\n");
                emailContent.append("   Price: ").append(item.getPrice()).append("\n");
                emailContent.append("   Quantity: ").append(item.getCount()).append("\n");
                emailContent.append("\n");
            }

            emailContent.append("\n");
        }

        SendMailForm sendMailForm = SendMailForm.builder()
            .from("test@mailgun.org")
            .to(customerEmail)
            .subject("Order Result")
            .text(emailContent.toString())
            .build();

        mailgunClient.sendEmail(sendMailForm);
    }
}
