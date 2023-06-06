package com.zerobase.cms.order.service;

import static com.zerobase.cms.order.exception.ErrorCode.NOT_FOUND_PRODUCT;

import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.repository.ProductRepository;
import com.zerobase.cms.order.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductRepository productRepository;

    public List<Product> searchByName(String name) {
        return productRepository.searchByName(name);
    }

    public Product getByProductId(Long productId) {
        return productRepository.findWithProductItemsById(productId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_PRODUCT));
    }
}
