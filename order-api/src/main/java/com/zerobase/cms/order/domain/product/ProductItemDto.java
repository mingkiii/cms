package com.zerobase.cms.order.domain.product;

import com.zerobase.cms.order.domain.model.ProductItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemDto {
    private Long id;
    private String name;
    private Integer price;
    private Integer count;

    public static ProductItemDto from(ProductItem item) {
        return ProductItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .price(item.getPrice())
            .count(item.getCount())
            .build();
    }
}
