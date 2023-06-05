package com.zerobase.cms.order.domain.product;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductForm {

    private String name;
    private String description;
    private List<AddProductItemForm> items;
}
