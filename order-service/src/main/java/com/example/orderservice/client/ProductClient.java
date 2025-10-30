package com.example.orderservice.client;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.common.dto.ProductDto;

@FeignClient(
   name = "product-service",
   fallback = ProductClientFailback.class
)
public interface ProductClient {
   @GetMapping("/products")
   List<ProductDto> getProducts();
}