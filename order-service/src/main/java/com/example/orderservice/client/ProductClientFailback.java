package com.example.orderservice.client;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.common.dto.ProductDto;

@Component
public class ProductClientFailback implements ProductClient {
    
    @Override
    public List<ProductDto> getProducts() {
        return Collections.singletonList(
            ProductDto.builder()
                .id("failback")
                .name("상품 정보를 불러올 수 없습니다")
                .price(0)
                .build()
        );
    }

}
