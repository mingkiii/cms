package com.zerobase.cms.order.service;

import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_ITEM;
import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_PRODUCT;
import static com.zerobase.cms.order.exception.ErrorCode.SAME_ITEM_NAME;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.ProductItem;
import com.zerobase.cms.order.domain.product.AddProductItemForm;
import com.zerobase.cms.order.domain.product.UpdateProductItemForm;
import com.zerobase.cms.order.domain.repository.ProductItemRepository;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import com.zerobase.cms.order.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductItemService {
    private final ProductRepository productRepository;
    private final ProductItemRepository productItemRepository;

    @Transactional
    public Product addProductItem(Long sellerId, AddProductItemForm form) {
        Product product =
            productRepository.findBySellerIdAndId(sellerId, form.getProductId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));
        if (product.getProductItems().stream()
            .anyMatch(item -> item.getName().equals(form.getName()))) {
            throw new CustomException(SAME_ITEM_NAME);
        }

        ProductItem productItem = ProductItem.of(sellerId, form);
        product.getProductItems().add(productItem);

        return product;
    }

    @Transactional
    public ProductItem updateProductItem(Long sellerId, UpdateProductItemForm form) {
        ProductItem productItem = productItemRepository.findById(form.getId())
            .filter(pi -> pi.getSellerId().equals(sellerId))
            .orElseThrow(() -> new CustomException(NOT_FOUND_ITEM));
        productItem.setName(form.getName());
        productItem.setCount(form.getCount());
        productItem.setPrice(form.getPrice());

        return productItem;
    }

    @Transactional
    public void deleteProductItem(Long sellerId, Long itemId) {
        ProductItem productItem = productItemRepository.findById(itemId)
            .filter(pi -> pi.getSellerId().equals(sellerId))
            .orElseThrow(() -> new CustomException(NOT_FOUND_ITEM));
        productItemRepository.delete(productItem);
    }
}
