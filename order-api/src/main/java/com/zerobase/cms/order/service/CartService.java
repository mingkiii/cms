package com.zerobase.cms.order.service;

import com.zerobase.cms.order.client.RedisClient;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

    private final RedisClient redisClient;

    public Cart getCart(Long customerId) {
        Cart cart = redisClient.get(customerId, Cart.class);
        return cart != null ? cart : new Cart();
    }

    public Cart putCart(Long customerId, Cart cart) {
        redisClient.put(customerId, cart);
        return cart;
    }

    public Cart addCart(Long customerId, AddProductCartForm form) {
        Cart cart = redisClient.get(customerId, Cart.class);
        if (cart == null) {
            cart = new Cart();
            cart.setCustomerId(customerId);
        }

        Optional<Cart.Product> productOptional =
            cart.getProducts().stream()
            .filter(p -> p.getId().equals(form.getId()))
            .findFirst();

        if (productOptional.isPresent()) { // 카트에 이미 같은 상품이 있는 경우
            Cart.Product redisProduct = productOptional.get();
            List<Cart.ProductItem> items =
                form.getItems().stream()
                    .map(Cart.ProductItem::from)
                    .collect(Collectors.toList());
            Map<Long, Cart.ProductItem> redisItemMap =
                redisProduct.getItems().stream()
                    .collect(Collectors.toMap(Cart.ProductItem::getId, it -> it));

            if (!redisProduct.getName().equals(form.getName())) { // 카트에 있는 상품의 정보 변경된 경우
                cart.addMessage(redisProduct.getName() + "의 정보가 변동되었습니다. 확인 부탁드립니다.");
            }

            for (Cart.ProductItem item : items) { // 카트에 있는 상품의 아이템 검사
                Cart.ProductItem redisItem = redisItemMap.get(item.getId());

                if (redisItem == null) {
                    redisProduct.getItems().add(item);
                } else {
                    if (!redisItem.getPrice().equals(item.getPrice())) {
                        cart.addMessage(
                            redisProduct.getName() + item.getName() + "의 가격이 변동되었습니다.");
                    }
                    // 카트에 같은 상품 아이템 수량 + 추가하는 상품 아이템 수량 == 카트 추가 완료
                    redisItem.setCount(redisItem.getCount() + item.getCount());
                }
            }
        } else { // 카트에 같은 상품이 없는 경우 상품 추가
            Cart.Product product = Cart.Product.from(form);
            cart.getProducts().add(product);
        }
        redisClient.put(customerId, cart);
        return cart;
    }


}
